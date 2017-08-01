package com.zongsheng.drink.h17.background.common;

import android.os.AsyncTask;
import android.os.SystemClock;

import com.zongsheng.drink.h17.ComActivity;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.common.Constant;
import com.zongsheng.drink.h17.common.L;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 机器命令task
 * Created by dongxiaofei on 2016/10/2.
 */

public class OpenCabinetDoorForAKMTask extends AsyncTask<String, Integer, String> {
    MachineOrderRstListener machineOrderRstListener;
    ComActivity comActivity;
    List<Integer> roadList;
    int boxindex;
    public OpenCabinetDoorForAKMTask(ComActivity comActivity, int boxindex, List<Integer> roadList, MachineOrderRstListener machineOrderRstListener) {
        this.comActivity = comActivity;
        this.roadList = roadList;
        this.machineOrderRstListener = machineOrderRstListener;
        this.boxindex = boxindex;
    }

    @Override
    protected String doInBackground(String... params) {
        String str = "";
        Map<Integer, String> kucunMap = new HashMap<>();
        kucunMap.putAll(MyApplication.getInstance().getAokemaGeZiKuCunMap().get(boxindex));
        //开始开门计数
        MyApplication.getInstance().setIsCount(true);
        long countTime = System.currentTimeMillis();
        for (Integer roadNo : roadList) {
            L.e("", "开始开门:" + roadNo);
            str = comActivity.openGeziDoorForAKM(boxindex, roadNo);
            if (!"".equals(str)) {
                SystemClock.sleep(300);
                L.e("", "开始开门:" + roadNo);
                str = comActivity.openGeziDoorForAKM(boxindex, roadNo);
                if (!"".equals(str)) {
                    // 开门失败
                    return str;
                }
            }
        }

        while((System.currentTimeMillis() - countTime) <= Constant.ONE_KEY_OPEN_DOOR){
            if(MyApplication.getInstance().count >= 50) {
                //注意顺序，不然可能会有错误
                MyApplication.getInstance().setIsCount(false);
                MyApplication.getInstance().count = 0;
                return "";
            }
            /*if((System.currentTimeMillis() - countTime) >= Constant.DIALOG_DISMISS_TIME && MyApplication.getInstance().count == 0){
                MyApplication.getInstance().setIsCount(false);
                MyApplication.getInstance().count = 0;
                return "9999/正忙";
            }*/
            SystemClock.sleep(5000);
        }
        MyApplication.getInstance().setIsCount(false);
        MyApplication.getInstance().count = 0;
        return "9999/正忙";
    }

    //TestAsyncTask被后台线程执行后，被UI线程被调用，一般用于初始化界面控件，如进度条
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    //doInBackground执行完后由UI线程调用，用于更新界面操作
    @Override
    protected void onPostExecute(String result) {
        if ("".equals(result)) {
            // 执行成功
            machineOrderRstListener.success();
        } else {
            // 执行失败
            machineOrderRstListener.fail();
        }

        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }
}
