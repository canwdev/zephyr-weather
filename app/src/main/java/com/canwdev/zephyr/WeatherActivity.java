package com.canwdev.zephyr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.canwdev.zephyr.gson.DailyForecast;
import com.canwdev.zephyr.gson.HourlyForecast;
import com.canwdev.zephyr.gson.Weather;
import com.canwdev.zephyr.service.UpdateWeatherService;
import com.canwdev.zephyr.util.Conf;
import com.canwdev.zephyr.util.HttpUtil;
import com.canwdev.zephyr.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public static final String WEATHER_API_URL = "https://free-api.heweather.com/v5/weather?";
    public static final String KEY = "&key=" + Conf.HEWEATHER_KEY;

    // public static final String WEATHER_API_URL_SAMPLE = WEATHER_API_URL + CITY_SAMPLE + KEY;
    private String cityWeatherId = "city=CN101240213";
    // 各控件
    private DrawerLayout mDrawerLayout;
    private Button buttonOpenDrawer;
    private SwipeRefreshLayout swipeRefresh;
    private ImageView bgImage;
    private ScrollView weatherScrollView;
    private TextView titleCityText;
    private TextView titleUpdateTimeText;
    private TextView temperatureText;
    private TextView weatherStatusText;
    /* 天气详细信息开始 */
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
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        // 适配透明状态
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        // 初始化各控件
        mDrawerLayout = (DrawerLayout) findViewById(R.id.weather_drawer);
        buttonOpenDrawer = (Button) findViewById(R.id.button_drawer);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.SwipeRefresh_weather);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        bgImage = (ImageView) findViewById(R.id.imageView_bg);
        weatherScrollView = (ScrollView) findViewById(R.id.ScrollView_weather);
        titleCityText = (TextView) findViewById(R.id.textView_cityName);
        titleUpdateTimeText = (TextView) findViewById(R.id.textView_updateTime);
        temperatureText = (TextView) findViewById(R.id.textView_tDegree);
        weatherStatusText = (TextView) findViewById(R.id.textView_tStatus);
        /* 天气详细信息开始 */
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
        pm25Text = (TextView) findViewById(R.id.textView_pm25);
        comfortText = (TextView) findViewById(R.id.textView_comfort);
        carWashText = (TextView) findViewById(R.id.textView_carWash);
        sportText = (TextView) findViewById(R.id.textView_sport);

        SharedPreferences prefAllSettings = getSharedPreferences(Conf.PREF_FILE_NAME, MODE_PRIVATE);
        String setCityWeatherId = prefAllSettings.getString(Conf.PREF_WEATHER_ID, null);

        String weatherCache = prefAllSettings.getString(Conf.PREF_WEATHER_SAVE, null);
        if (weatherCache != null) {
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherCache);
            // cityWeatherId = "city="+weather.basic.weatherId;

            // 检查默认地区设置是否一致
            if (setCityWeatherId != null) {
                cityWeatherId = "city=" + setCityWeatherId;
                if (!weather.basic.weatherId.equals(setCityWeatherId)) {
                    swipeRefresh.setRefreshing(true);
                    requestWeather(cityWeatherId);
                } else {
                    showWeatherInfo(weather);
                }
            } else {
                showWeatherInfo(weather);
            }
        } else {
            // 无缓存时去服务器查询天气
            if (setCityWeatherId != null) {
                cityWeatherId = "city=" + setCityWeatherId;
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
        String pictureUrl = prefAllSettings.getString(Conf.PREF_BG_URL, null);
        if (pictureUrl != null) {
            Glide.with(this).load(pictureUrl).into(bgImage);
        } else {
            loadBgImage();
        }

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

        // 设置抽屉内的点击事件
        final NavigationView navigation = (NavigationView) findViewById(R.id.weather_drawer_navigation);
        // navigation.setCheckedItem(R.id.item_settings);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.drawer_item_setArea:
                        Intent iSetArea = new Intent(WeatherActivity.this, ChooseAreaActivity.class);
                        // 去选择地区
                        startActivityForResult(iSetArea, 1);
                        break;
                    case R.id.drawer_item_settings:
                        Intent iGoSetting = new Intent(WeatherActivity.this, SettingsActivity.class);
                        startActivity(iGoSetting);
                        break;
                    case R.id.drawer_item_github:
                        Intent iGoGithub = new Intent(Intent.ACTION_VIEW);
                        iGoGithub.setData(Uri.parse(Conf.GITHUB_ADDRESS));
                        startActivity(iGoGithub);
                        break;
                    default:
                        break;
                }
                //navigation.setCheckedItem(item.getItemId());
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    // 接收更改地区的 Intent 返回值
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

    // 更新天气，保存设置，更新界面
    private void requestWeather(String weatherId) {
        final String weatherUrl = WEATHER_API_URL + weatherId + KEY;

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
                        }
                        weatherScrollView.setVisibility(View.VISIBLE);
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
                        Toast.makeText(WeatherActivity.this, "Get weather information failed"
                                , Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBgImage();
    }

    private void showWeatherInfo(Weather weather) {
        titleCityText.setText(weather.basic.cityName);
        titleUpdateTimeText.setText(weather.basic.update.updateTime);
        temperatureText.setText(weather.now.temperature + "℃");
        weatherStatusText.setText(weather.now.condition.info + "天");
        /* 天气详细信息开始 */
        temperatureDetailText.setText(weather.now.temperature + "℃");
        infoDetailText.setText(weather.now.condition.info);
        fellingDetailText.setText(weather.now.felling + "℃");
        pressureDetailText.setText(weather.now.pressure);
        humidityDetailText.setText(weather.now.humidity + "%");
        precipitationDetailText.setText(weather.now.precipitation + "mm");
        visibilityDetailText.setText(weather.now.visibility + "km");
        degreeDetailText.setText(weather.now.wind.degree);
        directionDetailText.setText(weather.now.wind.direction);
        windforceDetailText.setText(weather.now.wind.windforce + "级");
        speedDetailText.setText(weather.now.wind.speed + "kmph");
        /* 天气详细信息结束 */
        // 解析几小时预报
        hourlyForecastLayout.removeAllViews();
        for (HourlyForecast hourlyForecast : weather.hourlyForecastList) {
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.frag_weather_hourly_forecast_item, hourlyForecastLayout, false);
            TextView timeText = (TextView) view.findViewById(R.id.textView_hfTime);
            TextView statusText = (TextView) view.findViewById(R.id.textView_hfStatus);
            TextView tempe = (TextView) view.findViewById(R.id.textView_hfTempe);
            TextView probability = (TextView) view.findViewById(R.id.textView_hfPop);
            timeText.setText(hourlyForecast.date.split(" ")[1]);
            statusText.setText(hourlyForecast.condition.info);
            tempe.setText(hourlyForecast.temperature + "℃");
            probability.setText(hourlyForecast.probability + "%");
            hourlyForecastLayout.addView(view);
        }
        // 解析几天预报
        dailyForecastLayout.removeAllViews();
        for (DailyForecast dailyForecast : weather.dailyForecastList) {
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.frag_weather_daily_forecast_item, dailyForecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.textView_fDate);
            TextView infoText = (TextView) view.findViewById(R.id.textView_fStatus);
            TextView maxText = (TextView) view.findViewById(R.id.textView_tMax);
            TextView minText = (TextView) view.findViewById(R.id.textView_tMin);
            dateText.setText(dailyForecast.date);
            infoText.setText(dailyForecast.condition.info);
            maxText.setText(dailyForecast.temperature.max + "℃");
            minText.setText(dailyForecast.temperature.min + "℃");
            dailyForecastLayout.addView(view);
        }
        // 解析AQI指数
        LinearLayout LinearLayoutAqi = (LinearLayout) findViewById(R.id.LinearLayout_aqi);
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
            LinearLayoutAqi.setVisibility(View.VISIBLE);
        } else {
            LinearLayoutAqi.setVisibility(View.GONE);
        }
        comfortText.setText(weather.suggestion.comfort.info);
        carWashText.setText(weather.suggestion.carWash.info);
        sportText.setText(weather.suggestion.sport.info);
        weatherScrollView.setVisibility(View.VISIBLE);

        // 启动后台天气自动更新服务
        Intent intent = new Intent(this, UpdateWeatherService.class);
        startService(intent);
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
