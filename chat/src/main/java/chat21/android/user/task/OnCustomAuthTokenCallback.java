package chat21.android.user.task;

/**
 * Created by andrea on 10/04/17.
 */

public interface OnCustomAuthTokenCallback {

    void onCustomAuthRetrievedSuccess(String token);

    void onCustomAuthRetrievedWithError(Exception e);
}

