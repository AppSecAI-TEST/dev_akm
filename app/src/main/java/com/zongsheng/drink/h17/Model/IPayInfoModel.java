package com.zongsheng.drink.h17.Model;

import com.zongsheng.drink.h17.front.bean.PayModel;
import com.zongsheng.drink.h17.front.bean.ShipmentModel;

import io.realm.RealmResults;

/**
 * Created by Suchengjian on 2017.2.16.
 */

public interface IPayInfoModel {
    void updatePayModels(String orderSn);
    void cancel();
    void updateLocalKuncun(String road_no);
    void addPayModel2Realm(PayModel payModel);
    void addPayModel2Realm(PayModel payModel, String orderSn, String payType, String DeliveryStatus);
    RealmResults<PayModel> getPayModel4Realm(String order_sn);
    void addMQReceiver(ShipmentModel shipmentModel);
    void machineQueB(String machineID, String fiveStatus, String oneStatus);
}
