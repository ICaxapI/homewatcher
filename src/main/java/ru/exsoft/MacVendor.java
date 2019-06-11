package ru.exsoft;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MacVendor {
    private static final String baseURL = "https://www.macvendorlookup.com/oui.php?mac=";
    private static final JSONParser parser = new JSONParser();

    public static String get(String macAddress) {
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL(baseURL + macAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            String resultF = result.toString();
            System.out.println(resultF);
            JSONObject object = (JSONObject) ((JSONArray) parser.parse(resultF)).get(0);
            return (String) object.get("company");
        } catch (Exception e) {
            return macAddress;
        }
    }
}
