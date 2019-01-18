package com.qilinkeji.libsocket.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.location.AMapLocationQualityReport
import com.qilinkeji.libsocket.global.App
import com.qilinkeji.libsocket.global.Constants
import com.qilinkeji.qilinsync.global.QiLinSync
import com.qilinkeji.qilinsync.utils.LogUtils
import java.util.*


/**
 * @author zhangbo
 * @date 2018/7/27
 */
class LocationService : Service() {

    val TAG = LocationService::class.java.simpleName

    private lateinit var locationClient: AMapLocationClient
    private lateinit var locationOption: AMapLocationClientOption
    private lateinit var mLocationMap: HashMap<String, Any>

    override fun onCreate() {
        super.onCreate()
        initLocation()
        mLocationMap = HashMap(2)
    }


    private val binder = MyBinder()


    inner class MyBinder : Binder() {
        public fun getService(): LocationService {
            return this@LocationService
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocation()
        return super.onStartCommand(intent, flags, startId)
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
            sendMessageToSocket(location.longitude, location.latitude, location.time)
        }
    }

    /**
     * 获取GPS状态的字符串
     * @param statusCode GPS状态码
     * @return
     */
    private fun getGPSStatusString(statusCode: Int): String {
        var str = ""
        when (statusCode) {
            AMapLocationQualityReport.GPS_STATUS_OK -> str = "GPS状态正常"
            AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER -> str = "手机中没有GPS Provider，无法进行GPS定位"
            AMapLocationQualityReport.GPS_STATUS_OFF -> str = "GPS关闭，建议开启GPS，提高定位质量"
            AMapLocationQualityReport.GPS_STATUS_MODE_SAVING -> str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量"
            AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION -> str = "没有GPS定位权限，建议开启gps定位权限"
        }
        return str
    }

    val locationList = arrayListOf<Any>()

    private fun sendMessageToSocket(longitude: Double, latitude: Double, time: Long) {
        if (longitude != 0.0 && latitude != 0.0) {
            locationList.clear()
            locationList.add(longitude)
            locationList.add(latitude)
            mLocationMap["time"] = time
            mLocationMap["location"] = locationList
            QiLinSync.getInstance().updateGeoLocation(Constants.NODE_LOCATION_GEO_UPDATE + App.DRIVER_ID, mLocationMap)
        }
    }


    /**
     * 开始定位
     *
     * @since 2.8.0
     * @author hongming.wang
     */
    public fun startLocation() {
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
    public fun stopLocation() {
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