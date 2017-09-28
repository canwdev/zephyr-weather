package com.example.myweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    // 映射JSON字段与Java字段
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
