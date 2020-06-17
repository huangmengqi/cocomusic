package com.vpapps.cocomusics;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.vpapps.interfaces.ClickListenerPlayList;
import com.vpapps.interfaces.InterAdListener;
import com.vpapps.item.ItemAlbums;
import com.vpapps.item.ItemMyPlayList;
import com.vpapps.item.ItemSong;
import com.vpapps.utils.Constant;
import com.vpapps.utils.GlobalBus;
import com.vpapps.utils.Methods;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class SongByMyPlaylistActivity extends BaseActivity {

    AppBarLayout appBarLayout;
    Toolbar toolbar_playlist;
    Methods methods;
    RecyclerView rv;
    ItemMyPlayList itemMyPlayList;
    AdapterAllSongList adapter;
    ArrayList<ItemSong> arrayList;
    CircularProgressBar progressBar;
    FrameLayout frameLayout;
    ImageView iv_playlist, iv_playlist2;
    TextView tv_no_song;
    String addedFrom = "myplay";

    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_song_by_playlist, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        itemMyPlayList = (ItemMyPlayList) getIntent().getSerializableExtra("item");
        addedFrom = addedFrom + itemMyPlayList.getName();

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

                Intent intent = new Intent(SongByMyPlaylistActivity.this, PlayerService.class);
                intent.setAction(PlayerService.ACTION_PLAY);
                startService(intent);
            }
        });
        methods.forceRTLIfSupported(getWindow());

        toolbar.setVisibility(View.GONE);

        appBarLayout = findViewById(R.id.mainappbar);
        toolbar_playlist = findViewById(R.id.toolbar_playlist);
        setSupportActionBar(toolbar_playlist);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        arrayList = new ArrayList<>();

        frameLayout = findViewById(R.id.fl_empty);
        progressBar = findViewById(R.id.pb_song_by_playlist);
        progressBar.setVisibility(View.GONE);
        rv = findViewById(R.id.rv_song_by_playlist);
        LinearLayoutManager llm_banner = new LinearLayoutManager(this);
        rv.setLayoutManager(llm_banner);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);

        arrayList = dbHelper.loadDataPlaylist(itemMyPlayList.getId(), true);

        iv_playlist = findViewById(R.id.iv_collapse_playlist);
        iv_playlist2 = findViewById(R.id.iv_collapse_playlist2);
        tv_no_song = findViewById(R.id.tv_playlist_no_song);

        Picasso.get()
                .load(itemMyPlayList.getArrayListUrl().get(3))
                .into(iv_playlist);
        Picasso.get()
                .load(itemMyPlayList.getArrayListUrl().get(3))
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

        setAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appBarLayout.setExpanded(false);
            }
        });
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

    private void setAdapter() {
        adapter = new AdapterAllSongList(SongByMyPlaylistActivity.this, arrayList, new ClickListenerPlayList() {
            @Override
            public void onClick(int position) {
                methods.showInterAd(position, "");
            }

            @Override
            public void onItemZero() {
                setEmpty();
            }
        }, "playlist");
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