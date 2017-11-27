package  chat21.android.dao.groups;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import chat21.android.core.ChatManager;

import  chat21.android.dao.node.NodeDAO;
import  chat21.android.dao.node.NodeDAOImpl;
import  chat21.android.groups.models.Group;

import static  chat21.android.utils.DebugConstants.DEBUG_NODE_GROUPS;

/**
 * Created by stefanodp91 on 26/09/17.
 */

public class GroupsDAOImpl extends AbstractGroupDAO {
    private NodeDAO mNodeDAO;

    public GroupsDAOImpl(Context context) {
        super(context);

        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl: constructor");

        mNodeDAO = new NodeDAOImpl(context);
    }

    @Override
    public void createGroup(Group group, final OnGroupCreatedCallback callback) {
        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroup: group == " + group.toString());

        // retrieve the node groups
        DatabaseReference nodeGroups = mNodeDAO.getNodeGroups();
        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroup: nodeGroups == " + nodeGroups.toString());

        // create a child for the node groups
        DatabaseReference nodeGroup = nodeGroups.push();
        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroup: nodeGroup == " + nodeGroup.toString());

        // create a callback on group saving
        DatabaseReference.CompletionListener onGroupCreatedCallback = new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError,
                                   DatabaseReference databaseReference) {
                if (databaseError == null) {
                    // all done
                    Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroup.onGroupCreatedCallback" +
                            ".onComplete: databaseReference == " + databaseReference.toString());

                    String groupId = databaseReference.getKey();
                    Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroup.onGroupCreatedCallback" +
                            ".onComplete: groupId == " + groupId);

