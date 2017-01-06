package com.mwh.hotweather.gson;

/**
 * Created by Youg on 2017/1/6 13:24
 */

public class AQI {

    public AQICity city;

    public class AQICity{
        public String aqi;
        public String pm25;
        public String qlty;
    }

}
