package com.storeyfilms.zenith.async;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.storeyfilms.zenith.MainActivity;
import com.storeyfilms.zenith.SplashActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpAsyncTask extends AsyncTask<String, Void, Response> {
    private static final String TAG = "HttpAsyncTask";

    private SplashActivity     context;
    private String             m_curUser;
    public HttpAsyncTask(SplashActivity context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        Log.e(TAG, "..................Send Http Start...................");
        context.showWaiting(true);
    }

    @Override
    protected Response doInBackground(String... params) {
        Log.e(TAG, "..................Sending Http...................");

        String username = params[0];
        String email = params[1];
        m_curUser = username;
        String api_token = SplashActivity.API_TOKEN;

        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.indiegogo.com/2/campaigns/2526147/contributions.json").newBuilder();
        urlBuilder.addQueryParameter("api_token", api_token);
        urlBuilder.addQueryParameter("email", email);
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
                    JSONArray contAry = jsonObj.getJSONArray("response");

                    int maxContribute = 1;
                    int len = contAry.length();
                    for (int i = 0; i< len; i++) {
                        JSONObject contObj = contAry.getJSONObject(i);
                        //String user = contObj.getString("by");
                        //if (!user.equalsIgnoreCase(m_curUser)) continue;

                        int amount = contObj.getInt("amount");
                        if (maxContribute < amount) {
                            maxContribute = amount;
                        }
                    }
                    if (maxContribute < 5) {
                        maxContribute = 1;
                    } else if (maxContribute < 10) {
                        maxContribute = 5;
                    } else if (maxContribute < 20) {
                        maxContribute = 10;
                    } else if (maxContribute < 50) {
                        maxContribute = 20;
                    } else {
                        maxContribute = 50;
                    }

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(context.CONTRIBUTE_KEY, Integer.toString(maxContribute));
                    editor.commit();

                    updateUI(Integer.toString(maxContribute));
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
            //Toast.makeText(context, "Response error", Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String contribute = sharedPreferences.getString(SplashActivity.CONTRIBUTE_KEY, "");
            updateUI(contribute);
        }
        context.showWaiting(false);
    }


    private void updateUI(String contribute) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("contribute", contribute);
        context.startActivity(intent);
        context.finish();
    }
}
