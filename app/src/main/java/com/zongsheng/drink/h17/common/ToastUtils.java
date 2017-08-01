package com.zongsheng.drink.h17.common;

import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by 谢家勋 on 2016/9/14.
 */
public class ToastUtils {

    public static void showToast(final Activity context, final String msg) {
        if ("main".equals(Thread.currentThread().getName())) {
            Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            setToastStyle(toast);
            toast.setText(msg);
            toast.show();
        } else {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
                    setToastStyle(toast);
                    toast.show();
                }
            });
        }
    }

    private static void setToastStyle(Toast toast) {
        LinearLayout linearLayout = (LinearLayout) toast.getView();
        TextView messageTextView = (TextView) linearLayout.getChildAt(0);
        messageTextView.setTextSize(28);
    }
}
