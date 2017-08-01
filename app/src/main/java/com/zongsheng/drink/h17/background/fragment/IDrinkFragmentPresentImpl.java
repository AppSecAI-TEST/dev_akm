package com.zongsheng.drink.h17.background.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Response;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.activity.BuhuoActivity;
import com.zongsheng.drink.h17.background.bean.PanDInfo;
import com.zongsheng.drink.h17.background.bean.RoadTemple;
import com.zongsheng.drink.h17.common.Constant;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.LoadingUtil;
import com.zongsheng.drink.h17.common.NetWorkRequImpl;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.common.ToastUtils;
import com.zongsheng.drink.h17.common.popupwindow.ActionItem;
import com.zongsheng.drink.h17.common.popupwindow.TitlePopup;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.interfaces.INetWorkRequInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Suchengjian on 2017.4.19.
 */

public class IDrinkFragmentPresentImpl implements IDrinkFragmentPresent {

    private final int CHEJI = 2;
    private final int GETMUBAN = 1;
    private final int UPLOADMUBAN = 3;
    private final int PANDIAN_WHAT = 4;

    private INetWorkRequInterface iNetWorkRequInterface = new NetWorkRequImpl(this);
    private IDrinkFragmentInterface iDrinkFragmentInterface;
    private AlertView alertView;
    private Context context;

    private TitlePopup morePopup;

    private int currentPage = 0;
    /**
     * 自定义的Dialog
     */

    private Dialog loadingDialogEnd;// 加载完成
    private Dialog loadingDialogFail;// 加载失败

    public IDrinkFragmentPresentImpl(Context context, IDrinkFragmentInterface iDrinkFragmentInterface) {
        this.iDrinkFragmentInterface = iDrinkFragmentInterface;
        this.context = context;
    }

