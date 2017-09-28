package com.canwdev.zephyr.gson;

import com.google.gson.annotations.SerializedName;

// 今日天气
public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public Condition condition;

    public class Condition {
        @SerializedName("txt")
        public String info;
    }

    @SerializedName("fl")
    public String felling;

    @SerializedName("hum")
    public String humidity;

    @SerializedName("pcpn")
    public String precipitation;

    @SerializedName("vis")
    public String visibility;

    public Wind wind;

    public class Wind {
        @SerializedName("deg")
        public String degree;
        @SerializedName("dir")
        public String direction;
        @SerializedName("sc")
        public String windforce;
        @SerializedName("spd")
        public String speed;
    }
}
