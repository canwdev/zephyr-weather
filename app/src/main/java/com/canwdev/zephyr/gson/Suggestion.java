package com.canwdev.zephyr.gson;

import com.google.gson.annotations.SerializedName;

// 生活建议
public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;

    public class Comfort {
        @SerializedName("brf")
        public String title;

        @SerializedName("txt")
        public String info;
    }

    @SerializedName("cw")
    public CarWash carWash;

    public class CarWash {
        @SerializedName("brf")
        public String title;

        @SerializedName("txt")
        public String info;
    }

    @SerializedName("drsg")
    public Wearing wearing;

    public class Wearing {
        @SerializedName("brf")
        public String title;

        @SerializedName("txt")
        public String info;
    }

    @SerializedName("flu")
    public Influenza influenza;

    public class Influenza {
        @SerializedName("brf")
        public String title;

        @SerializedName("txt")
        public String info;
    }

    public Sport sport;

    public class Sport {
        @SerializedName("brf")
        public String title;

        @SerializedName("txt")
        public String info;
    }

    @SerializedName("trav")
    public Travel travel;

    public class Travel {
        @SerializedName("brf")
        public String title;

        @SerializedName("txt")
        public String info;
    }

    @SerializedName("uv")
    public Uv uv;

    public class Uv {
        @SerializedName("brf")
        public String title;

        @SerializedName("txt")
        public String info;
    }
}