    @Override
    public void submit(final List<GoodsInfo> goodsInfoList, boolean isChanged) {
        // 判断是否有重复的价格不同的商品
        Map<String, String> checkMap = new HashMap<>();
        for (GoodsInfo goodsInfo : goodsInfoList) {
            if (goodsInfo == null || goodsInfo.getGoodsCode() == null || "".equals(goodsInfo.getGoodsCode()) || "0".equals(goodsInfo.getGoodsCode())) {
                continue;
            }
            if (checkMap.containsKey(goodsInfo.getGoodsCode())) {
                if (Double.parseDouble(checkMap.get(goodsInfo.getGoodsCode())) != Double.parseDouble(goodsInfo.getPrice())) {
                    alertView = new AlertView("提示", "同种商品价格必须一致(" + goodsInfo.getGoodsName() + ")",
                            null, new String[]{"确认"}, null, context, AlertView.Style.Alert, DataUtil.dip2px(context, Double.parseDouble(context.getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
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
            } else {
                checkMap.put(goodsInfo.getGoodsCode(), goodsInfo.getPrice());
            }
        }
        if (checkMap.size() == 0) {
            ToastUtils.showToast((Activity) context, "货道未配置");
            return;
        }
        if (!isChanged) {
            alertView = new AlertView("提示", "货道信息没有任何修改", null,
                    new String[]{"确认"}, null, context, AlertView.Style.Alert, DataUtil.dip2px(context,Double.parseDouble(context.getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
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
        for(GoodsInfo goodsInfo : goodsInfoList){
            if (goodsInfo == null || goodsInfo.getGoodsCode() == null || "".equals(goodsInfo.getGoodsCode()) || "0".equals(goodsInfo.getGoodsCode())) {
                continue;
            }
            if(goodsInfo.getMaxKucun() == 0){
                alertView = new AlertView("提示", "最大库存("+goodsInfo.getGoodsName()+")不能为0", null,
                        new String[]{"确认"}, null, context, AlertView.Style.Alert, DataUtil.dip2px(context,Double.parseDouble(context.getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
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
        }
        alertView = new AlertView("提示", "提交会更改主控货道设置且耗时较久\n确定继续吗?",
                "取消", new String[]{"确认"}, null, context, AlertView.Style.Alert, DataUtil.dip2px(context, Double.parseDouble(context.getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (-1 == position) {
                    alertView.dismiss();
                } else {
                    //显示dialog
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ((BuhuoActivity) context).confirmForMainMachine(goodsInfoList);
                        }
                    }, 200);
                }
            }
        }).setCancelable(false).setOnDismissListener(null);
        alertView.show();
    }

    @Override
    public void more(int nwidth) {
        morePopup = new TitlePopup(context, nwidth / Integer.parseInt(context.getResources().getString(R.string.manger_Menu_wigth_percent)), ViewGroup.LayoutParams.WRAP_CONTENT);
        morePopup.addAction(new ActionItem("盘点"));
        morePopup.addAction(new ActionItem("上传模板"));
        morePopup.addAction(new ActionItem("获取模板"));
        morePopup.addAction(new ActionItem("退货撤机"));
        morePopup.addAction(new ActionItem("移机"));
        iDrinkFragmentInterface.PopSetItemClick(morePopup);
    }

    @Override
    public void chejiRequest(String machin_sn, String apply_type) {
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/put/" + MyApplication.getInstance().getMachine_sn() + "/remove/" + apply_type;
        iNetWorkRequInterface.request(url,CHEJI,RequestMethod.GET);
    }

    @Override
    public void requestMoBanFromNet(String machine_sn) {
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/" + machine_sn + "/roadtemplelist";
        iNetWorkRequInterface.request(url,GETMUBAN,RequestMethod.GET);
    }

    @Override
    public void uploadTemplete(String Templetename, List<GoodsInfo> goodsInfoList) {
        StringBuffer temple_roads = new StringBuffer();
        for (GoodsInfo goodsInfo : goodsInfoList) {
            temple_roads.append(goodsInfo.getRoad_no() + "," +
                    goodsInfo.getMaxKucun() + "," +
                    (goodsInfo.getGoodsID() == null ? "" : goodsInfo.getGoodsID()) + "," + (goodsInfo.getPrice() == null || "".equals(goodsInfo.getPrice()) ? 0 : Double.parseDouble(goodsInfo.getPrice())) * 0.1 + ";");
        }
        Log.e("模版上传", "上传模版的货道信息:" + temple_roads.toString());
        // 补货提交
        String url = null;
        try {
            url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/post/template?machineSn=" + MyApplication.getInstance().getMachine_sn() + "&templeName=" + URLEncoder.encode(Templetename, "UTF-8") + "&templeRoads=" +temple_roads;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        iNetWorkRequInterface.request(url,UPLOADMUBAN,RequestMethod.GET);
    }

    @Override
    public void clickPanD(String machine_sn,int currentPage) {
        this.currentPage = currentPage;
        String url = SysConfig.PANDIAN_ADDRESS + "&machineSn=" + machine_sn + "&currPage=" + currentPage + "&pagerSize=" + SysConfig.PAGERSIZE;
        iNetWorkRequInterface.request(url,PANDIAN_WHAT,RequestMethod.POST);
    }

    @Override
    public void onDestory() {
        if(iNetWorkRequInterface != null){
            iNetWorkRequInterface = null;
        }
        if(iDrinkFragmentInterface != null){
            iDrinkFragmentInterface = null;
        }
    }

    @Override
    public void onSucceed(int what, Response<String> response) throws Exception {

        iDrinkFragmentInterface.dialogDismiss();
        int responseCode = response.getHeaders().getResponseCode();// 服务器响应码
        if (responseCode == 200) {
            if (RequestMethod.HEAD != response.getRequestMethod()) {
                // 请求方法为HEAD时没有响应内容
                JSONObject jsonResult = null;
                try {
                    jsonResult = new JSONObject(response.get());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (what) {
                    case 0:
                        try {
                            if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                // 提交成功
                                ToastUtils.showToast((Activity) context, "成功");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case GETMUBAN:
                        try {
                            if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                Gson gson = new Gson();
                                Type type = new TypeToken<ArrayList<RoadTemple>>(){}.getType();
                                List<RoadTemple> roadTemples = gson.fromJson(jsonResult.getString("roadTempleList"), type);
                                iDrinkFragmentInterface.mubannotifyDataSetChanged(roadTemples);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case CHEJI: // 移机结果
                        try {
                            if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                // 处理成功
                                loadingDialogEnd = LoadingUtil.createLoadingDialog(context, "处理成功", 1, R.drawable.ic_success, false);
                                loadingDialogEnd.show();
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        loadingDialogEnd.dismiss();
                                    }
                                }, 1000);
                            } else {
                                ToastUtils.showToast((Activity) context, jsonResult.getString("error"));
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case UPLOADMUBAN: // 上传模版
                        if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                            // 处理成功
                            loadingDialogEnd = LoadingUtil.createLoadingDialog(context, "上传成功", 1, R.drawable.ic_success, false);
                            loadingDialogEnd.show();
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    loadingDialogEnd.dismiss();
                                }
                            }, 1000);
                        } else {
                            // 处理失败
                            loadingDialogFail = LoadingUtil.createLoadingDialog(context, "处理失败", 1, R.drawable.ic_quhuo_fail, false);
                            //显示dialog
                            loadingDialogFail.show();
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    loadingDialogFail.dismiss();
                                }
                            }, 1000);
                        }
                        break;
                    case PANDIAN_WHAT:
                        if(currentPage > 1) {
                            iDrinkFragmentInterface.loadmoreFinish();
                        }
                        try {
                            if(jsonResult != null && jsonResult.getString("status").equals("0")){
                                JSONArray jsonArray = jsonResult.getJSONArray("inventoryData");
                                if(jsonArray.length() == 0 && currentPage == 1){
                                    break;
                                }
                                ArrayList<PanDInfo> pandianInfoList = new ArrayList<>();
                                for(int i = 0; i < jsonArray.length();i++){
                                    PanDInfo panDInfo = new PanDInfo();
                                    panDInfo.setOperateTime(jsonArray.getJSONObject(i).getString("operateTime"));
                                    panDInfo.setGrossOnline(jsonArray.getJSONObject(i).getString("grossOnline"));
                                    panDInfo.setGrossOffline(jsonArray.getJSONObject(i).getString("grossOffline"));
                                    panDInfo.setSalesVolume(jsonArray.getJSONObject(i).getString("salesVolume"));
                                    pandianInfoList.add(panDInfo);
                                }
                                iDrinkFragmentInterface.pandiannotifyDataSetChanged(pandianInfoList,jsonResult.getInt("totalPage"));
                            }else {
                                loadingDialogFail = LoadingUtil.createLoadingDialog(context, "处理失败", 1, R.drawable.lose1, false);
                                //显示dialog
                                loadingDialogFail.show();
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        loadingDialogFail.dismiss();
                                    }
                                }, 1000);
                            }
                        }catch (Exception e){
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
        iDrinkFragmentInterface.dialogDismiss();
        if (what == 0) {
            alertView = new AlertView("提示", "网络连接出错，可以在APP端完成补货\nAPP首页-右上角加号-\n-查看今日运营报告-完成补货", null, new String[]{"确认"},
                    null, context, AlertView.Style.Alert, DataUtil.dip2px(context, Double.parseDouble(context.getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, int position) {
                    if (-1 == position) {
                        alertView.dismiss();
                    } else {
                        // TODO: 2016/8/24
                        alertView.dismiss();
                    }
                }
            }).setCancelable(true).setOnDismissListener(null);
            alertView.show();
        } else {
            ToastUtils.showToast((Activity) context, Constant.NETWORK_ERROR1);
        }
    }

}
