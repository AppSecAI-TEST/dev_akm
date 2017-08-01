package com.zongsheng.drink.h17.background.bean;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * 售货机信息列表
 */
public class MachineState extends RealmObject implements Serializable {

    /**
     * 售货机编号
     */
    String machineSn;
    /**
     * 主售货机编号
     */
    String mainMachineSn;
    /**
     * 售货机名称
     */
    String machineName;
    /**
     * 售货机类型
     */
    String machineType;
    /**
     * 显示顺序
     */
    String showSort;

    /**
     * 是否退货撤机 0:否；1:是；
     */
    String isRefundRemove;
    /**
     * 是否移机 0:否；1:是；
     */
    String isRemoveMachine;

    public String getMachineSn() {
        return machineSn;
    }

    public void setMachineSn(String machineSn) {
        this.machineSn = machineSn;
    }

    public String getIsRemoveMachine() {
        return isRemoveMachine;
    }

    public void setIsRemoveMachine(String isRemoveMachine) {
        this.isRemoveMachine = isRemoveMachine;
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

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public String getShowSort() {
        return showSort;
    }

    public void setShowSort(String showSort) {
        this.showSort = showSort;
    }

    public String getIsRefundRemove() {
        return isRefundRemove;
    }

    public void setIsRefundRemove(String isRefundRemove) {
        this.isRefundRemove = isRefundRemove;
    }
}
