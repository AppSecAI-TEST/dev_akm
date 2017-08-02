package com.zongsheng.drink.h17.service;

import android.content.Context;

import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.zongsheng.drink.h17.util.LogUtil;

/**
 * Created by 袁国栋 on 17/8/2.
 * 集成的个推推送服务
 */

public class GeTuiService extends GTIntentService{
    private LogUtil logUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        logUtil = new LogUtil(this.getClass().getSimpleName());
    }

    @Override
    public void onReceiveServicePid(Context context, int i) {

    }

    @Override
    public void onReceiveClientId(Context context, String s) {
        //接收CID ef3237dc0b13f9c848f5c1e0cc30d5f4
        logUtil.d("CID is "+s);
    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage gtTransmitMessage) {
        //接收推送信息
        String text = new String(gtTransmitMessage.getPayload());
        logUtil.d("Receive GeTui msg is "+text);
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean b) {
        //Client的在线状态变化
        logUtil.d("GeTui online state is "+b);
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage gtCmdMessage) {
        //回执
    }
}
