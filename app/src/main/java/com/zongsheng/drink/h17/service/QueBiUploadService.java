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
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.front.bean.QueBiRecord;
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
 * 缺币信息上传
 */
public class QueBiUploadService extends Service {

    // 数据请求
    private Request<String> request;
    /**
     * 数据库
     */
    Realm realm;
    /**
     * 要上传的记录
     */
    QueBiRecord firstRecord;

    private int nReqErrorTime =0;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
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
            getUploadRecords();
        }
    };

    /**
     * 取得要上传的记录信息
     */
    public void getUploadRecords() {
        Log.e("QueBiUploadService", "开始上传缺币记录");
        // 加载本地数据库中的商品信息
        RealmResults<QueBiRecord> result = realm.where(QueBiRecord.class).equalTo("isUploaded", "0").findAll();
        result = result.sort("createTime", Sort.ASCENDING);

        firstRecord = result.where().findFirst();
        if (firstRecord == null) {
            HandlerTask(SysConfig.L_REQ_AG_TIME_60);
            return;
        }
        // 上传销售记录
        uploadRequest();
    }

    /**
     * 上传缺币记录
     */
    private void uploadRequest() {
        // 创建请求对象。
        request = NoHttp.createStringRequest(SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/post/quebireport", RequestMethod.GET);
        //设置为必须网络
        request.setCacheMode(CacheMode.ONLY_REQUEST_NETWORK);

        Map<String, String> paramMap = new HashMap<String, String>();
        if (request != null) {
            // 参数
            paramMap.put("machineSn", firstRecord.getMachineSn());
            paramMap.put("isQuebiJiao", firstRecord.getIsQueBi_five());
            paramMap.put("isQuebiYuan", firstRecord.getIsQueBi_one());
            paramMap.put("alarmCode", "");
            paramMap.put("alarmReason", "");
            paramMap.put("alarmDesc", "");

            DataUtil.requestDateContrl(paramMap, request);
            // 添加到请求队列
            CallServer.getRequestInstance().add(this, 0, request, httpListener, true, false);
        } else {
            HandlerTask(SysConfig.L_REQ_AG_TIME_60);
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
                                }else if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_ARGS_ERROR)){
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            firstRecord.deleteFromRealm();
                                        }
                                    });
                                    firstRecord = null;
                                    task.run();
                                }else{
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
        Log.e("uploadService", "上传服务已关闭");
        super.onDestroy();
    }
}
