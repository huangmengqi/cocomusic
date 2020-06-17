package com.vpapps.asyncTask;

import android.os.AsyncTask;

import com.vpapps.interfaces.AlbumsListener;
import com.vpapps.item.ItemAlbums;
import com.vpapps.utils.Constant;
import com.vpapps.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.RequestBody;

public class LoadAlbums extends AsyncTask<String, String, String> {

    private RequestBody requestBody;
    private AlbumsListener albumsListener;
    private ArrayList<ItemAlbums> arrayList = new ArrayList<>();
    private String verifyStatus = "0", message = "";

    public LoadAlbums(AlbumsListener albumsListener, RequestBody requestBody) {
        this.albumsListener = albumsListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        albumsListener.onStart();
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
                    String id = objJson.getString(Constant.TAG_AID);
                    String name = objJson.getString(Constant.TAG_ALBUM_NAME);
                    String image = objJson.getString(Constant.TAG_ALBUM_IMAGE);
                    String thumb = objJson.getString(Constant.TAG_ALBUM_THUMB);

                    ItemAlbums objItem = new ItemAlbums(id, name, image, thumb);
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
        albumsListener.onEnd(s, verifyStatus, message, arrayList);
        super.onPostExecute(s);
    }
}