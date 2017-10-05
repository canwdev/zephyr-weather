package com.canwdev.zephyr.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.canwdev.zephyr.R;
import com.canwdev.zephyr.db.City;
import com.canwdev.zephyr.db.County;
import com.canwdev.zephyr.db.Province;
import com.canwdev.zephyr.db.RecentArea;
import com.canwdev.zephyr.gson.SearchedArea;
import com.canwdev.zephyr.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Utility {

    private static final String TAG = "Utility!!";
    Weather weather;
    Context context;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public Utility(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(Conf.PREF_FILE_NAME, context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // 解析JSON的省级数据，保存到数据库
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                //DataSupport.deleteAll(Province.class);

                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //解析JSON市级数据
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                //DataSupport.deleteAll(City.class);
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObjcet = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObjcet.getString("name"));
                    city.setCityCode(cityObjcet.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // 解析JSON县级数据
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                //DataSupport.deleteAll(County.class);
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setCityId(countyObject.getInt("id"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // 将返回的天气 JSON 解析成 Weather 对象实体
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather5");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 记录历史地区列表
    public static void recordRecentArea(String weatherId, String areaName) {
        final int historyCount = 5;
        final List<RecentArea> dbWeatherId = DataSupport.where("weatherid = ?", weatherId).find(RecentArea.class);

        // 如果已存在
        if (dbWeatherId.size() > 0) {
            final String lastWeatherId = dbWeatherId.get(0).getWeatherId();
            if (weatherId.equals(lastWeatherId)) {
                // 删除重复，并将最新选择的置顶
                List<RecentArea> recentAreaList = DataSupport.findAll(RecentArea.class);
                for (RecentArea area : recentAreaList) {
                    if (area.getWeatherId().equals(weatherId)) {
                        DataSupport.delete(RecentArea.class, area.getId());
                    }
                }
                if (recentAreaList.size() > historyCount) {
                    for (int i = 0; i <= recentAreaList.size() - historyCount; i++) {
                        DataSupport.delete(RecentArea.class, recentAreaList.get(i).getId());
                    }
                }
                RecentArea recentArea = new RecentArea();
                recentArea.setWeatherId(weatherId);
                recentArea.setAreaName(areaName);
                recentArea.save();
            }

        } else {
            // 删除多余历史
            List<RecentArea> recentAreaList = DataSupport.findAll(RecentArea.class);
            for (int i = 0; i <= recentAreaList.size() - historyCount; i++) {
                DataSupport.delete(RecentArea.class, recentAreaList.get(i).getId());
            }
            RecentArea recentArea = new RecentArea();
            recentArea.setWeatherId(weatherId);
            recentArea.setAreaName(areaName);
            recentArea.save();
        }


    }

    // 解析查询地区
    public static SearchedArea handleSearchAreaResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather5");
            String areaString = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(areaString, SearchedArea.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获取API返回的错误信息，返回String
    public static String getWeatherErrMsg(Context context, String errorMessage) {

        switch (errorMessage) {
            case "invalid key":
                errorMessage = context.getString(R.string.err_invalid_key);
                break;
            case "unknown city":
                errorMessage = context.getString(R.string.err_invalid_city);
                break;
            case "no data for this location":
                errorMessage = context.getString(R.string.err_no_area_data);
                break;
            case "no more requests":
                errorMessage = context.getString(R.string.err_no_more_requests);
                break;
            case "param invalid":
                errorMessage = context.getString(R.string.err_param_invalid);
                break;
            default:
                errorMessage = context.getString(R.string.err_unknow_error);
                break;
        }
        return errorMessage;
    }

    /*
     * 判断服务是否启动,context上下文对象 ，className服务的name
     */
    public static boolean isServiceRunning(Context mContext, String className) {

        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(100);

        if (!(serviceList.size() > 0)) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            // Log.d(TAG, "isServiceRunning: "+i+". "+serviceList.get(i).service.getClassName());
            if (serviceList.get(i).service.getClassName().equals(className)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    // 根据设置判断获取天气
    public Weather getWeather() {
        String weatherCache = pref.getString(Conf.PREF_WEATHER_SAVE, null);
        String setWeatherId = pref.getString(Conf.PREF_WEATHER_ID, null);
        // 有缓存时直接解析天气数据
        if (weatherCache != null) {
            Weather cWeather = Utility.handleWeatherResponse(weatherCache);
            // 检查城市id是否存在
            if (setWeatherId != null) {
                if (cWeather != null && "ok".equals(cWeather.status)) {
                    // 检查默认地区设置是否一致
                    if (!cWeather.basic.weatherId.equals(setWeatherId)) {
                        // 不一致
                        return requestWeather(setWeatherId);
                    } else {
                        // 如果缓存时间与系统时间相差大于-小时，则更新
                        SimpleDateFormat dateFormat = new SimpleDateFormat(Conf.DATE_TIME_FORMAT);
                        try {
                            Date cachedUpdateTime = dateFormat.parse(cWeather.basic.update.updateTime);
                            Date SystemTime = new Date();
                            long diff = SystemTime.getTime() - cachedUpdateTime.getTime();
                            double hours = (double) diff / (1000 * 60 * 60);
                            Log.d(TAG, "hours=" + hours + " WEATHER_UPDATE_HOURS=" + Conf.WEATHER_UPDATE_HOURS);
                            if (hours > Conf.WEATHER_UPDATE_HOURS) {
                                return requestWeather();
                            } else {
                                Log.d(TAG, "getWeather: cWeather");
                                return cWeather;
                            }
                        } catch (ParseException e) {
                            Log.e(TAG, "ParseException: " + e.getMessage(), e);
                            e.printStackTrace();
                        }
                    }
                }
            }
            return requestWeather();

        } else {
            // 无缓存时去服务器查询天气
            return requestWeather();
        }
    }

    // 根据 weatherId 请求天气
    public Weather requestWeather(String weatherId) {
        setBingPic();
        Log.d(TAG, "requestWeather: from server by weatherId");
        String cityWeatherId = "city=" + weatherId;
        String apiKey = "&key=" + Conf.getKey(context);
        final String weatherUrl = Conf.WEATHER_API_URL + cityWeatherId + apiKey;
        Thread requestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(weatherUrl)
                            .build();
                    Response response = client.newCall(request).execute();

                    final String responseText = response.body().string();
                    weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null && "ok".equals(weather.status)) {
                        editor.putString(Conf.PREF_WEATHER_SAVE, responseText);
                        editor.apply();
                        Utility.recordRecentArea(weather.basic.weatherId, weather.basic.cityName);
                    } else if (weather != null) {
                        Log.e(TAG, Utility.getWeatherErrMsg(context, weather.status));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        requestThread.start();
        try {
            requestThread.join();
            return weather;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 根据设置请求天气
    public Weather requestWeather() {
        setBingPic();
        Log.d(TAG, "requestWeather: from server by settings");
        String setCityWeatherId = "city=" + pref.getString(Conf.PREF_WEATHER_ID, null);
        String apiKey = "&key=" + Conf.getKey(context);
        final String weatherUrl = Conf.WEATHER_API_URL + setCityWeatherId + apiKey;
        Thread requestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(weatherUrl)
                            .build();
                    Response response = client.newCall(request).execute();

                    final String responseText = response.body().string();
                    weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null && "ok".equals(weather.status)) {
                        editor.putString(Conf.PREF_WEATHER_SAVE, responseText);
                        editor.apply();
                        Utility.recordRecentArea(weather.basic.weatherId, weather.basic.cityName);
                    } else if (weather != null) {
                        // 显示错误信息
                        Log.e(TAG, Utility.getWeatherErrMsg(context, weather.status));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        requestThread.start();
        // 线程运行完成后才返回值
        try {
            requestThread.join();
            return weather;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获取 Bing 每日一图地址，保存设置
    public void setBingPic() {
        final String bingPicApiUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(bingPicApiUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPicUrl = response.body().string();
                editor.putString(Conf.PREF_BG_URL, bingPicUrl);
                editor.apply();
            }
        });
    }
}
