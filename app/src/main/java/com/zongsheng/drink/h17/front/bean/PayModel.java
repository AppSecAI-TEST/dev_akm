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
    public String PushMachineSn;
    // 订单编号
    public String OrderSn;
    // 创建时间
    public String CreateTime;

    // 订单状态   0：订单生成，1：订单支付成功（未出货），3：已出货
    public int OrderStatus;
    //商品编码
    public String GoodsCode;
    // 购买数量
    public String GoodsNum;
    // 商品ID
    public String GoodsId;
    // 商品类型 1：饮料机  2：格子柜
    public String GoodsBelong;
    // 商品价格
    public String GoodsPrice;
    // 主机的机器编码
    public String MachineSn;
    // 支付时间
    public String PayTime;
    // 支付方式 0:现金支付;1:微信支付;2:支付宝支付
    public String PayType;
    // 出货时间
    public String DeliveryTime;
    // 机器交易编号
    // 流水号
    public String MachineTradeNo;
    /// 货道号
    public String MachineRoadNo;
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
    public String DeliveryStatus;

    public PayModel() {
    }

    public PayModel(PayModel payModel) {

        this.PushMachineSn = payModel.getPushMachineSn();
        this.CreateTime = payModel.getCreateTime();
        this.DeliveryStatus = payModel.getDeliveryStatus();
        this.DeliveryTime = payModel.getDeliveryTime();
        this.GoodsBelong = payModel.getGoodsBelong();
        this.recordInfo = payModel.getRecordInfo();
        this.param = payModel.getParam();
        this.goodsName = payModel.getGoodsName();
        this.isUploaded = payModel.getIsUploaded();
        this.MachineRoadNo = payModel.getMachineRoadNo();
        this.MachineTradeNo = payModel.getMachineTradeNo();
        this.PayType = payModel.getPayType();
        this.PayTime = payModel.getPayTime();
        this.MachineSn = payModel.getMachineSn();
        this.GoodsPrice = payModel.getGoodsPrice();
        this.GoodsId = payModel.getGoodsId();
        this.GoodsNum = payModel.getGoodsNum();
        this.GoodsCode = payModel.getGoodsCode();
        this.OrderStatus = payModel.getOrderStatus();
        this.OrderSn = payModel.getOrderSn();
    }

    @Override
    public String toString() {
        return "PayModel{" +
                "PushMachineSn='" + PushMachineSn + '\'' +
                ", OrderSn='" + OrderSn + '\'' +
                ", CreateTime='" + CreateTime + '\'' +
                ", OrderStatus=" + OrderStatus +
                ", GoodsCode='" + GoodsCode + '\'' +
                ", GoodsNum='" + GoodsNum + '\'' +
                ", GoodsId='" + GoodsId + '\'' +
                ", GoodsBelong='" + GoodsBelong + '\'' +
                ", GoodsPrice='" + GoodsPrice + '\'' +
                ", MachineSn='" + MachineSn + '\'' +
                ", PayTime='" + PayTime + '\'' +
                ", PayType='" + PayType + '\'' +
                ", DeliveryTime='" + DeliveryTime + '\'' +
                ", MachineTradeNo='" + MachineTradeNo + '\'' +
                ", MachineRoadNo='" + MachineRoadNo + '\'' +
                ", isUploaded='" + isUploaded + '\'' +
                ", recordInfo='" + recordInfo + '\'' +
                ", goodsName='" + goodsName + '\'' +
                ", param='" + param + '\'' +
                ", DeliveryStatus='" + DeliveryStatus + '\'' +
                '}';
    }
    //原型模式
    public PayModel clone() {
        return new PayModel(this);
    }

    public String getDeliveryStatus() {
        return DeliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        DeliveryStatus = deliveryStatus;
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
        return PushMachineSn;
    }

    public void setPushMachineSn(String pushMachineSn) {
        PushMachineSn = pushMachineSn;
    }

    public String getOrderSn() {
        return OrderSn;
    }

    public void setOrderSn(String orderSn) {
        OrderSn = orderSn;
    }

    public String getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(String createTime) {
        CreateTime = createTime;
    }

    public int getOrderStatus() {
        return OrderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        OrderStatus = orderStatus;
    }

    public String getGoodsCode() {
        return GoodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        GoodsCode = goodsCode;
    }

    public String getGoodsNum() {
        return GoodsNum;
    }

    public void setGoodsNum(String goodsNum) {
        GoodsNum = goodsNum;
    }

    public String getGoodsId() {
        return GoodsId;
    }

    public void setGoodsId(String goodsId) {
        GoodsId = goodsId;
    }

    public String getGoodsBelong() {
        return GoodsBelong;
    }

    public void setGoodsBelong(String goodsBelong) {
        GoodsBelong = goodsBelong;
    }

    public String getGoodsPrice() {
        return GoodsPrice;
    }

    public void setGoodsPrice(String goodsPrice) {
        GoodsPrice = goodsPrice;
    }

    public String getMachineSn() {
        return MachineSn;
    }

    public void setMachineSn(String machineSn) {
        MachineSn = machineSn;
    }

    public String getPayTime() {
        return PayTime;
    }

    public void setPayTime(String payTime) {
        PayTime = payTime;
    }

    public String getPayType() {
        return PayType;
    }

    public void setPayType(String payType) {
        PayType = payType;
    }

    public String getDeliveryTime() {
        return DeliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        DeliveryTime = deliveryTime;
    }

    public String getMachineTradeNo() {
        return MachineTradeNo;
    }

    public void setMachineTradeNo(String machineTradeNo) {
        MachineTradeNo = machineTradeNo;
    }

    public String getMachineRoadNo() {
        return MachineRoadNo;
    }

    public void setMachineRoadNo(String machineRoadNo) {
        MachineRoadNo = machineRoadNo;
    }
}
