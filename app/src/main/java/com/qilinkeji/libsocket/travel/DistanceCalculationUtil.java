package com.qilinkeji.libsocket.travel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * @author zxn
 */
public class DistanceCalculationUtil {
    @SuppressLint("StaticFieldLeak")
    private static DistanceCalculationUtil distanceCalculationUtil = null;

    private DistanceCalculationUtil(Context context) {
        this.mContext = context;
        this.satellites = 5;//当前提供定位服务的卫星个数
        this.travelForM = 0.0;// 行驶 米
        this.lastLatitude = 0;// 最后定位一次的纬度
        lastLongitude = 0;
        this.lastLongitude = 0;// 最后一次定位的经度
        curLatitude = 0;
        this.curLatitude = 0; //当前的经纬度
        curLongitude = 0;
        this.curLongitude = 0; //当前经度
        this.nowTime = System.currentTimeMillis();
        timeDifference = 0;
        this.timeDifference = 0;
        this.orderId = "0";
        this.isWaite = false;
    }

    private Context mContext;
    private int satellites;//当前提供定位服务的卫星个数
    private double travelForM;// 行驶 米
    private double lastLatitude;// 最后定位一次的纬度
    private double lastLongitude;// 最后一次定位的经度
    private double curLatitude; //当前的经纬度
    private double curLongitude; //当前经度
    private long lastLocationTime;
    private long nowTime;
    private int timeDifference;
    private String orderId;
    private boolean isWaite;
    private JsonArray routeList = null;
    private String time;//时间戳

    /**
     * @param context      当前界面
     * @param orderId      订单id
     * @param curLatitude  当前坐标
     * @param curLongitude 当前坐标
     * @param satellites   卫星数量
     * @return DistanceCalculationUtil
     */
    public static DistanceCalculationUtil getInstert(Context context, String orderId, double curLatitude, double curLongitude, int satellites) {

        if (distanceCalculationUtil == null || (!distanceCalculationUtil.orderId.equals("0") && !distanceCalculationUtil.orderId.equals(orderId))) {
            distanceCalculationUtil = new DistanceCalculationUtil(context);
        }
        distanceCalculationUtil.mContext = context;
        distanceCalculationUtil.orderId = orderId;
        distanceCalculationUtil.curLatitude = curLatitude;
        distanceCalculationUtil.curLongitude = curLongitude;
        distanceCalculationUtil.satellites = satellites;
        distanceCalculationUtil.nowTime = System.currentTimeMillis();
        if (distanceCalculationUtil.lastLocationTime == 0) {
            distanceCalculationUtil.lastLocationTime = distanceCalculationUtil.nowTime;
        }
        distanceCalculationUtil.timeDifference = Integer.valueOf((distanceCalculationUtil.nowTime - distanceCalculationUtil.lastLocationTime) + "") / 1000;
        if (distanceCalculationUtil.routeList == null) {
            distanceCalculationUtil.getsprotes();
        }
        return distanceCalculationUtil;
    }

    /**
     * 获取sp中存储的坐标信息
     */
    private void getsprotes() {

        routeList = getRoute();
        if (routeList.size() > 1) {
            lastLatitude = ((JsonObject)
                    routeList.get(routeList.size() - 1)).get("lat").getAsDouble();
            lastLongitude = ((JsonObject)
                    routeList.get(routeList.size() - 1)).get("lng").getAsDouble();
            lastLocationTime = ((JsonObject)
                    routeList.get(routeList.size() - 1)).get("time_of_long").getAsLong();
        } else {
            lastLocationTime = System.currentTimeMillis();
        }
    }

