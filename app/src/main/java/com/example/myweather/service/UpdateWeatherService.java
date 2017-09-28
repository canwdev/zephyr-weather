package com.example.myweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.example.myweather.WeatherActivity;
import com.example.myweather.gson.Weather;
import com.example.myweather.util.Conf;
import com.example.myweather.util.HttpUtil;
import com.example.myweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateWeatherService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // 每8小时更新一次
        int eightHours = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + eightHours;
        Intent i = new Intent(this, UpdateWeatherService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    // 更新天气，保存设置
    private void updateWeather() {
        SharedPreferences prefAllSettings = getSharedPreferences(Conf.PREF_FILE_NAME, MODE_PRIVATE);
        String setCityWeatherId = "city="+prefAllSettings.getString(Conf.PREF_WEATHER_ID, null);
        final String weatherUrl = WeatherActivity.WEATHER_API_URL+setCityWeatherId+WeatherActivity.KEY;

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                if (weather != null && "ok".equals(weather.status)) {
                    SharedPreferences.Editor editor = getSharedPreferences(Conf.PREF_FILE_NAME, MODE_PRIVATE).edit();
                    editor.putString(Conf.PREF_WEATHER_SAVE, responseText);
                    editor.apply();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
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
