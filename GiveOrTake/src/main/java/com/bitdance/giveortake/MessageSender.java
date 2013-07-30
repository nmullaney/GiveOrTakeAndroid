package com.bitdance.giveortake;

import android.content.Context;
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

    private Context context;

    public MessageSender (Context context) {
        this.context = context;
    }

    public SendMessageResponse sendMessage(Long itemID, String message) {
        Log.i(TAG, "sending message");
        SendMessageResponse messageResponse = null;
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
            if (result.has(Constants.ERROR_KEY)) {
                String error = result.getString(Constants.ERROR_KEY);
                messageResponse = new SendMessageResponse(error);
            } else {
                Integer numberOfMessages = result.getInt("numMessagesSent");
                itemID = result.getLong("itemID");
                messageResponse = new SendMessageResponse(itemID, numberOfMessages);
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to send message: ", ioe);
            messageResponse = new SendMessageResponse(
                    context.getResources().getString(R.string.error_try_again));
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse response: ", je);
            messageResponse = new SendMessageResponse(
                    context.getResources().getString(R.string.error_try_again));
        }
        return messageResponse;
    }

    public class SendMessageResponse {
        private boolean success;
        private String errorMessage;
        private Integer numberOfMessagesSent;
        private Long itemID;

        public SendMessageResponse(Long itemID, int numberOfMessagesSent) {
            this.itemID = itemID;
            this.numberOfMessagesSent = numberOfMessagesSent;
            this.success = true;
        }

        public SendMessageResponse(String errorMessage) {
            this.errorMessage = errorMessage;
            this.success = false;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Long getItemID() {
            return itemID;
        }

        public Integer getNumberOfMessagesSent() {
            return numberOfMessagesSent;
        }
    }
}
