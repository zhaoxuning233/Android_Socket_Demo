package com.qilinkeji.libsocket.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.qilinkeji.libsocket.R
import com.qilinkeji.libsocket.bean.BaseBean
import com.qilinkeji.libsocket.bean.OrderListBean
import com.qilinkeji.libsocket.global.App
import com.qilinkeji.libsocket.global.Urls
import com.qilinkeji.qilinsync.utils.GsonUtils
import kotlinx.android.synthetic.main.activity_order_list.*

class OrderListActivity : BaseActivity() {

    companion object {
        fun start(activity: AppCompatActivity) {
            val intent = Intent(activity, OrderListActivity::class.java)
            activity.startActivity(intent)
        }
    }


    private val adapter by lazy { ArrayAdapter<String>(this@OrderListActivity, android.R.layout.simple_list_item_1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_list)
        initAdapter()
        getOrderId()
    }

    private fun initAdapter() {
        list_view.adapter = adapter
        list_view.setOnItemClickListener { _, _, position, _ ->
            val id = adapter.getItem(position)
            LineActivity.start(this@OrderListActivity, id)
        }

    }

    private fun getOrderId() {
        OkGo.get<String>(Urls.ORDER_LIST)
                .params("driverId", App.DRIVER_ID)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        val type = object : TypeToken<BaseBean<List<OrderListBean>>>() {}.type
                        val baseOrderList = GsonUtils.getInstance().fromJson<BaseBean<List<OrderListBean>>>(response?.body(), type)
                        if (baseOrderList.isSuccess()) {
                            baseOrderList.data?.let { it ->
                                adapter.clear()
                                it.forEach {
                                    adapter.add(it.orderId)
                                }
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                })

    }
}
