package com.zongsheng.drink.h17.background.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.bean.LogSellInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 销售日志
 * Created by 谢家勋 on 2016/8/15.
 */
public class LogsSellAdapter extends BaseAdapter {
    private List<LogSellInfo> logsInfoList;
    private LayoutInflater mInflater;
    private Context context;

    public LogsSellAdapter(Context context, List<LogSellInfo> logsInfoList) {
        super();
        this.mInflater = LayoutInflater.from(context);
        this.logsInfoList = logsInfoList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return logsInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return logsInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_logs_sell, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        LogSellInfo logsInfo = logsInfoList.get(position);
        if (logsInfo != null) {
            holder.tvName.setText(logsInfo.getGoodsName());
            holder.tvType.setText(logsInfo.getPayType());
            holder.tvPrice.setText(logsInfo.getPrice() + "元");
            holder.tvTime.setText(logsInfo.getPayTime());
        }
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv_operatetime)
        TextView tvName;
        @BindView(R.id.tv_grossonline)
        TextView tvType;
        @BindView(R.id.tv_grossoffline)
        TextView tvPrice;
        @BindView(R.id.tv_salesvolume)
        TextView tvTime;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
