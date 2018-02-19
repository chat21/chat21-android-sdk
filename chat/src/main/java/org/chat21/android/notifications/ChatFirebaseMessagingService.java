package org.chat21.android.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;

import org.chat21.android.R;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.messages.activities.MessageListActivity;
import org.chat21.android.utils.StringUtils;

import static org.chat21.android.ui.ChatUI.BUNDLE_CHANNEL_TYPE;
import static org.chat21.android.utils.DebugConstants.DEBUG_NOTIFICATION;

/**
 * Created by stefanodp91 on 06/02/18.
 */

public class ChatFirebaseMessagingService extends FirebaseMessagingService {

    // There are two types of messages data messages and notification messages. Data messages are handled
    // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
    // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
    // is in the foreground. When the app is in the background an automatically generated notification is displayed.
    // When the user taps on the notification they are returned to the app. Messages containing both notification
    // and data payloads are treated as notification messages. The Firebase console always sends notification
    // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(DEBUG_NOTIFICATION, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(DEBUG_NOTIFICATION, "Message data payload: " + remoteMessage.getData());
//            example DIRECT:
//            {
//                sender=u2K7nLo2dTZEOYYTykrufN6BDF92,
//                sender_fullname=Stefano De Pascalis,
//                channel_type=direct,
//                text=Foreground,
//                timestamp=1517913805930,
//                recipient_fullname=Pinch Tozoom,
//                recipient=QetCMCeMldY06F4YPOeC6Rvph4C3
//            }
//
//            example GROUP:
//            {
//                google.sent_time=1518079821473,
//                google.ttl=2419200,
//                gcm.notification.e=1,
//                sender=QetCMCeMldY06F4YPOeC6Rvph4C3,
//                sender_fullname=Pinch Tozoom,
//                gcm.notification.sound=default,
//                gcm.notification.title=Pinch Tozoom,
//                channel_type=group,
//                from=77360455507,
//                text=Foreground group,
//                gcm.notification.sound2=default,
//                timestamp=1518079821455,
//                google.message_id=0:1518079821483572%90a182f990a182f9,
//                recipient_fullname=Meeting 8 Feb 2018,
//                gcm.notification.body=Foreground group,
//                gcm.notification.icon=ic_notification_small,
//                recipient=-L4oY__34pvOXBfgeTak,
//                gcm.notification.click_action=NEW_MESSAGE,
//                collapse_key=chat21.android.demo
//            }

            String sender = remoteMessage.getData().get("sender");
            String senderFullName = remoteMessage.getData().get("sender_fullname");
            String channelType = remoteMessage.getData().get("channel_type");
            String text = remoteMessage.getData().get("text");
            String timestamp = remoteMessage.getData().get("timestamp");
            String recipientFullName = remoteMessage.getData().get("recipient_fullname");
            String recipient = remoteMessage.getData().get("recipient");

            String currentOpenConversationId = ChatManager.getInstance()
                    .getConversationsHandler()
                    .getCurrentOpenConversationId();

            if (channelType.equals(Message.DIRECT_CHANNEL_TYPE)) {

                if(StringUtils.isValid(currentOpenConversationId) && !currentOpenConversationId.equals(sender)) {
                    sendDirectNotification(sender, senderFullName, text, channelType);
                } else {
                    if(!StringUtils.isValid(currentOpenConversationId)) {
                        sendDirectNotification(sender, senderFullName, text, channelType);
                    }
                }
            } else if (channelType.equals(Message.GROUP_CHANNEL_TYPE)) {
                if(StringUtils.isValid(currentOpenConversationId) && !currentOpenConversationId.equals(recipient)) {
                    sendGroupNotification(recipient, recipientFullName, senderFullName, text, channelType);
                } else {
                    if(!StringUtils.isValid(currentOpenConversationId)) {
                        sendGroupNotification(recipient, recipientFullName, senderFullName, text, channelType);
                    }
                }
            } else {
                // default case
                if(StringUtils.isValid(currentOpenConversationId) && !currentOpenConversationId.equals(sender)) {
                    sendDirectNotification(sender, senderFullName, text, channelType);
                } else {
                    if(!StringUtils.isValid(currentOpenConversationId)) {
                        sendDirectNotification(sender, senderFullName, text, channelType);
                    }
                }
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(DEBUG_NOTIFICATION, "Message Notification Body: " + remoteMessage.getNotification().getBody());
//            example DIRECT:
//            Message Notification Body: Foreground
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    /**
     * Create and show a direct notification containing the received FCM message.
     *
     * @param sender         the id of the message's sender
     * @param senderFullName the display name of the message's sender
     * @param text           the message text
     */
    private void sendDirectNotification(String sender, String senderFullName, String text, String channel) {

        Intent intent = new Intent(this, MessageListActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, new ChatUser(sender, senderFullName));
        intent.putExtra(BUNDLE_CHANNEL_TYPE, channel);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channel)
                        .setSmallIcon(R.drawable.ic_notification_small)
                        .setContentTitle(senderFullName)
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Oreo fix
        String channelId = channel;
        String channelName = channel;
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        int notificationId = (int) new Date().getTime();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    /**
     * Create and show a group notification containing the received FCM message.
     *
     * @param sender         the id of the message's sender
     * @param senderFullName the display name of the message's sender
     * @param text           the message text
     */
    private void sendGroupNotification(String sender, String senderFullName, String recipientFullName, String text, String channel) {

        String title = recipientFullName + " @" + senderFullName;

        Intent intent = new Intent(this, MessageListActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, new ChatUser(sender, senderFullName));
        intent.putExtra(BUNDLE_CHANNEL_TYPE, channel);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channel)
                        .setSmallIcon(R.drawable.ic_notification_small)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Oreo fix
        String channelId = channel;
        String channelName = channel;
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        int notificationId = (int) new Date().getTime();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}