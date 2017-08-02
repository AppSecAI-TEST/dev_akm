package com.zongsheng.drink.h17.front.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 17/8/2.
 */

public class PayM {

    private String id;
    @SerializedName("pay_name")
    private String payName;
    @SerializedName("pic_url")
    private String picUrl;
    @SerializedName("del_flag")
    private String delFlag;

    public String getDelFlag() {
        return delFlag;
    }

    public String getId() {
        return id;
    }

    public String getPayName() {
        return payName;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPayName(String payName) {
        this.payName = payName;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    @Override
    public String toString() {
        return payName;
    }
}
