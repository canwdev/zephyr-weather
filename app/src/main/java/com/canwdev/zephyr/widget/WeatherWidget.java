package com.canwdev.zephyr.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.widget.RemoteViews;

import com.canwdev.zephyr.R;
import com.canwdev.zephyr.RecentAreaActivity;
import com.canwdev.zephyr.WeatherActivity;
import com.canwdev.zephyr.db.RecentArea;
import com.canwdev.zephyr.service.WidgetService;

import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class WeatherWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Intent iGoWeather = new Intent(context, WeatherActivity.class);
        PendingIntent piGoWeather = PendingIntent.getActivity(context, 0, iGoWeather, 0);

        // 打开系统时钟
        Intent iGoClock = getPackageIntent("com.android.deskclock", context);
        PendingIntent piGoClock = PendingIntent.getActivity(context, 0, iGoClock, 0);
        // 打开系统日历
        Intent iGoCalendar = getPackageIntent("com.android.calendar", context);
        PendingIntent piGoCalendar = PendingIntent.getActivity(context, 0, iGoCalendar, 0);

        Intent iGoRecentArea = new Intent(context, RecentAreaActivity.class);
        PendingIntent piGoRecentArea = PendingIntent.getActivity(context, 0, iGoRecentArea, 0);

        Intent iRefresh = new Intent(context, WidgetService.class);
        PendingIntent piRefresh=PendingIntent.getService(context, 0, iRefresh, 0);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather);
        views.setOnClickPendingIntent(R.id.widgetLayout, piGoWeather);
        views.setOnClickPendingIntent(R.id.ll_wt, piGoClock);
        views.setOnClickPendingIntent(R.id.ll_wd, piGoCalendar);
        views.setOnClickPendingIntent(R.id.textView_widget_city, piGoRecentArea);
        views.setOnClickPendingIntent(R.id.textView_widget_weather, piRefresh);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    // ComponentName clock = new ComponentName("com.android.deskclock", "com.android.deskclock.DeskClock");
    // Intent intent = new Intent(Intent.ACTION_MAIN);
    // intent.setComponent(clock);
    // 有时不知道应用程序的启动Activity的类名，而只知道包名，可以通过ResolveInfo类来取得启动Acitivty的类名
    private static Intent getPackageIntent(String packageName, Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(pi.packageName);
            List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(resolveIntent, 0);
            ResolveInfo ri = apps.iterator().next();
            if (ri != null) {
                packageName = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName(packageName, className);
                intent.setComponent(cn);
                return intent;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return new Intent(context, WeatherActivity.class);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // 小部件刷新
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        context.startService(new Intent(context, WidgetService.class));
    }

    @Override
    public void onEnabled(Context context) {
        // 第一次创建小部件

    }

    @Override
    public void onDisabled(Context context) {
        // 小部件移除
        context.stopService(new Intent(context, WidgetService.class));
    }


}

