package com.zongsheng.drink.h17;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.dwin.navy.serialportapi.ComAokema;
import com.zongsheng.drink.h17.background.bean.BaseInfo;
import com.zongsheng.drink.h17.background.bean.BindGeZi;
import com.zongsheng.drink.h17.base.BasePresenter;
import com.zongsheng.drink.h17.common.Constant;
import com.zongsheng.drink.h17.common.L;
import com.zongsheng.drink.h17.common.MyCountDownTimer;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.common.ToastUtils;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.front.bean.MachineFaultRecord;
import com.zongsheng.drink.h17.front.bean.PayModel;
import com.zongsheng.drink.h17.front.bean.QueHuoRecord;
import com.zongsheng.drink.h17.front.bean.ShipmentModel;
import com.zongsheng.drink.h17.interfaces.IVSICallback2View;
import com.zongsheng.drink.h17.loading.LoadingActivity;
import com.zongsheng.drink.h17.observable.SerialObservable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.zongsheng.drink.h17.common.SysConfig.TIME_FORMAT_S;

/**
 * 连接程序
 * Created by dxf on 2016/8/22.
 */
public abstract class ComActivity<V, T extends BasePresenter<V>> extends FragmentActivity implements Observer {

    private static final int XIAN_SHI = 0x100;
    // 机器链接程序
    private ComAokema comVSI;
    // 主机链接状态
    public boolean machineConncted = false;
    private String TAG = "ComActivity";
    private Reference<IVSICallback2View> ivsiCallback2ViewWeakReference = null;
    public boolean isNeedInitMachineInfo = false;
    MachineInfoGetTimer machineInfoGetTimer;
    public Realm realm;
    protected T presenter;
    /**
     * 查询类型 1:现金出货前查询 2:非现金出货前查询
     */
    private String machineQueryType = "";
    /**
     * 出货查询计时器timer
     */
    BeforeSellSearchTimer beforeSellSearchTimer;
    /**
     * 销售订单号
     */
    private String saleOrderID = "";

    private GoodsInfo saleGoodsInfo;

    private long startTime = new Date().getTime();

    /**
     * 最后出货时间
     */
    public long lastSaleTime = 0;

    /**
     * 是否在后端维护
     */
    public boolean isInBackPage = false;

    /**
     * 主柜状态 0:关闭 1:开启
     */
    public String doorStatus = "0";

    /**
     * 获取到售空状态的时间
     */
    private long emptyIntoGetTime = 0;
    private String lastRoadInfo = "";

    int nowBoxIndex = 0;
    long geziEmptyGetTime = 0;

    ////////////////澳柯玛格子柜添加变量///////////////////


    ////////////////澳柯玛格子柜添加变量///////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        // 启动和主控VSI的通讯
        Log.e(TAG, "初始化COMVSI");
        comVSI = ComAokema.getInstance2();
        boolean isInit = false;
        if (comVSI == null) {
            isInit = true;
            comVSI = ComAokema.getInstance();
        }
        comVSI.openSerialPort();
        presenter = createPresenter();
        if (presenter != null) {
            presenter.attachView((V) this);
        }
        SerialObservable.getInstance().regist(this);
        if (!isInit || !MyApplication.getInstance().isWeihu()) {
            machineInfoGetTimer = new MachineInfoGetTimer(3000, 3000);
            machineInfoGetTimer.start();
        }
    }

    ///////////////////////////////////接收继承类调用//////////////////////////////////////////

    protected abstract T createPresenter();

    /**
     * 查询投币数
     */
    public void request_get_status_intrymoney() {
//        comVSI.request_get_status_intrymoney();
    }

    /**
     * 设置串口连接的货道数
     */
    public void setRoadCount(int count) {
        if (comVSI != null) {
            comVSI.setRoadCount(count);
        }
    }

    /**
     * 现金出货
     */
    public void sellByCash(int goodsCode) {
        Log.i(TAG, "现金出货前查询");
        // 现金出货前查询
        machineQueryType = "1";// 现金出货
        // 根据goodsCode查找出货货道商品
        RealmResults<GoodsInfo> results = realm.where(GoodsInfo.class).equalTo("goodsBelong", "1").
                equalTo("goodsCode", String.valueOf(goodsCode)).findAll();
        results = results.sort("road_no", Sort.ASCENDING);
        saleGoodsInfo = null;
        if (results.size() > 0) {
            String[] sellEmptyArr = MyApplication.getInstance().getSellEmptyInfo().split(",");
            // 有货的商品code
            Map<Integer, Integer> goodsCodeRoadNoMap = new HashMap<Integer, Integer>();
            for (int i = 0; i < sellEmptyArr.length; i++) {
                if ("0".equals(sellEmptyArr[i])) { // 有货
                    goodsCodeRoadNoMap.put((i + 1), 0);
                }
            }
            Log.i(TAG, "取得本地保存产品数据:" + results.size());
            for (GoodsInfo goodsInfo : results) {
                if (goodsCodeRoadNoMap.containsKey(goodsInfo.getRoad_no())) {
                    saleGoodsInfo = realm.copyFromRealm(goodsInfo);
                    break;
                }
            }
        }
        if (saleGoodsInfo == null) {
            ToastUtils.showToast(this, "所选商品已售空");
            return;
        }
        // 判断两次操作时间间隔
        long time = new Date().getTime();
        if (lastSaleTime != 0 && time - lastSaleTime < 1000) {
            SystemClock.sleep(1000 - (time - lastSaleTime));
        }
        lastSaleTime = time;
        // 安卓工控机发起扣款请求 dealSerialNumber:交易序列号,channelNum:料道值 ,PAY_WAY支付方式
        final int dealSerialNumber = (int) new Date().getTime();
        String s = comVSI.toPay(dealSerialNumber, (byte) saleGoodsInfo.getRoad_no(), (byte) 1, ((int) Double.parseDouble(saleGoodsInfo.getPrice())) * 10, 0);
        Log.e(TAG, "现金出货结果:" + s);
        if (!"".equals(s)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String s = comVSI.toPay(dealSerialNumber, (byte) saleGoodsInfo.getRoad_no(), (byte) 1, ((int) Double.parseDouble(saleGoodsInfo.getPrice())) * 10, 0);
                    Log.e(TAG, "现金出货结果:" + s);
                }
            }, 500);
        }
    }

    /**
     * 非现金出货（饮料机）
     */
    public void sellByNoCash(int goodsCode, String orderID, String saleOrderPrice) {
        Log.i(TAG, "非现金出货前查询");
        saleOrderID = orderID;
        MyApplication.getInstance().setNoCashorderSn(orderID);
        machineQueryType = "2";// 非现金出货
        // 根据goodsCode查找出货货道商品
        RealmResults<GoodsInfo> results = realm.where(GoodsInfo.class).equalTo("goodsBelong", "1").
                equalTo("goodsCode", String.valueOf(goodsCode)).findAll();
        results = results.sort("road_no", Sort.ASCENDING);
        saleGoodsInfo = null;
        if (results.size() > 0) {
            String[] sellEmptyArr = MyApplication.getInstance().getSellEmptyInfo().split(",");
            // 有货的商品code
            Map<Integer, Integer> goodsCodeRoadNoMap = new HashMap<Integer, Integer>();
            for (int i = 0; i < sellEmptyArr.length; i++) {
                if ("0".equals(sellEmptyArr[i])) { // 有货
                    goodsCodeRoadNoMap.put((i + 1), 0);
                }
            }
            Log.i(TAG, "取得本地保存产品数据:" + results.size());
            for (GoodsInfo goodsInfo : results) {
                if (goodsCodeRoadNoMap.containsKey(goodsInfo.getRoad_no())) {
                    saleGoodsInfo = realm.copyFromRealm(goodsInfo);
                    break;
                }
            }
        }
        if (saleGoodsInfo == null) {
            ToastUtils.showToast(this, "所选商品已售空");
            return;
        }
        Log.e(TAG, "推送后真实出货的货道数:" + saleGoodsInfo.getRoad_no() + " " + goodsCode);
        // 安卓工控机发起扣款请求 dealSerialNumber:交易序列号,channelNum:料道值 ,PAY_WAY支付方式
        final int dealSerialNumber = (int) new Date().getTime();
        String s = comVSI.toPayForNoCash(dealSerialNumber, (byte) saleGoodsInfo.getRoad_no(), (byte) 1, 0, 0);
        Log.e(TAG, "非现金出货结果:" + s);
        if (!"".equals(s)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String s = comVSI.toPayForNoCash(dealSerialNumber, (byte) saleGoodsInfo.getRoad_no(), (byte) 1, 0, 0);
                    Log.e(TAG, "非现金出货结果:" + s);
                }
            }, 500);
        }
    }


    /**
     * 设置价格
     *
     * @param boxNo  箱号
     * @param roadNo 料道号
     */
    public String setGeziChannelPrice(final int boxNo, final int roadNo, final int price) {

        String s = comVSI.setGeziChannelPrice(boxNo, roadNo, price);
        Log.e(TAG, "非现金出货结果:" + s);
        if (!"".equals(s)) {
            SystemClock.sleep(500);
            s = comVSI.setGeziChannelPrice(boxNo, roadNo, price);
            Log.e(TAG, "非现金出货结果:" + s);
        }
        return s;
    }

    public String requestConfigindCashPrice(final int price, final int boxindex, final int roadNo) {
        String res = "";
        res = comVSI.setGeziChannelPrice(boxindex, roadNo, price);
        if (!"".equals(res)) {
            SystemClock.sleep(500);
            res = comVSI.setGeziChannelPrice(boxindex, roadNo, price);
            if (!"".equals(res)) {
                return res;
            }
        }
        return res;
    }


    /**
     * 关闭串口通讯
     */
    public void closeComPort() {
        if (comVSI != null) {
            Log.e(TAG, "关闭串口连接了");
            comVSI.closeSerialPort();
        }
    }

    /**
     * 系统故障信息
     */
    public void getMachineFault() {
        String str = comVSI.checkSystemErrorInfo();
        if (!"".equals(str)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    comVSI.checkSystemErrorInfo();
                }
            }, 1000);
        }
    }

    public void getMachineFault2() {
        String str = comVSI.checkSystemErrorInfo2();
        if (!"".equals(str)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    comVSI.checkSystemErrorInfo2();
                }
            }, 1000);
        }
    }

    /**
     * 获取缺币信息
     */
    public void getQueBi() {
        // aokema donothing
    }

