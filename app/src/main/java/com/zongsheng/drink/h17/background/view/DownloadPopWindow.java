package com.zongsheng.drink.h17.background.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.view.CircleProgressBar;

/**
 * 显示软件信息-检查更新时下载内容
 * Created by goolong on 17/2/28.
 */
public class DownloadPopWindow extends PopupWindow {

    private LayoutInflater layoutInflater;
    private View view;

    private TextView tvProcess;
    private TextView tvVersion;
    private CircleProgressBar pbDownload;

    public DownloadPopWindow(Context context) {
        layoutInflater = LayoutInflater.from(context);
        init();
    }

    /**
     * 初始化视图
     */
    private void init() {
        view = layoutInflater.inflate(R.layout.ly_update, null);
        setContentView(view);
        initView();
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setOutsideTouchable(false);
    }

    /**
     * 绑定View
     */
    private void initView() {
        tvProcess = (TextView) view.findViewById(R.id.tvProcess);
        tvVersion = (TextView) view.findViewById(R.id.tvVersion);
        pbDownload = (CircleProgressBar) view.findViewById(R.id.pbDownload);
    }

    /**
     * 设置更新内容
     * @param process
     * @param progress
     */
    public void setUpdateInfo(String process, int progress) {
        tvProcess.setText(process);
        pbDownload.setProgress(progress);
    }

    /**
     * 设置更新版本名字
     * @param version
     */
    public void setVersionName(String version) {
        tvVersion.setText("版本:" + version);
    }

}
