package com.vpapps.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vpapps.adapter.AdapterArtist;
import com.vpapps.interfaces.InterAdListener;
import com.vpapps.item.ItemArtist;
import com.vpapps.cocomusics.R;
import com.vpapps.cocomusics.SongByOfflineActivity;
import com.vpapps.utils.Constant;
import com.vpapps.utils.Methods;
import com.vpapps.utils.RecyclerItemClickListener;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class FragmentOFArtist extends Fragment {

    private Methods methods;
    private RecyclerView rv;
    private AdapterArtist adapterArtist;
    private CircularProgressBar progressBar;

    private FrameLayout frameLayout;
    private String errr_msg = "";
    private SearchView searchView;
    private Boolean isLoaded = false;

    public static FragmentOFArtist newInstance(int sectionNumber) {
        return new FragmentOFArtist();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_categories, container, false);

        methods = new Methods(getActivity(), new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                Intent intent = new Intent(getActivity(), SongByOfflineActivity.class);
                intent.putExtra("type", getString(R.string.artist));
                intent.putExtra("id", adapterArtist.getItem(position).getId());
                intent.putExtra("name", adapterArtist.getItem(position).getName());
                startActivity(intent);
            }
        });
        errr_msg = getString(R.string.err_no_artist_found);

        progressBar = rootView.findViewById(R.id.pb_cat);
        frameLayout = rootView.findViewById(R.id.fl_empty);

        rv = rootView.findViewById(R.id.rv_cat);
        GridLayoutManager glm_banner = new GridLayoutManager(getActivity(), 3);
        rv.setLayoutManager(glm_banner);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);

        rv.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                methods.showInterAd(position, "");
            }
        }));

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            if (adapterArtist != null) {
                if (!searchView.isIconified()) {
                    adapterArtist.getFilter().filter(s);
                    adapterArtist.notifyDataSetChanged();
                }
            }
            return true;
        }
    };

    class LoadOfflineArtist extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
//            Constant.arrayListOfflineArtist.clear();
            frameLayout.setVisibility(View.GONE);
            rv.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            if (Constant.arrayListOfflineArtist.size() == 0) {
                getListOfArtist();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (getActivity() != null) {
                setAdapter();
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private void getListOfArtist() {
        try {
            String where = null;

            final Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
            final String id = MediaStore.Audio.Artists._ID;
            final String artist_name = MediaStore.Audio.Artists.ARTIST;

            final String[] columns = {id, artist_name};
            Cursor cursor = getActivity().getContentResolver().query(uri, columns, where, null, null);

            // add playlsit to list

            if (cursor != null && cursor.moveToFirst()) {

                do {

                    String aid = String.valueOf(cursor.getLong(cursor.getColumnIndex(id)));
                    String name = cursor.getString(cursor.getColumnIndex(artist_name));
                    String image = "null";

                    Constant.arrayListOfflineArtist.add(new ItemArtist(aid, name, image, image));

                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAdapter() {
        if (adapterArtist == null) {
            adapterArtist = new AdapterArtist(getActivity(), Constant.arrayListOfflineArtist);
            rv.setAdapter(adapterArtist);
            isLoaded = true;
        }
        setEmpty();
    }

    public void setEmpty() {
        if (Constant.arrayListOfflineArtist.size() > 0) {
            rv.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View myView = inflater.inflate(R.layout.layout_err_nodata, null);

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errr_msg);
            myView.findViewById(R.id.btn_empty_try).setVisibility(View.GONE);


            frameLayout.addView(myView);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser && rv != null) {
            if (!isLoaded) {
                new LoadOfflineArtist().execute();
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }
}