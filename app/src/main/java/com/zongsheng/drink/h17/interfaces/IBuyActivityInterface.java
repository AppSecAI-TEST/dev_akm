package com.zongsheng.drink.h17.interfaces;


import android.view.View;

/**
 * Created by Suchengjian on 2017.2.14.
 */

public interface IBuyActivityInterface {
    void closeAd(boolean isResetTimer);

    void onClickAgain(View view);

    String swtchPayType(String payType);
}
