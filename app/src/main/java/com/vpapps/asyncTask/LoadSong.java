package com.vpapps.asyncTask;

import android.os.AsyncTask;

import com.vpapps.interfaces.SongListener;
import com.vpapps.item.ItemSong;
import com.vpapps.utils.Constant;
import com.vpapps.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.RequestBody;

public class LoadSong extends AsyncTask<String, String, String> {

    private SongListener songListener;
    private RequestBody requestBody;
    private ArrayList<ItemSong> arrayList = new ArrayList<>();
    private String verifyStatus = "0", message = "";

    public LoadSong(SongListener songListener, RequestBody requestBody) {
        this.songListener = songListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        songListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String json = JsonUtils.okhttpPost(Constant.SERVER_URL, requestBody);

            JSONObject mainJson = new JSONObject(json);
            JSONArray jsonArray = mainJson.getJSONArray(Constant.TAG_ROOT);

            if (jsonArray.length() > 0 && jsonArray.getJSONObject(0).has("songs_list")) {
                jsonArray = jsonArray.getJSONObject(0).getJSONArray("songs_list");
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject objJson = jsonArray.getJSONObject(i);

                if (!objJson.has(Constant.TAG_SUCCESS)) {
                    String id = objJson.getString(Constant.TAG_ID);
                    String cid = objJson.getString(Constant.TAG_CAT_ID);
                    String cname = objJson.getString(Constant.TAG_CAT_NAME);
                    String artist = objJson.getString(Constant.TAG_ARTIST);
                    String name = objJson.getString(Constant.TAG_SONG_NAME);
                    String url = objJson.getString(Constant.TAG_MP3_URL);
                    String desc = objJson.getString(Constant.TAG_DESC);
                    String duration = objJson.getString(Constant.TAG_DURATION);
                    String thumb = objJson.getString(Constant.TAG_THUMB_B).replace(" ", "%20");
                    String thumb_small = objJson.getString(Constant.TAG_THUMB_S).replace(" ", "%20");
                    String total_rate = objJson.getString(Constant.TAG_TOTAL_RATE);
                    String avg_rate = objJson.getString(Constant.TAG_AVG_RATE);
                    String views = objJson.getString(Constant.TAG_VIEWS);
                    String downloads = objJson.getString(Constant.TAG_DOWNLOADS);

                    ItemSong objItem = new ItemSong(id, cid, cname, artist, url, thumb, thumb_small, name, duration, desc, total_rate, avg_rate, views, downloads);
                    arrayList.add(objItem);
                } else {
                    verifyStatus = objJson.getString(Constant.TAG_SUCCESS);
                    message = objJson.getString(Constant.TAG_MSG);
                }
            }
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        songListener.onEnd(s, verifyStatus, message, arrayList);
        super.onPostExecute(s);
    }
}