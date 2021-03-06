package com.zongsheng.drink.h17.util;

import android.text.TextUtils;
import android.util.Log;

import java.util.Collections;
import java.util.List;

/**
 * Created by 袁国栋 on 17/8/1.
 * 一个日志工具，可以控制单实例或全局是否打印日志，避免频繁使用注释，同时将日志存入本地
 */

public class LogUtil {
    private String TAG;
    //控制所有实例是否打印Log
    private volatile static boolean shouldPrintLogAllCtrl = true;
    //控制单个实例是否打印Log
    private boolean shouldPrintLog;
    //要输出到磁盘的TAG
    private static List<String> logTags;

    public LogUtil(String TAG){
        this.TAG = TAG;
        shouldPrintLog = true;
    }

    /**
     * 设置要输出到磁盘的日志Tag
     * @param logTags tag列表
     */
    public static void setLogTags(List<String> logTags) {
        LogUtil.logTags = logTags;
    }

    /**
     * 设置是否打印Log
     * @param flag true 表示打印，false 表示不打印，默认为true
     */
    public void setShouldPrintLog(boolean flag){
        this.shouldPrintLog = flag;
    }

    /**
     * 设置全局是否打印Log
     * @param flag true 表示打印，false 表示不打印，默认为true
     */
    public void setShouldPrintLogAllCtrl(boolean flag){
        shouldPrintLogAllCtrl = flag;
    }

    /**
     * Log.d 使用默认的TAG打印Log
     * @param text Log内容
     */
    public void d(String text){
        d(TAG,text);
    }

    /**
     * Log.d 使用指定的TAG打印Log
     * @param TAG TAG
     * @param text Log内容
     */
    public void d(String TAG,String text){
        if (!TextUtils.isEmpty(TAG) && logTags.contains(TAG)){
            FileUtils.writeStringToFile(TAG+" - "+text);
        }
        if (shouldPrintLogAllCtrl && shouldPrintLog){
            Log.d(TAG,text);
        }
    }

    /**
     * Log.e 使用默认的TAG打印Log
     * @param text Log内容
     */
    public void e(String text){
        e(TAG,text);
    }

    /**
     * Log.e 使用指定的TAG打印Log
     * @param TAG TAG
     * @param text Log内容
     */
    public void e(String TAG,String text){
        if (!TextUtils.isEmpty(TAG) && logTags.contains(TAG)){
            FileUtils.writeStringToFile(TAG+" - "+text);
        }
        if (shouldPrintLogAllCtrl && shouldPrintLog){
            Log.e(TAG,text);
        }
    }
    /**
     * Log.v 使用默认的TAG打印Log
     * @param text Log内容
     */
    public void v(String text){
        v(TAG,text);
    }

    /**
     * Log.v 使用指定的TAG打印Log
     * @param TAG TAG
     * @param text Log内容
     */
    public void v(String TAG,String text){
        if (!TextUtils.isEmpty(TAG) && logTags.contains(TAG)){
            FileUtils.writeStringToFile(TAG+" - "+text);
        }
        if (shouldPrintLogAllCtrl && shouldPrintLog){
            Log.v(TAG,text);
        }
    }
    /**
     * Log.i 使用默认的TAG打印Log
     * @param text Log内容
     */
    public void i(String text){
        i(TAG,text);
    }

    /**
     * Log.i 使用指定的TAG打印Log
     * @param TAG TAG
     * @param text Log内容
     */
    public void i(String TAG,String text){
        if (!TextUtils.isEmpty(TAG) && logTags.contains(TAG)){
            FileUtils.writeStringToFile(TAG+" - "+text);
        }
        if (shouldPrintLogAllCtrl && shouldPrintLog){
            Log.i(TAG,text);
        }
    }
    /**
     * Log.w 使用默认的TAG打印Log
     * @param text Log内容
     */
    public void w(String text){
        w(TAG,text);
    }

    /**
     * Log.w 使用指定的TAG打印Log
     * @param TAG TAG
     * @param text Log内容
     */
    public void w(String TAG,String text){
        if (!TextUtils.isEmpty(TAG) && logTags.contains(TAG)){
            FileUtils.writeStringToFile(TAG+" - "+text);
        }
        if (shouldPrintLogAllCtrl && shouldPrintLog){
            Log.w(TAG,text);
        }
    }

}
