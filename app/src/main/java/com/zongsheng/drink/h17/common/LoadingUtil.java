package com.zongsheng.drink.h17.common;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.R;


/**
 * 加载等待
 * Created by 谢家勋 on 2016/7/21.
 */
public class LoadingUtil {
    /**
     * 得到自定义的progressDialog
     *
     * @param context
     * @param msg     内容
     * @param loadingType 0是加载progressbar 1是加载img
     * @param imgId 图片id 0为默认
     * @param isAnim 是否为动画图片
     * @return Dialog
     */
    public static Dialog createLoadingDialog(Context context, String msg , int loadingType , int imgId , boolean isAnim) {
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        int width = wm.getDefaultDisplay().getWidth();

        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width;
        if(MyApplication.getInstance().getVersionType().equals(SysConfig.AOKEMA)){
            width = dm.widthPixels;
        }else{
            width = dm.heightPixels;
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.toast_loading, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progressBar);// 进度条
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        ImageView imageView = (ImageView) v.findViewById(R.id.img);
        if (0 == loadingType){// 进度条
            progressBar.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            if (imgId != 0){
                progressBar.setIndeterminateDrawable(context.getResources().getDrawable(imgId));
            }
        }else {// 图片
            progressBar.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            if (imgId != 0){
                imageView.setImageResource(imgId);
            }
        }
        if (isAnim){
            // 加载动画
            Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                    context, R.anim.loading_animation);
            // 使用ImageView显示动画
            imageView.startAnimation(hyperspaceJumpAnimation);
        }

        tipTextView.setText(msg);// 设置加载信息

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

        loadingDialog.setCanceledOnTouchOutside(false);// 可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(width * 1 / 3, width * 1 / 3));// 设置布局
        return loadingDialog;
    }

    public static Dialog createLoadingDialog(Context context, String msg , int loadingType , int imgId , boolean isAnim, boolean isCancelAble) {
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        int width = wm.getDefaultDisplay().getWidth();
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width;
        if(context.getResources().getString(R.string.versionType).equals(SysConfig.AOKEMA)){
            width = dm.widthPixels;
        }else{
            width = dm.heightPixels;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.toast_loading, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progressBar);// 进度条
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        ImageView imageView = (ImageView) v.findViewById(R.id.img);
        if (0 == loadingType){// 进度条
            progressBar.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            if (imgId != 0){
                progressBar.setIndeterminateDrawable(context.getResources().getDrawable(imgId));
            }
        }else {// 图片
            progressBar.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            if (imgId != 0){
                imageView.setImageResource(imgId);
            }
        }
        if (isAnim){
            // 加载动画
            Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                    context, R.anim.loading_animation);
            // 使用ImageView显示动画
            imageView.startAnimation(hyperspaceJumpAnimation);
        }
        tipTextView.setText(msg);// 设置加载信息
        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog
        loadingDialog.setCanceledOnTouchOutside(isCancelAble);// 可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(width * 1 / 3, width * 1 / 3));// 设置布局
        return loadingDialog;
    }

    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
