package com.zongsheng.drink.h17.front.bean;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 格子柜信息
 * Created by dongxiaofei on 16/9/13.
 */

public class CabinetInfo extends RealmObject {

    /** 机器编号 */
    @PrimaryKey
    private String machineID;
    /** 机器类型 */
    private String machineType;
    /** 顺序 */
    private int showSort;
    /** 货道数 */
    private int roadCount;

    public String getMachineID() {
        return machineID;
    }

    public void setMachineID(String machineID) {
        this.machineID = machineID;
    }

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public int getShowSort() {
        return showSort;
    }

    public void setShowSort(int showSort) {
        this.showSort = showSort;
    }

    public int getRoadCount() {
        return roadCount;
    }

    public void setRoadCount(int roadCount) {
        this.roadCount = roadCount;
    }
}
