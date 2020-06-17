package com.vpapps.asyncTask;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vpapps.interfaces.ArtistListener;
import com.vpapps.interfaces.CatListener;
import com.vpapps.item.ItemArtist;
import com.vpapps.item.ItemCat;
import com.vpapps.utils.API;
import com.vpapps.utils.Constant;
import com.vpapps.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class LoadArtist extends AsyncTask<String, String, String> {

    private RequestBody requestBody;
    private ArtistListener artistListener;
    private ArrayList<ItemArtist> arrayList = new ArrayList<>();
    private String verifyStatus = "0", message = "";

    public LoadArtist(ArtistListener artistListener, RequestBody requestBody) {
        this.artistListener = artistListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        artistListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String json = JsonUtils.okhttpPost(Constant.SERVER_URL, requestBody);
            JSONObject mainJson = new JSONObject(json);
            JSONArray jsonArray = mainJson.getJSONArray(Constant.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject objJson = jsonArray.getJSONObject(i);

                if (!objJson.has(Constant.TAG_SUCCESS)) {
                    String id = objJson.getString(Constant.TAG_ID);
                    String name = objJson.getString(Constant.TAG_ARTIST_NAME);
                    String image = objJson.getString(Constant.TAG_ARTIST_IMAGE);
                    String thumb = objJson.getString(Constant.TAG_ARTIST_THUMB);

                    ItemArtist objItem = new ItemArtist(id, name, image, thumb);
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
        artistListener.onEnd(s, verifyStatus, message, arrayList);
        super.onPostExecute(s);
    }
}