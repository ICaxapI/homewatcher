package ru.exsoft.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GeneralUtils {

    public static String getOutput(String command) throws IOException {
        Process proc = Runtime.getRuntime().exec(command);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        String s;
        StringBuilder sb = new StringBuilder();
        while ((s = stdInput.readLine()) != null) {
            sb.append(s).append("\n");
            while ((s = stdError.readLine()) != null) {
                sb.append(s).append("\n");
            }
        }
        return sb.toString();
    }

}
