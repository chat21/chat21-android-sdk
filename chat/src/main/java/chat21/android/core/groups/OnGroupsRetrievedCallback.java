package chat21.android.core.groups;

import java.util.List;

import chat21.android.core.groups.models.ChatGroup;

/**
 * Created by stefanodp91 on 26/09/17.
 */

public interface OnGroupsRetrievedCallback {

    void onGroupsRetrievedSuccess(List<ChatGroup> chatGroups);

    void onGroupsRetrievedError(Exception e);
}