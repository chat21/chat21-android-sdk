package org.chat21.android.utils.http_manager;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by stefanodp91 on 26/07/18.
 */
class HttpDELETETask extends AsyncTask<Object, String, String> {
    private static final String TAG = HttpDELETETask.class.getName();

    private InputStream inputStream;
    private HttpURLConnection urlConnection;
    private byte[] outputBytes;
    private String queryParams;
    private String responseData;
    private OnResponseRetrievedCallback callback;
    private String mAuth;

    private Map<String, String> mHeaderParams;

    public HttpDELETETask(String queryParams, OnResponseRetrievedCallback callback) {
        this.queryParams = queryParams;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(Object... params) {
        OutputStream os = null;
        // Send data
        try {

            // forming th java.net.URL object
            URL url = new URL(params[0].toString());
            urlConnection = (HttpURLConnection) url.openConnection();

            // set header
            if (mHeaderParams != null && mHeaderParams.size() > 0) {
                for (Map.Entry<String, String> entry : mHeaderParams.entrySet()) {
                    urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // set auth
            if (mAuth != null && !mAuth.isEmpty())
                urlConnection.setRequestProperty("Authorization", mAuth);

            // pass delete data
            outputBytes = queryParams.getBytes();

            urlConnection.setRequestMethod("DELETE");

            urlConnection.connect();
            os = urlConnection.getOutputStream();
            os.write(outputBytes);


            // Get Response and execute WebService request
            int statusCode = urlConnection.getResponseCode();

            // 200 represents HTTP OK
            if (statusCode == HttpsURLConnection.HTTP_OK) {
                inputStream = new BufferedInputStream(urlConnection.getInputStream());
                responseData = convertStreamToString(inputStream);
            } else if (statusCode == HttpsURLConnection.HTTP_NO_CONTENT) {
                responseData = "deleted with status " + statusCode;
            } else {
                responseData = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null)
                    os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return responseData;
    }

    // source : https://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
    public static String convertStreamToString(InputStream inputStream) throws IOException {

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
// StandardCharsets.UTF_8.name() > JDK 7
        return result.toString("UTF-8");
    }


    @Override
    protected void onPostExecute(String response) {
        Log.i(TAG, "onPostExecute");

        if (response != null && !response.isEmpty())
            callback.onSuccess(response);
        else
            callback.onError(new Exception("response is not valid"));
    }

    public void setAuth(String auth) {
        mAuth = auth;
    }

    public void setHeaderParams(Map<String, String> headerParams) {
        mHeaderParams = headerParams;
    }
}