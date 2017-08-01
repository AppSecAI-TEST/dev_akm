package com.zongsheng.drink.h17.front.bean;

/**
 * Created by Administrator on 17/7/24.
 * 定义可以使用的网络支付方式
 */

public class PayMethod {
    private boolean aLiPay;
    private boolean weiXin;
    private boolean jingDong;

    public void setaLiPayEnable(boolean aLiPay) {
        this.aLiPay = aLiPay;
    }

    public boolean isaLiPayEnable() {
        return aLiPay;
    }

    public void setWeiXinEnable(boolean weiXin) {
        this.weiXin = weiXin;
    }

    public boolean isWeiXinEnable() {
        return weiXin;
    }

    public void setJingDongEnable(boolean jingDong) {
        this.jingDong = jingDong;
    }

    public boolean isJingDongEnable() {
        return jingDong;
    }

    public int getPayMethodCount(){
        int count=0;
        if (isaLiPayEnable()){
            count++;
        }
        if (isJingDongEnable()){
            count++;
        }
        if (isWeiXinEnable()){
            count++;
        }
        return count;
    }
}
