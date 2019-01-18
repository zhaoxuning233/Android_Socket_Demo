package com.qilinkeji.libsocket.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.maps.utils.SpatialRelationUtil
import com.amap.api.maps.utils.overlay.SmoothMoveMarker
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.qilinkeji.libsocket.R
import com.qilinkeji.libsocket.bean.LinePoint
import com.qilinkeji.libsocket.global.Constants
import com.qilinkeji.libsocket.util.ToastUtil
import com.qilinkeji.qilinsync.utils.GsonUtils
import com.qilinkeji.qilinsync.utils.LogUtils
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_line.*
import java.util.*

class LineActivity : AppCompatActivity() {

    private val aMap by lazy { mapview.map }


    private val pointList = mutableListOf<LatLng>()

    private val orderId by lazy { intent.getStringExtra(Constants.ORDER_ID) }

    private var mPolyline: Polyline? = null


    companion object {
        fun start(activity: AppCompatActivity, orderId: String) {
            val intent = Intent(activity, LineActivity::class.java)
            intent.putExtra(Constants.ORDER_ID, orderId)
            activity.startActivity(intent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_line)
        mapview.onCreate(savedInstanceState)
        load()
    }


    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
        mapview.onResume()
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        mapview.onPause()
    }

    /**
     * 方法必须重写
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapview.onSaveInstanceState(outState)
    }

    /**
     * 方法必须重写
     */
    override fun onDestroy() {
        super.onDestroy()
        mapview.onDestroy()
    }


    private fun load() {
        val url = "http://wsdemo.chuangshiqilin.cn/order/Order/getOrderPoint/orderId/$orderId"
        OkGo.get<String>(url)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        val line: LinePoint = GsonUtils.getInstance().fromJson(response?.body(), LinePoint::class.java)
                        pointList.clear()
                        line.data?.forEach { it ->
                            pointList.add(LatLng(it.lat, it.lng))
                        }
                        addPolylinessoild(pointList)
                    }
                })
    }

    private fun addPolylinessoild(list: List<LatLng>) {
        if (list.isNotEmpty()) {

            tv_distance.text = "公里数:${calcKm(list)}"

            val colorList = ArrayList<Int>()
            val bitmapDescriptors = ArrayList<BitmapDescriptor>()

            val colors = intArrayOf(Color.argb(255, 0, 255, 0), Color.argb(255, 255, 255, 0), Color.argb(255, 255, 0, 0))

            val textureList = ArrayList<BitmapDescriptor>()
            textureList.add(BitmapDescriptorFactory.fromResource(R.drawable.custtexture))

            val texIndexList = ArrayList<Int>()
            texIndexList.add(0)
            texIndexList.add(1)
            texIndexList.add(2)

            val random = Random()
            for (i in list.indices) {
                colorList.add(colors[random.nextInt(3)])
                bitmapDescriptors.add(textureList[0])

            }

            mPolyline = aMap.addPolyline(PolylineOptions().setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.custtexture))
                    .addAll(list)
                    .useGradient(true)
                    .width(18f))
            val bounds = if (list.size > 2) {
                LatLngBounds(list[0], list[list.size - 2])
            } else {
                LatLngBounds(list[0], list[0])
            }
            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            startMove()
        }
    }

    private fun calcKm(list: List<LatLng>): Double {
        if (list.size < 2) {
            return 0.0
        }
        var distance = 0.0
        for (index in 1 until list.size) {
            distance += AMapUtils.calculateLineDistance(list[index - 1], list[index])
        }
        return distance
    }


    /**
     * 开始移动
     */
    fun startMove() {

        if (mPolyline == null) {
            ToastUtil.show(this, "请先设置路线")
            return
        }

        // 读取轨迹点
        val points = pointList
        // 构建 轨迹的显示区域
        val bounds = if (points.size > 2) {
            LatLngBounds(points[0], points[points.size - 2])
        } else {
            LatLngBounds(points[0], points[0])
        }
        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50))

        // 实例 SmoothMoveMarker 对象
        val smoothMarker = SmoothMoveMarker(aMap)
        // 设置 平滑移动的 图标
        smoothMarker.setDescriptor(BitmapDescriptorFactory.fromResource(R.drawable.icon_car))

        // 取轨迹点的第一个点 作为 平滑移动的启动
        val drivePoint = points[0]
        val pair = SpatialRelationUtil.calShortestDistancePoint(points, drivePoint)
        points[pair.first] = drivePoint
        val subList = points.subList(pair.first, points.size)

        // 设置轨迹点
        smoothMarker.setPoints(subList)
        // 设置平滑移动的总时间  单位  秒
        smoothMarker.setTotalDuration(40)

        // 设置  自定义的InfoWindow 适配器
        aMap.setInfoWindowAdapter(infoWindowAdapter)
        // 显示 infowindow
        smoothMarker.marker?.showInfoWindow()

        // 设置移动的监听事件  返回 距终点的距离  单位 米
        smoothMarker.setMoveListener { distance ->
            runOnUiThread {
                if (infoWindowLayout != null && title != null) {

                    title!!.text = "距离终点还有： " + distance.toInt() + "米"
                }
            }
        }

        // 开始移动
        smoothMarker.startSmoothMove()

    }

    /**
     * 个性化定制的信息窗口视图的类
     * 如果要定制化渲染这个信息窗口，需要重载getInfoWindow(Marker)方法。
     * 如果只是需要替换信息窗口的内容，则需要重载getInfoContents(Marker)方法。
     */
    private var infoWindowAdapter: AMap.InfoWindowAdapter = object : AMap.InfoWindowAdapter {

        // 个性化Marker的InfoWindow 视图
        // 如果这个方法返回null，则将会使用默认的信息窗口风格，内容将会调用getInfoContents(Marker)方法获取
        override fun getInfoWindow(marker: Marker): View {

            return getInfoWindowView(marker)
        }

        // 这个方法只有在getInfoWindow(Marker)返回null 时才会被调用
        // 定制化的view 做这个信息窗口的内容，如果返回null 将以默认内容渲染
        override fun getInfoContents(marker: Marker): View {

            return getInfoWindowView(marker)
        }
    }

    private var infoWindowLayout: LinearLayout? = null
    private var title: TextView? = null
    private var snippet: TextView? = null

    /**
     * 自定义View并且绑定数据方法
     * @param marker 点击的Marker对象
     * @return  返回自定义窗口的视图
     */
    private fun getInfoWindowView(marker: Marker): View {
        if (infoWindowLayout == null) {
            infoWindowLayout = LinearLayout(this)
            infoWindowLayout!!.orientation = LinearLayout.VERTICAL
            title = TextView(this)
            snippet = TextView(this)
            title?.setTextColor(Color.BLACK)
            snippet?.setTextColor(Color.BLACK)
            infoWindowLayout?.setBackgroundResource(R.drawable.infowindow_bg)

            infoWindowLayout?.addView(title)
            infoWindowLayout?.addView(snippet)
        }

        return infoWindowLayout as LinearLayout
    }


}
