package com.zongsheng.drink.h17.common;


import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.CacheMode;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.interfaces.INetWorkRequCallBackListener;
import com.zongsheng.drink.h17.interfaces.INetWorkRequInterface;
import com.zongsheng.drink.h17.nohttp.CallServer;
import com.zongsheng.drink.h17.nohttp.HttpListener;


import java.util.Map;



/**
 * Created by Suchengjian on 2017.2.17.
 */

public class NetWorkRequImpl implements INetWorkRequInterface, HttpListener<String> {
    private INetWorkRequCallBackListener iNetWorkRequCallBackListener;
    // 数据请求
    private Request<String> request;

    public NetWorkRequImpl(INetWorkRequCallBackListener iNetWorkRequCallBackListener) {
        this.iNetWorkRequCallBackListener = iNetWorkRequCallBackListener;
    }

    @Override
    public void request(String url, int what, RequestMethod requestMethod, Map<String, String> paraMap) {
        request = NoHttp.createStringRequest(url, requestMethod);
        //设置为必须网络
        request.setCacheMode(CacheMode.ONLY_REQUEST_NETWORK);
        if (request != null) {
            DataUtil.requestDateContrl(paraMap, request);
            // 添加到请求队列
            CallServer.getRequestInstance().add(MyApplication.getInstance(), what, request, this, true, false);
        }
    }

    @Override
    public void request(String url, int what, RequestMethod requestMethod) {
        request = NoHttp.createStringRequest(url, requestMethod);
        //设置为必须网络
        request.setCacheMode(CacheMode.ONLY_REQUEST_NETWORK);
        if (request != null) {
            // 添加到请求队列
            CallServer.getRequestInstance().add(MyApplication.getInstance(), what, request, this, true, false);
        }
    }

    @Override
    public void cancel() {
        if(request != null){
            request.cancel();
            request = null;
        }
    }


    @Override
    public void onSucceed(int what, Response<String> response) {
        try {
            if (iNetWorkRequCallBackListener != null) {
                iNetWorkRequCallBackListener.onSucceed(what, response);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {
        try {
            if (iNetWorkRequCallBackListener != null) {
                iNetWorkRequCallBackListener.onFailed(what, url, tag, exception, responseCode, networkMillis);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
