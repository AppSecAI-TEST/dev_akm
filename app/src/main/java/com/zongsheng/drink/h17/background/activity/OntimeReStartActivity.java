package com.zongsheng.drink.h17.background.activity;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.MarkLog;
import com.zongsheng.drink.h17.common.SharedPreferencesUtils;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.common.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.yolanda.nohttp.RequestMethod.HEAD;

/**
 * Created by 谢家勋 on 2016/9/14.
 */
public class OntimeReStartActivity extends Activity implements Observer{

    @BindView(R.id.rl_back)
    RelativeLayout rlBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.rl_top)
    RelativeLayout rlTop;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.iv_set_time)
    ImageView ivSetTime;
    @BindView(R.id.tv_how_long)
    TextView tvHowLong;
    @BindView(R.id.sw_isopen)
    Switch sw;
    @BindView(R.id.rel_time)
    RelativeLayout relTime;
    @BindView(R.id.btn_)
    Button btn;
    private int hour = 0;
    private int mint = 0;
    private SimpleDateFormat df;
    long time1;
    private String isFirst = "1";

    /** 是否开启 0:否 1:是 */
    private String isOpen;
    /** 保存的关机时间 00:00 */
    private String restartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ontime_restart);
        ButterKnife.bind(this);
        df = new SimpleDateFormat("HH:mm");
        MyObservable.getInstance().registObserver(this);
        isOpen = SharedPreferencesUtils.getParam(OntimeReStartActivity.this, "isOpen", "").toString();
        restartTime = SharedPreferencesUtils.getParam(OntimeReStartActivity.this, "restartTime", "").toString();
        if ("".equals(restartTime)) {
            time1 = System.currentTimeMillis();
            tvTime.setText(df.format(time1));
            hour = new Date().getHours();
            mint = new Date().getMinutes();
        } else {
            tvTime.setText(restartTime);
            try {
                hour = Integer.parseInt(restartTime.split(":")[0]);
                mint = Integer.parseInt(restartTime.split(":")[1]);
            } catch (Exception e) {}
        }
        if ("0".equals(isOpen) || "".equals(isOpen)) {
            sw.setChecked(false);
            relTime.setClickable(false);
        } else {
            sw.setChecked(true);
            relTime.setClickable(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyObservable.getInstance().unregistObserver(this);
    }

    @OnClick({R.id.rl_back, R.id.rel_time, R.id.btn_})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.rel_time:
                final TimePickerDialog dialog = new TimePickerDialog(OntimeReStartActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                        tvHowLong.setVisibility(View.VISIBLE);
                        time1 = System.currentTimeMillis();
//                        tvHowLong.setText(3+"小时"+11+"分钟后重启");
                        hour = hourOfDay;
                        mint = minute;
                        if (hourOfDay < 10) {
                            if (minute < 10) {
                                tvTime.setText("0" + hourOfDay + ":" + "0" + minute);
                            } else {
                                tvTime.setText("0" + hourOfDay + ":" + minute);
                            }
                        } else {
                            if (minute < 10) {
                                tvTime.setText(hourOfDay + ":" + "0" + minute);
                            } else {
                                tvTime.setText(hourOfDay + ":" + minute);
                            }
                        }

                    }
                }, hour, mint, true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();


                break;
            case R.id.btn_:
                boolean isTrue;
                if (sw.isChecked()) {
                    isTrue = true;
                } else {
                    isTrue = false;
                }
                String shut_time = "";
                String power_time = "";
                if(hour < 10){
                    if(mint < 10){
                        shut_time =  "0"+hour + ":" +"0"+mint; //自动关机时间
                    }else {
                        shut_time = "0"+hour + ":"+mint; //自动关机时间
                    }
                }else {
                    if(mint < 10){
                        shut_time = hour + ":" +"0"+mint; //自动关机时间
                    }else {
                        shut_time = hour + ":"+mint; //自动关机时间
                    }
                }

                if(hour < 10){
                    if(mint < 5){
                        power_time = "0"+hour + ":" + "0"+(mint+5); //自动开机时间
                    }else {
                        if(mint >= 55){
                            if(hour <9 ){
                                power_time = "0"+(hour+1) + ":" + "0" +(mint-55); //自动开机时间
                            }else {
                                power_time = (hour+1) + ":" + "0" +(mint-55); //自动开机时间
                            }
                        }else {
                            power_time = "0"+hour + ":" + (mint+5); //自动开机时间
                        }
                    }
                }else {
                    if(hour == 23){
                        if(mint < 5){
                            power_time = hour + ":" + "0"+(mint+5); //自动开机时间
                        }else {
                            if(mint >= 55){
                                power_time = "00" + ":" + "0" + (mint - 55); //自动开机时间
                            }else {
                                power_time = hour + ":" + (mint+5); //自动开机时间
                            }
                        }
                    }else {
                        if(mint < 5){
                            power_time = hour + ":" + "0"+(mint+5); //自动开机时间
                        }else {
                            if(mint >= 55){
                                power_time = (hour+1) + ":" + "0" + (mint - 55); //自动开机时间
                            } else {
                                power_time = hour + ":" + (mint+5); //自动开机时间
                            }
                        }
                    }
                }
                Log.i("111111", hour + "ww" + mint + "ww" + isTrue + " " + power_time + " " + shut_time);
                if (sw.isChecked()){
                    ToastUtils.showToast(OntimeReStartActivity.this, "设置重启成功");
                    //写入操作日志
                    MarkLog.markLog("饮料机" + ((MyApplication) getApplication()).getMachine_sn() + "开启定时重启,重启时间"
                            + shut_time, SysConfig.LOG_LEVEL_MIDDLE, ((MyApplication) getApplication()).getMachine_sn());
                    isFirst = "2";
                    SharedPreferencesUtils.setParam(OntimeReStartActivity.this, "isOpen", "1");
                    SharedPreferencesUtils.setParam(OntimeReStartActivity.this, "restartTime", shut_time);
                    SharedPreferencesUtils.setParam(OntimeReStartActivity.this, "powerTime", power_time);

                    Intent intent = new Intent("com.ubox.auto_power_shut");
                    intent.putExtra("effective", isTrue); //true 为启动此功能， false 为关闭此功能
                    intent.putExtra("power_time", power_time);
                    intent.putExtra("shut_time", shut_time);
                    sendBroadcast(intent);
                } else {
                    SharedPreferencesUtils.setParam(OntimeReStartActivity.this, "isOpen", "0");
                    ToastUtils.showToast(OntimeReStartActivity.this, "自动重启已关闭");
                    MarkLog.markLog("饮料机" + ((MyApplication) getApplication()).getMachine_sn() + "关闭定时重启", SysConfig.LOG_LEVEL_MIDDLE, ((MyApplication) getApplication()).getMachine_sn());
                }
                break;
        }
    }

    @OnClick(R.id.sw_isopen)
    public void onClick() {
        if (sw.isChecked()) {
            relTime.setClickable(true);
            btn.setClickable(true);
        } else {
            relTime.setClickable(false);
//            btn.setClickable(false);
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
