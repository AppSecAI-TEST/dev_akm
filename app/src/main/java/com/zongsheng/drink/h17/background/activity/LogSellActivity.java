package com.zongsheng.drink.h17.background.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.MarkLog;
import com.zongsheng.drink.h17.background.adapter.LogsSellAdapter;
import com.zongsheng.drink.h17.background.bean.LogSellInfo;
import com.zongsheng.drink.h17.common.MyListView;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.front.bean.PayModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * 销售日志
 * Created by 谢家勋 on 2016/8/23.
 */
public class LogSellActivity extends Activity implements Observer{

    @BindView(R.id.mlv)
    MyListView mlv;
    private List<LogSellInfo> logsInfoList = new ArrayList<>();
    private LogsSellAdapter adapter;
    /** 数据库 */
    private Realm realm;
//    private Map<String, String> map = new HashMap<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_sell);
        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();
        initView();
        MyObservable.getInstance().registObserver(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        }, 200);
        //写入操作日志
        MarkLog.markLog("查看销售记录", SysConfig.LOG_LEVEL_MIDDLE, ((MyApplication) getApplication()).getMachine_sn());
    }


    /**
     * 初始化
     */
    private void initView(){
        adapter = new LogsSellAdapter(this, logsInfoList);
        mlv.setAdapter(adapter);
    }
    /**
     * 初始化
     */
    private void initData(){
//        RealmResults<ShopGoods> goodsInfoList = realm.where(ShopGoods.class).findAll();
//        if(goodsInfoList.size() > 0 && goodsInfoList != null){
//            for (int i = 0; i <goodsInfoList.size() ; i++) {
//                map.put(goodsInfoList.get(i).getGoodsId(), goodsInfoList.get(i).getGoodsName());
//            }
//        }


        RealmResults<PayModel> results = realm.where(PayModel.class).findAll();
        results = results.sort("createTime", Sort.DESCENDING);
        for (int i = 0; i <results.size() ; i++) {
            LogSellInfo logsellInfo = new LogSellInfo();
            String recordInfo = results.get(i).getRecordInfo();
            String[] ss = recordInfo.split(",");
            if (ss.length < 8) {
                continue;
            }
            logsellInfo.setGoodsName(results.get(i).getGoodsName());

            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            String showTime = "";
            try {
                Date time = df.parse(ss[1]);
                df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                showTime = df.format(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            logsellInfo.setPayTime(showTime);
            switch (ss[2]){
                case "1"://现金
                    logsellInfo.setPayType("现金");
                    logsellInfo.setPrice(String.valueOf(Double.parseDouble(ss[3]) * 0.1));
                    break;
                case "49"://非现金
                    logsellInfo.setPayType("非现金");
                    logsellInfo.setPrice("(" + results.get(i).getGoodsPrice() + " / " + String.valueOf(Double.parseDouble(ss[4]) * 0.1) + ")");
                    break;
                default:
                    break;
            }
            logsInfoList.add(logsellInfo);
        }
        // 提交

        adapter.notifyDataSetChanged();
    }

    @OnClick(R.id.rl_back)
    public void onClick() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(realm != null){
            realm.close();
        }
        MyObservable.getInstance().unregistObserver(this);
    }

    @Override
    public void onBackPressed() {
        //不允许右键退出,必须使用按钮退出
        //super.onPause();
    }

    @Override
    public void update(Observable observable, Object o) {
        finish();
    }
}
