package com.canwdev.zephyr.gson;

import com.google.gson.annotations.SerializedName;

// 天气预报
public class DailyForecast {
    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public Condition condition;

    public class Temperature {
        public String max;
        public String min;
    }

    public class Condition {
        @SerializedName("txt_d")
        public String info;
    }
}
