package com.zongsheng.drink.h17.background.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.CacheMode;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;
import com.zongsheng.drink.h17.ComActivity;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.MarkLog;
import com.zongsheng.drink.h17.background.bean.BindDesk;
import com.zongsheng.drink.h17.background.bean.BindGeZi;
import com.zongsheng.drink.h17.background.bean.UpateModel;
import com.zongsheng.drink.h17.background.common.BuhuoForAKMTask;
import com.zongsheng.drink.h17.background.common.MachineOrderRstListener;
import com.zongsheng.drink.h17.background.common.MachineOrderTask;
import com.zongsheng.drink.h17.background.common.OpenCabinetDoorForAKMTask;
import com.zongsheng.drink.h17.background.fragment.DeskFragment;
import com.zongsheng.drink.h17.background.fragment.DrinkFragment;
import com.zongsheng.drink.h17.background.fragment.GeZiFragment;
import com.zongsheng.drink.h17.base.BasePresenter;
import com.zongsheng.drink.h17.common.Constant;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.HideKeyBoard;
import com.zongsheng.drink.h17.common.L;
import com.zongsheng.drink.h17.common.LoadingUtil;
import com.zongsheng.drink.h17.common.MyCountDownTimer;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.common.ToastUtils;
import com.zongsheng.drink.h17.common.mylistener.IListener;
import com.zongsheng.drink.h17.common.mylistener.ListenerManager;
import com.zongsheng.drink.h17.common.popupwindow.ActionItem;
import com.zongsheng.drink.h17.common.popupwindow.TitlePopup;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.front.bean.QueHuoRecord;
import com.zongsheng.drink.h17.nohttp.CallServer;
import com.zongsheng.drink.h17.nohttp.HttpListener;
import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.observable.SerialObservable;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
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


/**
 * 后台
 * Created by 谢家勋 on 2016/8/22.
 */
