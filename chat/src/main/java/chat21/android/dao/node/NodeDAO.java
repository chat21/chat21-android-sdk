
package chat21.android.dao.node;


import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import chat21.android.R;
import chat21.android.core.ChatManager;

import static chat21.android.utils.DebugConstants.DEBUG_NODE_REF;

/**
 * Created by stefanodp91 on 08/09/17.
 */
public class NodeDAO {
    private static final String TAG = NodeDAO.class.getName();

    private String appId;

    public NodeDAO(String appId) {
        this.appId = appId;
    }

    /**
     * Returns the node contacts
     *
     * @return
     */
    public DatabaseReference getNodeContacts() {
        Log.d(TAG, "getNodeContacts");

        DatabaseReference node = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId + "/contacts");
//        node.keepSynced(true);

        Log.d(DEBUG_NODE_REF, "NodeDAO.getNodeContacts: node == " + node.toString());
        return node;
    }

    /**
     * Returns the firebase node "groups"
     *
     * @return the node "groups"
     */
    public DatabaseReference getNodeGroups() {
        Log.d(TAG, "getNodeGroups");

        DatabaseReference node = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId + "/groups");
//        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAO.getNodeGroups: node == " + node.toString());

        return node;
    }

    /**
     * Returns the list of message of a specific conversation
     *
     * @return the list of messages
     */
    public DatabaseReference getNodeMessages() {
        Log.d(TAG, "getNodeMessages");

        DatabaseReference node = getNodeUsers()
                .child("messages");
//        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAO.getNodeMessages: node == " + node.toString());

        return node;
    }

    /**
     * Returns the list of message of a specific conversation
     * for the user with recipientId
     *
     * @param recipientId
     * @return the list of messages
     */
    public DatabaseReference getNodeMessages(String recipientId) {
        Log.d(TAG, "getNodeMessages");

        DatabaseReference node = getNodeMessages(appId)
                .child(recipientId);
//        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAO.getNodeMessages: node == " + node.toString());

        return node;
    }


    /**
     * Return the firebase node "users"
     *
     * @return the firebase node "users"
     */
    public DatabaseReference getNodeUsers() {
        Log.d(TAG, "getNodeUsers");

        DatabaseReference node = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId + "/users");
//        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAO.getNodeUsers: node == " + node.toString());

        return node;
    }

    /**
     * Returns the list of conversations of the current logged user
     *
     * @return the list of conversations
     */
    public DatabaseReference getNodeConversations() {
        Log.d(TAG, "getNodeConversations");

        DatabaseReference node = getNodeUsers()
                .child(ChatManager.getInstance().getLoggedUser().getId())
                .child("conversations");
//        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAO.getNodeConversations: node == " + node.toString());

        return node;
    }


    /**
     * Returns the list of conversations of a specific user from its userId
     *
     * @param userId
     * @return the list of conversations
     */
    public DatabaseReference getNodeConversations(String userId) {
        Log.d(TAG, "getNodeConversations: userId == " + userId);

        DatabaseReference node = getNodeUsers()
                .child(userId)
                .child("conversations");
//        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAO.getNodeConversations: userId == " + userId
                + ", node == " + node.toString());
        return node;
    }


    /**
     * Returns the list of instances of the current logged user
     *
     * @return the list of instances
     */
    // TODO: 15/09/17
    // attualmente è una sola instance nota come instanceId
    // in seguito diventerà "instances" con n istanze
    public DatabaseReference getNodeInstances() {
        Log.d(TAG, "getNodeInstances");

        DatabaseReference node = getNodeUsers()
                .child(ChatManager.getInstance().getLoggedUser().getId())
                .child("instanceId");
//        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAO.getNodeInstances: node == " + node.toString());

        return node;
    }

    /**
     * Returns a specific group from the "group" node by the groupId
     *
     * @param groupId the group to search
     * @return
     */
    public DatabaseReference getGroupById(String groupId) {
        Log.d(TAG, "getGroupById: groupId == " + groupId);

        DatabaseReference node = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId + "/groups/" + groupId);
//        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAO.getGroupById: groupId == " + groupId
                + ", node == " + node.toString());

        return node;
    }

    /**
     * Returns members belonging to a group identified by its groupId
     *
     * @param groupId the group to search
     * @return
     */
    public DatabaseReference getGroupMembersNode(String groupId) {
        Log.d(TAG, "getGroupMembersNode: groupId == " + groupId);

        DatabaseReference node = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId + "/groups/" + groupId + "/members");
//        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAO.getGroupMembersNode: groupId == " + groupId
                + ", node == " + node.toString());

        return node;
    }

//    /**
//     * Returns the admin of the group
//     *
//     * @param groupId the group to search inside
//     * @return
//     */
//    public DatabaseReference getGroupAdmin(String groupId) {
//        Log.d(TAG, "getGroupAdmin: groupId == " + groupId);
//
//        DatabaseReference node = FirebaseDatabase.getInstance().getReference()
//                .child("apps/" + appId + "/groups/" + groupId + "/owner");
////        node.keepSynced(true); // enable cache
//
//        Log.d(DEBUG_NODE_REF, "NodeDAO.getGroupAdmin: groupId == " + groupId
//                + ", node == " + node.toString());
//
//        return node;
//    }

    /**
     * returns the public storage folder
     *
     * @return
     */
    public StorageReference getPublicStorageFolder(Context context) {
        Log.d(TAG, "getPublicStorageFolder");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference node = storage.getReferenceFromUrl(
                context.getString(R.string.firebase_storage_reference))
                .child("public");

        Log.d(DEBUG_NODE_REF, "NodeDAO.getPublicStorageFolder: node == " + node.toString());

        return node;
    }
}