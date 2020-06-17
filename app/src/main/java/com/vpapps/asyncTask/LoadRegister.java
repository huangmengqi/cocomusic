package com.vpapps.asyncTask;

import android.os.AsyncTask;

import com.vpapps.interfaces.SuccessListener;
import com.vpapps.utils.Constant;
import com.vpapps.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.RequestBody;

public class LoadRegister extends AsyncTask<String, String, String> {

    private RequestBody requestBody;
    private SuccessListener successListener;
    private String success = "0", message = "";

    public LoadRegister(SuccessListener successListener, RequestBody requestBody) {
        this.successListener = successListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        successListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String json = JsonUtils.okhttpPost(Constant.SERVER_URL, requestBody);
            JSONObject mainJson = new JSONObject(json);
            JSONArray jsonArray = mainJson.getJSONArray(Constant.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject c = jsonArray.getJSONObject(i);

                success = c.getString(Constant.TAG_SUCCESS);
                message = c.getString(Constant.TAG_MSG);
            }
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        successListener.onEnd(s, success, message);
        super.onPostExecute(s);
    }
}