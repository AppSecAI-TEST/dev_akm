package com.zongsheng.drink.h17.background.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.bean.ApkBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/9/29.
 */
public class FileListAdapter extends BaseAdapter {
    private List<ApkBean> apkBeanList;
    private Context context;
    private LayoutInflater layoutInflater;

    public FileListAdapter(Context context, List<ApkBean> apkBeanList) {
        this.apkBeanList = apkBeanList;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return apkBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return apkBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_list_name, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tevName.setText(apkBeanList.get(position).getFileName());
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tev_name)
        TextView tevName;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
