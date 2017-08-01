package com.zongsheng.drink.h17.background.common;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.zongsheng.drink.h17.ComActivity;
import com.zongsheng.drink.h17.background.activity.InfoActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * U盘apk拷贝并安装
 * Created by dongxiaofei on 2016/10/2.
 */

public class ApkFileCopyTask extends AsyncTask<String, Integer, String> {
    File oldfile;
    String newPath;
    MachineOrderRstListener machineOrderRstListener;
    Context context;
    public ApkFileCopyTask(Context context, File oldfile, String newPath, MachineOrderRstListener machineOrderRstListener) {
        this.oldfile = oldfile;
        this.newPath = newPath;
        this.machineOrderRstListener = machineOrderRstListener;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            int bytesum = 0;
            int byteread = 0;
            if (oldfile.exists()) { //文件不存在时
                InputStream inStream = new FileInputStream(oldfile); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            } else {
                return "1";
            }
            File file = new File(newPath);
            if (!file.exists()) {
                return "1";
            }
            // 安装
            Instanll(file, context);
        } catch (Exception e) {
            e.printStackTrace();
           return "1";
        }
        return "0";
    }

    //TestAsyncTask被后台线程执行后，被UI线程被调用，一般用于初始化界面控件，如进度条
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    //doInBackground执行完后由UI线程调用，用于更新界面操作
    @Override
    protected void onPostExecute(String result) {
        if ("0".equals(result)) {
            // 执行成功
            machineOrderRstListener.success();
        } else {
            // 执行失败
            machineOrderRstListener.fail();
        }

        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    //安装下载后的apk文件
    private void Instanll(File file, Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

}
