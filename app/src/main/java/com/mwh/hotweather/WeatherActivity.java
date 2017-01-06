package com.mwh.hotweather;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mwh.hotweather.gson.Forecast;
import com.mwh.hotweather.gson.Weather;
import com.mwh.hotweather.util.ACache;
import com.mwh.hotweather.util.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Response;

import static com.lzy.okgo.cache.CacheMode.FIRST_CACHE_THEN_REQUEST;

/**
 * 显示天信息
 */
public class WeatherActivity extends AppCompatActivity {

    @BindView(R.id.title_city)
    TextView titleCity;
    @BindView(R.id.title_update_time)
    TextView titleUpdateTime;
    @BindView(R.id.degree_text)
    TextView degreeText;
    @BindView(R.id.weather_info_text)
    TextView weatherInfoText;
    @BindView(R.id.forecast_layout)
    LinearLayout forecastLayout;
    @BindView(R.id.aqi_text)
    TextView aqiText;
    @BindView(R.id.pm25_text)
    TextView pm25Text;
    @BindView(R.id.comfort_text)
    TextView comfortText;
    @BindView(R.id.car_wash_text)
    TextView carWashText;
    @BindView(R.id.sport_text)
    TextView sportText;
    @BindView(R.id.weather_layout)
    ScrollView weatherLayout;
    @BindView(R.id.activity_weather)
    FrameLayout activityWeather;
    @BindView(R.id.qlty_text)
    TextView qltyText;
    @BindView(R.id.bg_img)
    ImageView bgImg;

    private Context mContext;
    private ACache mCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //透明状态栏
        if (Build.VERSION.SDK_INT>=21){
            //5.0的系统才执行
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//活动不仅显示在状态栏上面
            getWindow().setStatusBarColor(Color.TRANSPARENT);//设置成透明
        }
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);
        mContext=this;
        mCache=ACache.get(mContext);

        //无缓存就从网络获取
        String weatherId = mCache.getAsString("weather_id");
        weatherLayout.setVisibility(View.INVISIBLE);
        requestWeather(weatherId);

        //获得背景图片
        loadBgPic();
    }

    /**
     * 从服务器请求天气数据
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        OkGo.get(weatherUrl)
                .cacheMode(FIRST_CACHE_THEN_REQUEST)
                .cacheKey("weather")
                .cacheTime(86400000)//一天
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {

                        Weather weather = Utility.handleWeatherResponse(s);
                        if (weather != null && "ok".equals(weather.status)) {
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCacheSuccess(String s, Call call) {
                        //有缓存就从缓存中读取
                        onSuccess(s, call, null);
                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
        loadBgPic();
    }

    /**
     * 显示实体类中的数据
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast :
                weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dataText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dataText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            qltyText.setText(weather.aqi.city.qlty);
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度: " + weather.suggestion.comfort.info;
        String carWash = "洗车指数: " + weather.suggestion.carWash.info;
        String sport = "运动建议: " + weather.suggestion.sport.info;

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

    }

    /**
     * 从服务器获得背景图片
     */
    private void loadBgPic(){
        String prcUrl="http://guolin.tech/api/bing_pic";
        OkGo.get(prcUrl)
                .cacheKey("bg_img_url")
                .cacheMode(FIRST_CACHE_THEN_REQUEST)
                .cacheTime(86400000)//一天
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        //加载图片
                        Glide.with(mContext).load(s).into(bgImg);
                    }

                    @Override
                    public void onCacheSuccess(String s, Call call) {
                        onSuccess(s,call,null);
                    }
                });
    }
}
