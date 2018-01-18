package chat21.android.notifications;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Map;

import chat21.android.core.ChatManager;
import chat21.android.core.users.models.ChatUser;
import chat21.android.core.users.models.IChatUser;
import chat21.android.utils.TimeUtils;

import static chat21.android.utils.DebugConstants.DEBUG_NOTIFICATION;

/**
 * Created by andrea on 28/03/17.
 */

//https://github.com/firebase/quickstart-android/blob/master/messaging/app/src/main/java/com/google/firebase/quickstart/fcm/MyFirebaseMessagingService.java
public class ChatFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(DEBUG_NOTIFICATION, "ChatFirebaseMessagingService.onMessageReceived from: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(DEBUG_NOTIFICATION, "ChatFirebaseMessagingService.onMessageReceived: payload == " + remoteMessage.getData());

            String body = remoteMessage.getData().get("text");
            Log.d(DEBUG_NOTIFICATION, "ChatFirebaseMessagingService.onMessageReceived: text == " + body);

            // resolve Issue #38
            // retrieve timestamp
            String timestamp = remoteMessage.getData().get("timestamp");
            Log.d(DEBUG_NOTIFICATION, "ChatFirebaseMessagingService.onMessageReceived: timestamp == " + timestamp);
            String formattedTimestamp = TimeUtils.timestampToHour(Long.parseLong(timestamp));
            Log.d(DEBUG_NOTIFICATION, "ChatFirebaseMessagingService.onMessageReceived: formattedTimestamp == " + formattedTimestamp);

            // resolve Issue #22
//            TODO implement for group
//            if (StringUtils.isValid(getGroupId(remoteMessage.getData()))) {
//                Log.i(TAG_NOTIFICATION, "ChatFirebaseMessagingService.onMessageReceived: " +
//                        "is group conversation with groupId: " + getGroupId(remoteMessage.getData()));
//
//                // group notification
//                String title = getGroupName(remoteMessage.getData());
//                String senderFullName = StringUtils.isValid(remoteMessage.getData().get("sender_fullname")) ?
//                        remoteMessage.getData().get("sender_fullname")
//                        : remoteMessage.getData().get("sender");
//                body = senderFullName + ": " + body;
//
//                sendGroupNotification(title, body, getGroupId(remoteMessage.getData()), formattedTimestamp);
//            } else {
//            Log.d(DEBUG_NOTIFICATION, "ChatFirebaseMessagingService.onMessageReceived: " +
//                    "is one to one conversation.");

            // one to one notification
            String title = remoteMessage.getData().get("sender_fullname");
            Log.d(DEBUG_NOTIFICATION, "ChatFirebaseMessagingService.onMessageReceived: title == " + title);

            sendNotification(title, body, remoteMessage.getData(), formattedTimestamp);
//            }
        } else {
            Log.d(DEBUG_NOTIFICATION, "ChatFirebaseMessagingService.onMessageReceived: " +
                    "remoteMessage.getData().size() < 0");
        }
    }

    // TODO: 19/06/17 raggruppamento notifiche come gmail
