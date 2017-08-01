package com.zongsheng.drink.h17.background.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.bean.LogSellInfo;
import com.zongsheng.drink.h17.background.bean.PanDInfo;
import com.zongsheng.drink.h17.background.bean.PandianInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 盘点
 * Created by 谢家勋 on 2016/8/15.
 */
public class PandianAdapter extends BaseAdapter {
    private List<PanDInfo> logsInfoList;
    private LayoutInflater mInflater;
    private Context context;

    public PandianAdapter(Context context, List<PanDInfo> logsInfoList) {
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
        PanDInfo logsInfo = logsInfoList.get(position);
        if (logsInfo != null) {
            holder.tvoperatetime.setText(logsInfo.getOperateTime());
            holder.tvgrossonline.setText(logsInfo.getGrossOnline());
            holder.tvgrossoffline.setText(logsInfo.getGrossOffline());
            holder.tvsalesvolume.setText(logsInfo.getSalesVolume());
        }
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv_operatetime)
        TextView tvoperatetime;
        @BindView(R.id.tv_grossonline)
        TextView tvgrossonline;
        @BindView(R.id.tv_grossoffline)
        TextView tvgrossoffline;
        @BindView(R.id.tv_salesvolume)
        TextView tvsalesvolume;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
