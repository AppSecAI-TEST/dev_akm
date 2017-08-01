package com.zongsheng.drink.h17.background.bean;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 货道信息
 * Created by 谢家勋 on 2016/9/20.
 */
public class MachineRoad implements Serializable {

    /**
     * 售货机编号
     */
    String machineSn;
    /**
     * 货道号
     */
    String roadNo;
    /**
     * 货道容量
     */
    String roadNum;

    /**
     * 商品url
     */
    String goodsImage;
    /**
     * 商品名称
     */
    String goodsName;
    /**
     * 货道库存
     */
    String inventory;
    /**
     * 商品编号
     */
    String goodsId;
    /**
     * 商品价格
     */
    String goodsPrice;
    /**
     * 创建时间
     */
    String createTime;
    /**
     * 本地库存
     */
    String localKuCun;
    /**
     * 属于那个机型
     */
    private String goodsBelong;
    /**是否自营 0:自营;1:非自营*/
    private String ascription;

    /** 商品简称 */
    private String goodsAbbreviation;

    public String getGoodsAbbreviation() {
        return goodsAbbreviation;
    }

    public void setGoodsAbbreviation(String goodsAbbreviation) {
        this.goodsAbbreviation = goodsAbbreviation;
    }

    public String getGoodsBelong() {
        return goodsBelong;
    }

    public void setGoodsBelong(String goodsBelong) {
        this.goodsBelong = goodsBelong;
    }

    public String getGoodsImage() {
        return goodsImage;
    }

    public void setGoodsImage(String goodsImage) {
        this.goodsImage = goodsImage;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getLocalKuCun() {
        return localKuCun;
    }

    public void setLocalKuCun(String localKuCun) {
        this.localKuCun = localKuCun;
    }

    public String getMachineSn() {
        return machineSn;
    }

    public void setMachineSn(String machineSn) {
        this.machineSn = machineSn;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getRoadNo() {
        return roadNo;
    }

    public void setRoadNo(String roadNo) {
        this.roadNo = roadNo;
    }

    public String getRoadNum() {
        return roadNum;
    }

    public void setRoadNum(String roadNum) {
        this.roadNum = roadNum;
    }

    public String getInventory() {
        return inventory;
    }

    public void setInventory(String inventory) {
        this.inventory = inventory;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsPrice() {
        return goodsPrice;
    }

    public void setGoodsPrice(String goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    public String getAscription() {
        return ascription;
    }

    public void setAscription(String ascription) {
        this.ascription = ascription;
    }
}
