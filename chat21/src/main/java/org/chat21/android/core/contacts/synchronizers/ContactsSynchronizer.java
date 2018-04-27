package org.chat21.android.core.contacts.synchronizers;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.contacts.listeners.ContactListener;
import org.chat21.android.core.exception.ChatFieldNotFoundException;
import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.utils.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.chat21.android.utils.DebugConstants.DEBUG_CONTACTS_SYNC;

/**
 * Created by andrealeo on 04/01/18.
 */

public class ContactsSynchronizer {

    private CopyOnWriteArrayList<IChatUser> contacts = new CopyOnWriteArrayList<>(); // contacts in memory

    private DatabaseReference contactsNode;

    private ChildEventListener contactsChildEventListener;

    private List<ContactListener> contactListeners;

    public ContactsSynchronizer(String firebaseUrl, String appId) {

        contactListeners = new ArrayList<>();

        if (StringUtils.isValid(firebaseUrl)) {
            this.contactsNode = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(firebaseUrl)
                    .child("/apps/" + appId + "/contacts/");
        } else {
            this.contactsNode = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("/apps/" + appId + "/contacts/");
        }
        this.contactsNode.keepSynced(true);

        Log.d(DEBUG_CONTACTS_SYNC, "contactsNode : " + contactsNode.toString());
    }

    public synchronized ChildEventListener connect() {
        Log.d(DEBUG_CONTACTS_SYNC, "connecting  for contacts ");

//        contactsNode.orderByChild("firstname").addListenerForSingleValueEvent(new ValueEventListener() {
//            public void onDataChange(DataSnapshot dataSnapshot) {
//               Log.d("TAG", dataSnapshot.toString());
//               Log.d("TAG", "We're done loading the initial " + dataSnapshot.getChildrenCount() + " items");
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        if (contactsChildEventListener == null) {

            Log.d(DEBUG_CONTACTS_SYNC, "creating a new contactsChildEventListener");

            contactsChildEventListener = contactsNode.orderByChild("firstname").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                    new AsyncTask<DataSnapshot, Void, Void>() {
                        @Override
                        protected Void doInBackground(final DataSnapshot... snapshots) {
                            new Thread(new Runnable() {
                                public void run() {
                                    Log.v(DEBUG_CONTACTS_SYNC, "ContactsSynchronizer.connect.onChildAdded");

                                    try {
                                        IChatUser contact = decodeContactSnapShop(snapshots[0]);
                                        Log.d(DEBUG_CONTACTS_SYNC, "ContactsSynchronizer.connect.onChildAdded.contact : " + contact);
                                        addContact(contact);
                                    } catch (ChatFieldNotFoundException cfnfe) {
                                        Log.w(DEBUG_CONTACTS_SYNC, "Error decoding contact on onChildAdded " + cfnfe.getMessage());
//                                notifySubscriberAdded(null, new ChatRuntimeException(cfnfe));
                                    } catch (Exception e) {
                                        notifySubscriberAdded(null, new ChatRuntimeException(e));
                                    }
                                }
                            }).start();

                            return null;
                        }
                    }.execute(dataSnapshot);
                }

                //for return receipt
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {

//                    new AsyncTask<DataSnapshot, Void, Void>() {
//                        @Override
//                        protected Void doInBackground(DataSnapshot... snapshots) {
//                            Log.v(DEBUG_CONTACTS_SYNC, "ContactsSynchronizer.connect.onChildChanged");

                    try {
                        IChatUser contact = decodeContactSnapShop(dataSnapshot);
                        updateLoggedUser(contact);
                        Log.d(DEBUG_CONTACTS_SYNC, "ContactsSynchronizer.connect.onChildChanged.contact : " + contact);
                        updateContact(contact);
                    } catch (ChatFieldNotFoundException cfnfe) {
                        Log.w(DEBUG_CONTACTS_SYNC, "Error decoding contact on onChildChanged " + cfnfe.getMessage());
//                                notifySubscriberChanged(null, new ChatRuntimeException(cfnfe));
                    } catch (Exception e) {
                        notifySubscriberChanged(null, new ChatRuntimeException(e));
                    }
//                            return null;
//                        }
//                    }.execute(dataSnapshot);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.v(DEBUG_CONTACTS_SYNC, "ContactsSynchronizer.connect.onChildRemoved");

                    try {
                        IChatUser contact = decodeContactSnapShop(dataSnapshot);

                        Log.d(DEBUG_CONTACTS_SYNC, "ContactsSynchronizer.connect.onChildRemoved.contact : " + contact);

                        contacts.remove(contact);

                        if (contactListeners != null) {
                            for (ContactListener contactListener : contactListeners) {
                                contactListener.onContactRemoved(contact, null);
                            }
                        }

                    } catch (ChatFieldNotFoundException cfnfe) {
                        Log.w(DEBUG_CONTACTS_SYNC, "Error decoding contact on onContactRemoved " + cfnfe.getMessage());
                    } catch (Exception e) {
                        if (contactListeners != null) {
                            for (ContactListener contactListener : contactListeners) {
                                contactListener.onContactRemoved(null, new ChatRuntimeException(e));
                            }
                        }
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
//                Log.d(TAG, "observeMessages.onChildMoved");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
//                Log.d(TAG, "observeMessages.onCancelled");

                }
            });

