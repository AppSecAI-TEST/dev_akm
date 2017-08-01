package com.zongsheng.drink.h17.presenter;

import com.zongsheng.drink.h17.front.bean.PayModel;
import com.zongsheng.drink.h17.front.bean.ShipmentModel;
import com.zongsheng.drink.h17.front.common.ShowBuyPageListener;
import com.zongsheng.drink.h17.interfaces.IBuyGoodsPopWindowView;
import com.zongsheng.drink.h17.interfaces.INetWorkRequCallBackListener;

/**
 * Created by Suchengjian on 2017.2.16.
 */

public interface IBuyActivityPresenter extends INetWorkRequCallBackListener {
    void cancel();
    void receiverMQMsg(String msg);
    void refundRequest(ShipmentModel shipmentModel);
    void bindPopView(IBuyGoodsPopWindowView buyGoodsPopWindowView);
    void setVersionNum();
    void updateLocalKuncun(String road_sn);
    void shipmentSuccess(String huodao, String goodsCode, String goodsCount,
                         String machineQueryType, String saleOrderID, int boxIndex, String saleTime, String trade_no);
    void bindListener(ShowBuyPageListener showBuyPageListener);

    void saleWithoutCash(final ShipmentModel shipmentModel);
    void costMoneyForCabinet(final ShipmentModel shipmentModel, final int money, final boolean isOnLine);
    void saleByCash(final int goodsCode);
    void onlinePaySend2MQ(PayModel payModel, String orderSn, String payType, String DeliveryStatus);
    void machineQueB(String machineID, String fiveStatus, String oneStatus);
    void saleNocashForDesk(final ShipmentModel shipmentModel);


    /**
     * 获取数据
     */
    void initData();

    void close();
}
