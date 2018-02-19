package org.chat21.android.core.authentication.task;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

/**
 * Created by andrea on 10/04/17.
 */

public class GetCustomTokenTask extends AsyncTask<Object, Void, String> {
    private static final String TAG = GetCustomTokenTask.class.getName();
    private OnCustomAuthTokenCallback onCustomAuthTokenCallback;
    private Exception mException;// fix Issue #24

    public GetCustomTokenTask(OnCustomAuthTokenCallback onCustomAuthTokenCallback) {
        Log.d(TAG, "GetCustomTokenTask");

        this.onCustomAuthTokenCallback = onCustomAuthTokenCallback;
    }

    @Override
    protected String doInBackground(Object... params) {
        Log.d(TAG, "doInBackground");

        String urlService = (String) params[0];

        Log.d(TAG, "service == " + urlService);

        try {
            URL url = new URL(urlService);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();


            //Impostazione dello User-Agent.
//                conn.setRequestProperty("User-Agent", "Smart21 - Android - AppVersion: " + appVersion);
            //  conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                conn.setRequestProperty("Accept-Language", ChatUtils.getLanguage());


            conn.setReadTimeout(15000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

//            List<NameValuePair> postDataParams = new ArrayList<>();
//            postDataParams.add(new BasicNameValuePair(SMART21_TYPE, type));
//            postDataParams.add(new BasicNameValuePair(SMART21_TO, to));
//            postDataParams.add(new BasicNameValuePair(SMART21_JSON, json));
//            postDataParams.add(new BasicNameValuePair(SMART21_MESSAGE, message));
//            postDataParams.add(new BasicNameValuePair(SMART21_HISTORY_VISIBLE, "false"));

//            Log.d(TAG, "params == " + getQuery(postDataParams));

//            OutputStream os = conn.getOutputStream();
//            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//            writer.write(getQuery(postDataParams));
//
//            writer.flush();
//            writer.close();
//            os.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                return readInputStreamToString(conn);

            } else {
                String errorMessage = "doInBackground: " +
                        "error retrieving the token: response code: " + responseCode;
                Log.e(TAG, errorMessage);
                FirebaseCrash.report(new Exception(errorMessage));

                return null;
            }
        } catch (ProtocolException e) {
            mException = e;
            e.printStackTrace();
            return null;
        } catch (MalformedURLException e) {
            mException = e;
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            mException = e;
            e.printStackTrace();
            return null;
        } catch (SSLHandshakeException e) {
            // fix Issue #24
            mException = e;
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            mException = e;
            e.printStackTrace();
            return null;
        }
    }

    private static String readInputStreamToString(HttpURLConnection connection) {
        Log.d(TAG, "readInputStreamToString");

        String result = null;
        StringBuffer sb = new StringBuffer();
        InputStream is = null;

        try {
            is = new BufferedInputStream(connection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
        } catch (Exception e) {
            Log.d(TAG, "Error reading InputStream");
            result = null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.d(TAG, "Error closing InputStream");
                }
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d(TAG, "onPostExecute");

        if (result != null) {
            onCustomAuthTokenCallback.onCustomAuthRetrievedSuccess(result);
        } else {
//            onCustomAuthTokenCallback.onCustomAuthRetrievedWithError(
//                    new Exception("error retrieving auth token."));
            // fix Issue #24
            onCustomAuthTokenCallback.onCustomAuthRetrievedWithError(mException);
        }
    }
}