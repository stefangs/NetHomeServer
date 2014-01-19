/*
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.items.hue;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class JsonRestClient {

    public JSONObject get(String baseUrl, String resource, JSONObject argument) throws Exception {
        return new JSONObject(performRequest(baseUrl, resource, argument != null ?argument.toString() : "", "GET"));
    }

    public JSONArray put(String baseUrl, String resource, JSONObject argument) throws Exception {
        return new JSONArray(performRequest(baseUrl, resource, argument != null ?argument.toString() : "", "PUT"));
    }

    public JSONArray post(String baseUrl, String resource, JSONObject argument) throws Exception {
        return new JSONArray(performRequest(baseUrl, resource, argument != null ?argument.toString() : "", "POST"));
    }

    private String performRequest(String baseUrl, String resource, String body, String method) throws Exception {
        HttpURLConnection connection = null;
        DataOutputStream wr = null;
        BufferedReader rd = null;
        StringBuilder sb = new StringBuilder();
        String line;
        URL serverAddress;

        try {
            serverAddress = new URL(baseUrl + resource);

            //Set up the initial connection
            connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);
            if (body.length() > 0) {
                connection.setRequestProperty("Content-Length", "" + Integer.toString(body.getBytes().length));
                wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(body);
                wr.flush();
                wr.close();
            }
            connection.connect();
            if (connection.getResponseCode() < 200 || connection.getResponseCode() > 299) {
                throw new Exception("Bad HTTP response code: " + connection.getResponseCode());
            }
            rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while ((line = rd.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }

        } finally {
            if (rd != null) {
                rd.close();
            }
            if (wr != null) {
                wr.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return sb.toString();
    }
}
