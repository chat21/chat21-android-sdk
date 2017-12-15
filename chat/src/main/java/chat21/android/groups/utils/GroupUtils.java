package chat21.android.groups.utils;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat21.android.core.ChatManager;
import chat21.android.core.groups.models.Group;
import chat21.android.core.users.models.ChatUser;
import chat21.android.core.users.models.IChatUser;
import chat21.android.utils.StringUtils;

/**
 * Created by stefanodp91 on 14/07/17.
 */

public class GroupUtils {
    private static final String TAG = GroupUtils.class.getName();

    public static boolean isValidGroup(Group group) {
        if (group != null) {

            boolean isValidName = StringUtils.isValid(group.getName());
            boolean isValidMembers = group.getMembers() != null && group.getMembers().size() > 0;
            boolean isValidOwner = StringUtils.isValid(group.getOwner());
            boolean isValidTimestamp = group.getCreatedOnLong() != 0;

            if (isValidName && isValidMembers && isValidOwner && isValidTimestamp)
                return true;
            else
                return false;
        } else {
            return false;
        }
    }

    /**
     * @param dataSnapshot the datasnapshot to decode
     * @return the decoded group
     */
    public static Group decodeGroupSnapShot(DataSnapshot dataSnapshot) {
        Log.d(TAG, "decodeGroupSnapShot");

        Group group = new Group();

//        String groupId = dataSnapshot.getKey();

        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            String key = snapshot.getKey();

            if (key.equals("members")) {
                Map<String, Integer> membersMap = (Map<String, Integer>) snapshot.getValue();
                group.addMembers(membersMap);
            } else if (key.equals("owner")) {
                String owner = (String) snapshot.getValue();
                group.setOwner(owner);
            } else if (key.equals("createdOn")) {
                long createdOn = (long) snapshot.getValue();
                group.setTimestamp(createdOn);
            } else if (key.equals("iconURL")) {
                String iconUrl = (String) snapshot.getValue();
                group.setIconURL(iconUrl);
            } else if (key.equals("name")) {
                String name = (String) snapshot.getValue();
                group.setName(name);
            }
        }

        return group;
    }

    public static void subscribeOnGroupsChanges(String appId, final String groupId,
                                                final OnGroupsChangeListener onGroupsChangeListener) {

        // retrieve group
        DatabaseReference nodeGroup = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId + "/groups/" + groupId);

        Log.d(TAG, "subscribeOnGroupsChanges.nodeGroup: " + nodeGroup.getRef());

        nodeGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

//                Log.d(TAG, dataSnapshot.toString());

                Group group = GroupUtils.decodeGroupSnapShot(dataSnapshot);

                onGroupsChangeListener.onGroupChanged(group, groupId);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onGroupsChangeListener.onGroupCancelled(databaseError.getMessage());
            }
        });

    }

    public static void subscribeOnNodeMembersChanges(String appId, String groupId,
                                                     final OnNodeMembersChangeListener onNodeMembersChangeListener) {

        DatabaseReference nodeGroup = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId + "/groups/" + groupId);

        // subscribe for members change
        nodeGroup.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded");

                if (dataSnapshot.getKey().equals("members")) {
                    Map<String, Integer> membersMap = (Map<String, Integer>) dataSnapshot.getValue();

                    List<IChatUser> members = new ArrayList<IChatUser>();

                    // bugfix Issue #18
                    for (Map.Entry<String, Integer> member : membersMap.entrySet()) {
                        IChatUser mUser = new ChatUser();
                        mUser.setId(member.getKey());
                        mUser.setFullName(member.getKey());
                        members.add(mUser);
                    }

                    onNodeMembersChangeListener.onMembersAdded(members);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged");

                if (dataSnapshot.getKey().equals("members")) {
                    Map<String, Integer> membersMap = (Map<String, Integer>) dataSnapshot.getValue();

                    List<IChatUser> members = new ArrayList<IChatUser>();

                    // bugfix Issue #18
                    for (Map.Entry<String, Integer> member : membersMap.entrySet()) {
                        IChatUser mUser = new ChatUser();
                        mUser.setId(member.getKey());
                        mUser.setFullName(member.getKey());
                        members.add(mUser);
                    }

                    onNodeMembersChangeListener.onNodeMembersChanged(members);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved");

                onNodeMembersChangeListener.onNodeMembersRemoved();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildMoved");

                onNodeMembersChangeListener.onNodeMembersMoved();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled");

                onNodeMembersChangeListener.onNodeMembersCancelled(databaseError.toString());
            }
        });
    }

    public static boolean isAnAdmin(Group group, String userId) {
        Log.d(TAG, "isAnAdmin");

        return userId.equals(group.getOwner())
                && group.getMembers().containsKey(userId) ? true : false;
    }

    public static String getGroupMembersAsList(Map<String, Integer> membersMap) {
        String members = "";

        for (Map.Entry<String, Integer> entry : membersMap.entrySet()) {
            String groupUserId = entry.getKey();

            String denormalizedUserId = groupUserId.replace("_", ".");

            // if the member is not the current user shows the member username
            if (!groupUserId.equals(ChatManager.getInstance().getLoggedUser().getId())) {
                members += (denormalizedUserId + ", ");
            }
        }

//        // add the current logged user as first member of the group
//        members = context.getString(R.string.activity_message_list_group_info_you_label) + ", " + members;

        // remove empty spaces
        members = members.trim();

        // if the member string end with the separator, remove it
        if (members.endsWith(","))
            members = members.substring(0, members.length() - 1);

        return members;
    }

    public static Map<String, Object> decodeGroupMembersSnapshop(DataSnapshot dataSnapshot) {
        Map<String, Object> members = new HashMap<>();

        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
            members = (Map<String, Object>) dataSnapshot.getValue();
        }

        return members;
    }

    public interface OnGroupsChangeListener {
        void onGroupChanged(Group group, String groupId);

        void onGroupCancelled(String errorMessage);
    }

    // Exposes the firebase listener methods
    // these methods are called on firebase change events
    public interface OnNodeMembersChangeListener {
        void onMembersAdded(List<IChatUser> members);

        void onNodeMembersChanged(List<IChatUser> members);

        void onNodeMembersRemoved();

        void onNodeMembersMoved();

        void onNodeMembersCancelled(String errormessage);
    }

    public interface OnGroupCreatedListener {
        void onGroupCreatedSuccess(String groupId, Group group);

        void onGroupCreatedError(String errorMessage);
    }

    public interface OnGroupUpdatedListener {
        void onGroupUpdatedSuccess(String groupId, Group group);

        void onGroupUpdatedError(String errorMessage);
    }
}