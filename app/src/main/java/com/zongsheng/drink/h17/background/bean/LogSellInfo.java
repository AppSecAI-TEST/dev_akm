package com.zongsheng.drink.h17.background.bean;

import java.io.Serializable;

/**
 * 销售日志
 * Created by 谢家勋 on 2016/8/23.
 */
public class LogSellInfo implements Serializable {
    /**
     * 名字
     */
    private String goodsName;
    /**
     * 方式
     */
    private String payType;
    /**
     * 金额
     */
    private String price;
    /**
     * 时间
     */
    private String payTime;
    /** 在线支付金额 */
    private String noCashPrice;

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getNoCashPrice() {
        return noCashPrice;
    }

    public void setNoCashPrice(String noCashPrice) {
        this.noCashPrice = noCashPrice;
    }
}
