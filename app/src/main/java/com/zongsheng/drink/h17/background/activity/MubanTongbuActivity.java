package com.zongsheng.drink.h17.background.activity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.LoadingUtil;

import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 商品同步
 * Created by 谢家勋 on 2016/8/23.
 */
public class MubanTongbuActivity extends Activity implements OnItemClickListener,Observer{

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_to_pingtai)
    TextView tvToPingtai;

    private AlertView alertView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tongbu_goods);
        ButterKnife.bind(this);
        tvTitle.setText("模板同步");
        tvToPingtai.setVisibility(View.VISIBLE);
        MyObservable.getInstance().registObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyObservable.getInstance().unregistObserver(this);
    }

    @OnClick({R.id.rl_back, R.id.tv_pingtai, R.id.tv_from_usb, R.id.tv_to_usb, R.id.tv_to_pingtai})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.tv_pingtai:// 平台同步
                alertView = new AlertView("提示", "此操作可能消耗较多流量，确定继续吗？", "取消", new String[]{"确认"},
                        null, MubanTongbuActivity.this, AlertView.Style.Alert, DataUtil.dip2px(MubanTongbuActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), this).setCancelable(false).setOnDismissListener(null);
                alertView.show();
                break;
            case R.id.tv_from_usb:// 从U盘导入
                final Dialog dialogFromUsb = LoadingUtil.createLoadingDialog(this, "正在从U盘导入...", 1, R.drawable.ic_ios_juhua, true);
                dialogFromUsb.show();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //显示dialog
                        dialogFromUsb.dismiss();
                    }
                }, 3000);
                break;
            case R.id.tv_to_usb:// 导出至U盘
                final Dialog dialogToUsb = LoadingUtil.createLoadingDialog(this, "正在导出至U盘...", 1, R.drawable.ic_ios_juhua, true);
                dialogToUsb.show();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //显示dialog
                        dialogToUsb.dismiss();
                    }
                }, 3000);
                break;
            case R.id.tv_to_pingtai:// 同步到平台
                final Dialog dialogToPingtai = LoadingUtil.createLoadingDialog(this, "正在同步至平台...", 1, R.drawable.ic_ios_juhua, true);
                dialogToPingtai.show();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //显示dialog
                        dialogToPingtai.dismiss();
                    }
                }, 3000);
                break;
            default:
                break;
        }
    }
    @Override
    public void onItemClick(Object o, int position) {
        if (-1 == position) {
            alertView.dismiss();
        } else {
            // TODO: 2016/8/23
            alertView.dismiss();
            final Dialog dialogPingtai = LoadingUtil.createLoadingDialog(this, "正在从平台同步...", 1, R.drawable.ic_ios_juhua, true);
            dialogPingtai.show();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    //显示dialog
                    dialogPingtai.dismiss();
                }
            }, 3000);
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
