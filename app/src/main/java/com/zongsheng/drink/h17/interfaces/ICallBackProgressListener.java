package com.zongsheng.drink.h17.interfaces;

import java.io.File;

/**
 * Created by Administrator on 2017.3.3.
 */

public interface ICallBackProgressListener {
    void downloadSuccess(File file);
    void callProgress(int progress);
    void closePopWindow();
    void closePopWindow(String msg);
}
