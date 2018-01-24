package chat21.android.core.groups;

/**
 * Created by stefanodp91 on 26/09/17.
 */

public interface OnGroupUpdatedCallback {

    void onGroupUpdatedSuccess(String groupId);

    void onGroupUpdatedError(Exception e);
}
