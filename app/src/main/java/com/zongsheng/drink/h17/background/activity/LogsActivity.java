package com.zongsheng.drink.h17.background.activity;

import android.app.Activity;
import android.os.Bundle;

import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.MarkLog;
import com.zongsheng.drink.h17.background.adapter.LogsAdapter;
import com.zongsheng.drink.h17.background.bean.LogsInfo;
import com.zongsheng.drink.h17.common.MyListView;
import com.zongsheng.drink.h17.common.SysConfig;

import java.util.ArrayList;
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
 * 操作日志
 * Created by 谢家勋 on 2016/8/23.
 */
public class LogsActivity extends Activity implements Observer{

    @BindView(R.id.mlv)
    MyListView mlv;
    private List<LogsInfo> logsInfoList = new ArrayList<>();
    private LogsAdapter adapter;
    private Realm realm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        ButterKnife.bind(this);
        MyObservable.getInstance().registObserver(this);
        realm = Realm.getDefaultInstance();
        initData();
        initView();
        //写入操作日志
        MarkLog.markLog("查看操作日志", SysConfig.LOG_LEVEL_MIDDLE, ((MyApplication) getApplication()).getMachine_sn());
    }

    /**
     * 初始化
     */
    private void initView(){
        adapter = new LogsAdapter(this, logsInfoList);
        mlv.setAdapter(adapter);
    }
    /**
     * 初始化
     */
    private void initData(){
        // 获取本地的操作日志
        RealmResults<LogsInfo> infos = realm.where(LogsInfo.class).findAll().sort("oprateTime", Sort.DESCENDING);

        logsInfoList = infos;

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
