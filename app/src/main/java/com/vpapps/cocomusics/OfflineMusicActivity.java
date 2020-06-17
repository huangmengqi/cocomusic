package com.vpapps.cocomusics;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.vpapps.fragment.FragmentOFAlbums;
import com.vpapps.fragment.FragmentOFArtist;
import com.vpapps.fragment.FragmentOFPlaylist;
import com.vpapps.fragment.FragmentOFSongs;
import com.vpapps.utils.Constant;

public class OfflineMusicActivity extends BaseActivity {


    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_offline_music, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        methods.forceRTLIfSupported(getWindow());

        toolbar.setVisibility(View.GONE);
        Toolbar toolbar_off = findViewById(R.id.toolbar_offline);
        toolbar_off.setTitle(getString(R.string.music_library));
        setSupportActionBar(toolbar_off);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_back);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(5);

        tabLayout = findViewById(R.id.tabs);

        if (checkPer()) {
            initTabs();
//            new LoadOfflineSongs().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class LoadOfflineSongs extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            if (Constant.arrayListOfflineSongs.size() == 0) {
                methods.getListOfflineSongs();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
        }
    }

    private void initTabs() {
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return FragmentOFSongs.newInstance(position);
                case 1:
                    return FragmentOFPlaylist.newInstance(position);
                case 2:
                    return FragmentOFArtist.newInstance(position);
                case 3:
                    return FragmentOFAlbums.newInstance(position);
                default:
                    return FragmentOFAlbums.newInstance(position);
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    @Override
    public void onBackPressed() {
        if (mLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    public Boolean checkPer() {

        if ((ContextCompat.checkSelfPermission(OfflineMusicActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_PHONE_STATE"}, 1);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        boolean canUseExternalStorage = false;

        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canUseExternalStorage = true;
                    initTabs();
//                    new LoadOfflineSongs().execute();
                }

                if (!canUseExternalStorage) {
                    Toast.makeText(OfflineMusicActivity.this, getResources().getString(R.string.err_cannot_use_features), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
