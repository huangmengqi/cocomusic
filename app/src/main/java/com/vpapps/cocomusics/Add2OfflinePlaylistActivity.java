package com.vpapps.cocomusics;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vpapps.adapter.AdapterMyPlaylist;
import com.vpapps.interfaces.ClickListenerPlayList;
import com.vpapps.item.ItemMyPlayList;
import com.vpapps.utils.DBHelper;
import com.vpapps.utils.Methods;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Add2OfflinePlaylistActivity extends AppCompatActivity {

    Toolbar toolbar;
    Methods methods;
    DBHelper dbHelper;
    RecyclerView rv;
    AdapterMyPlaylist adapterMyPlaylist;
    ArrayList<ItemMyPlayList> arrayList;
    FrameLayout frameLayout;
    LinearLayout ll_local, ll_recent;
    String pid = "";
    Boolean isLoaded = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_2_off_playlist);

        pid = getIntent().getStringExtra("pid");

        dbHelper = new DBHelper(this);
        methods = new Methods(this);
        methods.forceRTLIfSupported(getWindow());
        methods.setStatusColor(getWindow());

        toolbar = this.findViewById(R.id.toolbar_add_2_offplay);
        toolbar.setTitle(getString(R.string.add_songs));
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        arrayList = new ArrayList<>();
        arrayList.addAll(dbHelper.loadPlayList(false));

        frameLayout = findViewById(R.id.fl_empty);
        ll_local = findViewById(R.id.ll_local);
        ll_recent = findViewById(R.id.ll_recent);

        rv = findViewById(R.id.rv_add_2_offplay);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rv.setLayoutManager(gridLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);
        rv.setNestedScrollingEnabled(false);

        ll_local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Add2OfflinePlaylistActivity.this, SelectSongActivity.class);
                intent.putExtra("pid",pid);
                intent.putExtra("type",getString(R.string.songs));
                startActivity(intent);
            }
        });

        ll_recent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Add2OfflinePlaylistActivity.this, SelectSongActivity.class);
                intent.putExtra("pid",pid);
                intent.putExtra("type",getString(R.string.recent));
                startActivity(intent);
            }
        });

        adapterMyPlaylist = new AdapterMyPlaylist(this, arrayList, new ClickListenerPlayList() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(Add2OfflinePlaylistActivity.this, SelectSongActivity.class);
                intent.putExtra("pid",pid);
                intent.putExtra("type",getString(R.string.playlist));
                intent.putExtra("play_id",arrayList.get(position).getId());
                startActivity(intent);
            }

            @Override
            public void onItemZero() {
            }
        }, false);

        rv.setAdapter(adapterMyPlaylist);
        setEmpty();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void setEmpty() {
        if (arrayList.size() > 0) {
            frameLayout.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        } else {
            frameLayout.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);

            frameLayout.removeAllViews();
            LayoutInflater infltr = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View myView = infltr.inflate(R.layout.layout_err_nodata, null);

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(getString(R.string.err_no_playlist_found));

            myView.findViewById(R.id.btn_empty_try).setVisibility(View.GONE);
            frameLayout.addView(myView);
        }
    }

    @Override
    protected void onResume() {
        if(isLoaded) {
            arrayList.clear();
            arrayList.addAll(dbHelper.loadPlayList(false));
            adapterMyPlaylist.notifyDataSetChanged();
        }else {
            isLoaded = true;
        }
        super.onResume();
    }
}