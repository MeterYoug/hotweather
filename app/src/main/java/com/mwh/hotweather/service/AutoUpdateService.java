package com.mwh.hotweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mwh.hotweather.util.ACache;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 自动更新数据服务
 */
public class AutoUpdateService extends Service {
    private ACache mCache;

    public AutoUpdateService() {

    }

    @Override
    public IBinder onBind(Intent intent) {

        throw null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mCache = ACache.get(this);
        updateWeather();
        updateBingPic();
        AlarmManager manager= (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour=8*60*60*1000;//8小时
        long triggerAtTiem= SystemClock.elapsedRealtime()+anHour;
        Intent i=new Intent(this,AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTiem,pi);

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息
     */
    private void updateWeather() {
        String weatherId = mCache.getAsString("weather_id");
        if (weatherId != null) {
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
            OkGo.get(weatherUrl)
                    .tag(this)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(String s, Call call, Response response) {
                            mCache.put("weather", s);

                        }

                        @Override
                        public void onError(Call call, Response response, Exception e) {
                            super.onError(call, response, e);
                        }
                    });
        }
    }

    private void updateBingPic() {
        String prcUrl = "http://guolin.tech/api/bing_pic";
        OkGo.get(prcUrl)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        mCache.put("pic_url", s);
                    }

                });
    }
}
