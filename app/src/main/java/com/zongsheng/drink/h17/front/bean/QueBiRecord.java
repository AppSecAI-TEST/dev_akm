package com.zongsheng.drink.h17.front.bean;

import io.realm.RealmObject;

/**
 * 机器缺币记录信息
 * Created by dongxiaofei on 16/9/16.
 */

public class QueBiRecord extends RealmObject {

    /**
     * 5角是否缺币 0:否 1:是
     */
    private String isQueBi_five;
    /**
     * 1元是否缺币 0:否 1:是
     */
    private String isQueBi_one;
    /**
     * 售货机编号
     */
    private String machineSn;
    /**
     * 创建时间
     */
    private long createTime;
    /**
     * 是否已上传 0:否 1:是
     */
    private String isUploaded;

    public String getIsQueBi_five() {
        return isQueBi_five;
    }

    public void setIsQueBi_five(String isQueBi_five) {
        this.isQueBi_five = isQueBi_five;
    }

    public String getIsQueBi_one() {
        return isQueBi_one;
    }

    public void setIsQueBi_one(String isQueBi_one) {
        this.isQueBi_one = isQueBi_one;
    }

    public String getMachineSn() {
        return machineSn;
    }

    public void setMachineSn(String machineSn) {
        this.machineSn = machineSn;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getIsUploaded() {
        return isUploaded;
    }

    public void setIsUploaded(String isUploaded) {
        this.isUploaded = isUploaded;
    }
}
