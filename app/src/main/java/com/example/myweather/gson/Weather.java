package com.example.myweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// @All of the JSON Data
public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
