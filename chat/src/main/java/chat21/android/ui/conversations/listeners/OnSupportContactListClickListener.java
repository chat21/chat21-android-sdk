//package chat21.android.ui.conversations.listeners;
//
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//import android.view.View;
//
//import chat21.android.core.ChatManager;
//import chat21.android.core.conversations.models.Conversation;
//import chat21.android.core.messages.models.Message;
//import chat21.android.core.users.models.IChatUser;
//import chat21.android.ui.ChatUI;
//import chat21.android.ui.contacts.activites.ContactListActivity;
//import chat21.android.ui.messages.activities.MessageListActivity;
//
//import static chat21.android.ui.ChatUI.INTENT_BUNDLE_CONVERSATION;
//
///**
// * Created by frontiere21 on 08/11/16.
// */
//public class OnSupportContactListClickListener implements View.OnClickListener {
//    private static final String TAG = OnContactListClickListener.class.getName();
//
//    private Context context;
//    private IChatUser support;
//    private Class<?> contactListActivityClass = ContactListActivity.class;
//
//    public OnSupportContactListClickListener(Context context, IChatUser support) {
//        this.context = context;
//        this.support = support;
//    }
//
//    public void setContactListActivityClass(Class<?> contactListActivityClass) {
//        this.contactListActivityClass = contactListActivityClass;
//    }
//
//    @Override
//    public void onClick(View view) {
//        Log.d(TAG, "OnContactListClickListener");
//
//        // se l'utente correntemente loggato è il supporto, mostra la lista di contatti
//        // altrimenti avvia la conversazione con il supporto
//
//        if (ChatManager.getInstance().getLoggedUser().getId().compareTo(support.getId()) == 0) {
//            // targetClass MUST NOT BE NULL
//            Intent intent = new Intent(context, contactListActivityClass);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context
//            context.startActivity(intent);
//        } else {
//            Intent intent = new Intent(context, MessageListActivity.class);
//            intent.putExtra(INTENT_BUNDLE_CONVERSATION, createNewConversation());
//            intent.putExtra(ChatUI.INTENT_BUNDLE_IS_FROM_NOTIFICATION, false);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context
//            context.startActivity(intent);
//        }
//    }
//
//    // create a new conversation with the support account
//    private Conversation createNewConversation() {
//        Conversation conversation = new Conversation();
//        conversation.setSender(ChatManager.getInstance().getLoggedUser().getId());
//        conversation.setSender_fullname(ChatManager.getInstance().getLoggedUser().getFullName());
//        conversation.setConvers_with(support.getId());
//        conversation.setConvers_with_fullname(support.getFullName());
//        conversation.setRecipient(support.getId());
//        conversation.setRecipientFullName(support.getFullName());
//        conversation.setChannelType(Message.DIRECT_CHANNEL_TYPE);
//        conversation.setConversationId(support.getId());
//        return conversation;
//    }
//}