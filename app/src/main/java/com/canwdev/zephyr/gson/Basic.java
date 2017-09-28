package com.canwdev.zephyr.gson;

import com.google.gson.annotations.SerializedName;

// 基础数据
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
