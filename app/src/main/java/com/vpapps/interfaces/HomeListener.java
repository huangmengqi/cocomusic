package com.vpapps.interfaces;

import com.vpapps.item.ItemAlbums;
import com.vpapps.item.ItemArtist;
import com.vpapps.item.ItemHomeBanner;
import com.vpapps.item.ItemSong;

import java.util.ArrayList;

public interface HomeListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemHomeBanner> arrayListBanner, ArrayList<ItemAlbums> arrayListAlbums, ArrayList<ItemArtist> arrayListArtist, ArrayList<ItemSong> arrayListSongs);
}
