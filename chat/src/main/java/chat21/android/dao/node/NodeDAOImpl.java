
package chat21.android.dao.node;


import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import chat21.android.R;
import chat21.android.core.ChatManager;
import chat21.android.utils.StringUtils;

import static chat21.android.utils.DebugConstants.DEBUG_NODE_REF;

/**
 * Created by stefanodp91 on 08/09/17.
 */
public class NodeDAOImpl extends NodeDAOAbstract {
    private static final String TAG = NodeDAOImpl.class.getName();

    private static FirebaseDatabase mDatabase;

    public NodeDAOImpl(Context context) {
        super(context);
    }

    // source crash:
    // https://stackoverflow.com/questions/37448186/setpersistenceenabledtrue-crashes-app/37496319
    static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
//            // source :
//            // https://firebase.google.com/docs/database/android/offline-capabilities
//            try {
//                mDatabase.setPersistenceEnabled(true);
//            } catch (DatabaseException e) {
//                Log.e(TAG, e.getMessage());
//            }
        }
        return mDatabase;
    }

    @Override
    public DatabaseReference getNodeRoot() {
        Log.d(TAG, "getNodeRoot");

        DatabaseReference node = getDatabase()
                .getReferenceFromUrl(getContext().getString(R.string.root));
        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getNodeRoot: node == " + node.toString());

        return node;
    }

    @Override
    public DatabaseReference getNodeApps() {
        Log.d(TAG, "getNodeApps");

        DatabaseReference node = getNodeRoot()
                .child("apps");
        node.keepSynced(true);

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getNodeApps: node == " + node.toString());

        return node;
    }

    @Override
    public DatabaseReference getNodeApp() {
        Log.d(TAG, "getNodeApp");

        // TODO: 15/09/17 il tenant deve essere settato direttamente nel costruttore di questo dao
        String tenant;
        if (StringUtils.isValid(ChatManager.getInstance().getTenant())) {
            tenant = ChatManager.getInstance().getTenant();
        } else if (StringUtils.isValid(ChatManager.getInstance().getTenant())) {
            tenant = ChatManager.getInstance().getTenant();
        } else if (getContext() != null && StringUtils.isValid(getContext().getString(R.string.tenant))) {
            tenant = getContext().getString(R.string.tenant);
        } else {
            throw new RuntimeException("tenant is not valid");
        }
        Log.d(DEBUG_NODE_REF, "getNodeApp: TENANT == " + tenant);


        DatabaseReference node = getNodeApps()
                .child(tenant);
        node.keepSynced(true);

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getNodeApp: node == " + node.toString());
        return node;
    }

    @Override
    public DatabaseReference getNodeContacts() {
        Log.d(TAG, "getNodeContacts");

        DatabaseReference node = getNodeApp()
                .child("contacts");
        node.keepSynced(true);

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getNodeContacts: node == " + node.toString());
        return node;
    }

    @Override
    public DatabaseReference getNodeGroups() {
        Log.d(TAG, "getNodeGroups");

        DatabaseReference node = getNodeApp()
                .child("groups");
        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getNodeGroups: node == " + node.toString());

        return node;
    }

    @Override
    public DatabaseReference getNodeMessages() {
        Log.d(TAG, "getNodeMessages");

        DatabaseReference node = getNodeApp()
                .child("messages");
        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getNodeMessages: node == " + node.toString());

        return node;
    }

    // ############ PRESENCE ############ //
    @Override
    public DatabaseReference getNodePresence() {
        Log.d(TAG, "getNodePresence");

        DatabaseReference node = getNodeApp()
                .child("presence");
        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getNodePresence: node == " + node.toString());

        return node;
    }

    @Override
    public DatabaseReference getNodePresenceConnections(String userId) {
        Log.d(TAG, "getNodePresenceConnections");

        DatabaseReference node = getNodePresence()
                .child(userId)
                .child("connections");
        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getNodePresenceConnections: node == " + node.toString() +
                ", userId == " + userId);

        return node;
    }

    @Override
    public DatabaseReference getNodePresenceLastOnline(String userId) {
        Log.d(TAG, "getNodePresenceLastOnline");

        DatabaseReference node = getNodePresence()
                .child(userId)
                .child("lastOnline");
        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getNodePresenceLastOnline: node == " + node.toString() +
                ", userId == " + userId);

        return node;
    }

    @Override
    public DatabaseReference getNodePresenceConnectedMeta() {
        Log.d(TAG, "getNodePresenceConnectedMeta");

        String connectedRefURL = "/.info/connected";

        DatabaseReference node = getNodeRoot()
                .child(connectedRefURL);

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getNodePresenceConnectedMeta: node == " + node.toString());

        return node;
    }
    // ############ END PRESENCE ############ //

    @Override
    public DatabaseReference getNodeUsers() {
        Log.d(TAG, "getNodeUsers");

        DatabaseReference node = getNodeApp()
                .child("users");
        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getNodeUsers: node == " + node.toString());

        return node;
    }


    @Override
    public DatabaseReference getNodeConversations() {
        Log.d(TAG, "getNodeConversations");

        DatabaseReference node = getNodeUsers()
                .child(ChatManager.getInstance().getLoggedUser().getId())
                .child("conversations");
        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getNodeConversations: node == " + node.toString());

        return node;
    }

    // TODO: 15/09/17
    // attualmente è una sola instance nota come instanceId
    // in seguito diventerà "instances" con n istanze
    @Override
    public DatabaseReference getNodeInstances() {
        Log.d(TAG, "getNodeInstances");

        DatabaseReference node = getNodeUsers()
                .child(ChatManager.getInstance().getLoggedUser().getId())
                .child("instanceId");
        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getNodeInstances: node == " + node.toString());

        return node;
    }

    @Override
    public DatabaseReference getNodeConversations(String userId) {
        Log.d(TAG, "getNodeConversations: userId == " + userId);

        DatabaseReference node = getNodeUsers()
                .child(userId)
                .child("conversations");
        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getNodeConversations: userId == " + userId
                + ", node == " + node.toString());
        return node;
    }


    @Override
    public DatabaseReference getNodeConversation(String conversationId) {
        Log.d(TAG, "getNodeConversation: conversationId == " + conversationId);

        DatabaseReference node = getNodeMessages()
                .child(conversationId);
        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getNodeConversation: conversationId == " + conversationId
                + ", node == " + node.toString());

        return node;
    }

    @Override
    public DatabaseReference getGroupById(String groupId) {
        Log.d(TAG, "getGroupById: groupId == " + groupId);

        DatabaseReference node = getNodeGroups()
                .child(groupId);
        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getGroupById: groupId == " + groupId
                + ", node == " + node.toString());

        return node;
    }

    @Override
    public DatabaseReference getGroupMembersNode(String groupId) {
        Log.d(TAG, "getGroupMembersNode: groupId == " + groupId);

        DatabaseReference node = getNodeGroups()
                .child(groupId)
                .child("members");
        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getGroupMembersNode: groupId == " + groupId
                + ", node == " + node.toString());

        return node;
    }

    @Override
    public DatabaseReference getGroupAdmin(String groupId) {
        Log.d(TAG, "getGroupAdmin: groupId == " + groupId);

        DatabaseReference node = getNodeGroups()
                .child(groupId)
                .child("owner");
        node.keepSynced(true); // enable cache

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getGroupAdmin: groupId == " + groupId
                + ", node == " + node.toString());

        return node;
    }


    // return the firebase storage reference
    public StorageReference getStorageReference() {
        Log.d(TAG, "getStorageReference");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference node = storage.getReferenceFromUrl(
                getContext().getString(R.string.firebase_storage_reference));

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getStorageReference: node == " + node.toString());

        return node;
    }

    @Override
    public StorageReference getPublicStorageFolder() {
        Log.d(TAG, "getPublicStorageFolder");

        StorageReference node = getStorageReference()
                .child("public");

        Log.d(DEBUG_NODE_REF, "NodeDAOImpl.getPublicStorageFolder: node == " + node.toString());

        return node;
    }
}