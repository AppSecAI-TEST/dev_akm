package com.zongsheng.drink.h17.front.bean;


import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Created by Suchnegjian on 2017.2.13.
 */

public class PayModel extends RealmObject implements Serializable {

    /**
     * 商品所对应的机器编号
     */
    public String pushMachineSn;
    // 订单编号
    public String orderSn;
    // 创建时间
    public String createTime;

    // 订单状态   0：订单生成，1：订单支付成功（未出货），3：已出货
    public int orderStatus;
    //商品编码
    public String goodsCode;
    // 购买数量
    public String goodsNum;
    // 商品ID
    public String goodsId;
    // 商品类型 1：饮料机  2：格子柜
    public String goodsBelong;
    // 商品价格
    public String goodsPrice;
    // 主机的机器编码
    public String machineSn;
    // 支付时间
    public String payTime;
    // 支付方式 0:现金支付;1:微信支付;2:支付宝支付
    public String payType;
    // 出货时间
    public String deliveryTime;
    // 机器交易编号
    // 流水号
    public String machineTradeNo;
    /// 货道号
    public String machineRoadNo;
    /**
     * 是否已上传 0:否 1:是
     */
    private String isUploaded = "0";
    /**
     * 325,20160919133913,49,0,30,1,22,10001
     * 记录信息 交易号:212,交易时间:20160910104948,交易种类:现金,现金扣费:10角,非现金扣费:0角,出货结果:成功,货道号:21,商品编号:10
     */
    private String recordInfo;

    private String goodsName;
    /**
     * 临时变量
     */
    private String param;
    // 出货状态 0：未出货 1：已出货 2：出货失败
    public String deliveryStatus;

    public PayModel() {
    }

    public PayModel(PayModel payModel) {

        this.pushMachineSn = payModel.getPushMachineSn();
        this.createTime = payModel.getCreateTime();
        this.deliveryStatus = payModel.getDeliveryStatus();
        this.deliveryTime = payModel.getDeliveryTime();
        this.goodsBelong = payModel.getGoodsBelong();
        this.recordInfo = payModel.getRecordInfo();
        this.param = payModel.getParam();
        this.goodsName = payModel.getGoodsName();
        this.isUploaded = payModel.getIsUploaded();
        this.machineRoadNo = payModel.getMachineRoadNo();
        this.machineTradeNo = payModel.getMachineTradeNo();
        this.payType = payModel.getPayType();
        this.payTime = payModel.getPayTime();
        this.machineSn = payModel.getMachineSn();
        this.goodsPrice = payModel.getGoodsPrice();
        this.goodsId = payModel.getGoodsId();
        this.goodsNum = payModel.getGoodsNum();
        this.goodsCode = payModel.getGoodsCode();
        this.orderStatus = payModel.getOrderStatus();
        this.orderSn = payModel.getOrderSn();
    }

    @Override
    public String toString() {
        return "PayModel{" +
                "pushMachineSn='" + pushMachineSn + '\'' +
                ", orderSn='" + orderSn + '\'' +
                ", createTime='" + createTime + '\'' +
                ", orderStatus=" + orderStatus +
                ", goodsCode='" + goodsCode + '\'' +
                ", goodsNum='" + goodsNum + '\'' +
                ", goodsId='" + goodsId + '\'' +
                ", goodsBelong='" + goodsBelong + '\'' +
                ", goodsPrice='" + goodsPrice + '\'' +
                ", machineSn='" + machineSn + '\'' +
                ", payTime='" + payTime + '\'' +
                ", payType='" + payType + '\'' +
                ", deliveryTime='" + deliveryTime + '\'' +
                ", machineTradeNo='" + machineTradeNo + '\'' +
                ", machineRoadNo='" + machineRoadNo + '\'' +
                ", isUploaded='" + isUploaded + '\'' +
                ", recordInfo='" + recordInfo + '\'' +
                ", goodsName='" + goodsName + '\'' +
                ", param='" + param + '\'' +
                ", deliveryStatus='" + deliveryStatus + '\'' +
                '}';
    }
    //原型模式
    public PayModel clone() {
        return new PayModel(this);
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getRecordInfo() {
        return recordInfo;
    }

    public void setRecordInfo(String recordInfo) {
        this.recordInfo = recordInfo;
    }

    public String getIsUploaded() {
        return isUploaded;
    }

    public void setIsUploaded(String isUploaded) {
        this.isUploaded = isUploaded;
    }

    public String getPushMachineSn() {
        return pushMachineSn;
    }

    public void setPushMachineSn(String pushMachineSn) {
        this.pushMachineSn = pushMachineSn;
    }

    public String getOrderSn() {
        return orderSn;
    }

    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getGoodsCode() {
        return goodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        this.goodsCode = goodsCode;
    }

    public String getGoodsNum() {
        return goodsNum;
    }

    public void setGoodsNum(String goodsNum) {
        this.goodsNum = goodsNum;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsBelong() {
        return goodsBelong;
    }

    public void setGoodsBelong(String goodsBelong) {
        this.goodsBelong = goodsBelong;
    }

    public String getGoodsPrice() {
        return goodsPrice;
    }

    public void setGoodsPrice(String goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    public String getMachineSn() {
        return machineSn;
    }

    public void setMachineSn(String machineSn) {
        this.machineSn = machineSn;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getMachineTradeNo() {
        return machineTradeNo;
    }

    public void setMachineTradeNo(String machineTradeNo) {
        this.machineTradeNo = machineTradeNo;
    }

    public String getMachineRoadNo() {
        return machineRoadNo;
    }

    public void setMachineRoadNo(String machineRoadNo) {
        this.machineRoadNo = machineRoadNo;
    }
}
