package com.vpapps.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vpapps.adapter.AdapterAlbums;
import com.vpapps.asyncTask.LoadAlbums;
import com.vpapps.interfaces.AlbumsListener;
import com.vpapps.interfaces.InterAdListener;
import com.vpapps.item.ItemAlbums;
import com.vpapps.item.ItemArtist;
import com.vpapps.cocomusics.R;
import com.vpapps.cocomusics.SongByCatActivity;
import com.vpapps.utils.Constant;
import com.vpapps.utils.EndlessRecyclerViewScrollListener;
import com.vpapps.utils.Methods;
import com.vpapps.utils.RecyclerItemClickListener;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class FragmentAlbumsByArtist extends Fragment {

    private Methods methods;
    private RecyclerView rv;
    private AdapterAlbums adapterAlbums;
    private ArrayList<ItemAlbums> arrayList;
    private ItemArtist itemArtist;
    private CircularProgressBar progressBar;
    private TextView tv_artist;
    private LinearLayout ll_all_songs;

    private FrameLayout frameLayout;
    private String errr_msg;
    private GridLayoutManager glm_banner;
    private int page = 1;
    private Boolean isOver = false, isScroll = false, isLoading = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_albums_by_art, container, false);

        methods = new Methods(getActivity(), new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                Intent intent = new Intent(getActivity(), SongByCatActivity.class);
                switch (type) {
                    case "":
                        intent.putExtra("type", getString(R.string.albums));
                        intent.putExtra("id", adapterAlbums.getItem(position).getId());
                        intent.putExtra("name", adapterAlbums.getItem(position).getName());
                        break;
                    case "all":
                        intent.putExtra("type", getString(R.string.artist));
                        intent.putExtra("id", itemArtist.getId());
                        intent.putExtra("name", itemArtist.getName());
                        break;
                }
                startActivity(intent);
            }
        });

        arrayList = new ArrayList<>();
        itemArtist = (ItemArtist) getArguments().getSerializable("item");

        progressBar = rootView.findViewById(R.id.pb_albums);
        frameLayout = rootView.findViewById(R.id.fl_empty);

        tv_artist = rootView.findViewById(R.id.tv_artist);
        ll_all_songs = rootView.findViewById(R.id.ll_artist_all_songs);
        rv = rootView.findViewById(R.id.rv_albums);
        glm_banner = new GridLayoutManager(getActivity(), 2);
        glm_banner.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapterAlbums.isHeader(position) ? glm_banner.getSpanCount() : 1;
            }
        });

        rv.setNestedScrollingEnabled(false);
        rv.setLayoutManager(glm_banner);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);

        tv_artist.setText(itemArtist.getName());
        ll_all_songs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                methods.showInterAd(0, "all");
            }
        });

        rv.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                methods.showInterAd(position, "");
            }
        }));

        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(glm_banner) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (!isOver) {
                    if (!isLoading) {
                        isLoading = true;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isScroll = true;
                                loadAlbums();
                            }
                        }, 0);
                    }
                }
            }
        });

        loadAlbums();

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            Constant.search_item = s.replace(" ", "%20");
            FragmentSearchAlbums fsearch = new FragmentSearchAlbums();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.hide(getFragmentManager().getFragments().get(getFragmentManager().getBackStackEntryCount()));
            ft.add(R.id.fragment, fsearch, getString(R.string.search_albums));
            ft.addToBackStack(getString(R.string.search_albums));
            ft.commit();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    private void loadAlbums() {
        if (methods.isNetworkAvailable()) {
            LoadAlbums loadAlbums = new LoadAlbums(new AlbumsListener() {
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
                public void onEnd(String success, String verifyStatus, String message, ArrayList<ItemAlbums> arrayListAlbums) {
                    if (getActivity() != null) {
                        if (success.equals("1")) {
                            if (!verifyStatus.equals("-1")) {
                                if (arrayListAlbums.size() == 0) {
                                    isOver = true;
                                    errr_msg = getString(R.string.err_no_albums_found);
                                    try {
                                        adapterAlbums.hideHeader();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    setEmpty();
                                } else {
                                    page = page + 1;
                                    arrayList.addAll(arrayListAlbums);
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
                }
            }, methods.getAPIRequest(Constant.METHOD_ALBUMS_BY_ARTIST, page, "", itemArtist.getId(), "", "", "", "", "", "","","","","","","","", null));
            loadAlbums.execute(String.valueOf(page));
        } else {
            errr_msg = getString(R.string.err_internet_not_conn);
            setEmpty();
        }
    }

    private void setAdapter() {
        if (!isScroll) {
            adapterAlbums = new AdapterAlbums(getActivity(), arrayList, true);
            rv.setAdapter(adapterAlbums);
            setEmpty();
        } else {
            adapterAlbums.notifyDataSetChanged();
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
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View myView = null;
            if (errr_msg.equals(getString(R.string.err_no_albums_found))) {
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
                    loadAlbums();
                }
            });


            frameLayout.addView(myView);
        }
    }
}
