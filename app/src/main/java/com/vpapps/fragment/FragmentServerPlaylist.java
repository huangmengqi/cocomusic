package com.vpapps.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.vpapps.adapter.AdapterServerPlaylist;
import com.vpapps.asyncTask.LoadServerPlaylist;
import com.vpapps.interfaces.InterAdListener;
import com.vpapps.interfaces.ServerPlaylistListener;
import com.vpapps.item.ItemServerPlayList;
import com.vpapps.cocomusics.R;
import com.vpapps.cocomusics.SongByServerPlaylistActivity;
import com.vpapps.utils.Constant;
import com.vpapps.utils.EndlessRecyclerViewScrollListener;
import com.vpapps.utils.Methods;
import com.vpapps.utils.RecyclerItemClickListener;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class FragmentServerPlaylist extends Fragment {

    private Methods methods;
    private RecyclerView rv;
    private AdapterServerPlaylist adapter;
    private ArrayList<ItemServerPlayList> arrayList;
    private CircularProgressBar progressBar;
    private FrameLayout frameLayout;
    private GridLayoutManager glm_banner;

    private String errr_msg;
    private SearchView searchView;
    private int page = 1;
    private Boolean isOver = false, isScroll = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_albums, container, false);

        methods = new Methods(getActivity(), new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                Intent intent = new Intent(getActivity(), SongByServerPlaylistActivity.class);
                intent.putExtra("type", getString(R.string.playlist));
                intent.putExtra("item", adapter.getItem(position));
                startActivity(intent);
            }
        });

        arrayList = new ArrayList<>();

        frameLayout = rootView.findViewById(R.id.fl_empty);
        progressBar = rootView.findViewById(R.id.pb_albums);
        rv = rootView.findViewById(R.id.rv_albums);
        glm_banner = new GridLayoutManager(getActivity(), 2);
        glm_banner.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isHeader(position) ? glm_banner.getSpanCount() : 1;
            }
        });

        rv.setLayoutManager(glm_banner);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);

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
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isScroll = true;
                            loadPlaylist();
                        }
                    }, 0);
                } else {
                    adapter.hideHeader();
                }
            }
        });

        loadPlaylist();

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
            if (adapter != null) {
                if (!searchView.isIconified()) {
                    adapter.getFilter().filter(s);
                    adapter.notifyDataSetChanged();
                }
            }
            return true;
        }
    };

    private void loadPlaylist() {
        if (methods.isNetworkAvailable()) {
            LoadServerPlaylist loadServerPlaylist = new LoadServerPlaylist(new ServerPlaylistListener() {
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
                public void onEnd(String success, String verifyStatus, String message, ArrayList<ItemServerPlayList> arrayListPlaylist) {
                    if (getActivity() != null) {
                        if (success.equals("1")) {
                            if (!verifyStatus.equals("-1")) {
                                if (arrayListPlaylist.size() == 0) {
                                    isOver = true;
                                    errr_msg = getString(R.string.err_no_playlist_found);
                                    try {
                                        adapter.hideHeader();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    setEmpty();
                                } else {
                                    page = page + 1;
                                    arrayList.addAll(arrayListPlaylist);
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
                    }
                }
            }, methods.getAPIRequest(Constant.METHOD_SERVER_PLAYLIST, page, "", "", "", "", "", "", "", "", "","","","","","","", null));
            loadServerPlaylist.execute(String.valueOf(page));
        } else {
            errr_msg = getString(R.string.err_internet_not_conn);
            setEmpty();
        }
    }

    private void setAdapter() {
        if (!isScroll) {
            adapter = new AdapterServerPlaylist(getActivity(), arrayList);
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
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View myView = null;
            if (errr_msg.equals(getString(R.string.err_no_playlist_found))) {
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
                    loadPlaylist();
                }
            });


            frameLayout.addView(myView);
        }
    }
}