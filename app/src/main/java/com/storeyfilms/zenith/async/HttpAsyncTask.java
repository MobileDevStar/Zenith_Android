package com.storeyfilms.zenith.async;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpAsyncTask extends AsyncTask<String, Void, Response> {
    private static final String TAG = "HttpAsyncTask";

    private Context context;

    public HttpAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        Log.e(TAG, "..................Send Http Start...................");
    }

    @Override
    protected Response doInBackground(String... params) {
        Log.e(TAG, "..................Sending Http...................");

        String email = params[0];
        String api_token = "6293ec4d339638fcf3400178cb640c0c3de82c25ec8fbe3dfadb300c1c044b89";

        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.indiegogo.com/2/accounts/031584c9/contributions.json").newBuilder();
        urlBuilder.addQueryParameter("api_token", api_token);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return  null;
        }
    }

    @Override
    protected void onPostExecute(Response response) {
        Log.e(TAG, "..................Receive Http Response...................");
        if (response != null && response.isSuccessful()) {
            try {
                String responseData = response.body().string();
                Log.e(TAG, "..................Response success...................");
                Log.e(TAG, responseData);
                JSONObject jsonObj = new JSONObject(responseData);
                if (jsonObj != null) {

                } else {
                    Toast.makeText(context, "Response Error", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                Toast.makeText(context, "Response body to string error", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "..................Response body to string error...................");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "..................Response error...................");
            Toast.makeText(context, "Response error", Toast.LENGTH_SHORT).show();
        }
    }
}
