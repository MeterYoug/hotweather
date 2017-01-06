package com.mwh.hotweather;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import com.mwh.hotweather.service.AutoUpdateService;
import com.mwh.hotweather.util.ACache;
import com.mwh.hotweather.util.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Response;

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
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.nav_button)
    Button navButton;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    private Context mContext;
    private ACache mCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //透明状态栏
        if (Build.VERSION.SDK_INT >= 21) {
            //5.0的系统才执行
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//活动不仅显示在状态栏上面
            getWindow().setStatusBarColor(Color.TRANSPARENT);//设置成透明
        }
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);
        mContext = this;
        mCache = ACache.get(mContext);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        final String weatherId = mCache.getAsString("weather_id");
        String weather = mCache.getAsString("weather");
        if (!TextUtils.isEmpty(weather)) {
            Weather weathers = Utility.handleWeatherResponse(weather);
            showWeatherInfo(weathers);
        } else {
            //无缓存就从网络获取
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        String pic_url = mCache.getAsString("pic_url");
        if (!TextUtils.isEmpty(pic_url)) {
            //读取本地地址
            Glide.with(this).load(pic_url).into(bgImg);
        } else {
            //获得网络图片地址
            loadBgPic();
        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

        //打开侧滑菜单
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * 从服务器请求天气数据
     *
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        OkGo.get(weatherUrl)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        mCache.put("weather", s);
                        Weather weather = Utility.handleWeatherResponse(s);
                        if (weather != null && "ok".equals(weather.status)) {
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
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
        if (weather != null && "ok".equals(weather.status)) {
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
                dataText.setText(forecast.date.substring(5));
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

            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        } else {
            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * 从服务器获得背景图片
     */
    private void loadBgPic() {
        String prcUrl = "http://guolin.tech/api/bing_pic";
        OkGo.get(prcUrl)
//                .cacheKey("bg_img_url")
//                .cacheMode(FIRST_CACHE_THEN_REQUEST)
//                .cacheTime(86400000)//一天
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        mCache.put("pic_url", s);
                        //加载图片
                        Glide.with(mContext).load(s).into(bgImg);
                    }

                });
    }
}
