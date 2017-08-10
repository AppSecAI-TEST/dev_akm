package com.zongsheng.drink.h17.presenter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Response;
import com.zongsheng.drink.h17.ComActivity;
import com.zongsheng.drink.h17.Model.BuyActivityModelImpl;
import com.zongsheng.drink.h17.Model.IPayInfoModel;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.background.bean.BindDesk;
import com.zongsheng.drink.h17.background.bean.BindGeZi;
import com.zongsheng.drink.h17.base.BasePresenter;
import com.zongsheng.drink.h17.common.Constant;
import com.zongsheng.drink.h17.common.JsonControl;
import com.zongsheng.drink.h17.common.L;
import com.zongsheng.drink.h17.common.NetWorkRequImpl;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.front.bean.PayModel;
import com.zongsheng.drink.h17.front.bean.ShipStatusModel;
import com.zongsheng.drink.h17.front.bean.ShipmentModel;
import com.zongsheng.drink.h17.front.common.ShowBuyPageListener;
import com.zongsheng.drink.h17.front.popwindow.BuyGoodsPopWindow;
import com.zongsheng.drink.h17.interfaces.IBuyGoodsPopWindowView;
import com.zongsheng.drink.h17.interfaces.IBuyActivityInterface;
import com.zongsheng.drink.h17.interfaces.INetWorkRequInterface;
import com.zongsheng.drink.h17.service.ClientConnectMQ;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.RealmResults;
import io.realm.Sort;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.yolanda.nohttp.RequestMethod.HEAD;


/**
 * Created by Suchengjian on 2017.2.16.
 */

public class BuyActivityPresenterImpl extends BasePresenter<IBuyActivityInterface> implements IBuyActivityPresenter {

