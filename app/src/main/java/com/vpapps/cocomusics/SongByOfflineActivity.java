package com.vpapps.cocomusics;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.vpapps.adapter.AdapterOFSongList;
import com.vpapps.interfaces.ClickListenerPlayList;
import com.vpapps.interfaces.InterAdListener;
import com.vpapps.item.ItemAlbums;
import com.vpapps.item.ItemSong;
import com.vpapps.utils.Constant;
import com.vpapps.utils.GlobalBus;
import com.vpapps.utils.Methods;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class SongByOfflineActivity extends BaseActivity {

    Methods methods;
    RecyclerView rv;
    AdapterOFSongList adapter;
    ArrayList<ItemSong> arrayList;
    CircularProgressBar progressBar;
    String type = "", id = "", name = "";
    FrameLayout frameLayout;
    String addedFrom = "";

    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_song_by_cat, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        type = getIntent().getStringExtra("type");
        id = getIntent().getStringExtra("id");
        name = getIntent().getStringExtra("name");

        methods = new Methods(this, new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                Constant.isOnline = false;
                if(!Constant.addedFrom.equals(addedFrom)) {
                    Constant.arrayList_play.clear();
                    Constant.arrayList_play.addAll(arrayList);
                    Constant.addedFrom = addedFrom;
                    Constant.isNewAdded = true;
                }
                Constant.playPos = position;

                Intent intent = new Intent(SongByOfflineActivity.this, PlayerService.class);
                intent.setAction(PlayerService.ACTION_PLAY);
                startService(intent);
            }
        });
        methods.forceRTLIfSupported(getWindow());

//        toolbar = findViewById(R.id.toolbar_song_by_cat);
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_back);

        arrayList = new ArrayList<>();

        frameLayout = findViewById(R.id.fl_empty);
        progressBar = findViewById(R.id.pb_song_by_cat);
        rv = findViewById(R.id.rv_song_by_cat);
        LinearLayoutManager llm_banner = new LinearLayoutManager(this);
        rv.setLayoutManager(llm_banner);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);

        new LoadOfflineSongs().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            if (adapter != null) {
                if (!searchView.isIconified()) {
                    adapter.getFilter().filter(s);
                    adapter.notifyDataSetChanged();
                }
            }
            return true;
        }
    };

    class LoadOfflineSongs extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            arrayList.clear();
            frameLayout.setVisibility(View.GONE);
            rv.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            getListOfSongs();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            setAdapter();
            progressBar.setVisibility(View.GONE);
        }
    }

    public void getListOfSongs() {
        if (type.equals(getString(R.string.playlist))) {
            addedFrom = "offplay"+name;
            arrayList = dbHelper.loadDataPlaylist(id, false);
        } else if (type.equals(getString(R.string.albums))) {
            addedFrom = "offalbum"+name;
            String selection = "is_music != 0";

            selection = selection + " and album_id = " + id;

            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ALBUM_ID
            };
            final String sortOrder = MediaStore.Audio.AudioColumns.ALBUM + " COLLATE LOCALIZED ASC";
            Cursor cursor = null;
            try {
                Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                cursor = getContentResolver().query(uri, projection, selection, null, sortOrder);
                if (cursor != null) {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {

                        String id = String.valueOf(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                        long duration_long = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        String image = "";

                        long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                        image = String.valueOf(albumId);

                        String duration = methods.milliSecondsToTimerDownload(duration_long);

                        String desc = getString(R.string.title) + " - " + title + "</br>" + getString(R.string.artist) + " - " + artist;

                        arrayList.add(new ItemSong(id, "", "", artist, url, image, image, title, duration, desc, "0", "0", "0", "0"));

                        cursor.moveToNext();
                    }
                }

            } catch (Exception e) {
                Log.e("Media", e.toString());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (type.equals(getString(R.string.artist))) {
            addedFrom = "offartist"+name;
            String selection = "is_music != 0";

            selection = selection + " and artist_id = " + id;

            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ALBUM_ID
            };
//            final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
            final String sortOrder = MediaStore.Audio.Media.ARTIST + "  ASC";

            Cursor cursor = null;
            try {
                Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                cursor = getContentResolver().query(uri, projection, selection, null, sortOrder);
                if (cursor != null) {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {

                        String id = String.valueOf(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                        long duration_long = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        String image = "";

                        long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                        image = String.valueOf(albumId);

                        String duration = methods.milliSecondsToTimerDownload(duration_long);


                        String desc = getString(R.string.title) + " - " + title + "</br>" + getString(R.string.artist) + " - " + artist;

                        arrayList.add(new ItemSong(id, "", "", artist, url, image, image, title, duration, desc, "0", "0", "0", "0"));

                        cursor.moveToNext();
                    }
                }
            } catch (Exception e) {
                Log.e("Media", e.toString());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    private void setAdapter() {
        adapter = new AdapterOFSongList(SongByOfflineActivity.this, arrayList, new ClickListenerPlayList() {
            @Override
            public void onClick(int position) {
                methods.showInterAd(position, "");
            }

            @Override
            public void onItemZero() {

            }
        }, type);
        rv.setAdapter(adapter);
        setEmpty();
    }

    public void setEmpty() {
        if (arrayList.size() > 0) {
            rv.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View myView = inflater.inflate(R.layout.layout_err_nodata, null);

            myView.findViewById(R.id.btn_empty_try).setVisibility(View.GONE);
            frameLayout.addView(myView);
        }
    }


    @Override
    public void onBackPressed() {
        if (mLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (dialog_desc != null && dialog_desc.isShowing()) {
            dialog_desc.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEquilizerChange(ItemAlbums itemAlbums) {
        adapter.notifyDataSetChanged();
        GlobalBus.getBus().removeStickyEvent(itemAlbums);
    }
}