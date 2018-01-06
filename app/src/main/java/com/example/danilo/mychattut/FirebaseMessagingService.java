package com.example.danilo.mychattut;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by Danilo on 2017-12-01.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    //on message received while the app is running
    String clickAction;
    String from_sender_id;
    String notification_title;
    String notification_body;
    // Sets an ID for the notification
    int mNotificationId = (int) System.currentTimeMillis();
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String CHANNEL_ID = "my_channel_01";
        Log.d("FIEBASEMESS", "Message data payload: " + remoteMessage.getData().get("from_sender_id"));
        if (remoteMessage == null)
        {
            return;
        }

        clickAction = remoteMessage.getNotification().getClickAction();
        Intent resultIntent = new Intent(clickAction);
        if(remoteMessage.getData().get("from_sender_id") != null)
        {
            from_sender_id = remoteMessage.getData().get("from_sender_id");
            resultIntent.putExtra("visit_user_id", from_sender_id);
            Log.d("FIEBASEMESS222222222222", "INSIDE IF : " + remoteMessage.getData().get("from_sender_id"));
        }


        notification_title = remoteMessage.getNotification().getTitle();
        notification_body = remoteMessage.getNotification().getBody();


        // Check if message contains a notification payload.



        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);



        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        mNotificationId,
                        resultIntent,
                        PendingIntent.FLAG_ONE_SHOT
                );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this,CHANNEL_ID)
                        .setSmallIcon(R.mipmap.mychaticon)
                        .setAutoCancel(true)
                        .setContentTitle(notification_title)
                        .setContentText(notification_body)
                        .setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager)
                        getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(mNotificationId, mBuilder.build());
    }
}
