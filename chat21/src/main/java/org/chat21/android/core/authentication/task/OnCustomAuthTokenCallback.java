package org.chat21.android.core.authentication.task;

/**
 * Created by andrea on 10/04/17.
 */

public interface OnCustomAuthTokenCallback {

    void onCustomAuthRetrievedSuccess(String token);

    void onCustomAuthRetrievedWithError(Exception e);
}

