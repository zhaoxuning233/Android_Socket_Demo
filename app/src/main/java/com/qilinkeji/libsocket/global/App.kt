package com.qilinkeji.libsocket.global

import android.app.Application
import android.util.Log
import com.lzy.okgo.OkGo
import com.qilinkeji.daemon.QiLinDaemon
import com.qilinkeji.libsocket.R
import com.qilinkeji.libsocket.util.ProcessUtils
import com.qilinkeji.qilinsocket.config.Config
import com.qilinkeji.qilinsync.QiLinApp
import com.qilinkeji.qilinsync.global.QiLinOptions


/**
 * @author zhangbo
 * @date 2018/8/7
 */
class App : Application() {

    companion object {
        @Volatile
        var DRIVER_ID = "0016"
        lateinit var application: Application
    }

    override fun onCreate() {
        super.onCreate()
        QiLinApp.init(this, QiLinOptions.Builder().setSyncUrl("wss://ws.chuangshiqilin.com").setAppId("qilin_6sfkxd45s0").build(), Config.Builder().setShowLog(true).build())
        QiLinDaemon.getInstance().init(this, R.mipmap.ic_launcher)
        if (ProcessUtils.isMainProcess(this)) {
            Log.d("TAG", "Main")
            OkGo.getInstance().init(this)
            application = this@App
        }
    }

}