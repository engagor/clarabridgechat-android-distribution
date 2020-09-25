/* The MIT License
 *
 * Copyright (c) 2011 Paul Crawford
 * Copyright (c) 2013 Saul Howard
 *
 * Ported from Objective-C to Java by Saul Howard <saulpower1@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.saulpower.fayeclient;

import android.annotation.SuppressLint;
import android.os.Handler;

import com.saulpower.fayeclient.WebSocketClient.Listener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.Date;
import java.util.Iterator;

import com.clarabridge.core.AuthenticationError;
import com.clarabridge.core.Logger;

public class FayeClient implements Listener {

    private static final String TAG = "FayeClient";

    private static final String HANDSHAKE_CHANNEL = "/meta/handshake";
    private static final String CONNECT_CHANNEL = "/meta/connect";
    private static final String DISCONNECT_CHANNEL = "/meta/disconnect";
    private static final String SUBSCRIBE_CHANNEL = "/meta/subscribe";
    private static final String UNSUBSCRIBE_CHANNEL = "/meta/unsubscribe";

    private static final String KEY_CHANNEL = "channel";
    private static final String KEY_SUCCESS = "successful";
    private static final String KEY_CLIENT_ID = "clientId";
    private static final String KEY_VERSION = "version";
    private static final String KEY_MIN_VERSION = "minimumVersion";
    private static final String KEY_SUBSCRIPTION = "subscription";
    private static final String KEY_SUP_CONN_TYPES = "supportedConnectionTypes";
    private static final String KEY_CONN_TYPE = "connectionType";
    private static final String KEY_DATA = "data";
    private static final String KEY_ID = "id";
    private static final String KEY_EXT = "ext";
    private static final String KEY_ERROR = "error";

    private static final String VALUE_VERSION = "1.0";
    private static final String VALUE_MIN_VERSION = "1.0beta";
    private static final String VALUE_CONN_TYPE = "websocket";

    private static final String ERROR_UNKNOWN_CLIENT = "Unknown client";

    private long retryInterval = 10000L;
    private int maxConnectionAttempts = 3;

    private WebSocketClient client;
    private boolean connected = false;
    private int connectionAttempts = 0;

    private URI fayeUrl;
    private String fayeClientId;
    private String activeSubChannel;

    private JSONObject connectionExtension;

    private boolean running = false;
    private boolean reconnecting = false;

    private Handler handler;
    private Runnable connectionMonitor = new Runnable() {

        @Override
        public void run() {

            if (!connected) {

                openWebSocketConnection();

                if (connectionAttempts < maxConnectionAttempts) {
                    connectionAttempts++;
                    getHandler().postDelayed(this, retryInterval);
                }

            } else {

                getHandler().removeCallbacks(this);
                running = false;
                connectionAttempts = 0;
                reconnecting = false;
            }
        }
    };

    private FayeListener fayeListener;

    /**
     * Register a callback to be invoked for specific Faye client
     * events
     *
     * @param fayeListener The callback that will run
     */
    public void setFayeListener(FayeListener fayeListener) {
        this.fayeListener = fayeListener;
    }

    private Handler getHandler() {

        return handler;
    }

    /**
     * Creates a new Faye Client for communicating with a Faye server at the
     * provided URL and the specified channel.
     *
     * @param fayeUrl The URL of the FayeServer
     * @param channel The channel to subscribe to
     */
    public FayeClient(Handler handler, URI fayeUrl, String channel, int maxConnectionAttempts, long retryInterval) {
        this.handler = handler;
        this.fayeUrl = fayeUrl;
        activeSubChannel = channel;
        this.maxConnectionAttempts = maxConnectionAttempts;
        this.retryInterval = retryInterval;
    }

    /**
     * Connect to a server using the extension authentication object
     *
     * @param extension Bayeux extension authentication that exchanges authentication
     *                  credentials and tokens within Bayeux messages ext fields
     */
    public void connectToServer(JSONObject extension) {
        connectionAttempts = 0;
        connectionExtension = extension;
        openWebSocketConnection();
    }

    public void disconnectFromServer() {
        disconnect();
        connected = false;
        getHandler().removeCallbacks(connectionMonitor);
    }

    /**
     * Sends events on a channel by sending an event message
     *
     * @param json JSON object containing message to be sent to server
     */
    public void sendMessage(JSONObject json) {
        publish(json, connectionExtension);
    }

    private void openWebSocketConnection() {
        if (client != null) {
            client.disconnect();
            client = null;
        }

        client = new WebSocketClient(getHandler(), fayeUrl, this);
        client.connect();
    }

    public void closeWebSocketConnection() {
        Logger.d(TAG, "socket disconnected");

        if (client == null) {
            return;
        }

        client.disconnect();
    }

    public void resetWebSocketConnection() {
        if (!reconnecting) {

            reconnecting = true;
            connected = false;

            if (!running) {
                getHandler().post(connectionMonitor);
            }
        }
    }

    /**
     * Initiates a connection negotiation by sending a message to the
     * "/meta/handshake" channel.
     * <p>
     * Example JSON
     * {
     * KEY_CHANNEL: "/meta/handshake",
     * KEY_VERSION: "1.0",
     * KEY_MIN_VERSION: "1.0beta",
     * KEY_SUP_CONN_TYPES:
     * ["long-polling", "callback-polling", "iframe", "websocket]
     * }
     */
    private void handshake() {

        try {

            JSONArray connTypes = new JSONArray();
            connTypes.put("long-polling");
            connTypes.put("callback-polling");
            connTypes.put("iframe");
            connTypes.put("websocket");

            JSONObject json = new JSONObject();
            json.put(KEY_CHANNEL, HANDSHAKE_CHANNEL);
            json.put(KEY_VERSION, VALUE_VERSION);
            json.put(KEY_MIN_VERSION, VALUE_MIN_VERSION);
            json.put(KEY_SUP_CONN_TYPES, connTypes);

            client.send(json.toString());

        } catch (JSONException ex) {
            Logger.e(TAG, "Handshake Failed", ex);
        }
    }

    /**
     * After a Bayeux client has discovered the server's capabilities
     * with a handshake exchange, a connection is established by
     * sending a message to the "/meta/connect" channel.
     * <p>
     * Example JSON
     * {
     * KEY_CHANNEL: "/meta/connect",
     * KEY_CLIENT_ID: "Un1q31d3nt1f13r",
     * KEY_CONN_TYPES: "long-polling"
     * }
     */
    public void connect() {

        try {

            JSONObject json = new JSONObject();
            json.put(KEY_CHANNEL, CONNECT_CHANNEL);
            json.put(KEY_CLIENT_ID, fayeClientId);
            json.put(KEY_CONN_TYPE, VALUE_CONN_TYPE);

            client.send(json.toString());

        } catch (JSONException ex) {
            Logger.e(TAG, "Handshake Failed", ex);
        }
    }

    /**
     * Cease operation by sending a request to the "/meta/disconnect"
     * channel for the server to remove any client-related state.
     * <p>
     * Example JSON
     * {
     * KEY_CHANNEL: "/meta/disconnect",
     * KEY_CLIENT_ID: "Un1q31d3nt1f13r"
     * }
     */
    public void disconnect() {
        Logger.i(TAG, "socket disconnected");

        if (client == null) {
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put(KEY_CHANNEL, DISCONNECT_CHANNEL);
            json.put(KEY_CLIENT_ID, fayeClientId);

            client.send(json.toString());
            client.disconnect();

        } catch (JSONException ex) {
            Logger.e(TAG, "Handshake Failed", ex);
        }
    }


    /**
     * Register interest in a channel and request that messages published to
     * that channel are delivered.
     * <p>
     * Example JSON
     * {
     * KEY_CHANNEL: "/meta/subscribe",
     * KEY_CLIENT_ID: "Un1q31d3nt1f13r",
     * KEY_SUBSCRIPTION: "/foo/ **"
     * }
     */
    public void subscribe() {

        try {

            JSONObject json = new JSONObject();
            json.put(KEY_CHANNEL, SUBSCRIBE_CHANNEL);
            json.put(KEY_CLIENT_ID, fayeClientId);
            json.put(KEY_SUBSCRIPTION, activeSubChannel);

            if (null != connectionExtension) {
                // XXX Put the extensions on the object instead of inside KEY_EXT
                final Iterator<String> it = connectionExtension.keys();

                while (it.hasNext()) {
                    final String key = it.next();

                    json.put(key, connectionExtension.get(key));
                }
                // - XXX

                json.put(KEY_EXT, connectionExtension);
            }

            client.send(json.toString());

        } catch (JSONException ex) {
            Logger.e(TAG, "Handshake Failed", ex);
        }
    }


    /**
     * Send unsubscribe messages to cancel interest in channel and to request
     * that messages published to that channel are not delivered.
     * <p>
     * Example JSON
     * {
     * KEY_CHANNEL: "/meta/unsubscribe",
     * KEY_CLIENT_ID: "Un1q31d3nt1f13r",
     * KEY_SUBSCRIPTION: "/foo/**"
     * }
     */
    public void unsubscribe() {

        try {

            JSONObject json = new JSONObject();
            json.put(KEY_CHANNEL, UNSUBSCRIBE_CHANNEL);
            json.put(KEY_CLIENT_ID, fayeClientId);
            json.put(KEY_SUBSCRIPTION, activeSubChannel);

            client.send(json.toString());

        } catch (JSONException ex) {
            Logger.e(TAG, "Handshake Failed", ex);
        }
    }

    /**
     * Publish events on a channel by sending an event message
     * <p>
     * Example JSON
     * {
     * KEY_CHANNEL:    "/some/channel",
     * KEY_CLIENT_ID:    "Un1q31d3nt1f13r",
     * KEY_DATA:        "some application string or JSON encoded object",
     * KEY_ID:        "some unique message id"
     * }
     *
     * @param message   JSON object containing message to be sent to server
     * @param extension Bayeux extension authentication that exchanges authentication
     *                  credentials and tokens within Bayeux messages ext fields
     */
    public void publish(JSONObject message, JSONObject extension) {

        String channel = activeSubChannel;
        long number = (new Date()).getTime();
        String messageId = String.format("msg_%d_%d", number, 1);

        try {

            JSONObject json = new JSONObject();
            json.put(KEY_CHANNEL, channel);
            json.put(KEY_CLIENT_ID, fayeClientId);
            json.put(KEY_DATA, message);
            json.put(KEY_ID, messageId);

            if (null != extension) {
                json.put(KEY_EXT, extension);
            }

            client.send(json.toString());

        } catch (JSONException ex) {
            Logger.e(TAG, "Handshake Failed", ex);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.saulpower.fayeclient.WebSocketClient.Listener#onConnect()
     */
    @Override
    public void onConnect() {

        connected = true;
        reconnecting = false;
        handshake();
    }

    /*
     * (non-Javadoc)
     * @see com.saulpower.fayeclient.WebSocketClient.Listener#onMessage(java.lang.String)
     */
    @Override
    public void onMessage(String message) {

        parseFayeMessage(message);
    }

    /*
     * (non-Javadoc)
     * @see com.saulpower.fayeclient.WebSocketClient.Listener#onMessage(byte[])
     */
    @Override
    public void onMessage(byte[] data) {
        Logger.i(TAG, "Data message");
    }

    /*
     * (non-Javadoc)
     * @see com.saulpower.fayeclient.WebSocketClient.Listener#onDisconnect(int, java.lang.String)
     */
    @Override
    public void onDisconnect(int code, String reason) {

        connected = false;

        if (fayeListener != null) {
            fayeListener.disconnectedFromServer();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.saulpower.fayeclient.WebSocketClient.Listener#onError(java.lang.Exception)
     */
    @Override
    public void onError(Exception error) {
        //check if caused by unauthorized
        if (error instanceof UnauthorizedException) {
            Logger.d(TAG, "UnauthorizedException " + error.getMessage(), error);
            if (fayeListener != null) {
                final AuthenticationError authenticationError =
                        ((UnauthorizedException) error).getAuthenticationError();
                fayeListener.onAuthenticationError(authenticationError);
            }
            return;
        }

        if (!connected) {
            return;
        }

        Logger.d(TAG, "resetWebSocketConnection " + error.getMessage(), error);

        if (fayeListener != null) {
            fayeListener.disconnectedFromServer();
        }

        resetWebSocketConnection();
    }

    /**
     * Parse the Faye message and call the appropriate
     * listener method.
     *
     * @param message A json string from the Faye server
     */
    private void parseFayeMessage(String message) {

        try {

            JSONArray messageArray = new JSONArray(message);

            for (int i = 0; i < messageArray.length(); i++) {

                JSONObject fayeMessage = messageArray.optJSONObject(i);

                if (fayeMessage == null) {
                    continue;
                }

                String channel = fayeMessage.optString(KEY_CHANNEL);
                boolean success = fayeMessage.optBoolean(KEY_SUCCESS);

                if (channel.equals(HANDSHAKE_CHANNEL)) {

                    if (success) {

                        fayeClientId = fayeMessage.optString(KEY_CLIENT_ID);

                        if (fayeListener != null) {
                            fayeListener.connectedToServer();
                        }

                        connect();
                        subscribe();

                    }

                    return;
                }

                if (channel.equals(CONNECT_CHANNEL)) {

                    if (success) {

                        connected = true;
                        connect();

                    } else {
                        final String error = fayeMessage.optString(KEY_ERROR);
                        @SuppressLint("DefaultLocale")
                        final String unknownClientError = String.format(
                                "%d:%s:%s",
                                401,
                                fayeClientId,
                                ERROR_UNKNOWN_CLIENT
                        );

                        if (error != null && error.equals(unknownClientError)) {
                            onError(new Exception("Faye connect error: " + ERROR_UNKNOWN_CLIENT));

                            if (fayeListener != null) {
                                fayeListener.disconnectedFromServer();
                            }
                        }
                    }
                    return;
                }

                if (channel.equals(DISCONNECT_CHANNEL)) {

                    if (success) {

                        connected = false;
                        closeWebSocketConnection();

                        if (fayeListener != null) {
                            fayeListener.disconnectedFromServer();
                        }

                    }

                    return;
                }

                if (channel.equals(SUBSCRIBE_CHANNEL)) {

                    if (success) {

                        if (fayeListener != null) {
                            fayeListener.subscribedToChannel(fayeMessage.optString(KEY_SUBSCRIPTION));
                        }

                    }

                    return;
                }

                if (channel.equals(UNSUBSCRIBE_CHANNEL)) {
                    return;
                }

                if (isSubscribedToChannel(channel)) {

                    JSONObject data = fayeMessage.optJSONObject(KEY_DATA);

                    if (data != null && fayeListener != null) {
                        fayeListener.messageReceived(data);
                    }

                    return;
                }

            }

        } catch (JSONException ex) {
            Logger.e(TAG, "Could not parse faye message", ex);
        }
    }

    /**
     * Checks to see if we are subscribed to the passed in channel
     *
     * @param channel Name of channel to check
     * @return True if we are connected to the passed in channel, false
     * otherwise
     */
    private boolean isSubscribedToChannel(String channel) {

        boolean isSubscribed = false;

        if (activeSubChannel != null && activeSubChannel.length() > 0 && channel != null && channel.length() > 0) {

            String[] subscribedChannelSegments = activeSubChannel.split("/");
            String[] channelSegments = channel.split("/");

            int i = 0;
            isSubscribed = true;

            do {

                String s1 = subscribedChannelSegments[i];
                String s2 = (i < channelSegments.length ? channelSegments[i] : null);

                if (s2 == null) {
                    break;
                }

                if (!s2.equals(s1)) {

                    if (s1.equals("**")) {
                        break;
                    } else {
                        isSubscribed = false;
                    }
                }

                i++;

            } while (isSubscribed && i < subscribedChannelSegments.length);
        }

        return isSubscribed;
    }

    public interface FayeListener {
        void connectedToServer();

        void disconnectedFromServer();

        void onAuthenticationError(AuthenticationError authenticationError);

        void subscribedToChannel(String subscription);

        void subscriptionFailedWithError(String error);

        void messageReceived(JSONObject json);
    }
}
