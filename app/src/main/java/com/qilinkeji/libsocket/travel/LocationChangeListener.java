package com.qilinkeji.libsocket.travel;


/**
 * @author zxn
 */
public interface LocationChangeListener {
    void poiNameChange(String poiName);

    //    void travelForMChange(double travelForM);
    void travelLocation(double lat, double lng, int satellites);

    void timeChange(int count);
}
