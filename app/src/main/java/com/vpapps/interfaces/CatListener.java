package com.vpapps.interfaces;

import com.vpapps.item.ItemCat;

import java.util.ArrayList;

public interface CatListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemCat> arrayList);
}
