package com.zongsheng.drink.h17.front.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.front.activity.BuyActivity;
import com.zongsheng.drink.h17.front.adapter.MainViewPagerAdapter;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.front.common.ShowBuyPageListener;
import com.zongsheng.drink.h17.front.effect.DepthPageTransformer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 饮料
 * Created by dxf on 2016/8/30.
 */
public class GeziFragment extends Fragment {
    @BindView(R.id.vp_goods)
    ViewPager mViewPager;
    @BindView(R.id.iv_go_up)
    ImageView ivGoUp;
    @BindView(R.id.iv_go_down)
    ImageView ivGoDown;
    @BindView(R.id.tv_page)
    TextView tvPage;
    @BindView(R.id.rl_drink_top)
    RelativeLayout rlDrinkTop;
    @BindView(R.id.rl_food_top)
    RelativeLayout rlFoodTop;
    @BindView(R.id.rl_gezi_top)
    RelativeLayout rlGeziTop;
    private View view;
    private LayoutInflater inflater;

    private MainViewPagerAdapter mViewPagerAdapter;

    private static final String TAG = "NewBuyFragment";

    /** 购买监听 */
    private ShowBuyPageListener showBuyPageListener;

    /**
     * 分页
     */
    private int pageCount = 1;
    /**
     * 当前页
     */
    private int currentPage = 1;
    private List<GoodsInfo> goodsInfoList = new ArrayList<>();

    /** 1饮料，2食品，3格子 */
    private int selectType = 3;
    /**
     * 构造函数，防止崩溃
     */
    public GeziFragment() {
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
        this.inflater = inflater;
        view = inflater.inflate(R.layout.fragment_new_buy, null);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    /**
     * 初始化页面
     */
    private void initView() {
        // 设置选中
        rlDrinkTop.setSelected(true);
        // 假数据
        goodsInfoList = MyApplication.getInstance().getCabinetGoods();
        mViewPagerAdapter = new MainViewPagerAdapter(goodsInfoList, getContext(), onGoodsClickListener);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());
        mViewPager.setAdapter(mViewPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "切换页面:" + position);
                currentPage = position + 1;
                setPagePosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        double dd;
        if(MyApplication.getInstance().getVersionType().equals(SysConfig.AOKEMA)){
            dd = goodsInfoList.size() / 9.0;
        }else{
            dd = goodsInfoList.size() / 12.0;
        }
        Log.i(TAG, dd + ":页数dd");
        if (dd > (int) dd) {
            pageCount = (int) dd + 1;
        } else {
            pageCount = (int) dd;
        }
        if (pageCount == 0) {
            pageCount = 1;
        }
        Log.i(TAG, pageCount + ":页数");

        // 改变翻页按钮
        changePageBtn(true);

    }

    /** 商品点击事件 */
    MainViewPagerAdapter.OnGoodsClickListener onGoodsClickListener = new MainViewPagerAdapter.OnGoodsClickListener() {
        @Override
        public void onGoodsClick(GoodsInfo goodsInfo) {
            // 显示购买页面
            Log.e(TAG, "点击购买格子柜商品");
            if ("1".equals(goodsInfo.getIsSoldOut())) {
                Log.e(TAG, "无货");
                return;
            }
            if (showBuyPageListener != null) {
                showBuyPageListener.showBuyPage(goodsInfo, "1");
            }
        }
    };

