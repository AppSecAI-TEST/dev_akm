package com.zongsheng.drink.h17.common;

import android.util.Log;


/**
 * Created by Suchengjian on 2016.12.23.
 */

public class L { private L()
{
        /* cannot be instantiated */
    throw new UnsupportedOperationException("cannot be instantiated");

}
    public static boolean isDebug = true;// 是否需要打印bug，可以在application的onCreate函数里面初始化
    private static final String TAG = "way";
    private static StringBuffer sb = new StringBuffer();

    // 下面四个是默认tag的函数
    public static void i(String msg)
    {
        if (isDebug)
            Log.i(TAG, msg);
    }

    public static void d(String msg)
    {
        if (isDebug)
            Log.d(TAG, msg);
    }

    public static void e(String msg)
    {
        if (isDebug)
            Log.e(TAG, msg);
    }

    public static void v(String msg)
    {
        if (isDebug)
            Log.v(TAG, msg);
    }

    // 下面是传入自定义tag的函数
    public static void i(String tag, String msg)
    {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg)
    {
        if (isDebug)
            Log.d(tag, msg);
    }

    public static void e(String tag, String msg)
    {
        if (isDebug)
            Log.e(tag, msg);
    }

    public static void v(String tag, String msg)
    {
        if (isDebug)
            Log.v(tag, msg);
    }

    public static void vHex(String tag, String flag, byte[] buf){
        if(isDebug){
            for(int i = 0; i < buf.length; i++){
                if(sb == null){
                    sb = new StringBuffer();
                }
                sb.append(Integer.toHexString(buf[i] & 0xFF) + " ");
            }
            Log.v(tag,flag + sb.toString());
            sb.delete(0,sb.length());
        }
    }

    public static void dHex(String tag, String flag, byte[] buf){
        if(isDebug){
            for(int i = 0; i < buf.length; i++){
                if(sb == null){
                    sb = new StringBuffer();
                }
                sb.append(Integer.toHexString(buf[i] & 0xFF) + " ");
            }
            Log.d(tag,flag + sb.toString());
            sb.delete(0,sb.length());
        }
    }
}
