package com.clarabridge.core.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashDigest {
    public static String sha1(final byte[]... input) {
        MessageDigest m;

        if (input == null) {
            return null;
        }

        try {
            m = MessageDigest.getInstance("SHA-1");
        } catch (final NoSuchAlgorithmException ex) {
            m = null;
        }

        if (m != null) {
            boolean updated = false;

            for (final byte[] it : input) {
                if (it != null) {
                    m.update(it);
                    updated = true;
                }
            }

            if (!updated) {
                return null;
            }

            return hexEncode(m.digest());
        }

        return null;
    }

    public static String sha1(final String... s) {
        String hash;

        if (s == null) {
            return null;
        }

        try {
            final byte[][] v = new byte[s.length][];

            for (int i = 0; i < s.length; i++) {
                if (s[i] != null) {
                    v[i] = s[i].getBytes("UTF-8");
                }
            }

            hash = sha1(v);
        } catch (final UnsupportedEncodingException ex) {
            hash = null;
        }

        return hash;
    }

    private static String hexEncode(final byte[] input) {
        final StringBuilder result = new StringBuilder();
        final char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        for (final byte b : input) {
            result.append(digits[(b & 0xf0) >> 4]);
            result.append(digits[(b & 0x0f)]);
        }

        return result.toString();
    }
}

