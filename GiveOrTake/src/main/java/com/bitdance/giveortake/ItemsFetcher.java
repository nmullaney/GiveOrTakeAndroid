package com.bitdance.giveortake;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by nora on 6/17/13.
 */
public class ItemsFetcher {
    public static final String TAG = "ItemsFetcher";

    private Context context;

    public ItemsFetcher(Context context) {
        this.context = context;
    }

    public ArrayList<Item> fetchMostRecentItems() {
        Log.i(TAG, "Fetching the most recent items");
        ArrayList<Item> items = new ArrayList<Item>();
        StringBuilder urlsb = new StringBuilder();
        urlsb.append(Constants.BASE_URL).append("/items.php?userID=3&distance=20&showMyItems=1");
        String urlspec = urlsb.toString();
        String result = null;
        try {
            result = fetchURLData(urlspec);
            Log.i(TAG, "JSON = \n" + result);
            items = parseItems(result);
            Log.i(TAG, "Items = " + items);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch url data:", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse url data:", je);
        }

        return items;
    }

    private ArrayList<Item> parseItems(String result) throws JSONException {
        ArrayList<Item> items = new ArrayList<Item>();
        JSONObject allItems = (JSONObject) new JSONTokener(result).nextValue();
        JSONArray jsonItemArray = allItems.getJSONArray("items");
        for (int i = 0; i < jsonItemArray.length(); i++) {
            JSONObject jsonObject = jsonItemArray.getJSONObject(i);
            Item item = new Item(jsonObject);
            items.add(item);
        }
        return items;
    }

    ;

    public String fetchURLData(String urlspec) throws IOException {
        BufferedReader reader = null;
        URL url = new URL(urlspec);
        trustAllHosts();
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return s.equals("api.giveortakeapp.com");
            }
        });

        try {
            StringBuilder result = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                result.append(line + "\n");
            }
            return result.toString();
        } finally {
            connection.disconnect();
            if (reader != null) {
                reader.close();
            }
        }
    }

    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
