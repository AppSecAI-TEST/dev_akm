package com.zongsheng.drink.h17.front.bean;

import io.realm.RealmObject;

/**
 * 机器故障记录信息
 * Created by dongxiaofei on 16/9/16.
 */

public class MachineFaultRecord extends RealmObject {

    /** 主控是否故障 0:否 1:是 */
    private String isMasterFault;
    /** 主控故障说明 */
    private String masterAlarmReason;

    /** 纸币器是否故障 0:否 1:是 */
    private String isPaperFault;
    /** 纸币器故障说明 */
    private String paperAlarmFault;

    /** 硬币器是否故障 0:否 1:是 */
    private String isCoinFault;
    /** 硬币器故障说明 */
    private String coinAlarmReason;

    /** 创建时间 */
    private long createTime;
    /** 是否已上传 0:否 1:是 */
    private String isUploaded;

    public String getIsMasterFault() {
        return isMasterFault;
    }

    public void setIsMasterFault(String isMasterFault) {
        this.isMasterFault = isMasterFault;
    }

    public String getMasterAlarmReason() {
        return masterAlarmReason;
    }

    public void setMasterAlarmReason(String masterAlarmReason) {
        this.masterAlarmReason = masterAlarmReason;
    }

    public String getIsPaperFault() {
        return isPaperFault;
    }

    public void setIsPaperFault(String isPaperFault) {
        this.isPaperFault = isPaperFault;
    }

    public String getPaperAlarmFault() {
        return paperAlarmFault;
    }

    public void setPaperAlarmFault(String paperAlarmFault) {
        this.paperAlarmFault = paperAlarmFault;
    }

    public String getIsCoinFault() {
        return isCoinFault;
    }

    public void setIsCoinFault(String isCoinFault) {
        this.isCoinFault = isCoinFault;
    }

    public String getCoinAlarmReason() {
        return coinAlarmReason;
    }

    public void setCoinAlarmReason(String coinAlarmReason) {
        this.coinAlarmReason = coinAlarmReason;
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
