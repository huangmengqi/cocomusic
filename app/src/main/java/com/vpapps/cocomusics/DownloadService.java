package com.vpapps.cocomusics;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import com.vpapps.item.ItemSong;
import com.vpapps.utils.Encrypter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

public class DownloadService extends Service {

    NotificationCompat.Builder myNotify;
    RemoteViews rv;
    OkHttpClient client;
    public static final String ACTION_STOP = "com.mydownload.action.STOP";
    public static final String ACTION_START = "com.mydownload.action.START";
    public static final String ACTION_ADD = "com.mydownload.action.ADD";
    private static final String CANCEL_TAG = "c_tag";
    NotificationManager mNotificationManager;
    public static DownloadService downloadService;

    Encrypter enc;
    Boolean isDownloaded = false;
    Thread thread;
    Call call;
    public static int count = 0;
    public static ArrayList<String> arrayListName = new ArrayList<>();
    public static ArrayList<String> arrayListFilePath = new ArrayList<>();
    public static ArrayList<String> arrayListURL = new ArrayList<>();
    public static ArrayList<ItemSong> arrayListSong = new ArrayList<>();

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    int progress = Integer.parseInt(message.obj.toString());
                    rv.setProgressBar(R.id.progress, 100, progress, false);
//                    rv.setTextViewText(R.id.nf_percentage, getResources().getString(R.string.downloading) + " " + "(" + progress + " %)");
                    mNotificationManager.notify(1002, myNotify.build());
                    break;
                case 0:
                    rv.setTextViewText(R.id.nf_title, arrayListSong.get(0).getTitle());
                    rv.setTextViewText(R.id.nf_percentage, count - (arrayListURL.size() - 1) + "/" + count + " " + getString(R.string.downloading));
                    mNotificationManager.notify(1002, myNotify.build());
                    break;
                case 2:
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    rv.setProgressBar(R.id.progress, 100, 100, false);
                    rv.setTextViewText(R.id.nf_percentage, count + "/" + count + " " + getString(R.string.downloaded));
                    mNotificationManager.notify(1002, myNotify.build());
                    count = 0;
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        stopForeground(STOP_FOREGROUND_DETACH);
//                    }
                    break;
            }
            return false;
        }
    });

    public static DownloadService getInstance() {
        if (downloadService == null) {
            downloadService = new DownloadService();
        }
        return downloadService;
    }

    public static Boolean isDownloading() {
        return arrayListFilePath.size() != 0;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        enc = Encrypter.GetInstance();
        enc.Init(this, BuildConfig.DOWNLOAD_ENC_KEY);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "download_ch_1";
        myNotify = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        myNotify.setChannelId(NOTIFICATION_CHANNEL_ID);
        myNotify.setSmallIcon(R.drawable.ic_notification);
        myNotify.setTicker(getResources().getString(R.string.downloading));
        myNotify.setWhen(System.currentTimeMillis());
        myNotify.setOnlyAlertOnce(true);

        rv = new RemoteViews(getPackageName(), R.layout.my_custom_notification);
        rv.setTextViewText(R.id.nf_title, getString(R.string.app_name));
        rv.setProgressBar(R.id.progress, 100, 0, false);
        rv.setTextViewText(R.id.nf_percentage, getResources().getString(R.string.downloading) + " " + "(0%)");

        Intent closeIntent = new Intent(this, DownloadService.class);
        closeIntent.setAction(ACTION_STOP);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);
        rv.setOnClickPendingIntent(R.id.iv_stop_download, pcloseIntent);

        myNotify.setCustomContentView(rv);
        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Online Channel download";// The user-visible name of the channel.
            mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(mChannel);
        }
        startForeground(1002, myNotify.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            stopForeground(true);
            stop(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            if (intent.getAction() != null && intent.getAction().equals(ACTION_START)) {
                arrayListURL.add(intent.getStringExtra("downloadUrl"));
                arrayListFilePath.add(intent.getStringExtra("file_path"));
                arrayListName.add(intent.getStringExtra("file_name"));
                arrayListSong.add((ItemSong) intent.getSerializableExtra("item"));
                count = count + 1;
                init();
            } else if (intent.getAction() != null && intent.getAction().equals(ACTION_STOP)) {
                stop(intent);
            } else if (intent.getAction() != null && intent.getAction().equals(ACTION_ADD)) {
                ItemSong itemSong = (ItemSong) intent.getSerializableExtra("item");
                boolean flag = false;
                for (int i = 0; i < arrayListSong.size(); i++) {
                    if (itemSong.getId().equals(arrayListSong.get(i).getId())) {
                        flag = true;
                        break;
                    }
                }

                if (!flag) {
                    count = count + 1;
                    arrayListURL.add(intent.getStringExtra("downloadUrl"));
                    arrayListFilePath.add(intent.getStringExtra("file_path"));
                    arrayListName.add(intent.getStringExtra("file_name"));
                    arrayListSong.add(itemSong);

                    Message msg = mHandler.obtainMessage();
                    msg.what = 0;
                    mHandler.sendMessage(msg);
                }
            }
        }
        return START_STICKY;
    }

    private void stop(Intent intent) {
        try {
            count = 0;
            if (client != null) {
                for (Call call : client.dispatcher().runningCalls()) {
                    if (call.request().tag().equals(CANCEL_TAG))
                        call.cancel();
                }
            }
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
            try {
                new File(arrayListFilePath.get(0) + "/" + arrayListName.get(0).replace(".mp3", "")).delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            arrayListSong.clear();
            arrayListName.clear();
            arrayListURL.clear();
            arrayListFilePath.clear();
            stopForeground(true);
            if (intent != null) {
                stopService(intent);
            } else {
                stopSelf();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                isDownloaded = false;

                client = new OkHttpClient();
                Request.Builder builder = new Request.Builder()
                        .url(arrayListURL.get(0))
                        .addHeader("Accept-Encoding", "identity")
                        .get()
                        .tag(CANCEL_TAG);

                call = client.newCall(builder.build());
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        assert response.body() != null;
                        ResponseBody responseBody = ProgressHelper.withProgress(response.body(), new ProgressUIListener() {

                            //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                            @Override
                            public void onUIProgressStart(long totalBytes) {
                                super.onUIProgressStart(totalBytes);
                                Message msg = mHandler.obtainMessage();
                                msg.what = 0;
                                mHandler.sendMessage(msg);
                            }

                            @Override
                            public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                                if (!isDownloaded) {
                                    Message msg = mHandler.obtainMessage();
                                    msg.what = 1;
                                    msg.obj = (int) (100 * percent) + "";
                                    mHandler.sendMessage(msg);
                                }
                            }

                            //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                            @Override
                            public void onUIProgressFinish() {
                                super.onUIProgressFinish();
//                                try {
//                                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{currentPath},
//                                            null,
//                                            new MediaScannerConnection.OnScanCompletedListener() {
//                                                @Override
//                                                public void onScanCompleted(String path, Uri uri) {
//
//                                                }
//                                            });
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
                            }
                        });

                        try {

                            BufferedSource source = responseBody.source();
//                            File outFile = new File(arrayListFilePath.get(0) + "/" + arrayListName.get(0));
//                            BufferedSink sink = Okio.buffer(Okio.sink(outFile));
//                            source.readAll(sink);
//                            sink.flush();
//                            source.close();
                            enc.encrypt(arrayListFilePath.get(0) + "/" + arrayListName.get(0), source, arrayListSong.get(0));

                        } catch (Exception e) {
                            Log.d("show_data", e.toString());
                        }


                        if (arrayListURL.size() > 0) {
                            arrayListSong.remove(0);
                            arrayListName.remove(0);
                            arrayListFilePath.remove(0);
                            arrayListURL.remove(0);
                            if (!call.isCanceled() && arrayListURL.size() > 0) {
                                init();
                            } else {
                                Message msg = mHandler.obtainMessage();
                                msg.what = 2;
                                msg.obj = 0 + "";
                                mHandler.sendMessage(msg);
                                isDownloaded = true;
                            }
                        }
                    }
                });
            }
        });
        thread.start();
    }
}