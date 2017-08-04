package com.zongsheng.drink.h17.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.common.JsonControl;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.front.bean.PayModel;
import com.zongsheng.drink.h17.interfaces.ICallBack2Service;
import com.zongsheng.drink.h17.service.thread.MyThread;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * 批量上传销售记录服务
 */
public class SaleRecordUploadService extends Service implements ICallBack2Service {

    private MyThread myThread;
    private Realm realm;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        myThread = new MyThread(SysConfig.L_REQ_AG_TIME_30, this);
        myThread.start();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 上传记录
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (myThread != null) {
            myThread.killer();
        }
        super.onDestroy();
    }

    @Override
    public void doSomething() {
        try {
            realm = Realm.getDefaultInstance();
            List<PayModel> payModels = getPayModelNoUpLoad();
            if (payModels.size() == 0) {
                return;
            }
            if (machineSnIsOK() && ClientConnectMQ.getInstance().sendMessage(JsonControl.payModels2Json(payModels), SysConfig.MQ_ADDORDER)) {
                updatePayModels(payModels);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    private boolean machineSnIsOK() {
        return !MyApplication.getInstance().getMachine_sn().equals("") && MyApplication.getInstance().getMachine_sn().length() == 10;
    }

    private List<PayModel> getPayModelNoUpLoad() {
        RealmResults<PayModel> results = realm.where(PayModel.class).equalTo("isUploaded", "0").findAll().sort("CreateTime", Sort.ASCENDING);
        List<PayModel> payModels = new ArrayList<>();
        int i = 0;
        for (PayModel payModel : results) {
            if (i >= 10) {
                break;
            }
            payModels.add(payModel);
            i++;
        }
        return payModels;
    }

    private void updatePayModels(List<PayModel> payModels) {
        for (PayModel payModel : payModels) {
            MyApplication.getInstance().getLogBuyAndShip().d("上传销售记录(成功\\失败) = 订单号 : "+payModel.getOrderSn()+" ; 商品名 : "+payModel.getGoodsName());
            final PayModel results = realm.where(PayModel.class).equalTo("OrderSn", payModel.getOrderSn()).equalTo("isUploaded", "0").findFirst();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    results.setIsUploaded("1");
                }
            });
        }
    }
}
