package com.zongsheng.drink.h17.background.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话框管理
 */
public class DialogUtil {

    private Context context;
    private List<String> list;
    private String title;
    private DialogInterface.OnClickListener onDialogListener;

    public DialogUtil(Context context) {
        this.context = context;
        list = new ArrayList<String>();
    }

    private Dialog ListItemDialog() {
        AlertDialog.Builder build = new AlertDialog.Builder(context);
        build.setTitle(title);
        if (list != null && list.size() > 0) {
            CharSequence[] charItems = list.toArray(new CharSequence[list.size()]);
            build.setItems(charItems, onDialogListener);
        }
        return build.create();
    }


    public void setOnDialogListener(DialogInterface.OnClickListener onDialogListener) {
        this.onDialogListener = onDialogListener;
    }

    public void addItem(String item) {
        list.add(item);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void showDialog() {
        ListItemDialog().show();
    }

}
