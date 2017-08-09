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
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.front.bean.MachineFaultRecord;
import com.zongsheng.drink.h17.nohttp.CallServer;
import com.zongsheng.drink.h17.nohttp.HttpListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * 故障信息上传
 */
public class MachineFaultUploadService extends Service {


    // 数据请求
    private Request<String> request;
    /**
     * 数据库
     */
    Realm realm;
    /**
     * 要上传的记录
     */
    MachineFaultRecord firstRecord;

    /**
     * 请求失败的次数
     */
    private int nReqErrorTime = 0;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        MyApplication.getInstance().getLogInit().d("创建故障上传服务 = MachineFaultUploadService");
        super.onCreate();
        realm = Realm.getDefaultInstance();
        getUploadRecords();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 上传记录
        //getUploadRecords();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * task处理
     */
    TimerTask task = new TimerTask() {
        public void run() {
            getUploadRecords();
        }
    };

    /**
     * 取得要上传的记录信息
     */
    public void getUploadRecords() {

        // 加载本地数据库中的商品信息
        RealmResults<MachineFaultRecord> result = realm.where(MachineFaultRecord.class).equalTo("isUploaded", "0").findAll();
        result = result.sort("createTime"); // Sort ascending
        result = result.sort("createTime", Sort.ASCENDING);

        firstRecord = result.where().findFirst();
        if (firstRecord == null) {
            HandlerTask(SysConfig.L_REQ_AG_TIME_30);
            return;
        }
        // 上传销售记录
        uploadRequest();
    }


    private void uploadRequest() {
        // 创建请求对象。
        request = NoHttp.createStringRequest(SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/post/faultreport", RequestMethod.GET);
        //设置为必须网络
        request.setCacheMode(CacheMode.ONLY_REQUEST_NETWORK);
        Map<String, String> paramMap = new HashMap<String, String>();
        if (request != null) {
            // 参数
            paramMap.put("machineSn", ((MyApplication) getApplication()).getMachine_sn());
            paramMap.put("isMasterFault", firstRecord.getIsMasterFault());
            paramMap.put("masterAlarmReason", firstRecord.getMasterAlarmReason());
            paramMap.put("isPaperFault", firstRecord.getIsPaperFault());
            paramMap.put("paperAlarmReason", firstRecord.getPaperAlarmFault());
            paramMap.put("isCoinFault", firstRecord.getIsCoinFault());
            paramMap.put("coinAlarmReason", firstRecord.getCoinAlarmReason());
            DataUtil.requestDateContrl(paramMap, request);
            // 添加到请求队列
            MyApplication.getInstance().getLogInit().d("上传故障信息 = "+request.url());
            CallServer.getRequestInstance().add(this, 0, request, httpListener, true, false);
        } else {
            HandlerTask(SysConfig.L_REQ_AG_TIME_30);
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
                        case 0://验证码
                            try {
                                // 如果成功
                                if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                    // 上传成功
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            firstRecord.setIsUploaded("1");
                                        }
                                    });
                                    firstRecord = null;
                                    task.run();
                                } else if(jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_ARGS_ERROR)){
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            firstRecord.deleteFromRealm();
                                        }
                                    });
                                    firstRecord = null;
                                    task.run();
                                } else {
                                    nReqErrorTime++;
                                    if (nReqErrorTime > SysConfig.ERROR_TIME) {
                                        HandlerTask(SysConfig.L_REQ_AG_TIME_60);
                                        nReqErrorTime = 0;
                                    }else{
                                        firstRecord = null;
                                        task.run();
                                    }
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
        public void onFailed(int what, String url, final Object tag, Exception exception, int responseCode, long networkMillis) {
            HandlerTask(SysConfig.L_REQ_AG_TIME_10);
        }
    };

    @Override
    public void onDestroy() {
        MyApplication.getInstance().getLogInit().w("销毁故障上传服务 = MachineFaultUploadService");
//        Log.e("uploadService", "上传服务已关闭");
        super.onDestroy();
    }
}
