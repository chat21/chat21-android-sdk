package org.chat21.android.ui.login.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.chat21.android.R;
import org.chat21.android.core.ChatManager;
import org.chat21.android.utils.StringUtils;

import static org.chat21.android.ui.ChatUI.BUNDLE_SIGNED_UP_USER_EMAIL;
import static org.chat21.android.ui.ChatUI.BUNDLE_SIGNED_UP_USER_PASSWORD;

/**
 * Created by stefanodp91 on 04/01/18.
 */

public class ChatSignUpActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private FirebaseAuth mAuth;

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private TextInputEditText inputEmail;
    private TextInputEditText inputFirstName;
    private TextInputEditText inputLastName;
    private TextInputEditText inputPassword;
    private TextInputEditText inputRepeatPassword;
    private Button btnSignup;

    private interface OnUserCreatedOnFirebaseCallback {
        void onUserCreatedSuccess(String userUID);

        void onUserCreatedError(Exception e);
    }

    private interface OnUserCreatedOnContactsCallback {
        void onUserCreatedSuccess();

        void onUserCreatedError(Exception e);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_signup);

        // get Firebase auth instance
        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        progressBar = findViewById(R.id.progress_bar);

        inputEmail = findViewById(R.id.email);
        inputFirstName = findViewById(R.id.first_name);
        inputLastName = findViewById(R.id.last_name);
        inputPassword = findViewById(R.id.password);
        inputRepeatPassword = findViewById(R.id.repeat_password);

        btnSignup = findViewById(R.id.signup);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // validate email
                final String email = inputEmail.getText().toString().trim();
                boolean isValidEmail = validateEmail(email);
                if (!isValidEmail) {
                    return;
                }

                // validate first name
                final String firstName = inputFirstName.getText().toString().trim();
                boolean isValidFirstName = validateFirstName(firstName);
                if (!isValidFirstName) {
                    return;
                }

                // validate last name
                final String lastName = inputLastName.getText().toString().trim();
                boolean isValidLastName = validateLastName(lastName);
                if (!isValidLastName) {
                    return;
                }

                // validate password and repeated password
                final String password = inputPassword.getText().toString().trim();
                String repeatedPassword = inputRepeatPassword.getText().toString().trim();
                boolean areValidPasswords = validatePassword(password, repeatedPassword);
                if (!areValidPasswords) {
                    return;
                }

                // perform the user creation
                progressBar.setVisibility(View.VISIBLE);
                if (isValidEmail && areValidPasswords) {
                    createUserOnFirebaseAuthentication(email, password, new OnUserCreatedOnFirebaseCallback() {

                        @Override
                        public void onUserCreatedSuccess(final String userUID) {

                            final Map<String, Object> userMap = new HashMap<>();
                            userMap.put("email", email);
                            userMap.put("firstname", firstName);
                            userMap.put("imageurl", "");
                            userMap.put("lastname", lastName);
                            userMap.put("timestamp", new Date().getTime());
                            userMap.put("uid", userUID);

                            createUserOnContacts(userUID, userMap, new OnUserCreatedOnContactsCallback() {

                                @Override
                                public void onUserCreatedSuccess() {
                                    Intent intent = getIntent();
                                    intent.putExtra(BUNDLE_SIGNED_UP_USER_EMAIL, email);
                                    intent.putExtra(BUNDLE_SIGNED_UP_USER_PASSWORD, password);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }

                                @Override
                                public void onUserCreatedError(Exception e) {
                                    // TODO: 04/01/18
                                    Toast.makeText(ChatSignUpActivity.this, "Saving user on contacts failed." + e,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onUserCreatedError(Exception e) {
                            // TODO: 04/01/18  string
                            Toast.makeText(ChatSignUpActivity.this, "Authentication failed." + e,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    // email validation
    private boolean validateEmail(String email) {
        if (!StringUtils.isValid(email) || (StringUtils.isValid(email) && !StringUtils.validateEmail(email))) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_not_valid_email_label),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // first name validation
    private boolean validateFirstName(String firstName) {
        if (!StringUtils.isValid(firstName)) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_not_valid_first_name_label),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // last name validation
    private boolean validateLastName(String lastName) {
        if (!StringUtils.isValid(lastName)) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_not_valid_last_name_label),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // password validation
    private boolean validatePassword(String password, String repeatedPassword) {
        if (!StringUtils.isValid(password)) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_not_valid_password_label),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_not_valid_password_length_label, MIN_PASSWORD_LENGTH),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!StringUtils.isValid(repeatedPassword)) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_not_valid_repeated_password_label),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (repeatedPassword.length() < MIN_PASSWORD_LENGTH) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_not_valid_repeated_password_length_label, MIN_PASSWORD_LENGTH),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(repeatedPassword)) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_passwords_mismatch_label),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    private void createUserOnFirebaseAuthentication(final String email,
                                                    final String password,
                                                    final OnUserCreatedOnFirebaseCallback onUserCreatedOnFirebaseCallback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(ChatSignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Toast.makeText(ChatSignUpActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            onUserCreatedOnFirebaseCallback.onUserCreatedError(task.getException());
                        } else {
                            // user created with success
                            FirebaseUser firebaseUser = mAuth.getCurrentUser(); // retrieve the created user
                            onUserCreatedOnFirebaseCallback.onUserCreatedSuccess(firebaseUser.getUid());
                        }
                    }
                });
    }

    private void createUserOnContacts(String key, Map<String, Object> user,
                                      final OnUserCreatedOnContactsCallback onUserCreatedOnContactsCallback) {
        DatabaseReference contactsNode;
        if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
            contactsNode = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl)
                    .child("/apps/" + ChatManager.Configuration.appId + "/contacts");
        } else {
            contactsNode = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("/apps/" + ChatManager.Configuration.appId + "/contacts");
        }

        // save the user on contacts node
        contactsNode.child(key)
                .setValue(user, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            // all gone right
                            onUserCreatedOnContactsCallback.onUserCreatedSuccess();
                        } else {
                            // errors
                            onUserCreatedOnContactsCallback.onUserCreatedError(databaseError.toException());
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}