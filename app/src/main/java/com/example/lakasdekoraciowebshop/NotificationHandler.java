package com.example.lakasdekoraciowebshop;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.nio.channels.Channel;

public class NotificationHandler {
    private NotificationManager mManager;
    private Context mContext;
    private static final String CHANNEL_ID = "shop_notification_channel";
    private final int NOTIFICATION_ID = 0;

    public NotificationHandler(Context context) {
        this.mContext = context;
        this.mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createChannel();

    }

    private void createChannel(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            return;
        } else{
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Shop notification", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.RED);
            channel.setDescription("Notification from Lakásdekor Webshop");
            this.mManager.createNotificationChannel(channel);
        }

    }

    public void send(String message){

        Intent intent = new Intent(mContext, ShopListActivity.class) ;
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle("Lakásdekor Webshop")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_baseline_house_)
                .setContentIntent(pendingIntent);
        this.mManager.notify(NOTIFICATION_ID, builder.build());

    }

    public void cancel(){
        this.mManager.cancel(NOTIFICATION_ID);
    }
}
