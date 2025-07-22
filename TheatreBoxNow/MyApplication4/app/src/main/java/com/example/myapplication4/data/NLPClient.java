package com.example.myapplication4.data;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class NLPClient {
    private static final String TAG = "NLPClient";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client = new OkHttpClient();
    private String baseUrl = "http://10.0.2.2:5000";


    private boolean isTesting = false;
    private String mockResponse;

    public interface NLPCallback {
        void onResult(String intent, JSONObject entities);
        void onError(String error);
    }
    public NLPClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS).build();
    }

    public void setTestingMode(boolean testing) {
        this.isTesting = testing;
    }

    public void setMockResponse(String response) {
        this.mockResponse = response;
    }
    public void processText(String text,NLPCallback callback){
        if (isTesting) {
            handleMockResponse(callback);
            return;
        }
        JSONObject json = new JSONObject();

        try {
            //message to JSON
            json.put("text", text);
            Log.d("TAG", json.toString());

            //Request Make
            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url(baseUrl + "/process")
                    .post(body)
                    .build();

            //handle response or failure
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //get full response
                    String result = response.body().string();
                    Log.d("TAG", "Raw response: " + result);

                    //convert response to json
                    JSONObject jsonResult = null;
                    try {
                        jsonResult = new JSONObject(result);
                        Log.d("TAG", "Response to JSON: " + jsonResult);
                    } catch (JSONException e) {
                        Log.d("TAG", "runtime to  json response " );
                        throw new RuntimeException(e);
                    }

                    //get intent, entities and response
                    String intent = null;
                    try {
                        intent = jsonResult.getString("intent");
                    } catch (JSONException e) {
                        Log.d("TAG", "runtime to  json intent " );
                        throw new RuntimeException(e);
                    }
                    JSONObject entities = null;
                    try {
                        entities = jsonResult.getJSONObject("entities");
                    } catch (JSONException e) {
                        Log.d("TAG", "runtime to  json entities " );
                        throw new RuntimeException(e);
                    }

                    Log.d("TAG", "intent: " + intent);
                    Log.d("TAG", "entities: " + entities);

                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    String finalIntent = intent;
                    JSONObject finalEntities = entities;

                    mainHandler.post(() -> {
                        callback.onResult(finalIntent, finalEntities);
                    });
                }


                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("TAG", "onFailure: "+ e.getMessage());
                }
            });

        } catch (JSONException e) {
            Log.e("ERROR", "Request creation failed: "+ e.getMessage() );
        }
    }



    private void handleMockResponse(NLPCallback callback) {
        try {
            if (mockResponse == null) {
                callback.onError("No mock response set");
                return;
            }

            JSONObject result = new JSONObject(mockResponse);
            callback.onResult(
                    result.getString("intent"),
                    result.getJSONObject("entities")
            );
        } catch (JSONException e) {
            callback.onError("Invalid mock JSON: " + e.getMessage());
        }
    }

    // For production use
    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }
}