public class BuhuoActivity extends ComActivity implements IListener {
    @BindView(R.id.rl_back)
    RelativeLayout rlBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.iv_homepage_down)
    ImageView ivHomepageDown;
    @BindView(R.id.rl_top)
    RelativeLayout rlTop;
    @BindView(R.id.ll_top_click)
    LinearLayout llTopClick;
    @BindView(R.id.rl_shadow)
    RelativeLayout rlShadow;
    /**
     * 选中项目 1饮料，2弹簧机(副柜)，3格子柜
     */
    private int selectType = 1;

    private static final String TAG = "BuhuoActivity";

    /**
     * mFragments集合
     */
    private Fragment[] mFragments;
    /**
     * mFragments管理器
     */
    private FragmentManager fragmentManager;
    /**
     * Fragment的提交
     */
    private FragmentTransaction fragmentTransaction;
    /**
     * popupwindow
     */
    private TitlePopup titlePopup;
    private int width = 0;
    private Dialog dialogPingtai;
    private Dialog loadingDialogEnd;// 加载完成

    private Request<String> request;

    private String ITEMACTION;
    private Realm realm;
    /**
     * 数据库中取出在格子柜管理中取得的格子柜信息
     */
    private List<BindGeZi> bindGeZiList;
    /**
     * 数据库中取出在格子柜管理中的副柜信息
     */
    private List<BindDesk> bindDeskList;

    private Intent intent1;

    AlertView alertView;

    private String machineSn = "";
    private String goodsInfoJsonSave = "";
    private String goodsBelong = "";

    private String mNotConnectTitle = "";

    /**
     * 数据处理保存用
     */
    private List<GoodsInfo> goodsInfoList;

    private MachineFaultTimer machineFaultTimer;

    /**
     * 格子柜的串口通讯
     */
    // ComCabinet comCabinet;

    //是否有绑定的格子柜信息
    private boolean isHaveGezi;
    //是否有绑定的副柜信息
    private boolean isHaveDesk;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buhuo);
        ButterKnife.bind(this);
        isInBackPage = true;
        // 设置需要获取机器的基本信息
        isNeedInitMachineInfo = false;

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        MyObservable.getInstance().registObserver(this);
        ListenerManager.getInstance().registerListtener(this);
        realm = Realm.getDefaultInstance();
        initType();
        initMenu();
        initPop();
        machineFaultTimer = new MachineFaultTimer(2 * SysConfig.L_REQ_AG_TIME_10, 2 * SysConfig.L_REQ_AG_TIME_10);
        machineFaultTimer.start();
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    private void initType() {

    }

    private class MachineFaultTimer extends MyCountDownTimer {
        MachineFaultTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕
            Log.e(TAG, "开始获取故障信息!");
            getMachineFault2();
            if (machineFaultTimer != null) {
                machineFaultTimer.start();
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
        }
    }


    /**
     * 初始化menu
     */
    private void initPop() {
        // 获取本地的系统参数
        if ("1".equals(MyApplication.getInstance().getMachineType())) {
            ITEMACTION = "饮料柜";
        } else {
            ITEMACTION = "食品柜";
        }

        // 初始化pop
        titlePopup = new TitlePopup(BuhuoActivity.this, width / 4, ViewGroup.LayoutParams.WRAP_CONTENT);
        //titlePopup.
        titlePopup.addAction(new ActionItem(ITEMACTION));

        //获取数据中里边绑定的副柜信息
        RealmResults<BindDesk> bindDesk = realm.where(BindDesk.class).findAll();
        bindDeskList = realm.copyFromRealm(bindDesk);
        if (bindDeskList.size() != 0) {
            isHaveDesk = true;
        }

        if (isHaveDesk) {
            for (int i = 0; i < bindDeskList.size(); i++) {
                BindDesk bindDesk1 = bindDeskList.get(i);
                String machineSn = bindDesk1.getMachineSn();
                if (machineSn != null && machineSn.length() > 4) {
                    machineSn = machineSn.substring(machineSn.length() - 4);
                }
                titlePopup.addAction(new ActionItem("副柜(" + machineSn + ")"));
            }
        }

        //获取数据库里面绑定的格子柜的数量和型号
        RealmResults<BindGeZi> bindGeZis = realm.where(BindGeZi.class).findAll();
        bindGeZiList = realm.copyFromRealm(bindGeZis);
        if (bindGeZis.size() != 0) {
            isHaveGezi = true;
        }
        if (isHaveGezi) {
            for (int i = 0; i < bindGeZiList.size(); i++) {
                BindGeZi bindGeZi = bindGeZiList.get(i);
                String machineSn = bindGeZi.getMachineSn();
                if (machineSn != null && machineSn.length() > 4) {
                    machineSn = machineSn.substring(machineSn.length() - 4);
                }
                titlePopup.addAction(new ActionItem("格子柜(" + machineSn + ")"));
            }
        }

        // 设置选中项目
        titlePopup.setSelectedPosition(0);
        //TODO:这里点击切换待补货的格子柜
        titlePopup.setItemOnClickListener(new TitlePopup.OnItemOnClickListener() {
            @Override
            public void onItemClick(ActionItem item, int position) {
                tabClick(position);
                selectType = position + 1;
                tvTitle.setText(titlePopup.getAction(position).mTitle);

                if (position != 0) {
                    if (isHaveGezi && !isHaveDesk) {
                        mNotConnectTitle = bindGeZiList.get(position - 1).getMachineSn();
                    }

                    if (isHaveGezi && isHaveDesk) {
                        if (position == 1) {
                            mNotConnectTitle = bindDeskList.get(position - 1).getMachineSn();
                        }
                        if (position >= 2) {
                            mNotConnectTitle = bindDeskList.get(position - 2).getMachineSn();
                        }
                    }
                    if (!isHaveGezi && isHaveDesk) {
                        mNotConnectTitle = bindDeskList.get(position - 1).getMachineSn();
                    }
                }
                // 设置选中项目
                titlePopup.setSelectedPosition(position);
            }
        });
    }

    /**
     * 初始化menu
     */
    private void initMenu() {
        mFragments = new Fragment[3];// 3大模块
        fragmentManager = getSupportFragmentManager();
        mFragments[0] = fragmentManager.findFragmentById(R.id.fragment_drink1);//
        mFragments[1] = fragmentManager.findFragmentById(R.id.fragment_desk1);//
        mFragments[2] = fragmentManager.findFragmentById(R.id.fragment_gezi1);
        FramentcommitAllowingStateLoss();
        fragmentTransaction.show(mFragments[0])
                .commitAllowingStateLoss();// 3个模块提交
    }


    /**
     * tab的点击/选择格子柜类型
     */
    public void tabClick(int tags) {
        // TODO: 2016/9/18 当点击其他格子柜的时候  替换掉这个格子柜的信息
        FramentcommitAllowingStateLoss();
//        if (tags != 0) {
//            selectType = 2;
//            //保存当前点击的tags的值
////            setTag1111(tags);
//            ((DeskFragment) mFragments[1]).setSelectGeZi(bindDeskList.get(tags - 1));
//            fragmentTransaction.show(mFragments[1]).commitAllowingStateLoss();
//        } else {
//            selectType = 1;
//            fragmentTransaction.show(mFragments[0]).commitAllowingStateLoss();
//        }

        if (tags == 0) {
            selectType = 1;
            fragmentTransaction.show(mFragments[0]).commitAllowingStateLoss();
        }

        if (isHaveDesk) {
            if (tags == 1) {
                ((DeskFragment) mFragments[1]).setSelectDesk(bindDeskList.get(tags - 1));
                fragmentTransaction.show(mFragments[1]).commitAllowingStateLoss();
                selectType = 2;
            }
            if (tags >= 2) {
                ((GeZiFragment) mFragments[2]).setSelectGeZi(bindGeZiList.get(tags - 2));
                fragmentTransaction.show(mFragments[2]).commitAllowingStateLoss();
                selectType = 3;
            }
        }

        if (!isHaveDesk && isHaveGezi) {
            if (tags >= 1) {
                ((GeZiFragment) mFragments[2]).setSelectGeZi(bindGeZiList.get(tags - 1));
                fragmentTransaction.show(mFragments[2]).commitAllowingStateLoss();
                selectType = 3;
            }
        }
    }

    /**
     * 3个Fragment提交
     */
    private void FramentcommitAllowingStateLoss() {
        fragmentTransaction = fragmentManager.beginTransaction()
                .hide(mFragments[0]).hide(mFragments[1]).hide(mFragments[2]);// 2个模块
    }

    /**
     * fragement传递result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFragments[0].onActivityResult(requestCode, resultCode, data);
        mFragments[1].onActivityResult(requestCode, resultCode, data);
        mFragments[2].onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.ll_top_click)
    public void onClick() {
        titlePopup.show(llTopClick);
    }

    @OnClick(R.id.rl_back)
    public void back() {
        alertView = new AlertView("提示", Constant.BUHUO_NOSUBMIT, "取消",
                new String[]{"确认"}, null, this, AlertView.Style.Alert, DataUtil.dip2px(this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (-1 == position) {
                    alertView.dismiss();
                } else {
                    alertView.dismiss();
                    finish();
                }
            }
        }).setCancelable(true).setOnDismissListener(null);

        if (selectType == 1 && mFragments[0] != null) {
            if (((DrinkFragment) mFragments[0]).isChanged) {
                alertView.show();
                return;
            }
        } else if (selectType == 2 && mFragments[1] != null) {
            if (((DeskFragment) mFragments[1]).isChanged) {
                alertView.show();
                return;
            }
        } else if (selectType == 3 && mFragments[2] != null) {
            if (((GeZiFragment) mFragments[2]).isChanged) {
                alertView.show();
                return;
            }
        }
        finish();
    }

    @Override
    public void notifyAllActivity(String str) {
        if ("show".equals(str)) {
            rlShadow.setVisibility(View.VISIBLE);
            rlShadow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        } else if ("close".equals(str)) {
            rlShadow.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (HideKeyBoard.isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        if (request != null) {
            request.cancel();
        }
        if (machineFaultTimer != null) {
            machineFaultTimer.cancel();
            machineFaultTimer = null;
        }
        ListenerManager.getInstance().unRegisterListener(this);
        MyObservable.getInstance().unregistObserver(this);
        super.onDestroy();
    }

    /**
     * 澳柯玛格子柜提交补货
     */
    public void confirmForGeziMachineForAKM(final List<GoodsInfo> goodsInfoList, final String machineSn, final int boxIndex) {
        if (goodsInfoList == null || goodsInfoList.size() == 0) {
            ToastUtils.showToast(this, Constant.GOODS_DATA_ERROR);
            return;
        }
        Map<Integer, GoodsInfo> realmGoodsInfoMap = new HashMap<>();
        RealmResults<GoodsInfo> goodsInfoResults = realm.where(GoodsInfo.class).equalTo("goodsBelong", "2")
                .equalTo("machineID", machineSn).findAll();
        for (GoodsInfo goodsInfo : goodsInfoResults) {
            realmGoodsInfoMap.put(goodsInfo.getRoad_no(), goodsInfo);
        }
        this.goodsInfoList = goodsInfoList;
        this.machineSn = machineSn;
        goodsBelong = "2";

        boolean hasEmpty = false;
        boolean temp = false; //价格修改标识位
        List<UpateModel> roadNoList = new ArrayList<>();
        for (GoodsInfo goodsInfo : goodsInfoList) {
            if (goodsInfo == null || goodsInfo.getGoodsCode() == null || "".equals(goodsInfo.getGoodsCode()) || "0".equals(goodsInfo.getGoodsCode())) {
                continue;
            }
            if ("0".equals(goodsInfo.getKuCun()) && realmGoodsInfoMap.get(goodsInfo.getRoad_no()) != null && (int) Double.parseDouble(realmGoodsInfoMap.get(goodsInfo.getRoad_no()).getPrice()) != (int) Double.parseDouble(goodsInfo.getPrice())) {
                //售空状态且价格修改了
                hasEmpty = true;
                temp = true;
                roadNoList.add(DataUtil.setUpdateModel(SysConfig.UPDATE_STOCK, boxIndex, goodsInfo));
                roadNoList.add(DataUtil.setUpdateModel(SysConfig.UPDATE_PRICE, boxIndex, goodsInfo));
            } else if ("0".equals(goodsInfo.getKuCun())) {
                //售空
                hasEmpty = true;
                roadNoList.add(DataUtil.setUpdateModel(1, boxIndex, goodsInfo));
            } else if (realmGoodsInfoMap.get(goodsInfo.getRoad_no()) != null && (int) Double.parseDouble(realmGoodsInfoMap.get(goodsInfo.getRoad_no()).getPrice()) != (int) Double.parseDouble(goodsInfo.getPrice())) {
                //修改价格
                temp = true;
                hasEmpty = true;
                roadNoList.add(DataUtil.setUpdateModel(SysConfig.UPDATE_PRICE, boxIndex, goodsInfo));
            } else if (realmGoodsInfoMap.get(goodsInfo.getRoad_no()) == null) { //该货道之前没有设置过商品
                hasEmpty = true;
                temp = true;
                roadNoList.add(DataUtil.setUpdateModel(SysConfig.UPDATE_STOCK, boxIndex, goodsInfo));
                roadNoList.add(DataUtil.setUpdateModel(SysConfig.UPDATE_PRICE, boxIndex, goodsInfo));
            }
        }
        if (!temp && roadNoList.size() < goodsInfoList.size()) {
            hasEmpty = false;
        }
        dialogPingtai = LoadingUtil.createLoadingDialog(this, Constant.DEALING, 1, R.drawable.ic_ios_juhua, true);
        dialogPingtai.show();
        // 有空的 只能循环补货了
        if (hasEmpty) {
            L.e(SysConfig.ZPush, "逐条补货");
            // 逐条进行补货
            new BuhuoForAKMTask(this, boxIndex, roadNoList, machineBuHuoRstListener).execute();
        } else {
            L.e(SysConfig.ZPush, "一键补货 " + boxIndex);
            // 没有空的 一键补货
            String s = machineBuHuo(boxIndex, 0);
            if ("".equals(s)) {
                dialogPingtai.dismiss();
                getRoadEmptyInfo();
                confirmForGeziMachine(goodsInfoList, machineSn, "2");
            } else {
                dialogPingtai.dismiss();
                ToastUtils.showToast(this, Constant.MAIN_MACHINA_BUHUO_FAIL);
            }
        }

    }

    /**
     * 格子柜提交补货，只是更新本地数据库
     */
    private void confirmForGeziMachine(List<GoodsInfo> goodsInfoList, String machineSn, String goodsBelong) {
        //写入操作日志
        if (goodsInfoList == null || goodsInfoList.size() == 0) {
            ToastUtils.showToast(this, Constant.GOODS_DATA_ERROR);
            return;
        }
        this.goodsInfoList = goodsInfoList;
        dialogPingtai = LoadingUtil.createLoadingDialog(this, Constant.DEALING, 1, R.drawable.ic_ios_juhua, true);
        dialogPingtai.show();
        // 设置本地数据
        updateLocalData(goodsBelong, machineSn);
    }

    /**
     * 主柜提交补货
     */
    public void confirmForMainMachine(final List<GoodsInfo> goodsInfoList) {
        //写入操作日志
        if (goodsInfoList == null || goodsInfoList.size() == 0) {
            ToastUtils.showToast(this, Constant.GOODS_DATA_ERROR);
            return;
        }

        this.goodsInfoList = goodsInfoList;
        int totalCount = 0;

        // 计算货道占比
        int ziyingCount = 0;
        for (GoodsInfo goodsInfo : goodsInfoList) {
            if (goodsInfo != null && goodsInfo.getGoodsID() != null && !"".equals(goodsInfo.getGoodsID())) {
                totalCount++;
                if ("0".equals(goodsInfo.getAscription())) {
                    ziyingCount++;
                }
            }
        }

        if (MyApplication.getInstance().getRoadRatio() == null || "".equals(MyApplication.getInstance().getRoadRatio())) {
            ToastUtils.showToast(BuhuoActivity.this, Constant.GOODS_RATE_ERROR);
            return;
        }
        double mustCount = Double.parseDouble(MyApplication.getInstance().getRoadRatio()) * totalCount;
        mustCount = new BigDecimal(String.valueOf(mustCount)).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (ziyingCount < (int) mustCount) {
            alertView = new AlertView("提示", "自营商品不能低于" + (int) mustCount + "件", null,
                    new String[]{"确认"}, null, this, AlertView.Style.Alert, DataUtil.dip2px(BuhuoActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
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
            return;
        }
        // 请求机器更改主柜货道信息
        if (!isMachineConnected) {
            // 机器未连接
            ToastUtils.showToast(this, Constant.MAIN_MACHINA_DONOT_CON);
            return;
        }
        dialogPingtai = LoadingUtil.createLoadingDialog(BuhuoActivity.this, Constant.DEALING, 1, R.drawable.ic_ios_juhua, true);
        dialogPingtai.show();

        // 组装数据, 请求机器交互
        // 生成货道命令
        // 测试设置商品编码

        //补货前所有主机商品
        RealmResults<GoodsInfo> goodsInfos = realm.where(GoodsInfo.class).equalTo("goodsBelong", "1").findAll();
        //补货前货道和商品的映射
        Map<Integer, GoodsInfo> mainMachineGoodInfo = new HashMap<>();
        for (GoodsInfo goodsInfo : goodsInfos) {
            mainMachineGoodInfo.put(goodsInfo.getRoad_no(), goodsInfo);
        }
        List<UpateModel> raodInfo = new ArrayList<>();
        for (GoodsInfo goodsInfo : goodsInfoList) {
            //取出所有属于主机的商品
            if ("1".equals(goodsInfo.getGoodsBelong())) { // 1:主机 2:格子柜
                if ("".equals(goodsInfo.getGoodsID()) || goodsInfo.getGoodsID() == null) {
                    raodInfo.add(DataUtil.setUpdateModel(SysConfig.UPDATE_PRICE, 0, null));
                } else if (mainMachineGoodInfo.get(goodsInfo.getRoad_no()) != null && !mainMachineGoodInfo.get(goodsInfo.getRoad_no()).getPrice().equals(goodsInfo.getPrice())) {
                    raodInfo.add(DataUtil.setUpdateModel(SysConfig.UPDATE_PRICE, 0, goodsInfo));
                }
            }
        }
        new MachineOrderTask(BuhuoActivity.this, goodsInfoList.size(), machineOrderRstListener, raodInfo).execute();
    }

    /**
     * 副柜补货
     * @param goodsInfoList 补货后各货道对应的商品列表
     * @param machineSn 机器编码
     * @param boxIndex 箱号
     */
    public void confirmForDeskMachine(final List<GoodsInfo> goodsInfoList, final String machineSn, final int boxIndex) {
        if (goodsInfoList == null || goodsInfoList.size() == 0) {
            ToastUtils.showToast(this, Constant.GEZHI_DATA_ERROR);
            return;
        }
        //补货前货道和商品的映射
        Map<Integer, GoodsInfo> reamlGoodsInfoMap = new HashMap<>();
        //取出当前数据库中所有副柜的商品
        RealmResults<GoodsInfo> results = realm.where(GoodsInfo.class).equalTo("goodsBelong", "3")
                .equalTo("machineID", machineSn).findAll();
        for (GoodsInfo goodsInfo : results) {
            reamlGoodsInfoMap.put(goodsInfo.getRoad_no(), goodsInfo);
        }

        this.goodsInfoList = goodsInfoList;
        this.machineSn = machineSn;
        goodsBelong = "3";

        boolean hasEmpty = false;
        boolean priceChange = false;

        List<UpateModel> roadNumList = new ArrayList<>();
        //遍历补货后的所有商品
        for (GoodsInfo goodsInfo : goodsInfoList) {
            if (goodsInfo == null || goodsInfo.getGoodsCode() == null || "".equals(goodsInfo.getGoodsCode()) || "0".equals(goodsInfo.getGoodsCode())) {
                continue;
            }
            //TODO:这里可能有问题，kucun不可能为空，我改为了0
            if ("0".equals(goodsInfo.getKuCun()) && reamlGoodsInfoMap.get(goodsInfo.getRoad_no()) != null
                    && (int) Double.parseDouble(reamlGoodsInfoMap.get(goodsInfo.getRoad_no()).getPrice()) != (int) Double.parseDouble(goodsInfo.getPrice())) {
                //售空状态且价格被修改
                hasEmpty = true;
                priceChange = true;
                //修改库存
                roadNumList.add(DataUtil.setUpdateModel(SysConfig.UPDATE_STOCK, boxIndex, goodsInfo));
                //修改价格
                roadNumList.add(DataUtil.setUpdateModel(SysConfig.UPDATE_PRICE, boxIndex, goodsInfo));
            } else if ("0".equals(goodsInfo.getKuCun())) {
                //表示售空
                hasEmpty = true;
                //修改库存
                roadNumList.add(DataUtil.setUpdateModel(SysConfig.UPDATE_STOCK, boxIndex, goodsInfo));
            } else if (reamlGoodsInfoMap.get(goodsInfo.getRoad_no()) != null
                    && (int) Double.parseDouble(reamlGoodsInfoMap.get(goodsInfo.getRoad_no()).getPrice()) != (int) Double.parseDouble(goodsInfo.getPrice())) {
                //修改价格
                hasEmpty = true;
                priceChange = true;
                roadNumList.add(DataUtil.setUpdateModel(SysConfig.UPDATE_PRICE, boxIndex, goodsInfo));
            } else if (reamlGoodsInfoMap.get(goodsInfo.getRoad_no()) == null) {
                hasEmpty = true;
                priceChange = true;
                roadNumList.add(DataUtil.setUpdateModel(SysConfig.UPDATE_STOCK, boxIndex, goodsInfo));
                roadNumList.add(DataUtil.setUpdateModel(SysConfig.UPDATE_PRICE, boxIndex, goodsInfo));
            }
        }

        if (!priceChange && roadNumList.size() < goodsInfoList.size()) {
            hasEmpty = false;
        }

        if (!isMachineConnected) {
            ToastUtils.showToast(this, Constant.MAIN_MACHINA_DONOT_CON);
            return;
        }

        dialogPingtai = LoadingUtil.createLoadingDialog(this, Constant.DEALING, 1, R.drawable.ic_ios_juhua, true);
        dialogPingtai.show();
        //TODO:副柜的补货应该有问题，这里向副柜发送的补货后的各货道的商品数有问题，
        // 有空的 只能循环补货了
        if (hasEmpty) {
//            L.e(SysConfig.ZPush, "逐条补货");
            // 逐条进行补货
            new BuhuoForAKMTask(this, boxIndex, roadNumList, machineBuHuoRstListener).execute();
        } else {
//            L.e(SysConfig.ZPush, "一键补货 " + boxIndex);
            // 没有空的 一键补货,即各料道全部补满
            String s = machineBuHuo(boxIndex, 0);
            if ("".equals(s)) {
                dialogPingtai.dismiss();
                getRoadEmptyInfo();
                confirmForGeziMachine(goodsInfoList, machineSn, "3");
            } else {
                dialogPingtai.dismiss();
                ToastUtils.showToast(this, Constant.MAIN_MACHINA_BUHUO_FAIL);
            }
        }
    }

    /**
     * 机器指令处理结果回调
     */
    private MachineOrderRstListener machineOrderRstListener = new MachineOrderRstListener() {
        @Override
        public void success() {
            // 设置本地数据
            updateLocalData("1", MyApplication.getInstance().getMachine_sn());
        }

        @Override
        public void fail() {
            dialogPingtai.dismiss();
            ToastUtils.showToast(BuhuoActivity.this, Constant.MAIN_MACHINA_COMM_FAIL);
        }
    };

    //更新本地数据库商品信息
    private void updateLocalData(final String goodsBelong, String machineSn) {
        // 删除本地数据库的商品信息
        //更新成功后删除本地存储的商品信息

        if ("1".equals(goodsBelong)) {
            MarkLog.markLog("饮料机" + machineSn + "补货完成", SysConfig.LOG_LEVEL_IMPORTANT, machineSn);
            MyApplication.getInstance().getLogBuHuo().d("主机补货，更新本地商品数据");
        } else if ("2".equals(goodsBelong)) {
            MarkLog.markLog("格子柜" + machineSn + "补货完成", SysConfig.LOG_LEVEL_IMPORTANT, machineSn);
            MyApplication.getInstance().getLogBuHuo().d("格子柜补货，更新本地商品数据");
        } else {
            MarkLog.markLog("副柜" + machineSn + "补货完成", SysConfig.LOG_LEVEL_IMPORTANT, machineSn);
            MyApplication.getInstance().getLogBuHuo().d("副柜补货，更新本地商品数据");
        }

        final RealmResults<GoodsInfo> goodsInfos = realm.where(GoodsInfo.class).equalTo("goodsBelong", goodsBelong)
                .equalTo("machineID", machineSn).findAll();
        // 删除机器的缺货警告信息
        final RealmResults<QueHuoRecord> queHuoRecords = realm.where(QueHuoRecord.class).equalTo("machineSn", machineSn)
                .equalTo("isUploaded", "0").findAll();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                goodsInfos.deleteAllFromRealm();
                for (GoodsInfo goodsInfo : goodsInfoList) {
                    if (goodsInfo.getGoodsID() != null && !"".equals(goodsInfo.getGoodsID())) {
                        goodsInfo.setGoodsBelong(goodsBelong);
                        goodsInfo.setGoodsCode(goodsInfo.getGoodsID());
                        goodsInfo.setOnlineKuCun(Integer.parseInt(goodsInfo.getKuCun()));
                        realm.copyToRealm(goodsInfo);
                    }
                }
                for (QueHuoRecord queHuoRecord : queHuoRecords) {
                    queHuoRecord.setIsUploaded("1");
                }
            }
        });
        // 提交服务器
