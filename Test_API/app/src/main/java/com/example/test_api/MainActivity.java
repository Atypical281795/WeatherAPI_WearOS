package com.example.test_api;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView dateTextView;
    private TextView timeTextView;
    private ImageView weatherImageView;
    private static final Map<String, String> dayTranslations = new HashMap<>();
    private Handler handler;
    private Runnable updateTask;
    private NotificationManager notificationManager;
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "HourlyNotificationChannel";

    static {
        dayTranslations.put("Mon", "一");
        dayTranslations.put("Tue", "二");
        dayTranslations.put("Wed", "三");
        dayTranslations.put("Thu", "四");
        dayTranslations.put("Fri", "五");
        dayTranslations.put("Sat", "六");
        dayTranslations.put("Sun", "日");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dateTextView = findViewById(R.id.dateTextView);
        timeTextView = findViewById(R.id.timeTextView);
        weatherImageView = findViewById(R.id.weatherImageView);

        // 隱藏標題欄位
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 初始化通知管理器
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        // 定時更新抓取資料及整點報時
        handler = new Handler(Looper.getMainLooper());
        updateTask = new Runnable() {
            @Override
            public void run() {
                displayCurrentDateTime();
                checkOnTheHour();
                fetchWeatherData();
                handler.postDelayed(this, 1000); // 每分鐘更新一次
            }
        };
        handler.post(updateTask); // 初始執行一次更新任務
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTask); // 停止定時任務
    }

    private void displayCurrentDateTime() {
        TimeZone timeZone = TimeZone.getDefault();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd (E)", Locale.getDefault());
        dateFormat.setTimeZone(timeZone);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        timeFormat.setTimeZone(timeZone);

        String currentDate = dateFormat.format(new Date());
        String currentTime = timeFormat.format(new Date());

        for (Map.Entry<String, String> entry : dayTranslations.entrySet()) {
            if (currentDate.contains(entry.getKey())) {
                currentDate = currentDate.replace(entry.getKey(), entry.getValue());
                break;
            }
        }

        dateTextView.setText(currentDate);
        timeTextView.setText(currentTime);
        timeTextView.setScaleX(0.75f);
        timeTextView.setScaleY(0.75f);
    }

    private void fetchWeatherData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            WeatherApi.getAppSetting(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("test api failure", e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String responseData = response.body().string();
                        //Log.e("test api response", responseData);

                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            JSONObject cwaOpenData = jsonResponse.optJSONObject("cwaopendata");
                            if (cwaOpenData != null) {
                                JSONObject dataset = cwaOpenData.optJSONObject("dataset");
                                if (dataset != null) {
                                    JSONArray locations = dataset.optJSONArray("location");
                                    if (locations != null && locations.length() > 0) {
                                        JSONObject location = locations.optJSONObject(0);
                                        JSONArray weatherElements = location.optJSONArray("weatherElement");
                                        if (weatherElements != null && weatherElements.length() > 0) {
                                            JSONObject weatherElement = weatherElements.optJSONObject(0);
                                            JSONArray times = weatherElement.optJSONArray("time");
                                            if (times != null && times.length() > 0) {
                                                JSONObject time = times.optJSONObject(0);
                                                JSONArray elementValues = time.optJSONArray("elementValue");
                                                if (elementValues != null && elementValues.length() > 0) {
                                                    JSONObject elementValue = elementValues.optJSONObject(1);
                                                    String weatherCondition = elementValue.optString("value");
                                                    handler.post(() -> updateWeatherImage(weatherCondition));
                                                } else {
                                                    Log.e("test api", "Element value is null or empty");
                                                }
                                            } else {
                                                Log.e("test api", "Time is null or empty");
                                            }
                                        } else {
                                            Log.e("test api", "Weather elements are null or empty");
                                        }
                                    } else {
                                        Log.e("test api", "Locations are null or empty");
                                    }
                                } else {
                                    Log.e("test api", "Dataset is null or empty");
                                }
                            } else {
                                Log.e("test api", "CwaOpenData is null or empty");
                            }
                        } catch (JSONException e) {
                            Log.e("test api", "JSON parsing error: " + e.getMessage());
                        }
                    }
                }
            });
        });
    }

    private void updateWeatherImage(String weatherCondition) {
        int weatherCode = Integer.parseInt(weatherCondition);
        int resId = getResources().getIdentifier("weather_" + weatherCode, "drawable", getPackageName());
        weatherImageView.setImageResource(resId);
    }

    private void checkOnTheHour() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm", Locale.getDefault());
        String minute = dateFormat.format(new Date());
        if (minute.equals("00")) {
            showOnTheHourNotification();
        }
    }

    private void showOnTheHourNotification() {
        // 建立一個大文本樣式的通知
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                .bigText("!!");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("整點報時")
                .setStyle(bigTextStyle) // 設置大文本樣式
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // 顯示通知
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Hourly Notification";
            String description = "Notification for hourly reminder";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // 註冊通知頻道
            notificationManager.createNotificationChannel(channel);
        }
    }
}
