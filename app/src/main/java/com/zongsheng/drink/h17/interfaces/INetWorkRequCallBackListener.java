package com.zongsheng.drink.h17.interfaces;


import com.yolanda.nohttp.rest.Response;


/**
 * Created by Suchengjian on 2017.2.17.
 */

public interface INetWorkRequCallBackListener {
    void onSucceed(int what,Response<String> response) throws Exception;
    void onFailed(int what, String url, final Object tag, Exception exception, int responseCode, long networkMillis) throws Exception;
}
