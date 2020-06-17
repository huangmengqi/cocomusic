package com.vpapps.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;
import com.vpapps.cocomusics.BuildConfig;
import com.vpapps.cocomusics.R;
import com.vpapps.cocomusics.SplashActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class NotificationExtenderExample extends NotificationExtenderService {

    public static final int NOTIFICATION_ID = 1;
    String title, message, bigpicture, sname, url;

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {

        title = receivedResult.payload.title;
        message = receivedResult.payload.body;
        bigpicture = receivedResult.payload.bigPicture;

        try {
            Constant.pushCID = receivedResult.payload.additionalData.getString("cat_id");
            Constant.pushCName = receivedResult.payload.additionalData.getString("cat_name");

            if(receivedResult.payload.additionalData.has("artist_id")) {
                Constant.pushArtID = receivedResult.payload.additionalData.getString("artist_id");
                Constant.pushArtNAME = receivedResult.payload.additionalData.getString("artist_name");
            }

            if(receivedResult.payload.additionalData.has("album_id")) {
                Constant.pushAlbID = receivedResult.payload.additionalData.getString("album_id");
                Constant.pushAlbNAME = receivedResult.payload.additionalData.getString("album_name");
            }

            if(receivedResult.payload.additionalData.has("song_id")) {
                Constant.pushSID = receivedResult.payload.additionalData.getString("song_id");
                sname = receivedResult.payload.additionalData.getString("song_name");
            }
            url = receivedResult.payload.additionalData.getString("external_link");
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendNotification();

        return true;
    }

    private void sendNotification() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent;
        if (!Constant.pushSID.equals("0") || !Constant.pushCID.equals("0") || !Constant.pushArtID.equals("0") || !Constant.pushAlbID.equals("0")) {
            intent = new Intent(this, SplashActivity.class);
            intent.putExtra("ispushnoti", true);
        } else if(!url.equals("false") && !url.trim().isEmpty()){
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
        } else {
            intent = new Intent(this, SplashActivity.class);
        }

        NotificationChannel mChannel;
        String NOTIFICATION_CHANNEL_ID = "onlinesong_push";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Onlinesong Channel";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setSound(uri)
                .setAutoCancel(true)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setLights(Color.RED, 800, 800)
                .setContentText(message);

        mBuilder.setSmallIcon(getNotificationIcon(mBuilder));

        mBuilder.setContentTitle(title);
        mBuilder.setTicker(message);

        if (bigpicture != null) {
            mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(bigpicture)).setSummaryText(message));
        } else {
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        }

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

    }

    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(getColour());
            return R.drawable.ic_notification;
        } else {
            return R.mipmap.app_icon;
        }
    }

    private int getColour() {
        return 0xee2c7a;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            InputStream input;
            if(BuildConfig.SERVER_URL.contains("https://")) {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            } else {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            }
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }
}