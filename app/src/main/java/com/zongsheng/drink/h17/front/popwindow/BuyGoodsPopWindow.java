package com.zongsheng.drink.h17.front.popwindow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.common.Constant;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.L;
import com.zongsheng.drink.h17.common.MyCountDownTimer;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.common.ToastUtils;
import com.zongsheng.drink.h17.common.aes.AESUtil;
import com.zongsheng.drink.h17.front.activity.BuyActivity;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.front.bean.PayMethod;
import com.zongsheng.drink.h17.front.bean.ShipmentModel;
import com.zongsheng.drink.h17.interfaces.IBuyGoodsPopWindowView;
import com.zongsheng.drink.h17.util.FileUtils;
import com.zongsheng.drink.h17.util.LogUtil;
import com.zongsheng.drink.h17.util.QRCodeUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * 购买商品POPWINDOW
 * Created by xunku on 16/9/7.
 */

public class BuyGoodsPopWindow extends PopupWindow implements IBuyGoodsPopWindowView, View.OnClickListener {

    private LayoutInflater layoutInflater;
    private Context context;
    private View view;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SysConfig.TIME_FORMAT_S);

    /**
     * 提示布局
     */
    private RelativeLayout rl_how;
    /**
     * 现金数
     */
    private TextView tvCashCount;

    /**
     * 商品图片
     */
    private ImageView iv_goods_img;
    /**
     * 商品名称
     */
    private TextView tv_goods_name;
    /**
     * 价格
     */
    private TextView tv_goods_price;
    /**
     * 二维码图片
     */
    private ImageView iv_qrcode;

    private RelativeLayout mRlNetError;

    /**
     * 商品信息
     */
    private GoodsInfo goodsInfo;

    /**
     * 倒计时布局
     */
    private TextView tv_second;
    /**
     * 倒计时计时器
     */
    private PageCloseTimer pageCloseTimer;

    /**
     * 投币提示
     */
    private ImageView iv_coin_put;
    /**
     * 出货成功
     */
    private ImageView iv_coin_success;

    /**
     * 非现金支付成功
     */
    private ImageView iv_phone_success;
    /**
     * 非现金支付布局
     */
    private RelativeLayout rl_show_phone;

    private ProgressBar progressBar;

    /**
     * 显示的网络支付方式
     */
    private GridLayout gd_payMethod;

    /**
     * 售货结束
     */
    private boolean soldOver = false;
    private boolean soleSuccess = false;

    private boolean is_qcode_error = false;
    /**
     * 选择类型 0:机器按钮选择 2:工控机选择
     */
    private String selectType = "";

    private LogUtil logUtil;

    public BuyGoodsPopWindow(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        logUtil = new LogUtil(this.getClass().getSimpleName());
        //这里控制是否打印Log
        logUtil.setShouldPrintLog(false);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        view = layoutInflater.inflate(R.layout.popwindow_buy_goods, null);//加载布局文件
        setContentView(view);

        // 设置dismiss监听
        setOnDismissListener(onDismissListener);
        initView();
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        setWidth(dm.widthPixels);

        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setOutsideTouchable(false);
    }

    /**
     * 初始化页面
     */
    private void initView() {
        /* 返回按钮 */
        RelativeLayout rl_back = (RelativeLayout) view.findViewById(R.id.rl_back);
        rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (soleSuccess) {
                        if (Integer.parseInt(tv_second.getText().toString().replace("秒", "")) > 7) {
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dismiss();
            }
        });

        /* 问号按钮 */
        ImageView ivWenHao = (ImageView) view.findViewById(R.id.iv_wenhao);
        ivWenHao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_how.setVisibility(View.VISIBLE);
            }
        });

        rl_how = (RelativeLayout) view.findViewById(R.id.rl_how);
        /* 关闭提示按钮 */
        ImageView iv_close_how = (ImageView) view.findViewById(R.id.iv_close_how);
        iv_close_how.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_how.setVisibility(View.GONE);
            }
        });

        rl_how.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_how.setVisibility(View.GONE);
            }
        });

        view.findViewById(R.id.rl_intro_detail).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }
        );

        tvCashCount = (TextView) view.findViewById(R.id.tv_cash_count);

        // 商品图片
        iv_goods_img = (ImageView) view.findViewById(R.id.iv_goods_img);
        // 商品名称
        tv_goods_name = (TextView) view.findViewById(R.id.tv_goods_name);
        // 价格
        tv_goods_price = (TextView) view.findViewById(R.id.tv_goods_price);
        // 二维码图片
        iv_qrcode = (ImageView) view.findViewById(R.id.iv_qrcode);

        mRlNetError = (RelativeLayout) view.findViewById(R.id.rl_net_error);

        //加载二维码的进度
        progressBar = (ProgressBar) view.findViewById(R.id.pb_probar);
        // 倒计时
        tv_second = (TextView) view.findViewById(R.id.tv_second);
        // 投币提示
        iv_coin_put = (ImageView) view.findViewById(R.id.iv_coin_put);
        // 出货成功
        iv_coin_success = (ImageView) view.findViewById(R.id.iv_coin_success);

        // 非现金支付成功
        iv_phone_success = (ImageView) view.findViewById(R.id.iv_phone_success);
        // 非现金支付布局
        rl_show_phone = (RelativeLayout) view.findViewById(R.id.rl_show_phone);
