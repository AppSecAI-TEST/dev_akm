package com.zongsheng.drink.h17.common;

import android.widget.ListView;

public class MyListView extends ListView {
	public MyListView(android.content.Context context,
					  android.util.AttributeSet attrs) {
		super(context, attrs);
	}
	public MyListView(android.content.Context context) {
		super(context);
	}
	/**
	 * 设置小于10个item不滚动
	 */
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		if (getCount() <= 10){
			super.onMeasure(widthMeasureSpec, expandSpec);
		}else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}