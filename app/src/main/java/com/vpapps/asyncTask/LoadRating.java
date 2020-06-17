package com.vpapps.asyncTask;

import android.os.AsyncTask;

import com.vpapps.interfaces.RatingListener;
import com.vpapps.utils.Constant;
import com.vpapps.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.RequestBody;

public class LoadRating extends AsyncTask<String, String, String> {

    private RequestBody requestBody;
    private String msg = "", isRateSuccess = "0", rate = "0";
    private RatingListener ratingListener;

    public LoadRating(RatingListener ratingListener, RequestBody requestBody) {
        this.ratingListener = ratingListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        ratingListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        String json = JsonUtils.okhttpPost(Constant.SERVER_URL, requestBody);

        try {
            JSONObject jOb = new JSONObject(json);
            JSONArray jsonArray = jOb.getJSONArray(Constant.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject c = jsonArray.getJSONObject(i);

                isRateSuccess = c.getString(Constant.TAG_SUCCESS);
                msg = c.getString(Constant.TAG_MSG);
                if (c.has("rate_avg")) {
                    rate = c.getString("rate_avg");
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
        ratingListener.onEnd(String.valueOf(s), isRateSuccess, msg, Integer.parseInt(rate));
        super.onPostExecute(s);
    }
}