    private IPayInfoModel iPayInfoModel = new BuyActivityModelImpl();
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SysConfig.TIME_FORMAT_S);
    private IBuyActivityInterface iBuyActivityInterface;
    private INetWorkRequInterface iNetWorkRequInterface = null;
    private Context context;
    private boolean flag = true;
    private IBuyGoodsPopWindowView iBuyGoodsPopWindowView;
    private ShowBuyPageListener showBuyPageListener;

    private boolean isSaleing = false;

    public BuyActivityPresenterImpl(IBuyActivityInterface iBuyActivityInterface) {
        this.context = (Context) iBuyActivityInterface;
        bindView(iBuyActivityInterface);
    }

    @Override
    public void receiverMQMsg(String msg) {
//        Log.e("here", msg);
        try {
            final JSONObject jsonObject = new JSONObject(msg);
            if ("".equals(jsonObject.getString("goods_id"))) {
                return;
            }
            if (SysConfig.SHIPMENTTYPE.equals(jsonObject.getString("operationtype"))) {
                ShipmentModel shipmentModel = new ShipmentModel();
                shipmentModel.setGoods_belong(jsonObject.getString("goods_belong"));
                shipmentModel.setOrder_sn(jsonObject.getString("order_sn"));
                shipmentModel.setGoods_id(jsonObject.getString("goods_id"));
                shipmentModel.setPush_machine_sn(jsonObject.getString("push_machine_sn"));
                shipmentModel.setGoods_price(jsonObject.getString("goods_price"));
                shipmentModel.setPay_time(jsonObject.getString("pay_time"));
                shipmentModel.setTrade_no(jsonObject.getString("trade_no"));
                shipmentModel.setPay_type(jsonObject.getString(Constant.PAY_TYPE));
                shipmentModel.setRoad_no(jsonObject.getString("road_num"));
                iPayInfoModel.addMQReceiver(shipmentModel);
                MyApplication.getInstance().getLogBuyAndShip().d("收到网络支付信息 = "+msg);
                if (!isOrderSn(shipmentModel.getOrder_sn())) {
                    ShipStatusModel shipStatusModel = new ShipStatusModel();
                    shipStatusModel.setOrderSn(shipmentModel.getOrder_sn());
                    shipStatusModel.setMachineTime(simpleDateFormat.format(new Date()));
                    if (canShipment(shipmentModel.getOrder_sn())) {
//                        L.v(SysConfig.ZPush, "回调出货");
                        //回调出货
                        MyApplication.getInstance().getLogBuyAndShip().d("开始出货");
                        onlinePayShipment(shipmentModel);
                        shipStatusModel.setShipStatus(SysConfig.SHIPSTATUS_SHIP);
                    } else {
                        //超时退款
//                        L.v(SysConfig.ZPush, "退款操作");
                        MyApplication.getInstance().getLogBuyAndShip().d("网络支付超时退款");
                        refundRequest(shipmentModel);
                        shipStatusModel.setShipStatus(SysConfig.SHIPSTATUS_REFUND);
                    }
                    ClientConnectMQ.getInstance().sendShipStatus2MQ(JsonControl.ShipStatusModel2Json(shipStatusModel));
                }
            } else {
                //iPayInfoModel.updatePayModels(jsonObject.getString("order_sn"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onlinePayShipment(ShipmentModel shipmentModel) {
        saleWithoutCash(shipmentModel);
    }

    /**
     * 从最近的10条销售记录的缓存中读取
     *
     * @param orderSn
     * @return
     */
    private boolean isOrderSn(String orderSn) {
        for (String odersn : MyApplication.getInstance().getOrderSns()) {
            if (odersn.equals(orderSn)) {
                return true;
            }
        }
        if (MyApplication.getInstance().getOrderSns().size() >= SysConfig.ORDERSN_CACHE_SIZE) {
            MyApplication.getInstance().getOrderSns().remove(0);
        }
        MyApplication.getInstance().getOrderSns().add(orderSn);
        return false;
    }


    @Override
    public void refundRequest(ShipmentModel shipmentModel) {
        if (shipmentModel != null) {
            String automaticRefundState = MyApplication.getInstance().getAutomaticRefundState();
            //如果启用了自动退款
            if (!automaticRefundState.equals(Constant.AUTOREFUNDSTATE_NOUSED)) {
                if (iNetWorkRequInterface == null) {
                    iNetWorkRequInterface = new NetWorkRequImpl(this);
                }
                //如果对所有支付方式都启用了自动退款
                if (automaticRefundState.equals(Constant.AUTOREFUNDSTATE_ALLUSED)) {
                    //支付宝
                    if (Constant.CALLBACK_PAYTYPE_ALI.equals(shipmentModel.getPay_type())) {
                        String request = SysConfig.ALIBABA_REFUND_URL + "order_sn=" + shipmentModel.getOrder_sn() + "&total_fee=" + shipmentModel.getGoods_price() + "&trade_no=" + shipmentModel.getTrade_no() + "&machine_sn=" + MyApplication.getInstance().getMachine_sn();
                        iNetWorkRequInterface.request(request, 0, RequestMethod.GET);
                        MyApplication.getInstance().getLogBuyAndShip().d("发送支付宝退款请求 = "+request);
                    } else if (Constant.CALLBACK_PAYTYPE_WECHAT.equals(shipmentModel.getPay_type())) {
                        //微信
                        String request = SysConfig.WECHAT_REFUND_URL + "order_sn=" + shipmentModel.getOrder_sn() + "&total_fee=" + shipmentModel.getGoods_price() + "&machine_sn=" + MyApplication.getInstance().getMachine_sn();
                        iNetWorkRequInterface.request(request, 0, RequestMethod.GET);
                        MyApplication.getInstance().getLogBuyAndShip().d("发送微信退款请求 = "+request);
                    }
                } else if (automaticRefundState.equals(Constant.AUTOREFUNDSTATE_ALIPAY) && Constant.CALLBACK_PAYTYPE_ALI.equals(shipmentModel.getPay_type())) {
                    String request = SysConfig.ALIBABA_REFUND_URL + "order_sn=" + shipmentModel.getOrder_sn() + "&total_fee=" + shipmentModel.getGoods_price() + "&trade_no=" + shipmentModel.getTrade_no() + "&machine_sn=" + MyApplication.getInstance().getMachine_sn();
                    MyApplication.getInstance().getLogBuyAndShip().d("发送支付宝退款请求 = "+request);
                    iNetWorkRequInterface.request(request, 0, RequestMethod.GET);
                } else if (automaticRefundState.equals(Constant.AUTOREFUNDSTATE_WETCH) && Constant.CALLBACK_PAYTYPE_WECHAT.equals(shipmentModel.getPay_type())) {
                    String request = SysConfig.WECHAT_REFUND_URL + "order_sn=" + shipmentModel.getOrder_sn() + "&total_fee=" + shipmentModel.getGoods_price() + "&machine_sn=" + MyApplication.getInstance().getMachine_sn();
                    MyApplication.getInstance().getLogBuyAndShip().d("发送微信退款请求 = "+request);
                    iNetWorkRequInterface.request(request, 0, RequestMethod.GET);
                }
            }

            //TODO:京东支付，暂时处理
            if ("3".equals(shipmentModel.getPay_type())) {
                if (iNetWorkRequInterface == null) {
                    iNetWorkRequInterface = new NetWorkRequImpl(this);
                }
//                Log.d("test", SysConfig.JINGDONG_REFUND_URL + "order_sn=" + shipmentModel.getOrder_sn() + "&total_fee=" + shipmentModel.getGoods_price() + "&machine_sn=" + MyApplication.getInstance().getMachine_sn());
                String request = SysConfig.JINGDONG_REFUND_URL + "order_sn=" + shipmentModel.getOrder_sn() + "&total_fee=" + shipmentModel.getGoods_price() + "&machine_sn=" + MyApplication.getInstance().getMachine_sn();
                MyApplication.getInstance().getLogBuyAndShip().d("发送微信退款请求 = "+request);
                iNetWorkRequInterface.request(request, 0, RequestMethod.GET);
            }
        }
    }

    @Override
    public void bindPopView(IBuyGoodsPopWindowView buyGoodsPopWindowView) {
        this.iBuyGoodsPopWindowView = buyGoodsPopWindowView;
    }

    @Override
    public void setVersionNum() {
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/token/post/machine/" + MyApplication.getInstance().getMachine_sn() + "?versionNum=" + String.valueOf(getAppVersionName());
        if (iNetWorkRequInterface == null) {
            iNetWorkRequInterface = new NetWorkRequImpl(this);
        }
        iNetWorkRequInterface.request(url, 0, RequestMethod.GET);
    }

    @Override
    public void updateLocalKuncun(String road_no) {
        iPayInfoModel.updateLocalKuncun(road_no);
    }

    @Override
    public void shipmentSuccess(String huodao, String goodsCode, String goodsCount,
                                String machineQueryType, String saleOrderID, int boxIndex, String saleTime, String trade_no) {
        try {
            isSaleing = false;
//            Log.e(TAG, "出货成功!: goodsCode:" + goodsCode + " machineQueryType:" + machineQueryType);
            MyApplication.getInstance().getLogBuyAndShip().d("出货成功 = 订单号 : "+saleOrderID+" ; 箱号 : "+boxIndex+" ; 货道号 : "+trade_no);
//            MyApplication.getInstance().getLogBuyAndShip().d("");
            if (boxIndex == 0) { // 主柜
                for (GoodsInfo goodsInfo : MyApplication.getInstance().getGoodsInfos()) {
                    if (goodsCode.equals(goodsInfo.getGoodsCode())) {
                        // 现金出货成功
                        if (iBuyGoodsPopWindowView != null) {
                            iBuyGoodsPopWindowView.setGoodsInfo(goodsInfo, "");
                            iBuyGoodsPopWindowView.shipmentSuccessByCash();
                        }
                        break;
                    }
                }
            } else if (boxIndex == 1) {//副柜出货成功
                for (GoodsInfo goodsInfo : MyApplication.getInstance().getDeskGoodsInfo().values()) {
                    if (goodsCode.equals(goodsInfo.getGoodsCode())) {
                        if (iBuyGoodsPopWindowView != null) {
                            iBuyGoodsPopWindowView.setGoodsInfo(goodsInfo, "");
                            iBuyGoodsPopWindowView.shipmentSuccessByCash();
                        }
                    }
                }
            } else { // 格子柜出货
                for (GoodsInfo goodsInfo : MyApplication.getInstance().getCabinetGoods()) {
                    if (goodsCode.equals(goodsInfo.getGoodsCode())) {
                        // 现金出货成功
                        if (iBuyGoodsPopWindowView != null) {
                            iBuyGoodsPopWindowView.setGoodsInfo(goodsInfo, "");
                            iBuyGoodsPopWindowView.shipmentSuccessByCash();
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void bindListener(ShowBuyPageListener showBuyPageListener) {
        this.showBuyPageListener = showBuyPageListener;
    }

    @Override
    public void saleWithoutCash(final ShipmentModel shipmentModel) {
        //TODO:这些失败情况都没有退款操作
        if ("1".equals(shipmentModel.getGoods_belong())) { // 1:主机 2:格子柜 3:副柜
            if (!shipmentModel.getPush_machine_sn().equals(MyApplication.getInstance().getMachine_sn())) {
                MyApplication.getInstance().getLogBuyAndShip().d("主机出货失败 推送的支付信息机器编码和主机器编码不一致 = "+shipmentModel.getGoods_belong()+" : "+MyApplication.getInstance().getMachine_sn());
                return;
            }
        } else if ("2".equals(shipmentModel.getGoods_belong())) {
            boolean hasMachine = false;
            for (BindGeZi bindGeZi : MyApplication.getInstance().getBindGeZis()) {
                if (bindGeZi.getMachineSn().equals(shipmentModel.getPush_machine_sn())) {
                    hasMachine = true;
                    break;
                }
            }
            if (!hasMachine) {
                MyApplication.getInstance().getLogBuyAndShip().d("格子柜出货失败 没有找到符合该支付信息机器编码的格子柜 = 推送的机器编码 : "+shipmentModel.getGoods_belong());
                return;
            }
        } else {//是否是副柜中商品
            boolean haveDeskMachine = false;
            for (BindDesk bindDesk : MyApplication.getInstance().getBindDeskList()) {
                if (bindDesk.getMachineSn().equals(shipmentModel.getPush_machine_sn())) {
                    haveDeskMachine = true;
                    break;
                }
            }
            if (!haveDeskMachine){
                MyApplication.getInstance().getLogBuyAndShip().d("副柜出货失败 推送的支付信息机器编码和副柜机器编码不一致 = "+shipmentModel.getGoods_belong()+" : "+MyApplication.getInstance().getBindDeskList().get(0).getMachineSn());
                return;
            }
        }
        // 判断数据里是否已经存在该订单
        RealmResults<PayModel> result2 = iPayInfoModel.getPayModel4Realm(shipmentModel.getOrder_sn());
        if (result2.size() == 0) {
            //不存在
            if ("1".equals(shipmentModel.getGoods_belong())) { // 主机
                // 购买页面显示出货信息
                for (GoodsInfo goodsInfo : MyApplication.getInstance().getGoodsInfos()) {
                    if (shipmentModel.getGoods_id().equals(goodsInfo.getGoodsCode())) {
                        MyApplication.getInstance().getLogBuyAndShip().d("主机出货 找到匹配推送支付信息的商品 = 商品号 : " + goodsInfo.getGoodsCode() + " ; 商品名 : " + goodsInfo.getGoodsName()+" ; 货道号 : "+goodsInfo.getRoad_no());
                        // 非现金支付
                        if (iBuyGoodsPopWindowView != null) {
                            iBuyGoodsPopWindowView.setGoodsInfo(goodsInfo, "1");
                            iBuyGoodsPopWindowView.shipmentSuccessByNet();
                        }
                        break;
                    }
                }
            } else if ("2".equals(shipmentModel.getGoods_belong())) { // 格子柜数据
                // 购买页面显示出货信息
                for (GoodsInfo goodsInfo : MyApplication.getInstance().getCabinetGoods()) {
                    if (shipmentModel.getGoods_id().equals(goodsInfo.getGoodsCode())) {
                        MyApplication.getInstance().getLogBuyAndShip().d("格子柜 找到匹配推送支付信息的商品 = 商品号 : " + goodsInfo.getGoodsCode() + " ; 商品名 : " + goodsInfo.getGoodsName()+" ; 货道号 : "+goodsInfo.getRoad_no()+" ; 机器编码 : "+goodsInfo.getGoodsBelong());
                        // 非现金支付
                        if (iBuyGoodsPopWindowView != null) {
                            iBuyGoodsPopWindowView.setGoodsInfo(goodsInfo, "1");
                            iBuyGoodsPopWindowView.shipmentSuccessByNet();
                        }
                        break;
                    }
                }
            } else {
//                Log.e("副柜", "here");
                for (GoodsInfo goodsInfo : MyApplication.getInstance().getDeskGoodsInfo().values()) {
                    if (shipmentModel.getGoods_id().equals(goodsInfo.getGoodsCode())) {
                        MyApplication.getInstance().getLogBuyAndShip().d("副柜 找到匹配推送支付信息的商品 = 商品号 : " + goodsInfo.getGoodsCode() + " ; 商品名 : " + goodsInfo.getGoodsName()+" ; 货道号 : "+goodsInfo.getRoad_no()+" ; 机器编码 : "+goodsInfo.getGoodsBelong());
                        // 非现金支付
                        if (iBuyGoodsPopWindowView != null) {
                            iBuyGoodsPopWindowView.setGoodsInfo(goodsInfo, "1");
                            iBuyGoodsPopWindowView.shipmentSuccessByNet();
                        }
                        break;
                    }
                }
            }

            final long time = new Date().getTime();
            if ("1".equals(shipmentModel.getGoods_belong())) { // 主机
                if (isSaleing) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isSaleing = true;
                            // 如果两次出货时间太紧的话,延时出货
                            if (((ComActivity) iBuyActivityInterface).lastSaleTime != 0 && time - ((ComActivity) iBuyActivityInterface).lastSaleTime < 1000) {
                                MyApplication.getInstance().getLogBuyAndShip().d("两次出货时间太紧，延迟1秒出货");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((ComActivity) iBuyActivityInterface).sellByNoCash(Integer.parseInt(shipmentModel.getGoods_id()), shipmentModel.getOrder_sn(), shipmentModel.getGoods_price());
                                        ((ComActivity) iBuyActivityInterface).lastSaleTime = new Date().getTime();
                                    }
                                }, 1000 - (time - ((ComActivity) iBuyActivityInterface).lastSaleTime));
                            } else {
                                ((ComActivity) iBuyActivityInterface).sellByNoCash(Integer.parseInt(shipmentModel.getGoods_id()), shipmentModel.getOrder_sn(), shipmentModel.getGoods_price());
                                ((ComActivity) iBuyActivityInterface).lastSaleTime = time;
                            }
                        }
                    }, 1000);
                } else {
                    isSaleing = true;
                    // 如果两次出货时间太紧的话,延时出货
                    if (((ComActivity) iBuyActivityInterface).lastSaleTime != 0 && time - ((ComActivity) iBuyActivityInterface).lastSaleTime < 1000) {
                        MyApplication.getInstance().getLogBuyAndShip().d("两次出货时间太紧，延迟1秒出货");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ((ComActivity) iBuyActivityInterface).sellByNoCash(Integer.parseInt(shipmentModel.getGoods_id()), shipmentModel.getOrder_sn(), shipmentModel.getGoods_price());
                                ((ComActivity) iBuyActivityInterface).lastSaleTime = new Date().getTime();
                            }
                        }, 1000 - (time - ((ComActivity) iBuyActivityInterface).lastSaleTime));
                    } else {
                        ((ComActivity) iBuyActivityInterface).sellByNoCash(Integer.parseInt(shipmentModel.getGoods_id()), shipmentModel.getOrder_sn(), shipmentModel.getGoods_price());
                        ((ComActivity) iBuyActivityInterface).lastSaleTime = time;
                    }
                }
            } else if ("2".equals(shipmentModel.getGoods_belong())) { // 格子柜
                if (((ComActivity) iBuyActivityInterface).lastSaleTime != 0 && time - ((ComActivity) iBuyActivityInterface).lastSaleTime < 1000) {
                    MyApplication.getInstance().getLogBuyAndShip().d("两次出货时间太紧，延迟1秒出货");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            costMoneyForCabinet(shipmentModel, 0, true);
                            ((ComActivity) iBuyActivityInterface).lastSaleTime = new Date().getTime();
                        }
                    }, 1000 - (time - ((ComActivity) iBuyActivityInterface).lastSaleTime));
                } else {
                    costMoneyForCabinet(shipmentModel, 0, true);
                    ((ComActivity) iBuyActivityInterface).lastSaleTime = time;
                }
            } else {// 副柜
                if (((ComActivity) iBuyActivityInterface).lastSaleTime != 0 && (time - ((ComActivity) iBuyActivityInterface).lastSaleTime) < 1000) {
                    MyApplication.getInstance().getLogBuyAndShip().d("两次出货时间太紧，延迟1秒出货");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            saleNocashForDesk(shipmentModel);
                            ((ComActivity) iBuyActivityInterface).lastSaleTime = new Date().getTime();
                        }
                    }, 1000 - (time - ((ComActivity) iBuyActivityInterface).lastSaleTime));
                } else {
                    saleNocashForDesk(shipmentModel);
                    ((ComActivity) iBuyActivityInterface).lastSaleTime = new Date().getTime();
                }
            }
            iBuyActivityInterface.closeAd(true);
        }
    }

    @Override
    public void saleNocashForDesk(ShipmentModel shipmentModel) {
        ((ComActivity) iBuyActivityInterface).fuguiSaleTestByNoCash(Integer.parseInt(shipmentModel.getGoods_id()),
                shipmentModel.getOrder_sn(), Integer.parseInt(shipmentModel.getRoad_no()));
    }

    @Override
    public void costMoneyForCabinet(final ShipmentModel shipmentModel, int money, boolean isOnLine) {
        String index;
        // 货道
        int roadNo = 0;
        // 格子柜箱号
        int i = 1;
        // 格子柜机器编码和箱号的映射
        Map<String, Integer> bindGeziMap = new HashMap<>();
        int j = 1;
        //注意映射和格子柜的添加时间顺序有关
        for (BindGeZi bindGeZi : MyApplication.getInstance().getBindGeZis()) {
            bindGeziMap.put(bindGeZi.getMachineSn(), j + 1);
            j++;
        }
        // 搜索出有货的格子柜
        for (GoodsInfo goodsInfo : MyApplication.getInstance().getCabinetTotalGoods()) {
            if (goodsInfo.getMachineID() == null || goodsInfo.getRoad_no() == 0) {
                continue;
            }
            // 无此货道的时候 continue
            if (MyApplication.getInstance().getGeziRoadListMap().containsKey(bindGeziMap.get(goodsInfo.getMachineID()))) {
                if (!MyApplication.getInstance().getGeziRoadListMap().get(bindGeziMap.get(goodsInfo.getMachineID())).contains(goodsInfo.getRoad_no())) {
                    continue;
                }
            }
            //找到该种商品
            if (shipmentModel.getGoods_id().equals(goodsInfo.getGoodsCode())) {
                // 是否售空 0:未空 1:售空
                // 这里直接用货道号是不对的，getAokemaGeZiKuCunMap里面对应的货道号是1~80，而这里是真实的货道号，需要进行对应处理 1,2,3,4,5 11,12,13,14,15 ...
                int road_no = goodsInfo.getRoad_no();
                int roadIndex;
                if (road_no > 10) {
                    roadIndex = road_no - (((int) (road_no * 0.1)) * 2);
                } else {
                    roadIndex = road_no;
                }
                if (MyApplication.getInstance().getAokemaGeZiKuCunMap().containsKey(bindGeziMap.get(goodsInfo.getMachineID()))) {
                    if (MyApplication.getInstance().getAokemaGeZiKuCunMap().get(bindGeziMap.get(goodsInfo.getMachineID())).containsKey(roadIndex)) {
                        if ("0".equals(MyApplication.getInstance().getAokemaGeZiKuCunMap().get(bindGeziMap.get(goodsInfo.getMachineID())).get(roadIndex))) {
                            // 有货的时候
                            roadNo = goodsInfo.getRoad_no();
                            i = bindGeziMap.get(goodsInfo.getMachineID());
//                            Log.e(TAG, "找到货物了:" + goodsInfo.getMachineID() + ";" + road_no + ";" + goodsInfo.getGoodsName() + "；" + i);
                            MyApplication.getInstance().getLogBuyAndShip().d("找到了要出货的商品 = 箱号 : "+i+" ; 货道号 : "+goodsInfo.getRoad_no()+" ; 商品名 : "+goodsInfo.getGoodsName());
                            break;
                        }
                    }
                }
            }
        }
        index = String.valueOf(i);
        if ("".equals(index) || 0 == roadNo) {
            MyApplication.getInstance().getLogBuyAndShip().d("没有找到对应的商品");
//            Log.e(TAG, "没有找到可销售的货物");
            if (isOnLine) {
                // 更新订单出货失败
                PayModel payModel = ((ComActivity) iBuyActivityInterface).getBasePayModel(shipmentModel);
                iPayInfoModel.addPayModel2Realm(payModel);
                refundRequest(shipmentModel);
            }
            return;
        }
        final String geziIndex = index;
        final int road_no = roadNo;

        long time = new Date().getTime();
        if (((ComActivity) iBuyActivityInterface).lastSaleTime != 0 && time - ((ComActivity) iBuyActivityInterface).lastSaleTime < 1000) {
            SystemClock.sleep(1000 - (time - ((ComActivity) iBuyActivityInterface).lastSaleTime));
        }
        ((ComActivity) iBuyActivityInterface).lastSaleTime = time;
        if (!isOnLine) { // 现金支付
            if (isSaleing) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isSaleing = true;
                        // 扣币请求
                        ((ComActivity) iBuyActivityInterface).gezi_sale_aokemaByCash(Integer.parseInt(geziIndex), road_no, (int) Double.parseDouble(shipmentModel.getGoods_price()));
                    }
                }, 1000);
            } else {
                isSaleing = true;
                // 扣币请求
                ((ComActivity) iBuyActivityInterface).gezi_sale_aokemaByCash(Integer.parseInt(geziIndex), road_no, (int) Double.parseDouble(shipmentModel.getGoods_price()));
            }
        } else { // 在线支付
            // 非现金出货
            if (isSaleing) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isSaleing = true;
                        ((ComActivity) iBuyActivityInterface).gezi_sale_aokemaByNoCash(Integer.parseInt(geziIndex), road_no, shipmentModel.getOrder_sn(), shipmentModel.getGoods_price());
                    }
                }, 1000);
            } else {
                isSaleing = true;
                ((ComActivity) iBuyActivityInterface).gezi_sale_aokemaByNoCash(Integer.parseInt(geziIndex), road_no, shipmentModel.getOrder_sn(), shipmentModel.getGoods_price());
            }
        }
    }

    /**
     * 现金出货
     * @param goodsCode
     */
    @Override
    public void saleByCash(final int goodsCode) {
        if (isSaleing) {
            MyApplication.getInstance().getLogBuyAndShip().d("当前占用，延迟1秒发送出货指令");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isSaleing = true;
                    ((ComActivity) iBuyActivityInterface).sellByCash(goodsCode);
                }
            }, 1000);
        } else {
            isSaleing = true;
            ((ComActivity) iBuyActivityInterface).sellByCash(goodsCode);
        }
    }

    @Override
    public void onlinePaySend2MQ(PayModel payModel, String orderSn, String payType, String DeliveryStatus) {
        //在线支付payTime服务器不以机器端的为准
        iPayInfoModel.addPayModel2Realm(payModel, orderSn, iBuyActivityInterface.swtchPayType(payType), DeliveryStatus);
    }

    @Override
    public void machineQueB(String machineID, String fiveStatus, String oneStatus) {
        iPayInfoModel.machineQueB(machineID, fiveStatus, oneStatus);
    }


    /**
     * 获取当前版本号
     */
    private int getAppVersionName() {
        int versionName = 0;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            versionName = packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    @Override
    public void onSucceed(int what, Response<String> response) {

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
                            // 如果成功
                            if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                // DO NOTHING
                            } else {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        setVersionNum();
                                    }
                                }, SysConfig.L_REQ_AG_TIME_60);
                            }
                        } catch (Exception e) {
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
    public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {
        if (what == 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setVersionNum();
                }
            }, SysConfig.L_REQ_AG_TIME_60);
        }
    }


    /**
     * 订单中时间戳的格式为yyyyMMddHHmmss
     * 判断支付时长是否合理，90秒
     * TODO:这里的流程可能有问题，90秒后可能支付窗口已经关闭
     */
    private boolean canShipment(String ordersn) {
        String time = ordersn.substring(ordersn.length() - 14, ordersn.length());
        boolean res = false;
        try {
            Date paydate = simpleDateFormat.parse(time);
            //当前时间 减 订单生成的时间
            long ss = (new Date().getTime() - paydate.getTime()) / 1000;
            res = ss < SysConfig.SHIPMENT_TIME_LIMIT;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    protected void bindView(IBuyActivityInterface IView) {
        this.iBuyActivityInterface = IView;
    }

    @Override
    public void initData() {
        flag = true;
        new Thread() {
            @Override
            public void run() {
                super.run();
                Message msg = new Message();
                boolean tmp = ping();
                if (tmp) {
                    //网络连接正常
                    msg.what = 0;
                } else {
                    msg.what = 1;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    @Override
    public void close() {
        flag = true;
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    //网络连接正常
                    if (iBuyGoodsPopWindowView != null && flag) {
                        iBuyGoodsPopWindowView.NetWorkSuccess();
                    }
                    break;
                case 1:
                    if (iBuyGoodsPopWindowView != null && flag) {
                        iBuyGoodsPopWindowView.NetWorkError();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    //判断网络连接是否正常
    private boolean ping() {
        String result = null;
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("ping -c 1 -w 5000 " + SysConfig.PING_ADDRESS);//ping1次，超时时间为5s
            // PING的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "successful";
                return true;
            } else {
                result = "failed cannot reach the IP address";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assert p != null;
            p.destroy();
//            L.i("BuyActivity", "result = " + result);
            MyApplication.getInstance().getLogBuyAndShip().d("网络连接状态 = "+result);
        }
        return false;
    }

    @Override
    public void cancel() {
        if (iNetWorkRequInterface != null) {
            iNetWorkRequInterface.cancel();
            iNetWorkRequInterface = null;
        }
        if (iPayInfoModel != null) {
            iPayInfoModel.cancel();
            iPayInfoModel = null;
        }
    }
}
