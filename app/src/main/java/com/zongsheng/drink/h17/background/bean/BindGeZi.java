package com.zongsheng.drink.h17.background.bean;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by 谢家勋 on 2016/9/17.
 */
public class BindGeZi extends RealmObject implements Serializable {


    public String getMachineSn() {
        return machineSn;
    }

    public void setMachineSn(String machineSn) {
        this.machineSn = machineSn;
    }

    public String getMainMachineSn() {
        return mainMachineSn;
    }

    public void setMainMachineSn(String mainMachineSn) {
        this.mainMachineSn = mainMachineSn;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public String getMachineGroup() {
        return machineGroup;
    }

    public void setMachineGroup(String machineGroup) {
        this.machineGroup = machineGroup;
    }

    public String getTempleId() {
        return templeId;
    }

    public void setTempleId(String templeId) {
        this.templeId = templeId;
    }

    public String getUsedStatus() {
        return usedStatus;
    }

    public void setUsedStatus(String usedStatus) {
        this.usedStatus = usedStatus;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getShowSort() {
        return showSort;
    }

    public void setShowSort(String showSort) {
        this.showSort = showSort;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public boolean isRefundRemove() {
        return isRefundRemove;
    }

    public void setIsRefundRemove(boolean isRefundRemove) {
        this.isRefundRemove = isRefundRemove;
    }

    public boolean isRemoveMachine() {
        return isRemoveMachine;
    }

    public void setIsRemoveMachine(boolean isRemoveMachine) {
        this.isRemoveMachine = isRemoveMachine;
    }

    public String getRoadCount() {
        return roadCount;
    }

    public void setRoadCount(String roadCount) {
        this.roadCount = roadCount;
    }


    /** 格子柜编码*/
    @PrimaryKey
    String machineSn;

    String mainMachineSn;
    String machineName;
    String companyId;
    /**机器类型*/
    String machineType;
    String machineGroup;
    String templeId;
    String usedStatus;
    String routeId;
    String routeName;
    String positionId;
    String positionName;
    String showSort;
    /**格子柜鸽子数*/
    String roadCount;
    String createTime;
    boolean isRefundRemove;
    boolean isRemoveMachine;
}
