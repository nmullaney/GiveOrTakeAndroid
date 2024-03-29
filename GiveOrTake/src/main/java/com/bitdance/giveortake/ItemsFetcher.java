package com.bitdance.giveortake;

import android.content.Context;
import android.content.SharedPreferences;
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
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * The class handles the fetching and posting of Items.
 */
public class ItemsFetcher {
    public static final String TAG = "ItemsFetcher";

    private Context context;
    private ActiveUser activeUser;

    public ItemsFetcher(Context context, ActiveUser activeUser) {
        this.context = context;
        this.activeUser = activeUser;
        SSLConnectionHelper.trustAllHosts();
    }

    public ItemsResponse fetchMyItems(Integer offset) {
        ItemsFilter filter = new ItemsFilter();
        filter.setOffset(offset);
        filter.setLimit(Constants.MAX_ITEMS_TO_REQUEST);
        filter.setOwnedBy(activeUser.getUserID());
        return fetchItemsWithFilter(filter);
    }

    public ItemsResponse fetchItems(Integer offset, String query) {
        Log.d(TAG, "Fetching items with offset: " + offset);
        ItemsFilter filter = new ItemsFilter();
        SharedPreferences preferences = context
                .getSharedPreferences(Constants.FILTER_PREFERENCES, Context.MODE_PRIVATE);
        filter
                .setDistance(preferences.getInt(Constants.DISTANCE_PREFERENCE,
                        Constants.DEFAULT_DISTANCE))
                .setLimit(Constants.MAX_ITEMS_TO_REQUEST)
                .setOffset(offset)
                .setQuery(query)
                .setUserID(activeUser.getUserID())
                .setShowMyItems(preferences.getBoolean(Constants.SHOW_MY_ITEMS_PREFERENCE,
                        Constants.DEFAULT_SHOW_MY_ITEMS));
        return fetchItemsWithFilter(filter);
    }

    public ItemsResponse fetchSingleItem(Long itemID) {
        ItemsFilter filter = new ItemsFilter();
        filter.setItemID(itemID);
        return fetchItemsWithFilter(filter);
    }

    private ItemsResponse fetchItemsWithFilter(ItemsFilter filter) {
        ItemsResponse itemsResponse;
        String urlSpec = Constants.BASE_URL + "/items.php?" + filter.buildQueryString();
        try {
            String result = fetchURLStringData(urlSpec);
            Log.d(TAG, "JSON = \n" + result);
            itemsResponse = parseItems(result);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch url data:", ioe);
            itemsResponse = new ItemsResponse(context.getString(R.string.error_try_again));
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse url data:", je);
            itemsResponse = new ItemsResponse(context.getString(R.string.error_try_again));
        }

        return itemsResponse;
    }

    private ItemsResponse parseItems(String result) throws JSONException {
        ArrayList<Item> items = new ArrayList<Item>();
        JSONObject allItems = (JSONObject) new JSONTokener(result).nextValue();
        if (allItems.has(Constants.ERROR_KEY)) {
            return new ItemsResponse(allItems.getString(Constants.ERROR_KEY));
        }
        JSONArray jsonItemArray = allItems.getJSONArray("items");
        for (int i = 0; i < jsonItemArray.length(); i++) {
            JSONObject jsonObject = jsonItemArray.getJSONObject(i);
            Item item = new Item(jsonObject);
            items.add(item);
        }
        return new ItemsResponse(items);
    }

    public class ItemsResponse {
        private boolean success;
        private ArrayList<Item> items;
        private String error;

        public ItemsResponse(ArrayList<Item> items) {
            this.items = items;
            this.success = true;
        }

        public ItemsResponse(String error) {
            this.error = error;
            this.success = false;
        }

        public boolean isSuccess() {
            return success;
        }

        public ArrayList<Item> getItems() {
            return items;
        }

        public String getError() {
            return error;
        }
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

