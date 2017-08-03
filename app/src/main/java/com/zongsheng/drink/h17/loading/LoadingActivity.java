package com.zongsheng.drink.h17.loading;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.zongsheng.drink.h17.ComActivity;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.base.BasePresenter;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.interfaces.ILoadingInterface;
import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.common.DataUtil;

import com.zongsheng.drink.h17.common.SharedPreferencesUtils;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.front.activity.BuyActivity;
import com.zongsheng.drink.h17.observable.SerialObservable;
import com.zongsheng.drink.h17.presenter.ILoadingPresenter;
import com.zongsheng.drink.h17.presenter.LoadingPresenterImpl;
import com.zongsheng.drink.h17.receiver.Alarmreceiver;
import java.util.Observable;

import io.realm.Realm;
import io.realm.RealmResults;


/** 加载页面 */
public class LoadingActivity extends ComActivity implements ILoadingInterface{

    private static final String TAG = "Loading";

    private AlertView alertView;
    private ILoadingPresenter iLoadingPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        MyApplication.getInstance().setWeihu(false);
        iLoadingPresenter = (ILoadingPresenter) presenter;
        if(iLoadingPresenter != null) {
            iLoadingPresenter.getServiceMQIP();
        }
        /** 取得帮助信息*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(iLoadingPresenter != null) {
                    iLoadingPresenter.updataHelpInfo();
                }
            }
        }, 50);
        /** 取得格子柜信息*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(iLoadingPresenter != null) {
                    iLoadingPresenter.getBindGeziInfo();
                }
            }
        }, 200);

        /** 设置关机重启时间*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setRestartTime();
            }
        }, 800);
        // 设置需要获取机器的基本信息
        isNeedInitMachineInfo = true;
        MyObservable.getInstance().registObserver(this);
    }

    @Override
    protected BasePresenter createPresenter() {
        return new LoadingPresenterImpl(this);
    }

    /** 机器信息获取完成后处理 */
    @Override
    protected void AfterMachineInfoGetOver() {
        Log.d("ComAokema","AfterMachineInfoGetOver -----------------------------");
        iLoadingPresenter.AfterMachineInfoGetOver();
        closeComPort();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 跳转进入售卖页面
                Intent ii = new Intent(LoadingActivity.this, BuyActivity.class);
                startActivity(ii);
                finish();
            }
        }, 500);
    }


    @Override
    public void onBackPressed() {
        //不允许右键退出,必须使用按钮退出
        //super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(iLoadingPresenter != null){
            iLoadingPresenter.cancel();
            iLoadingPresenter = null;
        }
        MyObservable.getInstance().unregistObserver(this);
    }

    /** 主柜出货成功更改本地库存 */
    @Override
    public void updateLocalKuCun(String road_no) {
        if(iLoadingPresenter != null) {
            iLoadingPresenter.updateLocalKucun(road_no);
        }
    }

    @Override
    protected void updateDeskKucun(String road_no) {
        Log.e(TAG, "更新库存:" + road_no);
        RealmResults<GoodsInfo> goodsInfos = realm.where(GoodsInfo.class).equalTo("goodsBelong", "3")
                .equalTo("road_no", Integer.parseInt(road_no)).findAll();
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

    /** 设置关机重启时间 */
    private void setRestartTime() {
        String isOpen = SharedPreferencesUtils.getParam(this, "isOpen", "").toString();
        String restartTime = SharedPreferencesUtils.getParam(this, "restartTime", "").toString();
        String powerTime = SharedPreferencesUtils.getParam(this,"powerTime","").toString();

        // 打开状态
        if ("1".equals(isOpen) && !"".equals(restartTime) && !"".equals(powerTime)) {
            Intent intent = new Intent("com.ubox.auto_power_shut");
            intent.putExtra("effective", true); //true 为启动此功能， false 为关闭此功能
            intent.putExtra("shut_time", restartTime);
            intent.putExtra("power_time", powerTime);
            sendBroadcast(intent);
            return;
            //用户关闭状态
        }else if("0".equals(isOpen)){
            return;
            //从未设置状态
        }else {
            // 设置默认的3点关机重启
            Intent intent = new Intent("com.ubox.auto_power_shut");
            intent.putExtra("effective", true); //true 为启动此功能， false 为关闭此功能
            intent.putExtra("shut_time", "03:00");
            intent.putExtra("power_time", "03:05");
            SharedPreferencesUtils.setParam(this, "isOpen", "1");
            SharedPreferencesUtils.setParam(this, "restartTime", "03:00");
            SharedPreferencesUtils.setParam(this,"powerTime","03:05");
            sendBroadcast(intent);
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        if(observable instanceof SerialObservable){
            super.update(observable, o);
        }else if(observable instanceof MyObservable){
            finish();
        }
    }

    @Override
    public void showAlertView(int tag) {
        switch (tag){
            case SysConfig.NO_MACHINE_SN:
                alertView = new AlertView("提示", "机器编号未设定,请先设定!", null, new String[]{"确认"}, null,
                        this, AlertView.Style.Alert, DataUtil.dip2px(this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, int position) {
                        alertView.dismiss();
                    }
                }).setCancelable(false).setOnDismissListener(null);
                alertView.show();
                break;
            case SysConfig.MACHINE_CONNECT_ERROR:
                alertView = new AlertView("提示", "设备连接失败,点击确认后重试!", null, new String[]{"确认"}, null,
                        this, AlertView.Style.Alert, DataUtil.dip2px(this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, int position) {
                        // 重新启动
                        Intent ii = new Intent(LoadingActivity.this, Alarmreceiver.class);
                        ii.setAction("arui.alarm.action");
                        PendingIntent sender = PendingIntent.getBroadcast(LoadingActivity.this, 0,
                                ii, 0);
                        long firstime = SystemClock.elapsedRealtime();
                        AlarmManager am = (AlarmManager) LoadingActivity.this
                                .getSystemService(Context.ALARM_SERVICE);
                        // 10秒一个周期，不停的发送广播
                        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime,
                                5 * 1000, sender);
                        //退出程序
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                }).setCancelable(false).setOnDismissListener(null);
                alertView.show();
                break;
            default:
                break;
        }
    }
}
