package com.example.test_api;


import android.util.Log;
import java.util.concurrent.TimeUnit;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;


public class WeatherApi {


    public static final String TAG = "Execute";
    public static Call call;
    public static String API = "https://opendata.cwa.gov.tw/fileapi/v1/opendataapi/";
    public static void connect(Request request, Callback callback){
        OkHttpClient client = null;
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30,TimeUnit.SECONDS)
                .readTimeout(30,TimeUnit.SECONDS)
                .build();
        call = client.newCall(request);
        call.enqueue(callback);
    }
    public static void post(String api, RequestBody body, Callback callback){
        final Request request = new Request.Builder()
                .url(API + api)
                .post(body)
                .addHeader("content-Type", "application/json")
                .build();
        if (!api.contains("UploadMemberFile")) {
            Log.e(TAG, "post : " + request.toString() + "\nbody : " + bodyToString(request));
        }
        connect(request, callback);
    }
    public static void get(String api,Callback callback){
        final Request request = new Request.Builder()
                .url(API + api)
                .get()
                .addHeader("content-Type", "application/json")
                .build();
        Log.e(TAG,"get : " + request.toString() + "\nbody : " + bodyToString(request));
        connect(request, callback);
    }
    public static String bodyToString(final Request request){
        try{
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            String s = buffer.readUtf8();
            if (s.contains("image/jpeg")){
                s = "is upload image";
            }
            return s;
        }catch (Exception e){
            Log.e(TAG,"bodyToString : " + e.toString());
            return "did not work";
        }
    }
    public static void getAppSetting(
            Callback callback
    )
    {
        String api = "F-C0032-007?Authorization=CWA-3144FA67-852D-4841-9989-C6FBE2D2EC67&downloadType=WEB&format=JSON";
        get(api,callback);
    }
//    public static void getWeatherByLocation(double latitude, double longitude, Callback callback) {
//        String api = String.format("some_api_endpoint?lat=%f&lon=%f", latitude, longitude); // Replace with actual API endpoint for weather by location
//        get(api, callback);
//    }
}