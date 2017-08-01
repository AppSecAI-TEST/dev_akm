package com.zongsheng.drink.h17.background.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.bean.LogsInfo;
import com.zongsheng.drink.h17.background.bean.MubanInfo;
import com.zongsheng.drink.h17.background.bean.RoadTemple;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 获取模板
 * Created by 谢家勋 on 2016/8/15.
 */
public class MubanAdapter extends BaseAdapter {
    private List<RoadTemple> logsInfoList;
    private LayoutInflater mInflater;
    private Context context;

    public MubanAdapter(Context context, List<RoadTemple> logsInfoList) {
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
            convertView = mInflater.inflate(R.layout.item_logs, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final RoadTemple logsInfo = logsInfoList.get(position);

        if (logsInfo != null) {
            holder.tvOperation.setText(logsInfo.getTempleName());
//            holder.tvTime.setVisibility(View.GONE);
            holder.tvTime.setText(logsInfo.getCreateTime());
        }

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv_operation)
        TextView tvOperation;
        @BindView(R.id.tv_time)
        TextView tvTime;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
