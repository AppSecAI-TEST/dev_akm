package com.zongsheng.drink.h17.front.bean;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.Index;

/**
 * 商品
 * Created by 谢家勋 on 2016/8/15.
 */
public class GoodsInfo extends RealmObject implements Serializable {

    /** 产品id */
    private String goodsID;
    /** 产品名称 */
    private String goodsName;
    /** 价格 */
    private String price;
    /** 图片路径 */
    private String goodsImage;
    /** 本地库存 */
    private String kuCun;
    /** 是否售空 0:未空 1:售空 */
    private String isSoldOut;
    /** 商品编码(机器编码) */
    private String goodsCode;
    /** 显示顺序 */
    private int showSort;

    /** 产品所属 1:主机 2:格子柜 */
    @Index
    private String goodsBelong;

    /**是否自营 0:自营;1:非自营*/
    private String ascription;
    /** 20160809添加字段 商品二维码地址 */
    /** 二维码地址 */
    private String payQCodeUrl;

    /** 货道 1~n */
    private int road_no;
    /** 最大库存 */
    private int maxKucun;
    /** 线上库存 */
    private int onlineKuCun;
    /** 机器编号 */
    private String machineID;

    /** 产品简称 */
    private String goodsAbbreviation;

    /** 本地库存数判断用 */
    private int localKuCunForCheck;
    /** 判断用指定数量 */
    private int zhidingCount;

    public String getAscription() {
        return ascription;
    }

    public void setAscription(String ascription) {
        this.ascription = ascription;
    }

    public GoodsInfo(String goodsID, String goodsName, String price, String goodsImage, String kuCun,
                     String isSoldOut, String goodsCode, int showSort, String goodsBelong, String payQCodeUrl) {
        this.goodsID = goodsID;
        this.goodsName = goodsName;
        this.price = price;
        this.goodsImage = goodsImage;
        this.kuCun = kuCun;
        this.isSoldOut = isSoldOut;
        this.goodsCode = goodsCode;
        this.showSort = showSort;
        this.goodsBelong = goodsBelong;
        this.payQCodeUrl = payQCodeUrl;
    }


    public GoodsInfo(String goodsID, String goodsName, String price, String goodsImage, String kuCun,
                     String isSoldOut, String goodsCode, int showSort, String goodsBelong, String payQCodeUrl,
                     int road_no, int maxKucun, int onlineKuCun, String machineID) {
        this.goodsID = goodsID;
        this.goodsName = goodsName;
        this.price = price;
        this.goodsImage = goodsImage;
        this.kuCun = kuCun;
        this.isSoldOut = isSoldOut;
        this.goodsCode = goodsCode;
        this.showSort = showSort;
        this.goodsBelong = goodsBelong;
        this.payQCodeUrl = payQCodeUrl;
        this.road_no = road_no;
        this.maxKucun = maxKucun;
        this.onlineKuCun = onlineKuCun;
        this.machineID = machineID;
    }

    public GoodsInfo() {
    }

    @Override
    public String toString() {
        return "GoodsInfo{" +
                "goodsID='" + goodsID + '\'' +
                ", goodsName='" + goodsName + '\'' +
                ", price='" + price + '\'' +
                ", goodsImage='" + goodsImage + '\'' +
                ", kuCun='" + kuCun + '\'' +
                ", isSoldOut='" + isSoldOut + '\'' +
                ", goodsCode='" + goodsCode + '\'' +
                ", showSort=" + showSort +
                ", goodsBelong='" + goodsBelong + '\'' +
                ", ascription='" + ascription + '\'' +
                ", payQCodeUrl='" + payQCodeUrl + '\'' +
                ", road_no=" + road_no +
                ", maxKucun=" + maxKucun +
                ", onlineKuCun=" + onlineKuCun +
                ", machineID='" + machineID + '\'' +
                ", goodsAbbreviation='" + goodsAbbreviation + '\'' +
                ", localKuCunForCheck=" + localKuCunForCheck +
                ", zhidingCount=" + zhidingCount +
                '}';
    }

    public String getGoodsAbbreviation() {
        return goodsAbbreviation;
    }

    public void setGoodsAbbreviation(String goodsAbbreviation) {
        this.goodsAbbreviation = goodsAbbreviation;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getGoodsImage() {
        return goodsImage;
    }

    public void setGoodsImage(String goodsImage) {
        this.goodsImage = goodsImage;
    }

    public String getKuCun() {
        return kuCun;
    }

    public void setKuCun(String kuCun) {
        this.kuCun = kuCun;
    }

    public String getGoodsID() {
        return goodsID;
    }

    public void setGoodsID(String goodsID) {
        this.goodsID = goodsID;
    }

    public String getIsSoldOut() {
        return isSoldOut;
    }

    public void setIsSoldOut(String isSoldOut) {
        this.isSoldOut = isSoldOut;
    }

    public String getGoodsCode() {
        return goodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        this.goodsCode = goodsCode;
    }

    public int getShowSort() {
        return showSort;
    }

    public void setShowSort(int showSort) {
        this.showSort = showSort;
    }

    public String getGoodsBelong() {
        return goodsBelong;
    }

    public void setGoodsBelong(String goodsBelong) {
        this.goodsBelong = goodsBelong;
    }

    public String getPayQCodeUrl() {
        return payQCodeUrl;
    }

    public void setPayQCodeUrl(String payQCodeUrl) {
        this.payQCodeUrl = payQCodeUrl;
    }

    public int getRoad_no() {
        return road_no;
    }

    public void setRoad_no(int road_no) {
        this.road_no = road_no;
    }

    public int getMaxKucun() {
        return maxKucun;
    }

    public void setMaxKucun(int maxKucun) {
        this.maxKucun = maxKucun;
    }

    public int getOnlineKuCun() {
        return onlineKuCun;
    }

    public void setOnlineKuCun(int onlineKuCun) {
        this.onlineKuCun = onlineKuCun;
    }

    public String getMachineID() {
        return machineID;
    }

    public void setMachineID(String machineID) {
        this.machineID = machineID;
    }

    public int getLocalKuCunForCheck() {
        return localKuCunForCheck;
    }

    public void setLocalKuCunForCheck(int localKuCunForCheck) {
        this.localKuCunForCheck = localKuCunForCheck;
    }

    public int getZhidingCount() {
        return zhidingCount;
    }

    public void setZhidingCount(int zhidingCount) {
        this.zhidingCount = zhidingCount;
    }
}
