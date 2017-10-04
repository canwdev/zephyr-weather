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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotifation();
        updateBingPic();
        // 每小时后台更新一次
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + Conf.WEATHER_UPDATE_HOURS;
        Intent i = new Intent(this, UpdateWeatherService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME, triggerAtTime, pi);

        return super.onStartCommand(intent, flags, startId);
    }

    // 显示一条前台通知
    private void createNotifation() {
        Weather weather = new Utility(this).getWeather();

        if (weather != null && "ok".equals(weather.status)) {
            try {
                startForeground(1, getNotification(weather));
            } catch (ParseException e) {
                e.printStackTrace();
            }
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
                .setContentText(conditionInfo + " " + temperature + getString(R.string.u_celsius) + " (" + windForce + windDirection + ") ");
        if (0 < temperature) {
            builder.setProgress(50, temperature, false);
        }
        return builder.build();
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
