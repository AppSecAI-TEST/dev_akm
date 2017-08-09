package com.zongsheng.drink.h17.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.CacheMode;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.common.Constant;
import com.zongsheng.drink.h17.common.L;
import com.zongsheng.drink.h17.common.SharedPreferencesUtils;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.front.bean.BackGroundRequest;
import com.zongsheng.drink.h17.nohttp.CallServer;
import com.zongsheng.drink.h17.nohttp.HttpListener;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * 本地请求
 */
public class BackGroundRequestService extends Service {

    // 数据请求
    private Request<String> request;
    /**
     * 数据库
     */
    Realm realm;
    /**
     * 要上传的记录
     */
    BackGroundRequest firstRecord;

    private int nReqErrortime = 0;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        L.v(SysConfig.ZPush,"BackGroundRequestService onCreate...");
        MyApplication.getInstance().getLogInit().d("创建服务 = BackgroundRequestService");
        super.onCreate();
        realm = Realm.getDefaultInstance();
        getUploadRecords();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 上传记录
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * task处理
     */
    TimerTask task = new TimerTask() {
        public void run() {
            L.v(SysConfig.ZPush,"backgroundRequestService task run....");
            getUploadRecords();
        }
    };

    /**
     * 取得要上传的记录信息
     */
    public void getUploadRecords() {
        Log.e(SysConfig.ZPush, "开始处理待处理的接口请求");
        // 加载本地数据库中的信息
        RealmResults<BackGroundRequest> result = realm.where(BackGroundRequest.class).findAll();
        if (result == null || result.isEmpty()) {
            HandlerTask(SysConfig.L_REQ_AG_TIME_5);
            return;
        }
        firstRecord = result.first();
        if (firstRecord == null) {
            HandlerTask(SysConfig.L_REQ_AG_TIME_5);
            return;
        }
        // 上传记录
        uploadRequest();
    }

    /**
     * 上传销售记录
     */
    private void uploadRequest() {
        // 创建请求对象。
        request = NoHttp.createStringRequest(SysConfig.NET_SERVER_HOST_ADDRESS + firstRecord.getRequestInterface(), RequestMethod.GET);
        //设置为必须网络
        request.setCacheMode(CacheMode.ONLY_REQUEST_NETWORK);
        if (request != null) {
            // 添加到请求队列
            CallServer.getRequestInstance().add(this, firstRecord.getWhat(), request, httpListener, true, false);
        } else {
            HandlerTask(SysConfig.L_REQ_AG_TIME_5);
        }
    }

    private void HandlerTask(long time) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                task.run();
            }
        }, time);
    }

    private void deleteBackGroundReq() {
        nReqErrortime = 0;
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                firstRecord.deleteFromRealm();
            }
        });
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
                    switch (what) {
                        case Constant.BACKGROUND_WHAT_0://验证码
                            try {
                                // 如果成功
                                if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                    deleteBackGroundReq();
                                } else {
                                    nReqErrortime++;
                                    if (nReqErrortime > SysConfig.ERROR_TIME) {
                                        deleteBackGroundReq();
                                    }
                                }
                                task.run();
                            } catch (Exception e) {
                                task.run();
                                e.printStackTrace();
                            }
                            break;
                        case Constant.BACKGROUND_WHAT_MQ:
                            try {
                                if(jsonResult != null && jsonResult.getString(SysConfig.JSON_KEY_ERROR_CODE).equals(SysConfig.ERROR_CODE_SUCCESS)){
                                    JSONObject jsonObject = new JSONObject(jsonResult.getString(SysConfig.JSON_KEY_MACHINEINFOFOEMQ));
                                    MyApplication.getInstance().setUsedStatus(jsonObject.getString(SysConfig.JSON_KEY_USEDSTATUS));
                                    MyApplication.getInstance().setMqIP(jsonObject.getString(SysConfig.JSON_KEY_MQIP));
                                    MyApplication.getInstance().setAutomaticRefundState(jsonObject.getString(SysConfig.JSON_KEY_REFUNDSTATE));
                                    SharedPreferencesUtils.setParam(MyApplication.getInstance(),SysConfig.JSON_KEY_MQIP , jsonObject.getString(SysConfig.JSON_KEY_MQIP));
                                    SharedPreferencesUtils.setParam(MyApplication.getInstance(), SysConfig.JSON_KEY_USEDSTATUS, jsonObject.getString(SysConfig.JSON_KEY_USEDSTATUS));
                                    sendBroadcast(new Intent(SysConfig.RECEIVER_ACTION_DEAMON));
                                }
                                deleteBackGroundReq();
                            }catch(Exception e){
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
            HandlerTask(SysConfig.L_REQ_AG_TIME_15);
        }
    };

    @Override
    public void onDestroy() {
        MyApplication.getInstance().getLogInit().w("销毁服务 = BackgroundRequestService");
        cancel();
        super.onDestroy();
    }

    private void cancel(){
        if(request != null){
            request.cancel();
            request = null;
        }
    }

}
