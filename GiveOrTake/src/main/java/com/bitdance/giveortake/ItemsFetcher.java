package com.bitdance.giveortake;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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

    private static final int BUFFER_SIZE = 1024;

    private Context context;

    private HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return s.equals("api.giveortakeapp.com");
        }
    };

    public ItemsFetcher(Context context) {
        this.context = context;
        trustAllHosts();
    }

    public ArrayList<Item> fetchMyItems() {
        ItemsFilter filter = new ItemsFilter();
        filter.setOwnedBy(3);
        return fetchItemsWithFilter(filter);
    }

    public ArrayList<Item> fetchMostRecentItems() {
        Log.i(TAG, "Fetching the most recent items");
        ItemsFilter filter = new ItemsFilter();
        filter.setDistance(20).setUserID(3).setShowMyItems(true);
        return fetchItemsWithFilter(filter);
    }

    private ArrayList<Item> fetchItemsWithFilter(ItemsFilter filter) {
        ArrayList<Item> items = new ArrayList<Item>();
        StringBuilder urlsb = new StringBuilder();
        urlsb.append(Constants.BASE_URL).append("/items.php?").append(filter.buildQueryString());
        String urlspec = urlsb.toString();
        String result = null;
        try {
            result = fetchURLStringData(urlspec);
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

    public Drawable fetchItemThumbnail(Item item) {
        if (item.getThumbnailURL() == null) return null;

        try {
            return fetchURLBitmapDrawable(item.getThumbnailURL());
        } catch (IOException ioe) {
            Log.e(TAG, "Unable to download thumbnail: ", ioe);
            return null;
        }
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

    private String fetchURLStringData(String urlspec) throws IOException {
        BufferedReader reader = null;
        URL url = new URL(urlspec);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setHostnameVerifier(hostnameVerifier);

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

    private BitmapDrawable fetchURLBitmapDrawable(String urlspec) throws IOException {
        URL url = new URL(urlspec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream in = null;
        try {
            in = connection.getInputStream();
            BitmapDrawable drawable = new BitmapDrawable(context.getResources(),
                    BitmapFactory.decodeStream(in));
            return drawable;
        } finally {
            connection.disconnect();
            if (in != null) {
                in.close();
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
