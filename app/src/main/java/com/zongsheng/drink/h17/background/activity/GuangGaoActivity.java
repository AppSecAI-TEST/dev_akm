package com.zongsheng.drink.h17.background.activity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.MarkLog;
import com.zongsheng.drink.h17.background.common.MachineOrderRstListener;
import com.zongsheng.drink.h17.background.common.UdiskAdTongBuTask;
import com.zongsheng.drink.h17.common.LoadingUtil;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.common.ToastUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GuangGaoActivity extends Activity implements Observer{


    @BindView(R.id.rl_back)
    RelativeLayout rlBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.rl_top)
    RelativeLayout rlTop;
    @BindView(R.id.tv_shangpin)
    TextView tvShangpin;
    @BindView(R.id.tv_huodao)
    TextView tvHuodao;
    private MyApplication myApp;
    /**
     * 自定义的Dialog
     */
    private Dialog dialog;// 加载中
    private Dialog dialog1;// 加载完成
    /**
     * 计时器
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guang_gao);
        ButterKnife.bind(this);
        MyObservable.getInstance().registObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyObservable.getInstance().unregistObserver(this);
    }

    @OnClick({R.id.rl_back, R.id.tv_shangpin})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.tv_shangpin:
                browseToRoot();
                break;
            default:
                break;
        }
    }
    //浏览文件系统的根目录
    private void browseToRoot()
    {
        String filePath;
        if (SysConfig.AOKEMA.equals(MyApplication.getInstance().getVersionType())) {
            filePath = "/mnt/udisk/zongs/guanggao7";
        } else {
            filePath = "/mnt/udisk/zongs/guanggao23";
        }
        final File file = new File(filePath);
        if (file.exists()) {
            dialog = LoadingUtil.createLoadingDialog(this, "正在同步...", 0, 0, true);
            dialog.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    browseTo(file);;
                    //源文件夹
                    String yuan = file.getAbsolutePath();
                    //目的地
                    String mudi = MyApplication.getInstance().getSdCardPath() + SysConfig.SD_CARD_PATH_AD;
                    new UdiskAdTongBuTask(yuan, mudi, machineBuHuoRstListener).execute();
                }
            }, 1000);

        } else {
//            if (dialog != null) {// 关闭登录中dialog
//                dialog.dismiss();
//            }
            Toast.makeText(this,"U盘未正确插入或U盘中没有找到指定文件夹(zongs/guanggao[7/23])",Toast.LENGTH_LONG).show();
        }

    }
    //浏览指定的目录,如果是文件则进行打开操作
    private void browseTo(final File file)
    {
        //源文件夹
        String yuan = file.getAbsolutePath();
        //目的地
        String mudi = MyApplication.getInstance().getSdCardPath() + SysConfig.SD_CARD_PATH_AD;
        //建立目标文件夹
        File file2 = new File(mudi);
        if (file2.exists()){
            RecursionDeleteFile(file2);
        } else {
//            Toast.makeText(this,"meiyou1",Toast.LENGTH_LONG).show();
//            dialog.dismiss();
        }
        file2.mkdirs();
        //获取源文件夹当下的文件或目录
        File[] files = (new File(yuan)).listFiles();
        for(File file1 : files)
        {
            String fileName = file1.getName();
            if (!fileName.toLowerCase().endsWith("mp4") && !fileName.toLowerCase().endsWith("flv") &&
                    !fileName.toLowerCase().endsWith("jpg") && !fileName.toLowerCase().endsWith("jpeg") &&
                    !fileName.toLowerCase().endsWith("png")) {
                continue;
            }
            if(file1.isDirectory())
            {
                String yuanDir = yuan + "/" + file1.getName();
                String mudiDir = mudi + "/" + file1.getName();
                //复制目录
                copyDir(yuanDir, mudiDir);
            }
            else
            {
                copyFile(file1, new File(mudi + "/" + file1.getName()));
            }
        }
        if (dialog != null) {// 关闭登录中dialog
            dialog.dismiss();
        }
        MarkLog.markLog("从U盘同步广告", SysConfig.LOG_LEVEL_MIDDLE, MyApplication.getInstance().getMachine_sn());
        dialog1 = LoadingUtil.createLoadingDialog(GuangGaoActivity.this, "同步成功", 1, 0, false);// 开启登录完成dialog
        dialog1.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            dialog1.dismiss();
            }
        }, 1000);
    }

    /** 同步处理结果回调 */
    private MachineOrderRstListener machineBuHuoRstListener = new MachineOrderRstListener() {
        @Override
        public void success() {
            if (dialog != null) {// 关闭登录中dialog
                dialog.dismiss();
            }
            MarkLog.markLog("从U盘同步广告", SysConfig.LOG_LEVEL_MIDDLE, MyApplication.getInstance().getMachine_sn());
            dialog1 = LoadingUtil.createLoadingDialog(GuangGaoActivity.this, "同步成功", 1, 0, false);// 开启登录完成dialog
            dialog1.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog1.dismiss();
                }
            }, 1000);
        }

        @Override
        public void fail() {
            if (dialog != null)
                dialog.dismiss();
            ToastUtils.showToast(GuangGaoActivity.this, "同步中出现错误,请重试");
        }
    };

    //复制文件夹
    public  void copyDir(String yuanDir, String mudiDir)
    {
        (new File(mudiDir)).mkdirs();
        File[] files = (new File(yuanDir)).listFiles();
        for(File file : files)
        {
            if(file.isFile())
            {
                File yuanFile = file;//源文件
                File mudiFile = new File(new File(mudiDir).getAbsolutePath() + "/" + file.getName());
                copyFile(yuanFile, mudiFile);
            }
            else
            {
                String yuanJia = yuanDir + "/" + file.getName();
                String mudiJia = mudiDir + "/" + file.getName();
                copyDir(yuanJia, mudiJia);
            }
        }

    }
    //复制文件
    public  void copyFile(File yuanFile, File mudiFile)
    {
        BufferedInputStream buis = null;
        BufferedOutputStream buos = null;
        try {
            buis = new BufferedInputStream(new FileInputStream(yuanFile.getAbsoluteFile()));
            buos = new BufferedOutputStream(new FileOutputStream(mudiFile.getAbsoluteFile()));
            byte[] buf = new byte[1024];
            int len = 0;
            while((len = buis.read(buf)) != -1)
            {
                buos.write(buf, 0, len);
                buos.flush();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (buis != null) {
                    buis.close();
                }
                if (buos != null) {
                    buos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 递归删除文件和文件夹
     * @param file    要删除的根目录
     */
    public static void RecursionDeleteFile(File file){
        if(file.isFile()){
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                RecursionDeleteFile(f);
            }
            file.delete();
        }
    }

    @Override
    public void onBackPressed() {
        //不允许右键退出,必须使用按钮退出
        //super.onPause();
    }

    @Override
    public void update(Observable observable, Object o) {
        finish();
    }
}
