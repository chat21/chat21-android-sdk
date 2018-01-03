//package chat21.android.ui.conversations.listeners;
//
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//import android.view.View;
//
//import chat21.android.ui.contacts.activites.ContactListActivity;
//
///**
// * Created by stefanodp91 on 03/01/17.
// */
//public class OnContactListClickListener implements View.OnClickListener {
//    private static final String TAG = OnContactListClickListener.class.getName();
//
//    private Context context;
//    private Class<?> contactListActivityClass = ContactListActivity.class;
//
//    public OnContactListClickListener(Context context) {
//        Log.d(TAG, "OnContactListClickListener");
//        this.context = context;
//    }
//
//    public void setContactListActivityClass(Class<?> contactListActivityClass) {
//        this.contactListActivityClass = contactListActivityClass;
//    }
//
//    @Override
//    public void onClick(View view) {
//        Log.d(TAG, "onClick");
//
//        // targetClass MUST NOT BE NULL
//        Intent intent = new Intent(context, contactListActivityClass);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context
//        context.startActivity(intent);
//    }
//}