//    public void fuguiSaleTest(int roadNo) {
//        final int dealSerialNumber = (int) new Date().getTime();
//        String ss = comVSI.toPayForNoCash(dealSerialNumber, (byte) roadNo, (byte) 1, 0, 1);
//    }

    /**
     * 主机开始销售
     */
    public String machineStartSale() {
        String str = comVSI.sale();
        if (!"".equals(str)) {
            SystemClock.sleep(500);
            str = comVSI.sale();
            if (!"".equals(str)) {
                return "失败";
            } else {
                return str;
            }
        }
        return str;
    }

    /**
     * 获取货道售空状态，通过指令从工控机获得
     */
    public void getRoadEmptyInfo() {
        comVSI.checkThingsHaveOrNot(0);
        comVSI.sendEmpty();
    }

    /**
     * 副柜现金出货测试
     *
     * @param roadNo
     */
    public void fuguiSaleTestByCash(final int roadNo, final int money) {
        final int dealSerialNumber = (int) new Date().getTime();
        String s = comVSI.toPay(dealSerialNumber, (byte) roadNo, (byte) 1, money * 10, 1);
        if (!"".equals(s)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String s = comVSI.toPay(dealSerialNumber, (byte) roadNo, (byte) 1, money * 10, 1);
                }
            }, 500);
        }
    }

    /**
     * 副柜非现金出货测试
     */
    public void fuguiSaleTestByNoCash(final int goodsCode, String orderID, final int road_no) {
        final int dealSerialNumber = (int) new Date().getTime();
        saleOrderID = orderID;
        MyApplication.getInstance().setNoCashorderSn(orderID);

        GoodsInfo result = realm.where(GoodsInfo.class).equalTo("goodsBelong", "3")
                .equalTo("road_no", road_no).findFirst();

        machineQueryType = "2";// 非现金出货
        String s = comVSI.toPayForNoCash(dealSerialNumber, (byte) road_no, (byte) 1, 0, 1);
        Log.e("here", "非现金出货结果:" + s);
        if (!"".equals(s)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String s = comVSI.toPayForNoCash(dealSerialNumber, (byte) road_no, (byte) 1, 0, 1);
                    Log.e(TAG, "非现金出货结果:" + s);
                }
            }, 500);
        }
    }

    /**
     * 澳柯玛格子柜出货
     */
    public String gezi_sale_aokemaByCash(final int boxIndex, final int road_no, final int money) {
        final int dealSerialNumber = (int) new Date().getTime();
//        安卓工控机发起扣款请求 dealSerialNumber:交易序列号,channelNum:料道值 ,PAY_WAY支付方式
//                * 1-钱币 2-刷卡 3-支付宝 4-微信
        String s = comVSI.toPay(dealSerialNumber, (byte) road_no, (byte) 1, money * 10, boxIndex);
        Log.e(TAG, "澳柯玛格子柜出货结果:" + s + " " + money);
        if (!"".equals(s)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String s = comVSI.toPay(dealSerialNumber, (byte) road_no, (byte) 1, money * 10, boxIndex);
                    Log.e(TAG, "澳柯玛格子柜出货结果1:" + s);
                }
            }, 500);
        }
        return "";
    }

    /**
     * 澳柯玛格子柜非现金出货
     */
    public String gezi_sale_aokemaByNoCash(final int boxIndex, final int road_no, String orderID, String goods_price) {
        final int dealSerialNumber = (int) new Date().getTime();
        saleOrderID = orderID;
        MyApplication.getInstance().setNoCashorderSn(orderID);
        machineQueryType = "2";// 非现金出货
        String s = comVSI.toPayForNoCash(dealSerialNumber, (byte) road_no, (byte) 1, 0, boxIndex);
        Log.e(TAG, "非现金出货结果:" + s);
        if (!"".equals(s)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String s = comVSI.toPayForNoCash(dealSerialNumber, (byte) road_no, (byte) 1, 0, boxIndex);
                    Log.e(TAG, "非现金出货结果:" + s);
                }
            }, 500);
        }

        return "";
    }

    /**
     * 澳柯玛格子柜测试开门
     */
    public String openGeziDoorForAKM(final int boxIndex, final int road_no) {
        String s = comVSI.channelTest(boxIndex, (byte) road_no);
        if ("".equals(s) && MyApplication.getInstance().getGeziList() != null) {  //使用指令为了弥补连接多格子柜后，开门速度变而导致的一键开门效率变低的情况
            int size = MyApplication.getInstance().getGeziList().size();
            for (int i = 0; i <= size; i++) {
                comVSI.sendEmpty();
            }
        }
        return s;
    }

    /**
     * 给机器补货
     */
    public String machineBuHuo(final int boxIndex, final int road_no) {
        if (road_no == 0) {
            String s = comVSI.addKucun(boxIndex, (byte) road_no);
            if (!"".equals(s)) {
                SystemClock.sleep(500);
                s = comVSI.addKucun(boxIndex, (byte) road_no);
                if (!"".equals(s)) {
                    return s;
                }
            }
        } else {
            comVSI.sendEmpty();
            String s = comVSI.addKucunSingle(boxIndex, (byte) road_no);
            if ("".equals(s)) {
                comVSI.sendEmpty();
            }
            return s;
        }
        return "";
    }

    protected void getMachineStatus(IVSICallback2View ivsiCallback2View) {
        ivsiCallback2ViewWeakReference = new WeakReference<>(ivsiCallback2View);
        String str = comVSI.checkMachineStatus();
        if (!"".equals(str)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    comVSI.checkMachineStatus();
                }
            }, 500);
        }
    }

    private PayModel getBasePayModel(String recordInfo, GoodsInfo goodsInfo, String machinetradeNo, String machineroadNo, boolean isCash) {
        Date date = new Date();
        SimpleDateFormat formatMs = new SimpleDateFormat(SysConfig.TIME_FORMAT_S_SSS);
        String timeMs = formatMs.format(date);
        // 20170414152440043
        String time = timeMs.substring(0, 14);
        PayModel payModel = new PayModel();
        payModel.setIsUploaded("0");
        payModel.setPushMachineSn(goodsInfo.getMachineID());
        payModel.setPayType("0");
        payModel.setOrderStatus(3);
        payModel.setPayTime(time);
        payModel.setCreateTime(time);
        payModel.setDeliveryTime(time);
        payModel.setGoodsBelong(goodsInfo.getGoodsBelong());
        payModel.setGoodsCode(goodsInfo.getGoodsCode());
        payModel.setMachineRoadNo(machineroadNo);
        payModel.setMachineTradeNo(machinetradeNo);
        payModel.setMachineSn(MyApplication.getInstance().getMachine_sn());
        //现金支付&&饮料机商品->订单时间精确到毫秒
        if (isCash && "1".equals(goodsInfo.getGoodsBelong())) {
            payModel.setOrderSn(goodsInfo.getMachineID() + timeMs);
        } else {
            payModel.setOrderSn(goodsInfo.getMachineID() + time);
        }
        payModel.setGoodsNum("1");
        payModel.setGoodsPrice(String.valueOf(((int) Double.parseDouble(goodsInfo.getPrice()) * 0.1)));
        payModel.setGoodsId(goodsInfo.getGoodsID());
        payModel.setRecordInfo(recordInfo);
        payModel.setGoodsName(goodsInfo.getGoodsName());
        payModel.setDeliveryStatus("0");
        return payModel;
    }

    public PayModel getBasePayModel(ShipmentModel shipmentModel) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SysConfig.TIME_FORMAT_S);
        String time = simpleDateFormat.format(new Date());
        PayModel payModel = new PayModel();
        payModel.setIsUploaded("0");
        payModel.setPushMachineSn(shipmentModel.getMachine_sn());
        payModel.setPayType("0");
        payModel.setOrderStatus(3);
        payModel.setPayTime(time);
        payModel.setCreateTime(time);
        payModel.setDeliveryTime(time);
        payModel.setGoodsBelong(shipmentModel.getGoods_belong());
        payModel.setGoodsCode(shipmentModel.getGoods_id());
        payModel.setMachineRoadNo("0");
        payModel.setMachineTradeNo("0");
        payModel.setMachineSn(MyApplication.getInstance().getMachine_sn());
        payModel.setOrderSn(shipmentModel.getOrder_sn());
        payModel.setGoodsNum("1");
        payModel.setGoodsPrice(String.valueOf(((int) Double.parseDouble(shipmentModel.getGoods_price()) * 0.1)));//
        payModel.setGoodsId(shipmentModel.getGoods_id());
        payModel.setRecordInfo("");
        payModel.setGoodsName("");
        payModel.setDeliveryStatus(Constant.DELIVERYFAIL);
        return payModel;
    }


    private void cashPaySend2MQ(PayModel payModel, String payType, String DeliveryStatus) {
        final PayModel payModel1 = payModel.clone();
        payModel1.setDeliveryStatus(DeliveryStatus);
        payModel1.setPayType(switchPayType(payType));
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(payModel1);
            }
        });
    }

    //主控能记录的支付类型，01钱币，02银联IC（磁条），03闪付，04支付宝，05微信
    protected String switchPayType(String payType) {
        String res = "";
        switch (payType) {
            case "1":
                res = "0";
                break;
            case "2":
            case "3":
                res = "0"; //暂时记录成现金
                break;
            case "4":
                res = "2";
                break;
            case "5":
                res = "1";
                break;
        }
        return res;
    }

    private void shipmentFail(PayModel payModel, String payType, String DeliveryStatus) {
        final PayModel payModel1 = payModel.clone();
        payModel1.setDeliveryStatus(DeliveryStatus);
        payModel1.setPayType(switchPayType(payType));
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(payModel1);
            }
        });
    }

    // 取得机器的温度设置信息
    protected void getMachineTempInfo() {
    }

    /**
     * 设置机器的温度
     */
    public String setMachineTemp(String tempModel, String temp) {
        return "";
    }

    /**
     * 取消按钮选中
     */
    public void cancelSelect(int roadNo) {
    }
    /////////////////////////////////反馈继承类的方法/////////////////////////////////////

    /**
     * 在线支付成功并上传缓存队列
     */
    protected void onlinePaySend2MQ(PayModel payModel, String orderSn, String payType, String DeliveryStatus) {

    }


    /**
     * 机器信息初始化完成后操作
     */
    protected void AfterMachineInfoGetOver() {
        // give sub method to do
    }

    /**
     * 机器缺币请求
     */
    protected void machineQueB(String machineSn, String fiveStatus, String oneStatus) {
        // give sub method to do
    }

    /**
     * 更新页面商品库存信息
     */
    protected void updateGoodsInfo() {
        // give sub method to do
    }

    /**
     * 投币后显示硬币数
     */
    protected void showCashCount(int cashCount) {
        // give sub method to do
    }

    /**
     * 用户按钮选择商品
     */
    protected void selectGoodByButton(String goodsRoad, String goodsCode) {
        // give sub method to do
    }

    /**
     * 出货成功 (出货货道, 产品编号, 出货量)
     */
    protected void shipmentSuccess(String huodao, String goodsCode, String goodsCount, String machineQueryType,
                                   String saleOrderID, int boxIndex, String saleTime, String trade_no) {
        // give sub method to do
    }

    /**
     * 主柜出货成功更改本地库存
     */
    protected void updateLocalKuCun(String road_no) {
        // give sub method to do
    }

    /**
     * 出货成功之后更改副柜的库存信息
     *
     * @param road_no
     */
    protected void updateDeskKucun(String road_no) {
    }

    /**
     * 更新格子柜库存信息
     */
    protected void updateCabinetGoodsInfo() {
    }

    /////////////////////////其他方法////////////////////////////////

    /**
     * 获取机器数据
     */
    public void getMachineData() {
    }

    /**
     * 获取机器数据
     */
    public void getMachineDataForCom() {
        // 获取货道状态
        String str = comVSI.checkThingsHaveOrNot(0);
        if (!"".equals(str)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String str = comVSI.checkThingsHaveOrNot(0);
                    if (!"".equals(str) && comVSI.isConnecting) {
                        // 信息获取完成后处理
                        AfterMachineInfoGetOver();
                    }
                }
            }, 500);
        }
    }

    /**
     * 处理产品的缺货信息 是否售空 0:未空 1:售空
     */
    public void handleGoodsKuCun() {
        try {
            Realm realm = Realm.getDefaultInstance();
            if (MyApplication.getInstance().getSellEmptyInfo() == null || "".equals(MyApplication.getInstance().getSellEmptyInfo())) {
                return;
            }
            String[] sellEmptyArr = MyApplication.getInstance().getSellEmptyInfo().split(",");
            // 有货的货道
            Map<Integer, Integer> goodsCodeRoadNoMap = new HashMap<>();
            Map<Integer, Integer> emptyRoadNoMap = new HashMap<>();
            for (int i = 0; i < sellEmptyArr.length; i++) {
                if (SysConfig.NOTSOLDOUT_FLAG.equals(sellEmptyArr[i])) { // 有货
                    goodsCodeRoadNoMap.put((i + 1), 0);
                } else {
                    emptyRoadNoMap.put((i + 1), 0);
                }
            }

            // 取出有货的商品code集合
            Map<String, Integer> goodsCodeMap = new HashMap<>();
            for (Integer road_no : goodsCodeRoadNoMap.keySet()) {
                GoodsInfo good = realm.where(GoodsInfo.class).equalTo("goodsBelong", "1").equalTo("road_no", road_no).findFirst();
                if (good != null && good.getGoodsCode() != null && !"".equals(good.getGoodsCode())) {
                    goodsCodeMap.put(good.getGoodsCode(), road_no);
                }
            }
            if (!realm.isInTransaction()) {
                realm.beginTransaction();
            }
            for (GoodsInfo goodsInfo : MyApplication.getInstance().getGoodsInfos()) {
                //  0:未空 1:售空
                if (goodsCodeMap.containsKey(goodsInfo.getGoodsCode())) {
                    goodsInfo.setIsSoldOut(SysConfig.NOTSOLDOUT_FLAG);
                } else {
                    goodsInfo.setIsSoldOut(SysConfig.ISSOLDOUT_FLAG);
                }
            }
            // 将无货的货到库存设置为1
            for (Integer road_no : emptyRoadNoMap.keySet()) {
                GoodsInfo good = realm.where(GoodsInfo.class).equalTo("goodsBelong", "1").equalTo("road_no", road_no).findFirst();
                if (good != null && good.getGoodsCode() != null && !"".equals(good.getGoodsCode())) {
                    good.setKuCun("1");
                    good.setOnlineKuCun(1);
                }
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            realm.close();
        }
    }

    /**
     * 处理产品的缺货信息 是否售空 0:未空 1:售空
     */
    private void handleGeziKuCun() {
        try {
            // 格子柜的map 机器编码, 箱号
            Map<String, Integer> bindGeziMap = new HashMap<>();
            int i = 1;
            for (BindGeZi bindGeZi : MyApplication.getInstance().getBindGeZis()) {
                boolean isFail = false;
                for (int boxNum : MyApplication.getInstance().getConnetFailGeziList()) {
                    if (boxNum == i + 1) {
                        isFail = true;
                        break;
                    }
                }
                if (!isFail) {
                    bindGeziMap.put(bindGeZi.getMachineSn(), i + 1);
                }
                i++;
            }
            // 加载本地数据库中的商品信息
            RealmResults<GoodsInfo> results = realm.where(GoodsInfo.class).equalTo("goodsBelong", "2").findAll();
            results = results.sort("road_no", Sort.ASCENDING);
            List<GoodsInfo> cabinetGoods = results;
            Map<String, Integer> checkMap1 = new HashMap<>();
            // 缺货信息map Map<机器ID, 缺货信息;>
            Map<String, String> queHuoInfoMap = new HashMap<>();
            MyApplication.getInstance().getCabinetTotalGoods().clear();
            MyApplication.getInstance().getCabinetGoods().clear();
            if (cabinetGoods.size() > 0) {
                Log.i(TAG, "取得本地保存格子柜产品数据:" + cabinetGoods.size());
                if (!realm.isInTransaction()) {
                    realm.beginTransaction();
                }
                for (GoodsInfo goodsInfo : cabinetGoods) {
                    if (goodsInfo == null || goodsInfo.getGoodsID() == null || "".equals(goodsInfo.getGoodsID()) || "0".equals(goodsInfo.getGoodsID())) {
                        continue;
                    }
                    if (!bindGeziMap.containsKey(goodsInfo.getMachineID())) {
                        continue;
                    }
                    // 无此货道的时候 continue
                    if (MyApplication.getInstance().getGeziRoadListMap().containsKey(bindGeziMap.get(goodsInfo.getMachineID()))) {
                        if (!MyApplication.getInstance().getGeziRoadListMap().get(bindGeziMap.get(goodsInfo.getMachineID())).contains(goodsInfo.getRoad_no())) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    // format价格
                    goodsInfo.setPrice((int) Double.parseDouble(goodsInfo.getPrice()) + "");
                    int kucun = 0;
                    if (MyApplication.getInstance().getAokemaGeZiKuCunMap().containsKey(bindGeziMap.get(goodsInfo.getMachineID()))) {
                        // 这里直接用货道号是不对的。 getAokemaGeZiKuCunMap里面对应的是1~80  这里的road_no 是真实的
                        int road_no = goodsInfo.getRoad_no();
                        int index = 0;
                        if (road_no > 10) {
                            index = road_no - (((int) (road_no * 0.1)) * 2);
                        } else {
                            index = road_no;
                        }
                        if (MyApplication.getInstance().getAokemaGeZiKuCunMap().get(bindGeziMap.get(goodsInfo.getMachineID())).containsKey(index)) {
                            if ("0".equals(MyApplication.getInstance().getAokemaGeZiKuCunMap().get(bindGeziMap.get(goodsInfo.getMachineID())).get(index))) {
                                // 有货的时候
                                kucun = 1;
                            }
                        }
                    }
                    if (kucun == 0) { // 缺货的时候
                        if (queHuoInfoMap.containsKey(goodsInfo.getMachineID())) {
                            queHuoInfoMap.put(goodsInfo.getMachineID(), queHuoInfoMap.get(goodsInfo.getMachineID()) + goodsInfo.getRoad_no() + ";");
                        } else {
                            queHuoInfoMap.put(goodsInfo.getMachineID(), goodsInfo.getRoad_no() + ";");
                        }
                    } else {
                        if (!queHuoInfoMap.containsKey(goodsInfo.getMachineID())) {
                            queHuoInfoMap.put(goodsInfo.getMachineID(), "");
                        }
                    }
                    goodsInfo.setKuCun(String.valueOf(kucun));
                    if (checkMap1.containsKey(goodsInfo.getGoodsID())) {
                        MyApplication.getInstance().getCabinetTotalGoods().add(goodsInfo);
                        checkMap1.put(goodsInfo.getGoodsID(), checkMap1.get(goodsInfo.getGoodsID()) + kucun);
                        continue;
                    }
                    MyApplication.getInstance().getCabinetTotalGoods().add(goodsInfo);
                    checkMap1.put(goodsInfo.getGoodsID(), kucun);
                }
                for (String str : checkMap1.keySet()) {
                    for (GoodsInfo goodsInfo : cabinetGoods) {
                        if (checkMap1.get(str) == 0) {
                            if (str.equals(goodsInfo.getGoodsID())) {
                                MyApplication.getInstance().getCabinetGoods().add(goodsInfo);
                                break;
                            }
                        } else if (checkMap1.get(str) > 0) {
                            if (str.equals(goodsInfo.getGoodsID()) && Integer.parseInt(goodsInfo.getKuCun()) > 0) {
                                MyApplication.getInstance().getCabinetGoods().add(goodsInfo);
                                break;
                            }
                        }
                    }
                }
                // 计算商品库存
                for (GoodsInfo goodsInfo : MyApplication.getInstance().getCabinetGoods()) {
                    if (checkMap1.get(goodsInfo.getGoodsID()) > 0) {
                        goodsInfo.setIsSoldOut(SysConfig.NOTSOLDOUT_FLAG); //0:未空 1:售空
                    } else {
                        goodsInfo.setIsSoldOut(SysConfig.ISSOLDOUT_FLAG);
                    }
                }
                realm.commitTransaction();
                // 添加 缺货信息
                if (queHuoInfoMap.size() > 0) {
                    for (String machineID : queHuoInfoMap.keySet()) {
                        machineQuehuo(machineID, queHuoInfoMap.get(machineID));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Observable observable, Object o) { //一条指令一个线程
        postData(((BaseInfo) o).getCode(), ((BaseInfo) o).getInfo());
    }

    /**
     * 出货查询计时器timer
     */
    private class BeforeSellSearchTimer extends MyCountDownTimer {
        public BeforeSellSearchTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕
            machineQueryType = "";
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
        }
    }

    @Override
    protected void onPause() {
        if (comVSI != null) {
            comVSI.closeSerialPort();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (comVSI != null) {
            comVSI.openSerialPort();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (comVSI != null) {
            Log.e(TAG, "关闭串口连接了");
            comVSI.closeSerialPort();
        }
        if (machineInfoGetTimer != null) {
            machineInfoGetTimer.cancel();
            machineInfoGetTimer = null;
        }
        if (beforeSellSearchTimer != null) {
            beforeSellSearchTimer.cancel();
            beforeSellSearchTimer = null;
        }
        if (realm != null) {
            realm.close();
        }
        if (ivsiCallback2ViewWeakReference != null) {
            ivsiCallback2ViewWeakReference.clear();
            ivsiCallback2ViewWeakReference = null;
        }
        if (presenter != null) {
            presenter.detacheView();
        }
        SerialObservable.getInstance().unregist(this);
        super.onDestroy();
    }

    int geziKucunCheckCount = 0;
    /**
     * Handle线程，接收线程过来的消息
     */
    Handler msgHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // 处理消息
            switch (msg.what) {

                case XIAN_SHI:
                    String s = msg.obj.toString();
                    String sub = s.substring(0, 4);
                    if (!sub.equals("1001")) {
                        machineConncted = true;
                    }
                    // 时间过来的话,更新输入框信息
                    if (sub.equals("E203")) {
                        s = s.replace("E203", "");
                        Log.i(TAG, "现在时间" + s);
                        MyApplication.getInstance().setSystemTime(s);
                    } else if (sub.equals("E205")) {
                        s = s.replace("E205", "").replace("/各货道现金价_角:", "");
                        Log.i(TAG, "各货道现金价_角" + s);
                    } else if (sub.equals("E206")) {
                        s = s.replace("E206", "").replace("/各货道非现金价_角:", "");
                        Log.i(TAG, "各货道非现金价_角" + s);
                    } else if (sub.equals("E207")) {
                        s = s.replace("E207", "").replace("/各货道商品编码:", "");
                        Log.i(TAG, "各货道商品编码" + s);
                        MyApplication.getInstance().setGoodsCode(s);
                    } else if (sub.equals("1001")) {
                        Log.i(TAG, "主机连接上了");
                        // 主机连接上了
                        machineConncted = true;
                    } else if ("1000".equals(sub)) {
                        // 启动十秒后
                        if (!isNeedInitMachineInfo && (new Date().getTime() - startTime) > 20000 && !isInBackPage) {
                            Intent ii = new Intent(ComActivity.this, LoadingActivity.class);
                            startActivity(ii);
                            finish();
                        }
                        // 主机断开链接
                        machineConncted = false;
                        Log.e(TAG, "主机断开连接:" + machineConncted + " " + isNeedInitMachineInfo);
                    } else if ("0078".equals(sub)) {// 签到成功, 取得机器类型
                        s = s.replace("0078", "");
                        String[] info = s.split(",");
                        Log.e(TAG, "签到成功!: " + s);
                        MyApplication.getInstance().setMachineType(info[3]);
                        machineConncted = true;
                        if (machineInfoGetTimer != null) {
                            machineInfoGetTimer.cancel();
                        }
                    } else if ("0073".equals(sub)) {// 货道信息
                        s = s.replace("0073", "");
                        String[] ss = s.split(",");
                        Log.e(TAG, "货道数: " + s);
                        MyApplication.getInstance().setRoadCount(Integer.parseInt(ss[0]));
                        // 附加柜信息
                        String fujianInfo = ss[1];
                        if (fujianInfo.endsWith("|")) {
                            fujianInfo = fujianInfo.substring(0, fujianInfo.length() - 1);
                        }
                        fujianInfo = fujianInfo.replace("|", ",");
                        String[] fujian = fujianInfo.split(",");
                        for (int i = 1; i < fujian.length; i++) {
                            if ("1".equals(fujian[i])) {
                                // 存在格子柜
                                MyApplication.getInstance().getGeziList().add(i + 1);
                            }
                        }
                        machineConncted = true;
                    } else if ("007C".equals(sub)) { // 出货记录
                        String result = comVSI.checkThingsHaveOrNotForNow(0);
                        if (!"".equals(result)) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    String result = comVSI.checkThingsHaveOrNotForNow(0);
                                    if (!"".equals(result)) {
                                        comVSI.checkThingsHaveOrNot(0);
                                    }
                                }
                            }, 300);
                        }
                        s = s.replace("007C", "");
//                        36,250,9390,0,1,2176,0,0,0,2000-11-27 21:35 3',2152,1
//                        "售卖料道编号:" + s[0]+ ",售卖金额:" + s[1] + ",出货序列号:" + s[2] + ",卡号=" + s[3] + ",支付方式=" + s[4]
//                                + ",商品编号=" + s[5] + ",售货机设备编号=" + s[6] + ",出货结果="  + (s[7].equals("0")?"成功":"失败")
//                                +  ",卡剩余金额=" + s[8] + ",交易时间=" + s[9] + ",控制序列号=" + s[10] + 箱号 s[11];
                        Log.e(TAG, "出货记录!: " + s);
                        final String[] info = s.split(",");
                        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_S);
                        String saleTime = sdf.format(new Date());
                        // 判断出货记录是否已经存在
                        try {
                            PayModel payModel = realm.where(PayModel.class).equalTo("MachineTradeNo", info[2]).equalTo("PayTime", saleTime).findFirst();
                            if (payModel != null && payModel.getMachineTradeNo() != null && !"".equals(payModel.getMachineTradeNo())) {
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        int roadNo = Integer.parseInt(info[0]);
                        // 出货成功 (出货货道, 产品编号, 出货量)
                        // 根据货道获取产品code
                        GoodsInfo goodsInfo;
                        if (Integer.parseInt(info[11]) == 0) {
                            goodsInfo = realm.where(GoodsInfo.class).equalTo("goodsBelong", "1").equalTo("road_no", roadNo).findFirst();
                        } else if (Integer.parseInt(info[11]) == 1) {
                            //副柜的有效为十六进制11-68，换算为十进制之后为17-104
                            String ss = Integer.toHexString(Integer.parseInt(info[0]));
                            roadNo = Integer.parseInt(ss);
                            Log.e("COM", "货道号:" + roadNo);
                            goodsInfo = realm.where(GoodsInfo.class).equalTo("goodsBelong", "3").equalTo("road_no", roadNo).equalTo("machineID"
                                    , MyApplication.getInstance().getBindDeskList().get(0).getMachineSn()).findFirst();

                        } else {
                            String ss = Integer.toHexString(Integer.parseInt(info[0]));
                            if (ss.length() >= 2) {
                                ss = ss.substring(ss.length() - 2);
                            }
                            roadNo = Integer.parseInt(ss);
                            Log.e("COM", "货道号:" + roadNo);
                            goodsInfo = realm.where(GoodsInfo.class).equalTo("goodsBelong", "2").equalTo("road_no", roadNo).equalTo("machineID"
                                    , MyApplication.getInstance().getBindGeZis().get(Integer.parseInt(info[11]) - 2).getMachineSn()).findFirst();
                        }
                        if ("0".equals(info[7]) && Integer.parseInt(info[11]) == 0) {
                            updateLocalKuCun(String.valueOf(roadNo));
                        }

                        if ("0".equals(info[7]) && Integer.parseInt(info[11]) == 1) {
                            updateDeskKucun(String.valueOf(roadNo));
                        }
                        machineQueryType = "";
                        int payType = 1;
                        if ("4".equals(info[4])) {// 支付了0元 非现金支付
                            payType = 49;
                        }
                        String record;
                        // 记录信息 交易号:212,交易时间:20160910104948,交易种类:1:现金,现金扣费:10角,非现金扣费:0角,出货结果:1成功,货道号:21,商品编号:10
                        if (payType == 49) { // 在线支付
                            record = info[2] + "," + saleTime + "," + payType + ",0," + goodsInfo.getPrice() +
                                    "," + (info[7].equals("0") ? "1" : "2") + "," + roadNo + "," + goodsInfo.getGoodsCode();
                            if (info[7].equals(Constant.SHIPMENTSUCCESS)) {
                                onlinePaySend2MQ(getBasePayModel(record, goodsInfo, info[2], String.valueOf(roadNo), false), MyApplication.getInstance().getNoCashorderSn(), info[4], Constant.DELIVERYSUCCESS);
                            } else {
                                shipmentFail(getBasePayModel(record, goodsInfo, info[2], String.valueOf(roadNo), false), info[4], Constant.DELIVERYFAIL);
                            }
                        } else { // 现金支付
                            if (goodsInfo != null) {
                                record = info[2] + "," + saleTime + "," + payType + "," + Integer.parseInt(info[1]) / 10 +
                                        ",0," + (info[7].equals("0") ? "1" : "2") + "," + roadNo + "," + goodsInfo.getGoodsCode();
                            } else {
                                goodsInfo = new GoodsInfo();
                                goodsInfo.setPrice(String.valueOf(Integer.parseInt(info[1]) * 0.1));
                                goodsInfo.setMachineID(MyApplication.getInstance().getMachine_sn());
                                record = info[2] + "," + saleTime + "," + payType + "," + Integer.parseInt(info[1]) / 10 +
                                        ",0," + (info[7].equals("0") ? "1" : "2") + "," + roadNo + "," + goodsInfo.getGoodsCode();
                            }
                            if (info[7].equals(Constant.SHIPMENTSUCCESS)) {
                                cashPaySend2MQ(getBasePayModel(record, goodsInfo, info[2], String.valueOf(roadNo), true), info[4], Constant.DELIVERYSUCCESS);
                                if (goodsInfo.getGoodsCode() != null) {
                                    shipmentSuccess(String.valueOf(roadNo), goodsInfo.getGoodsCode(), "1", machineQueryType, saleOrderID, Integer.parseInt(info[11]),
                                            saleTime, info[2]);
                                }
                            } else {
                                shipmentFail(getBasePayModel(record, goodsInfo, info[2], String.valueOf(roadNo), true), info[4], Constant.DELIVERYFAIL);
                            }
                        }

                    } else if (s.length() >= 6 && "007700".equals(s.substring(0, 6))) {// 货道信息
                        s = s.replace("007700", "");
                        // "/投入金额信息,投币金额:" + s[0] + ",支付方式:" + s[1] + ",运行状态:" + s[2];
                        MyApplication.getInstance().setRoadCount(Integer.parseInt(s.split(",")[0]));
                    } else if ("007D".equals(sub)) {// 运行状态
                        s = s.replace("007D", "");
                        Log.e(TAG, "运行状态!: " + s);
//                        "/运行状态,能否营业:" + s[0] + ",门开关:" + s[1] + ",选中的按键编号:" + s[2]
//                                + ",选中的按键编号对应的料道编号:" + s[3]+ ",选中的按键编号对应的商品价格:" + s[4]+ ",1元个数:" + s[5]+ ",5角个数:" + s[6]
//                          s[7] 附件柜信息(0,1,0,0,0,0,0)【食品柜、格子柜1、……6】;
                        String[] ss = s.split(",");
                        String sysStatus = ss[0] + ",1," + ss[1] + ",1,1,0,0,0";
                        MyApplication.getInstance().setSystemStatus(sysStatus);
                        // 附加柜信息
                        if (ivsiCallback2ViewWeakReference != null && ivsiCallback2ViewWeakReference.get() != null) {
                            ivsiCallback2ViewWeakReference.get().MsgCallback(s);
                        }
                    } else if ("0079".equals(sub)) {// 故障信息 TODO
                        s = s.replace("0079", "");
                        MyApplication.getInstance().setTroubleStatus(s);
                        Log.i(TAG, "故障信息:" + s);
                        // 添加故障信息存入数据库
                        addFaultInfo(s);
                    } else if ("007B".equals(sub)) {// 货道信息
                        long now = new Date().getTime();
                        s = s.replace("007B", "");
                        if (now - emptyIntoGetTime < 1000) {
                            if (s.equals(lastRoadInfo)) {
                                break;
                            }
                        }
                        doEmptyControl(s);
                        lastRoadInfo = s;
                        emptyIntoGetTime = now;
                    } else if (s.length() >= 6 && "007617".equals(s.substring(0, 6))) {
                        s = s.replace("007617", "");
                        Log.e(TAG, "轮询信息:" + s);
                        String[] info = s.split(",");
                        if ("1".equals(doorStatus) && "0".equals(info[1])) {
                            getRoadEmptyInfo();
                        }
                        doorStatus = info[1];
                        if (!"".equals(info[3]) && !"0".equals(info[3])) {
                            // 用户按了商品按钮
                            Log.e(TAG, "用户选择了:" + info[3]);
                            // 取得商品code
                            GoodsInfo goodsInfo = realm.where(GoodsInfo.class).equalTo("goodsBelong", "1").equalTo("road_no", Integer.parseInt(info[3])).findFirst();
                            if (goodsInfo != null && !"".equals(goodsInfo.getGoodsCode())) {
                                selectGoodByButton(info[3], goodsInfo.getGoodsCode());
                            }
                            showCashCount(Integer.parseInt(info[5]) / 10);
                        } else {
                            if (!"".equals(info[5]) && !"0".equals(info[5])) {
                                showCashCount(Integer.parseInt(info[5]) / 10);
                            }
                        }
                    } else if ("007X".equals(sub)) {// 格子柜1货道信息
                        long now = new Date().getTime();
                        s = s.replace("007X", "");
                        String[] info = s.split(";");
                        final int boxIndex = Integer.parseInt(info[0]);
                        if (nowBoxIndex == boxIndex) {
                            if (now - geziEmptyGetTime < 1500) {
                                break;
                            }
                        }
                        nowBoxIndex = boxIndex;
                        geziEmptyGetTime = now;
                        String roadInfo = info[1].replace("1", "5").replace("0", "1").replace("5", "0");
                        Log.i(TAG, "格子柜(箱号:" + boxIndex + ")货道信息:" + roadInfo);
                        // 处理货道信息
                        Map<Integer, String> kucunMap = new HashMap<>();
                        int i = 1;
                        for (String kucunStatus : roadInfo.split(",")) {
                            kucunMap.put(i, kucunStatus);
                            i++;
                        }
                        MyApplication.getInstance().getAokemaGeZiKuCunMap().put(boxIndex, kucunMap);
                        // 数据设置问题的时候 最多取六次。
                        if (geziKucunCheckCount > 6) {
                            break;
                        }
                        // 如果还有格子柜 继续去取得库存信息
                        if (boxIndex - 1 < MyApplication.getInstance().getBindGeZis().size() && (boxIndex - 1) < MyApplication.getInstance().getGeziList().size()) {
                            geziKucunCheckCount++;
                            // 获取格子柜货道售空信息
                            String ss = comVSI.checkThingsHaveOrNot(MyApplication.getInstance().getGeziList().get(boxIndex - 1));
                            break;
                        } else {
                            // 信息获取完成后处理
                            AfterMachineInfoGetOver();
                            if (machineInfoGetTimer != null) {
                                machineInfoGetTimer.cancel();
                            }
                            if (!isNeedInitMachineInfo) {
                                handleGeziKuCun();
                                // 更新商品展示页面库存信息
                                updateCabinetGoodsInfo();
                            }
                        }

                    } else if ("0080".equals(sub)) { //  弹簧机料道配置信息
                        try {
                            s = s.replace("0080", "");
                            String[] info = s.split(";");

                            MyApplication.getInstance().setDeskRoadCount(Integer.parseInt(info[1]));
                            MyApplication.getInstance().setDeskConnState(true);

                            String roadInfo = "";
                            if (info.length > 2) {
                                Log.e(TAG, "弹簧机有效货道信息:" + info[2]);
                                roadInfo = info[2];
                            }
                            if (MyApplication.getInstance().getDeskRoadList().size() != 0) {
                                MyApplication.getInstance().getDeskRoadList().clear();
                            }
                            if (roadInfo != null && !"".equals(roadInfo)) {
                                for (String road : roadInfo.split(",")) {
                                    MyApplication.getInstance().getDeskRoadList().add(Integer.parseInt(road));
                                }
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                    } else if ("0081".equals(sub)) { // 格子柜料道配置信息
                        try {
                            s = s.replace("0081", "");
                            String[] info = s.split(";");
                            int boxIndex = Integer.parseInt(info[0]);
                            Log.e(TAG, "格子柜1货道数:" + info[1]);
                            MyApplication.getInstance().getGeziRoadCount().put(boxIndex, Integer.parseInt(info[1]));
                            String roadInfo = "";
                            if (info.length > 2) {
                                Log.e(TAG, "格子柜有效货道信息:" + info[2]);
                                roadInfo = info[2];
                            }
                            if (!MyApplication.getInstance().getGeziRoadListMap().containsKey(boxIndex)) {
                                MyApplication.getInstance().getGeziRoadListMap().put(boxIndex, new ArrayList<Integer>());
                            } else {
                                MyApplication.getInstance().getGeziRoadListMap().get(boxIndex).clear();
                            }
                            if (roadInfo != null && !"".equals(roadInfo)) {
                                for (String road : roadInfo.split(",")) {
                                    MyApplication.getInstance().getGeziRoadListMap().get(boxIndex).add(Integer.parseInt(road));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                default:
                    break;
            }
            return false;
        }
    });


    private void doEmptyControl(String s) {
        Log.i(TAG, "货道信息收到:" + s);
        s = s.replace("1", "5").replace("0", "1").replace("5", "0");

        Log.i(TAG, "货道信息HAH:" + s);
        // 如果有格子柜的话 不结束,继续去取得格子柜的货道是否售空信息
        if (MyApplication.getInstance().getGeziList().size() > 0) {
            geziKucunCheckCount = 1;
            // 获取格子柜货道售空信息
            String ss = comVSI.checkThingsHaveOrNot(MyApplication.getInstance().getGeziList().get(0));
        } else {
            // 信息获取完成后处理
            AfterMachineInfoGetOver();
            if (machineInfoGetTimer != null) {
                machineInfoGetTimer.cancel();
            }
        }
        L.v(SysConfig.ZPush, "2-------->" + s);
        MyApplication.getInstance().setSellEmptyInfo(s);

        if (!isNeedInitMachineInfo) {
            handleGoodsKuCun();
            // 更新商品展示页面库存信息
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateGoodsInfo();
                }
            }, 1000);
        }
        Log.i(TAG, "货道售空情况:" + s);
        // 判断是否缺货,缺货的话提交服务器
        try {
            String[] emptyInfoArr = MyApplication.getInstance().getSellEmptyInfo().split(",");
            String roads = "";
            GoodsInfo goodsInfo;
            int i = 0;
            for (String isEmpty : emptyInfoArr) {
                // 取得货道商品
                goodsInfo = realm.where(GoodsInfo.class).equalTo("goodsBelong", "1").equalTo("road_no", i + 1).findFirst();
                if (goodsInfo == null || goodsInfo.getGoodsCode() == null || "".equals(goodsInfo.getGoodsCode()) || "0".equals(goodsInfo.getGoodsCode())) {
                    i++;
                    continue;
                }
                if ("1".equals(isEmpty)) {
                    // 机器缺货
                    roads = roads + (i + 1) + ";";
                }
                i++;
            }
            // 机器缺货
            machineQuehuo(MyApplication.getInstance().getMachine_sn(), roads);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postData(String todispcode, String todispinfo) {
        String[] s = null;
        Message msg = msgHandler.obtainMessage();
        msg.what = XIAN_SHI;
        msg.obj = "未知";
        switch (todispcode) {
            case "0078":
            case "0073":
            case "007C":
            case "007700":
            case "007701":
            case "007617":
            case "007603":
            case "007D":
            case "007B":
            case "0079":
            case "007X":
            case "E102":
            case "E201":
            case "E203":
            case "E207":
            case "E301":
            case "E302":
            case "E303":
            case "E304":
            case "E306":
            case "E401":
            case "E500":
            case "0080":// 弹簧机料道配置信息
            case "0081":// 格子柜料道配置信息
                msg.obj = todispcode + todispinfo;
                break;
            case "1000":
                msg.obj = "" + todispcode + "/机器主控失联";
                break;
            case "1001":
                msg.obj = "" + todispcode + "/机器主控已连接上";
                break;
            case "2000":
                msg.obj = "" + todispcode + "/收到否定回答!";
                break;
            case "2001":
                msg.obj = todispcode;
                break;
            case "E205":
                s = todispinfo.split(",");
                msg.obj = "" + todispcode + "/" + "各货道现金价_角:1/" + s[0];
                for (int i = 1; i < s.length; i++) {
                    msg.obj = msg.obj + "," + (i + 1) + "/" + s[i];
                }
                break;
            case "E206":
                s = todispinfo.split(",");
                msg.obj = "" + todispcode + "/" + "各货道非现金价_角:1/" + s[0];
                for (int i = 1; i < s.length; i++) {
                    msg.obj = msg.obj + "," + (i + 1) + "/" + s[i];
                }
                break;
            case "E213":
                s = todispinfo.split(",");
                msg.obj = todispcode + s[0];
                break;
            case "E305":
                s = todispinfo.split(",");
                msg.obj = "" + todispcode + "/" + "库1温度:" + Integer.parseInt(s[0]) + "," + "库2温度:" + Integer.parseInt(s[1]) + "," + "库3温度:" + Integer.parseInt(s[1]);
                break;
            case "E402":
                msg.obj = "" + todispcode + "/" + "用户按了退币键";
                break;
            default:
                break;
        }
        if (!msg.obj.toString().equals("no")) {
            msgHandler.sendMessage(msg);
        }
    }

    /**
     * 获取机器信息计时器
     */
    class MachineInfoGetTimer extends MyCountDownTimer {
        public MachineInfoGetTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕
            getMachineDataForCom();
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
        }
    }


    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed()");
        //不允许右键退出,必须使用按钮退出
        //super.onPause();
    }

    /**
     * 添加故障信息存入数据库
     */
    private void addFaultInfo(String s) {
        final MachineFaultRecord machineFaultRecord = new MachineFaultRecord();
        String queBiFive = "0";
        String queBiOne = "0";
        if (!"".equals(s)) {
            if (s.contains("Y7-6")) {// 缺币五角
                queBiFive = "1";
            }
            if (s.contains("Y7-7")) {// 缺币一元
                queBiOne = "1";
            }
        }
        machineQueB(MyApplication.getInstance().getMachine_sn(), queBiFive, queBiOne);
        int failCount = MyApplication.getInstance().getConnetFailGeziList().size();
        MyApplication.getInstance().getConnetFailGeziList().clear();
        if (s.contains("Y9-1")) {// 格子柜1连接失败
            MyApplication.getInstance().getConnetFailGeziList().add(2);
        }
        if (s.contains("Y9-2")) {// 格子柜2连接失败
            MyApplication.getInstance().getConnetFailGeziList().add(3);
        }
        if (s.contains("Y9-3")) {// 格子柜3连接失败
            MyApplication.getInstance().getConnetFailGeziList().add(4);
        }
        if (s.contains("Y9-4")) {// 格子柜4连接失败
            MyApplication.getInstance().getConnetFailGeziList().add(5);
        }
        if (s.contains("Y9-5")) {// 格子柜5连接失败
            MyApplication.getInstance().getConnetFailGeziList().add(6);
        }
        if (s.contains("Y9-6")) {// 格子柜6连接失败
            MyApplication.getInstance().getConnetFailGeziList().add(7);
        }

        if (s.contains("Y9-0")) {// 副柜连接失败
            MyApplication.getInstance().setDeskConnState(false);
        }

        if (!isNeedInitMachineInfo) {
            if (failCount != MyApplication.getInstance().getConnetFailGeziList().size()) {
                handleGeziKuCun();
                // 更新商品展示页面库存信息
                updateCabinetGoodsInfo();
            }
        }
        Map<String, String> faultRst = MachineFaultUtil.getMachineFaultInfo(s);
        // 主控故障
        if ("".equals(faultRst.get(MachineFaultUtil.MASTER_TAG))) {
            machineFaultRecord.setIsMasterFault("0");
        } else {
            machineFaultRecord.setIsMasterFault("1");
        }
        machineFaultRecord.setMasterAlarmReason(faultRst.get(MachineFaultUtil.MASTER_TAG));
        // 纸币故障
        if ("".equals(faultRst.get(MachineFaultUtil.PAPER_TAG))) {
            machineFaultRecord.setIsPaperFault("0");
        } else {
            machineFaultRecord.setIsPaperFault("1");
        }
        machineFaultRecord.setPaperAlarmFault(faultRst.get(MachineFaultUtil.PAPER_TAG));
        // 硬币故障
        if ("".equals(faultRst.get(MachineFaultUtil.COIN_TAG))) {
            machineFaultRecord.setIsCoinFault("0");
        } else {
            machineFaultRecord.setIsCoinFault("1");
        }
        machineFaultRecord.setCoinAlarmReason(faultRst.get(MachineFaultUtil.COIN_TAG));

        machineFaultRecord.setCreateTime(new Date().getTime());
        machineFaultRecord.setIsUploaded("0");

        // 取得最后一条故障信息, 如果相同就不要再去处理了
        RealmResults<MachineFaultRecord> results = realm.where(MachineFaultRecord.class).findAll();
        results = results.sort("createTime", Sort.DESCENDING);
        if (results != null && results.size() > 0) {
            MachineFaultRecord oldMachineFault = results.first();
            if (oldMachineFault != null) {
                if (oldMachineFault.getIsMasterFault().equals(machineFaultRecord.getIsMasterFault())
                        && oldMachineFault.getMasterAlarmReason().equals(machineFaultRecord.getMasterAlarmReason())
                        && oldMachineFault.getIsPaperFault().equals(machineFaultRecord.getIsPaperFault())
                        && oldMachineFault.getPaperAlarmFault().equals(machineFaultRecord.getPaperAlarmFault())
                        && oldMachineFault.getIsCoinFault().equals(machineFaultRecord.getIsCoinFault())
                        && oldMachineFault.getCoinAlarmReason().equals(machineFaultRecord.getCoinAlarmReason())) {
                    return;
                }
            }
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(machineFaultRecord);
            }
        });
    }

    /**
     * 机器缺货
     */
    private void machineQuehuo(String machineSn, String roads) {
        // give sub method to do
        final QueHuoRecord queHuoRecord = new QueHuoRecord();
        String usefulRoads = roads;
        if ("".equals(usefulRoads)) {
            queHuoRecord.setIsQueHuo("0");
        } else {
            queHuoRecord.setIsQueHuo("1");
        }

        queHuoRecord.setMachineSn(machineSn);
        queHuoRecord.setRoad_no(usefulRoads);
        queHuoRecord.setCreateTime(new Date().getTime());
        queHuoRecord.setIsUploaded("0");

        // 取得最后一条故障信息, 如果相同就不要再去处理了
        RealmResults<QueHuoRecord> results = realm.where(QueHuoRecord.class).equalTo("machineSn", machineSn).findAll();
        results = results.sort("createTime", Sort.DESCENDING);
        if (results != null && results.size() > 0) {
            QueHuoRecord record = results.first();
            if (record != null) {
                if (record.getIsQueHuo().equals(queHuoRecord.getIsQueHuo())
                        && record.getMachineSn().equals(queHuoRecord.getMachineSn())
                        && record.getRoad_no().equals(queHuoRecord.getRoad_no())) {
                    return;
                }
            }
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(queHuoRecord);
            }
        });
    }
}