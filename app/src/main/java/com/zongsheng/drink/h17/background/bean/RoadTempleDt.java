package com.zongsheng.drink.h17.background.bean;

import java.io.Serializable;

/**
 * 模板货道列表
 * Created by dongxiaofei on 16/9/23.
 */

public class RoadTempleDt implements Serializable {

    /**
     * 模板编号 外键：货道模板表
     */
    private String templeId;
    /**
     * 货道号
     */
    private String roadNo;
    /**
     * 货道容量
     */
    private String roadNum;
    /**
     * 商品编号 外键：商品信息表
     */
    private String goodsId;

    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 商品简称
     */
    private String goodsAbbreviation;
    /**
     * 商品图片
     */
    private String goodsImage;

    /**是否自营 0:自营;1:非自营*/
    private String ascription;
    /** 价格 */
    private String goodsPrice;

    public String getTempleId() {
        return templeId;
    }

    public void setTempleId(String templeId) {
        this.templeId = templeId;
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

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsAbbreviation() {
        return goodsAbbreviation;
    }

    public void setGoodsAbbreviation(String goodsAbbreviation) {
        this.goodsAbbreviation = goodsAbbreviation;
    }

    public String getGoodsImage() {
        return goodsImage;
    }

    public void setGoodsImage(String goodsImage) {
        this.goodsImage = goodsImage;
    }

    public String getAscription() {
        return ascription;
    }

    public void setAscription(String ascription) {
        this.ascription = ascription;
    }

    public String getGoodsPrice() {
        return goodsPrice;
    }

    public void setGoodsPrice(String goodsPrice) {
        this.goodsPrice = goodsPrice;
    }
}
