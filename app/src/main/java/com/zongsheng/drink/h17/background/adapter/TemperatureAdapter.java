package com.zongsheng.drink.h17.background.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zongsheng.drink.h17.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 温度
 * Created by 谢家勋 on 2016/8/15.
 */
public class TemperatureAdapter extends BaseAdapter {
    private int[] infoList;
    private LayoutInflater mInflater;
    private Context context;
    private int chooseTemp;
    private String tempModel;

    public TemperatureAdapter(Context context, int[] infoList, int chooseTemp, String tempModel) {
        super();
        this.mInflater = LayoutInflater.from(context);
        this.infoList = infoList;
        this.context = context;
        this.chooseTemp = chooseTemp;
        this.tempModel = tempModel;
    }

    public void setChooseTemp(int chooseTemp) {
        this.chooseTemp = chooseTemp;
    }

    public void setTempModel(String tempModel) {
        this.tempModel = tempModel;
    }

    @Override
    public int getCount() {
        return infoList.length;
    }

    @Override
    public Object getItem(int position) {
        return infoList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_temperature, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        int temp = infoList[position];

        if ("0".equals(tempModel)) {
            holder.tvTempModel.setText("制冷");
        } else {
            holder.tvTempModel.setText("加热");
        }

        holder.tvTemp.setText(temp + "℃");

        if (chooseTemp == temp) {
            holder.rlBg.setBackgroundColor(context.getResources().getColor(R.color.list_item_presss_color));
        } else {
            holder.rlBg.setBackgroundColor(context.getResources().getColor(R.color.common_withe));
        }

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv_temp_model)
        TextView tvTempModel;
        @BindView(R.id.tv_temp)
        TextView tvTemp;
        @BindView(R.id.rl_bg)
        RelativeLayout rlBg;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
