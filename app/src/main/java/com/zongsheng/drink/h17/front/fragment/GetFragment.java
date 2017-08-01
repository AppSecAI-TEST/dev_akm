package com.zongsheng.drink.h17.front.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.common.LoadingUtil;
import com.zongsheng.drink.h17.common.MyCountDownTimer;
import com.zongsheng.drink.h17.front.activity.BuyActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 取货
 * Created by 谢家勋 on 2016/8/16.
 */
public class GetFragment extends Fragment {
    @BindView(R.id.edt_code)
    EditText edtCode;
    @BindView(R.id.tv_1)
    TextView tv1;
    @BindView(R.id.tv_2)
    TextView tv2;
    @BindView(R.id.tv_3)
    TextView tv3;
    @BindView(R.id.iv_delete)
    ImageView ivDelete;
    @BindView(R.id.tv_4)
    TextView tv4;
    @BindView(R.id.tv_5)
    TextView tv5;
    @BindView(R.id.tv_6)
    TextView tv6;
    @BindView(R.id.tv_clear)
    TextView tvClear;
    @BindView(R.id.tv_7)
    TextView tv7;
    @BindView(R.id.tv_8)
    TextView tv8;
    @BindView(R.id.tv_9)
    TextView tv9;
    @BindView(R.id.tv_star)
    TextView tvStar;
    @BindView(R.id.tv_0)
    TextView tv0;
    @BindView(R.id.tv_jing)
    TextView tvJing;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    private View view;
    private LayoutInflater inflater;

    private String code = "";
    /** 页面最大长度 */
    private int codeMaxLength = 10;
    /** 加载样式 */
    private Dialog dialog;
    /** 进度条timer */
    private DialogTimer dialogTimer;
    /** 验证结果dialog的timer */
    private ResultTimer resultTimer;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
            return view;
        }
        this.inflater = inflater;
        view = inflater.inflate(R.layout.fragment_get, null);
        ButterKnife.bind(this, view);

        return view;
    }

    @OnClick({R.id.tv_1, R.id.tv_2, R.id.tv_3, R.id.iv_delete, R.id.tv_4, R.id.tv_5, R.id.tv_6, R.id.tv_clear, R.id.tv_7, R.id.tv_8, R.id.tv_9, R.id.tv_star, R.id.tv_0, R.id.tv_jing, R.id.tv_confirm})
    public void onClick(View view) {
        // 无操作播放广告timer重新计时
        ((BuyActivity) getContext()).resetPlayAdTimer();

        switch (view.getId()) {
            case R.id.tv_1://1
                if (code.length() <= codeMaxLength){
                    code += "1";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_2://2
                if (code.length() <= codeMaxLength) {
                    code += "2";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_3://3
                if (code.length() <= codeMaxLength) {
                    code += "3";
                    edtCode.setText(code);
                }
                break;
            case R.id.iv_delete://退格
                if (code.length() > 0){
                    code = code.substring(0, code.length()-1);
                }
                edtCode.setText(code);
                break;
            case R.id.tv_4:// 4
                if (code.length() <= codeMaxLength) {
                    code += "4";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_5:// 5
                if (code.length() <= codeMaxLength) {
                    code += "5";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_6:// 6
                if (code.length() <= codeMaxLength) {
                    code += "6";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_clear:// 清空
                code = "";
                edtCode.setText(code);
                break;
            case R.id.tv_7:// 7
                if (code.length() <= codeMaxLength) {
                    code += "7";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_8:// 8
                if (code.length() <= codeMaxLength) {
                    code += "8";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_9:// 9
                if (code.length() <= codeMaxLength) {
                    code += "9";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_star:// 星号
                if (code.length() <= codeMaxLength) {
                    code += "＊";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_0:// 0
                if (code.length() <= codeMaxLength) {
                    code += "0";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_jing:// 井号
                if (code.length() <= codeMaxLength) {
                    code += "#";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_confirm:// TODO 确认
                if (code.length() == 0) {
                    return;
                }

                // 显示认证中
                //显示dialog正在发送 第一个0是加载图片还是加载进度，第二的0是默认的图片，最后一个是加载是否为动画
                dialog = LoadingUtil.createLoadingDialog(getContext(), "验证中...", 0, 0, true);
                dialog.show();
                // 五秒后显示验证成功
                if (dialogTimer != null) {
                    dialogTimer.cancel();
                }
                dialogTimer = new DialogTimer(1000, 2000);
                dialogTimer.start();

                break;
            default:
                break;
        }
    }

    /** 进度条计时器timer */
    class DialogTimer extends MyCountDownTimer {
        public DialogTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕
            dialog.dismiss();
            validFail();
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
        }
    }

    /** 进度条计时器timer */
    class ResultTimer extends MyCountDownTimer {
        public ResultTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
    
        @Override
        public void onFinish() {// 计时完毕
            dialog.dismiss();
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
        }
    }

    /** 验证失败 */
    private void validFail() {
        //显示dialog正在发送 第一个0是加载图片还是加载进度，第二的0是默认的图片，最后一个是加载是否为动画
        dialog = LoadingUtil.createLoadingDialog(getContext(), "取货码不正确!", 1, R.drawable.ic_quhuo_fail, false);
        dialog.show();
        // 五秒后显示验证成功
        if (resultTimer != null) {
            resultTimer.cancel();
        }
        resultTimer = new ResultTimer(1000, 1000);
        resultTimer.start();
        code = "";
        edtCode.setText(code);
    }


    /** 验证成功 */
    private void validSuccess() {
        // TODO 通讯主控 出货

        //显示dialog正在发送 第一个0是加载图片还是加载进度，第二的0是默认的图片，最后一个是加载是否为动画
        dialog = LoadingUtil.createLoadingDialog(getContext(), "取货成功!", 1, R.drawable.ic_quhuo_success, false);
        dialog.show();
        // 五秒后显示验证成功
        if (resultTimer != null) {
            resultTimer.cancel();
        }
        resultTimer = new ResultTimer(1000, 1000);
        resultTimer.start();
        code = "";
        edtCode.setText(code);
    }

    @Override
    public void onDestroy() {
        if (dialogTimer != null) {
            dialogTimer.cancel();
        }
        if (resultTimer != null) {
            resultTimer.cancel();
        }
        super.onDestroy();
    }

}
