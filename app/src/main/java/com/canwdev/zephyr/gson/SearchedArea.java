package com.canwdev.zephyr.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchedArea {
    public List<More> HeWeather5;

    public class More {
        public String status;
        public Basic basic;

        public class Basic {
            @SerializedName("city")
            public String cityName;
            @SerializedName("cnty")
            public String countryName;
            @SerializedName("id")
            public String weatherId;
            @SerializedName("prov")
            public String provinceName;
        }
    }
}
