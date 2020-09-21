package com.clarabridge.core.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public final class ProcessName {
    public static String getCurrentProcessName() throws IOException {
        BufferedReader cmdlineReader = null;

        try {
            FileInputStream fileInputStream = new FileInputStream("/proc/" + android.os.Process.myPid() + "/cmdline");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "iso-8859-1");
            cmdlineReader = new BufferedReader(inputStreamReader);

            int c;

            StringBuilder processName = new StringBuilder();

            while ((c = cmdlineReader.read()) > 0) {
                processName.append((char) c);
            }

            return processName.toString();
        } finally {
            if (cmdlineReader != null) {
                cmdlineReader.close();
            }
        }
    }
}
