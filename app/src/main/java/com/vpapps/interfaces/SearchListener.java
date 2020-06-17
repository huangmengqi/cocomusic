package com.vpapps.interfaces;

import com.vpapps.item.ItemAlbums;
import com.vpapps.item.ItemArtist;
import com.vpapps.item.ItemSong;

import java.util.ArrayList;

public interface SearchListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemSong> arrayListSong, ArrayList<ItemArtist> arrayListArtist, ArrayList<ItemAlbums> arrayListAlbums);
}
