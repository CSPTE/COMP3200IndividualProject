package com.example.finalversion.notification;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.finalversion.MainActivity;
import com.example.finalversion.R;

public class Receiver extends BroadcastReceiver {
    public String receiverId = "ID";
    public String receiverName = "Task";

    @Override
    public void onReceive(Context context, Intent intent) {
        receiverId = intent.getStringExtra("ID");
        receiverName = intent.getStringExtra("Name");
        showNotification(context);
    }

    public void showNotification(Context context) {
        int integerId = Integer.parseInt(receiverId);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 9999, intent, PendingIntent.FLAG_IMMUTABLE |
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "Channel1")
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(receiverName + " Reminder")
                .setContentText("We are what we repeatedly do. Excellence, then, is not an act, but a habit. - Will Durant")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pi)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(integerId, mBuilder.build());
    }
}