    /** 改变翻页按钮 */
    private void changePageBtn(boolean flag) {
        // 无操作播放广告timer重新计时
        if(flag) {
            ((BuyActivity) getContext()).resetPlayAdTimer();
        }
        if (currentPage != pageCount) { // 两页以上
            if (currentPage == 1) {
                // 首页且有2页以上
                ivGoUp.setImageResource(R.drawable.ic_left_small);
                ivGoUp.setPadding(DataUtil.dip2px(MyApplication.getInstance(), 4), DataUtil.dip2px(MyApplication.getInstance(), 4), DataUtil.dip2px(MyApplication.getInstance(), 4), DataUtil.dip2px(MyApplication.getInstance(), 4));
                ivGoDown.setImageResource(R.drawable.ic_right_big);
                ivGoDown.setPadding(0, 0, 0, 0);
            } else {
                if (currentPage < pageCount) {
                    // 中间页
                    ivGoUp.setImageResource(R.drawable.ic_left_big);
                    ivGoDown.setImageResource(R.drawable.ic_right_big);
                    ivGoUp.setPadding(0,0,0,0);
                    ivGoDown.setPadding(0,0,0,0);
                } else {
                    // 尾页
                    ivGoUp.setImageResource(R.drawable.ic_left_big);
                    ivGoUp.setPadding(0, 0, 0, 0);
                    ivGoDown.setImageResource(R.drawable.ic_right_small);
                    ivGoDown.setPadding(DataUtil.dip2px(MyApplication.getInstance(), 4), DataUtil.dip2px(MyApplication.getInstance(), 4), DataUtil.dip2px(MyApplication.getInstance(), 4), DataUtil.dip2px(MyApplication.getInstance(), 4));
                }
            }
        } else {
            if (pageCount == 1) {
                // 只有一页
                ivGoUp.setImageResource(R.drawable.ic_left_small);
                ivGoUp.setPadding(DataUtil.dip2px(MyApplication.getInstance(), 4), DataUtil.dip2px(MyApplication.getInstance(), 4), DataUtil.dip2px(MyApplication.getInstance(), 4), DataUtil.dip2px(MyApplication.getInstance(), 4));
                ivGoDown.setImageResource(R.drawable.ic_right_small);
                ivGoDown.setPadding(DataUtil.dip2px(MyApplication.getInstance(), 4), DataUtil.dip2px(MyApplication.getInstance(), 4), DataUtil.dip2px(MyApplication.getInstance(), 4), DataUtil.dip2px(MyApplication.getInstance(), 4));
            } else {
                // 尾页
                ivGoUp.setImageResource(R.drawable.ic_left_big);
                ivGoUp.setPadding(0, 0, 0, 0);
                ivGoDown.setImageResource(R.drawable.ic_right_small);
                ivGoDown.setPadding(DataUtil.dip2px(MyApplication.getInstance(), 4), DataUtil.dip2px(MyApplication.getInstance(), 4), DataUtil.dip2px(MyApplication.getInstance(), 4), DataUtil.dip2px(MyApplication.getInstance(), 4));
            }
        }

        // 页数
        SpannableString spannableString = new SpannableString(currentPage + "/" + pageCount);
        spannableString.setSpan(new AbsoluteSizeSpan(22, true), 0, String.valueOf(currentPage).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPage.setText(spannableString);

    }

    @OnClick({R.id.iv_go_up, R.id.iv_go_down, R.id.rl_drink_top, R.id.rl_food_top, R.id.rl_gezi_top})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_go_up:// 上一页
                if (currentPage != 1) {
                    currentPage--;
                    setPagePosition(currentPage - 1);
                }
                break;
            case R.id.iv_go_down:// 下一页
                if (currentPage <= pageCount - 1) {
                    currentPage++;
                    setPagePosition(currentPage - 1);
                }
                break;
            case R.id.rl_drink_top:
                if (selectType != 1){
                    selectType = 1;
                    rlDrinkTop.setSelected(true);
                    rlFoodTop.setSelected(false);
                    rlGeziTop.setSelected(false);
                }
                break;
            case R.id.rl_food_top:
                if (selectType != 2){
                    selectType = 2;
                    rlDrinkTop.setSelected(false);
                    rlFoodTop.setSelected(true);
                    rlGeziTop.setSelected(false);
                }
                break;
            case R.id.rl_gezi_top:
                if (selectType != 3){
                    selectType = 3;
                    rlDrinkTop.setSelected(false);
                    rlFoodTop.setSelected(false);
                    rlGeziTop.setSelected(true);
                }
                break;
            default:
                break;
        }
    }

    /** 设置页面的position */
    public void setPagePosition(int position) {
        mViewPager.setCurrentItem(position);
        // 改变翻页按钮
        changePageBtn(true);
    }

    /** 设置购买监听 */
    public void setBuyPageListener(ShowBuyPageListener showBuyPageListener) {
        this.showBuyPageListener = showBuyPageListener;
    }

    /** 更新页面展示的产品库存 */
    public void updateGoodsInfo() {
        if(MyApplication.getInstance().getVersionType().equals(SysConfig.AOKEMA23)){
            pageCount = (int) Math.ceil(goodsInfoList.size() / 12.0);
        }else{
            pageCount = (int) Math.ceil(goodsInfoList.size() / 9.0);
        }
        if(pageCount == 0){
            pageCount = 1;
        }
        if(currentPage > pageCount) {
            currentPage = 1;
        }
        changePageBtn(false);
        try {
            mViewPager.getAdapter().notifyDataSetChanged();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(currentPage - 1);
                    mViewPager.getAdapter().notifyDataSetChanged();
                }
            }, 300);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
