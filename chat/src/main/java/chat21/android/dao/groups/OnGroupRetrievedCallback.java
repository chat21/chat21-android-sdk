package  chat21.android.dao.groups;

import  chat21.android.core.groups.models.Group;

/**
 * Created by stefanodp91 on 26/09/17.
 */

public interface OnGroupRetrievedCallback {

    void onGroupRetrievedSuccess(String groupId, Group group);

    void onGroupRetrievedError(Exception e);
}
