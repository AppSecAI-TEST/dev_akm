package com.zongsheng.drink.h17.common.sortlistview;

import java.io.Serializable;

public class SortModel implements Serializable {

    private String name;   //显示的数据
    private String sortLetters;  //显示数据拼音的首字母
    private String goodType; // 商品的类型
    private String goodPrice; // 商品的价格
    private String goodId;     // 商品的编号
    /** 商品分类*/
    private String parentTypeId;
    /** 是否自营 0:自营;1:非自营*/
    private String ascription;
    /** 分类名称*/
    private String typeName;
    /** 商品简称 */
    String goodsAbbreviation;

    public String getGoodsAbbreviation() {
        return goodsAbbreviation;
    }

    public void setGoodsAbbreviation(String goodsAbbreviation) {
        this.goodsAbbreviation = goodsAbbreviation;
    }

    public String getAscription() {
        return ascription;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setAscription(String ascription) {
        this.ascription = ascription;
    }

    public String getParentTypeId() {
        return parentTypeId;
    }

    public void setParentTypeId(String parentTypeId) {
        this.parentTypeId = parentTypeId;
    }

    @Override
    public String toString() {
        return "SortModel{" +
                "name='" + name + '\'' +
                ", sortLetters='" + sortLetters + '\'' +
                ", goodType='" + goodType + '\'' +
                ", goodPrice='" + goodPrice + '\'' +
                ", goodId='" + goodId + '\'' +
                ", goodsImage='" + goodsImage + '\'' +
                '}';
    }

    /**商品图片*/
    private String goodsImage;

    public String getGoodsImage() {
        return goodsImage;
    }

    public void setGoodsImage(String goodsImage) {
        this.goodsImage = goodsImage;
    }

    public String getGoodType() {
        return goodType;
    }

    public void setGoodType(String goodType) {
        this.goodType = goodType;
    }

    public String getGoodPrice() {
        return goodPrice;
    }

    public void setGoodPrice(String goodPrice) {
        this.goodPrice = goodPrice;
    }

    public String getGoodId() {
        return goodId;
    }

    public void setGoodId(String goodId) {
        this.goodId = goodId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSortLetters() {
        return sortLetters;
    }

    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }
}