            Log.i(DEBUG_CONTACTS_SYNC, "connected for contacts ");

        } else {
            Log.i(DEBUG_CONTACTS_SYNC, "already connected to contacts ");
        }

        return contactsChildEventListener;
    }

    private void saveOrUpdateContactInMemory(IChatUser contact) {
        Log.d(DEBUG_CONTACTS_SYNC, "saveOrUpdateContactInMemory  for contact : " + contact);

        int index = contacts.indexOf(contact);

        if (index > -1) {
            contacts.set(index, contact);
            Log.v(DEBUG_CONTACTS_SYNC, "contact " + contact +
                    "updated into contacts at position " + index);

        } else {
            contacts.add(contact);
            Log.v(DEBUG_CONTACTS_SYNC, "contact " + contact + "is not found into contacts." +
                    " The contact was added at the end of the list");
        }
    }

//    private void saveOrUpdateContactOnStorage(IChatUser contact) {
//        Log.d(DEBUG_CONTACTS_SYNC, "saveOrUpdateContactOnStorage  for contact : " + contact);
//
//        IChatUser fetchedContact = Database.mUserDao.fetchUserById(contact.getId());
//        if (fetchedContact != null) {
//            // update user if exists
//            boolean isUpdated = Database.mUserDao.updateUser(contact);
//            Log.d(DEBUG_CONTACTS_SYNC, "ContactsSynchronizer.saveOrUpdateContactOnStorage:" +
//                    " contact " + contact + " updated: " + isUpdated);
//        } else {
//            // add the user if not exists
//            boolean isSaved = Database.mUserDao.addUser(contact);
//            Log.d(DEBUG_CONTACTS_SYNC, "ContactsSynchronizer.saveOrUpdateContactOnStorage:" +
//                    " contact " + contact + " saved: " + isSaved);
//        }
//    }

    public CopyOnWriteArrayList<IChatUser> getContacts() {
        return contacts;
    }

    public void setContacts(CopyOnWriteArrayList<IChatUser> contacts) {
        this.contacts = contacts;
    }

    public List<ContactListener> getContactListeners() {
        return contactListeners;
    }

    public void setContactListeners(List<ContactListener> contactListeners) {
        this.contactListeners = contactListeners;
    }

    public void upsertContactsListener(ContactListener contactListener) {
        Log.v(DEBUG_CONTACTS_SYNC, "  upsertContactsListener called");

        if (contactListeners.contains(contactListener)) {
            this.removeContactsListener(contactListener);
            this.addContactsListener(contactListener);
            Log.i(DEBUG_CONTACTS_SYNC, "  contactListener with hashCode: " + contactListener.hashCode() + " updated");

        } else {
            this.addContactsListener(contactListener);
            Log.i(DEBUG_CONTACTS_SYNC, "  contactListener with hashCode: " + contactListener.hashCode() + " added");
        }
    }

    public void addContactsListener(ContactListener contactListener) {
        Log.v(DEBUG_CONTACTS_SYNC, "  addContactsListener called");

        this.contactListeners.add(contactListener);

        Log.i(DEBUG_CONTACTS_SYNC, "  contactListener with hashCode: " + contactListener.hashCode() + " added");
    }

    public void removeContactsListener(ContactListener contactListener) {
        Log.v(DEBUG_CONTACTS_SYNC, "  removeContactsListener called");

        this.contactListeners.remove(contactListener);

        Log.i(DEBUG_CONTACTS_SYNC, "  contactListener with hashCode: " + contactListener.hashCode() + " removed");
    }

    public void removeAllContactsListeners() {
        this.contactListeners.clear();
        Log.i(DEBUG_CONTACTS_SYNC, "Removed all contactListeners");
    }

    public void addContact(IChatUser contact) {
        try {
            saveOrUpdateContactInMemory(contact);
//            saveOrUpdateContactOnStorage(contact);
            notifySubscriberAdded(contact, null);
        } catch (Exception e) {
            notifySubscriberAdded(null, new ChatRuntimeException(e));
        }
    }

    public void updateContact(IChatUser contact) {
        try {
            saveOrUpdateContactInMemory(contact);
//            saveOrUpdateContactOnStorage(contact);
            notifySubscriberChanged(contact, null);
        } catch (Exception e) {
            notifySubscriberChanged(null, new ChatRuntimeException(e));
        }
    }

    private void notifySubscriberAdded(IChatUser contact, ChatRuntimeException exception) {
        if (contactListeners != null) {
            for (ContactListener contactListener : contactListeners) {
                contactListener.onContactReceived(contact, exception);
            }
        }
    }

    private void notifySubscriberChanged(IChatUser contact, ChatRuntimeException exception) {
        if (contactListeners != null) {
            for (ContactListener contactListener : contactListeners) {
                contactListener.onContactChanged(contact, exception);
            }
        }
    }

    /**
     * It looks for the contact with {@code contactId}
     *
     * @param contactId the contact id to looking for
     * @return the contact if exists, null otherwise
     */
    public synchronized IChatUser findById(String contactId) {
//        for (IChatUser contact : contacts) {
//            if (contact.getId().equals(contactId)) {
//                return contact;
//            }
//        }
//        return null;



//        Iterator<IChatUser> contactsIterator = contacts.iterator();
//        if (contactsIterator.hasNext()) {
//            IChatUser contact = contactsIterator.next();
//            if (contact.getId().equals(contactId)) return contact;
//        }

        for (IChatUser contact : contacts) {
            if (contact.getId().equals(contactId)) {
                return contact;
            }
        }

        return null;
    }

    // update the serialized logged user
    private void updateLoggedUser(IChatUser contact) {
        IChatUser loggedUser = ChatManager.getInstance().getLoggedUser();

        if (contact.getId().equals(loggedUser.getId())) {
            ChatManager.getInstance().setLoggedUser(contact);
        }
    }

    public static IChatUser decodeContactSnapShop(DataSnapshot dataSnapshot) throws ChatFieldNotFoundException {
        Log.v(DEBUG_CONTACTS_SYNC, "decodeContactSnapShop called");

        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

        String contactId = dataSnapshot.getKey();

        String uid = (String) map.get("uid");
        if (uid == null) {
            throw new ChatFieldNotFoundException("Required uid field is null for contact id : " + contactId);
        }

        String firstName = (String) map.get("firstname");
        String lastName = (String) map.get("lastname");
        String imageUrl = (String) map.get("imageurl");
        String email = (String) map.get("email");


        Long timestamp = null;
        if (map.containsKey("timestamp")) {
            timestamp = (Long) map.get("timestamp");
        }

        IChatUser contact = new ChatUser();
        contact.setId(uid);
        contact.setFullName(firstName + " " + lastName);
        contact.setProfilePictureUrl(imageUrl);
        contact.setEmail(email);

        Log.v(DEBUG_CONTACTS_SYNC, "decodeContactSnapShop.contact : " + contact);

        return contact;
    }

    public void disconnect() {
        this.contactsNode.removeEventListener(this.contactsChildEventListener);
        this.removeAllContactsListeners();
    }
}