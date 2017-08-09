package com.zongsheng.drink.h17.front.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import com.zongsheng.drink.h17.ComActivity;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.base.BasePresenter;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.L;
import com.zongsheng.drink.h17.common.MyCountDownTimer;
import com.zongsheng.drink.h17.common.MyPhoneStateListener;
import com.zongsheng.drink.h17.common.MyVideoView;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.front.bean.AdInfo;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.front.bean.PayModel;
import com.zongsheng.drink.h17.front.bean.ShipmentModel;
import com.zongsheng.drink.h17.front.common.ShowBuyPageListener;
import com.zongsheng.drink.h17.front.fragment.DeskBuyFragment;
import com.zongsheng.drink.h17.front.fragment.GeziFragment;
import com.zongsheng.drink.h17.front.fragment.NewBuyFragment;
import com.zongsheng.drink.h17.front.popwindow.BuyGoodsPopWindow;
import com.zongsheng.drink.h17.interfaces.IBuyActivityInterface;
import com.zongsheng.drink.h17.interfaces.IBuyGoodsPopWindowView;
import com.zongsheng.drink.h17.interfaces.INetWorkRequInterface;
import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.observable.SerialObservable;
import com.zongsheng.drink.h17.presenter.BuyActivityPresenterImpl;
import com.zongsheng.drink.h17.presenter.IBuyActivityPresenter;
import com.zongsheng.drink.h17.service.LogUploadService;
import com.zongsheng.drink.h17.service.MachineFaultUploadService;
import com.zongsheng.drink.h17.service.SaleRecordUploadService;
import com.zongsheng.drink.h17.util.LogUtil;

import java.util.List;
import java.util.Observable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;


/**
 * 购买
 * Created by 谢家勋 on 2016/8/15.
 */
public class BuyActivity extends ComActivity<IBuyActivityInterface, BasePresenter<IBuyActivityInterface>> implements IBuyActivityInterface {

    private LogUtil logBuyAndShip;

    @BindView(R.id.iv_logo)
    ImageView ivLogo;
    @BindView(R.id.tv_code)
    TextView tvCode;
    @BindView(R.id.rl_top)
    RelativeLayout rlTop;
    @BindView(R.id.iv_close)
    ImageView ivClose;
    @BindView(R.id.iv_drink)
    ImageView ivDrink;
    @BindView(R.id.iv_get)
    ImageView ivGet;
    @BindView(R.id.iv_help)
    ImageView ivHelp;
    @BindView(R.id.rl_bottom)
    RelativeLayout rlBottom;
    @BindView(R.id.iv_xinhao)
    ImageView ivXinhao;
    @BindView(R.id.vv_show)
    MyVideoView vvShow;
    @BindView(R.id.rl_video)
    RelativeLayout flVideo;
    @BindView(R.id.iv_food)
    ImageView ivFood;
    @BindView(R.id.iv_show)
    ImageView ivShow;
    @BindView(R.id.btn_go_buy)
    Button btnGoBuy;
    @BindView(R.id.textClock)
    TextClock mTextClock;
    @BindView(R.id.iv_desk)
    ImageView ivDesk;

    private static final String TAG = "BuyActivity";
    /**
     * mFragments集合
     */
    private Fragment[] mFragments;
    /**
     * mFragments管理器
     */
    private FragmentManager fragmentManager;
    /**
     * 四个Fragment的提交
     */
    private FragmentTransaction fragmentTransaction;
    /**
     * 广告列表
     */
    private List<AdInfo> adlist;
    /**
     * 广告播放timer
     */
    private AdPlayTimer adPlayTimer;
    private TelephonyManager telephonyManager;
    private MyPhoneStateListener myPhoneStateListener;
    /**
     * 购买popwindow
     */
    private IBuyGoodsPopWindowView iBuyGoodsPopWindowView;
    /**
     * 播放广告倒计时
     */
    private PlayAdTimer playAdTimer;
    /**
     * 获取故障信息的timer
     */
    private MachineFaultTimer machineFaultTimer;
    /**
     * 获取故障信息的timer
     */
    private CheckQueBiTimer checkQueBiTimer;
    /**
     * 查询货道缺货状态Timer
     */
    private CheckRoadEmptyTimer checkRoadEmptyTimer;
    private IBuyActivityPresenter iBuyActivityPresenter;

