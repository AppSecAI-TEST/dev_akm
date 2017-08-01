package com.zongsheng.drink.h17.background.bean;

import java.io.File;
import java.io.Serializable;

/**
 * Created by Administrator on 2016/9/29.
 */
public class ApkBean implements Serializable {
    private String fileName;
    private File file;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
