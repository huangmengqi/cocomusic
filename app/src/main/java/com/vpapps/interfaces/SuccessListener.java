package com.vpapps.interfaces;

public interface SuccessListener {
    void onStart();
    void onEnd(String success, String registerSuccess, String message);
}