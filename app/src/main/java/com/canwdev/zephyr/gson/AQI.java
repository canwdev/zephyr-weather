package com.canwdev.zephyr.gson;

import com.google.gson.annotations.SerializedName;

// 空气质量
public class AQI {
    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
        @SerializedName("qlty")
        public String quality;
    }
}
