package com.vpapps.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vpapps.adapter.AdapterArtist;
import com.vpapps.asyncTask.LoadArtist;
import com.vpapps.interfaces.ArtistListener;
import com.vpapps.interfaces.InterAdListener;
import com.vpapps.item.ItemArtist;
import com.vpapps.cocomusics.MainActivity;
import com.vpapps.cocomusics.R;
import com.vpapps.utils.Constant;
import com.vpapps.utils.EndlessRecyclerViewScrollListener;
import com.vpapps.utils.Methods;
import com.vpapps.utils.RecyclerItemClickListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class FragmentArtist extends Fragment {

    private Methods methods;
    private RecyclerView rv;
    private AdapterArtist adapterArtist;
    private ArrayList<ItemArtist> arrayList;
    private CircularProgressBar progressBar;
    private FrameLayout frameLayout;
    private GridLayoutManager glm_banner;

    private String errr_msg;
    private int page = 1;
    private Boolean isOver = false, isScroll = false, isLoading = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_categories, container, false);

        methods = new Methods(getActivity(), new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                FragmentAlbumsByArtist f_alb = new FragmentAlbumsByArtist();
                Bundle bundle = new Bundle();
                bundle.putSerializable("item", arrayList.get(position));
                f_alb.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(getFragmentManager().getFragments().get(getFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.fragment, f_alb, getString(R.string.albums));
                ft.addToBackStack(getString(R.string.albums));
                ft.commit();
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.albums));
            }
        });

        arrayList = new ArrayList<>();

        progressBar = rootView.findViewById(R.id.pb_cat);
        frameLayout = rootView.findViewById(R.id.fl_empty);

        rv = rootView.findViewById(R.id.rv_cat);
        glm_banner = new GridLayoutManager(getActivity(),3);
        glm_banner.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapterArtist.isHeader(position) ? glm_banner.getSpanCount() : 1;
            }
        });

        rv.setLayoutManager(glm_banner);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);

        rv.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                methods.showInterAd(position,"");
            }
        }));

        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(glm_banner) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (!isOver) {
                    if(!isLoading) {
                        isLoading = true;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isScroll = true;
                                loadArtists();
                            }
                        }, 0);
                    }
                }
            }
        });

        loadArtists();

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
            FragmentSearchArtist fsearch = new FragmentSearchArtist();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.hide(getFragmentManager().getFragments().get(getFragmentManager().getBackStackEntryCount()));
            ft.add(R.id.fragment, fsearch, getString(R.string.search_artist));
            ft.addToBackStack(getString(R.string.search_artist));
            ft.commit();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    private void loadArtists() {
        if (methods.isNetworkAvailable()) {
            LoadArtist loadArtist = new LoadArtist(new ArtistListener() {
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
                public void onEnd(String success, String verifyStatus, String message, ArrayList<ItemArtist> arrayListArtist) {
                    if (getActivity() != null) {
                        if (success.equals("1")) {
                            if (!verifyStatus.equals("-1")) {
                                if (arrayListArtist.size() == 0) {
                                    isOver = true;
                                    errr_msg = getString(R.string.err_no_artist_found);
//                                    try {
//                                        adapterArtist.hideHeader();
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
                                    setEmpty();
                                } else {
                                    page = page + 1;
                                    arrayList.addAll(arrayListArtist);
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
            }, methods.getAPIRequest(Constant.METHOD_ARTIST, page, "", "", "", "", "", "", "", "","","","","","","","", null));
            loadArtist.execute(String.valueOf(page));
        } else {
            errr_msg = getString(R.string.err_internet_not_conn);
            setEmpty();
        }
    }

    private void setAdapter() {
        if (!isScroll) {
            adapterArtist = new AdapterArtist(getActivity(), arrayList);
            rv.setAdapter(adapterArtist);
            setEmpty();
        } else {
            adapterArtist.notifyDataSetChanged();
        }
    }

    public void setEmpty() {
        if(arrayList.size() > 0) {
            rv.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater)   getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View myView = null;
            if(errr_msg.equals(getString(R.string.err_no_artist_found))) {
                myView = inflater.inflate(R.layout.layout_err_nodata, null);
            } else if(errr_msg.equals(getString(R.string.err_internet_not_conn))) {
                myView = inflater.inflate(R.layout.layout_err_internet, null);
            } else if(errr_msg.equals(getString(R.string.err_server))){
                myView = inflater.inflate(R.layout.layout_err_server, null);
            }

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errr_msg);

            myView.findViewById(R.id.btn_empty_try).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadArtists();
                }
            });

            frameLayout.addView(myView);
        }
    }
}