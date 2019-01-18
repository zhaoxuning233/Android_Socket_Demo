package com.qilinkeji.libsocket.bean

/**
 * @author zhangbo
 * @date 2018/8/14
 */
open class BaseBean<T>(var data: T? = null, var code: Int = 0, var msg: String? = "") {

    fun isSuccess(): Boolean {
        return code == 200
    }
}