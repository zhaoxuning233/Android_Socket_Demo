package com.qilinkeji.libsocket.bean

data class LinePoint(val data: List<DataItem>?)


data class DataItem(val lng: Double = 0.0,
                    val lat: Double = 0.0)


