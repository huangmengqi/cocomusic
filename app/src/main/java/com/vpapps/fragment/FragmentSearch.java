package com.vpapps.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vpapps.adapter.AdapterAlbumsHome;
import com.vpapps.adapter.AdapterArtistHome;
import com.vpapps.adapter.AdapterRecent;
import com.vpapps.asyncTask.LoadSearch;
import com.vpapps.interfaces.ClickListenerPlayList;
import com.vpapps.interfaces.InterAdListener;
import com.vpapps.interfaces.SearchListener;
import com.vpapps.item.ItemAlbums;
import com.vpapps.item.ItemArtist;
import com.vpapps.item.ItemSong;
import com.vpapps.cocomusics.MainActivity;
import com.vpapps.cocomusics.PlayerService;
import com.vpapps.cocomusics.R;
import com.vpapps.cocomusics.SongByCatActivity;
import com.vpapps.utils.Constant;
import com.vpapps.utils.Methods;
import com.vpapps.utils.RecyclerItemClickListener;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class FragmentSearch extends Fragment {


    private Methods methods;
    private RecyclerView rv_songs, rv_artist, rv_albums;
    private AdapterRecent adapterSongs;
    private AdapterArtistHome adapterArtistHome;
    private AdapterAlbumsHome adapterAlbumsHome;
    private ArrayList<ItemSong> arrayList_songs;
    private ArrayList<ItemArtist> arrayList_artist;
    private ArrayList<ItemAlbums> arrayList_album;
    private LinearLayout ll_songs, ll_artist, ll_albums;
    private TextView tv_songs_all, tv_albums_all, tv_artist_all, tv_artist_tot, tv_album_tot, tv_song_tot;
    private LinearLayout ll_search;
    private CircularProgressBar progressBar;
    private FrameLayout frameLayout;

    private String errr_msg;
    private SearchView searchView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_search, container, false);

        methods = new Methods(getActivity(), new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                Intent intent = new Intent(getActivity(), SongByCatActivity.class);

                if(type.equals(getString(R.string.artist))) {
                    intent.putExtra("type", getString(R.string.artist));
                    intent.putExtra("id", arrayList_artist.get(position).getId());
                    intent.putExtra("name", arrayList_artist.get(position).getName());
                } else if(type.equals(getString(R.string.albums))) {
                    intent.putExtra("type", getString(R.string.albums));
                    intent.putExtra("id", arrayList_album.get(position).getId());
                    intent.putExtra("name", arrayList_album.get(position).getName());
                }
                startActivity(intent);
            }
        });
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.search));

        arrayList_songs = new ArrayList<>();
        arrayList_artist = new ArrayList<>();
        arrayList_album = new ArrayList<>();

        LinearLayout ll_adView = rootView.findViewById(R.id.ll_adView);
        LinearLayout ll_adView2 = rootView.findViewById(R.id.ll_adView2);
        methods.showBannerAd(ll_adView);
        methods.showBannerAd(ll_adView2);

        frameLayout = rootView.findViewById(R.id.fl_empty);
        progressBar = rootView.findViewById(R.id.pb_search);

        rv_artist = rootView.findViewById(R.id.rv_search_artist);
        LinearLayoutManager llm_artist = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rv_artist.setLayoutManager(llm_artist);
        rv_artist.setItemAnimator(new DefaultItemAnimator());
        rv_artist.setHasFixedSize(true);

        rv_albums = rootView.findViewById(R.id.rv_search_albums);
        LinearLayoutManager llm_albums = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rv_albums.setLayoutManager(llm_albums);
        rv_albums.setItemAnimator(new DefaultItemAnimator());
        rv_albums.setHasFixedSize(true);

        rv_songs = rootView.findViewById(R.id.rv_search_songs);
        LinearLayoutManager llm_songs = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rv_songs.setLayoutManager(llm_songs);
        rv_songs.setItemAnimator(new DefaultItemAnimator());
        rv_songs.setHasFixedSize(true);

        ll_search = rootView.findViewById(R.id.ll_search);

        ll_songs = rootView.findViewById(R.id.ll_search_song);
        ll_artist = rootView.findViewById(R.id.ll_search_artist);
        ll_albums = rootView.findViewById(R.id.ll_search_albums);

        tv_artist_tot = rootView.findViewById(R.id.tv_search_artist_tot);
        tv_album_tot = rootView.findViewById(R.id.tv_search_albums_tot);
        tv_song_tot = rootView.findViewById(R.id.tv_search_songs_tot);
        tv_artist_all = rootView.findViewById(R.id.tv_search_artist);
        tv_songs_all = rootView.findViewById(R.id.tv_search_songs_all);
        tv_albums_all = rootView.findViewById(R.id.tv_search_albums);


        rv_artist.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                methods.showInterAd(position, getString(R.string.artist));
            }
        }));

        rv_albums.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                methods.showInterAd(position, getString(R.string.albums));
            }
        }));

        tv_songs_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentSearchSong fsearch = new FragmentSearchSong();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(getFragmentManager().getFragments().get(getFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.fragment, fsearch, getString(R.string.search_songs));
                ft.addToBackStack(getString(R.string.search_songs));
                ft.commit();
            }
        });

        tv_artist_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentSearchArtist fartist = new FragmentSearchArtist();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(getFragmentManager().getFragments().get(getFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.fragment, fartist, getString(R.string.search_artist));
                ft.addToBackStack(getString(R.string.search_artist));
                ft.commit();
            }
        });

        tv_albums_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentSearchAlbums falbums = new FragmentSearchAlbums();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(getFragmentManager().getFragments().get(getFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.fragment, falbums, getString(R.string.search_albums));
                ft.addToBackStack(getString(R.string.search_albums));
                ft.commit();
            }
        });

        loadSongs();

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem item = menu.findItem(R.id.menu_search);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            if (methods.isNetworkAvailable()) {
                Constant.search_item = s.replace(" ", "%20");
                arrayList_songs.clear();
                arrayList_artist.clear();
                arrayList_album.clear();
                if (adapterSongs != null) {
                    adapterSongs.notifyDataSetChanged();
                }
                if (adapterAlbumsHome != null) {
                    adapterAlbumsHome.notifyDataSetChanged();
                }
                if (adapterArtistHome != null) {
                    adapterArtistHome.notifyDataSetChanged();
                }
                loadSongs();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.err_internet_not_conn), Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    private void loadSongs() {
        if (methods.isNetworkAvailable()) {
            LoadSearch loadSong = new LoadSearch(new SearchListener() {
                @Override
                public void onStart() {
                    arrayList_songs.clear();
                    frameLayout.setVisibility(View.GONE);
                    ll_search.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onEnd(String success, ArrayList<ItemSong> arrayListSong, ArrayList<ItemArtist> arrayListArtist, ArrayList<ItemAlbums> arrayListAlbums) {
                    if (getActivity() != null) {

                        if (success.equals("1")) {
                            errr_msg = getString(R.string.err_no_data_found);
                            arrayList_songs.addAll(arrayListSong);
                            arrayList_artist.addAll(arrayListArtist);
                            arrayList_album.addAll(arrayListAlbums);

                            tv_album_tot.setText("(" + arrayList_album.size() + ")");
                            tv_artist_tot.setText("(" + arrayList_artist.size() + ")");
                            tv_song_tot.setText("(" + arrayList_songs.size() + ")");

                            setAdapter();
                        }
                    } else {
                        errr_msg = getString(R.string.err_server);
                        setEmpty();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }, methods.getAPIRequest(Constant.METHOD_SEARCH, 1, "", "", Constant.search_item, "", "", "", "", "", "", "", "", "", "", "", "", null));
            loadSong.execute();
        } else {
            errr_msg = getString(R.string.err_internet_not_conn);
            setEmpty();
        }
    }

    private void setAdapter() {
        adapterSongs = new AdapterRecent(getActivity(), arrayList_songs, new ClickListenerPlayList() {
            @Override
            public void onClick(int position) {
                Constant.isOnline = true;
//                if (!Constant.addedFrom.equals(addedFrom)) {
                    Constant.arrayList_play.clear();
                    Constant.arrayList_play.addAll(arrayList_songs);
//                    Constant.addedFrom = addedFrom;
                    Constant.isNewAdded = true;
//                }
                Constant.playPos = position;

                Intent intent = new Intent(getActivity(), PlayerService.class);
                intent.setAction(PlayerService.ACTION_PLAY);
                getActivity().startService(intent);
            }

            @Override
            public void onItemZero() {

            }
        });
        rv_songs.setAdapter(adapterSongs);

        adapterArtistHome = new AdapterArtistHome(arrayList_artist);
        rv_artist.setAdapter(adapterArtistHome);

        adapterAlbumsHome = new AdapterAlbumsHome(arrayList_album);
        rv_albums.setAdapter(adapterAlbumsHome);

        setEmpty();
    }

    public void setEmpty() {
        if (arrayList_songs.size() > 0 || arrayList_album.size() > 0 || arrayList_artist.size() > 0) {
            ll_search.setVisibility(View.VISIBLE);
            if (arrayList_songs.size() > 0) {
                ll_songs.setVisibility(View.VISIBLE);
            } else {
                ll_songs.setVisibility(View.GONE);
            }

            if (arrayList_artist.size() > 0) {
                ll_artist.setVisibility(View.VISIBLE);
            } else {
                ll_artist.setVisibility(View.GONE);
            }

            if (arrayList_album.size() > 0) {
                ll_albums.setVisibility(View.VISIBLE);
            } else {
                ll_albums.setVisibility(View.GONE);
            }
        } else {
            frameLayout.setVisibility(View.VISIBLE);
            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View myView = null;
            if (errr_msg.equals(getString(R.string.err_no_data_found))) {
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
}