package com.zongsheng.drink.h17.background.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.bean.MachineInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by 谢家勋 on 2016/9/16.
 */
public class GeziAdapter extends BaseAdapter {

    private List<MachineInfo> geziList;
    private Context context;

    public GeziAdapter(Context context, List<MachineInfo> geziList) {
        super();
        this.geziList = geziList;
        this.context = context;

    }

    @Override
    public int getCount() {
        return geziList.size();
    }

    @Override
    public MachineInfo getItem(int position) {
        return geziList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context,R.layout.item_gezi_search_list,null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        MachineInfo info = getItem(position);
        Log.e("222222",info.getMachineSn());
        holder.tvGeziSn.setText(info.getMachineSn());


        holder.tvGeziName.setText(info.getMachineName());

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv_gezi_sn)
        TextView tvGeziSn;
        @BindView(R.id.tv_gezi_name)
        TextView tvGeziName;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
