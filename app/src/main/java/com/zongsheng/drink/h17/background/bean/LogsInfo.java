package com.zongsheng.drink.h17.background.bean;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 操作日志
 * Created by 谢家勋 on 2016/8/23.
 */
public class LogsInfo extends RealmObject implements Serializable {
    /** 日志编号  */
    int logId;
    /** 日志级别 0:一般;1:中等;2:重要  */
    String logLevel;
    /** 售货机编号  */
    String machineSn;
    /** 操作内容  */
    String oprateContent;
    /** 操作时间  */
    String oprateTime;

    //////////20161007添加是否已经上传///////////////
    /** 是否已上传 0:否 1:是 */
    private String isUploaded = "0";

    public String getOprateTime() {
        return oprateTime;
    }

    public void setOprateTime(String oprateTime) {
        this.oprateTime = oprateTime;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getMachineSn() {
        return machineSn;
    }

    public void setMachineSn(String machineSn) {
        this.machineSn = machineSn;
    }

    public String getOprateContent() {
        return oprateContent;
    }

    public void setOprateContent(String oprateContent) {
        this.oprateContent = oprateContent;
    }

    public String getIsUploaded() {
        return isUploaded;
    }

    public void setIsUploaded(String isUploaded) {
        this.isUploaded = isUploaded;
    }
}