//        machine_sn, goods_infos
//        goods_infos: 商品信息 (格式：货道号1,商品编号1,商品价格1,最大库存1,修改库存数1,本地库存1;货道号2,商品编号2,商品价格2,最大库存2,修改库存数2,本地库存2;)

        StringBuffer goods_infos = new StringBuffer();
        for (GoodsInfo goodsInfo : goodsInfoList) {
            String price = goodsInfo.getPrice();
            if (price == null || "".equals(price)) {
                price = "0.0";
            }
            goods_infos.append(goodsInfo.getRoad_no() + "," +
                    (goodsInfo.getGoodsID() == null ? "" : goodsInfo.getGoodsID()) + "," +
                    Double.parseDouble(price) * 0.1 + "," +
                    goodsInfo.getMaxKucun() + "," +
                    (Integer.parseInt(goodsInfo.getKuCun()) - goodsInfo.getLocalKuCunForCheck()) + "," +
                    (goodsInfo.getKuCun() == null ? "" : goodsInfo.getKuCun()) + ";");
        }
        // 补货提交
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/post/" + machineSn + "/replenishmentinfo?goodsInfos=" + goods_infos.toString();
        goodsInfoJsonSave = "api/machine/post/" + machineSn + "/replenishmentinfo?goodsInfos=" + goods_infos.toString();
        request = NoHttp.createStringRequest(url, RequestMethod.GET);
        //设置网络请求失败之后读取缓存,会设置定时重启,没意义,所以直接读取
        request.setCacheMode(CacheMode.ONLY_REQUEST_NETWORK);
        if (request != null) {
            request.setTag(goodsBelong);
            // 添加到请求队列
            CallServer.getRequestInstance().add(this, 1, request, httpListener, true, false);
            MyApplication.getInstance().getLogBuHuo().d("上传补货记录");
        } else {
            dialogPingtai.dismiss();
            ToastUtils.showToast(BuhuoActivity.this, Constant.COMMIT_FAIL_NET);
        }
        // 重新加载库存
        getRoadEmptyInfo();
    }

    /**
     * 开指定格子柜门澳柯玛
     */
    public void openGeziDoorForAokema(final int boxIndex, final int roadNo) {
        MyApplication.getInstance().getLogBuHuo().d("打开格子柜门 = 箱号 : "+boxIndex+" ; 货道号 : "+roadNo+" ; 连接失败的箱号列表 : "+MyApplication.getInstance().getConnetFailGeziList());
        if (SysConfig.MAIN_DOOR_IS_CLOSE.equals(doorStatus)) {
            MyApplication.getInstance().getLogBuHuo().d("请先开柜门");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 提示先开门
                    alertView = new AlertView("提示", "请先打开主柜门。", null, new String[]{"确认"}, null, BuhuoActivity.this, AlertView.Style.Alert, DataUtil.dip2px(BuhuoActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            if (-1 == position) {
                                alertView.dismiss();
                            } else {
                                alertView.dismiss();
                            }
                        }
                    }).setCancelable((true)).setOnDismissListener(null);
                    alertView.show();
                }
            }, 200);
            return;
        } else if (MyApplication.getInstance().getConnetFailGeziList().contains(boxIndex)) {
            MyApplication.getInstance().getLogBuHuo().d("格子柜箱号未连接");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 提示格子柜未连接
                    alertView = new AlertView("提示", "格子柜" + mNotConnectTitle + "未连接。", null, new String[]{"确认"}, null, BuhuoActivity.this, AlertView.Style.Alert, DataUtil.dip2px(BuhuoActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            if (-1 == position) {
                                alertView.dismiss();
                            } else {
                                alertView.dismiss();
                            }
                        }
                    }).setCancelable((true)).setOnDismissListener(null);
                    alertView.show();
                }
            }, 200);
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 判断货道之前是否有货 是否有货 0:有 1:无
                MyApplication.getInstance().getAokemaGeZiKuCunMap().get(boxIndex).get(roadNo);
                openGeziDoorForAKM(boxIndex, roadNo);
            }
        }).run();
    }

    /**
     * 开所有格子柜门 澳柯玛
     */
    public void openAllGeziDoorForAokema(final int boxIndex, final List<Integer> roadNoList) {
        if ("0".equals(doorStatus)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 提示先开门
                    alertView = new AlertView("提示", "请先打开主柜门。", null, new String[]{"确认"}, null, BuhuoActivity.this, AlertView.Style.Alert, DataUtil.dip2px(BuhuoActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            if (-1 == position) {
                                alertView.dismiss();
                            } else {
                                alertView.dismiss();
                            }
                        }
                    }).setCancelable((true)).setOnDismissListener(null);
                    alertView.show();
                }
            }, 200);
            return;
        } else if (MyApplication.getInstance().getConnetFailGeziList().contains(boxIndex)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 提示格子柜未连接
                    alertView = new AlertView("提示", "格子柜" + mNotConnectTitle + "未连接。", null, new String[]{"确认"}, null, BuhuoActivity.this, AlertView.Style.Alert, DataUtil.dip2px(BuhuoActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            if (-1 == position) {
                                alertView.dismiss();
                            } else {
                                alertView.dismiss();
                            }
                        }
                    }).setCancelable((true)).setOnDismissListener(null);
                    alertView.show();
                }
            }, 200);
            return;
        }

        dialogPingtai = LoadingUtil.createLoadingDialog(BuhuoActivity.this, "开门中...", 1, R.drawable.ic_ios_juhua, true);
        dialogPingtai.show();
        new OpenCabinetDoorForAKMTask(this, boxIndex, roadNoList, openDoorRstListener).execute();
    }

    /**
     * 机器指令处理结果回调
     */
    private MachineOrderRstListener openDoorRstListener = new MachineOrderRstListener() {
        @Override
        public void success() {
            try {
                if (dialogPingtai != null) {
                    dialogPingtai.dismiss();
                }
            } catch (Exception e) {
            }
        }

        @Override
        public void fail() {
            try {
                if (dialogPingtai != null)
                    dialogPingtai.dismiss();
                ToastUtils.showToast(BuhuoActivity.this, Constant.GEZIGUI_COMM_FAIL);
            } catch (Exception e) {
            }
        }
    };

    /**
     * 补货指令处理结果回调
     */
    private MachineOrderRstListener machineBuHuoRstListener = new MachineOrderRstListener() {
        @Override
        public void success() {
            if (dialogPingtai != null)
                dialogPingtai.dismiss();
            getRoadEmptyInfo();
            confirmForGeziMachine(goodsInfoList, machineSn, goodsBelong);
        }

        @Override
        public void fail() {
            if (dialogPingtai != null)
                dialogPingtai.dismiss();
            ToastUtils.showToast(BuhuoActivity.this, Constant.MAIN_MACHINA_BUHUO_FAIL);
        }
    };

    /**
     * 主柜出货成功更改本地库存
     * TODO:BuhuoActivity中这里不应该有这两个出货后更新库存的方法
     */
    @Override
    public void updateLocalKuCun(String road_no) {
//        Log.e(TAG, "更新库存:" + road_no);
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
//        Log.e(TAG, "更新库存:" + road_no);
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
    public void onBackPressed() {
//        Log.i(TAG, "onBackPressed()");
        //不允许右键退出,必须使用按钮退出
        //super.onPause();
    }

    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof SerialObservable) {
            super.update(observable, o);
        } else if (observable instanceof MyObservable) {
            finish();
        }
    }

    /**
     * 网络处理
     */
    private HttpListener<String> httpListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) throws JSONException {
            int responseCode = response.getHeaders().getResponseCode();// 服务器响应码
            if (responseCode == 200) {
                if (RequestMethod.HEAD != response.getRequestMethod()) {
                    // 请求方法为HEAD时没有响应内容
                    if (dialogPingtai != null) {
                        dialogPingtai.dismiss();
                    }
                    JSONObject jsonResult = null;
                    try {
                        jsonResult = new JSONObject(response.get());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    switch (what) {
                        case 1:
                            if (jsonResult != null && jsonResult.getString(SysConfig.JSON_KEY_ERROR_CODE).equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                //对于主柜的处理
                                if ("1".equals(request.getTag().toString())) {
                                    ((DrinkFragment) mFragments[0]).isChanged = false;
                                    ((DrinkFragment) mFragments[0]).resetData();
                                }
                                //对于副柜
                                if ("2".equals(request.getTag().toString())) {
                                    ((DeskFragment) mFragments[1]).isChanged = false;
                                    ((DeskFragment) mFragments[1]).resetData();
                                }

                                if ("3".equals(request.getTag().toString())) {
                                    ((GeZiFragment) mFragments[2]).isChanged = false;
                                    ((GeZiFragment) mFragments[2]).resetData();
                                }
                                // 处理成功
                                loadingDialogEnd = LoadingUtil.createLoadingDialog(BuhuoActivity.this, "处理成功", 1, R.drawable.ic_success, false);
                                loadingDialogEnd.show();
                                MyApplication.getInstance().getLogBuHuo().d("补货记录上传成功");
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        loadingDialogEnd.dismiss();
                                    }
                                }, 1000);
                            } else {
                                String res = "";
                                if (jsonResult != null) {
                                    res = jsonResult.getString(SysConfig.JSON_KEY_ERROR);
                                } else {
                                    res = Constant.NO_ERROR_MSG;
                                }
                                alertView = new AlertView("提示", res, null,
                                        new String[]{"确认"}, null, BuhuoActivity.this, AlertView.Style.Alert, DataUtil.dip2px(BuhuoActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(Object o, int position) {
                                        if (-1 == position) {
                                            alertView.dismiss();
                                        } else {
                                            alertView.dismiss();
                                        }
                                    }
                                });
                                alertView.show();
                            }

                            break;
                    }
                }
            }
        }

        @Override
        public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {
            MyApplication.getInstance().getLogBuHuo().d("补货记录上传失败");
            if (dialogPingtai != null) {
                dialogPingtai.dismiss();
            }
            if (networkMillis > 5000) {
                dialogPingtai.dismiss();
                ToastUtils.showToast(BuhuoActivity.this, Constant.NETWORK_ERROR1);
            }
            if (what == 1) {
                alertView = new AlertView("提示", Constant.NETWORK_ERROR2, null,
                        new String[]{"确认"}, null, BuhuoActivity.this, AlertView.Style.Alert, DataUtil.dip2px(BuhuoActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
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
            DataUtil.addBackGroundRequest(goodsInfoJsonSave, "", Constant.BACKGROUND_WHAT_0, false);
        }
    };
}
