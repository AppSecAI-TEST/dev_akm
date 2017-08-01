package com.zongsheng.drink.h17.background.common;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.zongsheng.drink.h17.ComActivity;
import com.zongsheng.drink.h17.background.bean.UpateModel;
import com.zongsheng.drink.h17.common.SysConfig;

import java.util.List;


/**
 * 澳柯玛格子柜补货
 * Created by dongxiaofei on 2016/10/2.
 */

public class BuhuoForAKMTask extends AsyncTask<String, Integer, String> {
    MachineOrderRstListener machineOrderRstListener;
    ComActivity comActivity;
    List<UpateModel> roadList;
    int boxindex;
    public BuhuoForAKMTask(ComActivity comActivity, int boxindex, List<UpateModel> roadList, MachineOrderRstListener machineOrderRstListener) {
        this.comActivity = comActivity;
        this.roadList = roadList;
        this.machineOrderRstListener = machineOrderRstListener;
        this.boxindex = boxindex;
    }

    @Override
    protected String doInBackground(String... params) {
        for (UpateModel upateModel : roadList) {
            if(upateModel != null) {
                setCommand2VSI(upateModel);
            }
        }
        SystemClock.sleep(500);
        comActivity.getRoadEmptyInfo();
        return "";
    }

    //TestAsyncTask被后台线程执行后，被UI线程被调用，一般用于初始化界面控件，如进度条
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    //doInBackground执行完后由UI线程调用，用于更新界面操作
    @Override
    protected void onPostExecute(String result) {
        if (machineOrderRstListener != null) {
            if ("".equals(result)) {
                // 执行成功
                machineOrderRstListener.success();
            } else {
                // 执行失败
                machineOrderRstListener.fail();
            }
        }
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    private void setCommand2VSI(UpateModel upateModel){
        String str = "";
        switch (upateModel.getType()){
            case SysConfig.UPDATE_STOCK:
                 str = comActivity.machineBuHuo(boxindex, upateModel.getRoad_no());
                if (!"".equals(str)) {
                    SystemClock.sleep(1000);
                    str = comActivity.machineBuHuo(boxindex, upateModel.getRoad_no());
                    if (!"".equals(str)) {
                        Log.e("", "补货失败:" + str + "(" + upateModel.getRoad_no() + ")");
                        // 开门失败
                        return;
                    }
                }
                break;
            case SysConfig.UPDATE_PRICE:
                str = comActivity.setGeziChannelPrice(boxindex,upateModel.getRoad_no(),(int)(Double.parseDouble(upateModel.getPrice()) * 10));
                if (!"".equals(str)) {
                    SystemClock.sleep(1000);
                    str = comActivity.setGeziChannelPrice(boxindex,upateModel.getRoad_no(),(int)(Double.parseDouble(upateModel.getPrice()) * 10));
                    if (!"".equals(str)) {
                        return;
                    }
                }
                break;
            case SysConfig.UPDATE_STOCKANDPRICE:

                break;
            default:
                break;
        }
    }
}
