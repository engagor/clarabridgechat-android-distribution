package com.saulpower.fayeclient;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.SocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.clarabridge.core.AuthenticationError;
import com.clarabridge.core.BuildConfig;
import com.clarabridge.core.Logger;
import com.clarabridge.core.http.Header;
import com.clarabridge.core.http.StatusLine;
import com.clarabridge.core.utils.BackgroundThread;

public class WebSocketClient {

    private static final String TAG = "WebSocketClient";
    private static final int SC_SWITCHING_PROTOCOLS = 101;
    private static TrustManager[] trustManagers;
    private final Object sendLock = new Object();
    private URI uri;
    private Listener listener;
    private Socket socket;
    private Thread thread;
    private Handler handler;
    private Handler uiHandler;
    private HybiParser parser;

    public WebSocketClient(Handler uiHandler, URI uri, Listener listener) {
        this.uiHandler = uiHandler;
        this.uri = uri;
        this.listener = listener;
        parser = new HybiParser(this);

        HandlerThread handlerThread = BackgroundThread.get();
        handler = new Handler(handlerThread.getLooper());
    }

    public static void setTrustManagers(TrustManager[] tm) {
        trustManagers = tm;
    }

    public Listener getListener() {
        return listener;
    }

    public void connect() {

        if (thread != null && thread.isAlive()) {
            return;
        }

        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    int port = (uri.getPort() != -1) ? uri.getPort() : (uri.getScheme().equals("wss") ? 443 : 80);

                    String path = TextUtils.isEmpty(uri.getPath()) ? "/" : uri.getPath();
                    if (!TextUtils.isEmpty(uri.getQuery())) {
                        path += "?" + uri.getQuery();
                    }

                    String originScheme = uri.getScheme().equals("wss") ? "https" : "http";
                    URI origin = new URI(originScheme, "//" + uri.getHost(), null);

                    SocketFactory factory = uri.getScheme().equals("wss")
                            ? getSslSocketFactory() : SocketFactory.getDefault();

                    socket = factory.createSocket(uri.getHost(), port);

                    String secret = createSecret();

                    PrintWriter out = new PrintWriter(socket.getOutputStream());
                    out.print("GET " + path + " HTTP/1.1\r\n");
                    out.print("Host: " + uri.getHost() + "\r\n");
                    out.print("Upgrade: websocket\r\n");
                    out.print("Connection: Upgrade\r\n");
                    out.print("Sec-WebSocket-Key: " + secret + "\r\n");
                    out.print("Sec-WebSocket-Version: 13\r\n");
                    out.print("Origin: " + origin.toString() + "\r\n");

                    out.print("\r\n");
                    out.flush();

                    HybiParser.HappyDataInputStream stream =
                            new HybiParser.HappyDataInputStream(socket.getInputStream());

                    // Read HTTP response status line.
                    StatusLine statusLine = StatusLine.parseStatusLine(readLine(stream));

                    if (statusLine == null) {
                        throw new Exception("Received no reply from server.");
                    } else if (statusLine.getCode() == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                        final AuthenticationError authenticationError = new AuthenticationError(
                                statusLine.getCode(), statusLine.getMessage());
                        throw new UnauthorizedException(authenticationError);
                    } else if (statusLine.getCode() != SC_SWITCHING_PROTOCOLS) {
                        throw new Exception("Unexpected code " + statusLine.getCode() + ", " + statusLine.getMessage());
                    }

                    // Read HTTP response headers.
                    String line;
                    boolean validated = false;

                    while (!TextUtils.isEmpty(line = readLine(stream))) {

                        Header header = Header.parseHeader(line);

                        // XXX Use equalsIgnoreCase() instead of equals()
                        if (header.getName().equalsIgnoreCase("Sec-WebSocket-Accept")) {

                            String expected = createSecretValidation(secret);
                            String actual = header.getValue();

                            if (!expected.equals(actual)) {
                                throw new Exception("Bad Sec-WebSocket-Accept header value.");
                            }

                            validated = true;
                        }
                    }

                    if (!validated) {
                        throw new Exception("No Sec-WebSocket-Accept header.");
                    }

                    listener.onConnect();

                    // Now decode websocket frames.
                    parser.start(stream);

                } catch (EOFException ex) {

                    onError(ex);

                } catch (SSLException ex) {

                    // Connection reset by peer
                    onError(ex);

                } catch (Exception ex) {
                    onError(ex);
                }
            }
        });
        thread.start();
    }

    private void onError(final Exception ex) {

        uiHandler.post(new Runnable() {

            @Override
            public void run() {
                if (socket != null) {
                    Logger.d(TAG, "WebSocket Error!", ex);

                    listener.onError(ex);
                }
            }
        });
    }

    private String createSecretValidation(String secret) {

        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        md.update((secret + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes());

        return Base64.encodeToString(md.digest(), Base64.DEFAULT).trim();
    }

    public void disconnect() {

        if (socket != null) {

            handler.post(new Runnable() {

                @Override
                public void run() {

                    try {

                        if (socket == null) {
                            return;
                        }

                        socket.close();
                        socket = null;

                        if (BuildConfig.DEBUG) {
                            Logger.d(TAG, "socket closed");
                        }

                    } catch (IOException ex) {
                        Logger.e(TAG, "Error while disconnecting", ex);
                        onError(ex);
                    }
                }
            });
        }
    }

    public void send(String data) {
        Logger.i(TAG, "Sending message: " + data);
        sendFrame(parser.frame(data));
    }

    public void send(byte[] data) {
        sendFrame(parser.frame(data));
    }

    // Can't use BufferedReader because it buffers past the HTTP data.
    private String readLine(HybiParser.HappyDataInputStream reader) throws IOException {

        int readChar = reader.read();

        if (readChar == -1) {
            return null;
        }

        StringBuilder string = new StringBuilder();

        while (readChar != '\n') {

            if (readChar != '\r') {
                string.append((char) readChar);
            }

            readChar = reader.read();

            if (readChar == -1) {
                return null;
            }
        }

        return string.toString();
    }

    private String createSecret() {

        byte[] nonce = new byte[16];

        for (int i = 0; i < 16; i++) {
            nonce[i] = (byte) (Math.random() * 256);
        }

        return Base64.encodeToString(nonce, Base64.DEFAULT).trim();
    }

    void sendFrame(final byte[] frame) {

        if (socket != null) {

            handler.post(new Runnable() {

                @Override
                public void run() {

                    try {

                        synchronized (sendLock) {
                            OutputStream outputStream = socket.getOutputStream();
                            outputStream.write(frame);
                            outputStream.flush();
                        }

                    } catch (Exception e) {
                        onError(e);
                    }
                }
            });
        }
    }

    private SSLSocketFactory getSslSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trustManagers, null);

        return context.getSocketFactory();
    }

    public interface Listener {
        void onConnect();

        void onMessage(String message);

        void onMessage(byte[] data);

        void onDisconnect(int code, String reason);

        void onError(Exception error);
    }
}
