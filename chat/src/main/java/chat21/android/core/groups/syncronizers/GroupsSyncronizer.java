package chat21.android.core.groups.syncronizers;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import chat21.android.core.exception.ChatRuntimeException;
import chat21.android.core.groups.listeners.GroupsListener;
import chat21.android.core.groups.models.ChatGroup;
import chat21.android.utils.StringUtils;

import static chat21.android.utils.DebugConstants.DEBUG_GROUPS;

/**
 * Created by stefanodp91 on 24/01/18.
 */

public class GroupsSyncronizer {

    private List<ChatGroup> chatGroups;
    private DatabaseReference groupsNode;
    private String appId;
    private String currentUserId;
    private List<GroupsListener> groupsListeners;
    private ChildEventListener groupsChildEventListener;

    public GroupsSyncronizer(String firebaseUrl, String appId, String currentUserId) {
        groupsListeners = new ArrayList<>();
        chatGroups = new ArrayList<>(); // chatGroups in memory

        this.appId = appId;
        this.currentUserId = currentUserId;

        if (StringUtils.isValid(firebaseUrl)) {
            this.groupsNode = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(firebaseUrl)
                    .child("/apps/" + appId + "/users/" + currentUserId + "/groups/");
        } else {
            this.groupsNode = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("/apps/" + appId + "/users/" + currentUserId + "/groups/");
        }
        this.groupsNode.keepSynced(true);

        Log.d(DEBUG_GROUPS, "GroupsSyncronizer.groupsNode == " + groupsNode.toString());
    }

    public ChildEventListener connect(GroupsListener groupsListener) {
        this.upsertGroupsListener(groupsListener);
        return connect();
    }

    public ChildEventListener connect() {

        if (this.groupsChildEventListener == null) {

            this.groupsChildEventListener = groupsNode.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                    Log.d(DEBUG_GROUPS, "GroupsSyncronizer.connect.onChildAdded");

                    try {
                        ChatGroup chatGroup = decodeGroupFromSnapshot(dataSnapshot);

                        saveOrUpdateGroupInMemory(chatGroup);

                        if (groupsListeners != null) {
                            for (GroupsListener groupsListener : groupsListeners) {
                                groupsListener.onGroupAdded(chatGroup, null);
                            }
                        }
                    } catch (Exception e) {
                        if (groupsListeners != null) {
                            for (GroupsListener groupsListener : groupsListeners) {
                                groupsListener.onGroupAdded(null, new ChatRuntimeException(e));
                            }
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                    Log.d(DEBUG_GROUPS, "GroupsSyncronizer.connect.onChildChanged");

                    try {
                        ChatGroup chatGroup = decodeGroupFromSnapshot(dataSnapshot);

                        saveOrUpdateGroupInMemory(chatGroup);

                        if (groupsListeners != null) {
                            for (GroupsListener groupsListener : groupsListeners) {
                                groupsListener.onGroupChanged(chatGroup, null);
                            }
                        }

                    } catch (Exception e) {
                        if (groupsListeners != null) {
                            for (GroupsListener groupsListener : groupsListeners) {
                                groupsListener.onGroupChanged(null, new ChatRuntimeException(e));
                            }
                        }
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(DEBUG_GROUPS, "GroupsSyncronizer.connect.onChildRemoved");

                    // TODO: 24/01/18
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
//                 Log.d(TAG, "GroupsSyncronizer.connect.onChildMoved");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
//                  Log.d(TAG, "GroupsSyncronizer.connect.onCancelled");

                }
            });
        } else {
            Log.i(DEBUG_GROUPS, "already connected : ");
        }

        return groupsChildEventListener;
    }

    public List<ChatGroup> getChatGroups() {
        return chatGroups;
    }

    // it checks if the group already exists.
    // if the group exists update it, add it otherwise
    private void saveOrUpdateGroupInMemory(ChatGroup newChatGroup) {

        // look for the group
        int index = -1;
        for (ChatGroup tempChatGroup : chatGroups) {
            if (tempChatGroup.equals(newChatGroup)) {
                index = chatGroups.indexOf(tempChatGroup);
                break;
            }
        }

        if (index != -1) {
            // group already exists
            chatGroups.set(index, newChatGroup); // update the existing group
        } else {
            // group not exists
            chatGroups.add(newChatGroup); // insert a new group
        }
    }

