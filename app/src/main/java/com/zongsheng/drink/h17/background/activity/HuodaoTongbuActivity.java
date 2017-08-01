package com.zongsheng.drink.h17.background.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Response;
import com.zongsheng.drink.h17.ComActivity;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.MarkLog;
import com.zongsheng.drink.h17.background.bean.BindGeZi;
import com.zongsheng.drink.h17.background.bean.MachineRoad;
import com.zongsheng.drink.h17.background.bean.UpateModel;
import com.zongsheng.drink.h17.background.common.MachineOrderRstListener;
import com.zongsheng.drink.h17.background.common.MachineOrderTask;
import com.zongsheng.drink.h17.base.BasePresenter;
import com.zongsheng.drink.h17.common.Constant;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.LoadingUtil;
import com.zongsheng.drink.h17.common.NetWorkRequImpl;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.common.ToastUtils;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.interfaces.INetWorkRequCallBackListener;
import com.zongsheng.drink.h17.interfaces.INetWorkRequInterface;
import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.observable.SerialObservable;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

import static com.yolanda.nohttp.RequestMethod.HEAD;

/**
 * 商品同步
 * Created by 谢家勋 on 2016/8/23.
 */
public class HuodaoTongbuActivity extends ComActivity implements INetWorkRequCallBackListener {

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_to_pingtai)
    TextView tvToPingtai;

    private AlertView alertView;
    /**
     * 数据库
     */
    private Realm realm;
    private Dialog dialog1;// 加载完成
    private Dialog dialogPingtai;
    private Map<String, String> map = new HashMap<>();
    private Map<String, String> geziMap = new HashMap<>();
    private Map<String, String> deskMap = new HashMap<>();

    /**
     * 服务器的货道信息
     */
    private List<MachineRoad> machineRoadList;
    private INetWorkRequInterface iNetWorkRequInterface = null;
    private String TAG = "HuodaoTongbuActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tongbu_goods);
        ButterKnife.bind(this);
        isInBackPage = true;
        realm = Realm.getDefaultInstance();
        tvTitle.setText("货道同步");
        tvToPingtai.setVisibility(View.VISIBLE);
        // 设置需要获取机器的基本信息
        isNeedInitMachineInfo = false;
        MyObservable.getInstance().registObserver(this);
        RealmResults<GoodsInfo> goodsInfoList = realm.where(GoodsInfo.class).findAll();
        if (goodsInfoList != null && goodsInfoList.size() > 0) {
            for (GoodsInfo goodsInfo : goodsInfoList) {
                if (SysConfig.MACHINE_TYPE_1.equals(goodsInfo.getGoodsBelong())) {
                    map.put(String.valueOf(goodsInfo.getRoad_no()), goodsInfo.getKuCun());
                } else if (SysConfig.MACHINE_TYPE_2.equals(goodsInfo.getGoodsBelong())) {
                    geziMap.put(String.valueOf(goodsInfo.getRoad_no()), goodsInfo.getKuCun());
                } else {
                    deskMap.put(String.valueOf(goodsInfo.getRoad_no()), goodsInfo.getKuCun());
                }
            }
        }
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @OnClick({R.id.rl_back, R.id.tv_pingtai, R.id.tv_to_pingtai})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.tv_pingtai:// 平台同步
                alertView = new AlertView("提示", "此操作可能耗时较久，确定继续吗？",
                        "取消", new String[]{"确认"}, null, HuodaoTongbuActivity.this, AlertView.Style.Alert, DataUtil.dip2px(HuodaoTongbuActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, int position) {
                        if (-1 == position) {
                            alertView.dismiss();
                        } else {
                            alertView.dismiss();
                            //显示dialog
                            askHttp1();
                        }
                    }
                }).setCancelable(false).setOnDismissListener(null);
                alertView.show();
                break;
            case R.id.tv_to_pingtai:// 同步到平台
                alertView = new AlertView("提示", "此操作可能消耗部分流量，确定继续吗？",
                        "取消", new String[]{"确认"}, null, HuodaoTongbuActivity.this, AlertView.Style.Alert, DataUtil.dip2px(HuodaoTongbuActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, int position) {
                        if (-1 == position) {
                            alertView.dismiss();
                        } else {
                            alertView.dismiss();
                            //显示dialog
                            askHttp();
                        }
                    }
                }).setCancelable(false).setOnDismissListener(null);
                alertView.show();
                break;
            default:
                break;
        }
    }

    //从平台同步
    private void askHttp1() {
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/synchro/machine/" + MyApplication.getInstance().getMachine_sn() + "/roads";
        if (iNetWorkRequInterface == null) {
            iNetWorkRequInterface = new NetWorkRequImpl(this);
        }
        dialogPingtai = LoadingUtil.createLoadingDialog(this, "正在从平台同步...", 1, R.drawable.ic_ios_juhua, true);
        dialogPingtai.show();
        iNetWorkRequInterface.request(url, 1, RequestMethod.GET);
    }

    //同步到平台
    private void askHttp() {
        String roadData = "";
        RealmResults<GoodsInfo> goodsInfoList = realm.where(GoodsInfo.class).findAll();
        // 提交
        if (goodsInfoList != null && goodsInfoList.size() > 0) {
            for (int i = 0; i < goodsInfoList.size(); i++) {
                String kucun = goodsInfoList.get(i).getKuCun();
                if ("".equals(kucun) || kucun == null) {
                    kucun = "0";
                }
                roadData += goodsInfoList.get(i).getMachineID() + "," + goodsInfoList.get(i).getRoad_no() + "," + goodsInfoList.get(i).getMaxKucun() + "," +
                        kucun + "," + goodsInfoList.get(i).getGoodsID() + "," + (Double.valueOf(goodsInfoList.get(i).getPrice()) / 10) + ";";
            }
        } else {
            ToastUtils.showToast(HuodaoTongbuActivity.this, "没有货道设置");
            return;
        }
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/synchro/put/roads?roadData=" + roadData;
        if (iNetWorkRequInterface == null) {
            iNetWorkRequInterface = new NetWorkRequImpl(this);
        }
        dialogPingtai = LoadingUtil.createLoadingDialog(this, "正在同步到平台...", 1, R.drawable.ic_ios_juhua, true);
        dialogPingtai.show();
        iNetWorkRequInterface.request(url, 0, RequestMethod.GET);
    }

    /**
     * 机器指令处理结果回调
     */
    private MachineOrderRstListener machineOrderRstListener = new MachineOrderRstListener() {
        @Override
        public void success() {
            // 设置本地数据
            updateLocalData();
        }

        @Override
        public void fail() {
            dialogDismiss(Constant.VSI_ERROR_02);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (iNetWorkRequInterface != null) {
            iNetWorkRequInterface.cancel();
            iNetWorkRequInterface = null;
        }
        if (realm != null) {
            realm.close();
        }
        MyObservable.getInstance().unregistObserver(this);
    }

    private int getBoxIndexByMachineSn(String machineSn) {
        int i = 0;
        int boxIndex = -1;
        for (BindGeZi bindGeZi : MyApplication.getInstance().getBindGeZis()) {
            if (bindGeZi.getMachineSn().equals(machineSn)) {
                if (MyApplication.getInstance().getGeziList().size() < (i + 1)) {
                    break;
                }
                boxIndex = MyApplication.getInstance().getGeziList().get(i);
                break;
            }
            i++;
        }
        return boxIndex;
    }

    /**
     * 更新本地数据
     */
    private void updateLocalData() {
        Log.e(TAG, "更新本地数据");
        //更新成功后删除本地存储的商品信息
        final RealmResults<GoodsInfo> goodsInfoList = realm.where(GoodsInfo.class).findAll();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                goodsInfoList.deleteAllFromRealm();
                // 插入新的货道信息
                for (MachineRoad machineRoad : machineRoadList) {
                    final GoodsInfo goodsInfo = new GoodsInfo();
                    if ("1".equals(machineRoad.getGoodsBelong())) {//goodsBelong为1的时候表示是饮料机商品
                        if (map.containsKey(machineRoad.getRoadNo())) {
                            String kucun = map.get(machineRoad.getRoadNo());
                            if (kucun == null || "".equals(kucun)) {
                                goodsInfo.setKuCun("0");
                            } else {
                                goodsInfo.setKuCun(kucun);
                            }
                        } else {
                            goodsInfo.setKuCun("0");
                        }
                    } else if ("2".equals(machineRoad.getGoodsBelong())) {//goodsBelong为2的时候表示是格子柜商品
                        if (geziMap.containsKey(machineRoad.getRoadNo())) {
                            String kucun = geziMap.get(machineRoad.getRoadNo());
                            if (kucun == null || "".equals(kucun)) {
                                goodsInfo.setKuCun("0");
                            } else {
                                goodsInfo.setKuCun(kucun);
                            }
                        } else {
                            goodsInfo.setKuCun("0");
                        }
                    } else {
                        if (deskMap.containsKey(machineRoad.getRoadNo())) {//goodsBelong为3的时候表示是副柜商品
                            String kucun = deskMap.get(machineRoad.getRoadNo());
                            if (kucun == null || "".equals(kucun)) {
                                goodsInfo.setKuCun("0");
                            } else {
                                goodsInfo.setKuCun(kucun);
                            }
                        } else {
                            goodsInfo.setKuCun("0");
                        }
                    }
                    goodsInfo.setGoodsName(machineRoad.getGoodsName());
                    goodsInfo.setPrice(String.valueOf((int) Double.parseDouble(machineRoad.getGoodsPrice())));
                    goodsInfo.setGoodsID(machineRoad.getGoodsId());
                    goodsInfo.setGoodsImage(machineRoad.getGoodsImage());
                    goodsInfo.setRoad_no(Integer.valueOf(machineRoad.getRoadNo()));
                    goodsInfo.setMaxKucun(Integer.valueOf(machineRoad.getRoadNum()));
                    goodsInfo.setOnlineKuCun(Integer.valueOf(machineRoad.getInventory()));
                    goodsInfo.setMachineID(machineRoad.getMachineSn());
                    goodsInfo.setGoodsCode(machineRoad.getGoodsId());
                    goodsInfo.setGoodsBelong(machineRoad.getGoodsBelong());
                    goodsInfo.setGoodsAbbreviation(machineRoad.getGoodsAbbreviation());
                    goodsInfo.setAscription(machineRoad.getAscription());
                    realm.copyToRealm(goodsInfo);
                }
            }
        });
        dialogPingtai.dismiss();
        dialog1 = LoadingUtil.createLoadingDialog(HuodaoTongbuActivity.this, "同步成功", 1, 0, false);// 开启登录完成dialog
        dialog1.show();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (dialog1 != null) {
                    dialog1.dismiss();
                }
            }
        }, SysConfig.L_TIME_1S);
    }

    /**
     * 主柜出货成功更改本地库存
     */
    @Override
    public void updateLocalKuCun(String road_no) {
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

    private void dialogDismiss(String sMsg) {
        if (dialogPingtai != null) {
            dialogPingtai.dismiss();
        }
        ToastUtils.showToast(HuodaoTongbuActivity.this, sMsg);
    }

    @Override
    public void onBackPressed() {
        //不允许右键退出,必须使用按钮退出
        //super.onPause();
    }

    @Override
    public void onSucceed(int what, Response<String> response) throws Exception {
        int responseCode = response.getHeaders().getResponseCode();// 服务器响应码
        if (responseCode == 200) {
            if (HEAD != response.getRequestMethod()) {
                JSONObject jsonResult = null;
                try {
                    jsonResult = new JSONObject(response.get());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (what) {
                    case 0://token
                        try {
                            if (dialogPingtai != null) {
                                dialogPingtai.dismiss();
                            }
                            // 如果成功
                            if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {

                                //写入操作日志
                                MarkLog.markLog("同步本地货道到平台", SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());

                                dialog1 = LoadingUtil.createLoadingDialog(HuodaoTongbuActivity.this, "同步成功", 1, 0, false);// 开启登录完成dialog
                                dialog1.show();
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        dialog1.dismiss();
                                    }
                                }, 1000);
                            } else {
                                dialogPingtai.dismiss();
                                ToastUtils.showToast(HuodaoTongbuActivity.this, jsonResult.getString("error"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1://token
                        try {
                            // 如果成功
                            if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                //写入操作日志
                                MarkLog.markLog("从平台同步货道", SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
                                Gson gson = new Gson();
                                Type type = new TypeToken<ArrayList<MachineRoad>>() {
                                }.getType();
                                machineRoadList = gson.fromJson(jsonResult.getString("machineRoadList"), type);
                                if (machineRoadList == null || machineRoadList.size() == 0) {
                                    dialogPingtai.dismiss();
                                    ToastUtils.showToast(HuodaoTongbuActivity.this, "同步失败,请重试!");
                                    return;
                                }
                                // 请求机器更改主柜货道信息
                                if (!machineConncted) {
                                    // 机器未连接
                                    dialogPingtai.dismiss();
                                    ToastUtils.showToast(HuodaoTongbuActivity.this, Constant.MAIN_MACHINA_DONOT_CON);
                                    return;
                                }
                                // 生成货道命令，测试设置商品编码
                                RealmResults<GoodsInfo> goodsInfoList = realm.where(GoodsInfo.class).equalTo("goodsBelong", "1").findAll();
                                Map<String, GoodsInfo> mainMachineGoodInfo = new HashMap<>();
                                for (GoodsInfo goodsInfo : goodsInfoList) {
                                    mainMachineGoodInfo.put(String.valueOf(goodsInfo.getRoad_no()), goodsInfo);
                                }
                                List<UpateModel> raodInfo = new ArrayList<>();
                                for (MachineRoad machineRoad : machineRoadList) {
                                    if ("1".equals(machineRoad.getGoodsBelong())) { // 1:主机 2:格子柜
                                        if ("".equals(machineRoad.getGoodsId()) || machineRoad.getGoodsId() == null) {
                                            raodInfo.add(DataUtil.setUpdateModel2(SysConfig.GOODSCODE, 0, null));
                                        } else if (mainMachineGoodInfo.get(machineRoad.getRoadNo()) != null && !mainMachineGoodInfo.get(machineRoad.getRoadNo()).getPrice().equals(machineRoad.getGoodsPrice())) {
                                            raodInfo.add(DataUtil.setUpdateModel2(SysConfig.UPDATE_PRICE, 0, machineRoad));
                                        } else if (mainMachineGoodInfo.get(machineRoad.getRoadNo()) == null) {
                                            raodInfo.add(DataUtil.setUpdateModel2(SysConfig.UPDATE_PRICE, 0, machineRoad));
                                        }
                                    } else if ("2".equals(machineRoad.getGoodsBelong())) {
                                        int boxIndex = getBoxIndexByMachineSn(machineRoad.getMachineSn());
                                        GoodsInfo goodsinfo = realm.where(GoodsInfo.class).equalTo("machineID", machineRoad.getMachineSn())
                                                .equalTo("road_no", Integer.parseInt(machineRoad.getRoadNo())).findFirst();
                                        if (goodsinfo == null) {
                                            raodInfo.add(DataUtil.setUpdateModel2(SysConfig.UPDATE_PRICE, boxIndex, machineRoad));
                                        } else if ((int) Double.parseDouble(goodsinfo.getPrice()) != (int) Double.parseDouble(machineRoad.getGoodsPrice())) {
                                            raodInfo.add(DataUtil.setUpdateModel2(SysConfig.UPDATE_PRICE, boxIndex, machineRoad));
                                        }
                                    } else {
                                        GoodsInfo goodsinfo = realm.where(GoodsInfo.class).equalTo("machineID", machineRoad.getMachineSn())
                                                .equalTo("road_no", Integer.parseInt(machineRoad.getRoadNo())).findFirst();
                                        if (goodsinfo == null) {
                                            raodInfo.add(DataUtil.setUpdateModel2(SysConfig.UPDATE_PRICE, 1, machineRoad));
                                        } else if ((int) Double.parseDouble(goodsinfo.getPrice()) != (int) Double.parseDouble(machineRoad.getGoodsPrice())) {
                                            raodInfo.add(DataUtil.setUpdateModel2(SysConfig.UPDATE_PRICE, 1, machineRoad));
                                        }
                                    }
                                }
                                int roadCount = MyApplication.getInstance().getRoadCount();
                                if (roadCount == 0) {
                                    dialogPingtai.dismiss();
                                    ToastUtils.showToast(HuodaoTongbuActivity.this, Constant.MAIN_MACHINA_DONOT_CON);
                                }
                                new MachineOrderTask(HuodaoTongbuActivity.this, roadCount, machineOrderRstListener, raodInfo).execute();
                            } else {
                                dialogPingtai.dismiss();
                                ToastUtils.showToast(HuodaoTongbuActivity.this, jsonResult.getString("error"));
                            }
                        } catch (Exception e) {
                            dialogPingtai.dismiss();
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) throws Exception {
        if (networkMillis > 5000 && dialogPingtai != null) {
            dialogPingtai.dismiss();
            ToastUtils.showToast(HuodaoTongbuActivity.this, "网络错误,请重试");
        }
        if (dialogPingtai != null) {
            dialogPingtai.dismiss();
        }
        ToastUtils.showToast(HuodaoTongbuActivity.this, "网络错误,请重试");
    }

    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof SerialObservable) {
            super.update(observable, o);
        } else if (observable instanceof MyObservable) {
            finish();
        }
    }
}
