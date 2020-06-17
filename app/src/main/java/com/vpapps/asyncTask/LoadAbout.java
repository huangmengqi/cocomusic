package com.vpapps.asyncTask;

import android.content.Context;
import android.os.AsyncTask;

import com.vpapps.interfaces.AboutListener;
import com.vpapps.item.ItemAbout;
import com.vpapps.utils.Constant;
import com.vpapps.utils.JsonUtils;
import com.vpapps.utils.Methods;

import org.json.JSONArray;
import org.json.JSONObject;

public class LoadAbout extends AsyncTask<String, String, String> {

    private Methods methods;
    private AboutListener aboutListener;
    private String message = "", verifyStatus = "0";

    public LoadAbout(Context context, AboutListener aboutListener) {
        this.aboutListener = aboutListener;
        methods = new Methods(context);
    }

    @Override
    protected void onPreExecute() {
        aboutListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String json = JsonUtils.okhttpPost(Constant.SERVER_URL, methods.getAPIRequest(Constant.METHOD_APP_DETAILS, 0, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", null));
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has(Constant.TAG_ROOT)) {
                JSONArray jsonArray = jsonObject.getJSONArray(Constant.TAG_ROOT);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject c = jsonArray.getJSONObject(i);

                    if (!c.has(Constant.TAG_SUCCESS)) {
                        String appname = c.getString("app_name");
                        String applogo = c.getString("app_logo");
                        String desc = c.getString("app_description");
                        String appversion = c.getString("app_version");
                        String appauthor = c.getString("app_author");
                        String appcontact = c.getString("app_contact");
                        String email = c.getString("app_email");
                        String website = c.getString("app_website");
                        String privacy = c.getString("app_privacy_policy");
                        String developedby = c.getString("app_developed_by");

                        Constant.ad_banner_id = c.getString("banner_ad_id");
                        Constant.ad_inter_id = c.getString("interstital_ad_id");
                        Constant.isBannerAd = Boolean.parseBoolean(c.getString("banner_ad"));
                        Constant.isInterAd = Boolean.parseBoolean(c.getString("interstital_ad"));
                        Constant.ad_publisher_id = c.getString("publisher_id");
                        Constant.adDisplay = Integer.parseInt(c.getString("interstital_ad_click"));
                        Constant.isSongDownload = Boolean.parseBoolean(c.getString("song_download"));
                        Constant.packageName = c.getString("package_name");

                        Constant.itemAbout = new ItemAbout(appname, applogo, desc, appversion, appauthor, appcontact, email, website, privacy, developedby);
                    } else {
                        verifyStatus = c.getString(Constant.TAG_SUCCESS);
                        message = c.getString(Constant.TAG_MSG);
                    }
                }
            }
            return "1";
        } catch (Exception ee) {
            ee.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        aboutListener.onEnd(s, verifyStatus, message);
        super.onPostExecute(s);
    }
}