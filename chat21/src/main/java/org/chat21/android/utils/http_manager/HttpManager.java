package org.chat21.android.utils.http_manager;

import android.content.Context;
import android.util.Log;

import java.util.Map;

/**
 * Created by stefanodp91 on 09/05/17.
 */
public class HttpManager {
    private static final String TAG = HttpManager.class.getName();

    private Context mContext;

    public HttpManager(Context context) {
        mContext = context;
    }

    public void makeHttpPOSTCall(final OnResponseRetrievedCallback<String> callback, String url,
                                 String queryParams) {

        OnResponseRetrievedCallback<String> mCallback = new OnResponseRetrievedCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "makeHttpPOSTCall.onSuccess");

//                Log.d(TAG, "response == " + response);
                callback.onSuccess(response);
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "makeHttpPOSTCall.onError");

                Log.e(TAG, e.getMessage());
                callback.onError(e);
            }
        };

        new HttpPOSTTask(queryParams, mCallback).execute(url);
    }

    public void makeHttpPOSTCall(final OnResponseRetrievedCallback<String> callback, String url,
                                 Map<String, String> headerParams, String queryParams) {

        OnResponseRetrievedCallback<String> mCallback = new OnResponseRetrievedCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "makeHttpPOSTCall.onSuccess");

//                Log.d(TAG, "response == " + response);
                callback.onSuccess(response);
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "makeHttpPOSTCall.onError");

                Log.e(TAG, e.getMessage());
                callback.onError(e);
            }
        };

        HttpPOSTTask httpPOSTTask = new HttpPOSTTask(queryParams, mCallback);
        httpPOSTTask.setHeaderParams(headerParams);
        httpPOSTTask.execute(url);
    }

    public void makeHttpDELETECall(final OnResponseRetrievedCallback<String> callback, String url,
                                 Map<String, String> headerParams, String queryParams) {

        OnResponseRetrievedCallback<String> mCallback = new OnResponseRetrievedCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "makeHttpDELETECall.onSuccess");

//                Log.d(TAG, "response == " + response);
                callback.onSuccess(response);
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "makeHttpDELETECall.onError");

                Log.e(TAG, e.getMessage());
                callback.onError(e);
            }
        };

        HttpDELETETask httpDELETETask = new HttpDELETETask(queryParams, mCallback);
        httpDELETETask.setHeaderParams(headerParams);
        httpDELETETask.execute(url);
    }

    public void makeHttpPUTCall(final OnResponseRetrievedCallback<String> callback, String url,
                                   Map<String, String> headerParams, String queryParams) {

        OnResponseRetrievedCallback<String> mCallback = new OnResponseRetrievedCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "makeHttpPUTCall.onSuccess");

//                Log.d(TAG, "response == " + response);
                callback.onSuccess(response);
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "makeHttpPUTCall.onError");

                Log.e(TAG, e.getMessage());
                callback.onError(e);
            }
        };

        HttpPUTTask httpPUTTask = new HttpPUTTask(queryParams, mCallback);
        httpPUTTask.setHeaderParams(headerParams);
        httpPUTTask.execute(url);
    }

    public void makeHttpGETCall(final OnResponseRetrievedCallback<String> callback, String url) {
        OnResponseRetrievedCallback<String> mCallback = new OnResponseRetrievedCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "makeHttpGETCall.onSuccess");

//                Log.d(TAG, "response == " + response);
                callback.onSuccess(response);
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "makeHttpGETCall.onError");

                Log.e(TAG, e.getMessage());
                callback.onError(e);
            }
        };

        new HttpGETTask(mCallback).execute(url);
    }

    public void makeHttpGETCall(final OnResponseRetrievedCallback<String> callback, String url,
                                String username, String password) {
        OnResponseRetrievedCallback<String> mCallback = new OnResponseRetrievedCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "makeHttpGETCall.onSuccess");

//                Log.d(TAG, "response == " + response);
                callback.onSuccess(response);
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "makeHttpGETCall.onError");

                Log.e(TAG, e.getMessage());
                callback.onError(e);
            }
        };

        HttpGETTask httpGETTask = new HttpGETTask(mCallback);
        httpGETTask.setCurlCredentials(username, password);
        httpGETTask.execute(url);
    }

    public Context getContext() {
        return mContext;
    }
}