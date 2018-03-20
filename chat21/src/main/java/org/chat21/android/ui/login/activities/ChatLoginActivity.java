package org.chat21.android.ui.login.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.chat21.android.R;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.authentication.task.RefreshFirebaseInstanceIdTask;
import org.chat21.android.core.exception.ChatFieldNotFoundException;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.contacts.activites.ContactListActivity;
import org.chat21.android.ui.conversations.listeners.OnNewConversationClickListener;
import org.chat21.android.utils.StringUtils;

import java.util.Map;

import static org.chat21.android.ui.ChatUI.BUNDLE_SIGNED_UP_USER_EMAIL;
import static org.chat21.android.ui.ChatUI.BUNDLE_SIGNED_UP_USER_PASSWORD;
import static org.chat21.android.ui.ChatUI.REQUEST_CODE_SIGNUP_ACTIVITY;
import static org.chat21.android.utils.DebugConstants.DEBUG_LOGIN;

/**
 * Created by stefanodp91 on 21/12/17.
 */

public class ChatLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ChatLoginActivity";

    private Toolbar toolbar;
    private EditText vEmail;
    private EditText vPassword;
    private Button vLogin;
    private Button vSignUp;
    private FirebaseAuth mAuth;

//    private String email, username, password;

    private interface OnUserLookUpComplete {
        void onUserRetrievedSuccess(IChatUser loggedUser);

        void onUserRetrievedError(Exception e);
    }

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_login);

        mAuth = FirebaseAuth.getInstance();

//        ChatAuthentication.getInstance().setTenant(ChatManager.getInstance().getTenant());
//        ChatAuthentication.getInstance().createAuthListener();

