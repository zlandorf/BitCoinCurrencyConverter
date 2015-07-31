package com.frozendust.zlandorf.bitcoincurrencyconverter.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zlandorf on 31/07/2015.
 */
public class HttpTask {

    /**
     *
     * @param urlString
     * @return
     * @throws IOException
     */
    public String request(String urlString) throws IOException {
        return request(urlString, "GET");
    }

    /**
     * @param urlString
     * @return
     * @throws IOException
     */
    public String request(String urlString, String requestMethod) throws IOException {
        InputStream inputStream = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) (new URL(urlString)).openConnection();
            connection.setRequestProperty("User-agent", "Android bitcoin app agent");
            connection.setRequestMethod(requestMethod);
            connection.connect();
            inputStream = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuffer response = new StringBuffer();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return response.toString();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
