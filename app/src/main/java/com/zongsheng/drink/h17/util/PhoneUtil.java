package com.zongsheng.drink.h17.util;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by Suchengjian on 2017.3.6.
 */

public class PhoneUtil {

    private TelephonyManager telephonyManager;

    public PhoneUtil(Context context) {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    //获取sim卡iccid
    public String getIccid() {
        String iccid = "N/A";
        iccid = telephonyManager.getSimSerialNumber();
        if(iccid == null){
            iccid = "";
        }
        return iccid;
    }
}