//        Log.d(DEBUG_LOGIN, "ChatLoginActivity.onCreate: auth state listener created ");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        vLogin = (Button) findViewById(R.id.login);
        vLogin.setOnClickListener(this);

        vSignUp = (Button) findViewById(R.id.signup);
        vSignUp.setOnClickListener(this);

        vEmail = (EditText) findViewById(R.id.email);
        vPassword = (EditText) findViewById(R.id.password);
        initPasswordIMEAction();
    }

    @Override
    public void onStart() {
        super.onStart();


        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
//        ChatAuthentication.getInstance().getFirebaseAuth()
//                .addAuthStateListener(ChatAuthentication.getInstance().getAuthListener());
//
//        Log.d(DEBUG_LOGIN, "ChatLoginActivity.onStart: auth state listener attached ");
    }

    @Override
    public void onStop() {
//        ChatAuthentication.getInstance().removeAuthStateListener();
//        Log.d(DEBUG_LOGIN, "ChatLoginActivity.onStart: auth state listener detached ");

        super.onStop();

        hideProgressDialog();

    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.login) {
            signIn(vEmail.getText().toString(), vPassword.getText().toString());
//            performLogin();
        } else if (viewId == R.id.signup) {
            startSignUpActivity();
        }
    }

    private void initPasswordIMEAction() {
        Log.d(DEBUG_LOGIN, "initPasswordIMEAction");

        /**
         * on ime click
         * source:
         * http://developer.android.com/training/keyboard-input/style.html#Action
         */
        vPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.d(DEBUG_LOGIN, "ChatLoginActivity.initPasswordIMEAction");

//                    performLogin();
                    signIn(vEmail.getText().toString(), vPassword.getText().toString());

                    handled = true;
                }
                return handled;
            }
        });
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);

        vEmail.setText(email);
        vPassword.setText(password);

        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            lookUpContactById(user.getUid(), new OnUserLookUpComplete() {
                                @Override
                                public void onUserRetrievedSuccess(IChatUser loggedUser) {
                                    Log.d(TAG, "ChatLoginActivity.signInWithEmail.onUserRetrievedSuccess: loggedUser == " + loggedUser.toString());

                                    ChatManager.Configuration mChatConfiguration =
                                            new ChatManager.Configuration.Builder(ChatManager.Configuration.appId)
//                                                    .firebaseUrl(ChatManager.Configuration.firebaseUrl)
//                                                    .storageBucket(ChatManager.Configuration.storageBucket)
                                                    .build();

//                                    IChatUser iChatUser = new ChatUser();
//                                    iChatUser.setId(user.getUid());
//                                    iChatUser.setEmail(user.getEmail());

                                    ChatManager.start(ChatLoginActivity.this, mChatConfiguration, loggedUser);
                                    Log.i(TAG, "chat has been initialized with success");

//                                    // get device token
                                    new RefreshFirebaseInstanceIdTask().execute();

                                    ChatUI.getInstance().setContext(ChatLoginActivity.this);
                                    Log.i(TAG, "ChatUI has been initialized with success");

                                    ChatUI.getInstance().enableGroups(true);

                                    // set on new conversation click listener
                                    // final IChatUser support = new ChatUser("support", "Chat21 Support");
                                    final IChatUser support = null;
                                    ChatUI.getInstance().setOnNewConversationClickListener(new OnNewConversationClickListener() {
                                        @Override
                                        public void onNewConversationClicked() {
                                            if (support != null) {
                                                ChatUI.getInstance().openConversationMessagesActivity(support);
                                            } else {
                                                Intent intent = new Intent(getApplicationContext(),
                                                        ContactListActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context

                                                startActivity(intent);
                                            }
                                        }
                                    });

//                                    // on attach button click listener
//                                    ChatUI.getInstance().setOnAttachClickListener(new OnAttachClickListener() {
//                                        @Override
//                                        public void onAttachClicked(Object object) {
//                                            Toast.makeText(getApplicationContext(),
//                                                    "onAttachClickListener", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//
//                                    // on create group button click listener
//                                    ChatUI.getInstance().setOnCreateGroupClickListener(new OnCreateGroupClickListener() {
//                                        @Override
//                                        public void onCreateGroupClicked() {
//                                            Toast.makeText(getApplicationContext(),
//                                                    "setOnCreateGroupClickListener", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
                                    Log.i(TAG, "ChatUI has been initialized with success");

                                    setResult(Activity.RESULT_OK);
                                    finish();
                                }

                                @Override
                                public void onUserRetrievedError(Exception e) {
                                    Log.d(TAG, "ChatLoginActivity.signInWithEmail.onUserRetrievedError: " + e.toString());
                                }
                            });

                            // enable persistence must be made before any other usage of FirebaseDatabase instance.
                            try {
                                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                            } catch (DatabaseException databaseException) {
                                Log.w(TAG, databaseException.toString());
                            } catch (Exception e) {
                                Log.w(TAG, e.toString());
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());

                            Toast.makeText(ChatLoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

//                            setResult(Activity.RESULT_CANCELED);
//                            finish();
//                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
//                            setResult(Activity.RESULT_CANCELED);
//                            finish();
//                            mStatusTextView.setText(R.string.auth_failed);
                        }
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private void startSignUpActivity() {
        Intent intent = new Intent(this, ChatSignUpActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SIGNUP_ACTIVITY);
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = vEmail.getText().toString();
        if (!StringUtils.isValid(email)) {
            vEmail.setError("Required.");
            valid = false;
        } else if (StringUtils.isValid(email) && !StringUtils.validateEmail(email)) {
            vEmail.setError("Not valid email.");
            valid = false;
        } else {
            vEmail.setError(null);
        }
        String password = vPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            vPassword.setError("Required.");
            valid = false;
        } else {
            vPassword.setError(null);
        }

        return valid;
    }

