package com.zongsheng.drink.h17.background.bean;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by 谢家勋 on 2016/9/14.
 */
public class ShopGoods extends RealmObject implements Serializable {

    /** 售货机商品编号 */
    String goodsId;
    /** 最底层商品分类ID */
    String goodsType;
    /** 顶级分类ID */
    String parentTypeId;
    /** 顶级分类名称 */
    String typeName;
    /** 是否自营 0:自营;1:非自营 */
    String ascription;
    /** 商品名称 */
    String goodsName;
    /** 商品简称 */
    String goodsAbbreviation;
    /** 商品图片 */
    String goodsImage;
    /** 商品价格 */
    String goodsPrice;
    /** 商品单位 */
    String goodsUnit;
    /** 每箱瓶数 */
    String goodsNum;
    /** 销售状态 0:销售中;1:已下架 */
    String goodsStatus;
    /** 备注 */
    String goodsDesc;

    @Override
    public String toString() {
        return "ShopGoods{" +
                "goodsId='" + goodsId + '\'' +
                ", goodsType='" + goodsType + '\'' +
                ", parentTypeId='" + parentTypeId + '\'' +
                ", typeName='" + typeName + '\'' +
                ", ascription='" + ascription + '\'' +
                ", goodsName='" + goodsName + '\'' +
                ", goodsAbbreviation='" + goodsAbbreviation + '\'' +
                ", goodsImage='" + goodsImage + '\'' +
                ", goodsPrice='" + goodsPrice + '\'' +
                ", goodsUnit='" + goodsUnit + '\'' +
                ", goodsNum='" + goodsNum + '\'' +
                ", goodsStatus='" + goodsStatus + '\'' +
                ", goodsDesc='" + goodsDesc + '\'' +
                '}';
    }

    public String getGoodsAbbreviation() {
        return goodsAbbreviation;
    }

    public void setGoodsAbbreviation(String goodsAbbreviation) {
        this.goodsAbbreviation = goodsAbbreviation;
    }


    public String getParentTypeId() {
        return parentTypeId;
    }

    public void setParentTypeId(String parentTypeId) {
        this.parentTypeId = parentTypeId;
    }


    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }


    public String getGoodsType() {
        return goodsType;
    }

    public void setGoodsType(String goodsType) {
        this.goodsType = goodsType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getAscription() {
        return ascription;
    }

    public void setAscription(String ascription) {
        this.ascription = ascription;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsImage() {
        return goodsImage;
    }

    public void setGoodsImage(String goodsImage) {
        this.goodsImage = goodsImage;
    }

    public String getGoodsPrice() {
        return goodsPrice;
    }

    public void setGoodsPrice(String goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    public String getGoodsUnit() {
        return goodsUnit;
    }

    public void setGoodsUnit(String goodsUnit) {
        this.goodsUnit = goodsUnit;
    }

    public String getGoodsNum() {
        return goodsNum;
    }

    public void setGoodsNum(String goodsNum) {
        this.goodsNum = goodsNum;
    }

    public String getGoodsStatus() {
        return goodsStatus;
    }

    public void setGoodsStatus(String goodsStatus) {
        this.goodsStatus = goodsStatus;
    }

}
