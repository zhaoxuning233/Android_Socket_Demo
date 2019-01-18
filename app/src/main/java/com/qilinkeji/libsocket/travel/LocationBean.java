package com.qilinkeji.libsocket.travel;

import java.io.Serializable;

/**
 * @author zxn
 */
public class LocationBean implements Serializable {
    private double longitude;
    private double latitude;
    private String poiName;
    private String content;

    public LocationBean(double longitude, double latitude, String poiName, String content) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.poiName = poiName;
        this.content = content;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getTitle() {
        return poiName;
    }

    public String getContent() {
        return content;
    }
}
