package com.zongsheng.drink.h17.background.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.service.BackBtnService;
import com.zongsheng.drink.h17.common.DataUtil;

import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 其他
 * Created by 谢家勋 on 2016/8/23.
 */
public class OtherActivty extends Activity implements Observer{

    AlertView alertView;
    @BindView(R.id.tv_temp_setting)
    TextView tvTempSetting;
    @BindView(R.id.tv_road_test)
    TextView tvRoadTest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        ButterKnife.bind(this);
        MyObservable.getInstance().registObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyObservable.getInstance().unregistObserver(this);
    }

    @OnClick({R.id.tv_log, R.id.tv_record, R.id.tv_info,
            R.id.rl_back, R.id.tv_ontime_restart, R.id.tv_show_setting, R.id.tv_set_machinesn
            , R.id.tv_temp_setting, R.id.tv_road_test})
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.tv_set_machinesn:// 设置机器编码
                intent.setClass(this, SetMachineSnActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_ontime_restart:// 定时重启
                intent.setClass(this, OntimeReStartActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_log:// 操作日志
                // 判断机器编号是否存在
                if ("".equals(MyApplication.getInstance().getMachine_sn())) {
                    alertView = new AlertView("提示", "机器编号未设定\n请确定机器配置文件是否正确放置\n处理完成后,请重启工控机\n如有疑问, 请联系客服!", null, new String[]{"确认"}, null,
                            this, AlertView.Style.Alert, DataUtil.dip2px(this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            alertView.dismiss();
                        }
                    }).setCancelable(false).setOnDismissListener(null);
                    alertView.show();
                    return;
                }
                intent.setClass(this, LogsActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_record:// 销售记录
                // 判断机器编号是否存在
                if ("".equals(MyApplication.getInstance().getMachine_sn())) {
                    alertView = new AlertView("提示", "机器编号未设定\n请确定机器配置文件是否正确放置\n处理完成后,请重启工控机\n如有疑问, 请联系客服!", null, new String[]{"确认"}, null,
                            this, AlertView.Style.Alert, DataUtil.dip2px(this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            alertView.dismiss();
                        }
                    }).setCancelable(false).setOnDismissListener(null);
                    alertView.show();
                    return;
                }
                intent.setClass(this, LogSellActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_info:// 信息
                intent.setClass(this, InfoActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_back:// 返回
                finish();
                break;
            case R.id.tv_show_setting:
                intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
                Intent ii = new Intent(this, BackBtnService.class);
                startService(ii);
                break;
            case R.id.tv_temp_setting:
                intent = new Intent(this, TemperatureActivty.class);
                startActivity(intent);
                break;
            case R.id.tv_road_test:
              /*  intent = new Intent(this, TestRoadForGuangOneActivty.class);
                startActivity(intent);*/
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent ii = new Intent(this, BackBtnService.class);
        stopService(ii);
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
