package com.zongsheng.drink.h17.front.bean;

import java.io.Serializable;

/**
 * 服务器端的帮助信息
 * Created by dongxiaofei on 16/9/13.
 */

public class ServerHelpInfo implements Serializable {

    /** 帮助编号 */
    private String helpId;
    /** 0:全部;1:售货机前台;2:售货机后台;3:APP;4:微信 */
    private String terminal;
    /** 问题描述 */
    private String questionDesc;
    /** 问题类型 1:普通；2:图文； */
    private String questionType;
    /** 问题原因 */
    private String questionReason;
    /** 解决方法 */
    private String resolvent;
    /** 创建日期 */
    private String createTime;

    public String getHelpId() {
        return helpId;
    }

    public void setHelpId(String helpId) {
        this.helpId = helpId;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public String getQuestionDesc() {
        return questionDesc;
    }

    public void setQuestionDesc(String questionDesc) {
        this.questionDesc = questionDesc;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getQuestionReason() {
        return questionReason;
    }

    public void setQuestionReason(String questionReason) {
        this.questionReason = questionReason;
    }

    public String getResolvent() {
        return resolvent;
    }

    public void setResolvent(String resolvent) {
        this.resolvent = resolvent;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
