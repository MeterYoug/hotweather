package com.mwh.hotweather;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mwh.hotweather.util.ACache;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private ACache mCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext=this;
        mCache=ACache.get(mContext);
        String weather_id=mCache.getAsString("weather_id");
        if (weather_id!=null){
            Intent intent=new Intent(this,WeatherActivity.class);
            intent.putExtra("weather_id",weather_id);
            startActivity(intent);
            finish();
        }

    }
}
