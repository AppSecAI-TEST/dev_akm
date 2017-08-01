package com.zongsheng.drink.h17.background.fragment;


import com.zongsheng.drink.h17.background.bean.PanDInfo;
import com.zongsheng.drink.h17.background.bean.RoadTemple;
import com.zongsheng.drink.h17.common.popupwindow.TitlePopup;

import java.util.List;

/**
 * Created by Suchengjian on 2017.4.19.
 */

public interface IDrinkFragmentInterface {
    void PopSetItemClick(TitlePopup pop);
    void mubannotifyDataSetChanged(List<RoadTemple> roadTemples);
    void pandiannotifyDataSetChanged(List<PanDInfo> panDInfoList,int totalPage);
    void loadmoreFinish();
    void dialogDismiss();
}
