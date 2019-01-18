package com.qilinkeji.libsocket.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.qilinkeji.libsocket.R
import com.qilinkeji.libsocket.global.App
import com.qilinkeji.libsocket.global.Constants
import com.qilinkeji.libsocket.service.LocationService
import com.qilinkeji.qilinsync.bean.DataSnapshot
import com.qilinkeji.qilinsync.global.QiLinSync
import com.qilinkeji.qilinsync.listener.ValueEventListener
import com.qilinkeji.qilinsync.utils.LogUtils
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : BaseActivity(), ValueEventListener {


    var count = 2000
    private var needPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE, Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EasyPermissions.requestPermissions(this, "必要的权限", 0, *needPermissions)
        et_node.setText(Constants.NODE_LOCATION + App.DRIVER_ID)
        LogUtils.d(TAG)
    }


    fun updateOrders(view: View) {
        val map = hashMapOf<String, Any>()
        val locationMap = hashMapOf<String, Double>()
        locationMap["lat"] = 127.123123
        locationMap["lng"] = 47.123123
        map[System.currentTimeMillis().toString()] = locationMap
        QiLinSync.getInstance().updateChildren(Constants.NODE_ORDER_LOCATION_PRE, map)
    }

    fun update(view: View) {
        val map = hashMapOf<String, Any>()
        map["status"] = count++
        QiLinSync.getInstance().updateChildren(et_node.text.toString(), map)
    }

    fun delete(view: View) {
        QiLinSync.getInstance().removeValue(et_node.text.toString())
    }

    fun set(view: View) {
        val map = hashMapOf<String, Any>()
        map["ceshi"] = 21412431312
        QiLinSync.getInstance().setValue(et_node.text.toString(), map)
    }

    fun register(view: View) {
        QiLinSync.getInstance().addValueEventListener(TAG, et_node.text.toString(), this@MainActivity)
    }

    fun removeValueEvent(view: View) {
        QiLinSync.getInstance().removeValueEventListener(TAG)
    }


    fun jump(view: View) {
        startActivity(Intent(this, ThreadTestActivity::class.java))
    }

    fun registerOfflineEvent(view: View) {
        QiLinSync.getInstance().offlineRemoveValue(et_node.text.toString())
    }

    fun offlineUpdateValue(view: View) {
        val map = HashMap<String, Any>(2)
        map["zhao"] = "gengchen"
        QiLinSync.getInstance().offlineUpdateValue(et_node.text.toString(), map)
    }


    override fun onDestroy() {
        super.onDestroy()
        QiLinSync.getInstance().removeValueEventListener(TAG)
    }

    override fun onDataChange(dataSnapshot: DataSnapshot?) {
        tv_receive.text = dataSnapshot?.data
    }
}
