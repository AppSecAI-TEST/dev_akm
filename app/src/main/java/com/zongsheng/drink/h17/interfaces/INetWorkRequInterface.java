package com.zongsheng.drink.h17.interfaces;

import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.download.DownloadListener;
import com.zongsheng.drink.h17.nohttp.HttpListener;

import java.util.Map;


/**
 * Created by Suchengjian on 2017.2.17.
 */

public interface INetWorkRequInterface {

    void request(String url,int what, RequestMethod requestMethod, Map<String,String> paraMap);

    void request(String url,int what, RequestMethod requestMethod);

    void request(String url, int what, RequestMethod requestMethod, HttpListener<String> httpListener);

    /**
     * 下载请求
     *
     */
    void downLoadRequest(String url,int what, String dirPath, String fileName, boolean isRange, boolean isDeleteOld, DownloadListener downloadListener);

    void cancel();
}
