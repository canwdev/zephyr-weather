package com.canwdev.zephyr.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// @All of the JSON Data 天气类，全部信息
public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;

    @SerializedName("hourly_forecast")
    public List<HourlyForecast> hourlyForecastList;

    @SerializedName("daily_forecast")
    public List<DailyForecast> dailyForecastList;
}
