package com.vpapps.asyncTask;

import android.os.AsyncTask;

import com.vpapps.interfaces.SearchListener;
import com.vpapps.interfaces.SongListener;
import com.vpapps.item.ItemAlbums;
import com.vpapps.item.ItemArtist;
import com.vpapps.item.ItemSong;
import com.vpapps.utils.Constant;
import com.vpapps.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.RequestBody;

public class LoadSearch extends AsyncTask<String, String, String> {

    private SearchListener searchListener;
    private RequestBody requestBody;
    private ArrayList<ItemSong> arrayListSong = new ArrayList<>();
    private ArrayList<ItemAlbums> arrayListAlbum = new ArrayList<>();
    private ArrayList<ItemArtist> arrayListArtist = new ArrayList<>();

    public LoadSearch(SearchListener searchListener, RequestBody requestBody) {
        this.searchListener = searchListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        searchListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String json = JsonUtils.okhttpPost(Constant.SERVER_URL, requestBody);

            JSONObject mainJson = new JSONObject(json);
            JSONObject jsonObjectRoot = mainJson.getJSONObject(Constant.TAG_ROOT);

            JSONArray jsonArray_albums = jsonObjectRoot.getJSONArray("search_album");
            for (int i = 0; i < jsonArray_albums.length(); i++) {
                JSONObject objJson = jsonArray_albums.getJSONObject(i);

                String id = objJson.getString(Constant.TAG_AID);
                String name = objJson.getString(Constant.TAG_ALBUM_NAME);
                String image = objJson.getString(Constant.TAG_ALBUM_IMAGE);
                String thumb = objJson.getString(Constant.TAG_ALBUM_THUMB);

                ItemAlbums objItem = new ItemAlbums(id, name, image, thumb);
                arrayListAlbum.add(objItem);
            }

            JSONArray jsonArray_artist = jsonObjectRoot.getJSONArray("search_artist");
            for (int i = 0; i < jsonArray_artist.length(); i++) {
                JSONObject objJson = jsonArray_artist.getJSONObject(i);

                String id = objJson.getString(Constant.TAG_ID);
                String name = objJson.getString(Constant.TAG_ARTIST_NAME);
                String image = objJson.getString(Constant.TAG_ARTIST_IMAGE);
                String thumb = objJson.getString(Constant.TAG_ARTIST_THUMB);

                ItemArtist objItem = new ItemArtist(id, name, image, thumb);
                arrayListArtist.add(objItem);
            }

            JSONArray jsonArray_song= jsonObjectRoot.getJSONArray("search_songs");
            for (int i = 0; i < jsonArray_song.length(); i++) {
                JSONObject objJson = jsonArray_song.getJSONObject(i);

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
                arrayListSong.add(objItem);
            }
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        searchListener.onEnd(s, arrayListSong, arrayListArtist, arrayListAlbum);
        super.onPostExecute(s);
    }
}