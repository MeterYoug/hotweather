package com.mwh.hotweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Youg on 2017/1/6 13:25
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More{

        @SerializedName("txt")
        public String info;
    }
}
