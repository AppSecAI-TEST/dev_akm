package com.zongsheng.drink.h17.background.bean;

import java.util.List;

/**
 * 货道模板信息
 * Created by 谢家勋 on 2016/9/14.
 */
public class RoadTemple {

    /** 模板编号 */
    private String templeId;
    /** 模板名称 */
    private String templeName;
    /** 模板描述 */
    private String templeDesc;
    /** 产品所属 1:主机 2:格子柜 */
    private String goodsBelong;
    /** 添加日期 */
    private String createTime;
    /** 模板货道列表 */
    private List<RoadTempleDt> roadTempleDtList;

    public String getTempleId() {
        return templeId;
    }

    public void setTempleId(String templeId) {
        this.templeId = templeId;
    }

    public String getTempleName() {
        return templeName;
    }

    public void setTempleName(String templeName) {
        this.templeName = templeName;
    }

    public String getTempleDesc() {
        return templeDesc;
    }

    public void setTempleDesc(String templeDesc) {
        this.templeDesc = templeDesc;
    }

    public String getGoodsBelong() {
        return goodsBelong;
    }

    public void setGoodsBelong(String goodsBelong) {
        this.goodsBelong = goodsBelong;
    }

    public List<RoadTempleDt> getRoadTempleDtList() {
        return roadTempleDtList;
    }

    public void setRoadTempleDtList(List<RoadTempleDt> roadTempleDtList) {
        this.roadTempleDtList = roadTempleDtList;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
