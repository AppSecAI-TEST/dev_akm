package com.zongsheng.drink.h17.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.zongsheng.drink.h17.front.activity.BuyActivity;
import com.zongsheng.drink.h17.loading.LoadingActivity;


/**
 * 开机自启动
 * Created by 谢家勋 on 2016/8/17.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    static final String action_boot = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(action_boot)) {

//            Intent ootStartIntent = new Intent(context, LoadingActivity.class);
//
//            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            context.startActivity(ootStartIntent);

            // 启动完成
            Intent ii = new Intent(context, Alarmreceiver.class);
            ii.setAction("arui.alarm.action");
            PendingIntent sender = PendingIntent.getBroadcast(context, 0,
                    ii, 0);
            long firstime = SystemClock.elapsedRealtime();
            AlarmManager am = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);

            // 10秒一个周期，不停的发送广播
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime,
                    10 * 1000, sender);

            am = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);
            // 发送定期
            Intent newIntent = new Intent(context, ClearAlarmReceiver.class);
            newIntent.setAction("android.zongsheng.alarm.clear.action");
            sender = PendingIntent.getBroadcast(context, 0, newIntent, 0);
            int interval = 3600 * 1000;//闹铃间隔， 这里设为1小时闹一次
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime + 1000 * 60, interval, sender);

        }

    }

}