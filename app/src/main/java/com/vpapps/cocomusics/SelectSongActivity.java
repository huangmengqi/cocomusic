package com.vpapps.cocomusics;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vpapps.adapter.AdapterSelectableSongList;
import com.vpapps.interfaces.ClickListenerPlayList;
import com.vpapps.item.ItemSong;
import com.vpapps.utils.Constant;
import com.vpapps.utils.DBHelper;
import com.vpapps.utils.Methods;

import java.util.ArrayList;

public class SelectSongActivity extends AppCompatActivity {

    Toolbar toolbar;
    Methods methods;
    DBHelper dbHelper;
    RecyclerView rv;
    ArrayList<ItemSong> arrayList;
    FrameLayout frameLayout;
    AdapterSelectableSongList adapter;
    CheckBox checkBox_menu;
    TextView tv_select_add;
    String pid = "", type = "", play_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_song);

        pid = getIntent().getStringExtra("pid");
        type = getIntent().getStringExtra("type");
        if (type.equals(getString(R.string.playlist))) {
            play_id = getIntent().getStringExtra("play_id");
        }

        dbHelper = new DBHelper(this);
        methods = new Methods(this);
        methods = new Methods(this);
        methods.forceRTLIfSupported(getWindow());
        methods.setStatusColor(getWindow());

        toolbar = this.findViewById(R.id.toolbar_select);
        toolbar.setTitle("0 " + getString(R.string.selected));
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tv_select_add = findViewById(R.id.tv_select_add);
        tv_select_add.setTypeface(tv_select_add.getTypeface(), Typeface.BOLD);
        frameLayout = findViewById(R.id.fl_empty);

        arrayList = new ArrayList<>();
        if (type.equals(getString(R.string.recent))) {
            arrayList.addAll(dbHelper.loadDataRecent(false, Constant.recentLimit));
        } else if (type.equals(getString(R.string.playlist))) {
            arrayList.addAll(dbHelper.loadDataPlaylist(play_id, false));
        } else if (type.equals(getString(R.string.edit))) {
            arrayList.addAll((ArrayList<ItemSong>) getIntent().getSerializableExtra("array"));
            tv_select_add.setText(getString(R.string.remove));
        } else {
            arrayList.addAll(Constant.arrayListOfflineSongs);
        }

        rv = findViewById(R.id.rv_select);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);
        rv.setNestedScrollingEnabled(false);

        adapter = new AdapterSelectableSongList(SelectSongActivity.this, arrayList, new ClickListenerPlayList() {
            @Override
            public void onClick(int position) {
                getSupportActionBar().setTitle(adapter.getSelectedCounts() + " " + getString(R.string.selected));
            }

            @Override
            public void onItemZero() {

            }
        });

        tv_select_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ItemSong> checkedArray = adapter.getSelectedIDs();
                if (checkedArray.size() > 0) {
                    for (int i = 0; i < checkedArray.size(); i++) {
                        if (!type.equals(getString(R.string.edit))) {
                            dbHelper.addToPlayList(checkedArray.get(i), pid, false);
                        } else {
                            dbHelper.removeFromPlayList(checkedArray.get(i).getId(), false);
                        }
                    }
//                    Toast.makeText(SelectSongActivity.this, getString(R.string.add_to_playlist), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SelectSongActivity.this, getString(R.string.select_song), Toast.LENGTH_SHORT).show();
                }
            }
        });

        rv.setAdapter(adapter);
        setEmpty();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_checkbox, menu);
        checkBox_menu = (CheckBox) menu.findItem(R.id.menu_cb).getActionView();
        checkBox_menu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                adapter.toggleSelectAll(isChecked);
                getSupportActionBar().setTitle(adapter.getSelectedCounts() + " " + getString(R.string.selected));
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
            textView.setText(getString(R.string.err_no_songs_found));

            myView.findViewById(R.id.btn_empty_try).setVisibility(View.GONE);
            frameLayout.addView(myView);
        }
    }
}