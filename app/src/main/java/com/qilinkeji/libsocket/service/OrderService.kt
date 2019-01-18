package com.qilinkeji.libsocket.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.qilinkeji.libsocket.global.App
import com.qilinkeji.libsocket.global.Constants
import com.qilinkeji.qilinsync.global.QiLinSync
import com.qilinkeji.qilinsync.utils.GsonUtils

/**
 * @author zhangbo
 * @date 2018/8/16
 */
class OrderService : Service() {

    private val binder = MyBinder()


    inner class MyBinder : Binder() {
        public fun getService(): OrderService {
            return this@OrderService
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }



}