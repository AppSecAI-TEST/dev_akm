package com.zongsheng.drink.h17.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.CacheMode;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.background.bean.LogsInfo;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.L;
import com.zongsheng.drink.h17.common.MyCountDownTimer;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.nohttp.CallServer;
import com.zongsheng.drink.h17.nohttp.HttpListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

public class LogUploadService extends Service {

	// 数据请求
	private Request<String> request;
	/** 数据库 */
	Realm realm;
	/** 要上传的记录 */
	List<LogsInfo> firstRecordList;
	private MyTimer myTimer;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		realm = Realm.getDefaultInstance();
		// 上传记录
		getUploadRecords();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	class MyTimer extends MyCountDownTimer {
		public MyTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {

		}
		@Override
		public void onFinish() {
			getUploadRecords();
		}
	}
	
	/** 取得要上传的记录信息 */
	public void getUploadRecords() {
		Log.e("uploadService", "开始上传日志");
		// 加载本地数据库中的商品信息
		RealmResults<LogsInfo> result = realm.where(LogsInfo.class).equalTo("isUploaded", "0").findAll();

		firstRecordList = realm.copyFromRealm(result);
		if (firstRecordList == null || firstRecordList.size() == 0) {

			if (myTimer != null) {
				myTimer.cancel();
			}
			myTimer = new MyTimer(120000, 120000);
			myTimer.start();

			return;
		}
		// 上传销售记录
		uploadRequest();
	}

	/** 上传销售记录 */
	private void uploadRequest() {

		Log.e("uploadService", "上传日志1");
		// 创建请求对象。
		request = NoHttp.createStringRequest(SysConfig.NET_SERVER_HOST_ADDRESS + "api/log/post/infos", RequestMethod.GET);
		//设置为必须网络
		request.setCacheMode(CacheMode.ONLY_REQUEST_NETWORK);

		Map<String, String> paramMap = new HashMap<String, String>();
		if (request != null) {
			// 参数
			StringBuffer log_infos = new StringBuffer();
			int i = 1;
			for (LogsInfo logsInfo : firstRecordList) {
				if (i > 10) {
					break;
				}
				i ++;
				if (logsInfo.getMachineSn() == null || "".equals(logsInfo.getMachineSn())) {
					continue;
				}

				log_infos.append(((MyApplication) getApplication()).getMachine_sn() + "|");
				log_infos.append(logsInfo.getLogLevel() + "|");
				log_infos.append(logsInfo.getOprateContent() + "|");
				log_infos.append(logsInfo.getOprateTime());
				log_infos.append(";");

			}
			String saleInfoStr = log_infos.toString();
			if (saleInfoStr.endsWith(";")) {
				saleInfoStr = saleInfoStr.substring(0, saleInfoStr.length() - 1);
			}
			try {
				paramMap.put("logInfos", URLEncoder.encode(saleInfoStr, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			DataUtil.requestDateContrl(paramMap, request);
			// 添加到请求队列
			CallServer.getRequestInstance().add(this, 0, request, httpListener, true, false);
		} else {
			if (myTimer != null) {
				myTimer.cancel();
			}
			myTimer = new MyTimer(120000, 120000);
			myTimer.start();
		}
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
											int i= 1;
											for (LogsInfo logsInfo : firstRecordList) {
												if (i > 10) {
													break;
												}
												i ++;
												List<LogsInfo> result = realm.where(LogsInfo.class).equalTo("oprateTime", logsInfo.getOprateTime()).
														equalTo("machineSn", logsInfo.getMachineSn())
														.equalTo("oprateContent", logsInfo.getOprateContent()).findAll();
												if (result == null || result.size() == 0) {
													continue;
												}
												for (LogsInfo logs : result) {
													logs.setIsUploaded("1");
												}
											}
										}
									});
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							firstRecordList = null;
							if (myTimer != null) {
								myTimer.cancel();
							}
							myTimer = new MyTimer(120000, 120000);
							myTimer.start();
							break;
						default:
							break;
					}
				}
			}
		}

		@Override
		public void onFailed(int what, String url, final Object tag, Exception exception, int responseCode, long networkMillis) {
			if (myTimer != null) {
				myTimer.cancel();
			}
			myTimer = new MyTimer(120000, 120000);
			myTimer.start();
		}
	};

	@Override
	public void onDestroy() {
		if (myTimer != null) {
			myTimer.cancel();
		}
		super.onDestroy();
	}
}