                    callback.onGroupCreatedSuccess(groupId);

                } else {
                    // errors
                    Log.e(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroup.onGroupCreatedCallback" +
                            ".onComplete" + databaseError.getMessage());

                    callback.onGroupCreatedError(new Exception(databaseError.getMessage()));
                }

            }
        };

        // save the group
        nodeGroup.setValue(group, onGroupCreatedCallback);
    }

    @Override
    public void updateGroup(String groupId, Group group, final OnGroupUpdatedCallback callback) {
        if (isValidGroup(group)) {
            Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.updateGroup: groupId == " + groupId + ", group == " + group.toString());

            // retrieve the node users
            DatabaseReference nodeUsers = mNodeDAO.getNodeUsers();
            Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.updateGroup: nodeUsers == " + nodeUsers.toString());

            // retrieve the current user id

            String currentUserId = ChatManager.getInstance().getLoggedUser().getId();
            Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.updateGroup: currentUserId == " + currentUserId);

            // retrieve the node for the current user
            DatabaseReference nodeCurrentUser = nodeUsers.child(currentUserId);
            Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.updateGroup: nodeCurrentUser == " + nodeCurrentUser.toString());

            // retrieve the node groups for the current user
            DatabaseReference nodeGroups = nodeCurrentUser.child("groups");
            Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.updateGroup: nodeGroups == " + nodeGroups.toString());

            // retrieve the group by its id
            DatabaseReference nodeGroup = nodeGroups.child(groupId);
            Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.updateGroup: nodeGroup == " + nodeGroup.toString());


            // create a callback on group saving
            DatabaseReference.CompletionListener onGroupUpdatedCallback = new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError,
                                       DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        // all done
                        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.updateGroup.onGroupUpdatedCallback" +
                                ".onComplete: databaseReference == " + databaseReference.toString());

                        String groupId = databaseReference.getKey();
                        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.updateGroup.onGroupUpdatedCallback" +
                                ".onComplete: groupId == " + groupId);

                        callback.onGroupUpdatedSuccess(groupId);

                    } else {
                        // errors
                        Log.e(DEBUG_NODE_GROUPS, "GroupsDAOImpl.updateGroup.onGroupUpdatedCallback" +
                                ".onComplete" + databaseError.getMessage());

                        callback.onGroupUpdatedError(new Exception(databaseError.getMessage()));
                    }

                }
            };

            // save the group
            nodeGroup.setValue(group, onGroupUpdatedCallback);


        } else {
            Log.e(DEBUG_NODE_GROUPS, "GroupsDAOImpl.updateGroup: group is null. ");
            callback.onGroupUpdatedError(new Exception("group is null. "));
        }
    }

    @Override
    public void getGroupByID(final String groupId, final OnGroupRetrievedCallback callback) {
        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupByID: groupId == " + groupId);

        // retrieve the node users
        DatabaseReference nodeUsers = mNodeDAO.getNodeUsers();
        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupByID: nodeUsers == " + nodeUsers.toString());

        // retrieve the current user id

        String currentUserId = ChatManager.getInstance().getLoggedUser().getId();
        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupByID: currentUserId == " + currentUserId);

        // retrieve the node for the current user
        DatabaseReference nodeCurrentUser = nodeUsers.child(currentUserId);
        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupByID: nodeCurrentUser == " + nodeCurrentUser.toString());

        // retrieve the node groups for the current user
        DatabaseReference nodeGroups = nodeCurrentUser.child("groups");
        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupByID: nodeGroups == " + nodeGroups.toString());

        // retrieve the group by its id
        DatabaseReference nodeGroup = nodeGroups.child(groupId);
        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupByID: nodeGroup == " + nodeGroup.toString());

        // retrieve the group
        nodeGroup.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupByID.addValueEventListener" +
                        ".onDataChange: dataSnapshot == " + dataSnapshot.toString());

                Group group = createGroupFromDataSnapshot(dataSnapshot);

                if (isValidGroup(group)) {
                    Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupByID.addValueEventListener" +
                            ".onDataChange: group == " + group.toString());

                    callback.onGroupRetrievedSuccess(groupId, group);
                } else {
                    Log.e(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupByID.addValueEventListener" +
                            ".onDataChange: group is null ");
                    callback.onGroupRetrievedError(new Exception("group is null. "));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // errors
                Log.e(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupByID.addValueEventListener" +
                        ".onCancelled" + databaseError.getMessage());

                callback.onGroupRetrievedError(new Exception(databaseError.getMessage()));
            }
        });
    }

    @Override
    public void getGroupsForUser(String userId, final OnGroupsRetrievedCallback callback) {
        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupsForUser: userId == " + userId);

        // retrieve the node users
        DatabaseReference nodeUsers = mNodeDAO.getNodeUsers();
        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupsForUser: nodeUsers == " + nodeUsers.toString());

        // retrieve the node for the current user
        DatabaseReference nodeUser = nodeUsers.child(userId);
        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupsForUser: nodeUser == " + nodeUser.toString());

        // retrieve the node groups for the current user
        DatabaseReference nodeGroups = nodeUser.child("groups");
        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupsForUser: nodeGroups == " + nodeGroups.toString());

        // retrieve the group
        nodeGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupsForUser.addValueEventListener" +
                        ".onDataChange: dataSnapshot == " + dataSnapshot.toString());

                List<Group> groupsList = getGroupsListFromDataSnapshot(dataSnapshot);
                if (groupsList != null && groupsList.size() > 0) {
                    Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupsForUser.addValueEventListener" +
                            ".onDataChange: groupsMap == " + groupsList.toString());

                    callback.onGroupsRetrievedSuccess(groupsList);
                } else {
                    Log.e(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupsForUser.addValueEventListener" +
                            ".onDataChange: groupsIdsList is null or empty");
                    callback.onGroupsRetrievedError(new Exception("groupsIdsList is null or empty"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // errors
                Log.e(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupsForUser.addValueEventListener" +
                        ".onCancelled" + databaseError.getMessage());

                callback.onGroupsRetrievedError(new Exception(databaseError.getMessage()));
            }
        });
    }

    // DataSnapshot {
