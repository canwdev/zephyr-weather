package com.canwdev.zephyr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.canwdev.zephyr.gson.DailyForecast;
import com.canwdev.zephyr.gson.HourlyForecast;
import com.canwdev.zephyr.gson.Weather;
import com.canwdev.zephyr.service.UpdateWeatherService;
import com.canwdev.zephyr.util.Conf;
import com.canwdev.zephyr.util.HttpUtil;
import com.canwdev.zephyr.util.Utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public final static int INTENT_CHOOSE_AREA = 1;
    // public final static int INTENT_RECENT_AREA = 2;

    private static final String TAG = "WeatherActivity!!";
    private SharedPreferences pref;
    private String apiKey;
    // public static final String WEATHER_API_URL_SAMPLE = WEATHER_API_URL + CITY_SAMPLE + apiKey;
    private String cityWeatherId = Conf.HEWEATHER_CITY_SAMPLE_NULL;
    // 各控件
    private DrawerLayout mDrawerLayout;
    private ImageButton buttonOpenDrawer;
    private ImageButton buttonShare;
    private SwipeRefreshLayout swipeRefresh;
    private ImageView bgImage;
    private ScrollView weatherScrollView;
    private TextView titleCityText;
    private TextView titleUpdateTimeText;
    private TextView temperatureText;
    private TextView weatherStatusText;
    /* 天气详细信息开始 */
    private TextView nowDetailTitleText;
    private TextView temperatureDetailText;
    private TextView infoDetailText;
    private TextView fellingDetailText;
    private TextView pressureDetailText;
    private TextView humidityDetailText;
    private TextView precipitationDetailText;
    private TextView visibilityDetailText;
    private TextView degreeDetailText;
    private TextView directionDetailText;
    private TextView windforceDetailText;
    private TextView speedDetailText;
    /* 天气详细信息结束 */
    private LinearLayout hourlyForecastLayout;
    private LinearLayout dailyForecastLayout;
    private TextView aqiText;
    private TextView qualityText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView wearingText;
    private TextView influenzaText;
    private TextView carWashText;
    private TextView travelText;
    private TextView uvText;
    private TextView sportText;
    private String shareWeatherText;
    /*刷新动画效果*/
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    final LinearLayout animView = (LinearLayout) findViewById(R.id.LinearLayout_mainAnim);
                    final Animation animShow = AnimationUtils.loadAnimation(WeatherActivity.this, R.anim.anim_card_show);
                    animView.setAnimation(animShow);
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        initView();
    }

    // 初始化各控件与设置
    private void initView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.weather_drawer);
        buttonOpenDrawer = (ImageButton) findViewById(R.id.button_drawer);
        buttonShare = (ImageButton) findViewById(R.id.button_shareWeather);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.SwipeRefresh_weather);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        bgImage = (ImageView) findViewById(R.id.imageView_bg);
        weatherScrollView = (ScrollView) findViewById(R.id.ScrollView_weather);
        titleCityText = (TextView) findViewById(R.id.textView_cityName);
        titleUpdateTimeText = (TextView) findViewById(R.id.textView_updateTime);
        temperatureText = (TextView) findViewById(R.id.textView_tDegree);
        weatherStatusText = (TextView) findViewById(R.id.textView_tStatus);
        /* 天气详细信息开始 */
        nowDetailTitleText = (TextView) findViewById(R.id.textView_nowDetailTitle);
        temperatureDetailText = (TextView) findViewById(R.id.temperature);
        infoDetailText = (TextView) findViewById(R.id.info);
        fellingDetailText = (TextView) findViewById(R.id.felling);
        pressureDetailText = (TextView) findViewById(R.id.pressure);
        humidityDetailText = (TextView) findViewById(R.id.humidity);
        precipitationDetailText = (TextView) findViewById(R.id.precipitation);
        visibilityDetailText = (TextView) findViewById(R.id.visibility);
        degreeDetailText = (TextView) findViewById(R.id.degree);
        directionDetailText = (TextView) findViewById(R.id.direction);
        windforceDetailText = (TextView) findViewById(R.id.windforce);
        speedDetailText = (TextView) findViewById(R.id.speed);
        /* 天气详细信息结束 */
        hourlyForecastLayout = (LinearLayout) findViewById(R.id.LinearLayout_hourlyForecast);
        dailyForecastLayout = (LinearLayout) findViewById(R.id.LinearLayout_dailyForecast);
        aqiText = (TextView) findViewById(R.id.textView_aqi);
        qualityText = (TextView) findViewById(R.id.textView_quality);
        pm25Text = (TextView) findViewById(R.id.textView_pm25);
        // 自定义字体
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/clockopia.ttf");
        aqiText.setTypeface(face);
        pm25Text.setTypeface(face);
        comfortText = (TextView) findViewById(R.id.textView_comfort);
        carWashText = (TextView) findViewById(R.id.textView_carWash);
        wearingText = (TextView) findViewById(R.id.textView_wearing);
        influenzaText = (TextView) findViewById(R.id.textView_influenza);
        sportText = (TextView) findViewById(R.id.textView_sport);
        travelText = (TextView) findViewById(R.id.textView_travel);
        uvText = (TextView) findViewById(R.id.textView_uv);

        pref = getSharedPreferences(Conf.PREF_FILE_NAME, MODE_PRIVATE);

        // 下拉刷新动作
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(cityWeatherId);
            }
        });

        // 打开抽屉按钮动作
        buttonOpenDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // 分享按钮动作
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareWeather();
            }
        });


        // 设置抽屉内的点击事件
        final NavigationView navigation = (NavigationView) findViewById(R.id.weather_drawer_navigation);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.drawer_item_recentArea:
                        Intent iRecentArea = new Intent(WeatherActivity.this, RecentAreaActivity.class);
                        startActivityForResult(iRecentArea, INTENT_CHOOSE_AREA);
                        break;
                    case R.id.drawer_item_setArea:
                        Intent iSetArea = new Intent(WeatherActivity.this, ChooseAreaActivity.class);
                        // 去选择地区
                        startActivityForResult(iSetArea, INTENT_CHOOSE_AREA);
                        break;
                    case R.id.drawer_item_settings:
                        Intent iGoSetting = new Intent(WeatherActivity.this, SettingsActivity.class);
                        startActivity(iGoSetting);
                        break;
                    case R.id.drawer_item_downImage:
                        loadBgImage();
                        String pictureUrl = getSharedPreferences(Conf.PREF_FILE_NAME, MODE_PRIVATE).getString(Conf.PREF_BG_URL, null);
                        if (pictureUrl != null) {
                            Uri uri = Uri.parse(pictureUrl);
                            Intent downloadIntent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(downloadIntent);
                        } else {
                            Snackbar.make(weatherScrollView, getString(R.string.get_picture_url_failed), Snackbar.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.drawer_item_github:
                        Intent iGoAbout = new Intent(WeatherActivity.this, AboutActivity.class);
                        startActivity(iGoAbout);
                        break;
                    default:
                        break;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    // 分享
    private void shareWeather() {
        Intent intent = new Intent(Intent.ACTION_SEND);

        // 分享天气详细信息截图
        CardView pscView = (CardView) findViewById(R.id.CardView_nowDetail);
        pscView.setDrawingCacheEnabled(true);
        Bitmap bitmap = pscView.getDrawingCache();
        bitmap = Bitmap.createBitmap(bitmap);
        pscView.setDrawingCacheEnabled(false);
        if (bitmap != null) {
            final File f = new File(getExternalCacheDir(), "img_cache.png");
            if (f.exists()) {
                f.delete();
            }
            try {
                FileOutputStream out = new FileOutputStream(f);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Uri u = Uri.fromFile(f);
            intent.putExtra(Intent.EXTRA_TEXT, shareWeatherText);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_STREAM, u);
            intent.setType("image/jpeg");
        } else {
            intent.putExtra(Intent.EXTRA_TEXT, shareWeatherText);
            intent.setType("text/plain");
        }

        startActivity(Intent.createChooser(intent, getString(R.string.share_with)));
    }

    // Activity 恢复前台时执行检查动作
    @Override
    protected void onStart() {
        super.onStart();
        apiKey = "&key=" + Conf.getKey(WeatherActivity.this);
        String setWeatherId = pref.getString(Conf.PREF_WEATHER_ID, null);
        String weatherCache = pref.getString(Conf.PREF_WEATHER_SAVE, null);
        cityWeatherId = "city=" + setWeatherId;
        // 有缓存时直接解析天气数据
        if (weatherCache != null) {
            Weather cWeather = Utility.handleWeatherResponse(weatherCache);
            // 检查城市id是否存在
            if (setWeatherId != null) {
                if (cWeather != null && "ok".equals(cWeather.status)) {
                    // 检查默认地区设置是否一致
                    if (!cWeather.basic.weatherId.equals(setWeatherId)) {
                        // 不一致
                        swipeRefresh.setRefreshing(true);
                        requestWeather(cityWeatherId);
                    } else {
                        // 如果缓存时间与系统时间相差大于-小时，则更新
                        SimpleDateFormat dateFormat = new SimpleDateFormat(Conf.DATE_TIME_FORMAT);
                        try {
                            Date cachedUpdateTime = dateFormat.parse(cWeather.basic.update.updateTime);
                            Date SystemTime = new Date();
                            long diff = SystemTime.getTime() - cachedUpdateTime.getTime();
                            double hours = (double) diff / (1000 * 60 * 60);
                            if (hours > Conf.WEATHER_UPDATE_HOURS) {
                                swipeRefresh.setRefreshing(true);
                                requestWeather(cityWeatherId);
                            } else {
                                showWeatherInfo(cWeather);
                            }
                        } catch (ParseException e) {
                            Log.e(TAG, "ParseException: " + e.getMessage(), e);
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                showWeatherInfo(cWeather);
            }
        } else {
            // 无缓存时去服务器查询天气
            if (setWeatherId != null) {
                swipeRefresh.setRefreshing(true);
                requestWeather(cityWeatherId);
            } else {
                weatherScrollView.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(WeatherActivity.this, ChooseAreaActivity.class);
                // 去选择地区
                startActivityForResult(intent, 1);
            }
        }

        // 背景图检查
        String pictureUrl = pref.getString(Conf.PREF_BG_URL, null);
        Boolean enabledBg = pref.getBoolean(Conf.PREF_ENABLE_BG_IMAGE, true);
        if (pictureUrl != null) {
            if (enabledBg) {
                Glide.with(this).load(pictureUrl).into(bgImage);
            } else {
                Glide.with(this).load(getResources().getColor(R.color.colorPrimary)).into(bgImage);
            }
        } else {
            loadBgImage();
        }
    }

    // 接收更改地区的 Intent 返回值
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INTENT_CHOOSE_AREA:
                if (resultCode == RESULT_OK) {
                    String rCityWeatherId = "city=" + data.getStringExtra("city_weather_id");
                    cityWeatherId = rCityWeatherId;
                    swipeRefresh.setRefreshing(true);
                    requestWeather(rCityWeatherId);
                }
                break;
            default:
        }
    }

    // 获取 Bing 每日一图地址，保存地址并设置背景
    private void loadBgImage() {
        if (pref.getBoolean(Conf.PREF_ENABLE_BG_IMAGE, true)) {
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(WeatherActivity.this).load(bingPicUrl).into(bgImage);
                        }
                    });
                }
            });
        }

    }

    // 更新天气，保存设置，更新界面
    private void requestWeather(final String weatherId) {
        final String weatherUrl = Conf.WEATHER_API_URL + weatherId + apiKey;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = getSharedPreferences(Conf.PREF_FILE_NAME, MODE_PRIVATE).edit();
                            editor.putString(Conf.PREF_WEATHER_SAVE, responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                            Utility.recordRecentArea(weather.basic.weatherId, weather.basic.cityName);
                        } else if (weather != null) {
                            // 显示错误信息
                            Snackbar.make(weatherScrollView,
                                    Utility.getWeatherErrMsg(WeatherActivity.this, weather.status), Snackbar.LENGTH_LONG).show();
                        }

                        weatherScrollView.setVisibility(View.VISIBLE);

                        /*刷新动画效果*/
                        final LinearLayout animView = (LinearLayout) findViewById(R.id.LinearLayout_mainAnim);
                        Animation animHide = AnimationUtils.loadAnimation(WeatherActivity.this, R.anim.anim_card_hide);
                        animView.setAnimation(animHide);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Message message = new Message();
                                message.what = 1;
                                Bundle data = new Bundle();
                                message.setData(data);
                                handler.sendMessage(message);
                            }
                        }).start();
                        /*刷新动画效果*/

                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        weatherScrollView.setVisibility(View.VISIBLE);
                        Snackbar.make(weatherScrollView, getString(R.string.get_weather_info_failed), Snackbar.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                        // 加载失败后加载缓存
                        String weatherCache = pref.getString(Conf.PREF_WEATHER_SAVE, null);
                        if (weatherCache != null) {
                            Weather cWeather = Utility.handleWeatherResponse(weatherCache);
                            showWeatherInfo(cWeather);
                        }
                    }
                });
            }
        });
        loadBgImage();
    }

    // 根据 weather 对象更新 UI 天气显示
    private void showWeatherInfo(Weather weather) {
        titleCityText.setText(weather.basic.cityName);
        nowDetailTitleText.setText(weather.basic.cityName);
        titleUpdateTimeText.setText(weather.basic.update.updateTime);
        temperatureText.setText(weather.now.temperature + getString(R.string.u_celsius));
        weatherStatusText.setText(weather.now.condition.info + getString(R.string.u_weather_decorate));
        // 设置分享信息
        shareWeatherText = weather.basic.cityName + ", "
                + weather.now.condition.info + ", "
                + weather.now.temperature + getString(R.string.u_celsius) + ", "
                + weather.basic.update.updateTime;
        /* 天气详细信息开始 */
        temperatureDetailText.setText(weather.now.temperature + getString(R.string.u_celsius));
        infoDetailText.setText(weather.now.condition.info);
        fellingDetailText.setText(weather.now.felling + getString(R.string.u_celsius));
        pressureDetailText.setText(weather.now.pressure);
        humidityDetailText.setText(weather.now.humidity + getString(R.string.u_percent));
        precipitationDetailText.setText(weather.now.precipitation + getString(R.string.u_millimeter));
        visibilityDetailText.setText(weather.now.visibility + getString(R.string.u_kilometer));
        degreeDetailText.setText(weather.now.wind.degree);
        directionDetailText.setText(weather.now.wind.direction);
        windforceDetailText.setText(weather.now.wind.windforce + getString(R.string.u_windforce));
        speedDetailText.setText(weather.now.wind.speed + getString(R.string.u_windspeed));
        /* 天气详细信息结束 */
        // 解析几小时预报
        CardView cardViewHourly = (CardView) findViewById(R.id.CardView_hourly);
        hourlyForecastLayout.removeAllViews();
        if (weather.hourlyForecastList.size() >= 1) {
            for (HourlyForecast hourlyForecast : weather.hourlyForecastList) {
                View view = LayoutInflater.from(this)
                        .inflate(R.layout.frag_weather_hourly_forecast_item, hourlyForecastLayout, false);
                TextView timeText = view.findViewById(R.id.textView_hfTime);
                TextView statusText = view.findViewById(R.id.textView_hfStatus);
                TextView tempe = view.findViewById(R.id.textView_hfTempe);
                TextView probability = view.findViewById(R.id.textView_hfPop);
                timeText.setText(hourlyForecast.date.split(" ")[1]);
                statusText.setText(hourlyForecast.condition.info);
                tempe.setText(hourlyForecast.temperature + getString(R.string.u_celsius));
                probability.setText(hourlyForecast.probability + getString(R.string.u_percent));
                hourlyForecastLayout.addView(view);
            }
            cardViewHourly.setVisibility(View.VISIBLE);
        } else {
            cardViewHourly.setVisibility(View.GONE);
        }

        // 解析几天预报
        dailyForecastLayout.removeAllViews();
        for (DailyForecast dailyForecast : weather.dailyForecastList) {
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.frag_weather_daily_forecast_item, dailyForecastLayout, false);
            TextView dateText = view.findViewById(R.id.textView_fDate);
            TextView infoText = view.findViewById(R.id.textView_fStatus);
            TextView maxText = view.findViewById(R.id.textView_tMax);
            TextView minText = view.findViewById(R.id.textView_tMin);
            dateText.setText(dailyForecast.date.substring(5));
            infoText.setText(dailyForecast.condition.info);
            maxText.setText(dailyForecast.temperature.max + getString(R.string.u_celsius));
            minText.setText(dailyForecast.temperature.min + getString(R.string.u_celsius));
            dailyForecastLayout.addView(view);
        }
        // 解析AQI指数
        CardView cardViewAqi = (CardView) findViewById(R.id.CardView_aqi);
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            qualityText.setText(weather.aqi.city.quality);
            pm25Text.setText(weather.aqi.city.pm25);
            cardViewAqi.setVisibility(View.VISIBLE);
        } else {
            cardViewAqi.setVisibility(View.GONE);
        }
        // 解析生活建议
        CardView cardViewSuggestion = (CardView) findViewById(R.id.CardViewSuggestion);
        if (weather.suggestion != null) {
            comfortText.setText("[" + weather.suggestion.comfort.title + "] " + weather.suggestion.comfort.info);
            carWashText.setText("[" + weather.suggestion.carWash.title + "] " + weather.suggestion.carWash.info);
            wearingText.setText("[" + weather.suggestion.wearing.title + "] " + weather.suggestion.wearing.info);
            influenzaText.setText("[" + weather.suggestion.influenza.title + "] " + weather.suggestion.influenza.info);
            sportText.setText("[" + weather.suggestion.sport.title + "] " + weather.suggestion.sport.info);
            travelText.setText("[" + weather.suggestion.travel.title + "] " + weather.suggestion.travel.info);
            uvText.setText("[" + weather.suggestion.uv.title + "] " + weather.suggestion.uv.info);
            cardViewSuggestion.setVisibility(View.VISIBLE);
        } else {
            cardViewSuggestion.setVisibility(View.GONE);
        }

        if (pref.getBoolean(Conf.PREF_ENABLE_SERVICE, false)) {
            // 启动后台天气自动更新服务
            Intent intent = new Intent(this, UpdateWeatherService.class);
            startService(intent);
        } else {
            Intent intent = new Intent(this, UpdateWeatherService.class);
            stopService(intent);
        }
    }

    @Override
    public void onBackPressed() {
        // 判断是否打开了抽屉，打开了则关闭，否则退出
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
