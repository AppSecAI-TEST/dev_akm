package com.zongsheng.drink.h17.front.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.front.bean.HelpInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * 帮助
 * Created by 谢家勋 on 2016/8/16.
 */
public class HelpFragment extends Fragment {
    @BindView(R.id.tv_reason)
    TextView tvReason;
    @BindView(R.id.rl_question)
    RelativeLayout rlQuestion;
    @BindView(R.id.rl_show)
    RelativeLayout rlShow;
    @BindView(R.id.iv_close_show)
    ImageView ivCloseShow;
    private View view;

    @BindView(R.id.lv_help_question)
    ListView lvHelpQuestion;

    @BindView(R.id.rl_intro)
    RelativeLayout rl_intro;
    @BindView(R.id.wv_help_intro)
    WebView wv_help_intro;

    List<HelpInfo> helpList;
    Realm realm;

    QuestionAdapter questionAdapter;

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
        view = inflater.inflate(R.layout.fragment_help, null);
        ButterKnife.bind(this, view);
        realm = Realm.getDefaultInstance();
        // 取得帮助数据
        // 加载本地数据库中的商品信息
        RealmResults<HelpInfo> result = realm.where(HelpInfo.class).findAll();
        result = result.sort("showSort"); // Sort ascending
        result = result.sort("showSort", Sort.ASCENDING);
        helpList = result;
        Log.i("HelpFragment,帮助信息数量: ", result.size() + "");

        questionAdapter = new QuestionAdapter(getContext(), helpList, inflater);
        lvHelpQuestion.setAdapter(questionAdapter);

        lvHelpQuestion.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                HelpInfo helpInfo = helpList.get(position);
                if ("1".equals(helpInfo.getHelpType())) {
                    rlQuestion.setVisibility(View.VISIBLE);
                    tvReason.setText(helpInfo.getReason());
                } else if ("2".equals(helpInfo.getHelpType())) {
                    rlQuestion.setVisibility(View.GONE);
                }

                wv_help_intro.getSettings().setDefaultTextEncodingName("UTF-8");// 设置默认为utf-8
                String content = "<span style='line-height:30px;font-size:22px;font-color:#343434'>" + helpInfo.getHelpIntro() + "</span>";
                wv_help_intro.loadData(content, "text/html; charset=UTF-8", null);// 这种写法可以正确解码

                rlShow.setVisibility(View.VISIBLE);
            }
        });




        rlShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlShow.setVisibility(View.GONE);
                wv_help_intro.loadData("", "text/html; charset=UTF-8", null);
            }
        });

        ivCloseShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlShow.setVisibility(View.GONE);
                tvReason.setText("");
                wv_help_intro.loadData("", "text/html; charset=UTF-8", null);
            }
        });


        return view;
    }

    @OnClick (R.id.rl_detail)
    void clickView(View view) {
        switch (view.getId()) {
            case R.id.rl_detail:
                // DO NOTHING
            break;
        }
    }

    /** ADAPTER */
    class QuestionAdapter extends BaseAdapter {
        private List<HelpInfo> helpInfoList;
        private LayoutInflater mInflater;
        private Context context;
        public QuestionAdapter(Context context, List<HelpInfo> helpInfoList, LayoutInflater inflater) {
            super();
            this.mInflater = inflater;
            this.helpInfoList = helpInfoList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return helpInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return helpInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_help_info, null);
                holder = new ViewHolder();
                holder.tvQuestion = (TextView) convertView.findViewById(R.id.tv_question);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final HelpInfo helpInfo = helpInfoList.get(index);

            if (helpInfo != null) {
                // 名字
                holder.tvQuestion.setText((index + 1) + ". " + helpInfo.getQuestion());
            } else {
                holder.tvQuestion.setText("");
            }

            return convertView;
        }

        class ViewHolder {
            TextView tvQuestion;
        }

    }


}
