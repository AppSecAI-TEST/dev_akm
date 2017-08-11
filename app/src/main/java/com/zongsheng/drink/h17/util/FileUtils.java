package com.zongsheng.drink.h17.util;

import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.common.Constant;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by xsnail on 2017/4/6.
 */
public class FileUtils {
    /**
     * 是否输出日志到文件
     */
    public static boolean isOpen = false;
    private static final SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd" + " " + "HH:mm:ss" + ": ");
    public static SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyy_MM_dd");
    private static File logFile;
    private static LogUtil logUtil = new LogUtil("FileUtils");
    private static RandomAccessFile raf;

    /**
     * 设置日志输出文件
     * @param file
     */
    public static void setLogFile(File file){
        closeLogFileStream();
        logFile = file;
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            raf = new RandomAccessFile(logFile, "rwd");
            MyApplication.getInstance().getLogInit().d("日志输出流打开 输出文件 = "+logFile.getName());

        } catch (IOException e) {
            e.printStackTrace();
            MyApplication.getInstance().getLogInit().d("创建日志文件失败 e = "+e);
        }
    }
    //日志记录是很频繁的操作，不应该每次都打开和关闭流，每次写文件时检查该文件是否已经被删除，应该重建并写入重建标志,检查时间，是否需要生成另一个文件
    public static synchronized void writeStringToFile(String strContent) {
        if(isOpen) {
            checkFileAndTimeStatues();
            // 每次写入时，都换行写
            String str = tempDate.format(new Date()) + strContent + "\r\n";
            try {
                if (raf != null){
                    raf.seek(logFile.length());
                    raf.write(str.getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void checkFileAndTimeStatues() {
        try {
            //检查文件是否被删除
            if (!logFile.exists()){
                closeLogFileStream();
                logFile.createNewFile();
                raf = new RandomAccessFile(logFile, "rwd");
                MyApplication.getInstance().getLogInit().d("日志文件被删除后重建 文件名 = "+logFile.getName());
            }
            //检查是否应该写入到新的日志文件
            String currentFileName = getTodayLogFileName();
            if (!logFile.getName().equals(currentFileName)){
                logFile = new File(Constant.PATH_NAME,currentFileName);
                setLogFile(logFile);
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * TODO:关闭日志输出流，应该在软件退出时调用
     */
    public static void closeLogFileStream(){
        if (raf != null){
            try {
                raf.close();
                raf = null;
                MyApplication.getInstance().getLogInit().d("日志输出流关闭 输出文件 = "+logFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 生成文件
    public static File createFilePath(String filePath, String fileName) {
        File file = null;
        createRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    private static void createRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(String filePath,String fileName){
        File file = new File(filePath + fileName);
        if(file.exists()){
            file.delete();
        }
    }

    /**
     * 获取今天应该生成的日志文件名
     * @return 日志文件名
     */
    public static String getTodayLogFileName(){
        String fileName = "command_"+fileNameFormat.format(new Date())+".txt";
        logUtil.d("日志文件名为 = "+fileName);
        return fileName;
    }

    /**
     * 在保存支付方式图标时用到，从id获得文件名
     * @param id 支付方式id
     * @return 文件名
     */
    public static String getPayIconFileName(String id){
        return "icon_"+id+".png";
    }

    /**
     * 在取出支付方式图标时用到，从id获得完整路径
     * @param id 支付方式id
     * @return 文件的完整路径
     */
    public static String getPayIconFullFilePath(String id){
        return MyApplication.getInstance().getSdCardPath()+"/zongs/pay_icon/"+getPayIconFileName(id);
    }
}
