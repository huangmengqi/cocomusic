package com.vpapps.utils;

import android.util.Base64;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.vpapps.cocomusics.BuildConfig;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class API {
    @Expose
    @SerializedName("sign")
    private String sign;
    @Expose
    @SerializedName("salt")
    private String salt;

    public API() {
        String apiKey = BuildConfig.API_KEY;
        salt = "" + getRandomSalt();
        sign = md5(apiKey + salt);
    }

    private int getRandomSalt() {
        Random random = new Random();
        return random.nextInt(900);
    }

    public static String md5(String input) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(input.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(String.format("%02x", messageDigest[i]));
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toBase64(String input) {
        byte[] encodeValue = Base64.encode(input.getBytes(), Base64.DEFAULT);
        return new String(encodeValue);
    }
}