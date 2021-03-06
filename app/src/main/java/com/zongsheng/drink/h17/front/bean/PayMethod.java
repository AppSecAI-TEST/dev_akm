package com.zongsheng.drink.h17.front.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 袁国栋 on 17/8/2.
 * 网络支付方式
 */

public class PayMethod {

    private String id;
    @SerializedName("pay_name")
    private String payName;
    @SerializedName("pic_url")
    private String picUrl;
    @SerializedName("del_flag")
    private String delFlag;

    public PayMethod(){}

    public PayMethod(String id,String payName,String picUrl,String delFlag){
        this.id = id;
        this.payName = payName;
        this.picUrl = picUrl;
        this.delFlag = delFlag;
    }
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
