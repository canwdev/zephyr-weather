package com.canwdev.zephyr.util;

import android.text.TextUtils;

import com.canwdev.zephyr.db.City;
import com.canwdev.zephyr.db.County;
import com.canwdev.zephyr.db.Province;
import com.canwdev.zephyr.db.RecentArea;
import com.canwdev.zephyr.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.List;

public class Utility {

    private static final String TAG = "Utility!!";

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
}
