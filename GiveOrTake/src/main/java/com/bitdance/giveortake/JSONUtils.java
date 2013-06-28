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
 * Created by nora on 6/28/13.
 */
public class JSONUtils {
    public static final String TAG = "JSONUtils";

    public static JSONObject parseResponse(HttpResponse response) throws IOException, JSONException {
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
        Log.i(TAG, "Got data: " + sb.toString());
        JSONTokener tokener = new JSONTokener(sb.toString());
        return (JSONObject) tokener.nextValue();
    }
}
