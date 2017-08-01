package com.zongsheng.drink.h17.background.activity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Response;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.common.Constant;
import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.MarkLog;
import com.zongsheng.drink.h17.background.bean.ShopGoods;
import com.zongsheng.drink.h17.background.bean.ShopType;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.LoadingUtil;
import com.zongsheng.drink.h17.common.NetWorkRequImpl;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.common.ToastUtils;
import com.zongsheng.drink.h17.interfaces.INetWorkRequCallBackListener;
import com.zongsheng.drink.h17.interfaces.INetWorkRequInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * 商品同步
 * Created by 谢家勋 on 2016/8/23.
 */
public class GoodsTongbuActivity extends Activity implements OnItemClickListener,INetWorkRequCallBackListener,Observer{

    private static final int GOODS_TYPE = 1;
    private static final int GOODS_SYCH = 0;
    private AlertView alertView;
    private Realm realm;
    private Dialog dialogPingtai;
    private RealmResults<ShopGoods> shopGoodses;

    private Dialog dialog1;// 加载完成
    private INetWorkRequInterface iNetWorkRequInterface = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tongbu_goods);
        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();
        MyObservable.getInstance().registObserver(this);
    }


    @OnClick({R.id.rl_back, R.id.tv_pingtai})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.tv_pingtai:// 平台同步
                //
                shopGoodses = realm.where(ShopGoods.class).findAll();

                String s;
                if(shopGoodses.size() != 0 ){
                     s = "现在机器内已经有数据了\n确认覆盖?";
                }else{
                     s ="点击下载商品清单\n确定继续吗？";
                }
                alertView = new AlertView("提示", s, "取消", new String[]{"确认"}, null,
                        GoodsTongbuActivity.this, AlertView.Style.Alert, DataUtil.dip2px(GoodsTongbuActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), this).setCancelable(false).setOnDismissListener(null);
                alertView.show();
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
            // TODO: 2016/8/23
            alertView.dismiss();
            dialogPingtai = LoadingUtil.createLoadingDialog(this, Constant.SYNCHING, 1, R.drawable.ic_ios_juhua, true);
            dialogPingtai.show();

            //访问网络 获取商品信息
            requestGoodInfo();

        }
    }

    private void requestShopType(){
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/goodstypelist";
        if(iNetWorkRequInterface == null){
            iNetWorkRequInterface = new NetWorkRequImpl(this);
        }
        iNetWorkRequInterface.request(url, GOODS_TYPE, RequestMethod.GET);
    }

    private void requestGoodInfo() {
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/synchro/goods";
        if(iNetWorkRequInterface == null){
            iNetWorkRequInterface = new NetWorkRequImpl(this);
        }
        iNetWorkRequInterface.request(url, GOODS_SYCH, RequestMethod.GET);
    }

    protected void onDestroy() {
        super.onDestroy();
        if(iNetWorkRequInterface != null){
            iNetWorkRequInterface.cancel();
            iNetWorkRequInterface = null;
        }
        if(realm != null) {
            realm.close();
        }
        MyObservable.getInstance().unregistObserver(this);
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
            if (RequestMethod.HEAD != response.getRequestMethod()) {
                // 请求方法为HEAD时没有响应内容
                JSONObject jsonResult = null;
                try {
                    jsonResult = new JSONObject(response.get());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (what) {
                    case GOODS_SYCH:
                        try {
                            if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                Gson gson = new Gson();
                                Type type = new TypeToken<ArrayList<ShopGoods>>(){}.getType();
                                final List<ShopGoods> searchList = gson.fromJson(jsonResult.getString("goodsList"), type);
                                if (searchList != null && searchList.size() > 0) {
                                    // 取得数据后删除原有数据
                                    if (!realm.isInTransaction()) {
                                        realm.beginTransaction();
                                    }
                                    shopGoodses.deleteAllFromRealm();
                                    realm.commitTransaction();
                                    //写入操作日志
                                    MarkLog.markLog("从平台同步商品", SysConfig.LOG_LEVEL_MIDDLE, MyApplication.getInstance().getMachine_sn());
                                } else {
                                    dialogPingtai.dismiss();
                                    ToastUtils.showToast(GoodsTongbuActivity.this, "同步失败,请重试!");
                                    return;
                                }
                                //数据请求成功之后  把所有的商品 存入到数据库中
                                // 方便按机器号查询
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        for (int i = 0; i < searchList.size(); i++) {
                                            final ShopGoods info = new ShopGoods();
                                            info.setGoodsId(searchList.get(i).getGoodsId());//商品id
                                            info.setGoodsType(searchList.get(i).getGoodsType());//商品分类
                                            info.setAscription(searchList.get(i).getAscription());//是否自营
                                            info.setGoodsName(searchList.get(i).getGoodsName());//商品名称
                                            info.setGoodsImage(searchList.get(i).getGoodsImage());//商品url
                                            info.setGoodsPrice(searchList.get(i).getGoodsPrice());//商品价格
                                            info.setTypeName(searchList.get(i).getTypeName());//分类名称
                                            info.setGoodsStatus(searchList.get(i).getGoodsStatus());//商品状态
                                            info.setGoodsAbbreviation(searchList.get(i).getGoodsAbbreviation());

                                            realm.copyToRealm(info);
                                        }
                                    }
                                });
                                //显示dialog
                                //访问网络 获取商品分类
                                requestShopType();
//                                   dialogPingtai.dismiss();
                            } else {
                                if (dialogPingtai != null) {
                                    dialogPingtai.dismiss();
                                }
                                ToastUtils.showToast(GoodsTongbuActivity.this, "同步失败,请重试!");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (dialogPingtai != null) {
                                dialogPingtai.dismiss();
                            }
                            ToastUtils.showToast(GoodsTongbuActivity.this, "同步失败,请重试!");
                        }
                        break;

                    case GOODS_TYPE:
                        try {
                            if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                Gson gson = new Gson();
                                Type type = new TypeToken<ArrayList<ShopType>>() {
                                }.getType();
                                final List<ShopType> shopTypeList = gson.fromJson(jsonResult.getString("typeList"), type);
                                if (shopTypeList != null && shopTypeList.size() > 0) {
                                    final RealmResults<ShopType> shopTypes = realm.where(ShopType.class).findAll();
                                    if (!realm.isInTransaction()) {
                                        realm.beginTransaction();
                                    }
                                    shopTypes.deleteAllFromRealm();
                                    realm.commitTransaction();
                                } else {
                                    dialogPingtai.dismiss();
                                    ToastUtils.showToast(GoodsTongbuActivity.this, "同步失败,请重试!");
                                    return;
                                }
                                //数据请求成功之后  把所有的商品 存入到数据库中
                                // 方便按机器号查询
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        for (int i = 0; i < shopTypeList.size(); i++) {
                                            final ShopType info = new ShopType();
                                            info.setParentId(shopTypeList.get(i).getParentId());//分类编号
                                            info.setTypeId(shopTypeList.get(i).getTypeId()); //上级
                                            info.setTypeName(shopTypeList.get(i).getTypeName());//分类名称

                                            realm.copyToRealm(info);
                                        }
                                    }
                                });
                                //显示dialog
                                dialogPingtai.dismiss();
                                dialog1 = LoadingUtil.createLoadingDialog(GoodsTongbuActivity.this, "同步成功", 1, 0, false);// 开启登录完成dialog
                                dialog1.show();
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        dialog1.dismiss();
                                    }
                                }, 1000);
                            } else {
                                if (dialogPingtai != null) {
                                    dialogPingtai.dismiss();
                                }
                                ToastUtils.showToast(GoodsTongbuActivity.this, "同步失败,请重试!");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (dialogPingtai != null) {
                                dialogPingtai.dismiss();
                            }
                            ToastUtils.showToast(GoodsTongbuActivity.this, "同步失败,请重试!");
                        }
                        break;
                }
            }
        } else {
            if (dialogPingtai != null) {
                dialogPingtai.dismiss();
            }
            ToastUtils.showToast(GoodsTongbuActivity.this, "同步失败,请重试!");
        }
    }

    @Override
    public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) throws Exception {
        if(networkMillis >5000 && dialogPingtai != null){
            dialogPingtai.dismiss();
        }
        if (dialogPingtai != null) {
            dialogPingtai.dismiss();
        }
        ToastUtils.showToast(GoodsTongbuActivity.this, "同步失败,请检查网络连接!");
    }

    @Override
    public void update(Observable observable, Object o) {
        finish();
    }
}
