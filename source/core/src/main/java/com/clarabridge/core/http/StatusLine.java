package com.clarabridge.core.http;

import java.net.ProtocolException;

public class StatusLine {

    private final int code;
    private final String message;

    public StatusLine(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static StatusLine parseStatusLine(String statusLine) throws ProtocolException {
        if (statusLine == null || statusLine.length() == 0) {
            return null;
        }

        // H T T P / 1 . 1   2 0 0   T e m p o r a r y   R e d i r e c t
        // 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0

        // Parse protocol like "HTTP/1.1" followed by a space.
        int codeStart;

        if (statusLine.startsWith("HTTP/1.")) {
            if (statusLine.length() < 9 || statusLine.charAt(8) != ' ') {
                throw new ProtocolException("Unexpected status line: " + statusLine);
            }
            int httpMinorVersion = statusLine.charAt(7) - '0';
            codeStart = 9;
            if (httpMinorVersion != 0 && httpMinorVersion != 1) {
                throw new ProtocolException("Unexpected status line: " + statusLine);
            }
        } else if (statusLine.startsWith("ICY ")) {
            // Shoutcast uses ICY instead of "HTTP/1.0".
            codeStart = 4;
        } else {
            throw new ProtocolException("Unexpected status line: " + statusLine);
        }

        // Parse response code like "200". Always 3 digits.
        if (statusLine.length() < codeStart + 3) {
            throw new ProtocolException("Unexpected status line: " + statusLine);
        }
        int code;

        try {
            code = Integer.parseInt(statusLine.substring(codeStart, codeStart + 3));
        } catch (NumberFormatException e) {
            throw new ProtocolException("Unexpected status line: " + statusLine);
        }

        // Parse an optional response message like "OK" or "Not Modified". If it
        // exists, it is separated from the response code by a space.
        String message = "";
        if (statusLine.length() > codeStart + 3) {
            if (statusLine.charAt(codeStart + 3) != ' ') {
                throw new ProtocolException("Unexpected status line: " + statusLine);
            }
            message = statusLine.substring(codeStart + 4);
        }

        return new StatusLine(code, message);
    }
}