    /**
     * 选中项目 1饮料，2柜子，3取货
     */
    private int selectType = 1;
    /**
     * 广告播放第几条
     */
    private int adPlayIndex = 0;
    /**
     * 视频播放判断用
     */
    private boolean videoPlayCheck = false;
    /**
     * 首页显示图标select
     */
    private int homePageIc_select;
    /**
     * 首页显示图标unselect
     */
    private int homePageIc_unselect;

    private INetWorkRequInterface iNetWorkRequInterface = null;

    int width;
    int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        logBuyAndShip = MyApplication.getInstance().getLogBuyAndShip();

        ButterKnife.bind(this);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        MyObservable.getInstance().registObserver(this);
        int roadCount = MyApplication.getInstance().getRoadCount();
        setRoadCount(roadCount);
        iBuyActivityPresenter = (IBuyActivityPresenter) presenter;
        iBuyActivityPresenter.bindListener(showBuyPageListener);
        // 设置需要获取机器的基本信息
        isNeedInitMachineInfo = false;
        //初始化时间
        initTextClock();
        // 初始化信号监听
        initTel();
        // 1:饮料机/其他综合机,货道数
        if ("1".equals(MyApplication.getInstance().getMachineType())) { // 饮料机
            homePageIc_select = R.drawable.ic_drink_selected;
            homePageIc_unselect = R.drawable.ic_drink_unselected;
        } else {
            homePageIc_select = R.drawable.ic_food_selected;
            homePageIc_unselect = R.drawable.ic_food_unselected;
        }
        // 初始化按钮
        initMenu();
        // 初始化动画效果
        initAnimation();
        // 初始化数据
        initData();

        // 启动上传服务
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                L.i(SysConfig.ZPush, "启动上传服务");
                Intent intent = new Intent();
                intent.setClass(BuyActivity.this, SaleRecordUploadService.class);
                startService(intent);

                intent = new Intent();
                intent.setClass(BuyActivity.this, MachineFaultUploadService.class);
                startService(intent);

