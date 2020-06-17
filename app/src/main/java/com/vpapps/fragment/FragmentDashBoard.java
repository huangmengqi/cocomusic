package com.vpapps.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.irfaan008.irbottomnavigation.SpaceItem;
import com.irfaan008.irbottomnavigation.SpaceNavigationView;
import com.irfaan008.irbottomnavigation.SpaceOnClickListener;
import com.vpapps.cocomusics.MainActivity;
import com.vpapps.cocomusics.OfflineMusicActivity;
import com.vpapps.cocomusics.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragmentDashBoard extends Fragment {

    static SpaceNavigationView spaceNavigationView;
    private FragmentManager fm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        setHasOptionsMenu(true);

        fm = getFragmentManager();

        spaceNavigationView = rootView.findViewById(R.id.space);
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem(getString(R.string.home), R.mipmap.ic_home_bottom));
        spaceNavigationView.addSpaceItem(new SpaceItem(getString(R.string.recent), R.mipmap.ic_recent));
        spaceNavigationView.addSpaceItem(new SpaceItem(getString(R.string.categories), R.mipmap.ic_categories));
        spaceNavigationView.addSpaceItem(new SpaceItem(getString(R.string.latest), R.mipmap.ic_latest));

        FragmentHome f1 = new FragmentHome();
        loadFrag(f1, getString(R.string.home));

        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                Intent intent = new Intent(getActivity(), OfflineMusicActivity.class);
                startActivity(intent);
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                switch (itemIndex) {
                    case 0:
                        FragmentHome f1 = new FragmentHome();
                        loadFrag(f1, getString(R.string.home));
                        break;
                    case 1:
                        FragmentRecentSongs frecent = new FragmentRecentSongs();
                        loadFrag(frecent, getString(R.string.recently_played));
                        break;
                    case 2:
                        FragmentCategories fcat = new FragmentCategories();
                        loadFrag(fcat, getString(R.string.categories));
                        break;
                    case 3:
                        FragmentLatest flatest = new FragmentLatest();
                        loadFrag(flatest, getString(R.string.latest));
                        break;
                }
            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {

            }
        });

        return rootView;
    }

    public void loadFrag(Fragment f1, String name) {
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (name.equals(getString(R.string.search))) {
            ft.hide(fm.getFragments().get(fm.getBackStackEntryCount()));
            ft.add(R.id.fragment_dash, f1, name);
            ft.addToBackStack(name);
        } else {
            ft.replace(R.id.fragment_dash, f1, name);
        }
        ft.commit();

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(name);
    }
}