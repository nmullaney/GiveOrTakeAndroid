package com.bitdance.giveortake;

import android.content.Context;
import android.util.Log;

import com.facebook.Session;
import com.google.android.gms.maps.model.LatLng;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * UserFetcher fetches and posts any data about Users to the backend.  It also handles login.
 */
public class UserFetcher {
    public static final String TAG = "UserFetcher";

    private Context context;
    private ActiveUser activeUser;

    public UserFetcher(Context context, ActiveUser activeUser) {
        this.context = context;
        this.activeUser = activeUser;
        SSLConnectionHelper.trustAllHosts();
    }

    public LoginUserResponse loginUser() {
        assert(Session.getActiveSession() != null);
        String fbAccessToken = Session.getActiveSession().getAccessToken();
        String urlSpec = Constants.BASE_URL + "/user.php";

        HttpClient client = SSLConnectionHelper.sslClient(new DefaultHttpClient());
        HttpPost post = new HttpPost(urlSpec);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("facebook_id", activeUser.getFacebookID()));
        nameValuePairs.add(new BasicNameValuePair("fb_access_token", fbAccessToken));
        nameValuePairs.add(new BasicNameValuePair("username", activeUser.getUserName()));
        nameValuePairs.add(new BasicNameValuePair("email", activeUser.getEmail()));

