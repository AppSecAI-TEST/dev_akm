package com.zongsheng.drink.h17.front.bean;

import io.realm.RealmObject;

/**
 * 机器缺货记录信息
 * Created by dongxiaofei on 16/9/16.
 */

public class QueHuoRecord extends RealmObject {

    /**
     * 是否缺货 0:否 1:是
     */
    private String isQueHuo;
    /**
     * 缺货货道号 多个用;拼接
     */
    private String road_no;
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

    public String getIsQueHuo() {
        return isQueHuo;
    }

    public void setIsQueHuo(String isQueHuo) {
        this.isQueHuo = isQueHuo;
    }

    public String getRoad_no() {
        return road_no;
    }

    public void setRoad_no(String road_no) {
        this.road_no = road_no;
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
