package com.zongsheng.drink.h17;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.dwin.navy.serialportapi.ComAokema;
import com.zongsheng.drink.h17.background.MarkLog;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
    public boolean isMachineConnected = false;
    private String TAG = "ComActivity";
    //在GeziActivity中用到，回调
    private Reference<IVSICallback2View> iVSICallback2ViewWeakReference = null;
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
     * 主机现金出货
     */
    public void sellByCash(int goodsCode) {
//        Log.i(TAG, "现金出货前查询");
        MyApplication.getInstance().getLogBuyAndShip().d("开始发送出货指令");
        machineQueryType = "1";// 现金出货
        // 根据goodsCode查找出货货道商品
        RealmResults<GoodsInfo> results = realm.where(GoodsInfo.class).equalTo("goodsBelong", "1").
                equalTo("goodsCode", String.valueOf(goodsCode)).findAll();
        results = results.sort("road_no", Sort.ASCENDING);
        saleGoodsInfo = null;
        if (results.size() > 0) {
            String[] sellEmptyArr = MyApplication.getInstance().getSellEmptyInfo().split(",");
            // 有货的商品货道号
            //TODO:我用HashSet代替了HashMap
            HashSet<Integer> goodsCodeRoadNum = new HashSet<Integer>();
            for (int i = 0; i < sellEmptyArr.length; i++) {
                if ("0".equals(sellEmptyArr[i])) { // 有货
                    //货道从1开始
                    goodsCodeRoadNum.add((i + 1));
                }
            }
//            Log.i(TAG, "取得本地保存产品数据:" + results.size());
            for (GoodsInfo goodsInfo : results) {
                //如果有货的货道号列表中包含该商品所属货道号，说明有货
                if (goodsCodeRoadNum.contains(goodsInfo.getRoad_no())) {
                    saleGoodsInfo = realm.copyFromRealm(goodsInfo);
                    break;
                }
            }
        }
        if (saleGoodsInfo == null) {
            MyApplication.getInstance().getLogBuyAndShip().d("所选商品已售空");
            //TODO:这里已经完成现金付款，但是没有找到可以出货的商品，应该退币或提示如何退币
            ToastUtils.showToast(this, "所选商品已售空");
            return;
        }
        MyApplication.getInstance().getLogBuyAndShip().d("找到要出货的商品 = 商品名 : "+saleGoodsInfo.getGoodsName()+" : "+saleGoodsInfo.getGoodsCode()+" ; 货道号 : "+saleGoodsInfo.getRoad_no());
        // 判断两次操作时间间隔
        long time = new Date().getTime();
        //两次出货时间不能小于1秒
        if (lastSaleTime != 0 && time - lastSaleTime < 1000) {
            SystemClock.sleep(1000 - (time - lastSaleTime));
        }
        lastSaleTime = time;
        // 安卓工控机发起扣款请求 dealSerialNumber:交易序列号,channelNum:料道值 ,PAY_WAY支付方式
        final int dealSerialNumber = (int) new Date().getTime();
        long payPrice = ((int) Double.parseDouble(saleGoodsInfo.getPrice())) * 10;
        String s = comVSI.toPay(dealSerialNumber, (byte) saleGoodsInfo.getRoad_no(), (byte) 1, payPrice, 0);
//        Log.e(TAG, "现金出货结果:" + s);

        if (!"".equals(s)) {
            MyApplication.getInstance().getLogBuyAndShip().d("机器正忙，延时0.5秒重新发送指令");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String s = comVSI.toPay(dealSerialNumber, (byte) saleGoodsInfo.getRoad_no(), (byte) 1, ((int) Double.parseDouble(saleGoodsInfo.getPrice())) * 10, 0);
//                    Log.e(TAG, "现金出货结果:" + s);
                }
            }, 500);
        }
    }

    /**
     * 非现金出货（饮料机）
     */
    public void sellByNoCash(int goodsCode, String orderID, String saleOrderPrice) {
        MyApplication.getInstance().getLogBuyAndShip().d("开始发送出货指令");
//        Log.i(TAG, "非现金出货前查询");
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
            //TODO:我用HashSet代替了HashMap
            HashSet<Integer> goodsCodeRoadNoMap = new HashSet<Integer>();
            for (int i = 0; i < sellEmptyArr.length; i++) {
                if ("0".equals(sellEmptyArr[i])) { // 有货
                    goodsCodeRoadNoMap.add((i + 1));
                }
            }
//            Log.i(TAG, "取得本地保存产品数据:" + results.size());
            for (GoodsInfo goodsInfo : results) {
                if (goodsCodeRoadNoMap.contains(goodsInfo.getRoad_no())) {
                    saleGoodsInfo = realm.copyFromRealm(goodsInfo);
                    break;
                }
            }
        }
        if (saleGoodsInfo == null) {
            MyApplication.getInstance().getLogBuyAndShip().d("所选商品已售空");
            ToastUtils.showToast(this, "所选商品已售空");
            return;
        }
