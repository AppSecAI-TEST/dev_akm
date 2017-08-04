package com.zongsheng.drink.h17.Model;

import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.background.bean.MQReceiver;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.front.bean.PayModel;
import com.zongsheng.drink.h17.front.bean.QueBiRecord;
import com.zongsheng.drink.h17.front.bean.ShipmentModel;


import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Suchengjian on 2017.2.16.
 */

public class BuyActivityModelImpl implements IPayInfoModel {

    private Realm realm;

    public BuyActivityModelImpl() {
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void cancel() {
        if (realm != null) {
            realm.close();
        }
    }

    @Override
    public void updateLocalKuncun(String road_no) {
        MyApplication.getInstance().getLogBuyAndShip().d("更新本地库存 : 货道号 = "+road_no);
        RealmResults<GoodsInfo> goodsInfos = realm.where(GoodsInfo.class).equalTo("goodsBelong", "1").equalTo("road_no", Integer.parseInt(road_no)).findAll();
        final GoodsInfo goodsInfo = goodsInfos.where().findFirst();
        if (goodsInfo != null && goodsInfo.getKuCun() != null && !"".equals(goodsInfo.getKuCun())) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    if (Integer.parseInt(goodsInfo.getKuCun()) >= 2) {
                        goodsInfo.setKuCun(String.valueOf(Integer.parseInt(goodsInfo.getKuCun()) - 1));
                    }
                    if (goodsInfo.getOnlineKuCun() >= 2) {
                        goodsInfo.setOnlineKuCun(goodsInfo.getOnlineKuCun() - 1);
                    }
                }
            });
        }
    }

    @Override
    public void addPayModel2Realm(final PayModel payModel) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(payModel);
            }
        });
    }

    @Override
    public void addPayModel2Realm(PayModel payModel, String orderSn, String payType, String DeliveryStatus) {
        MyApplication.getInstance().getLogBuyAndShip().d("将销售记录存入数据库");
        final PayModel payModel1 = payModel.clone();
        payModel1.setOrderSn(orderSn);
        payModel1.setPayType(payType);
        payModel1.setDeliveryStatus(DeliveryStatus);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(payModel1);
            }
        });
    }

    @Override
    public RealmResults<PayModel> getPayModel4Realm(String order_sn) {
        return realm.where(PayModel.class)
                .equalTo("OrderSn", order_sn).findAll();
    }

    @Override
    public void addMQReceiver(ShipmentModel shipmentModel) {

        final MQReceiver mqReceiver = new MQReceiver();
        mqReceiver.setOperationtype(shipmentModel.getOperationtype() + "");
        mqReceiver.setGoods_belong(shipmentModel.getGoods_belong());
        mqReceiver.setOrder_sn(shipmentModel.getOrder_sn());
        mqReceiver.setGoods_id(shipmentModel.getGoods_id());
        mqReceiver.setPush_machine_sn(shipmentModel.getPush_machine_sn());
        mqReceiver.setGoods_price(shipmentModel.getGoods_price());
        mqReceiver.setPay_time(shipmentModel.getPay_time());
        mqReceiver.setTrade_no(shipmentModel.getTrade_no());
        mqReceiver.setPay_type(shipmentModel.getPay_type());

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(mqReceiver);
            }
        });
    }

    @Override
    public void machineQueB(String machineID, String fiveStatus, String oneStatus) {
        final QueBiRecord queBiRecord = new QueBiRecord();
        queBiRecord.setMachineSn(machineID);
        queBiRecord.setIsQueBi_five(fiveStatus);
        queBiRecord.setIsQueBi_one(oneStatus);
        queBiRecord.setCreateTime(new Date().getTime());
        queBiRecord.setIsUploaded("0");
        // 取得最后一条故障信息, 如果相同就不要再去处理了
        RealmResults<QueBiRecord> results = realm.where(QueBiRecord.class).findAll();
        results = results.sort("createTime", Sort.DESCENDING);
        if (results.size() > 0) {
            QueBiRecord record = results.first();
            if (record != null) {
                if (record.getIsQueBi_five().equals(queBiRecord.getIsQueBi_five())
                        && record.getIsQueBi_one().equals(queBiRecord.getIsQueBi_one())
                        && record.getMachineSn().equals(queBiRecord.getMachineSn())
                        ) {
                    return;
                }
            }
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(queBiRecord);
            }
        });
    }

    @Override
    public void updatePayModels(String orderSn) {

        final RealmResults<PayModel> results = realm.where(PayModel.class).equalTo("OrderSn", orderSn).equalTo("isUploaded", "0").findAll();
        if (results.size() > 0) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (PayModel payModel : results) {
                        payModel.setIsUploaded("1");
                    }
                }
            });
        }
    }
}
