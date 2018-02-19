package org.chat21.android.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by stefanodp91 on 13/09/17.
 */

public abstract class AbstractNetworkReceiver extends BroadcastReceiver {
    public AbstractNetworkReceiver() {
        // required to avoid
        // Unable to instantiate receiver:
        // java.lang.InstantiationException: <AbstractNetworkReceiver> has no zero argument constructor
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null != intent) {
            NetworkInfo.State wifiState = null;
            NetworkInfo.State mobileState = null;
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            if (wifiState != null && mobileState != null
                    && NetworkInfo.State.CONNECTED != wifiState
                    && NetworkInfo.State.CONNECTED == mobileState) {
                // phone network connect success
                mobileNetwork();
            } else if (wifiState != null && mobileState != null
                    && NetworkInfo.State.CONNECTED != wifiState
                    && NetworkInfo.State.CONNECTED != mobileState) {
                // no network
                noNetwork();
            } else if (wifiState != null && NetworkInfo.State.CONNECTED == wifiState) {
                // wify connect success
                WIFINetwork();
            }
        }
    }

    public abstract void noNetwork();

    public abstract void mobileNetwork();

    public abstract void WIFINetwork();

    /**
     * Check the connectivity status and detect which connection type is in use (mobile, wifi.. )
     *
     * @return true if is connected, false otherwise
     */
    public static boolean isConnected(Context context) {

        boolean isConnected = false;

        NetworkInfo.State wifiState;
        NetworkInfo.State mobileState;
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        if (wifiState != null && mobileState != null
                && NetworkInfo.State.CONNECTED != wifiState
                && NetworkInfo.State.CONNECTED == mobileState) {
            // phone network connect success
            isConnected = true;
        } else if (wifiState != null && NetworkInfo.State.CONNECTED == wifiState) {
            // wifi connect success
            isConnected = true;
        } else if (wifiState != null && mobileState != null
                && NetworkInfo.State.CONNECTED != wifiState
                && NetworkInfo.State.CONNECTED != mobileState) {
            isConnected = false;
        }

        return isConnected;
    }
}