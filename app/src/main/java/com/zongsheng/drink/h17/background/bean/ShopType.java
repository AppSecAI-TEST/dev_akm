package com.zongsheng.drink.h17.background.bean;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Created by 谢家勋 on 2016/9/20.
 */
public class ShopType extends RealmObject implements Serializable {

    /** 分类编号 */
    String typeId;
    /** 上级ID */
    String parentId;
    /** 分类名称 */
    String typeName;

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
