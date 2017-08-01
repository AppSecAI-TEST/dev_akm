package com.zongsheng.drink.h17.front.bean;

import io.realm.RealmObject;

/**
 * 后台数据请求信息
 * Created by dongxiaofei on 16/9/21.
 */

public class BackGroundRequest extends RealmObject {

    /** 请求接口 eg:token/check_version.do */
    private String requestInterface;
    /** 请求参数 json形式的 */
    private String requestPara;

    private int what;

    @Override
    public String toString() {
        return "BackGroundRequest{" +
                "requestInterface='" + requestInterface + '\'' +
                ", requestPara='" + requestPara + '\'' +
                ", what=" + what +
                '}';
    }

    public int getWhat() {
        return what;
    }

    public void setWhat(int what) {
        this.what = what;
    }

    public BackGroundRequest() {
    }

    public BackGroundRequest(String requestInterface, String requestPara, int what) {
        this.requestInterface = requestInterface;
        this.requestPara = requestPara;
        this.what = what;
    }

    public String getRequestInterface() {
        return requestInterface;
    }

    public void setRequestInterface(String requestInterface) {
        this.requestInterface = requestInterface;
    }

    public String getRequestPara() {
        return requestPara;
    }

    public void setRequestPara(String requestPara) {
        this.requestPara = requestPara;
    }
}
