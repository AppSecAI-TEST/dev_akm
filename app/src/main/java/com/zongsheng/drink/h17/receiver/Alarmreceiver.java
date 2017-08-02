package com.zongsheng.drink.h17.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zongsheng.drink.h17.service.DaemonService;

/**
 *
 * Created by dongxiaofei on 16/9/10.
 * 当应用崩溃的时候重启App
 */

public class Alarmreceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("arui.alarm.action")) {
            Intent i = new Intent();
            i.setClass(context, DaemonService.class);
            // 启动service
            // 多次调用startService并不会启动多个service 而是会多次调用onStart
            context.startService(i);
        }
    }
}
