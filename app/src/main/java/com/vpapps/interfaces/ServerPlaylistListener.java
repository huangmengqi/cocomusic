package com.vpapps.interfaces;

import com.vpapps.item.ItemServerPlayList;

import java.util.ArrayList;

public interface ServerPlaylistListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemServerPlayList> arrayList);
}