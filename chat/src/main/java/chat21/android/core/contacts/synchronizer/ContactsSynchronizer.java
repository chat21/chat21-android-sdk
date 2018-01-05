package chat21.android.core.contacts.synchronizer;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import chat21.android.core.contacts.listeners.ContactListener;
import chat21.android.core.exception.ChatFieldNotFoundException;
import chat21.android.core.exception.ChatRuntimeException;
import chat21.android.core.users.models.ChatUser;
import chat21.android.core.users.models.IChatUser;

/**
 * Created by andrealeo on 04/01/18.
 */

public class ContactsSynchronizer {

    private static final String TAG = ContactsSynchronizer.class.getName();

    private List<IChatUser> contacts = new ArrayList<>(); // contacts in memory

    DatabaseReference contactsNode;

    ChildEventListener contactsChildEventListener;

    List<ContactListener> contactListeners;

    public ContactsSynchronizer(String firebaseUrl, String appId) {

        contactListeners = new ArrayList<>();

        this.contactsNode = FirebaseDatabase.getInstance().getReferenceFromUrl(firebaseUrl).child("/apps/" + appId + "/contacts/");
        this.contactsNode.keepSynced(true);

        Log.d(TAG, "contactsNode : " + contactsNode.toString());
    }

    public ChildEventListener connect() {
        Log.d(TAG, "connecting  for contacts ");

        if (contactsChildEventListener == null) {

            Log.d(TAG, "creating a new contactsChildEventListener");

            contactsChildEventListener = contactsNode.orderByChild("firstname").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                    Log.v(TAG, "ContactsSynchronizer.connect.onChildAdded");

                    try {
                        IChatUser contact = decodeContactSnapShop(dataSnapshot);
                        Log.d(TAG, "ContactsSynchronizer.connect.onChildAdded.contact : " + contact);

                        saveOrUpdateContactInMemory(contact);

                        if (contactListeners != null) {
                            for (ContactListener contactListener : contactListeners) {
                                contactListener.onContactReceived(contact, null);
                            }
                        }

                    } catch (ChatFieldNotFoundException cfnfe) {
                        Log.w(TAG, "Error decoding contact on onChildAdded " + cfnfe.getMessage());
                    } catch (Exception e) {
                        if (contactListeners != null) {
                            for (ContactListener contactListener : contactListeners) {
                                contactListener.onContactReceived(null, new ChatRuntimeException(e));
                            }
                        }
                    }
                }

                //for return recepit
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                    Log.v(TAG, "ContactsSynchronizer.connect.onChildChanged");

                    try {
                        IChatUser contact = decodeContactSnapShop(dataSnapshot);

                        Log.d(TAG, "ContactsSynchronizer.connect.onChildChanged.contact : " + contact);

                        saveOrUpdateContactInMemory(contact);

                        if (contactListeners != null) {
                            for (ContactListener contactListener : contactListeners) {
                                contactListener.onContactChanged(contact, null);
                            }
                        }

                    } catch (ChatFieldNotFoundException cfnfe) {
                        Log.w(TAG, "Error decoding contact on onChildChanged " + cfnfe.getMessage());
                    } catch (Exception e) {
                        if (contactListeners != null) {
                            for (ContactListener contactListener : contactListeners) {
                                contactListener.onContactChanged(null, new ChatRuntimeException(e));
                            }
                        }
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.v(TAG, "ContactsSynchronizer.connect.onChildRemoved");

                    try {
                        IChatUser contact = decodeContactSnapShop(dataSnapshot);

                        Log.d(TAG, "ContactsSynchronizer.connect.onChildRemoved.contact : " + contact);

                        contacts.remove(contact);

                        if (contactListeners != null) {
                            for (ContactListener contactListener : contactListeners) {
                                contactListener.onContactRemoved(contact, null);
                            }
                        }

                    } catch (ChatFieldNotFoundException cfnfe) {
                        Log.w(TAG, "Error decoding contact on onContactRemoved " + cfnfe.getMessage());
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

            Log.i(TAG, "connected for contacts ");

        } else {
            Log.i(TAG, "already connected to contacts ");
        }

        return contactsChildEventListener;
    }

    private void saveOrUpdateContactInMemory(IChatUser contact) {
        Log.d(TAG, "saveOrUpdateContactInMemory  for contact : " + contact);

        int index = contacts.indexOf(contact);

        if (index > -1) {
            contacts.set(index, contact);
            Log.v(TAG, "contact " + contact + "updated into contacts at position " + index);

        } else {
            contacts.add(contact);
            Log.v(TAG, "contact " + contact + "is not found into contacts. The contact was added at the end of the list");
        }
    }

    public List<IChatUser> getContacts() {
        return contacts;
    }

    public void setContacts(List<IChatUser> contacts) {
        this.contacts = contacts;
    }

    public List<ContactListener> getContactListeners() {
        return contactListeners;
    }

    public void setContactListeners(List<ContactListener> contactListeners) {
        this.contactListeners = contactListeners;
    }

    public void upsertContactsListener(ContactListener contactListener) {
        Log.v(TAG, "  upsertContactsListener called");

        if (contactListeners.contains(contactListener)) {
            this.removeContactsListener(contactListener);
            this.addContactsListener(contactListener);
            Log.i(TAG, "  contactListener with hashCode: " + contactListener.hashCode() + " updated");

        } else {
            this.addContactsListener(contactListener);
            Log.i(TAG, "  contactListener with hashCode: " + contactListener.hashCode() + " added");
        }
    }

    public void addContactsListener(ContactListener contactListener) {
        Log.v(TAG, "  addContactsListener called");

        this.contactListeners.add(contactListener);

        Log.i(TAG, "  contactListener with hashCode: " + contactListener.hashCode() + " added");
    }

    public void removeContactsListener(ContactListener contactListener) {
        Log.v(TAG, "  removeContactsListener called");

        this.contactListeners.remove(contactListener);

        Log.i(TAG, "  contactListener with hashCode: " + contactListener.hashCode() + " removed");
    }

    public void addContact(IChatUser contact) {
        this.contacts.add(contact);
    }

    public static IChatUser decodeContactSnapShop(DataSnapshot dataSnapshot) throws ChatFieldNotFoundException {
        Log.v(TAG, "decodeContactSnapShop called");

        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

        String contactId = dataSnapshot.getKey();

        String uid = (String) map.get("uid");
        if (uid == null) {
            throw new ChatFieldNotFoundException("Required uid field is null for contact id : " + contactId);
        }

        String firstName = (String) map.get("firstname");
        String lastName = (String) map.get("lastname");
        String imageUrl = (String) map.get("imageurl");


        Long timestamp = null;
        if (map.containsKey("timestamp")) {
            timestamp = (Long) map.get("timestamp");
        }

        IChatUser contact = new ChatUser();
        contact.setId(uid);
        contact.setFullName(firstName + " " + lastName);
        contact.setProfilePictureUrl(imageUrl);

        Log.v(TAG, "decodeContactSnapShop.contact : " + contact);

        return contact;
    }

//    public List<IChatUser> search(String keyWord) {
//        List<IChatUser> filteredList = new ArrayList<>();
//
//        if (contacts == null || !StringUtils.isValid(keyWord))
//            return filteredList;
//
//        for (IChatUser row : contacts) {
//            // search on the user fullname
//            if (row.getFullName().toLowerCase().contains(keyWord.toLowerCase())) {
//                filteredList.add(row);
//            }
//        }
//
//        return filteredList;
//    }
}