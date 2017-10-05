package com.canwdev.zephyr.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.canwdev.zephyr.R;
import com.canwdev.zephyr.gson.Weather;
import com.canwdev.zephyr.util.Conf;
import com.canwdev.zephyr.util.Utility;
import com.canwdev.zephyr.widget.WeatherWidget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

// 天气时钟小部件
public class WidgetService extends Service {
    private static final String TAG = "WidgetService!!";
    private Timer timerUpdateClock;
    private Timer timerUpdateWeather;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWidgetWeather();
        /*Log.d(TAG, "onStartCommand: WS");
        updateWidgetWeather();
        // 每小时后台更新一次
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + 1000;
        Intent i = new Intent(this, UpdateWeatherService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME, triggerAtTime, pi);*/

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timerUpdateClock = new Timer();
        timerUpdateClock.schedule(new UpdateClockTask(), 0, 1000);
        timerUpdateWeather = new Timer();
        timerUpdateWeather.schedule(new UpdateWeatherTask(), 0, Conf.WEATHER_SERVER_UPDATE_MS);

    }

    private void updateWidgetWeather() {
        Weather weather = new Utility(this).getWeather();
        if (weather != null) {
            if ("ok".equals(weather.status)) {
                String cityName = weather.basic.cityName;
                int temperature = Integer.valueOf(weather.now.temperature);
                String conditionInfo = weather.now.condition.info;

                AppWidgetManager widgetManager = AppWidgetManager.getInstance(getApplicationContext());

                RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.widget_weather);
                remoteView.setTextViewText(R.id.textView_widget_city, cityName);
                remoteView.setTextViewText(R.id.textView_widget_weather, conditionInfo + " "
                        + temperature + getString(R.string.u_celsius));

                ComponentName componentName = new ComponentName(getApplicationContext(), WeatherWidget.class);
                widgetManager.updateAppWidget(componentName, remoteView);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timerUpdateClock.cancel();
        timerUpdateClock = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 更新日期时间
    private final class UpdateClockTask extends TimerTask {
        @Override
        public void run() {
            SimpleDateFormat sdfTime = new SimpleDateFormat(Conf.WIDGET_CLOCK_TIME_FORMAT);
            SimpleDateFormat sdfDate = new SimpleDateFormat(Conf.WIDGET_CLOCK_DATE_FORMAT);
            Date now = new Date();
            String time = sdfTime.format(now);
            String date = sdfDate.format(now);

            AppWidgetManager widgetManager = AppWidgetManager.getInstance(getApplicationContext());

            RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.widget_weather);
            remoteView.setTextViewText(R.id.textView_widget_time, time);
            remoteView.setTextViewText(R.id.textView_widget_date, date);

            ComponentName componentName = new ComponentName(getApplicationContext(), WeatherWidget.class);
            widgetManager.updateAppWidget(componentName, remoteView);
        }
    }

    // 更新日期时间
    private final class UpdateWeatherTask extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "WidgetService: UpdateWeatherTask");
            updateWidgetWeather();
        }
    }
}
