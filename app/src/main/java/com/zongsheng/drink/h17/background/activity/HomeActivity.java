package com.zongsheng.drink.h17.background.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.CacheMode;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;
import com.zongsheng.drink.h17.ComActivity;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.background.bean.BindDesk;
import com.zongsheng.drink.h17.base.BasePresenter;
import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.MarkLog;
import com.zongsheng.drink.h17.background.bean.BindGeZi;
import com.zongsheng.drink.h17.background.bean.MachineState;
import com.zongsheng.drink.h17.common.Constant;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.L;
import com.zongsheng.drink.h17.common.LoadingUtil;
import com.zongsheng.drink.h17.common.SharedPreferencesUtils;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.common.ToastUtils;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.front.bean.QueHuoRecord;
import com.zongsheng.drink.h17.nohttp.CallServer;
import com.zongsheng.drink.h17.nohttp.HttpListener;
import com.zongsheng.drink.h17.observable.SerialObservable;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * 首页
 * Created by 谢家勋 on 2016/8/23.
 */
public class HomeActivity extends ComActivity implements OnItemClickListener {
    @BindView(R.id.rl_buhuo)
    RelativeLayout rlBuhuo;
    @BindView(R.id.rl_buman)
    RelativeLayout rlBuman;
    @BindView(R.id.rl_tongbu)
    RelativeLayout rlTongbu;
    @BindView(R.id.rl_add_gezi)
    RelativeLayout rlAddGezi;
    @BindView(R.id.rl_other)
    RelativeLayout rlOther;
    @BindView(R.id.rl_quit)
    RelativeLayout rlQuit;
    /**
     * 提醒弹窗
     */
    private AlertView alertView;
    /**
     * 数据库
     */
    private Realm realm;
    private Request<String> request;
    private Dialog dialog1;// 加载完成
    private Dialog dialogPingtai;

    /**
     * 格子柜列表
     */
    private List<BindGeZi> bindGeZiList = new ArrayList<>();
    /**
     * 格子补货成功数
     */
    private int geziIndex = 0;

