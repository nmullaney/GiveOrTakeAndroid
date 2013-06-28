package com.bitdance.giveortake;

import android.content.Context;
import android.util.Log;

import com.facebook.Session;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by nora on 6/23/13.
 */
public class UserFetcher {
    public static final String TAG = "UserFetcher";

    private Context context;

    public UserFetcher(Context context) {
        this.context = context;
        SSLConnectionHelper.trustAllHosts();
    }

    public boolean loginUser() {
        assert(Session.getActiveSession() != null);
        boolean success = false;
        String fbAccessToken = Session.getActiveSession().getAccessToken();
        ActiveUser activeUser = ActiveUser.getInstance();
        String urlSpec = Constants.BASE_URL + "/user.php";

        HttpClient client = SSLConnectionHelper.sslClient(new DefaultHttpClient());
        HttpPost post = new HttpPost(urlSpec);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("facebook_id", activeUser.getFacebookID()));
        nameValuePairs.add(new BasicNameValuePair("fb_access_token", fbAccessToken));
        // TODO: don't add empty values
        nameValuePairs.add(new BasicNameValuePair("username", activeUser.getUserName()));
        nameValuePairs.add(new BasicNameValuePair("email", activeUser.getEmail()));

        try {
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = client.execute(post);
            JSONObject result = JSONUtils.parseResponse(response);
            Log.i(TAG, result.toString());
            // TODO: handle parsing error json
            activeUser.updateFromJSON(result);
            success = true;
        } catch(IOException ioe) {
            Log.e(TAG, "Login failed due to exception:", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse json:", je);
        }
        return success;
    }

    public User fetchUser(Long userID) {
        String urlSpec = Constants.BASE_URL + "/user.php?";
        HttpClient client = SSLConnectionHelper.sslClient(new DefaultHttpClient());

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("user_id", userID.toString()));
        String paramString = URLEncodedUtils.format(nameValuePairs, "UTF-8");
        HttpGet get = new HttpGet(urlSpec + paramString);

        try {
            HttpResponse response = client.execute(get);
            JSONObject result = JSONUtils.parseResponse(response);
            User user = new User();
            user.updateFromJSON(result);
            return user;
        } catch (ClientProtocolException e) {
            Log.e(TAG, "Failed to get user:", e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to get user:", e);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse user:", e);
        }
        return null;
    }



}