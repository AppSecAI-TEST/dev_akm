package com.zongsheng.drink.h17.background.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Response;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.MarkLog;
import com.zongsheng.drink.h17.background.bean.SystemUpdate;
import com.zongsheng.drink.h17.background.common.ApkFileCopyTask;
import com.zongsheng.drink.h17.background.common.MachineOrderRstListener;
import com.zongsheng.drink.h17.background.view.DownloadPopWindow;
import com.zongsheng.drink.h17.common.Constant;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.GsonControl;
import com.zongsheng.drink.h17.common.LoadingUtil;
import com.zongsheng.drink.h17.common.NetWorkRequImpl;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.common.ToastUtils;
import com.zongsheng.drink.h17.common.UpdateService;
import com.zongsheng.drink.h17.interfaces.ICallBackProgressListener;
import com.zongsheng.drink.h17.interfaces.INetWorkRequCallBackListener;
import com.zongsheng.drink.h17.interfaces.INetWorkRequInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 软件信息
 * Created by 谢家勋 on 2016/8/23.
 */
public class InfoActivity extends Activity implements ICallBackProgressListener,INetWorkRequCallBackListener,Observer{

    @BindView(R.id.tv_version)
    TextView tvVersion;
    /**
     * 自定义的Dialog
     */
    private Dialog dialog;// 加载中
    private Dialog dialog1;// 加载完成
    private View parentView = null;
    private DownloadPopWindow downloadPopWindow = null;
    private INetWorkRequInterface iNetWorkRequInterface =  null;

    private String downloadUrl = "";
    private String version = "";
    AlertView alertView;

