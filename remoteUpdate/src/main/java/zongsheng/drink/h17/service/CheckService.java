package zongsheng.drink.h17.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.icu.util.TimeUnit;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import zongsheng.drink.h17.R;
import zongsheng.drink.h17.util.LogUtil;

/**
 * Created by Administrator on 17/8/2.
 * 检查售货App是否被关闭，自动重启该App
 */

public class CheckService extends Service {
    private LogUtil logUtil;
    private String packageName = "com.zongsheng.drink.h17.loading";
    private String className = "LoadingActivity";

    @Override
    public void onCreate() {
        super.onCreate();
        logUtil = new LogUtil(this.getClass().getSimpleName());
        logUtil.d("CheckService start");
        //变为前台服务，避免因为内存不足被杀掉
        Notification notification = new Notification.Builder(this)
                .setContentTitle("宗盛更新服务")
                .setContentText("守护进程")
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        startForeground(1,notification);
        new Thread(new TaskCheck()).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private class TaskCheck implements Runnable{
        @Override
        public void run() {
            while (!Thread.interrupted()){
                ActivityManager activityManager = (ActivityManager) CheckService.this.getSystemService(Context.ACTIVITY_SERVICE);
                //TODO:这里没有完成
                if (!hasBuyApp()){
                    //当前设备没有
                }
                boolean isRunning = false;
                //获取所有运行的服务
                for (ActivityManager.RunningServiceInfo appInfo : activityManager.getRunningServices(Integer.MAX_VALUE)){
                    logUtil.d("processInfo " + appInfo.service.getClassName());
                    if (appInfo.service.getClassName().equals("ServerHeartBeatRequestService")){
                        logUtil.d("找到了正在运行的服务");
                        isRunning = true;
                    }
                }
                if (!isRunning){
                    logUtil.d("尝试启动售货App");
                    ComponentName componentName = new ComponentName(packageName,className);
                    Intent intent = new Intent();
                    intent.setComponent(componentName);

                    startActivity(intent);
                }else {
                    logUtil.d("当前售货App正在运行");
                }
                try {
                    Thread.currentThread().sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 判断当前设备商是否有售卖App
     * @return
     */
    private boolean hasBuyApp(){
        PackageManager packageManager = getPackageManager();
        for (ApplicationInfo appInfo : packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES)){
            if (appInfo.className.equals("MyApplication"));{
                return true;
            }
        }
        return false;
    }

}
