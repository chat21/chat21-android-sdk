package  chat21.android.dao.node;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

/**
 * Created by stefanodp91 on 08/09/17.
 */

public interface NodeDAO {
    /**
     * Returns the firebase root node
     *
     * @return the root node
     */
    DatabaseReference getNodeRoot();

    /**
     * Returns the firebase apps node
     *
     * @return the apps node
     */
    DatabaseReference getNodeApps();

    /**
     * Returns the firebase app node.
     * <p>
     * The app specified in specified Chat.Configuration
     * <p>
     * Each tenants is an app
     *
     * @return the app node
     */
    DatabaseReference getNodeApp();

    /**
     * Returns the firebase app node.
     * <p>
     * Each tenants is an app
     *
     * @return the app node
     */

    /**
     * Returns the node contacts for the app specified in Chat.Configuration.
     *
     * @return the node contacts
     */
    DatabaseReference getNodeContacts();

    /**
     * Returns the firebase node "groups" for the app specified in Chat.Configuration.
     *
     * @return the node "groups"
     */
    DatabaseReference getNodeGroups();

    /**
     * Returns the list of message of a specific conversation
     * for the app specified in Chat.Configuration.
     *
     * @return the list of messages
     */
    DatabaseReference getNodeMessages();

    /**
     * Returns the user presence with online/offline status and lastOnline time
     * for the app specified in Chat.Configuration.
     *
     * @return the user presence node
     */
    DatabaseReference getNodePresence();

    /**
     * Returns the user devices connections
     * for the app specified in Chat.Configuration.
     *
     * @param userId the user for whom to retrieve the connections
     * @return the user connections node
     */
    DatabaseReference getNodePresenceConnections(String userId);

    /**
     * Returns the user last online time
     * for the app specified in Chat.Configuration.
     *
     * @param userId the user for whom to retrieve the last online time
     * @return the user last online node
     */
    DatabaseReference getNodePresenceLastOnline(String userId);

    /**
     * Returns the firebase virtual meta data for user connection
     *
     * @return the firebase virtual meta data for user connection
     */
    DatabaseReference getNodePresenceConnectedMeta();

    /**
     * Return the firebase node "users" for the app specified in Chat.Configuration.
     *
     * @return the firebase node "users"
     */
    DatabaseReference getNodeUsers();


    /**
     * Returns the list of conversations of the current logged user
     *
     * @return the list of conversations
     */
    DatabaseReference getNodeConversations();

    /**
     * Returns the list of instances of the current logged user
     * for the app specified in Chat.Configuration.
     *
     * @return the list of instances
     */
    // TODO: 15/09/17
    // attualmente è una sola instance nota come instanceId
    // in seguito diventerà "instances" con n istanze
    DatabaseReference getNodeInstances();


    /**
     * Returns the list of conversations of a specific user from its user id
     *
     * @return the list of conversations
     */
    DatabaseReference getNodeConversations(String userId);


    /**
     * Returns the conversation with the selected conversationId
     *
     * @param conversationId the conversationId
     * @return the conversation
     */
    DatabaseReference getNodeConversation(String conversationId);


    /**
     * Returns a specific group from the "group" node by the groupId
     *
     * @param groupId the group to search
     * @return
     */
    DatabaseReference getGroupById(String groupId);

    /**
     * Returns members belonging to a group identified by its groupId
     *
     * @param groupId the group to search
     * @return
     */
    DatabaseReference getGroupMembersNode(String groupId);

    /**
     * Returns the admin of the group
     *
     * @param groupId the group to search inside
     * @return
     */
    DatabaseReference getGroupAdmin(String groupId);


    /**
     * return the firebase storage reference
     *
     * @return
     */
    StorageReference getStorageReference();

    /**
     * returns the public storage folder
     *
     * @return
     */
    StorageReference getPublicStorageFolder();


    Context getContext();

    void setContext(Context context);
}
