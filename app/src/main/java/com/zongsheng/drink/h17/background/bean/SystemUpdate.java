package com.zongsheng.drink.h17.background.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/9/20.
 */
public class SystemUpdate implements Serializable {
    /** SEQ_ID */
    String seqId;
    /** 更新时间 */
    String updateDate;
    /** 版本类型1:安卓 2:ios */
    String versionType;
    /** 更新版本号 */
    String versionInt;
    /** 更新内容 */
    String content;
    /** 更新路径 */
    String updateUrl;
    /** 更新类型0：普通更新 1：强制更 */
    String updateType;
    /** 备注 */
    String note;
    /** 创建日期 */
    String createTime;
    /** 更新日期 */
    String updateTime;
    /** 删除flag */
    String delFlg;

    public String getSeqId() {
        return seqId;
    }

    public void setSeqId(String seqId) {
        this.seqId = seqId;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getVersionType() {
        return versionType;
    }

    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    public String getVersionInt() {
        return versionInt;
    }

    public void setVersionInt(String versionInt) {
        this.versionInt = versionInt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getDelFlg() {
        return delFlg;
    }

    public void setDelFlg(String delFlg) {
        this.delFlg = delFlg;
    }
}
