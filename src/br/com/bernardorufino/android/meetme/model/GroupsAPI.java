package br.com.bernardorufino.android.meetme.model;

import br.com.bernardorufino.android.meetme.helper.Helper;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class GroupsAPI {
    private static final String API_ADDRESS = "http://meetme-server.herokuapp.com/";
    private static final String CHARSET = "UTF-8";

    public static JSONObject create(String userName, double latitude, double longitude) throws IOException {
        return getJSON(
            API_ADDRESS +
            "groups/create?user=" + encode(userName) +
            "&lat=" + encode(String.valueOf(latitude)) +
            "&lon=" + encode(String.valueOf(longitude))
        );
    }

    public static JSONObject join(String groupPassword, String userName, double latitude, double longitude) throws IOException {
        return getJSON(
            API_ADDRESS +
            "groups/join/" + encode(groupPassword) +
            "?user=" + encode(userName) +
            "&lat=" + encode(String.valueOf(latitude)) +
            "&lon=" + encode(String.valueOf(longitude))
        );
    }

    public static JSONObject retrieve(String groupPassword) throws IOException {
        return getJSON(
            API_ADDRESS +
            "groups/retrieve/" + encode(groupPassword)
        );
    }

    public static boolean update(String groupPassword, int userID, double latitude, double longitude) throws IOException {
        String response = get(
            API_ADDRESS +
            "groups/update/" + encode(groupPassword) +
            "?user=" + encode(String.valueOf(userID)) +
            "&lat=" + encode(String.valueOf(latitude)) +
            "&lon=" + encode(String.valueOf(longitude))
        );
        return response != null && response.equals("true");
    }

    private static String request(HttpURLConnection connection) throws IOException {
        connection.setDoInput(true);
        connection.connect();
        if (connection.getResponseCode() != 200) return null;
        return IOUtils.toString(connection.getInputStream());
    }

    private static String get(String url) throws IOException {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            Helper.log("Openning " + url);
            connection.setRequestMethod("GET");
            String response = request(connection);
            Helper.log("Received " + response);
            return response;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private static JSONObject getJSON(String url) throws IOException {
        try {
            return new JSONObject(get(url));
        } catch (JSONException e) {
            return null;
        }
    }

    private static String encode(String string) {
        try {
            return URLEncoder.encode(string, CHARSET);
        } catch (UnsupportedEncodingException e) {
            return string;
        }
    }

    // Prevents instantiation
    private GroupsAPI() {
        throw new AssertionError("Cannot instantiate from " + this.getClass());
    }

}