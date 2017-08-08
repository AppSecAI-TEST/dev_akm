package com.zongsheng.drink.h17.background.common;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.zongsheng.drink.h17.ComActivity;
import com.zongsheng.drink.h17.background.bean.UpateModel;
import com.zongsheng.drink.h17.common.SysConfig;

import java.util.List;


/**
 * 机器命令task
 * Created by dongxiaofei on 2016/10/2.
 */

public class MachineOrderTask extends AsyncTask<String, Integer, String> {
    ComActivity activity;
    int roadCnt;
    private List<UpateModel> upateModels;
    MachineOrderRstListener machineOrderRstListener;
    public MachineOrderTask(ComActivity activity, int roadCnt, MachineOrderRstListener machineOrderRstListener, List<UpateModel> upateModels) {
        this.activity = activity;
        this.upateModels = upateModels;
        this.roadCnt = roadCnt;
        this.machineOrderRstListener = machineOrderRstListener;
    }

    @Override
    protected String doInBackground(String... params) {
        String rst = "";

        // 澳柯玛机器不设置货道编号
        for(UpateModel upateModel : upateModels){
            if(upateModel != null) {
                rst = setCommand2VSI(upateModel);
            }
            SystemClock.sleep(500);
        }
        return rst;
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

    private String  setCommand2VSI(UpateModel upateModel){
        String res = "";
        switch (upateModel.getType()){
            case SysConfig.UPDATE_PRICE:
                res = activity.requestConfigindCashPrice((int)Double.parseDouble(upateModel.getPrice()) * 10 ,upateModel.getBoxindex(),upateModel.getRoad_no());
                if (!"".equals(res)) {
                    SystemClock.sleep(1000);
                    res = activity.requestConfigindCashPrice((int)Double.parseDouble(upateModel.getPrice()) * 10 ,upateModel.getBoxindex(),upateModel.getRoad_no());
                    return res;
                }
//                Log.e("", "货道price结果:" + res);
                break;
            case SysConfig.GOODSCODE://澳柯玛机器不设置商品编码
                break;
            default:
                break;
        }
        return res;
    }
}
