package com.zongsheng.drink.h17.Model;

import com.zongsheng.drink.h17.background.bean.BindGeZi;
import com.zongsheng.drink.h17.front.bean.ServerHelpInfo;

import java.util.List;

/**
 * Created by Suchengjian on 2017.3.22.
 */

public interface ILoadingModel {
    void cancel();
    void delHelpInfo2Realm();
    void addHelpInfos2Realm(final List<ServerHelpInfo> helpinfos);
    void addBindGeZi2Realm(final List<BindGeZi> bindgezi);
    void updateLocalKucun(String road_no);
    void getGoods4Realm();
    void getGeZiInfo();
    void getDeskGoods4Realm();
    void getDeskInfo();
    /**
     *
     * @return true有格子柜数据，false没有格子柜数据
     */
    boolean isBindGeZiInfo();

}
