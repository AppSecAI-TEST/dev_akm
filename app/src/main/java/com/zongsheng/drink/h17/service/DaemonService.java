package com.zongsheng.drink.h17.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.zongsheng.drink.h17.loading.LoadingActivity;

import java.util.List;

/**
 * Created by dongxiaofei on 16/9/10.
 * 当应用崩溃的时候，此Service被启动
 * TODO:它只是启动售货App和更新App，这里使用IntentService更合适
 */

public class DaemonService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Log.v("=========", "***** DaemonService *****: onCreate");
    }

    @Override
    public void onStart(Intent intent, int startId) {
//        Log.v("=========", "***** DaemonService *****: onStart");
        // 这里可以做Service该做的事
//        if (!isActivityRunning(this, "com.zongsheng.drink.h17.loading.LoadingActivity")
//                && !isActivityRunning(this, "com.zongsheng.drink.h17.front.activity.BuyActivity")) {
        if (!isRunning(this)) {
//            Log.i("service", "应用未启动,现在启动");
            Intent ootStartIntent = new Intent(this, LoadingActivity.class);
            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(ootStartIntent);
        }

        if (!isBackRunning(this, "com.zongs.zongsheng_update")) {
            // 开启更新的APP
            startAPP("com.zongs.zongsheng_update");
        }

    }

    /**
     * 打开APP
     */
    public void startAPP(String appPackageName) {
        try {
            Intent intent = this.getPackageManager().getLaunchIntentForPackage(appPackageName);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("service", appPackageName + "未安装");
        }
    }
    //此方法是正常生命周期方法
    public void onDestroy()
    {
        Intent localIntent = new Intent();
        localIntent.setClass(this, DaemonService.class); // 销毁时重新启动Service
        this.startService(localIntent);
    }


//    /** 判断activity是否运行 */
//    public static boolean isActivityRunning(Context mContext, String activityClassName){
//        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(1);
//        if(info != null && info.size() > 0){
//            ComponentName component = info.get(0).topActivity;
//            if(activityClassName.equals(component.getClassName())){
//                return true;
//            }
//        }
//        return false;
//    }


    private boolean isRunning(Context mContext) {
        boolean isAppRunning = false;
        ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(getPackageName()) && info.baseActivity.getPackageName().equals(getPackageName())) {
                isAppRunning = true;
                //find it, break
                break;
            }
        }

        return isAppRunning;
    }

    private boolean isBackRunning(Context mContext, String packageName) {
        boolean isAppRunning = false;
        ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.baseActivity.getPackageName().equals(getPackageName())) {
                isAppRunning = true;
                //find it, break
                break;
            }
        }

        return isAppRunning;
    }

}
