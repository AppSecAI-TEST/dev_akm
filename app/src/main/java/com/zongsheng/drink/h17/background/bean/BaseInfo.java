package com.zongsheng.drink.h17.background.bean;

/**
 * Created by Suchengjian on 2017.3.17.
 */

public class BaseInfo {

    private String code;
    private String info;

    public BaseInfo() {
    }

    public BaseInfo(String code, String info) {
        this.code = code;
        this.info = info;
    }

    @Override
    public String toString() {
        return "BaseInfo{" +
                "code='" + code + '\'' +
                ", info='" + info + '\'' +
                '}';
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
