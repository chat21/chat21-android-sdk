package chat21.android.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import chat21.android.R;
import chat21.android.core.users.models.IChatUser;
import chat21.android.ui.ChatUI;
import chat21.android.ui.messages.activities.MessageListActivity;

import static chat21.android.utils.DebugConstants.DEBUG_NOTIFICATION;

/**
 * Created by stefanodp91 on 17/01/18.
 */

public class NotificationUtils {

    public static Intent createNotificationIntent(Context context, IChatUser sender) {
        final Intent notificationIntent = new Intent(context, MessageListActivity.class);

        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.putExtra(ChatUI.INTENT_BUNDLE_RECIPIENT, sender);
        notificationIntent.putExtra(ChatUI.INTENT_BUNDLE_IS_FROM_NOTIFICATION, true);

        return notificationIntent;
    }

    public static PendingIntent createPendingIntent(Context context, Intent notificationIntent) {
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return resultPendingIntent;
    }

    public static NotificationCompat.Builder createNotification(Context context, String conversationId, String title, String message,
                                                                String timestamp) {
        Log.d(DEBUG_NOTIFICATION, "createNotificationObject");

        // resolve Issue #38
        RemoteViews contentView = new RemoteViews(context.getPackageName(),
                R.layout.layout_custom_notification);
        contentView.setImageViewResource(R.id.image, R.drawable.ic_notification_foreground);
        contentView.setTextViewText(R.id.title, title);
        contentView.setTextViewText(R.id.text, message);
        contentView.setTextViewText(R.id.time, timestamp);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, conversationId)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContent(contentView)
                .setTicker(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setWhen(0)
                .setAutoCancel(true)
//                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));


////        Notification notification = mBuilder.build();
////        notification.flags |= Notification.FLAG_AUTO_CANCEL;
////        notification.defaults |= Notification.DEFAULT_SOUND;
////        notification.defaults |= Notification.DEFAULT_VIBRATE;
////
////        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
////
////        Notification notification = mBuilder
////                .setSmallIcon(R.drawable.ic_notification_small)
////                .setTicker(title)
////                .setContentText(message)
////                .setWhen(0)
////                .setAutoCancel(true)
////                .setContentTitle(title)
////                .setStyle(new NotificationCompat.InboxStyle())
////                .setContentIntent(pendingIntent)
////                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
////                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_foreground))
////                .build();
////
////        // hides the small icon at the bottom
////        // source: https://stackoverflow.com/questions/30887078/notificationcompat-android-how-to-show-only-large-icon-without-small
////        int smallIconId = getApplicationContext().getResources().getIdentifier("right_icon", "id", android.R.class.getPackage().getName());
////        if (smallIconId != 0) {
////            if (notification.contentView!=null)
////                notification.contentView.setViewVisibility(smallIconId, View.GONE);
////        }

        return builder;
    }

    public static NotificationCompat.Builder createHeadsUpNotification(Context context, String conversationId, String title, String message,
                                                                       String timestamp) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, conversationId)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setStyle(new NotificationCompat.InboxStyle())
//                // Show controls on lock screen even when user hides sensitive content.
//                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setTicker(title)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setWhen(0)
                .setAutoCancel(true)
                // Apply the media style template
                .setContentTitle(title)
                .setContentText(message)

//                        .setLargeIcon(albumArtBitmap)
                ;

        return builder;
    }
}
