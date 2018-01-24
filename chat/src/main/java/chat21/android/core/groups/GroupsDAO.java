package chat21.android.core.groups;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import chat21.android.core.ChatManager;
import chat21.android.core.groups.models.ChatGroup;
import chat21.android.utils.StringUtils;

import static chat21.android.utils.DebugConstants.DEBUG_GROUPS;

/**
 * Created by stefanodp91 on 26/09/17.
 */

public class GroupsDAO {

    private static final String TAG = GroupsDAO.class.getName();

    public void createGroup(ChatGroup chatGroup, final OnGroupCreatedCallback callback) {
        Log.d(DEBUG_GROUPS, "GroupsDAO.createGroup: chatGroup == " + chatGroup.toString());

        // retrieve the node groups
        DatabaseReference nodeGroups = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + ChatManager.getInstance().getAppId() + "/groups");
        Log.d(DEBUG_GROUPS, "GroupsDAO.createGroup: nodeGroups == " + nodeGroups.toString());

        // create a child for the node groups
        DatabaseReference nodeGroup = nodeGroups.push();
        Log.d(DEBUG_GROUPS, "GroupsDAO.createGroup: nodeGroup == " + nodeGroup.toString());

        // create a callback on chatGroup saving
        DatabaseReference.CompletionListener onGroupCreatedCallback = new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError,
                                   DatabaseReference databaseReference) {
                if (databaseError == null) {
                    // all done
                    Log.d(DEBUG_GROUPS, "GroupsDAO.createGroup.onGroupCreatedCallback" +
                            ".onComplete: databaseReference == " + databaseReference.toString());

                    String groupId = databaseReference.getKey();
                    Log.d(DEBUG_GROUPS, "GroupsDAO.createGroup.onGroupCreatedCallback" +
                            ".onComplete: groupId == " + groupId);

                    callback.onGroupCreatedSuccess(groupId);

                } else {
                    // errors
                    Log.e(DEBUG_GROUPS, "GroupsDAO.createGroup.onGroupCreatedCallback" +
                            ".onComplete" + databaseError.getMessage());

                    callback.onGroupCreatedError(new Exception(databaseError.getMessage()));
                }

            }
        };

        // save the chatGroup
        nodeGroup.setValue(chatGroup, onGroupCreatedCallback);
    }

    public void updateGroup(String groupId, ChatGroup chatGroup, final OnGroupUpdatedCallback callback) {
        if (GroupUtils.isValidGroup(chatGroup)) {
            Log.d(DEBUG_GROUPS, "GroupsDAO.updateGroup: groupId == " + groupId + ", chatGroup == " + chatGroup.toString());

            // retrieve the node users
            DatabaseReference nodeUsers = FirebaseDatabase.getInstance().getReference()
                    .child("apps/" + ChatManager.getInstance().getAppId() + "/users");
            Log.d(DEBUG_GROUPS, "GroupsDAO.updateGroup: nodeUsers == " + nodeUsers.toString());

            // retrieve the current user id

            String currentUserId = ChatManager.getInstance().getLoggedUser().getId();
            Log.d(DEBUG_GROUPS, "GroupsDAO.updateGroup: currentUserId == " + currentUserId);

            // retrieve the node for the current user
            DatabaseReference nodeCurrentUser = nodeUsers.child(currentUserId);
            Log.d(DEBUG_GROUPS, "GroupsDAO.updateGroup: nodeCurrentUser == " + nodeCurrentUser.toString());

            // retrieve the node groups for the current user
            DatabaseReference nodeGroups = nodeCurrentUser.child("groups");
            Log.d(DEBUG_GROUPS, "GroupsDAO.updateGroup: nodeGroups == " + nodeGroups.toString());

            // retrieve the chatGroup by its id
            DatabaseReference nodeGroup = nodeGroups.child(groupId);
            Log.d(DEBUG_GROUPS, "GroupsDAO.updateGroup: nodeGroup == " + nodeGroup.toString());


            // create a callback on chatGroup saving
            DatabaseReference.CompletionListener onGroupUpdatedCallback = new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError,
                                       DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        // all done
                        Log.d(DEBUG_GROUPS, "GroupsDAO.updateGroup.onGroupUpdatedCallback" +
                                ".onComplete: databaseReference == " + databaseReference.toString());

                        String groupId = databaseReference.getKey();
                        Log.d(DEBUG_GROUPS, "GroupsDAO.updateGroup.onGroupUpdatedCallback" +
                                ".onComplete: groupId == " + groupId);

                        callback.onGroupUpdatedSuccess(groupId);

                    } else {
                        // errors
                        Log.e(DEBUG_GROUPS, "GroupsDAO.updateGroup.onGroupUpdatedCallback" +
                                ".onComplete" + databaseError.getMessage());

                        callback.onGroupUpdatedError(new Exception(databaseError.getMessage()));
                    }

                }
            };

            // save the chatGroup
            nodeGroup.setValue(chatGroup, onGroupUpdatedCallback);


        } else {
            Log.e(DEBUG_GROUPS, "GroupsDAO.updateGroup: chatGroup is null. ");
            callback.onGroupUpdatedError(new Exception("chatGroup is null. "));
        }
    }

    public static void uploadGroup(String appId, String groupId, final ChatGroup chatGroup,
                                   final GroupUtils.OnGroupCreatedListener onGroupCreatedListener,
                                   final GroupUtils.OnGroupUpdatedListener onGroupUpdatedListener) {
        Log.d(TAG, "uploadGroup");

        if (chatGroup.getMembers().size() > 0) {
            if (!StringUtils.isValid(groupId)) {

                DatabaseReference nodeGroups;

                if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
                    nodeGroups = FirebaseDatabase.getInstance()
                            .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl)
                            .child("apps/" + ChatManager.getInstance().getAppId() + "/groups");
                } else {
                    nodeGroups = FirebaseDatabase.getInstance().getReference()
                            .child("apps/" + ChatManager.getInstance().getAppId() + "/groups");
                }

                nodeGroups.push().setValue(chatGroup, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        Log.d(TAG, "uploadGroup.onComplete");

                        if (databaseError != null) {
                            String errorMessage = "uploadGroup.onComplete: " +
                                    "chatGroup not uploaded. " + databaseError.getMessage();
                            onGroupCreatedListener.onGroupCreatedError(errorMessage);
                        } else {
                            Log.d(TAG, "chatGroup uploaded with success");

                            // example of database reference
                            // https://chat-95351.firebaseio.com/groups/-KncezORPzKzmAiIppSF
                            String groupId = databaseReference.getKey();

                            onGroupCreatedListener.onGroupCreatedSuccess(groupId, chatGroup);
                        }
                    }
                });
            } else {
                DatabaseReference nodeMembers;
                if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
                    nodeMembers = FirebaseDatabase.getInstance()
                            .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl)
                            .child("apps/" + appId + "/groups/" + groupId + "/members");
                } else {
                    nodeMembers = FirebaseDatabase.getInstance().getReference()
                            .child("apps/" + appId + "/groups/" + groupId + "/members");
                }

                nodeMembers.setValue(chatGroup.getMembers(),
                        new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError,
                                                   DatabaseReference databaseReference) {
                                Log.d(TAG, "uploadGroup.onComplete");

                                if (databaseError != null) {
                                    String errorMessage = "uploadGroup.onComplete: " +
                                            "chatGroup not uploaded. " + databaseError.getMessage();
                                    onGroupUpdatedListener.onGroupUpdatedError(errorMessage);
                                } else {
                                    Log.d(TAG, "chatGroup uploaded with success");

                                    DatabaseReference membersKey = databaseReference.getParent();
                                    String groupId = membersKey.getKey();

                                    onGroupUpdatedListener.onGroupUpdatedSuccess(groupId, chatGroup);
                                }
                            }
                        });
            }
        } else {
            String errorMessage = "uploadGroup: chatGroup not uploaded. " +
                    "cannot upload chatGroup. chatGroup size is less or equals 0";
            onGroupCreatedListener.onGroupCreatedError(errorMessage);
        }
    }

    public void getGroupsForUser(String userId, final OnGroupsRetrievedCallback callback) {
        Log.d(DEBUG_GROUPS, "GroupsDAO.getGroupsForUser: userId == " + userId);

        // retrieve the node users
        DatabaseReference nodeUsers = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + ChatManager.getInstance().getAppId() + "/users");
        Log.d(DEBUG_GROUPS, "GroupsDAO.getGroupsForUser: nodeUsers == " + nodeUsers.toString());

        // retrieve the node for the current user
        DatabaseReference nodeUser = nodeUsers.child(userId);
        Log.d(DEBUG_GROUPS, "GroupsDAO.getGroupsForUser: nodeUser == " + nodeUser.toString());

        // retrieve the node groups for the current user
        DatabaseReference nodeGroups = nodeUser.child("groups");
        Log.d(DEBUG_GROUPS, "GroupsDAO.getGroupsForUser: nodeGroups == " + nodeGroups.toString());

        // retrieve the group
        nodeGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(DEBUG_GROUPS, "GroupsDAO.getGroupsForUser.addValueEventListener" +
                        ".onDataChange: dataSnapshot == " + dataSnapshot.toString());

                List<ChatGroup> groupsList = getGroupsListFromDataSnapshot(dataSnapshot);
                if (groupsList != null && groupsList.size() > 0) {
                    Log.d(DEBUG_GROUPS, "GroupsDAO.getGroupsForUser.addValueEventListener" +
                            ".onDataChange: groupsMap == " + groupsList.toString());

                    callback.onGroupsRetrievedSuccess(groupsList);
                } else {
                    Log.e(DEBUG_GROUPS, "GroupsDAO.getGroupsForUser.addValueEventListener" +
                            ".onDataChange: groupsIdsList is null or empty");
                    callback.onGroupsRetrievedError(new Exception("groupsIdsList is null or empty"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // errors
                Log.e(DEBUG_GROUPS, "GroupsDAO.getGroupsForUser.addValueEventListener" +
                        ".onCancelled" + databaseError.getMessage());

                callback.onGroupsRetrievedError(new Exception(databaseError.getMessage()));
            }
        });
    }

    private List<ChatGroup> getGroupsListFromDataSnapshot(DataSnapshot dataSnapshot) {
        Log.d(DEBUG_GROUPS, "GroupsDAO.getGroupsListFromDataSnapshot:" +
                " dataSnapshot == " + dataSnapshot.toString());

        List<ChatGroup> groupsList = null;

        Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();

        if (value != null) {
            Log.d(DEBUG_GROUPS, "GroupsDAO.getGroupsListFromDataSnapshot:" +
                    " value == " + value.toString());

            groupsList = new ArrayList<>();

            // iterate each entry of groups map
            for (Map.Entry<String, Object> entry : value.entrySet()) {


                // retrieve the chatGroup map from dataset
                Map<String, Object> groupMap = (Map<String, Object>) entry.getValue();

                // create a new chatGroup
                ChatGroup chatGroup = new ChatGroup();

                // set chatGroup id
                String groupId = entry.getKey();
                chatGroup.setGroupId(groupId);

                // set name
                String name = (String) groupMap.get("name");
                chatGroup.setName(name);

                // set timestamp
                long createdOn = (long) groupMap.get("createdOn");
                chatGroup.setTimestamp(createdOn);

                // set icon
                String iconURL = (String) groupMap.get("iconURL");
                chatGroup.setIconURL(iconURL);

                // set owner
                String owner = (String) groupMap.get("owner");
                chatGroup.setOwner(owner);

                // set members
                Map<String, Integer> membersMap = (Map<String, Integer>) groupMap.get("members");
//                List<String> members = (List<String>) membersMap.keySet();

                chatGroup.setMembers(membersMap);

                // add chatGroup to map
                groupsList.add(chatGroup);
            }


        } else {
            Log.e(DEBUG_GROUPS, "GroupsDAO.getGroupsListFromDataSnapshot:" +
                    " value is null ");
        }

        if (groupsList != null) {
            Log.d(DEBUG_GROUPS, "GroupsDAO.getGroupsListFromDataSnapshot:" +
                    " groupsList == " + groupsList.toString());
        } else {
            Log.e(DEBUG_GROUPS, "GroupsDAO.getGroupsListFromDataSnapshot:" +
                    " groupsList is null ");
        }

        return groupsList;
    }
}