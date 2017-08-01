package com.zongsheng.drink.h17.background.fragment;

import android.view.View;

import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.interfaces.INetWorkRequCallBackListener;
import com.zongsheng.drink.h17.interfaces.INetWorkRequInterface;

import java.util.List;

/**
 * Created by Suchengjian on 2017.4.19.
 */

public interface IDrinkFragmentPresent extends INetWorkRequCallBackListener {

    void submit(List<GoodsInfo> goodsInfoList,boolean isChanged);

    void more(int nwidth);

    void chejiRequest(String machin_sn,String apply_type);

    void requestMoBanFromNet(String machine_sn);

    void uploadTemplete(String Templetename,List<GoodsInfo> goodsInfoList);

    void clickPanD(String machine_sn,int currentPage);

    void onDestory();

}
