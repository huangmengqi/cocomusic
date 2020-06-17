package com.vpapps.cocomusics;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.appbar.AppBarLayout;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import com.vpapps.adapter.AdapterAllSongList;
import com.vpapps.asyncTask.LoadSong;
import com.vpapps.interfaces.ClickListenerPlayList;
import com.vpapps.interfaces.InterAdListener;
import com.vpapps.interfaces.SongListener;
import com.vpapps.item.ItemAlbums;
import com.vpapps.item.ItemMyPlayList;
import com.vpapps.item.ItemServerPlayList;
import com.vpapps.item.ItemSong;
import com.vpapps.utils.Constant;
import com.vpapps.utils.EndlessRecyclerViewScrollListener;
import com.vpapps.utils.GlobalBus;
import com.vpapps.utils.Methods;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class SongByServerPlaylistActivity extends BaseActivity {

    Toolbar toolbar_playlist;
    Methods methods;
    RecyclerView rv;
    ItemServerPlayList itemServerPlayList;
    AdapterAllSongList adapter;
    ArrayList<ItemSong> arrayList;
    CircularProgressBar progressBar;
    String type = "", addedFrom = "serverplay";
    FrameLayout frameLayout;
    ImageView iv_playlist, iv_playlist2;
    TextView tv_no_song;
    int page = 1;
    Boolean isOver = false, isScroll = false, isLoading = false;

    String errr_msg;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_song_by_playlist, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        type = getIntent().getStringExtra("type");
        itemServerPlayList = (ItemServerPlayList) getIntent().getSerializableExtra("item");
        addedFrom = addedFrom + itemServerPlayList.getName();

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

                Intent intent = new Intent(SongByServerPlaylistActivity.this, PlayerService.class);
                intent.setAction(PlayerService.ACTION_PLAY);
                startService(intent);
            }
        });
        methods.forceRTLIfSupported(getWindow());

        toolbar.setVisibility(View.GONE);

        toolbar_playlist = findViewById(R.id.toolbar_playlist);
        setSupportActionBar(toolbar_playlist);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        arrayList = new ArrayList<>();

        frameLayout = findViewById(R.id.fl_empty);
        progressBar = findViewById(R.id.pb_song_by_playlist);
        rv = findViewById(R.id.rv_song_by_playlist);
        LinearLayoutManager llm_banner = new LinearLayoutManager(this);
        rv.setLayoutManager(llm_banner);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);

        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(llm_banner) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (!isOver) {
                    if (!isLoading) {
                        isLoading = true;
                        arrayList.add(null);
                        adapter.notifyItemInserted(arrayList.size());
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

        loadSongs();

        iv_playlist = findViewById(R.id.iv_collapse_playlist);
        iv_playlist2 = findViewById(R.id.iv_collapse_playlist2);
        tv_no_song = findViewById(R.id.tv_playlist_no_song);

        Picasso.get()
                .load(itemServerPlayList.getImage())
                .into(iv_playlist);
        Picasso.get()
                .load(itemServerPlayList.getImage())
                .into(iv_playlist2);

        AppBarLayout appBarLayout = findViewById(R.id.mainappbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                tv_no_song.setAlpha(1 - Math.abs((float) verticalOffset / appBarLayout.getTotalScrollRange()));
                iv_playlist.setAlpha(1 - Math.abs((float) verticalOffset / appBarLayout.getTotalScrollRange()));
                iv_playlist2.setAlpha(1 - Math.abs((float) verticalOffset / appBarLayout.getTotalScrollRange()));
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

    private void loadSongs() {
        if (methods.isNetworkAvailable()) {
            LoadSong loadSong = new LoadSong(new SongListener() {
                @Override
                public void onStart() {
                    if (isScroll) {
                        arrayList.remove(arrayList.size() - 1);
                        adapter.notifyItemRemoved(arrayList.size());
                    }
                    if (arrayList.size() == 0) {
                        arrayList.clear();
                        frameLayout.setVisibility(View.GONE);
                        rv.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onEnd(String success, String verifyStatus, String message, ArrayList<ItemSong> arrayListSong) {
                    if (success.equals("1")) {
                        if (!verifyStatus.equals("-1")) {
                            if (arrayListSong.size() == 0) {
                                isOver = true;
                                errr_msg = getString(R.string.err_no_songs_found);
                                setEmpty();
                            } else {
                                arrayList.addAll(arrayListSong);
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
            },methods.getAPIRequest(Constant.METHOD_SONG_BY_PLAYLIST, page, "", "", "", "", "", "", "", itemServerPlayList.getId(),"","","","","","","", null));
            loadSong.execute();

        } else {
            errr_msg = getString(R.string.err_internet_not_conn);
            setEmpty();
        }
    }

    private void setAdapter() {
        adapter = new AdapterAllSongList(SongByServerPlaylistActivity.this, arrayList, new ClickListenerPlayList() {
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
    }

    public void setEmpty() {
        tv_no_song.setText(arrayList.size() + " " + getString(R.string.songs));
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