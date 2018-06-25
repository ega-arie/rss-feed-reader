package com.ega.rssfeedreader.service;

import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpService extends AsyncTask<String, Void, String> {

    public interface OnTaskCompleted {
        void onSuccess(String response);
    }

    private OnTaskCompleted taskCompleted;

    public HttpService(){
    }

    public void onFinish(OnTaskCompleted taskCompleted){
        this.taskCompleted = taskCompleted;
    }

    @Override
    protected String doInBackground(String... params) {
        Response response = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(params[0])
                .build();

        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful())
                return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(String result){
        taskCompleted.onSuccess(result);
    }
}
