package com.zongsheng.drink.h17.common;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.widget.ImageView;

import com.zongsheng.drink.h17.R;


/**
 * 信号监听
 * Created by 谢家勋 on 2016/8/18.
 */
public class MyPhoneStateListener extends PhoneStateListener {
    private ImageView view;

    public MyPhoneStateListener() {
        super();
    }

    public MyPhoneStateListener(ImageView view) {
        super();
        this.view = view;
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        if (!signalStrength.isGsm()) {
            int dBm = signalStrength.getCdmaDbm();
            if (dBm >= -75) {
                //79-99
                view.setImageResource(R.drawable.ic_state_5);
            } else if (dBm >= -85) {
                //59-79
                view.setImageResource(R.drawable.ic_state_4);
            } else if (dBm >= -95) {
                //39-59
                view.setImageResource(R.drawable.ic_state_3);
            } else if (dBm >= -100) {
                //19-39
                view.setImageResource(R.drawable.ic_state_2);
            } else {
                //0-19
                view.setImageResource(R.drawable.ic_state_1);
            }
        } else {
            int asu = signalStrength.getGsmSignalStrength();
            if (asu < 0 || asu >= 99){
                view.setImageResource(R.drawable.ic_state_1);
            }
            else if (asu >= 16){
                view.setImageResource(R.drawable.ic_state_5);
            }
            else if (asu >= 8) {
                view.setImageResource(R.drawable.ic_state_4);
            }
            else if (asu >= 4){
                view.setImageResource(R.drawable.ic_state_3);
            }
            else {
                view.setImageResource(R.drawable.ic_state_2);
            }
        }
    }
}
