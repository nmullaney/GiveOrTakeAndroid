package com.bitdance.giveortake;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by nora on 6/28/13.
 */
public class JSONUtils {
    public static final String TAG = "JSONUtils";

    public static JSONObject parseResponse(HttpResponse response) throws IOException, JSONException {
        String strReponse = getStringFromResponse(response);
        Log.i(TAG, "Got data: " + strReponse);
        JSONTokener tokener = new JSONTokener(strReponse);
        return (JSONObject) tokener.nextValue();
    }

    public static JSONArray parseArrayResponse(HttpResponse response) throws IOException, JSONException {
        String strResponse = getStringFromResponse(response);
        Log.i(TAG, "Got data: " + strResponse);
        JSONTokener tokener = new JSONTokener(strResponse);
        JSONArray jsonArray = new JSONArray();
        while (tokener.more()) {
            JSONObject jsonObject = (JSONObject) tokener.nextValue();
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    private static String getStringFromResponse(HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStream in = response.getEntity().getContent();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
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
