package com.zongsheng.drink.h17.background.bean;

import java.io.Serializable;

/**
 * Created by Suchengjian on 2017.4.19.
 */

public class PanDInfo implements Serializable {
    private String operateTime;
    private String grossOnline;
    private String grossOffline;
    private String salesVolume;

    @Override
    public String toString() {
        return "PanDInfo{" +
                "operateTime='" + operateTime + '\'' +
                ", grossOnline='" + grossOnline + '\'' +
                ", grossOffline='" + grossOffline + '\'' +
                ", salesVolume='" + salesVolume + '\'' +
                '}';
    }

    public String getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(String operateTime) {
        this.operateTime = operateTime;
    }

    public String getGrossOnline() {
        return grossOnline;
    }

    public void setGrossOnline(String grossOnline) {
        this.grossOnline = grossOnline;
    }

    public String getGrossOffline() {
        return grossOffline;
    }

    public void setGrossOffline(String grossOffline) {
        this.grossOffline = grossOffline;
    }

    public String getSalesVolume() {
        return salesVolume;
    }

    public void setSalesVolume(String salesVolume) {
        this.salesVolume = salesVolume;
    }
}