    private MyConnection myConnection = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        parentView = LayoutInflater.from(this).inflate(R.layout.activity_info,null);
        ButterKnife.bind(this);
        downloadPopWindow = new DownloadPopWindow(InfoActivity.this);
        MyObservable.getInstance().registObserver(this);
        // 获取版本号并显示
        tvVersion.setText("版本信息：" + getVersionName());
    }

    @OnClick({R.id.rl_back, R.id.tv_login,R.id.tv_update_usb})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.tv_login:// 检查更新
                // TODO: 2016/8/23 更新
                getNewVersion();
                break;
            case R.id.tv_update_usb:// 检查更新
                browseToRoot();
                break;
            default:
                break;
        }
    }

    //浏览文件系统的根目录
    private void browseToRoot()
    {
        File file = new File("/mnt/udisk");
        if (file.exists()) {
            File file1 = new File("/mnt/udisk/zongs/");
            if (!file1.exists()){
                Toast.makeText(this,"U盘未正确插入或U盘中没有找到指定文件夹(zongs)",Toast.LENGTH_LONG).show();
                return;
            }

            File[] files = new File("/mnt/udisk/zongs/").listFiles();
            if (files == null || files.length == 0) {
                Toast.makeText(this,"文件夹(zongs)是空的",Toast.LENGTH_LONG).show();
                return;
            }

            final List<File> apkFileList = new ArrayList<>();
            for (File arrFile : files) {
                if (arrFile != null && arrFile.getName().endsWith(".apk")) {
                    apkFileList.add(arrFile);
                }
            }

            if (apkFileList.size() == 0) {
                Toast.makeText(this,"文件夹(zongs)内没有安装文件(.apk)",Toast.LENGTH_LONG).show();
                return;
            }

            String[] fileNameArr = new String[apkFileList.size()];
            for (int i = 0; i < apkFileList.size(); i ++) {
                fileNameArr[i] = apkFileList.get(i).getName();
            }

            // 弹出选择框
            alertView = new AlertView("请选择文件安装", null, "取消", fileNameArr, new String[]{},
                    InfoActivity.this, AlertView.Style.ActionSheet, DataUtil.dip2px(InfoActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, final
                int position) {
                    if (position == -1) {
                        alertView.dismiss();
                    } else {
                        dialog = LoadingUtil.createLoadingDialog(InfoActivity.this, "复制文件中...", 0, 0, true);
                        dialog.show();
                        // 拷贝文件到指定文件夹并打开
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                copyFile(apkFileList.get(position), ((MyApplication) getApplication()).getSdCardPath() + "/zongs/update.apk");
                                new ApkFileCopyTask(InfoActivity.this, apkFileList.get(position), ((MyApplication) getApplication()).getSdCardPath() + "/zongs/update.apk", machineOrderRstListener).execute();
                            }
                        }, 200);
                        return;
                    }
                }
            }).setCancelable(false).setOnDismissListener(null);
            alertView.show();
        } else {
            Toast.makeText(this,"请插入U盘",Toast.LENGTH_LONG).show();
        }

    }

    /** 机器指令处理结果回调 */
    private MachineOrderRstListener machineOrderRstListener = new MachineOrderRstListener() {
        @Override
        public void success() {
            //写入操作日志
            MarkLog.markLog("从U盘安装软件", SysConfig.LOG_LEVEL_IMPORTANT, ((MyApplication) getApplication()).getMachine_sn());
            dialog.dismiss();
        }
        @Override
        public void fail() {
            dialog.dismiss();
            Toast.makeText(InfoActivity.this, "复制文件出错,请重试",Toast.LENGTH_LONG).show();
        }
    };

    private void getNewVersion(){
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/system/machine/" + ((MyApplication) getApplication()).getMachine_sn();
        if(iNetWorkRequInterface == null){
            iNetWorkRequInterface = new NetWorkRequImpl(this);
        }
        dialog = LoadingUtil.createLoadingDialog(this, "处理中...", 0, 0, true);
        dialog.show();
        iNetWorkRequInterface.request(url,0, RequestMethod.GET);
    }

    /**
     * 获取当前版本号
     */
    public int getVersionCode()//获取版本号(内部识别号)
    {
        try {
            PackageInfo pi= getPackageManager().getPackageInfo(getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取当前版本号
     */
    public String getVersionName()//获取版本b
    {
        try {
            PackageInfo pi= getPackageManager().getPackageInfo(getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }

    protected void onDestroy() {
        if(iNetWorkRequInterface != null){
            iNetWorkRequInterface.cancel();
            iNetWorkRequInterface = null;
        }
        if(myConnection != null){
            unbindService(myConnection);
        }
        if(downloadPopWindow != null && downloadPopWindow.isShowing()){
            downloadPopWindow.dismiss();
        }
        MyObservable.getInstance().unregistObserver(this);
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        //不允许右键退出,必须使用按钮退出
        //super.onPause();
    }

    @Override
    public void onSucceed(int what, Response<String> response) {
        if (dialog != null) {
            dialog.dismiss();
        }
        int responseCode = response.getHeaders().getResponseCode();// 服务器响应码
        if (responseCode == 200) {
            if (RequestMethod.HEAD != response.getRequestMethod()) {
                JSONObject jsonResult = null;
                try {
                    jsonResult = new JSONObject(response.get());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (what) {
                    case 0://请求
                        try {
                            // 如果成功
                            if (jsonResult != null && jsonResult.getString(SysConfig.JSON_KEY_ERROR_CODE).equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                final SystemUpdate systemUpdate = GsonControl.getPerson(jsonResult.getString(SysConfig.JSON_KEY_SYSTEMVERSION), SystemUpdate.class);
                                if (systemUpdate != null
                                        && systemUpdate.getVersionInt() != null
                                        && !"".equals(systemUpdate
                                        .getVersionInt())) {
                                    if (getVersionCode() < Double
                                            .valueOf(systemUpdate
                                                    .getVersionInt())) {
                                        version = systemUpdate.getVersionInt();
                                        downloadUrl = systemUpdate.getUpdateUrl();

                                        alertView = new AlertView("提示", Constant.UPLOAD_VERSION01, "下次再说", new String[]{"立即更新"}, null,
                                                InfoActivity.this, AlertView.Style.Alert, DataUtil.dip2px(InfoActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                                            @Override
                                            public void onItemClick(Object o, int position) {
                                                if (position == -1) {
                                                    alertView.dismiss();
                                                } else {
                                                    alertView.dismiss();
                                                    ToastUtils.showToast(InfoActivity.this,Constant.START_DOWNLOAD);
                                                    Intent intent = new Intent(InfoActivity.this, UpdateService.class);
                                                    myConnection = new MyConnection();
                                                    bindService(intent,myConnection,BIND_AUTO_CREATE);
                                                }
                                            }
                                        }).setCancelable(true).setOnDismissListener(null);
                                        alertView.show();
                                        //写入操作日志
                                        MarkLog.markLog("更新软件", SysConfig.LOG_LEVEL_NORMAL, ((MyApplication) getApplication()).getMachine_sn());
                                    } else {
                                        ToastUtils.showToast(InfoActivity.this,Constant.RECENT_VERSION);
                                    }
                                } else {
                                    ToastUtils.showToast(InfoActivity.this,Constant.RECENT_VERSION);
                                }
                            } else {// 不成功
                                ToastUtils.showToast(InfoActivity.this,jsonResult != null ? jsonResult.getString(SysConfig.JSON_KEY_ERROR) : "");
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {
        if (dialog != null) {
            dialog.dismiss();
        }
        ToastUtils.showToast(InfoActivity.this, Constant.NETWORK_ERROR3);
    }

    @Override
    public void update(Observable observable, Object o) {
        finish();
    }


    private class MyConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ((UpdateService.MyBinder)iBinder).attach(InfoActivity.this);
            ((UpdateService.MyBinder)iBinder).startDownload(downloadUrl);
            downloadPopWindow.showAtLocation(parentView, RelativeLayout.CENTER_IN_PARENT,0,0);
            downloadPopWindow.setVersionName(version);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if(downloadPopWindow != null && !isFinishing()) {
                downloadPopWindow.dismiss();
            }
        }
    }
    //安装下载后的apk文件
    private void Instanll(File file, Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    @Override
    public void downloadSuccess(File file) {
        if(!isFinishing()){
            Instanll(file,this);
        }
    }

    @Override
    public void callProgress(int progress) {
        if(downloadPopWindow != null) {
            downloadPopWindow.setUpdateInfo("已下载" + progress + "%", progress);
        }
    }

    @Override
    public void closePopWindow() {
        if(downloadPopWindow != null &&  !isFinishing() && downloadPopWindow.isShowing()) {
            downloadPopWindow.dismiss();
        }
    }

    @Override
    public void closePopWindow(String msg) {
        if(downloadPopWindow != null && !isFinishing() && downloadPopWindow.isShowing()) {
            downloadPopWindow.dismiss();
        }
        ToastUtils.showToast(InfoActivity.this,msg);
    }
}
