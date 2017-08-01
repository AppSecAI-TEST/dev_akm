package com.zongsheng.drink.h17.front.bean;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 帮助信息
 * Created by dongxiaofei on 16/9/10.
 */

public class HelpInfo extends RealmObject implements Serializable {

    /** 帮助ID */
    @PrimaryKey
    private String helpID;
    /** 帮助类型 1:问题 2:说明 */
    private String helpType;
    /** 问题 */
    private String question;
    /** 原因 */
    private String reason;
    /** 答案 */
    private String answer;
    /** 说明内容 */
    private String helpIntro;
    /** 显示顺序 */
    private int showSort;


    public String getHelpID() {
        return helpID;
    }

    public void setHelpID(String helpID) {
        this.helpID = helpID;
    }

    public String getHelpType() {
        return helpType;
    }

    public void setHelpType(String helpType) {
        this.helpType = helpType;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getHelpIntro() {
        return helpIntro;
    }

    public void setHelpIntro(String helpIntro) {
        this.helpIntro = helpIntro;
    }

    public int getShowSort() {
        return showSort;
    }

    public void setShowSort(int showSort) {
        this.showSort = showSort;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
