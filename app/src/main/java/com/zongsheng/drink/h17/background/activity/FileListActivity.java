package com.zongsheng.drink.h17.background.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.MarkLog;
import com.zongsheng.drink.h17.background.bean.ApkBean;
import com.zongsheng.drink.h17.common.LoadingUtil;
import com.zongsheng.drink.h17.common.SysConfig;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileListActivity extends Activity {

    @BindView(R.id.list)
    ListView list;
    private List<ApkBean> apkBeanList;
    Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        ButterKnife.bind(this);
        Intent intent = this.getIntent();
        apkBeanList = (List<ApkBean>) intent.getSerializableExtra("list");//获取list方式
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                dialog = LoadingUtil.createLoadingDialog(FileListActivity.this, "正在检查...", 0, 0, true);
                dialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                    dialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(apkBeanList.get(position).getFile().getAbsoluteFile()),
                                "application/vnd.android.package-archive");
                        startActivity(intent);
                    }
                }, 2000);

                //写入操作日志
                MarkLog.markLog("从U盘安装软件", SysConfig.LOG_LEVEL_IMPORTANT, ((MyApplication) getApplication()).getMachine_sn());
            }
        });
    }
    @Override
    public void onBackPressed() {
        //不允许右键退出,必须使用按钮退出
        //super.onPause();
    }
}