    // it checks if the group already exists.
    // if the group exists delete it
    private void deleteGroupFromMemory(ChatGroup chatGroupToDelete) {
        // look for the group
        int index = -1;
        for (ChatGroup tempChatGroup : chatGroups) {
            if (tempChatGroup.equals(chatGroupToDelete)) {
                index = chatGroups.indexOf(tempChatGroup);
                break;
            }
        }

        if (index != -1) {
            // group already exists
            chatGroups.remove(index); // delete existing group
        }
    }

    public static ChatGroup decodeGroupFromSnapshot(DataSnapshot dataSnapshot) {
//        Log.d(DEBUG_GROUPS, "GroupsSyncronizer.decodeGroupFromSnapshot: dataSnapshot == " + dataSnapshot.toString());
//        DataSnapshot {
//            key = -L3XgCm9Fpve_cV5Ma_z,
//            value = {
//                    owner=Ua1JIHK8VLVzsuRIPM2ai0xScNi2,
//                    createdOn=1516705471863,
//                    members={
//                            Ua1JIHK8VLVzsuRIPM2ai0xScNi2=1,
//                            etWruToogVdIyLztne1tBu3VR902=1
//                    },
//                    name=Creato da iPhone
//            }
//        }

        ChatGroup chatGroup = new ChatGroup();
        chatGroup.setGroupId(dataSnapshot.getKey());

        if (dataSnapshot.getValue() != null) {
            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

            String owner = (String) map.get("owner");
            chatGroup.setOwner(owner);

            long createdOn = (long) map.get("createdOn");
            chatGroup.setTimestamp(createdOn);

            String name = (String) map.get("name");
            chatGroup.setName(name);

            Map<String, Integer> members = (Map<String, Integer>) map.get("members");
            chatGroup.setMembers(members);
        }

        Log.d(DEBUG_GROUPS, "GroupsSyncronizer.decodeGroupFromSnapshot: chatGroup == " + chatGroup.toString());
        return chatGroup;
    }

    public List<GroupsListener> getGroupsListener() {
        return groupsListeners;
    }

    public void addGroupsListener(GroupsListener groupsListener) {
        Log.v(DEBUG_GROUPS, "  addGroupsListener called");

        this.groupsListeners.add(groupsListener);

        Log.i(DEBUG_GROUPS, "  groupsListener with hashCode: " + groupsListener.hashCode() + " added");
    }

    public void removeGroupsListener(GroupsListener groupsListener) {
        Log.v(DEBUG_GROUPS, "  removeGroupsListener called");

        if (groupsListeners != null)
            this.groupsListeners.remove(groupsListener);

        Log.i(DEBUG_GROUPS, "  groupsListener with hashCode: " + groupsListener.hashCode() + " removed");
    }

    public void upsertGroupsListener(GroupsListener groupsListener) {
        Log.v(DEBUG_GROUPS, "  upsertGroupsListener called");

        if (groupsListeners.contains(groupsListener)) {
            this.removeGroupsListener(groupsListener);
            this.addGroupsListener(groupsListener);

            Log.i(DEBUG_GROUPS, "  groupsListener with hashCode: " + groupsListener.hashCode() + " updated");
        } else {
            this.addGroupsListener(groupsListener);

            Log.i(DEBUG_GROUPS, "  groupsListener with hashCode: " + groupsListener.hashCode() + " added");
        }
    }

    public void removeAllGroupsListeners() {
        this.groupsListeners = null;

        Log.i(DEBUG_GROUPS, "Removed all groupsListeners");
    }

    public ChildEventListener getGroupsChildEventListener() {
        return groupsChildEventListener;
    }

    public void disconnect() {
        this.groupsNode.removeEventListener(this.groupsChildEventListener);
        this.removeAllGroupsListeners();
    }

    /**
     * It looks for the group with {@code groupId}
     *
     * @param groupId the group id to looking for
     * @return the group if exists, null otherwise
     */
    public ChatGroup getById(String groupId) {
        for (ChatGroup chatGroup : chatGroups) {
            if (chatGroup.getGroupId().equals(groupId)) {
                return chatGroup;
            }
        }
        return null;
    }
}