package com.zongsheng.drink.h17.front.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.GrayscaleTransformation;

/**
 * 商品展示
 * Created by 谢家勋 on 2016/8/15.
 */
public class GoodsAdapter extends BaseAdapter {
    private List<GoodsInfo> goodsInfoList;
    private LayoutInflater mInflater;
    private Context context;
    public GoodsAdapter(Context context, List<GoodsInfo> goodsInfoList) {
        super();
        this.mInflater = LayoutInflater.from(context);
        this.goodsInfoList = goodsInfoList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return goodsInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return goodsInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_goods, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        GoodsInfo goodsInfo = goodsInfoList.get(position);

        if (goodsInfo != null) {

            // 名字
            holder.tvGoodsName.setText(goodsInfo.getGoodsName());

            // 价格
            holder.tvGoodsPrice.setText("¥ " + Double.parseDouble(goodsInfo.getPrice()) * 0.1);

            // 是否为空
            if ("".equals(goodsInfo.getKuCun()) || "0".equals(goodsInfo.getKuCun()) || goodsInfo.getKuCun() == null){
                holder.ivEmpty.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(R.drawable.ic_drink)
                        .into(holder.ivGoodsImg);
            } else {
                holder.ivEmpty.setVisibility(View.GONE);
                // 图
                Glide.with(context)
                        .load(R.drawable.ic_drink)
                        .into(holder.ivGoodsImg);
            }
        }

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.iv_empty)
        ImageView ivEmpty;
        @BindView(R.id.iv_goods_img)
        ImageView ivGoodsImg;
        @BindView(R.id.tv_goods_name)
        TextView tvGoodsName;
        @BindView(R.id.tv_goods_price)
        TextView tvGoodsPrice;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
