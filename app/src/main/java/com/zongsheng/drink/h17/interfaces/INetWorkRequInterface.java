package com.zongsheng.drink.h17.interfaces;

import com.yolanda.nohttp.RequestMethod;

import java.util.Map;


/**
 * Created by Suchengjian on 2017.2.17.
 */

public interface INetWorkRequInterface {

    void request(String url,int what, RequestMethod requestMethod, Map<String,String> paraMap);

    void request(String url,int what, RequestMethod requestMethod);

    void cancel();
}
