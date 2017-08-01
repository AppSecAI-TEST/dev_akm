package com.zongsheng.drink.h17.interfaces;

import com.zongsheng.drink.h17.front.bean.GoodsInfo;

/**
 * Created by Administrator on 2017.1.4.
 */

public interface IBuyGoodsPopWindowView {
    /**
     * 网络状态判断是正常
     */
    void NetWorkSuccess();

    /**
     * 网络状态不是正常的
     */
    void NetWorkError();

    /**
     * 设置现金数
     */
    void setCashCount(int cashCount);

    /**
     * 设置商品信息
     */
    void setGoodsInfo(final GoodsInfo goodsInfo, String selectType);
    /** 现金出货成功 */
    void shipmentSuccessByCash();

    /** 非现金支付成功 */
    void shipmentSuccessByNet();

    void closeWindow();
}
