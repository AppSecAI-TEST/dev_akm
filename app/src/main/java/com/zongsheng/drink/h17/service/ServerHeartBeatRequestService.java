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
import com.zongsheng.drink.h17.background.bean.BindGeZi;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.front.bean.BackGroundRequest;
import com.zongsheng.drink.h17.nohttp.CallServer;
import com.zongsheng.drink.h17.nohttp.HttpListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmResults;

/** 机器在线状态心跳请求 60s请求一次*/
public class ServerHeartBeatRequestService extends Service {

	// 数据请求
	private Request<String> request;
	Realm realm;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		realm = Realm.getDefaultInstance();
		uploadRequest();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 上传记录
		return super.onStartCommand(intent, flags, startId);
	}
	
	/** task处理 */
	TimerTask task = new TimerTask() {
		public void run() {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					uploadRequest();
				}
			}, SysConfig.L_REQ_AG_TIME_30M);
		}
	};
	
	/** 上传记录 */
	private void uploadRequest() {
		String machineSn = ((MyApplication) getApplication()).getMachine_sn();
		// 取得格子柜列表
		RealmResults<BindGeZi> bgezi = realm.where(BindGeZi.class).findAll();
		if (bgezi != null && bgezi.size() > 0) {
			for (BindGeZi gezi : realm.copyFromRealm(bgezi)) {
				if (gezi != null && gezi.getMachineSn() != null && "".equals(gezi.getMachineSn())) {
					machineSn = machineSn + ";" + gezi.getMachineSn();
				}
			}
 		}
		// 创建请求对象。
		String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/put/" + machineSn +"/status/online";
		request = NoHttp.createStringRequest(url, RequestMethod.GET);
		//设置为必须网络
		request.setCacheMode(CacheMode.ONLY_REQUEST_NETWORK);
		if (request != null) {
			// 添加到请求队列
			CallServer.getRequestInstance().add(this, 0, request, null, true, false);
		}
		task.run();
	}


	@Override
	public void onDestroy() {
		if (request != null) {
			request.cancel();
		}
		super.onDestroy();
	}
}
