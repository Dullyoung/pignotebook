package com.pig.notebook.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.pig.notebook.MainActivity;
import com.pig.notebook.R;

public class NotificationService extends Service {
    public NotificationService() {
    }

    NotificationManager notificationManager;
    String TAG = "aaaa";

    @Override

    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!=null){
            Bundle bundle = intent.getExtras();
            Log.i(TAG, "onBind: " + bundle.getString("id"));
            String text = bundle.getString("text");
            String title = bundle.getString("title");
            int id = Integer.parseInt(bundle.getString("id"));
            int imp = Integer.parseInt(bundle.getString("imp"));
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            getNotificationCompat(text, title, id, imp);
        }



        return START_STICKY;
    }

    private void getNotificationCompat(String text, String title, int id, int importance) {

        Notification.Builder notifyBuilder = new Notification.Builder(this)
                .setContentText(text)
                .setContentTitle(title)
                // 点击消失
                .setAutoCancel(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_background)
                // 通知产生的时间，会在通知信息里显示
                // 向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_ALL | Notification.DEFAULT_SOUND);
        //2：如果是8以上的系统，则新建一个消息通道，传一个channelId
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "pig";//消息通道的id，以后可以通过该id找到该消息通道
            String channelName = "小猪通知";//消息通道的name
            // 通知的优先级，作用就是优先级的不同。可以导致消息出现的形式不一样。
            // HIGH是会震动并且出现在屏幕的上方。设置优先级为low或者min时。来通知时都不会震动，且不会直接出现在屏幕上方
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            notifyBuilder.setChannelId(channelId);
        }
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        notifyBuilder.setContentIntent(resultPendingIntent);
        Notification notification = notifyBuilder.build();
        if (importance == 0) {
            notification.flags = NotificationManager.IMPORTANCE_MAX;
        }
        if (importance == 1) {
            notification.flags = Notification.FLAG_ONGOING_EVENT;
        }
        notificationManager.notify(id, notification);
    }
}
