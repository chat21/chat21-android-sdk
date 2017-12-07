package  chat21.android.dao.groups;

import  chat21.android.core.groups.models.Group;

/**
 * Created by stefanodp91 on 26/09/17.
 */

public interface GroupsDAO {

    void getGroupByID(String groupId, OnGroupRetrievedCallback callback);

    void getGroupsForUser(String userId, OnGroupsRetrievedCallback callback);

    void createGroup(Group group, OnGroupCreatedCallback callback);

    void updateGroup(String groupId, Group group, OnGroupUpdatedCallback callback);

    boolean isValidGroup(Group group);
}