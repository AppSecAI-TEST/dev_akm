package com.zongsheng.drink.h17.front.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zongsheng.drink.h17.ComActivity;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.common.ToastUtils;
import com.zongsheng.drink.h17.front.activity.BuyActivity;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.front.common.ShowBuyPageListener;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

/**
 * Created by torry on 2017/6/23.
 * 澳柯玛副柜功能添加
 */

public class DeskBuyFragment extends Fragment {
    @BindView(R.id.edt_code)
    EditText edtCode;
    @BindView(R.id.tv_1)
    TextView tv1;
    @BindView(R.id.tv_2)
    TextView tv2;
    @BindView(R.id.tv_3)
    TextView tv3;
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
    @BindView(R.id.tv_0)
    TextView tv0;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;

    private View view;
    private LayoutInflater inflater;

    private String code = "";
    /**
     * 页面最大长度
     */
    private int codeMaxLength = 10;
    /**
     * 加载样式
     */
    private Dialog dialog;

    ShowBuyPageListener showBuyPageListener;
    List<Integer> roadNoList;
    private Realm realm;
    Map<Integer, GoodsInfo> goodsInfoMap;

    public DeskBuyFragment() {
    }

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
        realm = Realm.getDefaultInstance();
        this.inflater = inflater;
        view = inflater.inflate(R.layout.fragment_desk_buy, null);
        ButterKnife.bind(this, view);
        initDeskRoad();
        return view;
    }

    private void initDeskRoad() {
        roadNoList = MyApplication.getInstance().getDeskRoadList();
        goodsInfoMap = MyApplication.getInstance().getDeskGoodsInfo();
    }

    @OnClick({R.id.tv_1, R.id.tv_2, R.id.tv_3, R.id.tv_4, R.id.tv_5, R.id.tv_6, R.id.tv_clear, R.id.tv_7, R.id.tv_8, R.id.tv_9, R.id.tv_0, R.id.tv_confirm})
    public void onClick(View view) {
        // 无操作播放广告timer重新计时
        ((BuyActivity) getContext()).resetPlayAdTimer();

        switch (view.getId()) {
            case R.id.tv_1://1
                if (code.length() <= codeMaxLength) {
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
            case R.id.tv_0:// 0
                if (code.length() <= codeMaxLength) {
                    code += "0";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_confirm://
//                Log.e("isConn", MyApplication.getInstance().getDeskConnState() + "");
                if (MyApplication.getInstance().getDeskConnState()) {
                    if (code.length() > 2) {
                        ToastUtils.showToast((Activity) this.getContext(), "货道号最多不超过3位！");
                    } else if (code.equals("") || code.length() == 0) {
                        ToastUtils.showToast((Activity) this.getContext(), "货道号不能为空！");
                    } else if (Integer.parseInt(code) > 66 || Integer.parseInt(code) < 11) {
                        ToastUtils.showToast((Activity) this.getContext(), "无效货道！");
                    } else {
                        int roadNum = Integer.parseInt(code);
                        if (goodsInfoMap.containsKey(roadNum)) {
                            GoodsInfo goodsInfo1 = goodsInfoMap.get(roadNum);
                            MyApplication.getInstance().getLogBuyAndShip().d("==================购买流程==================");
                            MyApplication.getInstance().getLogBuyAndShip().d("副柜购货 = 商品 : "+goodsInfo1.getGoodsName()+" ; 货道号 : "+roadNum);
                            //TODO:这里判断本地库存是否有货
                            if (goodsInfo1.getKuCun().equals("0")){
                                //如果库存为0，说明该货道已经售空
                                ToastUtils.showToast((Activity) this.getContext(), "该货道已售空，请选择其它货道");
                            }else {
                                //如果货道有货，开始购买支付流程
                                selectGoodInfo(goodsInfo1);
                            }
                        } else {
                            MyApplication.getInstance().getLogBuyAndShip().d("货道号 = "+code+" 未被启用");
                            ToastUtils.showToast((Activity) this.getContext(), "该货道未被启用！");
                        }
                    }
                } else {
                    MyApplication.getInstance().getLogBuyAndShip().d("副柜未连接");
                    ToastUtils.showToast((Activity) this.getContext(), "副柜机器未连接！");
                }

                code = "";
                edtCode.setText(code);

                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setBuyPageListener(ShowBuyPageListener showBuyPageListener) {
        this.showBuyPageListener = showBuyPageListener;
    }

    public void selectGoodInfo(GoodsInfo goodsInfo) {
        if ("1".equals(goodsInfo.getIsSoldOut())) {
            return;
        }
        if (showBuyPageListener != null) {
            showBuyPageListener.showBuyPage(goodsInfo, "1");
        }
    }
}