        LoginUserResponse loginUserResponse;
        try {
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = client.execute(post);
            JSONObject result = JSONUtils.parseResponse(response);
            Log.d(TAG, result.toString());
            if (result.has(Constants.ERROR_KEY)) {
                loginUserResponse = new LoginUserResponse(result.getString(Constants.ERROR_KEY));
            } else {
                activeUser.updateFromJSON(result);
                loginUserResponse = new LoginUserResponse();
            }
        } catch(IOException ioe) {
            Log.e(TAG, "Login failed due to exception:", ioe);
            loginUserResponse = new LoginUserResponse(context.getString(R.string.error_try_again));
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse json:", je);
            loginUserResponse = new LoginUserResponse(context.getString(R.string.error_try_again));
        }
        return loginUserResponse;
    }

    public class LoginUserResponse {
        private boolean success;
        private String error;
        // The User is always the singleton ActiveUser, so there's no need to store it here

        public LoginUserResponse() {
            this.success = true;
        }

        public LoginUserResponse(String error) {
            this.error = error;
            this.success = false;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return this.error;
        }
    }

    public UserResponse fetchUser(Long userID) {
        UserResponse userResponse;
        String urlSpec = Constants.BASE_URL + "/user.php?";
        HttpClient client = SSLConnectionHelper.sslClient(new DefaultHttpClient());

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("user_id", userID.toString()));
        String paramString = URLEncodedUtils.format(nameValuePairs, "UTF-8");
        HttpGet get = new HttpGet(urlSpec + paramString);

        try {
            HttpResponse response = client.execute(get);
            Log.d(TAG, response.toString());
            JSONObject result = JSONUtils.parseResponse(response);
            if (result.has(Constants.ERROR_KEY)) {
                userResponse = new UserResponse(result.getString(Constants.ERROR_KEY));
            } else {
                User user = new User();
                user.updateFromJSON(result);
                userResponse = new UserResponse(user);
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, "Failed to get user:", e);
            userResponse = new UserResponse(context.getString(R.string.error_try_again));
        } catch (IOException e) {
            Log.e(TAG, "Failed to get user:", e);
            userResponse = new UserResponse(context.getString(R.string.error_try_again));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse user:", e);
            userResponse = new UserResponse(context.getString(R.string.error_try_again));
        }
        return userResponse;
    }

    public class UserResponse {
        private boolean success;
        private User user;
        private String error;

        public UserResponse(User user) {
            this.user = user;
            this.success = true;
        }

        public UserResponse(String error) {
            this.error = error;
            this.success = false;
        }

        public boolean isSuccess() {
            return success;
        }

        public User getUser() {
            return user;
        }

        public String getError() {
            return error;
        }
    }

    public UsersResponse fetchUsersWhoWantItem(Long itemID, int minMessagesSent) {
        String urlSpec = Constants.BASE_URL + "/users.php?";
        HttpClient client = SSLConnectionHelper.sslClient(new DefaultHttpClient());
        ArrayList<User> users = new ArrayList<User>();
        UsersResponse usersResponse;

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("wantItemID", itemID.toString()));
        nameValuePairs.add(new BasicNameValuePair("minMessages", String.valueOf(minMessagesSent)));
        String paramString = URLEncodedUtils.format(nameValuePairs, "UTF-8");
        HttpGet get = new HttpGet(urlSpec + paramString);

        try {
            HttpResponse response = client.execute(get);
            Log.e(TAG, response.toString());
            JSONObject fullResult = JSONUtils.parseResponse(response);
            if (fullResult.has(Constants.ERROR_KEY)) {
                usersResponse = new UsersResponse(fullResult.getString(Constants.ERROR_KEY));
            } else {
                JSONArray usersJson = fullResult.getJSONArray("users");
                for (int i = 0; i < usersJson.length(); i++) {
                    JSONObject userJson = (JSONObject) usersJson.get(i);
                    User user = new User();
                    user.updateFromJSON(userJson);
                    users.add(user);
                }
                usersResponse = new UsersResponse(users);
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, "Failed to get user:", e);
            usersResponse = new UsersResponse(
                    context.getResources().getString(R.string.error_try_again));
        } catch (IOException e) {
            Log.e(TAG, "Failed to get user:", e);
            usersResponse = new UsersResponse(
                    context.getResources().getString(R.string.error_try_again));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse user:", e);
            usersResponse = new UsersResponse(
                    context.getResources().getString(R.string.error_try_again));
        }

        return usersResponse;
    }

    public class UsersResponse {
        private boolean success;
        private ArrayList<User> users;
        private String error;

        public UsersResponse(ArrayList<User> users) {
            this.users = users;
            this.success = true;
        }

        public UsersResponse(String error) {
            this.error = error;
            this.success = false;
        }

        public boolean isSuccess() {
            return success;
        }

        public ArrayList<User> getUsers() {
            return users;
        }

        public String getError() {
            return error;
        }
    }

    public UpdateResponse updateUsername(String newUsername) {
        Log.i(TAG, "Updating username to: " + newUsername);
        return updateUserField("username", newUsername);
    }

    public UpdateResponse addPendingEmail(String newEmail) {
        Log.i(TAG, "Adding pending email: " + newEmail);
        return updateEmailField("email", newEmail);
    }

    public UpdateResponse sendEmailCode(String emailCode) {
        Log.i(TAG, "Sending email code: " + emailCode);
        return updateEmailField("code", emailCode);
    }

    public UpdateResponse cancelPendingEmail() {
        Log.i(TAG, "Cancelling pending email");
        return updateEmailField("cancel_pending", "1");
    }

    public UpdateResponse updateLocation(LatLng latLng) {
        String urlSpec = Constants.BASE_URL + "/user.php";
        List <NameValuePair> data = new ArrayList<NameValuePair>();
        data.add(new BasicNameValuePair("latitude", String.valueOf(latLng.latitude)));
        data.add(new BasicNameValuePair("longitude", String.valueOf(latLng.longitude)));
        return updateUser(data, urlSpec);
    }

    private UpdateResponse updateUser(List<NameValuePair> data, String urlSpec) {
        UpdateResponse updateResponse = new UpdateResponse(false,
                context.getString(R.string.error_try_again));

        HttpClient client = SSLConnectionHelper.sslClient(new DefaultHttpClient());
        HttpPost post = new HttpPost(urlSpec);
        data.add(new BasicNameValuePair("user_id", activeUser.getUserID().toString()));
        data.add(new BasicNameValuePair("token", activeUser.getToken()));
        try {
            post.setEntity(new UrlEncodedFormEntity(data));
            HttpResponse response = client.execute(post);
            Log.i(TAG, "response = " + response.toString());
            JSONObject result = JSONUtils.parseResponse(response);
            Log.i(TAG, result.toString());

            if (result.has("error")) {
                updateResponse = new UpdateResponse(false, result.getString("error"));
            } else {
                activeUser.updateFromJSON(result);
                updateResponse = new UpdateResponse(true, null);
            }
        } catch(IOException ioe) {
            Log.e(TAG, "Login failed due to exception:", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse json:", je);
        }

        return updateResponse;
    }

    private UpdateResponse updateUserField(String fieldName, String fieldValue) {
        String urlSpec = Constants.BASE_URL + "/user.php";
        List <NameValuePair> data = new ArrayList<NameValuePair>();
        data.add(new BasicNameValuePair(fieldName, fieldValue));
        return updateUser(data, urlSpec);
    }

    private UpdateResponse updateEmailField(String fieldName, String fieldValue) {
        String urlSpec = Constants.BASE_URL + "/user/email.php";
        List <NameValuePair> data = new ArrayList<NameValuePair>();
        data.add(new BasicNameValuePair(fieldName, fieldValue));
        return updateUser(data, urlSpec);
    }

    public class UpdateResponse {
        private boolean success;
        private String errorMessage;

        public UpdateResponse(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

}
