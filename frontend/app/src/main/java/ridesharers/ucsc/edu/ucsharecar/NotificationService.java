package ridesharers.ucsc.edu.ucsharecar;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/*
 * NotificationService handles everything related to push notifications. It sends new tokens for
 * users to the BackendClient to handle, and also processes incoming data.
 *
 * For some reason it only gets activated when messages have *only* data, no notification attribute.
 * So we only use data.
 *
 * Note that this NotificationService should not try to get the BackendClient singleton while in the
 * background. This causes a crash. Right now, we try to only use it to send the token which should
 * only happen when the app is running (I hope.) TODO verify that
 */
public class NotificationService extends FirebaseMessagingService {
    private String TAG = "UCShareCar_FCM";

    public NotificationService() {
        super();
        Log.d(TAG, "Constructed NotificationService");
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "FCM from: " + remoteMessage.getFrom());

        // Only process if we have a notification
        if (remoteMessage.getData().size() > 0) {

            // Send the notification
            sendNotification(remoteMessage.getData().get("post_id"));
        }
        else {
            Log.w(TAG, "Received a malformed message");
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        BackendClient.getSingleton(this).registerFCM(token);
    }

    private void sendNotification(String postId) {
        Log.d(TAG, "Sending notification with postId="+postId);

        // Create the intent with the post
        Intent intent = new Intent(NotificationService.this, PostDetailActivity.class);
        intent.putExtra("post_id", postId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                NotificationService.this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        // Set some notification info
        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(NotificationService.this, channelId)
                        .setSmallIcon(R.drawable.car_logo)
                        .setContentTitle("Post Update")
                        .setContentText("A user has joined a ride you are in!")
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Send the notification!
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