    //获取行驶路径
    private JsonArray getRoute() {
        HashMap<String, String> map = TravelLocationUtil.getKMSP(mContext, "route" + orderId, new String[]{"route"});
        if (map.get("route").equals("-1")) {
            return new JsonArray();
        } else {
            try {
                JsonArray jsonArray = new JsonParser().parse(map.get("route")).getAsJsonArray();
                JsonArray routeArray = new JsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject obj = (JsonObject) jsonArray.get(i);
                    routeArray.add(obj);
                }
                return routeArray;
            } catch (Exception e) {
                e.printStackTrace();
                return new JsonArray();
            }
        }
    }

    /**
     * 过滤坐标点并判断坐标合理性
     */
    /**
     * 过滤坐标点并判断坐标合理性
     */
    public Double filter() {
        if (curLatitude == 0 || curLongitude == 0) {
            return 0.0;
        }

        if (lastLatitude == 0 || lastLongitude == 0) {
            lastLatitude = curLatitude;
            lastLongitude = curLongitude;
            TravelLocationUtil.getInstance(mContext).writefile("filename", "当前时间:" + time + "对lastLat--Lng赋值：" +
                    curLongitude + "," + curLatitude + "当前travelForM:" + travelForM + "\n");
            return 0.0;
        }

        double distance = AMapUtils.calculateLineDistance(new LatLng(lastLatitude, lastLongitude), new LatLng(curLatitude, curLongitude));
        distance = Double.valueOf(TravelLocationUtil.getbignum(distance + "", 4));
        TravelLocationUtil.getInstance(mContext).writefile("filename", "\n 当前时间:" + time +
                "当前坐标:" + curLatitude + "," + curLongitude + "; 上次坐标:" + lastLatitude + "," + lastLongitude + "本次计算：" +
                distance + "m--总距=travelForM=" + travelForM + "m\n" + "时间差" + timeDifference);

        Log.d("filter---", "本次计算=distance=" + distance + "m--总距=travelForM=" + travelForM + "m");

        if (choiceData(distance, timeDifference)) {
            travelForM += distance;

            //更新经纬
            lastLatitude = curLatitude;
            lastLongitude = curLongitude;
            lastLocationTime = nowTime;

            //保存当前点
            savePoint(distance, timeDifference);

            TravelLocationUtil.getInstance(mContext).writefile("filename", "---本次计算合法; 时间差：" +
                    timeDifference + ",行驶总距：" + travelForM + "m\n");
            Log.d("filter---", "---本次计算合法; 时间差：" + timeDifference + ",当前行驶距离：" + travelForM);
        } else {
            if (routeList.size() < 2) {
                //更新经纬
                lastLatitude = curLatitude;
                lastLongitude = curLongitude;
                lastLocationTime = nowTime;

                TravelLocationUtil.getInstance(mContext).writefile("filename", "---routeList.size()<2:更新上次经纬度和时间\n");
                Log.d("filter---", "---routeList.size()<2");
            } else {
                TravelLocationUtil.getInstance(mContext).writefile("filename", "???本次计算不合法; 时间差：" + timeDifference + "\n");
                JsonObject obj = (JsonObject) routeList.get(routeList.size() - 2);
                int longTimeSecond = ((JsonObject)
                        routeList.get(routeList.size() - 1)).get("second").getAsInt();
                double otherDistance = AMapUtils.calculateLineDistance(
                        new LatLng(obj.get("lat").getAsDouble(), obj.get("lng").getAsDouble()), new LatLng(curLatitude, curLongitude));
                otherDistance = Double.valueOf(TravelLocationUtil.getbignum(otherDistance + "", 4));

                TravelLocationUtil.getInstance(mContext).writefile("filename", "取点计算距离:" + otherDistance + "m,取点时差" + longTimeSecond + " \n");
                if (choiceData(otherDistance, longTimeSecond + timeDifference)) {
                    //获取上次计算距离
                    double longTimeDistance = ((JsonObject)
                            routeList.get(routeList.size() - 1)).get("distance").getAsDouble();
                    // 计算距离
                    travelForM -= longTimeDistance;
                    travelForM += otherDistance;
                    //更新经纬
                    lastLatitude = curLatitude;
                    lastLongitude = curLongitude;
                    lastLocationTime = nowTime;

                    //移除上一点
                    removeLastPoint();

                    //保存当前点
                    savePoint(otherDistance, timeDifference);

                    TravelLocationUtil.getInstance(mContext).writefile("filename", "上一点不合法已移除,本次计算保留，时差:" + (longTimeSecond + timeDifference) + ",行驶总距：" + travelForM + " \n");
                    Log.d("filter---", "上一点不合法已移除,本次计算保留，当前行驶距离：" + travelForM);
                } else {
                    TravelLocationUtil.getInstance(mContext).writefile("filename", "!!!当前点不合法 \n");
                    Log.d("filter---", "当前点不合法：");
                }
            }
        }
        updateTimeStamp();
        return travelForM;
    }
//    private static final int SEQUENCE_LINE_ID = 1;
//
//    public Double filter(List<TraceLocation> mTraceList) {
//        LBSTraceClient lbsTraceClient = LBSTraceClient.getInstance(mContext);
//        lbsTraceClient.queryProcessedTrace(SEQUENCE_LINE_ID, mTraceList, LBSTraceClient.TYPE_AMAP, traceListener);
//    }
//
//    private TraceListener traceListener = new TraceListener() {
//        @Override
//        public void onRequestFailed(int lineID, String errorInfo) {
//
//        }
//
//        @Override
//        public void onTraceProcessing(int lineID, int index, List<LatLng> segments) {
//
//        }
//
//        @Override
//        public void onFinished(int lineID, List<LatLng> linepoints, int distance, int waitingtime) {
//
//        }
//    };

    //筛选器
    private boolean choiceData(double distance, int second) {
        return (distance > 2 && distance < 31 * second);
    }

    //移除上一点
    private void removeLastPoint() {
        routeList.remove(routeList.size() - 1);
    }

    //更新全局的时间戳
    private void updateTimeStamp() {
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time = format.format(date);
    }

    //保存当前点
    private void savePoint(double distance, int second) {
        JsonObject obj = new JsonObject();
        obj.addProperty("second", second + "");
        obj.addProperty("lat", curLatitude + "");
        obj.addProperty("lng", curLongitude + "");
        obj.addProperty("distance", distance + "");
        obj.addProperty("time_of_long", lastLocationTime + "");
        routeList.add(obj);
        storage();
    }

    //存储器
    private void storage() {
        HashMap<String, String> travelMap = new HashMap<>();
        travelMap.put("route", routeList.toString());
        TravelLocationUtil.setKMSP(mContext, "key", travelMap);
    }
}
