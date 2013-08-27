package com.bitdance.giveortake;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * JSONUtils is a utility class for handling a few general JSON methods.
 */
public class JSONUtils {
    public static final String TAG = "JSONUtils";

    public static JSONObject parseResponse(HttpResponse response) throws IOException, JSONException {
        String strReponse = getStringFromResponse(response);
        Log.i(TAG, "Got data: " + strReponse);
        JSONTokener tokener = new JSONTokener(strReponse);
        return (JSONObject) tokener.nextValue();
    }

    private static String getStringFromResponse(HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStream in = response.getEntity().getContent();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } finally {
            in.close();
            if (reader != null) {
                reader.close();
            }
        }
        return sb.toString();
    }
}
