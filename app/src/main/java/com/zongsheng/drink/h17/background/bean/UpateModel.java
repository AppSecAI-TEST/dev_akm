package com.zongsheng.drink.h17.background.bean;

/**
 * Created by Suchengjian on 2017.2.25.
 */

public class UpateModel {
    /**
     * 1.更新库存值，2.更新价格，3.更新库存和价格,4.更新货道商品编号
     */
    private int type;
    /**货道号*/
    private int road_no;
    /**箱号*/
    private int boxindex;
    private String  price;
    private String goodsId;

    @Override
    public String toString() {
        return "UpateModel{" +
                "type=" + type +
                ", road_no=" + road_no +
                ", boxindex=" + boxindex +
                ", price='" + price + '\'' +
                ", goodsId='" + goodsId + '\'' +
                '}';
    }

    public int getBoxindex() {
        return boxindex;
    }

    public void setBoxindex(int boxindex) {
        this.boxindex = boxindex;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRoad_no() {
        return road_no;
    }

    public void setRoad_no(int road_no) {
        this.road_no = road_no;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
