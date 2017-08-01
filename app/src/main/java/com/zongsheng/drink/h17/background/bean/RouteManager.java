package com.zongsheng.drink.h17.background.bean;

import java.io.Serializable;

/**
 * Created by 任晓光 on 2016/9/13.
 */
public class RouteManager implements Serializable {
    /**
     * 负责人ID
     */
    String managerId;
    /**
     * 负责人名字
     */
    String managerName;
    /**
     * 所属公司ID
     */
    String companyId;
    /**
     * 联系方式/登录手机号
     */
    String managerTel;
    /**
     * 邮箱
     */
    String managerEmail;
    /**
     * 密码
     */
    String managerPassword;
    /**
     * 登录识别码
     */
    String loginIdentifier;
    /**
     * 创建日期
     */
    String createTime;
    /**
     * 更新日期
     */
    String updateTime;
    /**
     * 删除flag 0:未删除;1:已删除
     */
    String delFlg;

    /**
     * 自营货道比率
     */
    String roadRatio;


    public String getRoadRatio() {
        return roadRatio;
    }

    public void setRoadRatio(String roadRatio) {
        this.roadRatio = roadRatio;
    }


    public String getDelFlg() {
        return delFlg;
    }

    public void setDelFlg(String delFlg) {
        this.delFlg = delFlg;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getManagerTel() {
        return managerTel;
    }

    public void setManagerTel(String managerTel) {
        this.managerTel = managerTel;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }

    public String getManagerPassword() {
        return managerPassword;
    }

    public void setManagerPassword(String managerPassword) {
        this.managerPassword = managerPassword;
    }

    public String getLoginIdentifier() {
        return loginIdentifier;
    }

    public void setLoginIdentifier(String loginIdentifier) {
        this.loginIdentifier = loginIdentifier;
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

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

}
