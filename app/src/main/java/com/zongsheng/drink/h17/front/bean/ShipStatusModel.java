package com.zongsheng.drink.h17.front.bean;

/**
 * Created by Suchengjian on 2017.4.2.
 */

public class ShipStatusModel {

    public String OrderSn;
    public String MachineTime;

    //1出货，2退款
    public String ShipStatus;

    public ShipStatusModel() {
        OrderSn = "";
        MachineTime = "";
        ShipStatus = "";
    }

    @Override
    public String toString() {
        return "ShipStatusModel{" +
                "orderSn='" + OrderSn + '\'' +
                ", MachineTime='" + MachineTime + '\'' +
                ", ShipStatus='" + ShipStatus + '\'' +
                '}';
    }

    public String getOrderSn() {
        return OrderSn;
    }

    public void setOrderSn(String orderSn) {
        OrderSn = orderSn;
    }

    public String getMachineTime() {
        return MachineTime;
    }

    public void setMachineTime(String machineTime) {
        MachineTime = machineTime;
    }

    public String getShipStatus() {
        return ShipStatus;
    }

    public void setShipStatus(String shipStatus) {
        ShipStatus = shipStatus;
    }
}
