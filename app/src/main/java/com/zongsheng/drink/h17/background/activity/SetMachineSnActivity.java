package com.zongsheng.drink.h17.background.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.common.HideKeyBoard;
import com.zongsheng.drink.h17.common.SharedPreferencesUtils;
import com.zongsheng.drink.h17.common.ToastUtils;

import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 设置机器编码
 * Created by dongxiaofei on 2016/9/29.
 */

public class SetMachineSnActivity extends Activity implements Observer{

    @BindView(R.id.rl_back)
    RelativeLayout rlBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.rl_top)
    RelativeLayout rlTop;
    @BindView(R.id.edt_machine_sn)
    EditText edtMachineSn;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_machine_sn);
        ButterKnife.bind(this);
        MyObservable.getInstance().registObserver(this);
        initData();
    }

    /** 初始化内容 */
    private void initData() {
        String machineSn = SharedPreferencesUtils.getParam(this,"machine_sn", "").toString();
        if (machineSn != null  && !"".equals(machineSn)) {
            edtMachineSn.setText(machineSn);
            edtMachineSn.setSelection(edtMachineSn.getText().toString().length());
        }
    }

    @OnClick (R.id.tv_confirm)
    void clickBtn() {
        // 非空判断
        String inputMachineSn = edtMachineSn.getText().toString().trim();
        if ("".equals(inputMachineSn)) {
            ToastUtils.showToast(SetMachineSnActivity.this, "请输入机器编码");
            return;
        }
        if (inputMachineSn.length() < 10) {
            ToastUtils.showToast(SetMachineSnActivity.this, "机器编码不足10位");
            return;
        }
        // 验证机器编码正确性
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (HideKeyBoard.isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    protected void onDestroy() {
        super.onDestroy();
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
