package com.zongsheng.drink.h17.background.adapter;

import android.content.Context;
import android.os.Handler;
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
public class RoadTestAdapter extends BaseAdapter {
    private int[] infoList;
    private LayoutInflater mInflater;
    private Context context;

    public RoadTestAdapter(Context context, int[] infoList, TestItemClickListener testItemClickListener) {
        super();
        this.mInflater = LayoutInflater.from(context);
        this.infoList = infoList;
        this.context = context;
        this.testItemClickListener = testItemClickListener;
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
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_road_test, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final int temp = infoList[position];

        holder.tvRoadName.setText("料道" + temp);
        holder.tvTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.tvTest.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        holder.tvTest.setEnabled(true);
                    }
                }, 1000);
                testItemClickListener.onItemClick(temp);
            }
        });

        return convertView;
    }

    private TestItemClickListener testItemClickListener;

    public interface TestItemClickListener {
        void onItemClick(int roadNo);
    }


    static class ViewHolder {
        @BindView(R.id.tv_road_name)
        TextView tvRoadName;
        @BindView(R.id.tv_test)
        TextView tvTest;
        @BindView(R.id.rl_bg)
        RelativeLayout rlBg;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
