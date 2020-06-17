package com.vpapps.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.vpapps.adapter.AdapterPlaylistDialog;
import com.vpapps.interfaces.ClickListenerPlayList;
import com.vpapps.interfaces.InterAdListener;
import com.vpapps.item.ItemMyPlayList;
import com.vpapps.item.ItemSong;
import com.vpapps.item.ItemUser;
import com.vpapps.cocomusics.BuildConfig;
import com.vpapps.cocomusics.DownloadService;
import com.vpapps.cocomusics.LoginActivity;
import com.vpapps.cocomusics.R;
import com.yakivmospan.scytale.Crypto;
import com.yakivmospan.scytale.Options;
import com.yakivmospan.scytale.Store;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

import javax.crypto.SecretKey;
import javax.net.ssl.HttpsURLConnection;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Methods {


    @SerializedName("song_image")
    String song_image = "song_image";

    private Context context;
    private DBHelper dbHelper;
    private InterAdListener interAdListener;
    private SecretKey key;

    public Methods(Context context, Boolean flag) {
        this.context = context;

        Store store = new Store(context);
        if (!store.hasKey(BuildConfig.API_KEY)) {
            key = store.generateSymmetricKey(BuildConfig.API_KEY, null);
        } else {
            key = store.getSymmetricKey(BuildConfig.API_KEY, null);
        }
    }

    // constructor
    public Methods(Context context) {
        this.context = context;
        dbHelper = new DBHelper(context);
    }

    public Methods(Context context, InterAdListener interAdListener) {
        this.context = context;
        this.interAdListener = interAdListener;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();

        point.x = display.getWidth();
        point.y = display.getHeight();

        columnWidth = point.x;
        return columnWidth;
    }

    public int getScreenHeight() {
        int height;
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();

        point.x = display.getWidth();
        point.y = display.getHeight();

        height = point.y;
        return height;
    }

    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    public void animateHeartButton(final View v) {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(v, "rotation", 0f, 360f);
        rotationAnim.setDuration(300);
        rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(v, "scaleX", 0.2f, 1f);
        bounceAnimX.setDuration(300);
        bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(v, "scaleY", 0.2f, 1f);
        bounceAnimY.setDuration(300);
        bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
        bounceAnimY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });

        animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);
        animatorSet.start();
    }

    public void animatePhotoLike(final View vBgLike, final View ivLike) {
        vBgLike.setVisibility(View.VISIBLE);
        ivLike.setVisibility(View.VISIBLE);

        vBgLike.setScaleY(0.1f);
        vBgLike.setScaleX(0.1f);
        vBgLike.setAlpha(1f);
        ivLike.setScaleY(0.1f);
        ivLike.setScaleX(0.1f);

        android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();

        android.animation.ObjectAnimator bgScaleYAnim = android.animation.ObjectAnimator.ofFloat(vBgLike, "scaleY", 0.1f, 1f);
        bgScaleYAnim.setDuration(200);
        bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        android.animation.ObjectAnimator bgScaleXAnim = android.animation.ObjectAnimator.ofFloat(vBgLike, "scaleX", 0.1f, 1f);
        bgScaleXAnim.setDuration(200);
        bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        android.animation.ObjectAnimator bgAlphaAnim = android.animation.ObjectAnimator.ofFloat(vBgLike, "alpha", 1f, 0f);
        bgAlphaAnim.setDuration(200);
        bgAlphaAnim.setStartDelay(150);
        bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

        android.animation.ObjectAnimator imgScaleUpYAnim = android.animation.ObjectAnimator.ofFloat(ivLike, "scaleY", 0.1f, 1f);
        imgScaleUpYAnim.setDuration(300);
        imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        android.animation.ObjectAnimator imgScaleUpXAnim = android.animation.ObjectAnimator.ofFloat(ivLike, "scaleX", 0.1f, 1f);
        imgScaleUpXAnim.setDuration(300);
        imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

        android.animation.ObjectAnimator imgScaleDownYAnim = android.animation.ObjectAnimator.ofFloat(ivLike, "scaleY", 1f, 0f);
        imgScaleDownYAnim.setDuration(300);
        imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
        android.animation.ObjectAnimator imgScaleDownXAnim = android.animation.ObjectAnimator.ofFloat(ivLike, "scaleX", 1f, 0f);
        imgScaleDownXAnim.setDuration(300);
        imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
        animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

        animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                vBgLike.setVisibility(View.INVISIBLE);
                ivLike.setVisibility(View.INVISIBLE);
            }
        });
        animatorSet.start();
    }

    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String hourString = "";
        String secondsString = "";
        String minutesString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        long temp_milli = (long) calculateTime(Constant.arrayList_play.get(Constant.playPos).getDuration());
        int temp_hour = (int) (temp_milli / (1000 * 60 * 60));
        if (temp_hour != 0) {
            hourString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        // Prepending 0 to minutes if it is one digit
        if (minutes < 10) {
            minutesString = "0" + minutes;
        } else {
            minutesString = "" + minutes;
        }

        finalTimerString = hourString + minutesString + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public String milliSecondsToTimerDownload(long milliseconds) {
        String finalTimerString = "";
        String hourString = "";
        String secondsString = "";
        String minutesString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there

        if (hours != 0) {
            hourString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        // Prepending 0 to minutes if it is one digit
        if (minutes < 10) {
            minutesString = "0" + minutes;
        } else {
            minutesString = "" + minutes;
        }

        finalTimerString = hourString + minutesString + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }

    public long getSeekFromPercentage(int percentage, long totalDuration) {

        long currentSeconds = 0;
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        currentSeconds = (percentage * totalSeconds) / 100;

        // return percentage
        return currentSeconds * 1000;
    }

    /**
     * Function to change progress to timer
     *
     * @param progress      -
     * @param totalDuration returns current duration in milliseconds
     */
    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = totalDuration / 1000;
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    public int calculateTime(String duration) {
        int time = 0, min, sec, hr = 0;
        try {
            StringTokenizer st = new StringTokenizer(duration, ".");
            if (st.countTokens() == 3) {
                hr = Integer.parseInt(st.nextToken());
            }
            min = Integer.parseInt(st.nextToken());
            sec = Integer.parseInt(st.nextToken());
        } catch (Exception e) {
            StringTokenizer st = new StringTokenizer(duration, ":");
            if (st.countTokens() == 3) {
                hr = Integer.parseInt(st.nextToken());
            }
            min = Integer.parseInt(st.nextToken());
            sec = Integer.parseInt(st.nextToken());
        }
        time = ((hr * 3600) + (min * 60) + sec) * 1000;
        return time;
    }

    public void forceRTLIfSupported(Window window) {
        if (context.getResources().getString(R.string.isRTL).equals("true")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                window.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }
    }

    private AdView showPersonalizedAds(LinearLayout linearLayout) {
        if (Constant.isBannerAd) {
            AdView adView = new AdView(context);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.setAdUnitId(Constant.ad_banner_id);
            adView.setAdSize(AdSize.BANNER);
            linearLayout.addView(adView);
            adView.loadAd(adRequest);

            return adView;
        } else {
            return null;
        }
    }

    private AdView showNonPersonalizedAds(LinearLayout linearLayout) {
        Bundle extras = new Bundle();
        extras.putString("npa", "1");
        if (Constant.isBannerAd) {
            AdView adView = new AdView(context);
            AdRequest adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
            adView.setAdUnitId(Constant.ad_banner_id);
            adView.setAdSize(AdSize.BANNER);
            linearLayout.addView(adView);
            adView.loadAd(adRequest);
            return adView;
        } else {
            return null;
        }
    }

    public AdView showBannerAd(LinearLayout linearLayout) {
        if (isNetworkAvailable()) {
            Log.e("aaa - bannner",Constant.ad_banner_id);
            if (ConsentInformation.getInstance(context).getConsentStatus() == ConsentStatus.NON_PERSONALIZED) {
                return showNonPersonalizedAds(linearLayout);
            } else {
                return showPersonalizedAds(linearLayout);
            }
        } else {
            return null;
        }
    }

    public void showInterAd(final int pos, final String type) {
        if (Constant.isInterAd) {
            Constant.adCount = Constant.adCount + 1;
            if (Constant.adCount % Constant.adDisplay == 0) {
                final InterstitialAd interstitialAd = new InterstitialAd(context);
                AdRequest adRequest;
                if (ConsentInformation.getInstance(context).getConsentStatus() == ConsentStatus.PERSONALIZED) {
                    adRequest = new AdRequest.Builder()
                            .build();
                } else {
                    Bundle extras = new Bundle();
                    extras.putString("npa", "1");
                    adRequest = new AdRequest.Builder()
                            .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                            .build();
                }
                interstitialAd.setAdUnitId(Constant.ad_inter_id);
                interstitialAd.loadAd(adRequest);
                interstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        interstitialAd.show();
                    }

                    public void onAdClosed() {
                        interAdListener.onClick(pos, type);
                        super.onAdClosed();
                    }

                    @Override
                    public void onAdFailedToLoad(int i) {
                        interAdListener.onClick(pos, type);
                        super.onAdFailedToLoad(i);
                    }
                });
//                } else {
//                    interAdListener.onClick(pos, type);
//                }
            } else {
                interAdListener.onClick(pos, type);
            }
        } else {
            interAdListener.onClick(pos, type);
        }
    }

    public void setStatusColor(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    public String format(Number number) {
        char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
        long numValue = number.longValue();
        int value = (int) Math.floor(Math.log10(numValue));
        int base = value / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.0").format(numValue / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat("#,##0").format(numValue);
        }
    }

    public int getColumnWidth(int column, int grid_padding) {
        Resources r = context.getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, grid_padding, r.getDisplayMetrics());
        return (int) ((getScreenWidth() - ((column + 1) * padding)) / column);
    }

    public GradientDrawable getGradientDrawable(int first, int second) {
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(15);
        gd.setColors(new int[]{first, second});
        gd.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gd.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
        gd.mutate();
        return gd;
    }

    public Bitmap getBitmapFromURL(String src) {
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
            e.printStackTrace();
            return null;
        }
    }

    public boolean isYoutubeAppInstalled() {
        Intent mIntent = context.getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
        return mIntent != null;
    }

    public void openPlaylists(final ItemSong itemSong, final Boolean isOnline) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_playlist);

        final ArrayList<ItemMyPlayList> arrayList_playlist = dbHelper.loadPlayList(isOnline);

        final ImageView iv_close = dialog.findViewById(R.id.iv_playlist_close);
        final Button button_close = dialog.findViewById(R.id.button_close_dialog);
        final TextView textView = dialog.findViewById(R.id.tv_empty_dialog_pl);
        final RecyclerView recyclerView = dialog.findViewById(R.id.rv_dialog_playlist);
        final LinearLayout ll_add_playlist = dialog.findViewById(R.id.ll_add_playlist);
        final AppCompatButton btn_add_new = dialog.findViewById(R.id.button_add);
        final AppCompatButton btn_add = dialog.findViewById(R.id.button_dialog_addplaylist);
        final EditText et_Add = dialog.findViewById(R.id.et_playlist_name);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        final AdapterPlaylistDialog adapterPlaylist = new AdapterPlaylistDialog(arrayList_playlist, new ClickListenerPlayList() {
            @Override
            public void onClick(int position) {
                dbHelper.addToPlayList(itemSong, arrayList_playlist.get(position).getId(), isOnline);
                Toast.makeText(context, context.getString(R.string.song_add_to_playlist) + arrayList_playlist.get(position).getName(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

            @Override
            public void onItemZero() {
                textView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
        recyclerView.setAdapter(adapterPlaylist);
        if (arrayList_playlist.size() == 0) {
            textView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_add_playlist.setVisibility(View.VISIBLE);
                btn_add.setVisibility(View.GONE);
            }
        });

        btn_add_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_Add.getText().toString().trim().isEmpty()) {
                    arrayList_playlist.clear();
                    arrayList_playlist.addAll(dbHelper.addPlayList(et_Add.getText().toString(), isOnline));
                    adapterPlaylist.notifyDataSetChanged();
                    textView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    Toast.makeText(context, context.getString(R.string.playlist_added), Toast.LENGTH_SHORT).show();

                    et_Add.setText("");
                } else {
                    Toast.makeText(context, context.getString(R.string.enter_playlist_name), Toast.LENGTH_SHORT).show();
                }
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
    }

    public void getListOfflineSongs() {
        long aa = System.currentTimeMillis();
        Constant.arrayListOfflineSongs.clear();
        ContentResolver contentResolver = context.getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, MediaStore.Audio.Media.TITLE + " ASC");

        if (songCursor != null && songCursor.moveToFirst()) {
            do {
                String id = String.valueOf(songCursor.getLong(songCursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                long duration_long = songCursor.getLong(songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String title = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String url = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String image = "null";

                long albumId = songCursor.getLong(songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                image = String.valueOf(albumId);

                String duration = milliSecondsToTimerDownload(duration_long);

                String desc = context.getString(R.string.title) + " - " + title + "</br>" + context.getString(R.string.artist) + " - " + artist;

                Constant.arrayListOfflineSongs.add(new ItemSong(id, "", "", artist, url, image, image, title, duration, desc, "0", "0", "0", "0"));
            } while (songCursor.moveToNext());
        }
        long bb = System.currentTimeMillis();
    }

    public Uri getAlbumArtUri(int album_id) {
        Uri songCover = Uri.parse("content://media/external/audio/albumart");
        Uri uriSongCover = ContentUris.withAppendedId(songCover, album_id);
        if (uriSongCover == null) {
            return null;
        }
        return uriSongCover;
    }

    public void shareSong(ItemSong itemSong, Boolean isOnline) {
        if (isOnline) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.share_song));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, context.getResources().getString(R.string.listening) + " - " + itemSong.getTitle() + "\n\nvia " + context.getResources().getString(R.string.app_name) + " - http://play.google.com/store/apps/details?id=" + context.getPackageName());
            context.startActivity(Intent.createChooser(sharingIntent, context.getResources().getString(R.string.share_song)));
        } else {
            try {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("audio/mp3");
                share.putExtra(Intent.EXTRA_STREAM, Uri.parse(itemSong.getUrl()));
                share.putExtra(android.content.Intent.EXTRA_TEXT, context.getResources().getString(R.string.listening) + " - " + itemSong.getTitle() + "\n\nvia " + context.getResources().getString(R.string.app_name) + " - http://play.google.com/store/apps/details?id=" + context.getPackageName());
                context.startActivity(Intent.createChooser(share, context.getResources().getString(R.string.share_song)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public GradientDrawable getRoundDrawable(int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.mutate();
        gd.setCornerRadius(10);
        return gd;
    }

    public GradientDrawable getRoundDrawableRadis(int color, int radius) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.mutate();
        gd.setCornerRadius(radius);
        return gd;
    }

    public String encrypt(String value) {
        try {
            Crypto crypto = new Crypto(Options.TRANSFORMATION_SYMMETRIC);
            return crypto.encrypt(value, key);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String decrypt(String value) {
        try {
            Crypto crypto = new Crypto(Options.TRANSFORMATION_SYMMETRIC);
            return crypto.decrypt(value, key);
        } catch (Exception e) {
            Crypto crypto = new Crypto(Options.TRANSFORMATION_SYMMETRIC);
            return crypto.encrypt("null", key);
        }
    }

    public void clickLogin() {
        if (Constant.isLogged) {
            logout((Activity) context);
            Toast.makeText(context, context.getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(context, LoginActivity.class);
            intent.putExtra("from", "app");
            context.startActivity(intent);
        }
    }

    public void changeAutoLogin(Boolean isAutoLogin) {
        SharedPref sharePref = new SharedPref(context);
        sharePref.setIsAutoLogin(isAutoLogin);
    }

    public void logout(Activity activity) {
        changeAutoLogin(false);
        Constant.isLogged = false;
        Constant.itemUser = new ItemUser("", "", "", "");
        Intent intent1 = new Intent(context, LoginActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.putExtra("from", "");
        context.startActivity(intent1);
        activity.finish();
    }

    public void download(final ItemSong itemSong) {

        File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + context.getString(R.string.app_name) + "/temp");
        if (!root.exists()) {
            root.mkdirs();
        }

        Random random = new Random();
        String a = String.valueOf(System.currentTimeMillis());
        String name = String.valueOf(random.nextInt((999999 - 100000) + 100000)) + a.substring(a.length() - 6, a.length() - 1);

//            File file = new File(root, Constant.arrayList_play.get(viewpager.getCurrentItem()).getTitle() + ".mp3");
        File file = new File(root, name + ".mp3");

//            if (!file.exists()) {
        if (!dbHelper.checkDownload(itemSong.getId())) {

            String url = itemSong.getUrl();

            if (!DownloadService.getInstance().isDownloading()) {
                Intent serviceIntent = new Intent(context, DownloadService.class);
                serviceIntent.setAction(DownloadService.ACTION_START);
                serviceIntent.putExtra("downloadUrl", url);
                serviceIntent.putExtra("file_path", root.toString());
                serviceIntent.putExtra("file_name", file.getName());
                serviceIntent.putExtra("item", itemSong);
                context.startService(serviceIntent);
            } else {
                Intent serviceIntent = new Intent(context, DownloadService.class);
                serviceIntent.setAction(DownloadService.ACTION_ADD);
                serviceIntent.putExtra("downloadUrl", url);
                serviceIntent.putExtra("file_path", root.toString());
                serviceIntent.putExtra("file_name", file.getName());
                serviceIntent.putExtra("item", itemSong);
                context.startService(serviceIntent);
            }

            new AsyncTask<String, String, String>() {
                @Override
                protected String doInBackground(String... strings) {

                    String json = JsonUtils.okhttpPost(Constant.SERVER_URL, getAPIRequest(Constant.METHOD_DOWNLOAD_COUNT, 0, "", itemSong.getId(), "", "", "", "", "", "","","","","","","","", null));
//                    String json = JsonUtils.okhttpGET(Constant.URL_DOWNLOAD_COUNT + itemSong.getId());
                    return null;
                }
            }.execute();
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.already_download), Toast.LENGTH_SHORT).show();
        }
    }

    public String getPathImage(Uri uri) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String filePath = "";
                String wholeID = DocumentsContract.getDocumentId(uri);

                // Split at colon, use second item in the array
                String id = wholeID.split(":")[1];

                String[] column = {MediaStore.Images.Media.DATA};

                // where id is equal to
                String sel = MediaStore.Images.Media._ID + "=?";

                Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[]{id}, null);

                int columnIndex = cursor.getColumnIndex(column[0]);

                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
                return filePath;
            } else {

                if (uri == null) {
                    return null;
                }
                // try to retrieve the image from the media store first
                // this will only work for images selected from gallery
                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    int column_index = cursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String retunn = cursor.getString(column_index);
                    cursor.close();
                    return retunn;
                }
                // this is our fallback here
                return uri.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (uri == null) {
                return null;
            }
            // try to retrieve the image from the media store first
            // this will only work for images selected from gallery
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String returnn = cursor.getString(column_index);
                cursor.close();
                return returnn;
            }
            // this is our fallback here
            return uri.getPath();
        }
    }

    public void getVerifyDialog(String title, String message) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, R.style.ThemeDialog);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                finish();
            }
        });
        alertDialog.show();
    }

    public RequestBody getAPIRequest(String method, int page, String deviceID, String songID, String searchText, String searchType, String catID, String albumID, String artistName, String playListID, String rate, String email, String password, String name, String phone, String userID, String reportMessage, File file) {
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", method);
        jsObj.addProperty("package_name", context.getPackageName());

        switch (method) {
            case Constant.METHOD_LATEST:

                jsObj.addProperty("page", String.valueOf(page));

                break;
            case Constant.METHOD_SINGLE_SONG:

                jsObj.addProperty("song_id", songID);
                jsObj.addProperty("device_id", deviceID);

                break;
            case Constant.METHOD_SEARCH:

                jsObj.addProperty("page", String.valueOf(page));
                jsObj.addProperty("search_text", searchText);
                jsObj.addProperty("search_type", searchType);

                break;
            case Constant.METHOD_ALL_SONGS:

                jsObj.addProperty("page", String.valueOf(page));

                break;
            case Constant.METHOD_SONG_BY_CAT:

                jsObj.addProperty("cat_id", catID);
                jsObj.addProperty("page", String.valueOf(page));

                break;
            case Constant.METHOD_SONG_BY_ALBUMS:

                jsObj.addProperty("album_id", albumID);
                jsObj.addProperty("page", String.valueOf(page));

                break;
            case Constant.METHOD_SONG_BY_ARTIST:

                jsObj.addProperty("artist_name", artistName);
                jsObj.addProperty("page", String.valueOf(page));

                break;
            case Constant.METHOD_SONG_BY_PLAYLIST:

                jsObj.addProperty("playlist_id", playListID);
                jsObj.addProperty("page", String.valueOf(page));

                break;
            case Constant.METHOD_ALBUMS:

                jsObj.addProperty("page", String.valueOf(page));

                break;
            case Constant.METHOD_ARTIST:

                jsObj.addProperty("page", String.valueOf(page));

                break;
            case Constant.METHOD_CAT:

                jsObj.addProperty("page", String.valueOf(page));

                break;
            case Constant.METHOD_SERVER_PLAYLIST:

                jsObj.addProperty("page", String.valueOf(page));

                break;
            case Constant.METHOD_RATINGS:

                jsObj.addProperty("post_id", songID);
                jsObj.addProperty("device_id", deviceID);
                jsObj.addProperty("rate", rate);

                break;
            case Constant.METHOD_LOGIN:

                jsObj.addProperty("email", email);
                jsObj.addProperty("password", password);

                break;
            case Constant.METHOD_REGISTER:

                jsObj.addProperty("name", name);
                jsObj.addProperty("email", email);
                jsObj.addProperty("password", password);
                jsObj.addProperty("phone", phone);

                break;
            case Constant.METHOD_FORGOT_PASSWORD:

                jsObj.addProperty("user_email", email);

                break;
            case Constant.METHOD_PROFILE:

                jsObj.addProperty("user_id", userID);

                break;
            case Constant.METHOD_PROFILE_EDIT:

                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("name", name);
                jsObj.addProperty("email", email);
                jsObj.addProperty("password", password);
                jsObj.addProperty("phone", phone);

                break;
            case Constant.METHOD_REPORT:

                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("song_id", songID);
                jsObj.addProperty("report", reportMessage);

                break;
            case Constant.METHOD_SUGGESTION:

                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("song_title", name);
                jsObj.addProperty("message", reportMessage);

                break;
            case Constant.METHOD_ALBUMS_BY_ARTIST:

                jsObj.addProperty("artist_id", songID);
                jsObj.addProperty("page", page);

                break;
            case Constant.METHOD_DOWNLOAD_COUNT:

                jsObj.addProperty("song_id", songID);

                break;
        }

        if (method.equals(Constant.METHOD_SUGGESTION)) {
            final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");

            return new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(song_image, file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file))
                    .addFormDataPart("data", API.toBase64(jsObj.toString()))
                    .build();
        } else {
            return new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("data", API.toBase64(jsObj.toString()))
                    .build();
        }
    }
}