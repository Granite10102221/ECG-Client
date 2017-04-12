package com.orking.egc;

/**
 * Created by zhanglei on 2017/4/10.
 */

public class EGCData {

    private final int POINT_COUNT = 14;

    public int HRT;
    public int BRT;
    public int LEADStatus;
    public int[] mPoints;

    public EGCData(){
        mPoints = new int[POINT_COUNT];
    }

    public EGCData(int hrt, int brt, int leadStatus, int[] points){
        HRT = hrt;
        BRT = brt;
        LEADStatus = leadStatus;
        mPoints = points;
    }
}
