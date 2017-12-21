package chat21.android.ui.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import chat21.android.R;
import chat21.android.core.ChatAuthentication;
import chat21.android.core.ChatManager;
import chat21.android.core.users.models.ChatUser;
import chat21.android.core.users.models.IChatUser;
import chat21.android.utils.StringUtils;

import static chat21.android.utils.DebugConstants.DEBUG_LOGIN;

/**
 * Created by stefanodp91 on 21/12/17.
 */

public class ChatLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText vEmail;
    private EditText vPassword;
    private Button vLogin;

    private String email, username, password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_login);

        ChatAuthentication.getInstance().setTenant(ChatManager.getInstance().getTenant());
        ChatAuthentication.getInstance().createAuthListener();
        Log.d(DEBUG_LOGIN, "ChatLoginActivity.onCreate: auth state listener created ");

        vLogin = (Button) findViewById(R.id.login);
        vEmail = (EditText) findViewById(R.id.email);
        vPassword = (EditText) findViewById(R.id.password);

        initPasswordIMEAction();
        vLogin.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        ChatAuthentication.getInstance().getFirebaseAuth()
                .addAuthStateListener(ChatAuthentication.getInstance().getAuthListener());

        Log.d(DEBUG_LOGIN, "ChatLoginActivity.onStart: auth state listener attached ");
    }

    @Override
    public void onStop() {
        ChatAuthentication.getInstance().removeAuthStateListener();
        Log.d(DEBUG_LOGIN, "ChatLoginActivity.onStart: auth state listener detached ");

        super.onStop();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.login) {
            performLogin();
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

                    performLogin();

                    handled = true;
                }
                return handled;
            }
        });
    }

    private void performLogin() {

        if (StringUtils.isValid(vEmail.getText().toString()) && StringUtils.isValid(vPassword.getText().toString())) {
            IChatUser loggedUser = new ChatUser();
            loggedUser.setEmail(vEmail.getText().toString());
            loggedUser.set(vEmail.getText().toString());


            // usename and password are valid
        } else {
            // email is not valid
            if (!StringUtils.isValid(vEmail.getText().toString())) {

            }

            // password is not valid
            if (!StringUtils.isValid(vPassword.getText().toString())) {

            }
        }

    }
}
