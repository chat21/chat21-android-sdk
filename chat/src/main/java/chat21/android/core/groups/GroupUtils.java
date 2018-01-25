package chat21.android.core.groups;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import chat21.android.core.ChatManager;
import chat21.android.core.groups.models.ChatGroup;
import chat21.android.utils.StringUtils;

/**
 * Created by stefanodp91 on 14/07/17.
 */

public class GroupUtils {
    private static final String TAG = GroupUtils.class.getName();

    public static boolean isValidGroup(ChatGroup chatGroup) {
        if (chatGroup != null) {

            boolean isValidName = StringUtils.isValid(chatGroup.getName());
            boolean isValidMembers = chatGroup.getMembers() != null && chatGroup.getMembers().size() > 0;
            boolean isValidOwner = StringUtils.isValid(chatGroup.getOwner());
            boolean isValidTimestamp = chatGroup.getCreatedOnLong() != 0;

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
    public static ChatGroup decodeGroupSnapShot(DataSnapshot dataSnapshot) {
        Log.d(TAG, "decodeGroupSnapShot");

        ChatGroup chatGroup = new ChatGroup();

//        String groupId = dataSnapshot.getKey();

        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            String key = snapshot.getKey();

            if (key.equals("members")) {
                Map<String, Integer> membersMap = (Map<String, Integer>) snapshot.getValue();
                chatGroup.setMembers(membersMap);
            } else if (key.equals("owner")) {
                String owner = (String) snapshot.getValue();
                chatGroup.setOwner(owner);
            } else if (key.equals("createdOn")) {
                long createdOn = (long) snapshot.getValue();
                chatGroup.setTimestamp(createdOn);
            } else if (key.equals("iconURL")) {
                String iconUrl = (String) snapshot.getValue();
                chatGroup.setIconURL(iconUrl);
            } else if (key.equals("name")) {
                String name = (String) snapshot.getValue();
                chatGroup.setName(name);
            }
        }

        return chatGroup;
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

                ChatGroup chatGroup = GroupUtils.decodeGroupSnapShot(dataSnapshot);

                onGroupsChangeListener.onGroupChanged(chatGroup, groupId);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onGroupsChangeListener.onGroupCancelled(databaseError.getMessage());
            }
        });

    }

    public static boolean isAnAdmin(ChatGroup chatGroup, String userId) {
        Log.d(TAG, "isAnAdmin");

        return userId.equals(chatGroup.getOwner()) && chatGroup.getMembers().containsKey(userId) ? true : false;
    }


    public interface OnGroupsChangeListener {
        void onGroupChanged(ChatGroup chatGroup, String groupId);

        void onGroupCancelled(String errorMessage);
    }

    public interface OnGroupCreatedListener {
        void onGroupCreatedSuccess(String groupId, ChatGroup chatGroup);

        void onGroupCreatedError(String errorMessage);
    }

    public interface OnGroupUpdatedListener {
        void onGroupUpdatedSuccess(String groupId, ChatGroup chatGroup);

        void onGroupUpdatedError(String errorMessage);
    }
}