//        iv_qrcode.setOnClickListener(this);
        mRlNetError.setOnClickListener(this);

        //显示支持的网络支付方式
        gd_payMethod=(GridLayout)view.findViewById(R.id.gridLayout_payMethod);
        List<PayMethod> enabledPayMethods = MyApplication.getInstance().getEnabledPayMethod();
        setPayMethod(enabledPayMethods);
    }
    /**
     * 初始化显示支付图标的GridLayout
     * @param enabledPayMethods 支持的网络支付方式
     */
    private void setPayMethod(final List<PayMethod> enabledPayMethods){
        view.post(new Runnable() {
            @Override
            public void run() {
                int width=gd_payMethod.getMeasuredWidth();
                int height=gd_payMethod.getMeasuredHeight();

                int payMethodCount=enabledPayMethods.size();
                int itemWidth=width/payMethodCount;
                int itemHeight=height;
                int tempCount=0;
                logUtil.d("payMethod","width = "+width);
                logUtil.d("payMethod","itemWidth = "+itemWidth);
                //如果网络支付方式为0，应该显示提示信息
                if (payMethodCount==0){
                    TextView textView=new TextView(context);
                    textView.setText("没有支持的扫码支付");
                    GridLayout.Spec row= GridLayout.spec(0);
                    GridLayout.Spec col=GridLayout.spec(0);
                    GridLayout.LayoutParams layoutParams=new GridLayout.LayoutParams(row,col);
                    layoutParams.setGravity(Gravity.CENTER);
                    layoutParams.width= GridLayout.LayoutParams.WRAP_CONTENT;
                    layoutParams.height=itemHeight;
                    gd_payMethod.addView(textView,layoutParams);
                    return;
                }
                for (PayMethod payMethod : enabledPayMethods){
                    ImageView icon=new ImageView(context);
                    //对图片加载之前进行缩放，获取GridLayout高度，以此作为边长
                    //也可以使用Glide直接加载图片
                    Bitmap bitmap = decodeBitmapFromFile(FileUtils.getPayIconFullFilePath(payMethod.getId()),height,height);
                    icon.setImageBitmap(bitmap);
                    icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    GridLayout.Spec row= GridLayout.spec(0);
                    GridLayout.Spec col=GridLayout.spec(tempCount);
                    tempCount++;
                    GridLayout.LayoutParams layoutParams=new GridLayout.LayoutParams(row,col);
                    layoutParams.setGravity(Gravity.CENTER);
                    layoutParams.width=itemWidth;
                    layoutParams.height=itemHeight;

                    gd_payMethod.addView(icon,layoutParams);
                }

            }
        });
    }
    //加载图片之前进行缩放，这种方式可以减少Bitmap的内存占用
    private Bitmap decodeBitmapFromFile(String filePath,int requireWidth,int requireHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath,options);
        //计算缩放率
        int height = options.outHeight;
        int width = options.outWidth;
        //默认为1，表示不缩放
        int inSimpleSize = 1;
        if (height > requireHeight || width > requireWidth){
            int halfHeight = height/2;
            int halfWidth = width/2;
            while ((halfHeight/inSimpleSize >= requireHeight) && (halfWidth/inSimpleSize >= requireWidth)){
                inSimpleSize *= 2;
            }
        }
        options.inSampleSize = inSimpleSize;
        options.inJustDecodeBounds = false;
        logUtil.d("缩放率 = "+inSimpleSize);
        return BitmapFactory.decodeFile(filePath,options);
    }
    private OnDismissListener onDismissListener = new OnDismissListener() {
        @Override
        public void onDismiss() {
            iv_goods_img.setImageResource(R.drawable.ic_default_image);
            tv_goods_name.setText("");
            tv_goods_price.setText("");
            iv_qrcode.setImageDrawable(null);
            tv_second.setVisibility(View.GONE);
            tvCashCount.setText("已投入0元");
            if (pageCloseTimer != null) {
                pageCloseTimer.cancel();
            }
            // 关闭页面显示广告倒计时重新计时
            ((BuyActivity) context).resetPlayAdTimer();
            // 取消选中信息
            ((BuyActivity) context).cancelSelect(goodsInfo.getRoad_no());
            goodsInfo = null;
            selectType = "";
            rl_how.setVisibility(View.GONE);
        }
    };

    @Override
    public void NetWorkSuccess() {
        if (goodsInfo != null && iv_qrcode.getVisibility() == View.GONE && progressBar.getVisibility() == View.VISIBLE && mRlNetError.getVisibility() == View.GONE) {
            if (MyApplication.getInstance().isMQstate() && MyApplication.getInstance().isSnexist() && !MyApplication.getInstance().getMqIP().equals("") && MyApplication.getInstance().getUsedStatus().equals(SysConfig.IS_AUTHENTICATION)) {
                iv_qrcode.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                if (isNetworkAvailable(context)) {
                    is_qcode_error = false;
                    String time = simpleDateFormat.format(new Date());
                    String push_machine_sn;
                    String goods_belong;
                    if ("1".equals(goodsInfo.getGoodsBelong())) { // 1:主机 2:格子柜
                        push_machine_sn = MyApplication.getInstance().getMachine_sn();
                        goods_belong = "1";
                    } else if ("2".equals(goodsInfo.getGoodsBelong())) {
                        push_machine_sn = goodsInfo.getMachineID();
                        goods_belong = "2";
                    } else {
                        push_machine_sn = goodsInfo.getMachineID();
                        goods_belong = "3";
                    }

                    // 生成二维码
                    String order_sn = push_machine_sn + time;////yyyyMMddhhmmss
                    String price_encrypt = "";
                    try {
                        price_encrypt = AESUtil.encrypt((Double.parseDouble(goodsInfo.getPrice()) * 0.1) + "", SysConfig.PAY_AES_KEY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String qCodeUrl = SysConfig.PAY_NET_TEST_URL + "ms=" + MyApplication.getInstance().getMachine_sn()
                            + "&gc=" + goodsInfo.getGoodsCode() + "&gn=1&gi=" + goodsInfo.getGoodsID() + "&gb=" + goods_belong + "&gp=" + price_encrypt
                            + "&os=" + order_sn + "&pms=" + push_machine_sn + "&mt=" + time + "&op=1" + "&rn=" + goodsInfo.getRoad_no();
                    L.d(SysConfig.ZPush, "qCodeUrl------------------------------->" + qCodeUrl);
                    iv_qrcode.setImageBitmap(QRCodeUtil.createImage(qCodeUrl, 400, 400, null));
                } else {
                    iv_qrcode.setVisibility(View.GONE);
                    mRlNetError.setVisibility(View.VISIBLE);
                    is_qcode_error = true;
                }
            } else if (MyApplication.getInstance().getMqIP().equals("")) {
                String requestInterface = "api/machine/" + MyApplication.getInstance().getMachine_sn() + "/mqserverip?simCode=" + MyApplication.getInstance().getMachine_ccid();
                DataUtil.addBackGroundRequest(requestInterface, "", Constant.BACKGROUND_WHAT_MQ, true);
            } else if (!MyApplication.getInstance().isSnexist()) {
                ToastUtils.showToast((Activity) context, SysConfig.ERROR_INFO_NOEXIST);
            } else if (!MyApplication.getInstance().getUsedStatus().equals(SysConfig.IS_AUTHENTICATION)) {
                ToastUtils.showToast((Activity) context, SysConfig.ERROR_INFO_NOUSED);
            }
        }
    }

    @Override
    public void NetWorkError() {
        if (iv_qrcode.getVisibility() == View.GONE && progressBar.getVisibility() == View.VISIBLE && mRlNetError.getVisibility() == View.GONE) {
            progressBar.setVisibility(View.GONE);
            mRlNetError.setVisibility(View.VISIBLE);
            is_qcode_error = true;
        }
    }

    @Override
    public void setCashCount(int cashCount) {
        tvCashCount.setText("已投入" + String.valueOf(cashCount * 0.1) + "元");
        if (soldOver) {
            return;
        }
        // 判断钱够不够 够的话出货并退币
        if (cashCount >= (int) Double.parseDouble(goodsInfo.getPrice())) {
            Log.e("POP", "钱够了 出货吧" + goodsInfo.getPrice());
            // 出货
            if ("1".equals(goodsInfo.getGoodsBelong())) {// 1:主机 2:格子柜
                if ("0".equals(selectType)) {// 按钮选择的 机器自动处理
                    soldOver = true;
                    Log.e("POP", "钱够了 出货吧 按钮选择的 机器自动处理");
                    return;
                }
                soldOver = true;
                ((BuyActivity) context).saleByCash(Integer.parseInt(goodsInfo.getGoodsCode()));
            } else if ("2".equals(goodsInfo.getGoodsBelong())) {
                soldOver = true;
                // 请求主控扣费 扣费成功 请求格子柜出货
                ShipmentModel shipmentModel = new ShipmentModel();
                shipmentModel.setPay_type("0");
                shipmentModel.setMachine_sn(goodsInfo.getMachineID());
                shipmentModel.setGoods_belong(goodsInfo.getGoodsBelong());
                shipmentModel.setGoods_id(goodsInfo.getGoodsID());
                shipmentModel.setGoods_price(goodsInfo.getPrice());
                shipmentModel.setOrder_sn("");
                ((BuyActivity) context).costMoneyForCabinet(shipmentModel, (int) Double.parseDouble(goodsInfo.getPrice()), false);
            } else {
                soldOver = true;
                Log.e("POP", "" + goodsInfo.getRoad_no());
                ((BuyActivity) context).fuguiSaleTestByCash(goodsInfo.getRoad_no(), (int) Double.parseDouble(goodsInfo.getPrice()));
            }
        }
    }

    @Override
    public void setGoodsInfo(GoodsInfo goodsInfo, String selectType) {
        this.selectType = selectType;
        if (this.goodsInfo != null) {
            if (this.goodsInfo.getGoodsCode().equals(goodsInfo.getGoodsCode())) {
                return;
            }
        }
        soldOver = false;
        soleSuccess = false;

        this.goodsInfo = goodsInfo;
        // 图片
        Glide.with(context)
                .load(goodsInfo.getGoodsImage())
                .dontAnimate()
                .error(R.drawable.ic_default_image)
                .into(iv_goods_img);
        // 名称
        tv_goods_name.setText(goodsInfo.getGoodsName());
        // 价格
        tv_goods_price.setText("¥ " + Double.parseDouble(goodsInfo.getPrice()) * 0.1);
        iv_qrcode.setImageBitmap(null);
        rl_show_phone.setVisibility(View.VISIBLE);
        iv_phone_success.setVisibility(View.GONE);
        iv_qrcode.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        mRlNetError.setVisibility(View.GONE);

        if (pageCloseTimer != null) {
            pageCloseTimer.cancel();
        }
        /* 页面总时间 */
        long TOTAL_TIME = 46000;
        tv_second.setText(TOTAL_TIME / 1000 + "秒");
        tv_second.setVisibility(View.VISIBLE);
        pageCloseTimer = new PageCloseTimer(TOTAL_TIME, 1000);
        pageCloseTimer.start();

        tvCashCount.setVisibility(View.VISIBLE);
        iv_coin_put.setVisibility(View.VISIBLE);
        iv_coin_success.setVisibility(View.GONE);

    }

    @Override
    public void shipmentSuccessByCash() {
        Log.e("chuhuo", "出货成功");
        soldOver = true;
        soleSuccess = true;
        tvCashCount.setVisibility(View.GONE);
        iv_coin_put.setVisibility(View.GONE);
        iv_coin_success.setVisibility(View.VISIBLE);

        if (pageCloseTimer != null) {
            pageCloseTimer.cancel();
        }
        tv_second.setText(10 + "秒");
        tv_second.setVisibility(View.VISIBLE);
        pageCloseTimer = new PageCloseTimer(10000, 1000);
        pageCloseTimer.start();
    }

    @Override
    public void shipmentSuccessByNet() {
        soldOver = true;
        soleSuccess = true;
        rl_show_phone.setVisibility(View.GONE);
        iv_phone_success.setVisibility(View.VISIBLE);

        if (pageCloseTimer != null) {
            pageCloseTimer.cancel();
        }
        tv_second.setText(10 + "秒");
        tv_second.setVisibility(View.VISIBLE);
        pageCloseTimer = new PageCloseTimer(10000, 1000);
        pageCloseTimer.start();
    }

    @Override
    public void closeWindow() {
        if (isShowing()) {
            dismiss();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_net_error:
                if (is_qcode_error && mRlNetError.getVisibility() == View.VISIBLE) {
                    mRlNetError.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    ((BuyActivity) context).onClickAgain(view);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 计时器timer
     */
    private class PageCloseTimer extends MyCountDownTimer {
        PageCloseTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕
            if (isShowing()) {
                dismiss();
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
            tv_second.setText(millisUntilFinished / 1000 + "秒");//45000
            if (millisUntilFinished < 37000 && progressBar.getVisibility() == View.VISIBLE && iv_qrcode.getVisibility() == View.GONE) {
                NetWorkError();
            }
        }
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

}
