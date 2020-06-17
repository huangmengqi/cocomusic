package com.vpapps.interfaces;

public interface LoginListener {
    void onStart();
    void onEnd(String success, String loginSuccess, String message, String user_id, String user_name);
}