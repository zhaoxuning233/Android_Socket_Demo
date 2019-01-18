package com.qilinkeji.libsocket.travel;


import android.graphics.Color;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;

import java.util.List;

public class AddPolylineUtil {
    public static void addPolyLine(AMap aMap, List<LatLng> latLngs) {
        aMap.addPolyline(new PolylineOptions().addAll(latLngs).width(10).color(Color.argb(255, 1, 1, 1)));
    }
}
