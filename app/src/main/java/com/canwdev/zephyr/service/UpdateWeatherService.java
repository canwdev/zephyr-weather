package com.canwdev.zephyr.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.canwdev.zephyr.R;
import com.canwdev.zephyr.WeatherActivity;
import com.canwdev.zephyr.gson.Weather;
import com.canwdev.zephyr.util.Conf;
import com.canwdev.zephyr.util.HttpUtil;
import com.canwdev.zephyr.util.Utility;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateWeatherService extends Service {
    private static final String TAG = "UWS!!";
    Weather weather;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // 每1小时后台更新一次
        int eightHours = 1 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + eightHours;
        Intent i = new Intent(this, UpdateWeatherService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotifation();
    }

    // 显示一条前台通知
    private void createNotifation(){
        if (weather != null) {
            if ("ok".equals(weather.status)) {
                try {
                    startForeground(1, getNotification(weather));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } else {
            updateWeather();
        }
    }

    private Notification getNotification(Weather weather) throws ParseException {
        int temperature = Integer.valueOf(weather.now.temperature);
        String cityName = weather.basic.cityName;
        String windForce = weather.now.wind.windforce;
        String windDirection = weather.now.wind.direction;
        String conditionInfo = weather.now.condition.info;
        SimpleDateFormat dateFormat = new SimpleDateFormat(Conf.DATE_TIME_FORMAT);
        Date updateTime = dateFormat.parse(weather.basic.update.updateTime);

        Intent intent = new Intent(this, WeatherActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_cloud_circle_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setContentIntent(pi)
                .setWhen(updateTime.getTime())
                .setContentTitle(cityName)
                .setContentText(conditionInfo + " " + temperature + "℃"+" ("+windForce+"级"+windDirection+") ");
        if (0 < temperature) {
            builder.setProgress(50, temperature, false);
        }
        return builder.build();
    }

    // 更新天气，保存设置，显示通知
    private void updateWeather() {
        SharedPreferences prefAllSettings = getSharedPreferences(Conf.PREF_FILE_NAME, MODE_PRIVATE);
        String setCityWeatherId = "city=" + prefAllSettings.getString(Conf.PREF_WEATHER_ID, null);
        String apiKey = "&key=" + Conf.getKey(this);
        final String weatherUrl = Conf.WEATHER_API_URL +setCityWeatherId+ apiKey;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                weather = Utility.handleWeatherResponse(responseText);
                if (weather != null && "ok".equals(weather.status)) {
                    SharedPreferences.Editor editor = getSharedPreferences(Conf.PREF_FILE_NAME, MODE_PRIVATE).edit();
                    editor.putString(Conf.PREF_WEATHER_SAVE, responseText);
                    editor.apply();
                    if (weather != null) {
                        createNotifation();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e(TAG, "onFailure: ", e);
            }
        });

    }

    // 获取 Bing 每日一图地址，保存设置
    private void updateBingPic() {
        final String bingPicApiUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(bingPicApiUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPicUrl = response.body().string();
                SharedPreferences.Editor editor = getSharedPreferences(Conf.PREF_FILE_NAME, MODE_PRIVATE).edit();
                editor.putString(Conf.PREF_BG_URL, bingPicUrl);
                editor.apply();
            }
        });
    }
}
