package com.canwdev.zephyr.gson;

import com.google.gson.annotations.SerializedName;

public class HourlyForecast {
    public String date;

    @SerializedName("cond")
    public Condition condition;

    public class Condition {
        @SerializedName("txt")
        public String info;
    }

    @SerializedName("hum")
    public String humidity;

    @SerializedName("pop")
    public String probability;

    @SerializedName("pres")
    public String pressure;

    @SerializedName("tmp")
    public String temperature;

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
