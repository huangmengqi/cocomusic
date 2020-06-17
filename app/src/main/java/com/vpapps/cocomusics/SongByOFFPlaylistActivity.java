package com.vpapps.cocomusics;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AlertDialog;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.vpapps.adapter.AdapterOFSongList;
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

public class SongByOFFPlaylistActivity extends BaseActivity {

    AppBarLayout appBarLayout;
    CollapsingToolbarLayout collapsingToolbarLayout;
    Toolbar toolbar_playlist;
    Methods methods;
    RecyclerView rv;
    ItemMyPlayList itemMyPlayList;
    AdapterOFSongList adapter;
    ArrayList<ItemSong> arrayList;
    CircularProgressBar progressBar;
    FrameLayout frameLayout;
    ImageView iv_playlist, iv_playlist2;
    TextView tv_no_song;
    Boolean isLoaded = false;
    String addedFrom = "offplay";

    SearchView searchView;
    LinearLayout ll_edit, ll_delete, ll_add_2_playlist, ll_addQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_song_by_offline_playlist, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        itemMyPlayList = (ItemMyPlayList) getIntent().getSerializableExtra("item");
        addedFrom = addedFrom + itemMyPlayList.getName();

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

                Intent intent = new Intent(SongByOFFPlaylistActivity.this, PlayerService.class);
                intent.setAction(PlayerService.ACTION_PLAY);
                startService(intent);
            }
        });
        methods.forceRTLIfSupported(getWindow());

        toolbar.setVisibility(View.GONE);

        appBarLayout = findViewById(R.id.mainappbar);
        toolbar_playlist = findViewById(R.id.toolbar_playlist);
        setSupportActionBar(toolbar_playlist);
//        getSupportActionBar().setTitle(itemMyPlayList.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        collapsingToolbarLayout = findViewById(R.id.collapsing_play);
        collapsingToolbarLayout.setTitle(itemMyPlayList.getName());

        arrayList = new ArrayList<>();

        frameLayout = findViewById(R.id.fl_empty);
        progressBar = findViewById(R.id.pb_song_by_playlist);
        progressBar.setVisibility(View.GONE);

        ll_edit = findViewById(R.id.ll_edit);
        ll_delete = findViewById(R.id.ll_delete);
        ll_add_2_playlist = findViewById(R.id.ll_add_2_offplay);
        ll_addQueue = findViewById(R.id.ll_addQueue);

        rv = findViewById(R.id.rv_song_by_playlist);
        LinearLayoutManager llm_banner = new LinearLayoutManager(this);
        rv.setLayoutManager(llm_banner);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);
        rv.setNestedScrollingEnabled(false);

        iv_playlist = findViewById(R.id.iv_collapse_playlist);
        iv_playlist2 = findViewById(R.id.iv_collapse_playlist2);
        tv_no_song = findViewById(R.id.tv_playlist_no_song);

        Picasso.get()
                .load(methods.getAlbumArtUri(Integer.parseInt(itemMyPlayList.getArrayListUrl().get(3))))
                .placeholder(R.drawable.placeholder_song)
                .into(iv_playlist);
        Picasso.get()
                .load(methods.getAlbumArtUri(Integer.parseInt(itemMyPlayList.getArrayListUrl().get(3))))
                .placeholder(R.drawable.placeholder_song)
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

        ll_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDeleteDialog();
            }
        });

        ll_add_2_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SongByOFFPlaylistActivity.this, Add2OfflinePlaylistActivity.class);
                intent.putExtra("pid", itemMyPlayList.getId());
                startActivity(intent);
            }
        });

        ll_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SongByOFFPlaylistActivity.this, SelectSongActivity.class);
                intent.putExtra("type", getString(R.string.edit));
                intent.putExtra("pid", itemMyPlayList.getId());
                intent.putExtra("array", arrayList);
                startActivity(intent);
            }
        });

        ll_addQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arrayList.size() > 0) {
                    if (Constant.isOnline || Constant.isDownloaded) {
                        showQueueAlert();
                    } else {
                        Constant.arrayList_play.addAll(arrayList);
                        GlobalBus.getBus().postSticky(new ItemMyPlayList("", "", null));
                        Toast.makeText(SongByOFFPlaylistActivity.this, getString(R.string.queue_updated), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SongByOFFPlaylistActivity.this, getString(R.string.no_songs_to_add_queue), Toast.LENGTH_SHORT).show();
                }
            }
        });

        new LoadOfflineSongs().execute();
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

    class LoadOfflineSongs extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            arrayList.clear();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            arrayList = dbHelper.loadDataPlaylist(itemMyPlayList.getId(), false);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            setAdapter();
        }
    }

    private void setAdapter() {
        adapter = new AdapterOFSongList(SongByOFFPlaylistActivity.this, arrayList, new ClickListenerPlayList() {
            @Override
            public void onClick(int position) {
                methods.showInterAd(position, "");
            }

            @Override
            public void onItemZero() {
                setEmpty();
            }
        }, getString(R.string.playlist));
        rv.setAdapter(adapter);
        setEmpty();
    }

    private void openDeleteDialog() {
        AlertDialog.Builder dialog;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog = new AlertDialog.Builder(SongByOFFPlaylistActivity.this, R.style.ThemeDialog);
        } else {
            dialog = new AlertDialog.Builder(SongByOFFPlaylistActivity.this);
        }
        dialog.setTitle(getString(R.string.delete));
        dialog.setMessage(getString(R.string.sure_delete_playlist));
        dialog.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dbHelper.removePlayList(itemMyPlayList.getId(), false);
                finish();
            }
        });
        dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();
    }

    private void showQueueAlert() {
        AlertDialog.Builder dialog;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog = new AlertDialog.Builder(SongByOFFPlaylistActivity.this, R.style.ThemeDialog);
        } else {
            dialog = new AlertDialog.Builder(SongByOFFPlaylistActivity.this);
        }
        dialog.setTitle(getString(R.string.add_to_queue));
        dialog.setMessage(getString(R.string.off_add_qeue_alert));
        dialog.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Constant.arrayList_play.clear();
                Constant.arrayList_play.addAll(arrayList);

                Toast.makeText(SongByOFFPlaylistActivity.this, getString(R.string.queue_updated), Toast.LENGTH_SHORT).show();
                GlobalBus.getBus().postSticky(new ItemMyPlayList("", "", null));
                methods.showInterAd(0, "");
            }
        });
        dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();
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
    protected void onResume() {
        if (isLoaded) {

            new LoadOfflineSongs().execute();

            tv_no_song.setText(arrayList.size() + " " + getString(R.string.songs));
            Picasso.get()
                    .load(methods.getAlbumArtUri(Integer.parseInt(itemMyPlayList.getArrayListUrl().get(3))))
                    .placeholder(R.drawable.placeholder_song)
                    .into(iv_playlist2);
            Picasso.get()
                    .load(methods.getAlbumArtUri(Integer.parseInt(itemMyPlayList.getArrayListUrl().get(3))))
                    .placeholder(R.drawable.placeholder_song)
                    .into(iv_playlist, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            if (arrayList.size() > 0) {
                                Picasso.get()
                                        .load(arrayList.get(arrayList.size() - 1).getImageBig())
                                        .into(iv_playlist);
                                Picasso.get()
                                        .load(arrayList.get(arrayList.size() - 1).getImageBig())
                                        .into(iv_playlist2);
                            } else {
                                iv_playlist.setImageResource(R.drawable.placeholder_song);
                                iv_playlist2.setImageResource(R.drawable.placeholder_song);
                            }
                        }
                    });
        } else {
            isLoaded = true;
        }
        super.onResume();
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