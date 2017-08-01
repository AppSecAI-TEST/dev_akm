package com.zongsheng.drink.h17.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zongsheng.drink.h17.common.L;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.service.HexinService;


/**
 * Created by Suchengjian on 2017.2.14.
 */

    public class DeamonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(SysConfig.RECEIVER_ACTION_DEAMON.equals(intent.getAction())){
            context.startService(new Intent(context, HexinService.class));
        }
    }
}
