package com.qilinkeji.libsocket.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.qilinkeji.daemon.QiLinDaemon
import com.qilinkeji.libsocket.R
import com.qilinkeji.libsocket.bean.BaseBean
import com.qilinkeji.libsocket.bean.OrderInfo
import com.qilinkeji.libsocket.global.App
import com.qilinkeji.libsocket.global.Constants
import com.qilinkeji.libsocket.global.Urls
import com.qilinkeji.libsocket.service.LocationService
import com.qilinkeji.libsocket.util.MediaPlayUtils
import com.qilinkeji.qilinsync.global.QiLinSync
import com.qilinkeji.qilinsync.utils.GsonUtils
import com.qilinkeji.qilinsync.utils.LogUtils
import top.limuyang2.customldialog.IOSMsgDialog


class ThreadTestActivity : BaseActivity() {


    companion object {
        fun start(activity: AppCompatActivity) {
            val intent = Intent(activity, ThreadTestActivity::class.java)
            activity.startActivity(intent)
        }
    }


    private val locationIntent by lazy { Intent(this, LocationService::class.java) }
    internal var myBinder: LocationService.MyBinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thread_test)
        initWindow()
        initListener()
        startBind()
        QiLinSync.getInstance().offlineRemoveValue(Constants.NODE_ONLINE + App.DRIVER_ID)
        LogUtils.d(TAG)
        QiLinDaemon.getInstance().startDaemon(this)
    }

    private fun startBind() {
        val myConnection = MyConnection()
        bindService(locationIntent, myConnection, Context.BIND_AUTO_CREATE)
    }


    fun linePoint(view: View) {
        OrderListActivity.start(this)
    }


    /**
     * 初始化监听
     */
    private fun initListener() {
        QiLinSync.getInstance().addValueEventListener(TAG, Constants.NODE_ORDER_LISTENER + App.DRIVER_ID) { dataSnapshot ->
            if (GsonUtils.getInstance().isJsonObject(dataSnapshot?.data)) {
                val data: JsonObject = JsonParser().parse(dataSnapshot?.data).asJsonObject
                if (data.has(App.DRIVER_ID)) {
                    val driverInfo = data.getAsJsonObject(App.DRIVER_ID)
                    if (driverInfo.has(Constants.CURRENT_ORDER_ID)) {
                        val orderId = driverInfo.get(Constants.CURRENT_ORDER_ID).asInt
                        getOrderInfo(orderId)
                        addOrderListener(orderId)
                    }
                }
            }
        }
    }


    var cancelDialog: IOSMsgDialog? = null
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


    //自定义ServiceConnection
    internal inner class MyConnection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {

        }


        override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
            myBinder = binder as? LocationService.MyBinder
        }
    }


    fun getOrder(view: View) {
        showOrderDialog("点完了啥反应也没有, 是不是怪难受的. 😆", isRing = false)
    }


    fun makeOrder(view: View) {
        val order = "http://wsdemo.chuangshiqilin.cn/order/Order/createOrder?start_addr=鸿达光电子产业园&start_addr_lat=43.256123&start_addr_lng=125.123456&driver_num=1&user_phone_num=15543287601&get_order_type=distance&use_car_time=2018/8/14 14:22&distance_price=39&time_price=0&distance=8.7&time=0"
        OkGo.get<String>(order)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        val bean = GsonUtils.getInstance().fromJson(response?.body(), BaseBean::class.java)
                        if (bean.isSuccess()) {
                            Toast.makeText(this@ThreadTestActivity, "下单成功", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ThreadTestActivity, bean.msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
    }


    fun onLine(view: View) {
        OkGo.post<String>(Urls.ORDER_ONLINE)
                .params("driverId", App.DRIVER_ID)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        val bean = GsonUtils.getInstance().fromJson(response?.body(), BaseBean::class.java)
                        if (bean.isSuccess()) {
                            Toast.makeText(this@ThreadTestActivity, "上线成功", Toast.LENGTH_SHORT).show()
                            myBinder?.getService()?.startLocation()
                        } else {
                            Toast.makeText(this@ThreadTestActivity, bean.msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
    }


    fun offLine(view: View) {
        OkGo.post<String>(Urls.ORDER_OFFLINE)
                .params("driverId", App.DRIVER_ID)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        val bean = GsonUtils.getInstance().fromJson(response?.body(), BaseBean::class.java)
                        if (bean.isSuccess()) {
                            Toast.makeText(this@ThreadTestActivity, "下线成功", Toast.LENGTH_SHORT).show()
                            QiLinSync.getInstance().removeValue(Constants.NODE_LOCATION)
                            myBinder?.getService()?.stopLocation()
                        } else {
                            Toast.makeText(this@ThreadTestActivity, bean.msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                })

    }

    var orderDialog: IOSMsgDialog? = null
    /**
     * 弹窗
     */
    private fun showOrderDialog(orderInfo: String, orderId: Int = -1, isRing: Boolean = true) {
        orderDialog = IOSMsgDialog.init(supportFragmentManager)
                .setTitle("新订单")
                .setNegativeButton("拒绝", View.OnClickListener {
                    if (isRing) {
                        MediaPlayUtils.getInstance().playRing(R.raw.dispatch_fail, false)
                        rejectOrder(orderId)
                    }
                })
                .setPositiveButton("接受", View.OnClickListener {
                    if (isRing) {
                        MediaPlayUtils.getInstance().playRing(R.raw.start_order, false)
                        orderTaking(orderId)
                    }
                })
        if (isRing)
            MediaPlayUtils.getInstance().playRing(R.raw.dispatch_new, false)
        try {
            orderDialog?.setMessage(orderInfo)
            orderDialog?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        orderDialog?.dismiss()
        cancelDialog?.dismiss()
        QiLinSync.getInstance().removeValueEventListener(TAG)
        QiLinDaemon.getInstance().stopDaemon()
    }

    private fun initWindow() {
        val win = window
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        win.setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT)
    }

    /**
     * 监听
     */
    private fun orderTaking(orderId: Int, driverId: String = App.DRIVER_ID) {
        OkGo.post<String>(Urls.ORDER_TAKING)
                .params("driverId", driverId)
                .params("orderId", orderId)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        val bean = GsonUtils.getInstance().fromJson(response?.body(), BaseBean::class.java)
                        if (bean.isSuccess()) {
                            OrderActivity.start(this@ThreadTestActivity, orderId)
                        } else {
                            Toast.makeText(this@ThreadTestActivity, bean.msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
    }


    /**
     * 获取订单信息
     */
    private fun getOrderInfo(orderId: Int) {
        OkGo.get<String>(Urls.ORDER_INFO)
                .params("orderId", orderId)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        val type = object : TypeToken<BaseBean<OrderInfo>>() {}.type
                        val baseOrderInfo = GsonUtils.getInstance().fromJson<BaseBean<OrderInfo>>(response?.body(), type)
                        if (baseOrderInfo.isSuccess()) {
                            baseOrderInfo.data?.let {
                                showOrderDialog(response?.body() ?: "", orderId)
                            }
                        }
                    }
                })
    }


    /**
     * 拒单
     */
    private fun rejectOrder(orderId: Int, driverId: String = App.DRIVER_ID) {
        OkGo.post<String>(Urls.REJECT_ORDER)
                .params("driverId", driverId)
                .params("orderId", orderId)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        val bean = GsonUtils.getInstance().fromJson(response?.body(), BaseBean::class.java)
                        if (bean.isSuccess()) {
                            Toast.makeText(this@ThreadTestActivity, "拒单成功", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ThreadTestActivity, bean.msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
    }

}
