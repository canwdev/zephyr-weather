package com.canwdev.zephyr.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Conf {
    public static final String PREF_FILE_NAME = "all_settings";
    public static final String PREF_AREA_NAME = "countyName";
    public static final String PREF_WEATHER_ID = "weatherId";
    public static final String PREF_API_KEY = "apiKey";
    public static final String PREF_BG_URL = "bgUrl";
    public static final String PREF_WEATHER_SAVE = "weatherCache";
    public static final String PREF_ENABLE_SERVICE = "enableService";
    public static final String PREF_ENABLE_BG_IMAGE = "enableBgImage";

    // 天气接口，示例：https://free-api.heweather.com/v5/weather?city=yourcity&key=yourkey
    public static final String WEATHER_API_URL = "https://free-api.heweather.com/v5/weather?";
    // 地区查询接口，示例：https://api.heweather.com/v5/search?city=yourcity&key=yourkey
    public static final String HEWEATHER_SEARCH_AREA_API = "https://api.heweather.com/v5/search?";
    public static final String HEWEATHER_CITY_SAMPLE = "city=CN101240213";
    public static final String HEWEATHER_CITY_SAMPLE_NULL = "city=null";
    public static final String HEWEATHER_CHINA_CITY_LIST = "https://cdn.heweather.com/china-city-list.txt";
    public static final String HEWEATHER_CITY_LIST = "https://www.heweather.com/documents/city";
    public static final String HEWEATHER_REGISTER = "http://console.heweather.com/register";
    public static final String HEWEATHER_API_KEY = "74ac2716affa4279b5db3898be81c25d";
    public static final String GITHUB_ADDRESS = "https://github.com/canwdev/zephyr-weather";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String WIDGET_CLOCK_TIME_FORMAT = "HH:mm";
    public static final String WIDGET_CLOCK_DATE_FORMAT = "yyyy-MM-dd";
    public static final int WEATHER_SERVER_UPDATE_MS = 30*60*1000;
    public static final double WEATHER_UPDATE_HOURS = 1.03;

    public static String getKey(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Conf.PREF_FILE_NAME, context.MODE_PRIVATE);
        String setKey = pref.getString(Conf.PREF_API_KEY, "");
        if (!setKey.isEmpty()) {
            return setKey;
        } else {
            return HEWEATHER_API_KEY;
        }
    }
}
