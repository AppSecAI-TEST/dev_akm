package com.zongsheng.drink.h17.background.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.R;

import java.util.Observable;
import java.util.Observer;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 同步管理
 * Created by 谢家勋 on 2016/8/23.
 */
public class TongbuManagerActivity extends Activity implements Observer{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tongbu);
        ButterKnife.bind(this);
        MyObservable.getInstance().registObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyObservable.getInstance().unregistObserver(this);
    }

    @OnClick({R.id.rl_back, R.id.tv_shangpin, R.id.tv_huodao,  R.id.tv_ad})
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.tv_shangpin:// 商品同步
                intent.setClass(this, GoodsTongbuActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_huodao:// 货道同步
                intent.setClass(this, HuodaoTongbuActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_ad:// 广告同步
                intent.setClass(this, GuangGaoActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
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
