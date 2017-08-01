package com.zongsheng.drink.h17.presenter;

/**
 * Created by Suchengjian on 2017.3.22.
 */

public interface ILoadingPresenter {
    void cancel();
    void getServiceMQIP();
    void getBindGeziInfo();
    void updataHelpInfo();
    void updateLocalKucun(String road_no);
    void AfterMachineInfoGetOver();
}
