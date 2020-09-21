package com.clarabridge.core.http;

import java.io.IOException;

public class Header {

    private final String name;
    private final String value;

    public Header(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public static Header parseHeader(String line) throws IOException {
        int colon = line.indexOf(':');

        if (colon == -1) {
            throw new IOException("Invalid header: " + line);
        }

        String name = line.substring(0, colon).trim();
        String value = line.substring(colon + 1).trim();

        return new Header(name, value);
    }
}
