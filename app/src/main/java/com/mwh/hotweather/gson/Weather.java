package com.mwh.hotweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Youg on 2017/1/6 13:33
 */

public class Weather {

    public String status;

    public Basic basic;

    public AQI aqi;

    public  Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
