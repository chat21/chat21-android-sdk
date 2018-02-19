package org.chat21.android.connectivity;

import android.util.Log;

/**
 * Created by stefanodp91 on 13/09/17.
 */

public class NetworkReceiver extends AbstractNetworkReceiver {
    private static final String TAG = NetworkReceiver.class.getName();

    public NetworkReceiver() {
        // required to avoid
        // Unable to instantiate receiver:
        // java.lang.InstantiationException: <NetworkReceiver> has no zero argument constructor
    }

    @Override
    public void noNetwork() {
        Log.d(TAG, "noNetwork");
    }

    @Override
    public void mobileNetwork() {
        Log.d(TAG, "mobileNetwork");
    }

    @Override
    public void WIFINetwork() {
        Log.d(TAG, "WIFINetwork");
    }
}