                intent = new Intent();
                intent.setClass(BuyActivity.this, LogUploadService.class);
                startService(intent);

            }
        }, 8000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 注册推送reciver
                rigisterPush();
            }
        }, 500);
        // 启动获取故障信息timer
        new Thread(new Runnable() {
            @Override
            public void run() {
                machineFaultTimer = new MachineFaultTimer(30000, 30000);
                machineFaultTimer.start();

                // 检查缺币信息
                checkQueBiTimer = new CheckQueBiTimer(60000 * 2, 60000 * 2);
                checkQueBiTimer.start();

                // 检查货道缺货状态
                checkRoadEmptyTimer = new CheckRoadEmptyTimer(30000, 30000);
                checkRoadEmptyTimer.start();
            }
        }).run();
        iBuyActivityPresenter.setVersionNum();
    }

    private void initTextClock() {
        if (DateFormat.is24HourFormat(this)) {
            // 设置24时制显示格式
            mTextClock.setFormat24Hour("yyyy-MM-dd kk:mm");
        } else {
            // 设置12时制显示格式
            mTextClock.setFormat12Hour("yyyy-MM-dd hh:mm");
        }
    }

    @Override
    protected BasePresenter createPresenter() {
        return new BuyActivityPresenterImpl(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        tvCode.setText(MyApplication.getInstance().getMachine_sn() + SysConfig.VERSIONTAG);
        ivDrink.setImageResource(homePageIc_select);
        //如果后台设置中没有连接格子柜，则购买界面就不显示格子柜标签
        if (MyApplication.getInstance().getBindGeZis().size() > 0 && MyApplication.getInstance().getGeziList().size() > 0) {
            ivFood.setVisibility(View.VISIBLE);
        }

        if (MyApplication.getInstance().getBindDeskList() != null && MyApplication.getInstance().getBindDeskList().size() > 0) {
            ivDesk.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化动画
     */
    public void initAnimation() {
        // 底部文字动画
        final AlphaAnimation animationStart = new AlphaAnimation(0f, 1f);
        animationStart.setDuration(5000);
        final AlphaAnimation animationEnd = new AlphaAnimation(1f, 0f);
        animationEnd.setDuration(5000);
        animationEnd.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                btnGoBuy.startAnimation(animationStart);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animationStart.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                btnGoBuy.startAnimation(animationEnd);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        btnGoBuy.startAnimation(animationStart);
    }

    /**
     * 初始化信号监听
     */
    public void initTel() {
        if (myPhoneStateListener == null) {
            myPhoneStateListener = new MyPhoneStateListener(ivXinhao);
        }
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }


    /**
     * 初始化menu
     */
    private void initMenu() {
        mFragments = new Fragment[5];// 底部的5大模块
        fragmentManager = getSupportFragmentManager();
        mFragments[0] = fragmentManager.findFragmentById(R.id.fragment_drink);//
        ((NewBuyFragment) mFragments[0]).setBuyPageListener(showBuyPageListener);
        mFragments[1] = fragmentManager.findFragmentById(R.id.fragment_gezi);//
        ((GeziFragment) mFragments[1]).setBuyPageListener(showBuyPageListener);
        mFragments[2] = fragmentManager.findFragmentById(R.id.fragment_desk);//
        ((DeskBuyFragment) mFragments[2]).setBuyPageListener(showBuyPageListener);
        mFragments[3] = fragmentManager.findFragmentById(R.id.fragment_get);//
        mFragments[4] = fragmentManager
                .findFragmentById(R.id.fragment_help);//
        FramentcommitAllowingStateLoss();
        fragmentTransaction.show(mFragments[0])
                .commitAllowingStateLoss();// 5个模块提交
    }

    /**
     * tab的点击
     */
    private void tabClick(int tags) {
        // 无操作播放广告timer重新计时
        resetPlayAdTimer();

        FramentcommitAllowingStateLoss();
        fragmentTransaction.show(mFragments[tags]).commitAllowingStateLoss();
    }

    /**
     * 3个Fragment提交
     */
    private void FramentcommitAllowingStateLoss() {
        fragmentTransaction = fragmentManager.beginTransaction()
                .hide(mFragments[0]).hide(mFragments[1]).hide(mFragments[2]).hide(mFragments[3]).hide(mFragments[4]);// 四个模块
    }

    @Override
    protected void onStart() {
        // 启动广告
        //startPlayAds();
        super.onStart();
    }

    @OnClick({R.id.iv_close, R.id.iv_drink, R.id.iv_desk, R.id.iv_get, R.id.iv_help, R.id.rl_video, R.id.iv_food})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_close:// 关闭
//                tabClick(0);
                // 重设fragment
                resetFragementChoose();
                ((NewBuyFragment) mFragments[0]).setPagePosition(0);
                if (playAdTimer != null) {
                    playAdTimer.cancel();
                }
                startPlayAds();
                ivClose.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ivClose.setEnabled(true);
                    }
                }, 1000);
                break;
            case R.id.iv_drink:// 饮料
                if (selectType != 1) {
                    // 非饮料
                    selectType = 1;
                    // 改变margin和 图片
                    ivDrink.setImageResource(homePageIc_select);
                    ivFood.setImageResource(R.drawable.ic_gezi_unselected);
                    ivDesk.setImageResource(R.drawable.ic_desk_unselected);
                    ivGet.setImageResource(R.drawable.ic_get_unselected);
                    ivHelp.setImageResource(R.drawable.ic_question_unselected);

                    ivDrink.setPadding(0, 0, 0, 0);
                    ivGet.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    ivDesk.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    ivHelp.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    ivFood.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    tabClick(0);
                }
                break;
            case R.id.iv_food:// 格子柜
                if (selectType != 2) {
                    // 非取货
                    selectType = 2;
                    // 改变margin和 图片
                    ivDrink.setImageResource(homePageIc_unselect);
                    ivGet.setImageResource(R.drawable.ic_get_unselected);
                    ivFood.setImageResource(R.drawable.ic_gezi_selected);
                    ivDesk.setImageResource(R.drawable.ic_desk_unselected);
                    ivHelp.setImageResource(R.drawable.ic_question_unselected);
                    ivFood.setPadding(0, 0, 0, 0);
                    ivDrink.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    ivDesk.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    ivHelp.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    ivGet.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    tabClick(1);
                }
                break;

            case R.id.iv_desk://副柜
                if (selectType != 3) {
                    // 非取货
                    selectType = 3;
                    // 改变margin和 图片
                    ivDrink.setImageResource(homePageIc_unselect);
                    ivGet.setImageResource(R.drawable.ic_get_unselected);
                    ivFood.setImageResource(R.drawable.ic_gezi_unselected);
                    ivDesk.setImageResource(R.drawable.ic_desk_selected);
                    ivHelp.setImageResource(R.drawable.ic_question_unselected);
                    ivFood.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    ivDrink.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    ivDesk.setPadding(0, 0, 0, 0);
                    ivHelp.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    ivGet.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    tabClick(2);
                }
                break;

            case R.id.iv_get:// 取货
                if (selectType != 4) {
                    // 非取货
                    selectType = 4;
                    // 改变margin和 图片
                    ivDrink.setImageResource(homePageIc_unselect);
                    ivGet.setImageResource(R.drawable.ic_get_selected);
                    ivFood.setImageResource(R.drawable.ic_gezi_unselected);
                    ivDesk.setImageResource(R.drawable.ic_desk_unselected);
                    ivHelp.setImageResource(R.drawable.ic_question_unselected);
                    ivGet.setPadding(0, 0, 0, 0);
                    ivDrink.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    ivDesk.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    ivHelp.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    ivFood.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    tabClick(3);
                }
                break;
            case R.id.iv_help:// 帮助
                if (selectType != 5) {
                    // 非帮助
                    selectType = 5;
                    // 改变margin和 图片
                    ivDrink.setImageResource(homePageIc_unselect);
                    ivGet.setImageResource(R.drawable.ic_get_unselected);
                    ivHelp.setImageResource(R.drawable.ic_question_selected);
                    ivDesk.setImageResource(R.drawable.ic_desk_unselected);
                    ivFood.setImageResource(R.drawable.ic_gezi_unselected);
                    ivHelp.setPadding(0, 0, 0, 0);
                    ivGet.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    ivDrink.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    ivFood.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    ivDesk.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
                    tabClick(4);
                }
                break;
            case R.id.rl_video:// 视频
                closeAd(true);
                break;
            default:
                break;
        }
    }

    /**
     * 重设fragment选中
     */
    private void resetFragementChoose() {
        if (selectType != 1) {
            // 非饮料
            selectType = 1;
            // 改变margin和 图片
            ivDrink.setImageResource(R.drawable.ic_drink_selected);
            ivFood.setImageResource(R.drawable.ic_gezi_unselected);
            ivGet.setImageResource(R.drawable.ic_get_unselected);
            ivDesk.setImageResource(R.drawable.ic_desk_unselected);
            ivHelp.setImageResource(R.drawable.ic_question_unselected);
            ivDrink.setPadding(0, 0, 0, 0);
            ivGet.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
            ivHelp.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
            ivFood.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);
            ivDesk.setPadding(0, DataUtil.dip2px(this, 6), 0, 0);

            FramentcommitAllowingStateLoss();
            try {
                fragmentTransaction.show(mFragments[0]).commitAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * fragement传递result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFragments[0].onActivityResult(requestCode, resultCode, data);
        mFragments[1].onActivityResult(requestCode, resultCode, data);
        mFragments[2].onActivityResult(requestCode, resultCode, data);
        mFragments[3].onActivityResult(requestCode, resultCode, data);
        mFragments[4].onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeAd(false);
        telephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPlayAds();
        telephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    /***
     * 开始播放广告
     */
    private void startPlayAds() {
        adPlayIndex = 0;
        if (adPlayTimer != null) {
            adPlayTimer.cancel();
        }
        if (adlist == null) {
            adlist = MyApplication.getInstance().getAdFileList();
        }
        if (adlist.size() == 0) { // 没有广告, 不播放
            flVideo.setVisibility(View.GONE);
            vvShow.setVisibility(View.GONE);
            ivShow.setVisibility(View.GONE);
            return;
        }
        // 重设fragment
        resetFragementChoose();
        // 播放广告
        playAD();
    }

    private Bitmap bitmap;

    /**
     * 播放广告
     */
    private void playAD() {
        AdInfo adInfo = adlist.get(adPlayIndex);
        if (adInfo != null) {
            if ("1".equals(adInfo.getAdType())) { // 播放图片
                vvShow.stopPlayback();
                flVideo.setVisibility(View.VISIBLE);
//                vvShow.setVisibility(View.GONE);
                ivShow.setVisibility(View.VISIBLE);
                //从本地取图片
                if (bitmap != null) {
                    bitmap.recycle();
                }
                try {
                    bitmap = DataUtil.getLoacalBitmap(adInfo.getAdPath(), width, height);
                } catch (Exception e) {
                    bitmap = null;
                }
                if (bitmap != null) {
                    ivShow.setImageBitmap(bitmap);    //设置Bitmap
                }
                if (adPlayTimer == null) {
                    adPlayTimer = new AdPlayTimer(SysConfig.IMAGE_AD_PALY_TIME, SysConfig.IMAGE_AD_PALY_TIME);
                }
                adPlayTimer.start();
            } else { // 播放视频
                vvShow.setMediaController(null);
                vvShow.setVideoURI(Uri.parse(adInfo.getAdPath()));
                vvShow.start();
                if (videoPlayCheck) {
                    flVideo.setVisibility(View.VISIBLE);
                    vvShow.setVisibility(View.VISIBLE);
                    ivShow.setVisibility(View.GONE);
                    videoPlayCheck = false;
                }
                vvShow.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
//                        Log.e(TAG, "vvShow OnPrepared");
                        flVideo.setVisibility(View.VISIBLE);
                        vvShow.setVisibility(View.VISIBLE);
                        ivShow.setVisibility(View.GONE);
                    }
                });
                vvShow.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                continuePalyAds();
                            }
                        }, 500);
                        return true;
                    }
                });
                vvShow.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) { // 播放结束
                        continuePalyAds();
                    }
                });
            }
        } else {
            flVideo.setVisibility(View.GONE);
            vvShow.setVisibility(View.GONE);
            ivShow.setVisibility(View.GONE);
        }
    }

    /**
     * 继续播放广告
     */
    private void continuePalyAds() {
        if (adPlayTimer != null) {
            adPlayTimer.cancel();
        }
        adPlayIndex++;
        if (adPlayIndex >= adlist.size()) {
            adPlayIndex = 0;
        }
        playAD();
    }

    @Override
    public void closeAd(boolean isResetTimer) {
        flVideo.setVisibility(View.GONE);
        vvShow.setVisibility(View.GONE);
        ivShow.setVisibility(View.GONE);
        vvShow.stopPlayback();
        videoPlayCheck = true;
        if (adPlayTimer != null) {
            adPlayTimer.cancel();
        }

        // 无操作播放广告timer重新计时
        if (isResetTimer) {
            resetPlayAdTimer();
        }
    }

    @Override
    public void onClickAgain(View view) {
        iBuyActivityPresenter.initData();
    }


    @Override
    public String swtchPayType(String payType) {
        return switchPayType(payType);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof SerialObservable) {
            super.update(observable, o);
        } else if (observable instanceof MyObservable) {
            finish();
        }
    }


    /**
     * 广告播放timer
     */
    private class AdPlayTimer extends MyCountDownTimer {
        AdPlayTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕
            // 继续播放广告
            continuePalyAds();
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
        }
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed()");
        //不允许右键退出,必须使用按钮退出
        //super.onPause();
    }

    @Override
    protected void onDestroy() {
        try {
            if (playAdTimer != null) {
                playAdTimer.cancel();
                playAdTimer = null;
            }
            if (adPlayTimer != null) {
                adPlayTimer.cancel();
                adPlayTimer = null;
            }
            if (iNetWorkRequInterface != null) {
                iNetWorkRequInterface.cancel();
                iNetWorkRequInterface = null;
            }
            if (machineFaultTimer != null) {
                machineFaultTimer.cancel();
                machineFaultTimer = null;
            }
            if (checkQueBiTimer != null) {
                checkQueBiTimer.cancel();
                checkQueBiTimer = null;
            }
            if (checkRoadEmptyTimer != null) {
                checkRoadEmptyTimer.cancel();
                checkRoadEmptyTimer = null;
            }
            if (iBuyActivityPresenter != null) {
                iBuyActivityPresenter.cancel();
                iBuyActivityPresenter = null;
            }
            if (iBuyGoodsPopWindowView != null) {
                iBuyGoodsPopWindowView.closeWindow();
                iBuyGoodsPopWindowView = null;
            }
            unRegisterPhoneState();
            unregisterReceiver(payDataReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MyObservable.getInstance().unregistObserver(this);
        super.onDestroy();
    }

    //取消监听，防止内存泄露
    private void unRegisterPhoneState() {
        if (myPhoneStateListener != null && telephonyManager != null) {
            myPhoneStateListener = null;
            telephonyManager = null;
        }
    }

    /**
     * 显示购买页面popwindow
     */
    private ShowBuyPageListener showBuyPageListener = new ShowBuyPageListener() {
        @Override
        public void showBuyPage(GoodsInfo goodsInfo, String selectType) {
            try {
                // 无操作播放广告timer停止计时
                if (playAdTimer != null) {
                    playAdTimer.cancel();
                }
                if (iBuyGoodsPopWindowView == null) {
                    iBuyGoodsPopWindowView = new BuyGoodsPopWindow(BuyActivity.this);
                    iBuyActivityPresenter.bindPopView(iBuyGoodsPopWindowView);
                }
                if (!((BuyGoodsPopWindow) iBuyGoodsPopWindowView).isShowing()) {
                    ((BuyGoodsPopWindow) iBuyGoodsPopWindowView).showAsDropDown(rlTop);
                }
                iBuyActivityPresenter.initData();
                // 设置页面商品信息
                iBuyGoodsPopWindowView.setGoodsInfo(goodsInfo, selectType);
                // 取得用户投币数
                request_get_status_intrymoney();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    @Override
    public void cancelSelect(int roadNo) {
        if (iBuyActivityPresenter != null) {
            iBuyActivityPresenter.close();
        }
    }

    /**
     * 显示硬币数
     */
    @Override
    protected void showCashCount(int count) {
        if (iBuyGoodsPopWindowView != null && ((BuyGoodsPopWindow) iBuyGoodsPopWindowView).isShowing()) {
            iBuyGoodsPopWindowView.setCashCount(count);
        } else {
            closeAd(true);
        }
    }

    /**
     * 按钮选择商品
     */
    @Override
    protected void selectGoodByButton(String goodsRoad, String goodsCode) {
        for (GoodsInfo goodsInfo : MyApplication.getInstance().getGoodsInfos()) {
            if (goodsInfo.getGoodsCode().equals(goodsCode)) {
                closeAd(true);
                showBuyPageListener.showBuyPage(goodsInfo, "0");
                break;
            }
        }
    }

    /**
     * 无操作播放广告计时器timer
     */
    private class PlayAdTimer extends MyCountDownTimer {
        PlayAdTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕
            if (flVideo.getVisibility() == View.GONE) {
                MyApplication.getInstance().getLogInit().d("计时完毕 开始播放广告");
                startPlayAds();
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
        }
    }

    /**
     * 取得故障信息的timer
     */
    private class MachineFaultTimer extends MyCountDownTimer {
        MachineFaultTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕
            Log.e(TAG, "开始获取故障信息!");
            getMachineFault();
            if (machineFaultTimer != null) {
                machineFaultTimer.start();
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
        }
    }

    /**
     * 查询是否缺币的timer
     */
    private class CheckQueBiTimer extends MyCountDownTimer {
        CheckQueBiTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕
            Log.e(TAG, "开始获取缺币信息!");
            // TODO 获取缺币信息
            getQueBi();
            if (checkQueBiTimer != null) {
                checkQueBiTimer.start();
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
        }
    }

    /**
     * 检测货道是否售空的timer
     */
    private class CheckRoadEmptyTimer extends MyCountDownTimer {
        CheckRoadEmptyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕
            Log.e(TAG, "开始获取货到缺货信息!");
            getRoadEmptyInfo();
            if (checkRoadEmptyTimer != null) {
                checkRoadEmptyTimer.start();
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
        }
    }

    /**
     * 广告播放倒计时重新计时
     */
    public void resetPlayAdTimer() {
        MyApplication.getInstance().logInit.d(TAG, "广告播放倒计时重新计时 60s");
        if (playAdTimer != null) {
            playAdTimer.cancel();
        }
        playAdTimer = new PlayAdTimer(SysConfig.L_REQ_AG_TIME_60, SysConfig.L_REQ_AG_TIME_60);
        playAdTimer.start();
    }

    /**
     * 出货成功 (出货货道, 产品编号, 出货量)
     */
    @Override
    protected void shipmentSuccess(String huodao, String goodsCode, String goodsCount,
                                   String machineQueryType, String saleOrderID, int boxIndex, String saleTime, String trade_no) {
        iBuyActivityPresenter.shipmentSuccess(huodao, goodsCode, goodsCount, machineQueryType, saleOrderID, boxIndex, saleTime, trade_no);
    }

    private void rigisterPush() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SysConfig.PUSH_MESSAGE_ACTION);
        registerReceiver(payDataReceiver, filter);
    }

    /**
     * 接收支付推送消息
     */
    public BroadcastReceiver payDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("data");
            String type = intent.getStringExtra("type");
            if ("2".equals(type)) {  // 推送消息
                if (data == null) {
                    data = "";
                }
//                L.d(TAG, "收到推送消息:" + data);
                if (iBuyActivityPresenter == null) {
                    iBuyActivityPresenter = (IBuyActivityPresenter) createPresenter();
                }
                iBuyActivityPresenter.receiverMQMsg(data);
            }
        }
    };

    // 机器缺币 status 01:5角 02:1元
    @Override
    protected void machineQueB(String machineID, String fiveStatus, String oneStatus) {
        if (iBuyActivityPresenter != null) {
            iBuyActivityPresenter.machineQueB(machineID, fiveStatus, oneStatus);
        }
    }

    /**
     * 主柜出货成功并更改本地库存
     */
    @Override
    public void updateLocalKuCun(String road_no) {
        iBuyActivityPresenter.updateLocalKuncun(road_no);
    }

    /**
     * 副柜出货成功并更改本地库存
     * @param road_no 出货货道号
     */
    @Override
    protected void updateDeskKucun(String road_no) {
//        Log.e(TAG, "更新库存:" + road_no);
        MyApplication.getInstance().getLogBuyAndShip().d("副柜更新本地库存 : 货道号 = "+road_no);
        RealmResults<GoodsInfo> goodsInfos = realm.where(GoodsInfo.class).equalTo("goodsBelong", "3")
                .equalTo("road_no", Integer.parseInt(road_no)).findAll();
        final GoodsInfo goodsInfo = goodsInfos.where().findFirst();
        if (goodsInfo != null && goodsInfo.getKuCun() != null && !"".equals(goodsInfo.getKuCun())) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    //TODO:这里不对，副柜库存不会保留一件商品，应该直接减1
//                    if (Integer.parseInt(goodsInfo.getKuCun()) >= 2) {
//                        goodsInfo.setKuCun(String.valueOf(Integer.parseInt(goodsInfo.getKuCun()) - 1));
//                    }
//                    if (goodsInfo.getOnlineKuCun() >= 2) {
//                        goodsInfo.setOnlineKuCun(goodsInfo.getOnlineKuCun() - 1);
//                    }
                    goodsInfo.setKuCun(String.valueOf(Integer.parseInt(goodsInfo.getKuCun())-1));
                    goodsInfo.setOnlineKuCun(goodsInfo.getOnlineKuCun()-1);
                }
            });
        }
    }

    /**
     * 格子柜出货前扣费请求
     */
    public void costMoneyForCabinet(final ShipmentModel shipmentModel, final int money, final boolean isOnLine) {
        iBuyActivityPresenter.costMoneyForCabinet(shipmentModel, money, isOnLine);
    }

    /**
     * 格子柜出货
     */
    public void geziSaleOut() {

    }

    /**
     * 在线支付成功并插入数据库
     */
    @Override
    protected void onlinePaySend2MQ(PayModel payModel, String orderSn, String payType, String DeliveryStatus) {
        iBuyActivityPresenter.onlinePaySend2MQ(payModel, orderSn, payType, DeliveryStatus);
    }

    /**
     * 更新页面商品库存信息
     */
    @Override
    protected void updateGoodsInfo() {
        ((NewBuyFragment) mFragments[0]).updateGoodsInfo();
    }

    /**
     * 更新格子柜库存信息
     */
    @Override
    protected void updateCabinetGoodsInfo() {
        ((GeziFragment) mFragments[1]).updateGoodsInfo();
    }

    /**
     * 现金出货
     */
    public void saleByCash(final int goodsCode) {
        iBuyActivityPresenter.saleByCash(goodsCode);
    }
}