    public ItemResponse postItem(Item item) {
        String urlSpec = Constants.BASE_URL + "/item.php";
        HttpClient client = SSLConnectionHelper.sslClient(new DefaultHttpClient());
        HttpPost post = new HttpPost(urlSpec);
        MultipartEntity multipartEntity = new MultipartEntity();
        ItemResponse itemResponse;
        try {
            if (item.getId() != null) {
                multipartEntity.addPart("item_id", new StringBody(String.valueOf(item.getId())));
            }
            multipartEntity.addPart("user_id", new StringBody(String.valueOf(item.getUserID())));
            multipartEntity.addPart("token", new StringBody(activeUser.getToken()));
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
            itemResponse = new ItemResponse(context.getString(R.string.error_try_again));
            return itemResponse;
        }

        try {
            post.setEntity(multipartEntity);
            HttpResponse response = client.execute(post);
            JSONObject result = JSONUtils.parseResponse(response);
            if (result.has(Constants.ERROR_KEY)) {
                itemResponse = new ItemResponse(result.getString(Constants.ERROR_KEY));
            } else {
                JSONObject itemJSON = result.getJSONObject("item");
                item.updateFromJSON(itemJSON);
                itemResponse = new ItemResponse(item);
            }
            if (result.has("karma")) {
                JSONObject karmaData = result.getJSONObject("karma");
                Integer newKarmaValue = karmaData.getInt("updatedKarma");
                activeUser.getUser().setKarma(newKarmaValue);
                itemResponse.setKarmaChange(karmaData.getInt("karmaChange"));
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to post item", ioe);
            itemResponse = new ItemResponse(context.getString(R.string.login_failure));
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse item", je);
            itemResponse = new ItemResponse(context.getString(R.string.login_failure));
        }
        return itemResponse;
    }

    public class ItemResponse {
        private boolean success;
        private Item item;
        private String error;
        private Integer karmaChange = 0;

        public ItemResponse(Item item) {
            this.item = item;
            this.success = true;
        }

        public ItemResponse(String error) {
            this.error = error;
            this.success = false;
        }

        public boolean isSuccess() {
            return success;
        }

        public Item getItem() {
            return item;
        }

        public String getError() {
            return error;
        }

        public Integer getKarmaChange() {
            return karmaChange;
        }

        public void setKarmaChange(Integer karmaChange) {
            this.karmaChange = karmaChange;
        }
    }

    public DeleteItemsResponse deleteItems(ArrayList<Long> itemIDs) {
        String urlSpec = Constants.BASE_URL + "/items/delete.php";
        HttpClient client = SSLConnectionHelper.sslClient(new DefaultHttpClient());
        HttpPost post = new HttpPost(urlSpec);
        MultipartEntity multipartEntity = new MultipartEntity();
        DeleteItemsResponse deleteItemsResponse;
        try {
            multipartEntity.addPart("user_id", new StringBody(String.valueOf(activeUser.getUserID())));
            multipartEntity.addPart("token", new StringBody(activeUser.getToken()));
            for (Long itemID : itemIDs) {
                multipartEntity.addPart("item_ids[]", new StringBody(String.valueOf(itemID)));
            }
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "Failed to build request", uee);
            deleteItemsResponse = new DeleteItemsResponse(context.getString(R.string.error));
            return deleteItemsResponse;
        }

        try {
            post.setEntity(multipartEntity);
            HttpResponse response = client.execute(post);
            JSONObject result = JSONUtils.parseResponse(response);
            Log.i(TAG, result.toString());
            if (result.has(Constants.ERROR_KEY)) {
                deleteItemsResponse = new DeleteItemsResponse(result.getString(Constants.ERROR_KEY));
            } else {
                ArrayList<Long> failedIDs = new ArrayList<Long>();
                if (result.has("failed")) {
                    JSONArray failedIDsJSON = result.getJSONArray("failed");

                    for (int i = 0; i < failedIDsJSON.length(); i++) {
                        failedIDs.add(failedIDsJSON.getLong(i));
                    }
                }
                ArrayList<Long> successfulIDs = new ArrayList<Long>();
                if (result.has("deleted")) {
                    JSONArray successfulIDsJSON = result.getJSONArray("deleted");

                    for (int i = 0; i < successfulIDsJSON.length(); i++) {
                        successfulIDs.add(successfulIDsJSON.getLong(i));
                    }
                }
                String error = null;
                if (successfulIDs.isEmpty()) {
                    error = context.getString(R.string.items_delete_failed);
                } else if (failedIDs.isEmpty()) {
                    error = context.getString(R.string.items_some_delete_failed);
                }
                deleteItemsResponse = new DeleteItemsResponse(successfulIDs, failedIDs, error);
                if (result.has("karma")) {
                    JSONObject karmaData = result.getJSONObject("karma");
                    Integer newKarmaValue = karmaData.getInt("updatedKarma");
                    activeUser.getUser().setKarma(newKarmaValue);
                    // change will always be negative, so no need to note it
                }
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to delete item", ioe);
            deleteItemsResponse = new DeleteItemsResponse(context.getString(R.string.error_try_again));
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse delete item response", je);
            deleteItemsResponse = new DeleteItemsResponse(context.getString(R.string.error_try_again));
        }

        return deleteItemsResponse;
    }

    public class DeleteItemsResponse {
        private boolean success;
        private ArrayList<Long> successfulIDs;
        private ArrayList<Long> failedIDs;
        private String error;

        public DeleteItemsResponse(ArrayList<Long> successfulIDs, ArrayList<Long> failedIDs, String error) {
            this.successfulIDs = successfulIDs;
            this.failedIDs = failedIDs;
            this.error = error;
            this.success = (this.failedIDs == null) || (this.failedIDs.isEmpty());
        }

        public DeleteItemsResponse(String error) {
            this.error = error;
            this.success = false;
        }

        public boolean isSuccess() {
            return success;
        }

        public ArrayList<Long> getSuccessfulIDs() {
            return successfulIDs;
        }

        public String getError() {
            return error;
        }
    }

    public UserWantsItemResponse userWantsItem(Long itemID) {

        String urlSpec = Constants.BASE_URL + "/item/want.php";
        HttpClient client = SSLConnectionHelper.sslClient(new DefaultHttpClient());
        HttpPost post = new HttpPost(urlSpec);
        MultipartEntity multipartEntity = new MultipartEntity();
        UserWantsItemResponse userWantsItemResponse;
        try {
            multipartEntity.addPart("user_id", new StringBody(String.valueOf(activeUser.getUserID())));
            multipartEntity.addPart("token", new StringBody(activeUser.getToken()));
            multipartEntity.addPart("item_id", new StringBody(String.valueOf(itemID)));
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "Failed to build request", uee);
            userWantsItemResponse = new UserWantsItemResponse(context.getString(R.string.error_try_again));
            return userWantsItemResponse;
        }

        try {
            post.setEntity(multipartEntity);
            HttpResponse response = client.execute(post);
            JSONObject result = JSONUtils.parseResponse(response);
            Log.i(TAG, result.toString());
            if (result.has(Constants.ERROR_KEY)) {
                userWantsItemResponse = new UserWantsItemResponse(result.getString(Constants.ERROR_KEY));
            } else {
                Integer messagesSent = result.getInt("numMessagesSent");
                userWantsItemResponse = new UserWantsItemResponse(itemID, messagesSent);
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to delete item", ioe);
            userWantsItemResponse = new UserWantsItemResponse(context.getString(R.string.error_try_again));
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse delete item response", je);
            userWantsItemResponse = new UserWantsItemResponse(context.getString(R.string.error_try_again));
        }

        return userWantsItemResponse;
    }

    public class UserWantsItemResponse {
        private boolean success;
        private String error;
        private Long itemID;
        private Integer numMessagesSent;

        public UserWantsItemResponse(String error) {
            this.error = error;
            this.success = false;
        }

        public UserWantsItemResponse(Long itemID, Integer numMessagesSent) {
            this.itemID = itemID;
            this.numMessagesSent = numMessagesSent;
            this.success = true;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }

        public Long getItemID() {
            return itemID;
        }

        public Integer getNumMessagesSent() {
            return numMessagesSent;
        }
    }
}
