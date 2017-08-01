package com.zongsheng.drink.h17.background.activity;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.zongsheng.drink.h17.ComActivity;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.base.BasePresenter;
import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.adapter.TemperatureAdapter;
import com.zongsheng.drink.h17.common.ToastUtils;
import com.zongsheng.drink.h17.common.popupwindow.ActionItem;
import com.zongsheng.drink.h17.common.popupwindow.TitlePopup;
import com.zongsheng.drink.h17.observable.SerialObservable;


import java.util.Observable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 温度控制
 * Created by dxf on 2016/8/23.
 */
public class TemperatureActivty extends ComActivity{

    MyApplication application;
    AlertView alertView;
    @BindView(R.id.tv_submit)
    TextView tvSubmit;
    @BindView(R.id.tv_more)
    TextView tvMore;
    @BindView(R.id.rl_contrl_btn)
    RelativeLayout rlContrlBtn;
    @BindView(R.id.tv_temp_model)
    TextView tvTempModel;
    @BindView(R.id.tv_temp)
    TextView tvTemp;
    @BindView(R.id.lv_temp_choose)
    ListView lvTempChoose;
    @BindView(R.id.tv_temp_now)
    TextView tvTempNow;

    /** 温度模式 0：制冷 1：加热 */
    private String tempModel = "";
    /** 设定温度 */
    private String setTemp = "";

    private int[] coldTempList = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
    private int[] hotTempList = {10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30
                            ,31 ,32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49};

    private TemperatureAdapter adapter;
    private TitlePopup morePopup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        ButterKnife.bind(this);
        application = (MyApplication) getApplication();
        // 取得机器的温度设置信息
        getMachineTempInfo();
        MyObservable.getInstance().registObserver(this);
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyObservable.getInstance().unregistObserver(this);
    }

    /** 初始化页面 */
    private void initView() {
        if ("0".equals(tempModel)) { // 制冷
            tvTempModel.setText("设置模式：制冷");
            tvMore.setText("制冷");
        } else if ("1".equals(tempModel)) {
            tvTempModel.setText("设置模式：加热");
            tvMore.setText("加热");
        } else {
            tvTempModel.setText("设置模式：未知");
        }

        tvTemp.setText("设置温度：" + setTemp + "℃");
        // 初始化adapter
        if ("0".equals(tempModel)) {
            adapter = new TemperatureAdapter(this, coldTempList, Integer.parseInt(setTemp),tempModel);
        } else {
            adapter = new TemperatureAdapter(this, hotTempList, Integer.parseInt(setTemp),tempModel);
        }

        lvTempChoose.setAdapter(adapter);
        lvTempChoose.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int chooseTemp = (int) adapter.getItem(position);
                adapter.setChooseTemp(chooseTemp);
                adapter.notifyDataSetChanged();
                setTemp = String.valueOf(chooseTemp);
                tvTemp.setText("设置温度：" + setTemp + "℃");
            }
        });
    }

    @OnClick({R.id.rl_back, R.id.tv_more, R.id.tv_submit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_back:// 返回
                finish();
                break;
            case R.id.tv_more:
                morePopup = new TitlePopup(this, (int) (tvMore.getWidth() * 1.5), ViewGroup.LayoutParams.WRAP_CONTENT);
                morePopup.addAction(new ActionItem("制冷"));
                morePopup.addAction(new ActionItem("加热"));
                morePopup.setItemOnClickListener(new TitlePopup.OnItemOnClickListener() {
                    @Override
                    public void onItemClick(ActionItem item, int position2) {
                        switch (position2) {
                            case 0:// 制冷
                                if ("0".equals(tempModel)) {
                                    break;
                                }
                                tempModel = "0";
                                setTemp = "10";
                                initView();
                                break;
                            case 1:// 加热
                                if ("1".equals(tempModel)) {
                                    break;
                                }
                                tempModel = "1";
                                setTemp = "20";
                                initView();
                                break;
                            default:
                                break;
                        }
                    }
                });
                morePopup.setBackgroundDrawable(new BitmapDrawable());
                backgroundAlpha(0.5f);
                morePopup.show(tvMore);
                morePopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        backgroundAlpha(1f);
                    }
                });
                break;
            case R.id.tv_submit: // 提交
                tvSubmit.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        tvSubmit.setEnabled(true);
                    }
                }, 1000);
                // 设置温度
                String rts = setMachineTemp(tempModel, setTemp);
                if ("".equals(rts)) {
                    ToastUtils.showToast(TemperatureActivty.this, "设置成功");
                } else {
                    ToastUtils.showToast(TemperatureActivty.this, "设置失败，请重试");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }

    @Override
    public void onBackPressed() {
        //不允许右键退出,必须使用按钮退出
        //super.onPause();
    }

    @Override
    public void update(Observable observable, Object o) {
        if(observable instanceof SerialObservable){
            super.update(observable, o);
        }else if(observable instanceof MyObservable){
            finish();
        }
    }
}
