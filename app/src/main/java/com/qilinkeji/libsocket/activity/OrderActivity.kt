package com.qilinkeji.libsocket.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.qilinkeji.libsocket.R
import com.qilinkeji.libsocket.bean.BaseBean
import com.qilinkeji.libsocket.global.App
import com.qilinkeji.libsocket.global.Constants
import com.qilinkeji.libsocket.global.Urls
import com.qilinkeji.libsocket.service.CalcKmService
import com.qilinkeji.libsocket.travel.DistanceCalculationUtil
import com.qilinkeji.libsocket.travel.LocationChangeListener
import com.qilinkeji.libsocket.travel.TravelService
import com.qilinkeji.qilinsync.global.QiLinSync
import com.qilinkeji.qilinsync.utils.GsonUtils
import com.qilinkeji.qilinsync.utils.LogUtils
import kotlinx.android.synthetic.main.activity_order.*
import top.limuyang2.customldialog.IOSMsgDialog

class OrderActivity : BaseActivity(), LocationChangeListener {


    private val calcKmService by lazy { Intent(applicationContext, CalcKmService::class.java) }

    private val orderId by lazy { intent.getIntExtra(Constants.CURRENT_ORDER_ID, -1) }

    val serviceIntent by lazy { Intent(this, TravelService::class.java) }

    var cancelDialog: IOSMsgDialog? = null

    private var mTravelBinder: TravelService.TravelBinder? = null

    private val aMap: AMap by lazy { map.map }

    companion object {
        fun start(activity: AppCompatActivity, orderId: Int) {
            val intent = Intent(activity, OrderActivity::class.java)
            intent.putExtra(Constants.CURRENT_ORDER_ID, orderId)
            activity.startActivity(intent)
        }

        const val FLAG_HOMEKEY_DISPATCHED: Int = 0x80000000.toInt()
    }

    override fun onAttachedToWindow() {
        this.window.addFlags(FLAG_HOMEKEY_DISPATCHED)
        super.onAttachedToWindow()
    }

    private val mTravelConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mTravelBinder = service as TravelService.TravelBinder
            mTravelBinder!!.service?.setLocationListener(this@OrderActivity)
            mTravelBinder?.start()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "Disconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        map.onCreate(savedInstanceState)
//        setUpMap()

        calcKmService.putExtra(Constants.CURRENT_ORDER_ID, orderId)

        startService(calcKmService)


        bindService(serviceIntent, mTravelConnection, Context.BIND_AUTO_CREATE)

        addOrderListener(orderId)
        LogUtils.d(TAG)
    }


    /**
     * 设置一些amap的属性
     */
    private fun setUpMap() {
        // 设置默认定位按钮是否显示
        aMap.uiSettings.isMyLocationButtonEnabled = false
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.isMyLocationEnabled = true
//        setupLocationStyle()
    }


    /**
     * 设置自定义定位蓝点
     */
    private fun setupLocationStyle() {
        // 自定义系统定位蓝点
        val myLocationStyle = MyLocationStyle()
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car))
        //自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(5f)
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.myLocationStyle = myLocationStyle
    }


    fun finishOrder(view: View) {
        OkGo.post<String>(Urls.REPORT_ORDER)
                .params("driverId", App.DRIVER_ID)
                .params("orderId", orderId)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        if (GsonUtils.getInstance().fromJson(response?.body(), BaseBean::class.java).isSuccess()) {
                            Toast.makeText(this@OrderActivity, "上报成功", Toast.LENGTH_SHORT).show()
                            stopService(calcKmService)
                            finish()
                        }
                    }
                })
    }

    /**
     * 订单监听
     */
    private fun addOrderListener(orderId: Int) {
        QiLinSync.getInstance().addValueEventListener(TAG, Constants.ORDER_LISTENER + "$orderId") { dataSnapshot ->
            if (GsonUtils.getInstance().isJsonObject(dataSnapshot?.data)) {
                val data: JsonObject = JsonParser().parse(dataSnapshot?.data).asJsonObject
                if (data.has(orderId.toString())) {
                    val driverInfo = data.getAsJsonObject(orderId.toString())
                    if (driverInfo != null && driverInfo.has("status")) {
                        if (driverInfo.get("status").asInt == 5) {
                            try {
                                cancelDialog = IOSMsgDialog.init(supportFragmentManager)
                                        .setTitle("温馨提示")
                                        .setMessage("订单取消")
                                        .setPositiveButton("确定")
                                        .show()
                            } catch (e: IllegalStateException) {
                                e.printStackTrace()
                            }

                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mTravelBinder?.service?.removeLocationListener()
        map.onDestroy()
        QiLinSync.getInstance().removeValueEventListener(TAG)
        unbindService(mTravelConnection)
    }

    override fun poiNameChange(poiName: String?) {
        tv_poi_name.text = poiName.toString()
    }


    var preLat = 0.0
    var preLng = 0.0
    var marker: Marker? = null
    var options = PolylineOptions().useGradient(true).width(18f)


    private fun addMarkersToMap(latLng: LatLng) {
        val markerOption = MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car))
                .position(latLng)
        marker = aMap.addMarker(markerOption)
    }

    override fun travelLocation(lat: Double, lng: Double, satellites: Int) {
        if (lat != 0.0 && lng != 0.0) {
            val currentLocation = LatLng(lat, lng)
            marker?.destroy()
            addMarkersToMap(currentLocation)
            aMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation))
            val travelForM = DistanceCalculationUtil.getInstert(this@OrderActivity, App.DRIVER_ID, lat, lng, satellites).filter()
            tv_distance.text = travelForM.toString()
            if (preLat != 0.0 && preLng != 0.0) {
                aMap.addPolyline(options.add(LatLng(preLat, preLng), currentLocation))
            }
            preLat = lat
            preLng = lng
        }
    }


    override fun timeChange(count: Int) {
        tv_time.text = count.toString()
    }
}
