package com.zongsheng.drink.h17.common;

import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zongsheng.drink.h17.MyApplication;

/**
 * Created by 谢家勋 on 2016/9/14.
 */
public class ToastUtils {

    //全局Toast，防止遮挡
    private static Toast toast = Toast.makeText(MyApplication.getInstance(),"",Toast.LENGTH_LONG);

    public static void showToast(final Activity context, final String msg) {
        if ("main".equals(Thread.currentThread().getName())) {
//            Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            setToastStyle(toast);
            toast.setText(msg);
            toast.show();
        } else {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
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
