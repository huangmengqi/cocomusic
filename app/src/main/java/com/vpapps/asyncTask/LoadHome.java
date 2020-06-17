package com.vpapps.asyncTask;

import android.os.AsyncTask;

import com.vpapps.interfaces.HomeListener;
import com.vpapps.item.ItemAlbums;
import com.vpapps.item.ItemArtist;
import com.vpapps.item.ItemHomeBanner;
import com.vpapps.item.ItemSong;
import com.vpapps.utils.Constant;
import com.vpapps.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.RequestBody;

public class LoadHome extends AsyncTask<String, String, String> {

    private RequestBody requestBody;
    private HomeListener homeListener;
    private ArrayList<ItemHomeBanner> arrayListBanner = new ArrayList<>();
    private ArrayList<ItemAlbums> arrayListAlbums = new ArrayList<>();
    private ArrayList<ItemArtist> arrayListArtist = new ArrayList<>();
    private ArrayList<ItemSong> arrayListSongs = new ArrayList<>();

    public LoadHome(HomeListener homeListener, RequestBody requestBody) {
        this.homeListener = homeListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        homeListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {

            String json = JsonUtils.okhttpPost(Constant.SERVER_URL, requestBody);

            JSONObject mainJson = new JSONObject(json);
            JSONObject jsonObject = mainJson.getJSONObject(Constant.TAG_ROOT);

            JSONArray jsonArrayBanner = jsonObject.getJSONArray("home_banner");

            for (int i = 0; i < jsonArrayBanner.length(); i++) {
                JSONObject objJsonBanner = jsonArrayBanner.getJSONObject(i);

                String banner_id = objJsonBanner.getString(Constant.TAG_BID);
                String banner_title = objJsonBanner.getString(Constant.TAG_BANNER_TITLE);
                String banner_desc = objJsonBanner.getString(Constant.TAG_BANNER_DESC);
                String banner_image = objJsonBanner.getString(Constant.TAG_BANNER_IMAGE);
                String banner_total = objJsonBanner.getString(Constant.TAG_BANNER_TOTAL);

                JSONArray jABannerSongs = objJsonBanner.getJSONArray("songs_list");
                ArrayList<ItemSong> arrayListBannerSongs = new ArrayList<>();
                for (int j = 0; j < jABannerSongs.length(); j++) {
                    JSONObject objJson = jABannerSongs.getJSONObject(j);

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
                    arrayListBannerSongs.add(objItem);
                }

                arrayListBanner.add(new ItemHomeBanner(banner_id, banner_title, banner_image, banner_desc, banner_total, arrayListBannerSongs));
            }

            JSONArray jsonArrayArtist = jsonObject.getJSONArray("latest_artist");
            for (int i = 0; i < jsonArrayArtist.length(); i++) {
                JSONObject objJson = jsonArrayArtist.getJSONObject(i);

                String id = objJson.getString(Constant.TAG_ID);
                String name = objJson.getString(Constant.TAG_ARTIST_NAME);
                String image = objJson.getString(Constant.TAG_ARTIST_IMAGE);
                String thumb = objJson.getString(Constant.TAG_ARTIST_THUMB);

                ItemArtist objItem = new ItemArtist(id, name, image, thumb);
                arrayListArtist.add(objItem);
            }

            JSONArray jsonArrayAlbums = jsonObject.getJSONArray("latest_album");
            for (int i = 0; i < jsonArrayAlbums.length(); i++) {
                JSONObject objJson = jsonArrayAlbums.getJSONObject(i);

                String id = objJson.getString(Constant.TAG_AID);
                String name = objJson.getString(Constant.TAG_ALBUM_NAME);
                String image = objJson.getString(Constant.TAG_ALBUM_IMAGE);
                String thumb = objJson.getString(Constant.TAG_ALBUM_THUMB);

                ItemAlbums objItem = new ItemAlbums(id, name, image, thumb);
                arrayListAlbums.add(objItem);
            }

            JSONArray jsonArraySongs = jsonObject.getJSONArray("trending_songs");
            for (int i = 0; i < jsonArraySongs.length(); i++) {
                JSONObject objJson = jsonArraySongs.getJSONObject(i);

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
                arrayListSongs.add(objItem);
            }
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        homeListener.onEnd(s, arrayListBanner, arrayListAlbums, arrayListArtist, arrayListSongs);
        super.onPostExecute(s);
    }
}