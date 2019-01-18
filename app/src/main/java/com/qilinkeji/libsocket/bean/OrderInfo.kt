package com.qilinkeji.libsocket.bean

import com.google.gson.annotations.SerializedName

data class OrderInfo(@SerializedName("start_addr_lat")
                     val startAddrLat: String = "",
                     @SerializedName("distance")
                     val distance: String = "",
                     @SerializedName("get_order_type")
                     val getOrderType: String = "",
                     @SerializedName("user_phone_num")
                     val userPhoneNum: String = "",
                     @SerializedName("use_car_time")
                     val useCarTime: String = "",
                     @SerializedName("start_addr_lng")
                     val startAddrLng: String = "",
                     @SerializedName("id")
                     val id: Int = 0,
                     @SerializedName("time")
                     val time: String = "",
                     @SerializedName("start_addr")
                     val startAddr: String = "",
                     @SerializedName("order_id")
                     val orderId: String = "",
                     @SerializedName("time_price")
                     val timePrice: String = "",
                     @SerializedName("distance_price")
                     val distancePrice: String = "")