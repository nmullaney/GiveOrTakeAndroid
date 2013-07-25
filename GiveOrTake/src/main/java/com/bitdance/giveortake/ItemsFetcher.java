package com.bitdance.giveortake;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * The class handles the fetching and posting of Items.
 */
public class ItemsFetcher {
    public static final String TAG = "ItemsFetcher";

    private Context context;

    public ItemsFetcher(Context context) {
        this.context = context;
        SSLConnectionHelper.trustAllHosts();
    }

    public ArrayList<Item> fetchMyItems() {
        ItemsFilter filter = new ItemsFilter();
        filter.setOwnedBy(ActiveUser.getInstance().getUserID());
        return fetchItemsWithFilter(filter);
    }

    public ArrayList<Item> fetchMostRecentItems() {
        Log.i(TAG, "Fetching the most recent items");
        ItemsFilter filter = new ItemsFilter();
        filter
                .setDistance(20)
                .setUserID(ActiveUser.getInstance().getUserID())
                .setShowMyItems(true);
        return fetchItemsWithFilter(filter);
    }

    private ArrayList<Item> fetchItemsWithFilter(ItemsFilter filter) {
        ArrayList<Item> items = new ArrayList<Item>();
        String urlSpec = Constants.BASE_URL + "/items.php?" + filter.buildQueryString();
        String result;
        try {
            result = fetchURLStringData(urlSpec);
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
        connection.setHostnameVerifier(SSLConnectionHelper.getHostnameVerifier());

        try {
            StringBuilder result = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            return result.toString();
        } finally {
            connection.disconnect();
            if (reader != null) {
                reader.close();
            }
        }
    }

    private BitmapDrawable fetchURLBitmapDrawable(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream in = null;
        try {
            in = connection.getInputStream();
            return new BitmapDrawable(context.getResources(),
                    BitmapFactory.decodeStream(in));
        } finally {
            connection.disconnect();
            if (in != null) {
                in.close();
            }
        }
    }

    public Item postItem(Item item) {
        String urlSpec = Constants.BASE_URL + "/item.php";
        HttpClient client = SSLConnectionHelper.sslClient(new DefaultHttpClient());
        HttpPost post = new HttpPost(urlSpec);
        MultipartEntity multipartEntity = new MultipartEntity();
        try {
            if (item.getId() != null) {
                multipartEntity.addPart("item_id", new StringBody(String.valueOf(item.getId())));
            }
            multipartEntity.addPart("user_id", new StringBody(String.valueOf(item.getUserID())));
            multipartEntity.addPart("token", new StringBody(ActiveUser.getInstance().getToken()));
            multipartEntity.addPart("name", new StringBody(item.getName()));
            multipartEntity.addPart("desc", new StringBody(item.getDescription()));
            multipartEntity.addPart("state", new StringBody(item.getState().getName()));
            if (item.getStateUserID() != null)
                multipartEntity.addPart("state_user_id",
                        new StringBody(String.valueOf(item.getStateUserID())));
            multipartEntity.addPart("thumbnail", new ByteArrayBody(item.getThumbnailData(context),
                    "thumbnail"));
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "Failed to build request", uee);
        }

        try {
            post.setEntity(multipartEntity);
            HttpResponse response = client.execute(post);
            JSONObject result = JSONUtils.parseResponse(response);
            JSONObject itemJSON = result.getJSONObject("item");
            item.updateFromJSON(itemJSON);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to post item", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse item", je);
        }
        return item;
    }
}