//  key = -KuxZBrsyCgxuRjUlI7Y,
//  value = {
//      name=test dao 3,
//      createdOn=1506418937575,
//      iconURL=NOICON,
//      members={
//          test2=1,
//          stefano_depascalis=1
//      },
//      owner=stefano_depascalis
//  }
// }
    private Group createGroupFromDataSnapshot(DataSnapshot dataSnapshot) {
        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroupFromDataSnapshot:" +
                " dataSnapshot == " + dataSnapshot.toString());

        Group group = null;

        Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();

        if (value != null) {
            Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroupFromDataSnapshot:" +
                    " value == " + value.toString());

            // create the group
            group = new Group();

            // get name
            String name;
            try {
                name = (String) value.get("name");
                group.setName(name);
            } catch (Exception e) {
                Log.e(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroupFromDataSnapshot:" +
                        "cannot retrieve name. " + e.getMessage());
                name = null;
            }
            Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroupFromDataSnapshot:" +
                    " name == " + name);


            // get timestamp
            long timestamp;
            try {
                timestamp = (long) value.get("createdOn");
                group.setTimestamp(timestamp);
            } catch (Exception e) {
                Log.e(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroupFromDataSnapshot:" +
                        "cannot retrieve timestamp. " + e.getMessage());
                timestamp = 0L;
            }
            Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroupFromDataSnapshot:" +
                    " createdOn == " + timestamp);


            // get iconURL
            String iconURL;
            try {
                iconURL = (String) value.get("iconURL");
                group.setIconURL(iconURL);
            } catch (Exception e) {
                Log.e(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroupFromDataSnapshot:" +
                        "cannot retrieve iconURL. " + e.getMessage());
                iconURL = "NOICON";
            }
            Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroupFromDataSnapshot:" +
                    " iconURL == " + iconURL);


            // get owner
            String owner;
            try {
                owner = (String) value.get("owner");
                group.setOwner(owner);
            } catch (Exception e) {
                Log.e(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroupFromDataSnapshot:" +
                        "cannot retrieve owner. " + e.getMessage());
                owner = null;
            }
            Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroupFromDataSnapshot:" +
                    " owner == " + owner);


            // get members
            Map<String, Integer> members;
            try {
                members = (Map<String, Integer>) value.get("members");

                group.addMembers(members);

            } catch (Exception e) {
                Log.e(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroupFromDataSnapshot:" +
                        "cannot retrieve owner. " + e.getMessage());
                members = new HashMap<>();
            }
            Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroupFromDataSnapshot:" +
                    " members == " + members);
        } else {
            Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroupFromDataSnapshot:" +
                    " value is null ");
        }

        if (group != null) {
            Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroupFromDataSnapshot:" +
                    " group == " + group.toString());
        } else {
            Log.e(DEBUG_NODE_GROUPS, "GroupsDAOImpl.createGroupFromDataSnapshot:" +
                    " group is null ");
        }

        return group;
    }

    // DataSnapshot {
//  key = groups,
//  value = {
//      -KuxpQdNXubMP-IeHACf={
//          name=gruppo Stefano,
//          createdOn=1506423454684,
//          iconURL=NOICON,
//          members={
//              stefano_depascalis=1,
//              testtest=1
//          },
//          owner=stefano_depascalis
//      },
//      -KuxpjJkA2ZTQUb-GAzt={
//          name=😎😎😎😎😎,
//          createdOn=1506423535766,
//          iconURL=NOICON,
//          members={
//              stefano_depascalis=1,
//              test1_test=1
//          },
//          owner=stefano_depascalis
//      },
//      -Kuxpexgj0Vb7mZd9FLB={
//          name=altro gruppo,
//          createdOn=1506423517756,
//          iconURL=NOICON,
//          members={
//              stefano_depascalis=1,
//              test1_test=1,
//              testtest=1
//          },
//      owner=stefano_depascalis
//      }
//  }
// }
    private List<Group> getGroupsListFromDataSnapshot(DataSnapshot dataSnapshot) {
        Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupsListFromDataSnapshot:" +
                " dataSnapshot == " + dataSnapshot.toString());

        List<Group> groupsList = null;

        Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();

        if (value != null) {
            Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupsListFromDataSnapshot:" +
                    " value == " + value.toString());

            groupsList = new ArrayList<>();

            // iterate each entry of groups map
            for (Map.Entry<String, Object> entry : value.entrySet()) {


                // retrieve the group map from dataset
                Map<String, Object> groupMap = (Map<String, Object>) entry.getValue();

                // create a new group
                Group group = new Group();

                // set group id
                String groupId = entry.getKey();
                group.setGroupId(groupId);

                // set name
                String name = (String) groupMap.get("name");
                group.setName(name);

                // set timestamp
                long createdOn = (long) groupMap.get("createdOn");
                group.setTimestamp(createdOn);

                // set icon
                String iconURL = (String) groupMap.get("iconURL");
                group.setIconURL(iconURL);

                // set owner
                String owner = (String) groupMap.get("owner");
                group.setOwner(owner);

                // set members
                Map<String, Integer> membersMap = (Map<String, Integer>) groupMap.get("members");
//                List<String> members = (List<String>) membersMap.keySet();

                group.addMembers(membersMap);

                // add group to map
                groupsList.add(group);
            }


        } else {
            Log.e(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupsListFromDataSnapshot:" +
                    " value is null ");
        }

        if (groupsList != null) {
            Log.d(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupsListFromDataSnapshot:" +
                    " groupsList == " + groupsList.toString());
        } else {
            Log.e(DEBUG_NODE_GROUPS, "GroupsDAOImpl.getGroupsListFromDataSnapshot:" +
                    " groupsList is null ");
        }

        return groupsList;
    }
}