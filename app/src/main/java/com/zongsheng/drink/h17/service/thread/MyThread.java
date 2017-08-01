package com.zongsheng.drink.h17.service.thread;

import android.os.SystemClock;

import com.zongsheng.drink.h17.common.L;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.interfaces.ICallBack2Service;

/**
 * Created by Suchengjian on 2017.2.18.
 */

public class MyThread extends Thread {
    private volatile boolean isRunning = true;
    private long  interruptTime;
    private ICallBack2Service iCallBack2Service;

    public MyThread(long interruptTime, ICallBack2Service iCallBack2Service) {
        this.interruptTime = interruptTime;
        this.iCallBack2Service = iCallBack2Service;
    }

    @Override
    public void run() {
        while (isRunning){ //非阻塞过程中通过判断中断标志来退出
            try {
                L.v(SysConfig.ZPush,hashCode()+"-----doSomething");
                iCallBack2Service.doSomething();
                Thread.sleep(interruptTime);
            }catch (Exception e){
                e.printStackTrace();
                SystemClock.sleep(SysConfig.L_REQ_AG_TIME_5);
            }
        }
    }
    public void killer(){
        isRunning = false;
        iCallBack2Service = null;
    }

}
