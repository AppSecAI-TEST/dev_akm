package com.zongsheng.drink.h17.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.interfaces.ICallBack2Service;
import com.zongsheng.drink.h17.service.thread.MyThread;


/**
 * Created by Suchengjian on 2017.2.11.
 */

public class HexinService extends Service implements ICallBack2Service{

    private MyThread subscribeThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        subscribe();
    }

    /**
     * 消费者线程
     */
    private void subscribe() {
        subscribeThread = new MyThread(SysConfig.L_REQ_AG_TIME_5,this);
        subscribeThread.start();
    }
    //用于从线程中获取数据，更新ui
    final Handler incomingMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(subscribeThread != null) {
            subscribeThread.killer();
        }
        sendBroadcast(new Intent(SysConfig.RECEIVER_ACTION_DEAMON));
    }

    @Override
    public void doSomething() {
        if(!MyApplication.getInstance().getMqIP().equals("")){
            if(!MyApplication.getInstance().getMqIP().equals(ClientConnectMQ.getInstance().getQuefactory().getHost())) {
                ClientConnectMQ.getInstance().setfactoryHost(MyApplication.getInstance().getMqIP());
            }
            ClientConnectMQ.getInstance().receiverMsg2MQ(getApplicationContext(),incomingMessageHandler);
        }
    }
}
