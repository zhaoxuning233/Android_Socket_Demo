package com.qilinkeji.libsocket.travel;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.trace.TraceLocation;
import com.qilinkeji.qilinsync.utils.GsonUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zxn
 */
public class TravelService extends Service {

    private AMapLocationClient locationClient = null;
    private TravelBinder mBinder;
    private LocationChangeListener mLocationChangeListener;
    private int count = 1;
    private boolean hadStartService = false;
    private ScheduledExecutorService executorService;
    private static final int SECONDS = 60;
    private List<TraceLocation> mTraceList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new TravelBinder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class TravelBinder extends Binder {
        public TravelService getService() {
            return TravelService.this;
        }

        public void start() {
            Toast.makeText(getApplicationContext(), "start", Toast.LENGTH_SHORT).show();
            startLocation();
        }

        public void pause() {
            Toast.makeText(getApplicationContext(), "pause", Toast.LENGTH_SHORT).show();
            pauseLocation();
        }

        public void stop() {
            Toast.makeText(getApplicationContext(), "stop", Toast.LENGTH_SHORT).show();
            stopLocation();
        }

        public void restart() {
            Toast.makeText(getApplicationContext(), "restart", Toast.LENGTH_SHORT).show();
            startLocation();
        }

        public void startTiming() {
            if (!hadStartService) {
                startScheduler();
            }
        }

        public void stopTiming() {
            hadStartService = false;
            if (executorService != null) {
                executorService.shutdown();
            }
        }
    }

    private void startScheduler() {
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                // TODO: 2018/8/21  在定时器中还要完成的业务逻辑
                if (count % SECONDS == 0) {
                    // TODO: 2018/8/21 每60秒做一次持久化 ，防止计时器数据丢失
                }
                mLocationChangeListener.timeChange(count);
                count++;
            }
        }, 0, 1, TimeUnit.SECONDS);

        hadStartService = true;
    }

    public void setLocationListener(LocationChangeListener locationListener) {
        this.mLocationChangeListener = locationListener;
    }


    public void removeLocationListener() {
        this.mLocationChangeListener = null;
    }

    /**
     * 异步获取定位结果
     */
    AMapLocationListener mAMapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    try {
                        //获取到经纬度的Bean对象
                        LocationBean locationBean = new LocationBean(amapLocation.getLongitude(), amapLocation.getLatitude(), amapLocation.getPoiName(), "");
                        //locationBean转为json
                        String locationJsonData = GsonUtils.getInstance().toJson(locationBean);
                        // TODO: 更新数据库 ShareData.UpdateLocationData(TravelService.this, locationJsonData);

                        // 获取当前定位点基础信息
                        int satellites = amapLocation.getSatellites();
                        double currentLatitude = amapLocation.getLatitude();
                        double currentLongitude = amapLocation.getLongitude();
                        float bearing = amapLocation.getBearing();
                        float speed = amapLocation.getSpeed();
                        long time = amapLocation.getTime();

                        // 构建TraceLocation对象
                        TraceLocation traceLocation = new TraceLocation();
                        traceLocation.setBearing(bearing);
                        traceLocation.setSpeed(speed);
                        traceLocation.setTime(time);
                        traceLocation.setLatitude(currentLatitude);
                        traceLocation.setLongitude(currentLongitude);

                        // 构建List存储TraceLocation点信息
                        mTraceList.add(traceLocation);

                        // 把当前计算信息写日志
                        TravelLocationUtil.getInstance(TravelService.this).writefile("filename", "content");
                        // 计算总距离

                        double travelForM = DistanceCalculationUtil.getInstert(TravelService.this, "", currentLatitude, currentLongitude, satellites).filter();

                        // TODO: 2018/8/20 更新长连接中的各种状态（司机，订单）


                        // 回调监听，更新主页面ui
                        if (mLocationChangeListener != null) {
                            mLocationChangeListener.poiNameChange(amapLocation.getPoiName());
//                            mLocationChangeListener.travelForMChange(travelForM);
                            mLocationChangeListener.travelLocation(currentLatitude, currentLongitude, satellites);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };


    /**
     * AMapLocationClientOption初始化
     */
    private AMapLocationClientOption getdefLocationOption(long time) {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        //可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setGpsFirst(false);
        //可选，设置定位间隔。默认为3秒
        mOption.setInterval(time);
        //可选，设置是否返回逆地理地址信息。默认是true
        mOption.setNeedAddress(true);
        //可选，设置是否单次定位。默认是false
        mOption.setOnceLocation(false);
        //可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        mOption.setOnceLocationLatest(false);
        //可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);
        //可选，设置是否使用传感器。默认是false
        mOption.setSensorEnable(false);
        //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setWifiScan(true);
        //可选，设置是否使用缓存定位，默认为true
        mOption.setLocationCacheEnable(true);
        return mOption;
    }


    /**
     * AMapLocationClient初始化
     */
    private void setgeolocation() {
        locationClient = new AMapLocationClient(this);
        //初始化定位参数
        AMapLocationClientOption locationOption = getdefLocationOption(3000);
        //设置定位监听
        locationClient.setLocationListener(mAMapLocationListener);
        //设置定位参数
        locationClient.setLocationOption(locationOption);
    }

    public void startLocation() {
        if (locationClient == null) {
            setgeolocation();
        }
        if (!locationClient.isStarted()) {
            //启动定位
            locationClient.startLocation();
        }
    }

    public void pauseLocation() {
        locationClient.stopLocation();
    }

    public void stopLocation() {
        locationClient.stopLocation();
        locationClient.onDestroy();
    }
}
