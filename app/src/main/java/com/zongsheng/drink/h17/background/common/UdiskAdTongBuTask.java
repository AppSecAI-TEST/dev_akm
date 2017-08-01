package com.zongsheng.drink.h17.background.common;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * U盘广告同步task
 * Created by dongxiaofei on 2016/10/24.
 */

public class UdiskAdTongBuTask extends AsyncTask<String, Integer, String> {
    MachineOrderRstListener machineOrderRstListener;
    String fromFilePath = "";
    String toFilePath = "";
    public UdiskAdTongBuTask(String fromFilePath, String toFilePath, MachineOrderRstListener machineOrderRstListener) {
        this.fromFilePath = fromFilePath;
        this.toFilePath = toFilePath;
        this.machineOrderRstListener = machineOrderRstListener;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            //建立目标文件夹
            File file2 = new File(toFilePath);
            if (file2.exists()) {
                RecursionDeleteFile(file2);
            } else {
//            Toast.makeText(this,"meiyou1",Toast.LENGTH_LONG).show();
//            dialog.dismiss();
            }
            file2.mkdirs();
            //获取源文件夹当下的文件或目录
            File[] files = (new File(fromFilePath)).listFiles();
            for (File file1 : files) {
                String fileName = file1.getName();
                if (!fileName.toLowerCase().endsWith("mp4") && !fileName.toLowerCase().endsWith("flv") &&
                        !fileName.toLowerCase().endsWith("jpg") && !fileName.toLowerCase().endsWith("jpeg") &&
                        !fileName.toLowerCase().endsWith("png")) {
                    continue;
                }
                if (file1.isDirectory()) {
                    String yuanDir = fromFilePath + "/" + file1.getName();
                    String mudiDir = toFilePath + "/" + file1.getName();
                    //复制目录
                    copyDir(yuanDir, mudiDir);
                } else {
                    copyFile(file1, new File(toFilePath + "/" + file1.getName()));
                }
            }
        } catch (Exception e) {
            return "失败";
        }
        return "";
    }

    //TestAsyncTask被后台线程执行后，被UI线程被调用，一般用于初始化界面控件，如进度条
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    //doInBackground执行完后由UI线程调用，用于更新界面操作
    @Override
    protected void onPostExecute(String result) {
        if ("".equals(result)) {
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
}
