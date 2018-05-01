package com.example.admin.navisuber;

import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.renderscript.RenderScript;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String CHANNEL_ID = "notification_channel";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        if(remoteMessage.getData().size() > 0){
            try {
                JSONObject jsonObject = new JSONObject(remoteMessage.getData().toString());
                JSONObject data = jsonObject.getJSONObject(getResources().getString(R.string.notification_data));

                String title = data.getString(getResources().getString(R.string.notification_title));
                String message = data.getString(getResources().getString(R.string.notification_message));

                Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setSound(defaultSoundUri)
                        //.setVibrate(new long[] {})
                        .setAutoCancel(true);
                        //.setContentIntent()

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(1, builder.build());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
