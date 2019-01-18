package com.qilinkeji.libsocket.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.qilinkeji.libsocket.global.Constants
import com.qilinkeji.qilinsync.global.QiLinSync
import com.qilinkeji.qilinsync.utils.LogUtils
import java.util.*

/**
 * @author zhangbo
 * @date 2018/8/15
 */
class CalcKmService : Service() {

    val TAG = CalcKmService::class.java.simpleName
    var orderId: Int? = -1
    private lateinit var locationClient: AMapLocationClient
    private lateinit var locationOption: AMapLocationClientOption
    private lateinit var mLocationMap: HashMap<String, Any>
    private val map by lazy { hashMapOf<String, Any>() }
    private val locationMap by lazy { hashMapOf<String, Any>() }

    override fun onCreate() {
        super.onCreate()
        initLocation()
        mLocationMap = HashMap(3)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        orderId = intent?.getIntExtra(Constants.CURRENT_ORDER_ID, -1)
        startLocation()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyLocation()
    }


    /**
     * 初始化定位
     *
     * @since 2.8.0
     * @author hongming.wang
     */
    private fun initLocation() {
        locationClient = AMapLocationClient(this.applicationContext)
        locationOption = getDefaultOption()
        locationClient.setLocationOption(locationOption)
        locationClient.setLocationListener(locationListener)
    }

    /**
     * 默认的定位参数
     * @since 2.8.0
     * @author hongming.wang
     */
    private fun getDefaultOption(): AMapLocationClientOption {
        val mOption = AMapLocationClientOption()
        mOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.isGpsFirst = false//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.httpTimeOut = 30000//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.interval = 2000//可选，设置定位间隔。默认为2秒
        mOption.isNeedAddress = true//可选，设置是否返回逆地理地址信息。默认是true
        mOption.isOnceLocation = false//可选，设置是否单次定位。默认是false
        mOption.isOnceLocationLatest = false//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP)//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.isSensorEnable = false//可选，设置是否使用传感器。默认是false
        mOption.isWifiScan = true //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.isLocationCacheEnable = true //可选，设置是否使用缓存定位，默认为true
        mOption.geoLanguage = AMapLocationClientOption.GeoLanguage.DEFAULT//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
        return mOption
    }


    /**
     * 定位监听
     */
    private var locationListener: AMapLocationListener = AMapLocationListener { location ->
        if (null != location) {
//            sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
//            sb.append("角    度    : " + location.getBearing() + "\n");
            sendMessageToSocket(location.longitude, location.latitude, location.speed, location.bearing, location.time, location.provider, location.accuracy, location.address)


        }
    }


    private var preLongitude = 0.0
    private var preLatitude = 0.0
    private fun sendMessageToSocket(longitude: Double, latitude: Double, speed: Float, bearing: Float, time: Long, provider: String, accuracy: Float, address: String) {
        if (longitude != 0.0 && latitude != 0.0 && orderId != -1 && preLatitude != latitude && preLongitude != longitude) {
            preLatitude = latitude
            preLongitude = longitude
            locationMap.clear()
            map.clear()
            locationMap["lat"] = latitude
            locationMap["lng"] = longitude
            locationMap["speed"] = speed
            locationMap["bearing"] = bearing
            locationMap["time"] = time
            locationMap["provider"] = provider
            locationMap["accuracy"] = accuracy
            locationMap["address"] = address
            map[System.currentTimeMillis().toString()] = locationMap
            QiLinSync.getInstance().updateChildren(Constants.NODE_ORDER_LOCATION_PRE + "$orderId", map)
        }
    }


    /**
     * 开始定位
     *
     * @since 2.8.0
     * @author hongming.wang
     */
    private fun startLocation() {
        // 设置定位参数
        locationClient.setLocationOption(locationOption)
        // 启动定位
        locationClient.startLocation()
        LogUtils.d("startLocation")
    }

    /**
     * 停止定位
     *
     * @since 2.8.0
     * @author hongming.wang
     */
    private fun stopLocation() {
        // 停止定位
        locationClient.stopLocation()
    }

    /**
     * 销毁定位
     *
     * @since 2.8.0
     * @author hongming.wang
     */
    private fun destroyLocation() {
        locationClient.onDestroy()
        LogUtils.d("destroyLocation")
    }
}