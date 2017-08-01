package com.zongsheng.drink.h17.nohttp;

/**
 * 用于外部取消请求的处理。
 */
public interface RequestHandle {

    public void cancle();

    public boolean isRunning();

}