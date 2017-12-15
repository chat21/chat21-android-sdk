package chat21.android.dao.groups;

import java.util.List;

import  chat21.android.core.groups.models.Group;

/**
 * Created by stefanodp91 on 26/09/17.
 */

public interface OnGroupsRetrievedCallback {

    void onGroupsRetrievedSuccess(List<Group> groups);

    void onGroupsRetrievedError(Exception e);
}