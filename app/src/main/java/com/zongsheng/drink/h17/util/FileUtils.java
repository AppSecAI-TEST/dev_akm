package com.zongsheng.drink.h17.util;

import com.zongsheng.drink.h17.common.Constant;
import com.zongsheng.drink.h17.common.L;
import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by xsnail on 2017/4/6.
 */
public class FileUtils {
    public static boolean isOpen = false;
    static final SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd" + " "
            + "hh:mm:ss" + ": ");
    // 将字符串写入到文本文件中
    public static void writeStringToFile(String strcontent) {
        if(isOpen) {
            // 每次写入时，都换行写
            String strContent = tempDate.format(new Date()) + strcontent + "\r\n";
            try {
                File file = new File(Constant.PATH_NAME + Constant.FILE_NAME);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                RandomAccessFile raf = new RandomAccessFile(file, "rwd");
                raf.seek(file.length());
                raf.write(strContent.getBytes());
                raf.close();
            } catch (Exception e) {
                L.d("Create File Failed", e.toString());
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
}