//        Log.e(TAG, "推送后真实出货的货道数:" + saleGoodsInfo.getRoad_no() + " " + goodsCode);
        // 安卓工控机发起扣款请求 dealSerialNumber:交易序列号,channelNum:料道值 ,PAY_WAY支付方式
        final int dealSerialNumber = (int) new Date().getTime();
        String s = comVSI.toPayForNoCash(dealSerialNumber, (byte) saleGoodsInfo.getRoad_no(), (byte) 1, 0, 0);
//        Log.e(TAG, "非现金出货结果:" + s);
        if (!"".equals(s)) {
            MyApplication.getInstance().getLogBuyAndShip().d("机器正忙，延时0.5秒重新发送指令");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String s = comVSI.toPayForNoCash(dealSerialNumber, (byte) saleGoodsInfo.getRoad_no(), (byte) 1, 0, 0);
//                    Log.e(TAG, "非现金出货结果:" + s);
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
        if (!"".equals(s)) {
            SystemClock.sleep(500);
            s = comVSI.setGeziChannelPrice(boxNo, roadNo, price);
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
//        Log.e("here", "非现金出货结果:" + s);
        if (!"".equals(s)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String s = comVSI.toPayForNoCash(dealSerialNumber, (byte) road_no, (byte) 1, 0, 1);
//                    Log.e(TAG, "非现金出货结果:" + s);
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
//        Log.e(TAG, "澳柯玛格子柜出货结果:" + s + " " + money);
        if (!"".equals(s)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String s = comVSI.toPay(dealSerialNumber, (byte) road_no, (byte) 1, money * 10, boxIndex);
//                    Log.e(TAG, "澳柯玛格子柜出货结果1:" + s);
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
//        Log.e(TAG, "非现金出货结果:" + s);
        if (!"".equals(s)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String s = comVSI.toPayForNoCash(dealSerialNumber, (byte) road_no, (byte) 1, 0, boxIndex);
//                    Log.e(TAG, "非现金出货结果:" + s);
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
        iVSICallback2ViewWeakReference = new WeakReference<>(ivsiCallback2View);
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
        MyApplication.getInstance().getLogBuyAndShip().d("将销售记录存入数据库 = 订单号 : "+payModel.getOrderSn()+" ; 商品名 : "+payModel.getGoodsName()+" : "+payModel.getGoodsCode());
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
        MyApplication.getInstance().getLogBuyAndShip().d("将出货失败记录存入数据库 = 订单号 : "+payModel.getOrderSn()+" ; 商品名 : "+payModel.getGoodsName()+" : "+payModel.getGoodsCode());
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
        //由LoadingActivity实现
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
                    if (!"".equals(str) && comVSI.isConnected) {
                        // 信息获取完成后处理
                        AfterMachineInfoGetOver();
                    }
                }
            }, 500);
        }
    }

    /**
     * 处理主机产品的缺货信息 是否售空 0:未空 1:售空
     * 根据VMC报告的缺货信息更新GoodsInfo数据库，间接影响售货界面和补货界面显示的货道是否有货情况
     */
    public void handleGoodsKuCun() {
//        MyApplication.getInstance().getLogInit().d("处理格主机的库存和缺货判定");
        try {
            Realm realm = Realm.getDefaultInstance();
            if (MyApplication.getInstance().getSellEmptyInfo() == null || "".equals(MyApplication.getInstance().getSellEmptyInfo())) {
                return;
            }
            //获取主机各货道的缺货情况
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
     * 处理格子柜产品的缺货信息 是否售空 0:未空 1:售空
     * TODO:主机和格子柜的缺货信息判定依赖VMC发来的各货道缺货记录，可能有问题
     */
    private void handleGeziKuCun() {
//        MyApplication.getInstance().getLogBuyAndShip().d("处理格子柜的库存和缺货判定");
        try {
            // 格子柜的map 机器编码, 箱号
            // 当前实际连接成功的格子柜列表
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
            // 加载本地数据库中格子柜的商品信息
            RealmResults<GoodsInfo> results = realm.where(GoodsInfo.class).equalTo("goodsBelong", "2").findAll();
            results = results.sort("road_no", Sort.ASCENDING);
            List<GoodsInfo> cabinetGoods = results;
            Map<String, Integer> checkMap1 = new HashMap<>();
            // 缺货信息map Map<机器ID, 缺货的货道;>记录该机器所有的缺货货道
            Map<String, String> queHuoInfoMap = new HashMap<>();
            MyApplication.getInstance().getCabinetTotalGoods().clear();
            MyApplication.getInstance().getCabinetGoods().clear();
            if (cabinetGoods.size() > 0) {
//                Log.i(TAG, "取得本地保存格子柜产品数据:" + cabinetGoods.size());
                if (!realm.isInTransaction()) {
                    realm.beginTransaction();
                }
                //遍历所有格子柜商品
                for (GoodsInfo goodsInfo : cabinetGoods) {
                    if (goodsInfo == null || goodsInfo.getGoodsID() == null || "".equals(goodsInfo.getGoodsID()) || "0".equals(goodsInfo.getGoodsID())) {
                        continue;
                    }
                    //商品所属格子柜没有连接
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
                        // 这里直接用货道号是不对的。 getAokemaGeZiKuCunMap里面对应的是1~80  这里的road_no 是真实的，不是1~80
                        // 把真实货道号转换成getAokemaGeziKucunMap的格式
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
                    } else { // 不缺货
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
                    //checkMap的键为商品ID，值为该类型商品在格子柜的所有格子中的总和数
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
                MyApplication.getInstance().getLogInit().d("所有格子柜商品种类数 = "+MyApplication.getInstance().getCabinetGoods().size());
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
        if (iVSICallback2ViewWeakReference != null) {
            iVSICallback2ViewWeakReference.clear();
            iVSICallback2ViewWeakReference = null;
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
                        isMachineConnected = true;
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
//                        Log.i(TAG, "各货道商品编码" + s);
                        MyApplication.getInstance().setGoodsCode(s);
                        MyApplication.getInstance().getLogInit().d("各货道商品编码 = "+MyApplication.getInstance().getGoodsCode());
                    } else if (sub.equals("1001")) {
                        Log.i(TAG, "主机连接上了");
                        // 主机连接上了
                        isMachineConnected = true;
                    } else if ("1000".equals(sub)) {
                        // 启动十秒后
                        if (!isNeedInitMachineInfo && (new Date().getTime() - startTime) > 20000 && !isInBackPage) {
                            MyApplication.getInstance().getLogInit().e("VMC失联!");
                            Intent ii = new Intent(ComActivity.this, LoadingActivity.class);
                            MyApplication.getInstance().getLogInit().d("重启LoadingActivity");
                            //TODO:这里销毁当前Activity并重启LoadingActivity有问题，其它Activity怎么办
                            startActivity(ii);
                            finish();
                        }
                        // 主机断开链接
                        isMachineConnected = false;
//                        Log.e(TAG, "主机断开连接:" + isMachineConnected + " " + isNeedInitMachineInfo);
                    } else if ("0078".equals(sub)) {// 签到成功, 取得机器类型
                        s = s.replace("0078", "");
                        String[] info = s.split(",");
//                        Log.e(TAG, "签到成功!: " + s);
                        MyApplication.getInstance().setMachineType(info[3]);
                        MyApplication.getInstance().getLogInit().d("主机类型(1:饮料机) = "+MyApplication.getInstance().getMachineType());
                        isMachineConnected = true;
                        if (machineInfoGetTimer != null) {
                            machineInfoGetTimer.cancel();
                        }
                    } else if ("0073".equals(sub)) {// 货道信息
                        s = s.replace("0073", "");
                        String[] ss = s.split(",");
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
                        isMachineConnected = true;
                    } else if ("007C".equals(sub)) {
                        // VMC发来出货记录
                        String result = comVSI.checkThingsHaveOrNotForNow(0);
                        if (!"".equals(result)) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //查询VMC的指定箱号是否有货，这导致VMC在出货后向PC发送7B指令，导致主机、格子柜、副柜的缺货信息判断处理
                                    String result = comVSI.checkThingsHaveOrNotForNow(0);
                                    if (!"".equals(result)) {
                                        comVSI.checkThingsHaveOrNot(0);
                                    }
                                }
                            }, 300);
                        }
                        s = s.replace("007C", "");
//                        36,250,9390,0,1,2176,0,0,0,2000-11-27 21:35 3',2152,1
//                        "售卖料道编号:" + s[0]+ ",售卖金额:" + s[1] + ",流水号:" + s[2] + ",卡号=" + s[3] + ",支付方式=" + s[4]
//                                + ",商品编号=" + s[5] + ",售货机设备编号=" + s[6] + ",出货结果="  + (s[7].equals("0")?"成功":"失败")
//                                +  ",卡剩余金额=" + s[8] + ",交易时间=" + s[9] + ",控制序列号=" + s[10] + 箱号 s[11];
                        final String[] info = s.split(",");
                        MyApplication.getInstance().getLogBuyAndShip().d("VMC出货记录 = "+Arrays.toString(info));
                        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_S);
                        String saleTime = sdf.format(new Date());
                        // 判断出货记录是否已经存在
                        try {
                            PayModel payModel = realm.where(PayModel.class).equalTo("machineTradeNo", info[2]).equalTo("payTime", saleTime).findFirst();
                            if (payModel != null && payModel.getMachineTradeNo() != null && !"".equals(payModel.getMachineTradeNo())) {
                                MyApplication.getInstance().getLogBuyAndShip().d("当前出货记录已经存在 = 出货序列号 : "+info[2]+" ; payTime : "+saleTime);
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        int roadNo = Integer.parseInt(info[0]);
                        // 出货成功 (出货货道, 产品编号, 出货量)
                        // 根据货道获取产品code
                        GoodsInfo goodsInfo;
                        //如果箱号为0，主机
                        if (Integer.parseInt(info[11]) == 0) {
                            //找到商品
                            goodsInfo = realm.where(GoodsInfo.class).equalTo("goodsBelong", "1").equalTo("road_no", roadNo).findFirst();
                        } else if (Integer.parseInt(info[11]) == 1) {
                            //箱号为1，副柜
                            //副柜的有效为十六进制11-68，换算为十进制之后为17-104
                            String ss = Integer.toHexString(Integer.parseInt(info[0]));
                            roadNo = Integer.parseInt(ss);
//                            Log.e("COM", "货道号:" + roadNo);
                            goodsInfo = realm.where(GoodsInfo.class).equalTo("goodsBelong", "3").equalTo("road_no", roadNo).equalTo("machineID"
                                    , MyApplication.getInstance().getBindDeskList().get(0).getMachineSn()).findFirst();
                            MyApplication.getInstance().getLogBuyAndShip().d("副柜出货，找到货道对应的商品 = 商品名 : "+goodsInfo.getGoodsName()+" : "+goodsInfo.getGoodsCode());
                        } else {
                            //格子柜
                            String ss = Integer.toHexString(Integer.parseInt(info[0]));
                            if (ss.length() >= 2) {
                                ss = ss.substring(ss.length() - 2);
                            }
                            roadNo = Integer.parseInt(ss);
//                            Log.e("COM", "货道号:" + roadNo);
                            goodsInfo = realm.where(GoodsInfo.class).equalTo("goodsBelong", "2").equalTo("road_no", roadNo).equalTo("machineID"
                                    , MyApplication.getInstance().getBindGeZis().get(Integer.parseInt(info[11]) - 2).getMachineSn()).findFirst();
                        }
                        MyApplication.getInstance().getLogBuyAndShip().d("VMC返回出货记录 = 箱号 : "+info[11]+" ; 货道号 : "+goodsInfo.getRoad_no()+" ; 出货结果 : "+(info[7].equals("0")?"成功":"失败")+" ; 商品编号 : "+info[5]+" ; 支付方式 : "+info[4]+" ; 出货序列号 : "+info[2]+" ; 机器编号 : "+info[6]);

                        machineQueryType = "";
                        int payType = 1;
                        if ("4".equals(info[4])) {// 支付了0元 非现金支付
                            payType = 49;
                        }
                        String record;
                        // 记录信息 交易号:212,交易时间:20160910104948,交易种类:1:现金,现金扣费:10角,非现金扣费:0角,出货结果:1成功,货道号:21,商品编号:10
                        PayModel payModel;
                        if (payType == 49) { // 非现金支付
                            MyApplication.getInstance().getLogBuyAndShip().d("非现金支付");
                            record = info[2] + "," + saleTime + "," + payType + ",0," + goodsInfo.getPrice() +
                                    "," + (info[7].equals("0") ? "1" : "2") + "," + roadNo + "," + goodsInfo.getGoodsCode();
                            payModel = getBasePayModel(record, goodsInfo, info[2], String.valueOf(roadNo), false);
                            if (info[7].equals(Constant.SHIPMENTSUCCESS)) {
                                onlinePaySend2MQ(payModel, MyApplication.getInstance().getNoCashorderSn(), info[4], Constant.DELIVERYSUCCESS);
                            } else {
                                //TODO:支付失败后，没有发现退款操作
                                shipmentFail(payModel, info[4], Constant.DELIVERYFAIL);
                            }
                        } else { // 现金支付
                            MyApplication.getInstance().getLogBuyAndShip().d("现金支付");
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
                            payModel = getBasePayModel(record, goodsInfo, info[2], String.valueOf(roadNo), true);
                            if (info[7].equals(Constant.SHIPMENTSUCCESS)) {
                                cashPaySend2MQ(payModel, info[4], Constant.DELIVERYSUCCESS);
                                if (goodsInfo.getGoodsCode() != null) {
                                    //现金支付没有订单号，只有流水号
                                    shipmentSuccess(String.valueOf(roadNo), goodsInfo.getGoodsCode(), "1", machineQueryType, payModel.getOrderSn(), Integer.parseInt(info[11]),
                                            saleTime, String.valueOf(roadNo));
                                }
                            } else {
                                //TODO:支付失败，只是将记录写入数据库，没有显示支付失败信息
                                shipmentFail(payModel, info[4], Constant.DELIVERYFAIL);
                            }
                        }

                        //出货后主机、副柜更新库存，格子柜的更新库存在VMC发送7B指令的时候，主机、格子柜的检查缺货信息也在那里，副柜的检查缺货信息在这里
                        if (Constant.SHIPMENTSUCCESS.equals(info[7])){
                            //出货成功
                            //箱号为0，主机出货成功
                            if (Integer.parseInt(info[11]) == 0) {
                                //更新货道库存
                                updateLocalKuCun(String.valueOf(roadNo));
                                MyApplication.getInstance().getLogBuyAndShip().d("主机出货成功，货道号 = "+roadNo);
                            }else if (Integer.parseInt(info[11]) == 1) {
                                //箱号为1，副柜出货成功
                                MyApplication.getInstance().getLogBuyAndShip().d("副柜出货成功，货道号 = "+roadNo);
                                updateDeskKucun(String.valueOf(roadNo));
                                //TODO:和主机、格子柜不同，副柜在这里检查各货道是否缺货
                                checkDeskQueHuo(info[6],roadNo);
                            }else {
                                //格子柜出货成功
                                MyApplication.getInstance().getLogBuyAndShip().d("格子柜柜出货成功，货道号 = "+roadNo);
                            }
                            //TODO:将出货结果计入日志
                        }else {
                            MyApplication.getInstance().getLogBuyAndShip().d("出货失败");
                        }
                    } else if (s.length() >= 6 && "007700".equals(s.substring(0, 6))) {// 货道信息
                        s = s.replace("007700", "");
                        // "/投入金额信息,投币金额:" + s[0] + ",支付方式:" + s[1] + ",运行状态:" + s[2];
                        MyApplication.getInstance().setRoadCount(Integer.parseInt(s.split(",")[0]));
                    } else if ("007D".equals(sub)) {// 运行状态
                        s = s.replace("007D", "");
//                        Log.e(TAG, "运行状态!: " + s);
//                        "/运行状态,能否营业:" + s[0] + ",门开关:" + s[1] + ",选中的按键编号:" + s[2]
//                                + ",选中的按键编号对应的料道编号:" + s[3]+ ",选中的按键编号对应的商品价格:" + s[4]+ ",1元个数:" + s[5]+ ",5角个数:" + s[6]
//                          s[7] 附件柜信息(0,1,0,0,0,0,0)【食品柜、格子柜1、……6】;
                        String[] ss = s.split(",");
                        String sysStatus = ss[0] + ",1," + ss[1] + ",1,1,0,0,0";
                        MyApplication.getInstance().setSystemStatus(sysStatus);
                        // 附加柜信息
                        if (iVSICallback2ViewWeakReference != null && iVSICallback2ViewWeakReference.get() != null) {
                            iVSICallback2ViewWeakReference.get().MsgCallback(s);
                        }
                    } else if ("0079".equals(sub)) {// 故障信息
                        s = s.replace("0079", "");
                        MyApplication.getInstance().setTroubleStatus(s);
//                        Log.i(TAG, "故障信息:" + s);
                        // 添加故障信息存入数据库
                        addFaultInfo(s);
                    } else if ("007B".equals(sub)) {// 主机是否缺货信息
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
                        // info[0] mCanSale ; info[1] mIsDoorOpen ; info[2] 箱号 ; info[3] 货道号 ; info[4] 价格 ; info[5] 投币金额

                        s = s.replace("007617", "");
//                        Log.e(TAG, "轮询信息:" + s);
                        String[] info = s.split(",");
                        //门关闭，处于正常销售状态
                        if ("1".equals(doorStatus) && "0".equals(info[1])) {
                            getRoadEmptyInfo();
                        }
                        doorStatus = info[1];
                        //货道号不为0，说明用户点击了售货机按钮
                        if (!"".equals(info[3]) && !"0".equals(info[3])) {
                            // 用户按了商品按钮
                            MyApplication.getInstance().getLogBuyAndShip().d("==================购买流程==================");
                            MyApplication.getInstance().getLogBuyAndShip().d("售货机按键选择商品 = "+"箱号 = "+info[2]+" ; 货道号 = "+info[3]+" ; 价格 : "+info[4]);
//                            Log.e(TAG, "用户选择了:" + info[3]);
                            // 取得商品code
                            GoodsInfo goodsInfo = realm.where(GoodsInfo.class).equalTo("goodsBelong", "1").equalTo("road_no", Integer.parseInt(info[3])).findFirst();
                            if (goodsInfo != null && !"".equals(goodsInfo.getGoodsCode())) {
                                MyApplication.getInstance().getLogBuyAndShip().d("找到按键对应的商品 = "+goodsInfo.getGoodsName()+" "+goodsInfo.getGoodsCode());
                                selectGoodByButton(info[3], goodsInfo.getGoodsCode());
                            }else {
                                MyApplication.getInstance().getLogBuyAndShip().d("没有找到商品 = "+"箱号 = "+info[2]+" ; 货道号 = "+info[3]);
                            }
                            //如果用户投币的话，显示投币数
                            showCashCount(Integer.parseInt(info[5]) / 10);
                        } else {
                            if (!"".equals(info[5]) && !"0".equals(info[5])) {
                                showCashCount(Integer.parseInt(info[5]) / 10);
                            }
                        }
                    } else if ("007X".equals(sub)) {// 各个副柜、格子柜货道是否有货信息，和7B是同一个指令
                        long now = new Date().getTime();
                        s = s.replace("007X", "");
                        String[] info = s.split(";");
                        //箱号
                        final int boxIndex = Integer.parseInt(info[0]);
                        if (nowBoxIndex == boxIndex) {
                            //防止短时间内重复执行库存检查
                            if (now - geziEmptyGetTime < 1500) {
                                break;
                            }
                        }
                        nowBoxIndex = boxIndex;
                        geziEmptyGetTime = now;
                        //替换0和1，0表示有货，1表示无货
                        String roadInfo = info[1].replace("1", "5").replace("0", "1").replace("5", "0");
//                        Log.i(TAG, "格子柜(箱号:" + boxIndex + ")货道信息:" + roadInfo);
//                        //TODO:增加副柜的缺货处理
//                        if (boxIndex == 1){
//                            handleDeskKucun(roadInfo);
//                        }
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

                    } else if ("0080".equals(sub)) { //  副柜弹簧机料道配置信息
                        try {
                            s = s.replace("0080", "");
                            String[] info = s.split(";");

                            MyApplication.getInstance().setDeskRoadCount(Integer.parseInt(info[1]));
                            MyApplication.getInstance().setDeskConnState(true);

                            String roadInfo = "";
                            if (info.length > 2) {
//                                Log.e(TAG, "弹簧机有效货道信息:" + info[2]);
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
//                            Log.e(TAG, "格子柜1货道数:" + info[1]);
                            MyApplication.getInstance().getGeziRoadCount().put(boxIndex, Integer.parseInt(info[1]));
                            String roadInfo = "";
                            if (info.length > 2) {
//                                Log.e(TAG, "格子柜有效货道信息:" + info[2]);
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

    private void handleDeskKucun(String infoString){
        String[] roadState = infoString.split(",");
        int roadCount;
        int count = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0;i < 6;i++){
            for (int j = 0;j < 8;j++){
                roadCount = (i+1)*10+(j+1);
                //此货到有效
                if (MyApplication.getInstance().getDeskRoadList().contains(roadCount)){
                    //此货道缺货
                    if (roadState[count].equals("1")){
                        stringBuilder.append(roadCount+";");
                    }
                }
                count++;
            }
        }
        String roadQuehuo = stringBuilder.toString();
        roadQuehuo = roadQuehuo.substring(0,roadQuehuo.length()-1);
        MyApplication.getInstance().getLogBuHuo().d("副柜缺货信息 = 货道号 : "+roadQuehuo);
    }

    private void checkDeskQueHuo(String machineSn,int roadNum){
        GoodsInfo goodsInfo = MyApplication.getInstance().getDeskGoodsInfo().get(roadNum);
        MyApplication.getInstance().getLogBuyAndShip().d("检查副柜是否缺货");
        if (goodsInfo != null){
            //说明缺货
            if (Integer.parseInt(goodsInfo.getKuCun()) <= 0){
                MyApplication.getInstance().getLogBuyAndShip().d("检查副柜是否缺货 = 找到出货货道商品 = 副柜缺货 货道号 : "+roadNum);
                machineQuehuo(machineSn,roadNum+"");
            }
        }
    }

    /**
     * 收到VMC发来的主机缺货信息后进行处理
     * @param s
     */
    private void doEmptyControl(String s) {
//        MyApplication.getInstance().getLogBuyAndShip().d("处理主机的库存和缺货判定");
        //取反
        s = s.replace("1", "5").replace("0", "1").replace("5", "0");

//        Log.i(TAG, "货道信息HAH:" + s);
//        //TODO:副柜缺货信息
//        if (MyApplication.getInstance().getBindDeskList().size()>0){
//            //如果有副柜的话，请求VMC报告副柜缺货情况
//            comVSI.checkThingsHaveOrNot(1);
//        }
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
//        L.v(SysConfig.ZPush, "2-------->" + s);
        //设置Application中表示主机货道是否缺货的全局变量
        MyApplication.getInstance().setSellEmptyInfo(s);
        if (!isNeedInitMachineInfo) {
            handleGoodsKuCun();
            // 更新商品展示页面库存信息，因为有的商品可能缺货了
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateGoodsInfo();
                }
            }, 1000);
        }
//        Log.i(TAG, "货道售空情况:" + s);
        // 判断是否缺货,缺货的话提交服务器，只是保存到数据库，由Service定时上传到服务器
        try {
            //获取主机各货道是否缺货信息
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
            //TODO:主机如果不缺货，roads为空，也会添加到缺货数据库?这里进行一个判断，不缺货就不添加
            if (!roads.equals("")){
                machineQuehuo(MyApplication.getInstance().getMachine_sn(), roads);
            }
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
        //将连接失败的箱号存入列表
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
        MyApplication.getInstance().getLogBasicCom().d("PARSE 连接失败的格子柜箱号 = "+MyApplication.getInstance().getConnetFailGeziList());
        MyApplication.getInstance().getLogBasicCom().d("PARSE 副柜是否连接 = "+MyApplication.getInstance().getDeskConnState());
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
     * 处理机器缺货，将缺货信息写入数据库，被Service上传到服务器
     * @param machineSn 机器编码
     * @param roads 缺货的货道号
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
        MyApplication.getInstance().getLogBuyAndShip().d("添加缺货信息 = 机器编号 : "+machineSn+" ; 货道号 : "+roads);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(queHuoRecord);
            }
        });
    }
}