//    private void performLogin() {
//
//
//        if (StringUtils.isValid(vEmail.getText().toString()) && StringUtils.isValid(vPassword.getText().toString())) {
//
//            final IChatUser loggedUser = new ChatUser();
//            loggedUser.setEmail(vEmail.getText().toString());
////            loggedUser.setPassword(vPassword.getText().toString());
//
//            String email = vEmail.getText().toString();
//            String password = vPassword.getText().toString();
//
//            ChatAuthentication.getInstance().signInWithEmailAndPassword(this,email, password, new ChatAuthentication.OnChatLoginCallback(){
//                    @Override
//                    public void onChatLoginSuccess() {
//
//                        ChatManager.getInstance().setLoggedUser(loggedUser);
//
//                        setResult(Activity.RESULT_OK);
//                        finish();
//                        //TODO DELETE ACTIVITY STACK
//                    }
//
//                    @Override
//                    public void onChatLoginError(Exception e) {
//                        // fix Issue #24
//                        Log.e(DEBUG_LOGIN, "signInWithUid.onChatLoginError. " + e.getMessage());
//
//                        setResult(Activity.RESULT_CANCELED);
//                        finish();
//                    }
//            });
//
//
//        } else {
//            // email is not valid
//            if (!StringUtils.isValid(vEmail.getText().toString())) {
//                //TODO
//            }
//
//            // password is not valid
//            if (!StringUtils.isValid(vPassword.getText().toString())) {
//
//            }
//        }
//
//
//    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
//            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGNUP_ACTIVITY) {
            if (resultCode == RESULT_OK) {

                // set username
                String email = data.getStringExtra(BUNDLE_SIGNED_UP_USER_EMAIL);
//                vEmail.setText(email);

                // set password
                String password = data.getStringExtra(BUNDLE_SIGNED_UP_USER_PASSWORD);
//                vPassword.setText(password);

                signIn(email, password);
            }
        }
    }

    private void lookUpContactById(String userId, final OnUserLookUpComplete onUserLookUpComplete) {


        DatabaseReference contactsNode;
        if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
            contactsNode = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl)
                    .child("/apps/" + ChatManager.Configuration.appId + "/contacts/" + userId);
        } else {
            contactsNode = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("/apps/" + ChatManager.Configuration.appId + "/contacts/" + userId);
        }

        contactsNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(DEBUG_LOGIN, "ChatLoginActivity.lookUpContactById: dataSnapshot == " + dataSnapshot.toString());

                if (dataSnapshot.getValue() != null) {
                    try {
                        IChatUser loggedUser = decodeContactSnapShop(dataSnapshot);
                        Log.d(DEBUG_LOGIN, "ChatLoginActivity.lookUpContactById.onDataChange: loggedUser == " + loggedUser.toString());
                        onUserLookUpComplete.onUserRetrievedSuccess(loggedUser);
                    } catch (ChatFieldNotFoundException e) {
                        Log.e(DEBUG_LOGIN, "ChatLoginActivity.lookUpContactById.onDataChange: " + e.toString());
                        onUserLookUpComplete.onUserRetrievedError(e);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(DEBUG_LOGIN, "ChatLoginActivity.lookUpContactById: " + databaseError.toString());
                onUserLookUpComplete.onUserRetrievedError(databaseError.toException());
            }
        });
    }

    private static IChatUser decodeContactSnapShop(DataSnapshot dataSnapshot) throws ChatFieldNotFoundException {
        Log.v(TAG, "decodeContactSnapShop called");

        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

//        String contactId = dataSnapshot.getKey();

        String uid = (String) map.get("uid");
        if (uid == null) {
            throw new ChatFieldNotFoundException("Required uid field is null for contact id : " + uid);
        }

//        String timestamp = (String) map.get("timestamp");

        String lastname = (String) map.get("lastname");
        String firstname = (String) map.get("firstname");
        String imageurl = (String) map.get("imageurl");
        String email = (String) map.get("email");


        IChatUser contact = new ChatUser();
        contact.setId(uid);
        contact.setEmail(email);
        contact.setFullName(firstname + " " + lastname);
        contact.setProfilePictureUrl(imageurl);

        Log.v(TAG, "decodeContactSnapShop.contact : " + contact);

        return contact;
    }
}