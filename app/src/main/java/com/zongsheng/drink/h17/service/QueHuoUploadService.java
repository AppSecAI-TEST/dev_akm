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
import com.zongsheng.drink.h17.common.MyCountDownTimer;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.front.bean.QueHuoRecord;
import com.zongsheng.drink.h17.nohttp.CallServer;
import com.zongsheng.drink.h17.nohttp.HttpListener;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/** 缺货信息上传 */
public class QueHuoUploadService extends Service {

	// 数据请求
	private Request<String> request;
	/** 数据库 */
	Realm realm;
	/** 要上传的记录 */
	QueHuoRecord firstRecord;

	private MyTimer myTimer;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		MyApplication.getInstance().getLogInit().d("创建缺货上传服务 = QueHuoUploadService");
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

//		Log.e("QueBiUploadService", "开始上传缺货记录");
		// 加载本地数据库中的商品信息
		RealmResults<QueHuoRecord> result = realm.where(QueHuoRecord.class).equalTo("isUploaded", "0").findAll();
		result = result.sort("createTime", Sort.ASCENDING);

		firstRecord = result.where().findFirst();
		if (firstRecord == null) {
			if (myTimer == null) {
				myTimer = new MyTimer(120000, 120000);
			}
			myTimer.start();
			return;
		}
		// 上传销售记录
		uploadRequest();
	}

	/** 上传缺货记录 */
	private void uploadRequest() {
//		Log.e("uploadQuehuo", "上传缺货了");
		MyApplication.getInstance().getLogBuyAndShip().d("上传缺货记录 = 机器编号 : "+firstRecord.getMachineSn()+" ; 货道号 : "+firstRecord.getRoad_no());
		String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/post/quehuoreport?machineSn=" + firstRecord.getMachineSn() + "&alarmDesc=&isQuehuo=" + firstRecord.getIsQueHuo() + "&roadNos="+ firstRecord.getRoad_no() + "&alarmReason=&alarmCode=";
		request = NoHttp.createStringRequest(url, RequestMethod.GET);
		//设置为必须网络
		request.setCacheMode(CacheMode.ONLY_REQUEST_NETWORK);
		// 添加到请求队列
		CallServer.getRequestInstance().add(this, 0, request, httpListener, true, false);
		if (myTimer == null) {
			myTimer = new MyTimer(120000, 120000);
		}
		myTimer.start();
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
								if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS) && firstRecord != null) {
									// 上传成功
									MyApplication.getInstance().getLogBuyAndShip().d("缺货记录上传成功");
									realm.executeTransaction(new Realm.Transaction() {
										@Override
										public void execute(Realm realm) {
											firstRecord.setIsUploaded("1");
										}
									});
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

		}
	};

	@Override
	public void onDestroy() {
		Log.e(SysConfig.ZPush, "缺货上传服务已关闭");
		MyApplication.getInstance().getLogInit().w("销毁缺货上传服务 = QueHuoUploadService");
		if (myTimer != null) {
			myTimer.cancel();
		}
		super.onDestroy();
	}
}
