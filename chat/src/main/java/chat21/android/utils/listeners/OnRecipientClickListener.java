package chat21.android.utils.listeners;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.google.firebase.crash.FirebaseCrash;

import chat21.android.R;
import chat21.android.ui.ChatUI;
import chat21.android.utils.StringUtils;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by frontiere21 on 03/10/16.
 */
public class OnRecipientClickListener implements View.OnClickListener {
    private Context context;
    private String contactId;
    private Class callingClass;

    public OnRecipientClickListener(Context context, @NonNull String contactId) {
        this.context = context;
        this.contactId = contactId;
    }

    public void setCallingActivity(Class callingClass) {
        this.callingClass = callingClass;
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onRecipientProfilePictureClickListener");

        try {
            // targetClass MUST NOT BE NULL
            Class<?> targetClass = Class.forName(context.getString(R.string.target_contact_profile_activity_class));
            if (targetClass != null && StringUtils.isValid(contactId)) {
                Intent intent = new Intent(context, targetClass);
                intent.putExtra(ChatUI.INTENT_BUNDLE_CONTACT_ID, contactId);
                if (callingClass != null) {
                    intent.putExtra(ChatUI.INTENT_BUNDLE_CALLING_ACTIVITY, callingClass);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context
                context.startActivity(intent);
            } else {
                String errorMessage = "user profile activity target class is null or contactId is null";
                Log.e(TAG, errorMessage);
                FirebaseCrash.report(new Exception(errorMessage));
            }
        } catch (ClassNotFoundException e) {
            String errorMessage = "cannot retrieve the user profile activity target class. \n" + e.getMessage();
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

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }
}