package com.qilinkeji.libsocket.bean

import com.google.gson.annotations.SerializedName

data class OrderListBean(
        @SerializedName("id") val id: Int,
        @SerializedName("order_id") val orderId: String,
        @SerializedName("start_addr") val startAddr: String,
        @SerializedName("start_addr_lat") val startAddrLat: String,
        @SerializedName("start_addr_lng") val startAddrLng: String,
        @SerializedName("driver_num") val driverNum: String,
        @SerializedName("user_phone_num") val userPhoneNum: String,
        @SerializedName("get_order_type") val getOrderType: String,
        @SerializedName("use_car_time") val useCarTime: String,
        @SerializedName("distance_price") val distancePrice: String,
        @SerializedName("time_price") val timePrice: String,
        @SerializedName("distance") val distance: String,
        @SerializedName("time") val time: String,
        @SerializedName("status") val status: Int,
        @SerializedName("driver_id") val driverId: String,
        @SerializedName("point_path") val pointPath: List<List<String>>,
        @SerializedName("mile") val mile: String
)