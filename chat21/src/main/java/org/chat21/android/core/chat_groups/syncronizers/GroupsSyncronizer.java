package org.chat21.android.core.chat_groups.syncronizers;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.chat21.android.core.chat_groups.listeners.ChatGroupCreatedListener;
import org.chat21.android.core.chat_groups.listeners.ChatGroupsListener;
import org.chat21.android.core.chat_groups.models.ChatGroup;
import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.utils.StringUtils;

import static org.chat21.android.utils.DebugConstants.DEBUG_GROUPS;

/**
 * Created by stefanodp91 on 24/01/18.
 */

public class GroupsSyncronizer {

    private List<ChatGroupsListener> chatGroupsListeners;
    private List<ChatGroup> chatGroups;
    private String currentUserId;
    private DatabaseReference appGroupsNode;
    private DatabaseReference userGroupsNode;
    private ChildEventListener groupsChildEventListener;

    public GroupsSyncronizer(String firebaseUrl, String appId, String currentUserId) {
        chatGroupsListeners = new ArrayList<>();
        chatGroups = new ArrayList<>(); // chatGroups in memory
        this.currentUserId = currentUserId;

        setupAppGroupsNode(firebaseUrl, appId);
        setupUserGroupsNode(firebaseUrl, appId, currentUserId);
    }

    private void setupAppGroupsNode(String firebaseUrl, String appId) {
        if (StringUtils.isValid(firebaseUrl)) {
            this.appGroupsNode = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(firebaseUrl)
                    .child("/apps/" + appId + "/groups/");
        } else {
            this.appGroupsNode = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("/apps/" + appId + "/groups/");
        }
        this.appGroupsNode.keepSynced(true);
        Log.d(DEBUG_GROUPS, "GroupsSyncronizer.appGroupsNode == " + appGroupsNode.toString());
    }

    private void setupUserGroupsNode(String firebaseUrl, String appId, String currentUserId) {
        if (StringUtils.isValid(firebaseUrl)) {
            this.userGroupsNode = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(firebaseUrl)
                    .child("/apps/" + appId + "/users/" + currentUserId + "/groups/");
        } else {
            this.userGroupsNode = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("/apps/" + appId + "/users/" + currentUserId + "/groups/");
        }
        this.userGroupsNode.keepSynced(true);
        Log.d(DEBUG_GROUPS, "GroupsSyncronizer.userGroupsNode == " + userGroupsNode.toString());
    }

    public ChildEventListener connect(ChatGroupsListener chatGroupsListener) {
        this.upsertGroupsListener(chatGroupsListener);
        return connect();
    }

