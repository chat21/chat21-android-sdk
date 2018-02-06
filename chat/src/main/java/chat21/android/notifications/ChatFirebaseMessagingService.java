package chat21.android.notifications;

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

import chat21.android.R;
import chat21.android.core.messages.models.Message;
import chat21.android.core.users.models.ChatUser;
import chat21.android.ui.ChatUI;
import chat21.android.ui.messages.activities.MessageListActivity;

import static chat21.android.ui.ChatUI.BUNDLE_CHANNEL_TYPE;
import static chat21.android.utils.DebugConstants.DEBUG_NOTIFICATION;

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
//            Message data payload: {
//                sender=u2K7nLo2dTZEOYYTykrufN6BDF92,
//                sender_fullname=Stefano De Pascalis,
//                channel_type=direct,
//                text=Foreground,
//                timestamp=1517913805930,
//                recipient_fullname=Pinch Tozoom,
//                recipient=QetCMCeMldY06F4YPOeC6Rvph4C3
//            }

            String sender = remoteMessage.getData().get("sender");
            String senderFullName = remoteMessage.getData().get("sender_fullname");
            String channelType = remoteMessage.getData().get("channel_type");
            String text = remoteMessage.getData().get("text");
            String timestamp = remoteMessage.getData().get("timestamp");
            String recipientFullName = remoteMessage.getData().get("recipient_fullname");
            String recipient = remoteMessage.getData().get("recipient");

            if (channelType.equals(Message.DIRECT_CHANNEL_TYPE)) {
                sendDirectNotification(sender, senderFullName, text, channelType);
            } else if (channelType.equals(Message.DIRECT_CHANNEL_TYPE)) {
//                sendGroupNotification();
            } else {
                // default case
                sendDirectNotification(sender, senderFullName, text, channelType);
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
     * Create and show a simple notification containing the received FCM message.
     *
     * @param sender         the id of the message's sender
     * @param senderFullName the display name of the message's sender
     * @param text           the message text
     */
    private void sendDirectNotification(String sender, String senderFullName, String text, String channel) {

        Intent intent = new Intent(this, MessageListActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, new ChatUser(sender, senderFullName));
        intent.putExtra(BUNDLE_CHANNEL_TYPE, channel);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

//        String channelId = getString(R.string.default_notification_channel_id);
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
}