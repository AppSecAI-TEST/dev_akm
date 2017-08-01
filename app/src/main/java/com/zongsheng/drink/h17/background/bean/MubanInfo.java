package com.zongsheng.drink.h17.background.bean;

import java.io.Serializable;

/**
 * 获取模板
 * Created by 谢家勋 on 2016/8/23.
 */
public class MubanInfo implements Serializable {
    /**
     * 操作
     */
    private String operation;
    /**
     * 时间
     */
    private String operationTime;

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(String operationTime) {
        this.operationTime = operationTime;
    }
}
