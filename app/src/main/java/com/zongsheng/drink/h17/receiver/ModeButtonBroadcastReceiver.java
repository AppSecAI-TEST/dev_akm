package com.zongsheng.drink.h17.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.background.MarkLog;
import com.zongsheng.drink.h17.background.activity.HomeActivity;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.background.service.BackBtnService;
import com.zongsheng.drink.h17.loading.LoadingActivity;



/**
 * MODE键监听
 * Created by 董晓飞 on 2016/8/17.
 * TODO:切换MODE时没有处理当前模式的Activity，有可能造成内存泄漏，这可能和屏蔽返回键有关
 */

public class ModeButtonBroadcastReceiver extends BroadcastReceiver {

    @Override

    public void onReceive(Context context, Intent intent) {

        Intent iii = new Intent(context, BackBtnService.class);
        context.stopService(iii);
        boolean mIsEngMode = intent.getIntExtra("state", 1) == 1;
//        Log.e("判断:", "是否在维护模式:" + mIsEngMode);
        if (mIsEngMode) {// 打开维护模式
            if (MyApplication.getInstance().isWeihu()) {// 已经在维护模式
                return;
            }
        } else {// 关闭维护模式
            if (!MyApplication.getInstance().isWeihu()) {
                return;
            }
        }
//        Log.e("执行:", "切换");
        if (intent.getAction().equals(SysConfig.ENG_MODE_SWITCH)) {
            MyObservable.getInstance().notifyChange();
            if (mIsEngMode) {// 打开维护模式
                //写入操作日志
                MarkLog.markLog("登陆机器管理端", SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
                Intent ii = new Intent(context, HomeActivity.class);
                ii.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(ii);
            } else {// 关闭维护模式
                Intent ii = new Intent(context, LoadingActivity.class);
                ii.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(ii);
            }
        }
    }
}