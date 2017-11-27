package  chat21.android.dao.groups;

/**
 * Created by stefanodp91 on 26/09/17.
 */

public interface OnGroupCreatedCallback {

    void onGroupCreatedSuccess(String groupId);

    void onGroupCreatedError(Exception e);
}
