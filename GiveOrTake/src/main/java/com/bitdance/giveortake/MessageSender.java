package com.bitdance.giveortake;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nora on 6/28/13.
 */
public class MessageSender {
    public static final String TAG = "MessageSender";

    public boolean sendMessage(Long itemID, String message) {
        Log.i(TAG, "sending message");
        boolean success = false;
        HttpClient client = SSLConnectionHelper.sslClient(new DefaultHttpClient());
        String urlSpec = Constants.BASE_URL + "/item/message.php";
        HttpPost post = new HttpPost(urlSpec);
        ActiveUser activeUser = ActiveUser.getInstance();
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("user_id",
                String.valueOf(activeUser.getUserID())));
        nameValuePairs.add(new BasicNameValuePair("token",
                activeUser.getToken()));
        nameValuePairs.add(new BasicNameValuePair("item_id",
                String.valueOf(itemID)));
        nameValuePairs.add(new BasicNameValuePair("message", message));

        try {
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = client.execute(post);
            JSONObject result = JSONUtils.parseResponse(response);
            success = true;
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to send message: ", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse response: ", je);
        }
        return success;
    }
}
