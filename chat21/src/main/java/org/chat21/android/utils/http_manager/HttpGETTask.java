package org.chat21.android.utils.http_manager;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by stefanodp91 on 09/05/17.
 */
class HttpGETTask extends AsyncTask<String, Void, String> {
    private static final String TAG = HttpGETTask.class.getName();

    private OnResponseRetrievedCallback callback;

    private Exception exception = null;

    private String mContentType = "application/x-www-form-urlencoded";
    private int mTimeout = 5000;

    private String mAuth;

    public HttpGETTask(OnResponseRetrievedCallback callback) {
        Log.d(TAG, "HttpGETTask.constructor");

        this.callback = callback;
    }

    public void setCurlCredentials(String username, String password) {
        Log.d(TAG, "setCurlCredentials");

        mAuth = username + ":" + password;
        mAuth = Base64.encodeToString(mAuth.getBytes(), Base64.DEFAULT);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        Log.d(TAG, "doInBackground");

        StringBuilder sb = null;
        BufferedReader reader = null;
        String response = null;
        try {

            URL url = new URL(params[0]);
            Log.d(TAG, "HttpGETTask.doInBackground: url == " + url.toString());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", mContentType);
            connection.setRequestProperty("Authorization", "Basic " + mAuth);
            connection.setConnectTimeout(mTimeout);
            connection.setRequestMethod("GET");
            connection.connect();
            int statusCode = connection.getResponseCode();
            //Log.e("statusCode", "" + statusCode);
            if (statusCode == 200) {
                sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            }

            connection.disconnect();
            if (sb != null)
                response = sb.toString();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            exception = e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    exception = e;
                }
            }
        }
        return response;
    }

    @Override
    protected void onPostExecute(String response) {
        Log.d(TAG, "onPostExecute");

        if (exception == null) {
            callback.onSuccess(response);
        } else {
            callback.onError(exception);
        }
    }

    public void setContentType(String contentType) {
        Log.d(TAG, "setContentType");

        mContentType = contentType;
    }

    public String getContentType() {
        Log.d(TAG, "getContentType");

        return mContentType;
    }

    public void setTimeout(int timeout) {
        Log.d(TAG, "setTimeout");

        mTimeout = timeout;
    }

    public int getTimeout() {
        Log.d(TAG, "getTimeout");

        return mTimeout;
    }
}