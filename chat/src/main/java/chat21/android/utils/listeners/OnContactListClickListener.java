package chat21.android.utils.listeners;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.google.firebase.crash.FirebaseCrash;

import chat21.android.R;

/**
 * Custom listener to open the contact list activity declared in
 * "chat_settings.xml (target_contact_list_activity_class)"
 * <p>
 * Created by frontiere21 on 03/10/16.
 */
public class OnContactListClickListener implements View.OnClickListener {
    private static final String TAG = OnContactListClickListener.class.getName();
    private Context context;

    public OnContactListClickListener(Context context) {
        Log.d(TAG, "OnContactListClickListener");
        this.context = context;
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick");

        try {
            // targetClass MUST NOT BE NULL
            Class<?> targetClass = Class.forName(context.getString(R.string.target_contact_list_activity_class));
            Intent intent = new Intent(context, targetClass);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            String errorMessage = "cannot retrieve the user list activity target class. \n" + e.getMessage();
            Log.e(TAG, errorMessage);
            FirebaseCrash.report(new Exception(errorMessage));
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}