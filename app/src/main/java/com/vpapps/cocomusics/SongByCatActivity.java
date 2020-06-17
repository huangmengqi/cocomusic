package com.vpapps.cocomusics;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.vpapps.adapter.AdapterAllSongList;
import com.vpapps.asyncTask.LoadSong;
import com.vpapps.interfaces.ClickListenerPlayList;
import com.vpapps.interfaces.InterAdListener;
import com.vpapps.interfaces.SongListener;
import com.vpapps.item.ItemAlbums;
import com.vpapps.item.ItemMyPlayList;
import com.vpapps.item.ItemSong;
import com.vpapps.utils.Constant;
import com.vpapps.utils.EndlessRecyclerViewScrollListener;
import com.vpapps.utils.GlobalBus;
import com.vpapps.utils.Methods;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import okhttp3.RequestBody;

public class SongByCatActivity extends BaseActivity {

    Methods methods;
    RecyclerView rv;
    AdapterAllSongList adapter;
    ArrayList<ItemSong> arrayList;
    CircularProgressBar progressBar;
    String id = "", name = "", type = "";
    FrameLayout frameLayout;

    String errr_msg;
    SearchView searchView;
    Boolean isFromPush = false;
    int page = 1;
    String addedFrom = "";
    Boolean isOver = false, isScroll = false, isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_song_by_cat);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_song_by_cat, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        isFromPush = getIntent().getBooleanExtra("isPush", false);
        type = getIntent().getStringExtra("type");
        id = getIntent().getStringExtra("id");
        name = getIntent().getStringExtra("name");

        methods = new Methods(this, new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                Constant.isOnline = true;
                if(!Constant.addedFrom.equals(addedFrom)) {
                    Constant.arrayList_play.clear();
                    Constant.arrayList_play.addAll(arrayList);
                    Constant.addedFrom = addedFrom;
                    Constant.isNewAdded = true;
                }
                Constant.playPos = position;

                Intent intent = new Intent(SongByCatActivity.this, PlayerService.class);
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

        frameLayout = findViewById(R.id.fl_empty);
        progressBar = findViewById(R.id.pb_song_by_cat);
        rv = findViewById(R.id.rv_song_by_cat);
        LinearLayoutManager llm_banner = new LinearLayoutManager(this);
        rv.setLayoutManager(llm_banner);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);

        if (type.equals(getString(R.string.banner))) {
            addedFrom = "banner" + name;
            errr_msg = getString(R.string.err_no_songs_found);
            arrayList = (ArrayList<ItemSong>) getIntent().getSerializableExtra("songs");
            setAdapter();
            progressBar.setVisibility(View.GONE);
        } else {
            arrayList = new ArrayList<>();
            loadSongs();
        }

        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(llm_banner) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (!isOver) {
                    if (!isLoading) {
                        isLoading = true;

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isScroll = true;
                                loadSongs();
                            }
                        }, 0);
                    }
                }
            }
        });
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

    private void loadSongs() {
        if (methods.isNetworkAvailable()) {

            RequestBody requestBody = null;
            if (type.equals(getString(R.string.categories))) {
                addedFrom = "cat"+name;
                requestBody = methods.getAPIRequest(Constant.METHOD_SONG_BY_CAT, page, "", "", "", "", id,"","","","","","","","","","", null);
            } else if (type.equals(getString(R.string.albums))) {
                addedFrom = "albums"+name;
                requestBody = methods.getAPIRequest(Constant.METHOD_SONG_BY_ALBUMS, page, "", "", "", "", "", id,"","","","","","","","","", null);
            } else if (type.equals(getString(R.string.artist))) {
                addedFrom = "artist"+name;
                requestBody = methods.getAPIRequest(Constant.METHOD_SONG_BY_ARTIST, page, "", "", "", "", "", "", name.replace(" ","%20"),"","","","","","","","", null);
            } else if (type.equals(getString(R.string.playlist))) {
                addedFrom = "serverplay"+name;
                requestBody = methods.getAPIRequest(Constant.METHOD_SONG_BY_PLAYLIST, page, "", "", "", "", "", "", "", id,"","","","","","","", null);
            }

            LoadSong loadSong = new LoadSong(new SongListener() {
                @Override
                public void onStart() {

                    if (arrayList.size() == 0) {
                        arrayList.clear();
                        frameLayout.setVisibility(View.GONE);
                        rv.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onEnd(String success, String verifyStatus, String message, ArrayList<ItemSong> arrayListCatBySong) {
                    if (success.equals("1")) {
                        if (!verifyStatus.equals("-1")) {
                            if (arrayListCatBySong.size() == 0) {
                                isOver = true;
                                errr_msg = getString(R.string.err_no_songs_found);
                                setEmpty();
                            } else {
                                arrayList.addAll(arrayListCatBySong);
                                if(isScroll && Constant.addedFrom.equals(addedFrom)) {
                                    Constant.arrayList_play.clear();
                                    Constant.arrayList_play.addAll(arrayList);
                                    try {
                                        GlobalBus.getBus().postSticky(new ItemMyPlayList("", "", null));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                page = page + 1;
                                setAdapter();
                            }
                        } else {
                            methods.getVerifyDialog(getString(R.string.error_unauth_access), message);
                        }
                    } else {
                        errr_msg = getString(R.string.err_server);
                        setEmpty();
                    }
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                }
            }, requestBody);

            loadSong.execute();

        } else {
            errr_msg = getString(R.string.err_internet_not_conn);
            setEmpty();
        }
    }

    private void setAdapter() {
        if (!isScroll) {
            adapter = new AdapterAllSongList(SongByCatActivity.this, arrayList, new ClickListenerPlayList() {
                @Override
                public void onClick(int position) {
                    methods.showInterAd(position, "");
                }

                @Override
                public void onItemZero() {

                }
            }, "online");
            rv.setAdapter(adapter);
            setEmpty();
        } else {
            adapter.notifyDataSetChanged();
        }
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

            View myView = null;
            if (errr_msg.equals(getString(R.string.err_no_songs_found))) {
                myView = inflater.inflate(R.layout.layout_err_nodata, null);
            } else if (errr_msg.equals(getString(R.string.err_internet_not_conn))) {
                myView = inflater.inflate(R.layout.layout_err_internet, null);
            } else if (errr_msg.equals(getString(R.string.err_server))) {
                myView = inflater.inflate(R.layout.layout_err_server, null);
            }

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errr_msg);

            myView.findViewById(R.id.btn_empty_try).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadSongs();
                }
            });
            frameLayout.addView(myView);
        }
    }


    @Override
    public void onBackPressed() {
        if (mLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (dialog_desc != null && dialog_desc.isShowing()) {
            dialog_desc.dismiss();
        } else if (isFromPush) {
            Intent intent = new Intent(SongByCatActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
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