//    private void showGroupSummaryNotification(Conversation conversation) {
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//
//        String tenant = getString(R.string.tenant);
//        String conversationId = ConversationUtils.getConversationId(tenant, conversation.getSender(), conversation.getRecipient());
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        builder
//                .setContentTitle("Collpased Content Title")
//                .setContentText("Collpsed Content Text")
//                .setStyle(new NotificationCompat.InboxStyle()
//                        .addLine("Line 1")
//                        .addLine("Line 2")
//                        .setSummaryText(getString(R.string.app_name))
//                        .setBigContentTitle(conversation.getSender_fullname()))
//                .setNumber(2)
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification))
//                .setSmallIcon(R.drawable.ic_notification_small)
//                .setCategory(Notification.CATEGORY_EVENT)
//                .setGroupSummary(true)
//                .setGroup(conversationId);
//        Notification notification = builder.build();
//        notificationManager.notify(NOTIFICATION_ID, notification);
//    }
//
//    @Override
//    public void handleIntent(Intent intent) {
//        Intent launchIntent = new Intent(this, MessageListActivity.class); // TODO: 19/06/17
//        launchIntent.setAction(Intent.ACTION_MAIN);
//        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, launchIntent,
//                PendingIntent.FLAG_ONE_SHOT);
//        Bitmap rawBitmap = BitmapFactory.decodeResource(getResources(),
//                R.mipmap.ic_launcher);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_notification)
//                .setLargeIcon(rawBitmap)
//                .setContentTitle(intent.getStringExtra("gcm.notification.title"))
//                .setContentText(intent.getStringExtra("gcm.notification.body"))
//                .setAutoCancel(true)
//                .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager)     getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
//    }

    private void sendNotification(String title, String message, Map<String, String> data, String timestamp) {
        Log.d(DEBUG_NOTIFICATION, "ChatFirebaseMessagingService.sendNotification");

        int notificationId = (int) new Date().getTime();

        IChatUser sender = new ChatUser(data.get("sender"), data.get("sender_fullname"));
        Log.d(DEBUG_NOTIFICATION, "ChatFirebaseMessagingService.sendNotification: sender == " + sender.toString());


//        Intent resultIntent = new Intent(this, MessageListActivity.class);
//        resultIntent.putExtra(ChatUI.INTENT_BUNDLE_RECIPIENT, sender);
//        resultIntent.putExtra(ChatUI.INTENT_BUNDLE_IS_FROM_NOTIFICATION, true);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addParentStack(ConversationListActivity.class);
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


//        showGroupSummaryNotification(conversation);

        String conversationId = sender.getId();

//        // prevent start the conversation with 'conversationId'
//        // if it is the current active conversation
//        NotificationCompat.Builder notificationBuilder;
//        if (StringUtils.isValid(ChatManager.getInstance().activeConversation)) {
//            if (!ChatManager.getInstance().activeConversation.equals(conversationId)) {
//                // not the active conversation
//                Log.d(DEBUG_NOTIFICATION, "ChatFirebaseMessagingService.sendNotification: activeConversation == " + conversationId);
//                notificationBuilder = NotificationUtils.createNotification(this, conversationId, title, message, timestamp);
//                notificationBuilder.setContentIntent(resultPendingIntent);
//                Log.d(DEBUG_NOTIFICATION, "ChatFirebaseMessagingService.sendNotification: notification created");
//            } else {
//                // active conversation
//                notificationBuilder = NotificationUtils.createNotification(this, conversationId, title, message, timestamp);
//                // heads up notification
////                notificationBuilder = NotificationUtils.createHeadsUpNotification(this, conversationId, title, message, timestamp);
////                PendingIntent dismissIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, 0);
////                notificationBuilder.addAction(R.drawable.ic_clear_all_white_24dp, "Dismiss", dismissIntent);
//                Log.d(DEBUG_NOTIFICATION, "ChatFirebaseMessagingService.sendNotification: heads up notification created");
//            }
//        } else {
//            // activeConversation never set
//            notificationBuilder = NotificationUtils.createNotification(this, conversationId, title, message, timestamp);
//            notificationBuilder.setContentIntent(resultPendingIntent);
//            Log.d(DEBUG_NOTIFICATION, "ChatFirebaseMessagingService.sendNotification: notification created");
//        }

        Intent notificationIntent = NotificationUtils.createNotificationIntent(this, sender);

        PendingIntent resultPendingIntent = NotificationUtils.createPendingIntent(this, notificationIntent);

        // prevent start the conversation with 'conversationId'
        // if it is the current active conversation
        NotificationCompat.Builder notificationBuilder = NotificationUtils
                .createNotification(this, conversationId, title, message, timestamp);
        notificationBuilder.setContentIntent(resultPendingIntent);

        if (ChatManager.getInstance().getActiveConversation(conversationId)) {
            // the active conversation
            return;
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d(DEBUG_NOTIFICATION, "ChatFirebaseMessagingService.sendNotification: notification manager got");

        // required from heads up notification
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            NotificationChannel mChannel = new NotificationChannel(conversationId, conversationId, NotificationManager.IMPORTANCE_HIGH);
//            notificationManager.createNotificationChannel(mChannel);
//        }

        notificationManager.notify(notificationId, notificationBuilder.build());
        Log.d(DEBUG_NOTIFICATION, "ChatFirebaseMessagingService.sendNotification: notification notified");
    }

    // resolve Issue #22
//    private void sendGroupNotification(String title, String message,
//                                       String conversationId, String timestamp) {
//        Log.d(TAG, "sendGroupNotification");
//
//        // FIXME: 24/11/17
////        Chat.Configuration.setContext(getApplicationContext());
//
//        int notificationId = (int) new Date().getTime();
//
//        Intent resultIntent = new Intent(this, MessageListActivity.class);
//        resultIntent.putExtra(INTENT_BUNDLE_RECIPIENT_ID, conversationId);
//        resultIntent.putExtra(ChatUI.INTENT_BUNDLE_IS_FROM_NOTIFICATION, true);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addParentStack(ConversationListActivity.class);
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
////        showGroupSummaryNotification(conversation);
//
//        Notification notification = createNotificationObject(resultPendingIntent, title, message, timestamp);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(notificationId, notification);
//    }
//
//    // retrieve the group_id
//    private String getGroupId(Map<String, String> pushData) {
//        try {
//            String groupId = (String) pushData.get("group_id");
//            if (StringUtils.isValid(groupId)) {
//                return groupId;
//            } else {
//                Log.w(DEBUG_NOTIFICATION, "group_id is empty or null. ");
//            }
//        } catch (Exception e) {
//            Log.w(DEBUG_NOTIFICATION, "cannot retrieve group_id. it may not exist" + e.getMessage());
//        }
//        return null;
//    }
//
//    // retrieve the group_name
//    private String getGroupName(Map<String, String> pushData) {
//        try {
//            String groupName = (String) pushData.get("group_name");
//            if (StringUtils.isValid(groupName)) {
//                return groupName;
//            } else {
//                Log.w(DEBUG_NOTIFICATION, "group_name is empty or null. ");
//            }
//        } catch (Exception e) {
//            Log.w(DEBUG_NOTIFICATION, "cannot retrieve group_name. it may not exist" + e.getMessage());
//        }
//
//        return null;
//    }
}