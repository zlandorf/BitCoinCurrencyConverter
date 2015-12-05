package com.frozendust.zlandorf.bitcoincurrencyconverter.services;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpService {

    public String request(String urlString) throws IOException {
        return request(urlString, "GET");
    }

    public String request(String urlString, String requestMethod) throws IOException {
        Log.i("HTTP_TASK", "Requesting " + urlString);
        InputStream inputStream = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) (new URL(urlString)).openConnection();
            connection.setRequestProperty("User-agent", "Android bitcoin app agent");
            connection.setRequestMethod(requestMethod);
            connection.connect();
            inputStream = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append("\n");
            }

            return response.toString();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