    public ChildEventListener connect() {

        if (this.groupsChildEventListener == null) {

            this.groupsChildEventListener = userGroupsNode.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                    Log.d(DEBUG_GROUPS, "GroupsSyncronizer.connect.onChildAdded");

                    try {
                        ChatGroup chatGroup = decodeGroupFromSnapshot(dataSnapshot);

                        saveOrUpdateGroupInMemory(chatGroup);

                        if (chatGroupsListeners != null) {
                            for (ChatGroupsListener chatGroupsListener : chatGroupsListeners) {
                                chatGroupsListener.onGroupAdded(chatGroup, null);
                            }
                        }
                    } catch (Exception e) {
                        if (chatGroupsListeners != null) {
                            for (ChatGroupsListener chatGroupsListener : chatGroupsListeners) {
                                chatGroupsListener
                                        .onGroupAdded(null, new ChatRuntimeException(e));
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

                        if (chatGroupsListeners != null) {
                            for (ChatGroupsListener chatGroupsListener : chatGroupsListeners) {
                                chatGroupsListener.onGroupChanged(chatGroup, null);
                            }
                        }

                    } catch (Exception e) {
                        if (chatGroupsListeners != null) {
                            for (ChatGroupsListener chatGroupsListener : chatGroupsListeners) {
                                chatGroupsListener
                                        .onGroupChanged(null, new ChatRuntimeException(e));
                            }
                        }
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(DEBUG_GROUPS, "GroupsSyncronizer.connect.onChildRemoved");

                    try {
                        ChatGroup chatGroup = decodeGroupFromSnapshot(dataSnapshot);

                        saveOrUpdateGroupInMemory(chatGroup);

                        if (chatGroupsListeners != null) {
                            for (ChatGroupsListener chatGroupsListener : chatGroupsListeners) {
                                chatGroupsListener.onGroupRemoved(null);
                            }
                        }

                    } catch (Exception e) {
                        if (chatGroupsListeners != null) {
                            for (ChatGroupsListener chatGroupsListener : chatGroupsListeners) {
                                chatGroupsListener.onGroupRemoved(new ChatRuntimeException(e));
                            }
                        }
                    }
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
    public void saveOrUpdateGroupInMemory(ChatGroup newChatGroup) {

        // look for the group
        int index = -1;
        for (ChatGroup tempChatGroup : chatGroups) {
            if (tempChatGroup.getGroupId().equals(newChatGroup.getGroupId())) {
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

    public ChatGroup decodeGroupFromSnapshot(DataSnapshot dataSnapshot) {
//        Log.d(DEBUG_GROUPS, "GroupsSyncronizer.decodeGroupFromSnapshot:
// dataSnapshot == " + dataSnapshot.toString());
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

            try {
                String iconURL = (String) map.get("iconURL");
                chatGroup.setIconURL(iconURL);
            } catch (Exception e) {
                Log.w(DEBUG_GROUPS,  "GroupsSyncronizer.decodeGroupFromSnapshot: cannot retrieve iconURL");
            }

            String owner = (String) map.get("owner");
            chatGroup.setOwner(owner);

            long createdOn = (long) map.get("createdOn");
            chatGroup.setTimestamp(createdOn);

            String name = (String) map.get("name");
            chatGroup.setName(name);

            Map<String, Integer> members = (Map<String, Integer>) map.get("members");
            chatGroup.addMembers(members);
        }

        Log.d(DEBUG_GROUPS, "GroupsSyncronizer.decodeGroupFromSnapshot: " +
                "chatGroup == " + chatGroup.toString());
        return chatGroup;
    }

    public List<ChatGroupsListener> getGroupsListener() {
        return chatGroupsListeners;
    }

    public void addGroupsListener(ChatGroupsListener chatGroupsListener) {
        Log.v(DEBUG_GROUPS, "  addGroupsListener called");

        this.chatGroupsListeners.add(chatGroupsListener);

        Log.i(DEBUG_GROUPS, "  chatGroupsListener with hashCode: " +
                chatGroupsListener.hashCode() + " added");
    }

    public void removeGroupsListener(ChatGroupsListener chatGroupsListener) {
        Log.v(DEBUG_GROUPS, "  removeGroupsListener called");

        if (chatGroupsListeners != null)
            this.chatGroupsListeners.remove(chatGroupsListener);

        Log.i(DEBUG_GROUPS, "  chatGroupsListener with hashCode: " +
                chatGroupsListener.hashCode() + " removed");
    }

    public void upsertGroupsListener(ChatGroupsListener chatGroupsListener) {
        Log.v(DEBUG_GROUPS, "  upsertGroupsListener called");

        if (chatGroupsListeners.contains(chatGroupsListener)) {
            this.removeGroupsListener(chatGroupsListener);
            this.addGroupsListener(chatGroupsListener);

            Log.i(DEBUG_GROUPS, "  chatGroupsListener with hashCode: " +
                    chatGroupsListener.hashCode() + " updated");
        } else {
            this.addGroupsListener(chatGroupsListener);

            Log.i(DEBUG_GROUPS, "  chatGroupsListener with hashCode: " +
                    chatGroupsListener.hashCode() + " added");
        }
    }

    public void removeAllGroupsListeners() {
        this.chatGroupsListeners = null;

        Log.i(DEBUG_GROUPS, "Removed all chatGroupsListeners");
    }

    public ChildEventListener getGroupsChildEventListener() {
        return groupsChildEventListener;
    }

    public void disconnect() {
        this.userGroupsNode.removeEventListener(this.groupsChildEventListener);
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

    public void removeMemberFromChatGroup(String groupId, IChatUser toRemove) {
        ChatGroup chatGroup = getById(groupId);
        int index = chatGroups.indexOf(chatGroup);

        if (chatGroup.getMembers().containsKey(toRemove.getId())) {
            // remove from firebase app reference
            appGroupsNode.child("/" + groupId + "/members/" + toRemove.getId()).removeValue();

            // remove member from local group
            chatGroup.getMembers().remove(toRemove.getId());

            // update local chatGroups
            chatGroups.set(index, chatGroup);

            // notify all subscribers
            if (chatGroupsListeners != null) {
                for (ChatGroupsListener chatGroupsListener : chatGroupsListeners) {
                    chatGroupsListener.onGroupChanged(chatGroup, null);
                }
            }
        }
    }

    public void addMembersToChatGroup(String groupId, Map<String, Integer> toAdd) {
        ChatGroup chatGroup = getById(groupId);

        Map<String, Integer> chatGroupMembers = chatGroup.getMembers();

        // add the news member to the existing members
        // the map automatically override existing keys
        chatGroupMembers.putAll(toAdd);

        appGroupsNode.child("/" + chatGroup.getGroupId() + "/members/").setValue(chatGroupMembers);
    }

    public void createChatGroup(final String chatGroupName, Map<String, Integer> members,
                                final ChatGroupCreatedListener chatGroupCreatedListener) {

        DatabaseReference newGroupReference = this.appGroupsNode.push();
        String newGroupId = newGroupReference.getKey();

        final ChatGroup chatGroup = createGroupForFirebase(chatGroupName, members);
        chatGroup.setGroupId(newGroupId);

        newGroupReference.setValue(chatGroup, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.d(DEBUG_GROUPS, "GroupsSyncronizer.createChatGroup:" +
                            " databaseReference == " + databaseReference.toString());

                    saveOrUpdateGroupInMemory(chatGroup);

                    if (chatGroupCreatedListener != null) {
                        chatGroupCreatedListener.onChatGroupCreated(chatGroup, null);
                    }

                } else {
                    Log.e(DEBUG_GROUPS, "GroupsSyncronizer.createChatGroup:" +
                            " cannot create chatGroup " + databaseError.toException());
                    if (chatGroupCreatedListener != null) {
                        chatGroupCreatedListener.onChatGroupCreated(null,
                                new ChatRuntimeException(databaseError.toException()));
                    }
                }
            }
        });
    }

    private ChatGroup createGroupForFirebase(String chatGroupName, Map<String, Integer> members) {
        ChatGroup chatGroup = new ChatGroup();
        chatGroup.setName(chatGroupName);
        chatGroup.setMembers(members);
        chatGroup.setOwner(currentUserId);
        chatGroup.setTimestamp(new Date().getTime());
        return chatGroup;
    }
}