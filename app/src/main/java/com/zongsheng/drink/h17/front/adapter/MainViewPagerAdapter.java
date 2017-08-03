package com.zongsheng.drink.h17.front.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.util.LogUtil;

import java.util.LinkedHashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.GrayscaleTransformation;

/**
 * Created by 袁国栋 on 17/7/25.
 * 显示商品的ViewPager适配器
 */

public class MainViewPagerAdapter extends PagerAdapter implements View.OnClickListener{
    private LogUtil logUtil;
    private List<GoodsInfo> goodsInfoList;
    private Context context;
    private OnGoodsClickListener onGoodsClickListener;
    //每页加载的商品数
    private int itemsCountInPage;
    //商品的页数
    private int pageCount;
    private LinkedHashMap<Integer,GridLayout> viewCache;
    //每页的行数和列数
    private int rowInPage;
    private int colInPage;
    //高度偏移
    private int offsetHeight;
    //页面的宽度和高度
    private int width;
    private int height;

    public MainViewPagerAdapter(List<GoodsInfo> list, Context context, OnGoodsClickListener onGoodsClickListener){
        this.goodsInfoList=list;
        this.context=context;
        this.onGoodsClickListener=onGoodsClickListener;

        logUtil = new LogUtil(this.getClass().getSimpleName());
        //这里控制是否打印Log
        logUtil.setShouldPrintLog(false);

        //根据屏幕尺寸设置每页的商品数
        if (MyApplication.getInstance().getVersionType().equals(SysConfig.AOKEMA23)){
            itemsCountInPage=12;
            rowInPage=2;
            colInPage=6;
            offsetHeight=200;
        }else {
            itemsCountInPage=9;
            rowInPage=3;
            colInPage=3;
            offsetHeight=310;
        }

        pageCount=goodsInfoList.size()/itemsCountInPage+(goodsInfoList.size()%itemsCountInPage==0?0:1);

        logUtil.d("pageCount = "+goodsInfoList.size()/itemsCountInPage);
        logUtil.d("pageCount = "+(goodsInfoList.size()%itemsCountInPage==0?0:1));
        logUtil.d("pageCount = "+pageCount);
        logUtil.d("goodsCount = "+goodsInfoList.size());
        logUtil.d("rowCount = "+rowInPage);
        logUtil.d("colCount = "+colInPage);
        viewCache=new LinkedHashMap<>();
    }

    private GridLayout gridLayout;

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        //这里使用缓存重用GridLayout，不重用也行，因为数据量不大
        if (viewCache.containsValue(position)){
            gridLayout=viewCache.get(position);
        }else {
            gridLayout=new GridLayout(context);
            ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(gridLayout,layoutParams);
            //加入缓存
            viewCache.put(position,gridLayout);
            //设置页面信息
            initView(gridLayout,position,container);

        }
        return gridLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(viewCache.get(position));
    }

    @Override
    public int getCount() {
        return pageCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    /**
     * 初始化ViewPage的页面显示
     */
    private void initView(GridLayout gridLayout,int pagePosition,View container){

        width=container.getMeasuredWidth();
        //这里无法获得ViewPager正确的高度,所以进行了处理
        height=container.getMeasuredHeight()-offsetHeight;
        logUtil.d("GridLayout.width = "+width);
        logUtil.d("GridLayout.height = "+height);
        //该页面的商品数
        int count;
        if (pagePosition==pageCount-1){
            count=goodsInfoList.size()%itemsCountInPage;
        }else {
            count=itemsCountInPage;
        }
        //商品在列表中的索引
        int index;
        //itemView的尺寸
        int itemHeight=height/rowInPage;
        int itemWidth=width/colInPage;
        logUtil.d("width = "+width);
        logUtil.d("height = "+height);
        logUtil.d("itemWidth = "+itemWidth);
        logUtil.d("iteHeight = "+itemHeight);
        logUtil.d("goodsCountInCurrentPage = "+count);

        for (int i=0;i<count;i++){
            View view= LayoutInflater.from(context).inflate(R.layout.buy_goods_item,null,false);
            ViewHolder viewHolder=new ViewHolder(view);
            index=i+pagePosition*itemsCountInPage;
            GoodsInfo goodsInfo=goodsInfoList.get(index);

            logUtil.d("GoodsIndexInList = "+index+" | name = "+goodsInfo.getGoodsName());
            if (goodsInfo==null||!goodsInfo.isValid()){
                continue;
            }
            //初始化itemView
            viewHolder.tv_goodsName.setText(goodsInfo.getGoodsAbbreviation());
            //这里可能有问题
            viewHolder.tv_goodsPrice.setText("¥ "+(int)Double.parseDouble(goodsInfo.getPrice()) * 0.1);
            //是否为空 是否售空 0:未空 1:售空
            if (SysConfig.ISSOLDOUT_FLAG.equals(goodsInfo.getIsSoldOut())) {
                viewHolder.iv_empty.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(goodsInfo.getGoodsImage())
                        .dontAnimate()
                        .fitCenter()
                        .bitmapTransform(new GrayscaleTransformation(context))
                        .into(viewHolder.iv_goodsImg);
            } else {
                viewHolder.iv_empty.setVisibility(View.GONE);
                // 产品图片
                Glide.with(context)
                        .load(goodsInfo.getGoodsImage())
                        .dontAnimate()
                        .fitCenter()
                        .into(viewHolder.iv_goodsImg);
            }
            viewHolder.ll_goodsItem.setTag(goodsInfo);
            viewHolder.ll_goodsItem.setOnClickListener(this);
            //将itemView添加到GridLayout中
            View itemView=viewHolder.ll_goodsItem;
            //itemView处于的行数和列数
            GridLayout.Spec row=GridLayout.spec(i/colInPage);
            GridLayout.Spec col=GridLayout.spec(i%colInPage);
            GridLayout.LayoutParams layoutParams=new GridLayout.LayoutParams(row,col);
            layoutParams.height=itemHeight;
            layoutParams.width=itemWidth;
            gridLayout.addView(itemView,layoutParams);
        }

    }

    @Override
    public void onClick(View view) {
        GoodsInfo goodsInfo=(GoodsInfo)view.getTag();
        if (onGoodsClickListener!=null&&goodsInfo!=null){
            onGoodsClickListener.onGoodsClick(goodsInfo);
        }
    }
    public interface OnGoodsClickListener {
        void onGoodsClick(GoodsInfo goodsInfo);
    }
    static class ViewHolder{
        @BindView(R.id.ll_goods_item)
        LinearLayout ll_goodsItem;
        @BindView(R.id.iv_goods_img)
        ImageView iv_goodsImg;
        @BindView(R.id.iv_empty)
        ImageView iv_empty;
        @BindView(R.id.tv_goods_name)
        TextView tv_goodsName;
        @BindView(R.id.tv_goods_price)
        TextView tv_goodsPrice;
        public ViewHolder(View view){
            ButterKnife.bind(this,view);
        }
    }
}
