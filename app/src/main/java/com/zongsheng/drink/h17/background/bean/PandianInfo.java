package com.zongsheng.drink.h17.background.bean;

import java.io.Serializable;

/**
 * 盘点
 * Created by 谢家勋 on 2016/8/24.
 */
public class PandianInfo implements Serializable {
    /**
     * 名字
     */
    private String goodsName;
    /**
     * 货道id
     */
    private String id;
    /**
     * 存量
     */
    private String kucun;
    /**
     * 状态
     */
    private String sataus;

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKucun() {
        return kucun;
    }

    public void setKucun(String kucun) {
        this.kucun = kucun;
    }

    public String getSataus() {
        return sataus;
    }

    public void setSataus(String sataus) {
        this.sataus = sataus;
    }
}
