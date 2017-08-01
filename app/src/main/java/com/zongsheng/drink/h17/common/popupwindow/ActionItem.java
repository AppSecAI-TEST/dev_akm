package com.zongsheng.drink.h17.common.popupwindow;

import android.content.Context;

/**
 * @author yangyu
 *	功能描述：弹窗内部子类项（绘制标题和图标）
 */
public class ActionItem {
	//定义文本对象
	public CharSequence mTitle;
	
	public ActionItem(CharSequence title){
		this.mTitle = title;
	}
	
	public ActionItem(Context context, int titleId){
		this.mTitle = context.getResources().getText(titleId);
	}
	
	public ActionItem(Context context, CharSequence title) {
		this.mTitle = title;
	}
}

