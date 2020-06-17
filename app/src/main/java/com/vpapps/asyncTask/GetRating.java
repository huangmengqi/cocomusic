package com.vpapps.asyncTask;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vpapps.interfaces.RatingListener;
import com.vpapps.utils.API;
import com.vpapps.utils.Constant;
import com.vpapps.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class GetRating extends AsyncTask<String, String, Boolean> {

    private String rate = "0";
    private RatingListener ratingListener;
    RequestBody requestBody;

    public GetRating(RatingListener ratingListener, RequestBody requestBody) {
        this.ratingListener = ratingListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        ratingListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        String json = JsonUtils.okhttpPost(Constant.SERVER_URL, requestBody);

        try {
            JSONObject jOb = new JSONObject(json);
            JSONArray jsonArray = jOb.getJSONArray(Constant.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject c = jsonArray.getJSONObject(i);
                rate = c.getString(Constant.TAG_USER_RATE);
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ee) {
            ee.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean s) {
        ratingListener.onEnd(String.valueOf(s), "","", Integer.parseInt(rate));
        super.onPostExecute(s);
    }
}