    private List<BindDesk> bindDeskList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
        ButterKnife.bind(this);
        isInBackPage = true;
        realm = Realm.getDefaultInstance();
        // 设置需要获取机器的基本信息
        isNeedInitMachineInfo = false;
        MyObservable.getInstance().registObserver(this);
        MyApplication.getInstance().setWeihu(true);
        // 判断机器编号是否存在
        if ("".equals(MyApplication.getInstance().getInstance().getMachine_sn())) {
            alertView = new AlertView("提示", "机器编号未设定\n请确定机器配置文件是否正确放置\n处理完成后,请重启工控机\n如有疑问, 请联系客服!", null, new String[]{"确认"}, null,
                    this, AlertView.Style.Alert, DataUtil.dip2px(this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, int position) {
                    alertView.dismiss();
                }
            }).setCancelable(false).setOnDismissListener(null);
            alertView.show();
            return;
        }
        // 取得自营商品货比率
        requestRoadRatio();
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @OnClick({R.id.rl_quit, R.id.rl_other})
    void click(View view) {
        Intent ii = new Intent();
        switch (view.getId()) {
            case R.id.rl_quit:
                //写入操作日志
                MarkLog.markLog("退出机器管理端", SysConfig.LOG_LEVEL_NORMAL, MyApplication.getInstance().getMachine_sn());
                rlQuit.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rlQuit.setEnabled(true);
                    }
                }, 1000);
                ii.setAction(SysConfig.ENG_MODE_SWITCH);
                ii.putExtra("state", 0);
                sendBroadcast(ii);
                break;

            case R.id.rl_other:
                rlOther.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rlOther.setEnabled(true);
                    }
                }, 1000);
                // 跳转
                ii.setClass(this, OtherActivty.class);
                startActivity(ii);
                break;
        }

    }

    @OnClick({R.id.rl_buhuo, R.id.rl_buman, R.id.rl_tongbu, R.id.rl_add_gezi})
    public void onClick(View view) {
        // 判断机器编号是否存在
        if ("".equals(MyApplication.getInstance().getMachine_sn())) {
            alertView = new AlertView("提示", "机器编号未设定\n请确定机器配置文件是否正确放置\n处理完成后,请重启工控机\n如有疑问, 请联系客服!", null, new String[]{"确认"}, null,
                    this, AlertView.Style.Alert, DataUtil.dip2px(this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, int position) {
                    alertView.dismiss();
                }
            }).setCancelable(false).setOnDismissListener(null);
            alertView.show();
            return;
        }
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.rl_buhuo:// 补货
                rlBuhuo.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rlBuhuo.setEnabled(true);
                    }
                }, 500);
                // 跳转
                intent.setClass(this, BuhuoActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_buman:// 补满
                alertView = new AlertView("提示", "确定要一键补满所有货道吗？", "取消", new String[]{"确认"}, null,
                        HomeActivity.this, AlertView.Style.Alert, DataUtil.dip2px(HomeActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), this).setCancelable(false).setOnDismissListener(null);
                alertView.show();
                break;
            case R.id.rl_tongbu:// 同步
                rlBuman.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rlBuman.setEnabled(true);
                    }
                }, 1000);
                // 跳转
                intent.setClass(this, TongbuManagerActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_add_gezi:// 格子柜管理
                rlAddGezi.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rlAddGezi.setEnabled(true);
                    }
                }, 1000);
                startActivity(new Intent(this, GeZiActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(Object o, int position) {
        if (-1 == position) {
            alertView.dismiss();
        } else {
            //  进去补满修改数据库
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    amendDb();
                }
            }, 200);

        }
    }

    //补满数据库
    private void amendDb() {
        dialogPingtai = LoadingUtil.createLoadingDialog(this, "处理中...", 1, R.drawable.ic_ios_juhua, true, true);
        dialogPingtai.show();
        String roadData = "";
        RealmResults<GoodsInfo> goodsInfoList = realm.where(GoodsInfo.class)
                .equalTo("goodsBelong", "1").findAll();
        if (goodsInfoList.size() > 0 && goodsInfoList != null) {
            if (!realm.isInTransaction()) {
                realm.beginTransaction();
            }
            for (GoodsInfo goodsInfo : goodsInfoList) {
                goodsInfo.setLocalKuCunForCheck(Integer.parseInt(goodsInfo.getKuCun()));
                goodsInfo.setKuCun(String.valueOf(goodsInfo.getMaxKucun()));
                goodsInfo.setOnlineKuCun(goodsInfo.getMaxKucun());
                String kucun = goodsInfo.getKuCun();
                if ("".equals(kucun) || kucun == null) {
                    kucun = String.valueOf(goodsInfo.getMaxKucun());
                }
                roadData += goodsInfo.getRoad_no() + "," + goodsInfo.getGoodsID() + "," + (Double.valueOf(goodsInfo.getPrice()) * 0.1) + "," +
                        goodsInfo.getMaxKucun() + "," + (goodsInfo.getMaxKucun() - goodsInfo.getLocalKuCunForCheck()) + "," + kucun + ";";
            }
            realm.commitTransaction();
            // TODO 删除机器的缺货警告信息
            final RealmResults<QueHuoRecord> queHuoRecords = realm.where(QueHuoRecord.class).equalTo("machineSn", MyApplication.getInstance().getMachine_sn())
                    .equalTo("isUploaded", "0").findAll();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (QueHuoRecord queHuoRecord : queHuoRecords) {
                        queHuoRecord.setIsUploaded("1");
                    }
                }
            });
            askHttp(roadData);
        } else {
            ToastUtils.showToast(HomeActivity.this, "货道尚未设置");
            dialogPingtai.dismiss();
            return;
        }

    }

    //访问网络
    private void askHttp(String ss) {
        Log.e("补货", "补货数据:" + ss);
        // 创建请求对象。
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/post/" + MyApplication.getInstance().getMachine_sn() + "/replenishmentinfo?goodsInfos=" + ss;
        request = NoHttp.createStringRequest(url, RequestMethod.GET);
        //设置为必须网络
        request.setCacheMode(CacheMode.ONLY_REQUEST_NETWORK);
        if (request != null) {
            // 添加到请求队列
            CallServer.getRequestInstance().add(this, 0, request, httpListener, true, false);
        } else {
            ToastUtils.showToast(HomeActivity.this, "网络错误,请重试");
            dialogPingtai.dismiss();
        }
    }

    /**
     * 副柜开始进行补货操作
     */
    private void deskBuhuo() {
        BindDesk bindDesk = bindDeskList.get(0);
        String roadData = "";
        RealmResults<GoodsInfo> goodsInfosList = realm.where(GoodsInfo.class).equalTo("goodsBelong", "2")
                .equalTo("machineID", bindDesk.getMainMachineSn()).findAll();
        if (goodsInfosList != null && goodsInfosList.size() > 0) {
            if (!realm.isInTransaction()) {
                realm.beginTransaction();
            }
            for (GoodsInfo goodsInfo : goodsInfosList) {
                goodsInfo.setLocalKuCunForCheck(Integer.parseInt(goodsInfo.getKuCun()));
                goodsInfo.setKuCun(String.valueOf(goodsInfo.getMaxKucun()));
                goodsInfo.setOnlineKuCun(goodsInfo.getMaxKucun());
                String kucun = goodsInfo.getKuCun();
                if ("".equals(kucun) || kucun == null) {
                    kucun = String.valueOf(goodsInfo.getMaxKucun());
                }
                roadData += goodsInfo.getRoad_no() + "," + goodsInfo.getGoodsID() + "," + (Double.valueOf(goodsInfo.getPrice()) * 0.1) + "," +
                        goodsInfo.getMaxKucun() + "," + (goodsInfo.getMaxKucun() - goodsInfo.getLocalKuCunForCheck()) + "," + kucun + ";";
            }

            realm.commitTransaction();
            // TODO 删除机器的缺货警告信息
            final RealmResults<QueHuoRecord> queHuoRecords = realm.where(QueHuoRecord.class).equalTo("machineSn", bindDesk.getMachineSn())
                    .equalTo("isUploaded", "0").findAll();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (QueHuoRecord queHuoRecord : queHuoRecords) {
                        queHuoRecord.setIsUploaded("1");
                    }
                }
            });
            String s = machineBuHuo(1, 0);
            if (!"".equals(s)) {
                dialogPingtai.dismiss();
                ToastUtils.showToast(this, "副柜补货失败(主机通讯失败),请稍后重试。");
                return;
            }
            //}
            // 创建请求对象。
            String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/post/" + bindDesk.getMachineSn() + "/replenishmentinfo?goodsInfos=" + roadData;
            request = NoHttp.createStringRequest(url, RequestMethod.GET);
            //设置为必须网络
            request.setCacheMode(CacheMode.ONLY_REQUEST_NETWORK);
            if (request != null) {
                request.setTag(bindDesk.getMachineSn());
                // 添加到请求队列
                CallServer.getRequestInstance().add(this, 4, request, httpListener, true, false);
            } else {
                ToastUtils.showToast(HomeActivity.this, "网络错误,请重试");
                dialogPingtai.dismiss();
            }

        }
    }

    /**
     * 格子柜补货
     */
    private void geziBuhuo() {
        if (geziIndex > bindGeZiList.size()) {
            dialogPingtai.dismiss();
            dialog1 = LoadingUtil.createLoadingDialog(HomeActivity.this, "补货成功", 1, 0, false);// 开启登录完成dialog
            dialog1.show();
            getRoadEmptyInfo();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    dialog1.dismiss();
                }
            }, 1000);
            return;
        }
        String roadData = "";
        BindGeZi bindGeZi = bindGeZiList.get(geziIndex - 1);
        RealmResults<GoodsInfo> goodsInfoList = realm.where(GoodsInfo.class)
                .equalTo("goodsBelong", "2").equalTo("machineID", bindGeZi.getMachineSn()).findAll();
        if (goodsInfoList != null && goodsInfoList.size() > 0) {
            if (!realm.isInTransaction()) {
                realm.beginTransaction();
            }
            for (GoodsInfo goodsInfo : goodsInfoList) {
                goodsInfo.setLocalKuCunForCheck(Integer.parseInt(goodsInfo.getKuCun()));
                goodsInfo.setKuCun("1");
                goodsInfo.setOnlineKuCun(1);
                String kucun = goodsInfo.getKuCun();
                if ("".equals(kucun) || kucun == null) {
                    kucun = String.valueOf(goodsInfo.getMaxKucun());
                }
                roadData += goodsInfo.getRoad_no() + "," + goodsInfo.getGoodsID() + "," + (Double.valueOf(goodsInfo.getPrice()) * 0.1) + "," +
                        goodsInfo.getMaxKucun() + "," + (goodsInfo.getMaxKucun() - goodsInfo.getLocalKuCunForCheck()) + "," + kucun + ";";
            }
            realm.commitTransaction();
            // TODO 删除机器的缺货警告信息
            final RealmResults<QueHuoRecord> queHuoRecords = realm.where(QueHuoRecord.class).equalTo("machineSn", bindGeZi.getMachineSn())
                    .equalTo("isUploaded", "0").findAll();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (QueHuoRecord queHuoRecord : queHuoRecords) {
                        queHuoRecord.setIsUploaded("1");
                    }
                }
            });
            String s = machineBuHuo(geziIndex + 1, 0);
            if (!"".equals(s)) {
                dialogPingtai.dismiss();
                ToastUtils.showToast(this, "格子柜补货失败(主机通讯失败),请稍后重试。");
                return;
            }
            //}
            // 创建请求对象。
            String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/post/" + bindGeZi.getMachineSn() + "/replenishmentinfo?goodsInfos=" + roadData;
            request = NoHttp.createStringRequest(url, RequestMethod.GET);
            //设置为必须网络
            request.setCacheMode(CacheMode.ONLY_REQUEST_NETWORK);
            if (request != null) {
                request.setTag(bindGeZi.getMachineSn());
                // 添加到请求队列
                CallServer.getRequestInstance().add(this, 3, request, httpListener, true, false);
            } else {
                ToastUtils.showToast(HomeActivity.this, "网络错误,请重试");
                dialogPingtai.dismiss();
            }

        } else {
            geziIndex++;
            geziBuhuo();
        }
    }

    /**
     * 取得自营商品货比率
     */
    private void requestRoadRatio() {
        // 创建请求对象。
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/" + MyApplication.getInstance().getMachine_sn() + "/roadratio";
        request = NoHttp.createStringRequest(url, RequestMethod.GET);
        //设置为必须网络
        request.setCacheMode(CacheMode.ONLY_REQUEST_NETWORK);
        if (request != null) {
            // 添加到请求队列
            CallServer.getRequestInstance().add(this, 2, request, httpListener, true, false);
        }
    }

    /**
     * 网络处理
     */
    private HttpListener<String> httpListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            int responseCode = response.getHeaders().getResponseCode();// 服务器响应码
            if (responseCode == 200) {
                if (RequestMethod.HEAD != response.getRequestMethod()) {
                    JSONObject jsonResult = null;
                    try {
                        jsonResult = new JSONObject(response.get());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    L.v(SysConfig.ZPush, "----->" + jsonResult.toString());
                    switch (what) {
                        case 0://token
                            try {
                                // 如果成功
                                if (jsonResult != null && jsonResult.getString(SysConfig.JSON_KEY_ERROR_CODE).equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                    //写入操作日志
                                    MarkLog.markLog("饮料机" + MyApplication.getInstance().getMachine_sn() + "一键补满", SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
                                    RealmResults<BindDesk> desks = realm.where(BindDesk.class).findAll().sort("createTime", Sort.ASCENDING);
                                    bindDeskList = realm.copyFromRealm(desks);

                                    if (bindDeskList == null || bindDeskList.size() == 0) {
                                        dialogPingtai.dismiss();
                                        dialog1 = LoadingUtil.createLoadingDialog(HomeActivity.this, "补货成功", 1, 0, false);// 开启登录完成dialog
                                        dialog1.show();
                                        new Handler().postDelayed(new Runnable() {
                                            public void run() {
                                                dialog1.dismiss();
                                            }
                                        }, 1000);
                                    } else {
                                        // 副柜补货开始
                                        deskBuhuo();
                                    }
                                } else {
                                    String res = "";
                                    if (jsonResult != null) {
                                        res = jsonResult.getString(SysConfig.JSON_KEY_ERROR);
                                    } else {
                                        res = Constant.NO_ERROR_MSG;
                                    }
                                    dialogPingtai.dismiss();
                                    alertView = new AlertView("提示", res, null,
                                            new String[]{"确认"}, null, HomeActivity.this, AlertView.Style.Alert, DataUtil.dip2px(HomeActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                                        @Override
                                        public void onItemClick(Object o, int position) {
                                            if (-1 == position) {
                                                alertView.dismiss();
                                            } else {
                                                alertView.dismiss();
                                            }
                                        }
                                    }).setCancelable(true).setOnDismissListener(null);
                                    alertView.show();
                                }
                            } catch (Exception e) {
                                dialogPingtai.dismiss();
                                ToastUtils.showToast(HomeActivity.this, "补货失败,请重试!");
                                e.printStackTrace();
                            }
                            break;
                        case 1:
                            try {
                                // 如果成功
                                if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<ArrayList<MachineState>>() {
                                    }.getType();
                                    final List<MachineState> stateList = gson.fromJson(jsonResult.getString("machineList"), type);

                                    if (stateList != null && stateList.size() > 0) {
                                        // 取得数据后删除原有数据
                                        final RealmResults<MachineState> states = realm.where(MachineState.class).findAll();
                                        // All changes to data must happen in a transaction
                                        if (!realm.isInTransaction()) {
                                            realm.beginTransaction();
                                        }
                                        states.deleteAllFromRealm();
                                        realm.commitTransaction();

                                        if (!realm.isInTransaction()) {
                                            realm.beginTransaction();
                                        }
                                        realm.copyToRealm(stateList);
                                        realm.commitTransaction();
                                    } else {
                                        return;
                                    }
                                } else {
                                    ToastUtils.showToast(HomeActivity.this, jsonResult.getString("error"));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;

                        case 2: // 自营商品货比率
                            try {
                                // 如果成功
                                if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                    String roadRatio = jsonResult.getString("roadRatio");
                                    if (roadRatio != null && !"".equals(roadRatio)) {
                                        SharedPreferencesUtils.setParam(HomeActivity.this, "roadRatio", String.valueOf(Double.parseDouble(roadRatio)));
                                        MyApplication.getInstance().setRoadRatio(roadRatio);
                                    }
                                    MyApplication.getInstance().setSnexist(true);
                                } else if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_REQ_ERROR)) {
                                    Toast.makeText(HomeActivity.this, jsonResult.getString("error"), Toast.LENGTH_LONG).show();
                                    if (jsonResult.getString("error").equals(SysConfig.ERROR_INFO_SNEXIST)) {
                                        MyApplication.getInstance().setSnexist(false);
                                    } else {
                                        MyApplication.getInstance().setSnexist(true);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;

                        case 3:// 格子柜补货
                            try {
                                // 如果成功
                                if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                    //写入操作日志
                                    MarkLog.markLog("格子柜" + (String) request.getTag() + "一键补满", SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
                                    geziIndex++;
                                    geziBuhuo();
                                } else {
                                    String res = "";
                                    if (jsonResult != null) {
                                        res = jsonResult.getString(SysConfig.JSON_KEY_ERROR);
                                    } else {
                                        res = Constant.NO_ERROR_MSG;
                                    }
                                    dialogPingtai.dismiss();
                                    alertView = new AlertView("提示", res, null,
                                            new String[]{"确认"}, null, HomeActivity.this, AlertView.Style.Alert, DataUtil.dip2px(HomeActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                                        @Override
                                        public void onItemClick(Object o, int position) {
                                            if (-1 == position) {
                                                alertView.dismiss();
                                            } else {
                                                alertView.dismiss();
                                            }
                                        }
                                    }).setCancelable(true).setOnDismissListener(null);
                                    alertView.show();
                                }
                            } catch (Exception e) {
                                dialogPingtai.dismiss();
                                ToastUtils.showToast(HomeActivity.this, "补货失败,请重试!");
                                e.printStackTrace();
                            }
                            break;

                        case 4:
                            try {
                                // 如果成功
                                if (jsonResult != null && jsonResult.getString(SysConfig.JSON_KEY_ERROR_CODE).equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                    //写入操作日志
                                    MarkLog.markLog("副柜" + (String) request.getTag() + "一键补满", SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
                                    // 取得格子柜列表
                                    RealmResults<BindGeZi> cabinetInfos = realm.where(BindGeZi.class).findAll();
                                    cabinetInfos = cabinetInfos.sort("createTime", Sort.ASCENDING);
                                    bindGeZiList = realm.copyFromRealm(cabinetInfos);
                                    if (bindGeZiList == null || bindGeZiList.size() == 0) {
                                        dialogPingtai.dismiss();
                                        dialog1 = LoadingUtil.createLoadingDialog(HomeActivity.this, "补货成功", 1, 0, false);// 开启登录完成dialog
                                        dialog1.show();
                                        new Handler().postDelayed(new Runnable() {
                                            public void run() {
                                                dialog1.dismiss();
                                            }
                                        }, 1000);
                                    } else {
                                        // 格子柜补货开始
                                        geziIndex = 1;
                                        geziBuhuo();
                                    }
                                } else {
                                    String res = "";
                                    if (jsonResult != null) {
                                        res = jsonResult.getString(SysConfig.JSON_KEY_ERROR);
                                    } else {
                                        res = Constant.NO_ERROR_MSG;
                                    }
                                    dialogPingtai.dismiss();
                                    alertView = new AlertView("提示", res, null,
                                            new String[]{"确认"}, null, HomeActivity.this, AlertView.Style.Alert, DataUtil.dip2px(HomeActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                                        @Override
                                        public void onItemClick(Object o, int position) {
                                            if (-1 == position) {
                                                alertView.dismiss();
                                            } else {
                                                alertView.dismiss();
                                            }
                                        }
                                    }).setCancelable(true).setOnDismissListener(null);
                                    alertView.show();
                                }
                            } catch (Exception e) {
                                dialogPingtai.dismiss();
                                ToastUtils.showToast(HomeActivity.this, "补货失败,请重试!");
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
        public void onFailed(int what, String url, final Object tag, Exception exception, int responseCode, long networkMillis) {
            if (dialogPingtai != null) {
                dialogPingtai.dismiss();
            }
            if (networkMillis > 8000) {
                if (what != 2) {
                    alertView = new AlertView("提示", "网络连接出错，可以在APP端完成补货\nAPP首页-右上角加号-\n-查看今日运营报告-完成补货", null,
                            new String[]{"确认"}, null, HomeActivity.this, AlertView.Style.Alert, DataUtil.dip2px(HomeActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            if (-1 == position) {
                                alertView.dismiss();
                            } else {
                                alertView.dismiss();
                            }
                        }
                    }).setCancelable(true).setOnDismissListener(null);
                    alertView.show();
                }
            }
            if (what != 2) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertView = new AlertView("提示", "网络连接出错，可以在APP端完成补货\nAPP首页-右上角加号-\n-查看今日运营报告-完成补货", null,
                                new String[]{"确认"}, null, HomeActivity.this, AlertView.Style.Alert, DataUtil.dip2px(HomeActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                            @Override
                            public void onItemClick(Object o, int position) {
                                if (-1 == position) {
                                    alertView.dismiss();
                                } else {
                                    alertView.dismiss();
                                }
                            }
                        }).setCancelable(true).setOnDismissListener(null);
                        alertView.show();
                    }
                }, 200);
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (request != null) {
            request.cancel();
        }
        if (realm != null) {
            realm.close();
        }
        MyObservable.getInstance().unregistObserver(this);
    }

    @Override
    public void onBackPressed() {
        //不允许右键退出,必须使用按钮退出
        //super.onPause();
    }

    /**
     * 主柜出货成功更改本地库存
     */
    @Override
    public void updateLocalKuCun(String road_no) {
        RealmResults<GoodsInfo> goodsInfos = realm.where(GoodsInfo.class).equalTo("goodsBelong", "1").equalTo("road_no", Integer.parseInt(road_no)).findAll();
        final GoodsInfo goodsInfo = goodsInfos.where().findFirst();
        if (goodsInfo != null && goodsInfo.getKuCun() != null && !"".equals(goodsInfo.getKuCun())) {
            if (!realm.isInTransaction()) {
                realm.beginTransaction();
            }
            if (Integer.parseInt(goodsInfo.getKuCun()) >= 2) {
                goodsInfo.setKuCun(String.valueOf(Integer.parseInt(goodsInfo.getKuCun()) - 1));
            }
            if (goodsInfo.getOnlineKuCun() >= 2) {
                goodsInfo.setOnlineKuCun(goodsInfo.getOnlineKuCun() - 1);
            }
            realm.commitTransaction();
        }
    }

    @Override
    protected void updateDeskKucun(String road_no) {
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

    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof SerialObservable) {
            super.update(observable, o);
        } else if (observable instanceof MyObservable) {
            finish();
        }
    }
}
