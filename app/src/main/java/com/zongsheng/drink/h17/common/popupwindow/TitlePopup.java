package com.zongsheng.drink.h17.common.popupwindow;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.zongsheng.drink.h17.R;

import java.util.ArrayList;


/**
 * @author yangyu
 *         功能描述：标题按钮上的弹窗（继承自PopupWindow）
 */
public class TitlePopup extends PopupWindow {
    private Context mContext;

    //列表弹窗的间隔
    protected final int LIST_PADDING = 320;

    //实例化一个矩形
    private Rect mRect = new Rect();

    //坐标的位置（x、y）
    private final int[] mLocation = new int[2];

    //屏幕的宽度和高度
    private int mScreenWidth, mScreenHeight;

    //判断是否需要添加或更新列表子类项
    private boolean mIsDirty;

    //位置在中心
    private int popupGravity = Gravity.CENTER_HORIZONTAL;

    //弹窗子类项选中时的监听
    private OnItemOnClickListener mItemOnClickListener;

    //定义列表对象
    private ListView mListView;

    //定义弹窗子类项列表
    private ArrayList<ActionItem> mActionItems = new ArrayList<ActionItem>();

    //	public TitlePopup(Context context){
//		//设置布局的参数
//		this(context, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//	}
    // 选中的位置
    private int selectedPosition = -1;

    // 整个的View
    private View view;
    private int weizhi_type;

    public TitlePopup(Context context, int width, int height) {
//		Log.i("1111111",""+height);
        this.mContext = context;

        //设置可以获得焦点
        setFocusable(true);
        //设置弹窗内可点击
        setTouchable(true);
        //设置弹窗外可点击
        setOutsideTouchable(true);

        //获得屏幕的宽度和高度
		mScreenWidth = PopupUtil.getScreenWidth(mContext);
//
    	mScreenHeight = PopupUtil.getScreenHeight(mContext);
        // 防止宽度高度 = 0
        if (width == 0){
            width = 100;
        }
        if (height == 0){
            height = 100;
        }
        //设置弹窗的宽度和高度
        setWidth(width);
//		if(mActionItems.size()>7){
//			setHeight(height);
//		}else {
        setHeight(height);
        Log.i("宽度高度", getWidth() + "/" + getHeight());
//		}
//		setWidth(width);
//
        setBackgroundDrawable(new BitmapDrawable());

        //设置弹窗的布局界面
        setContentView(view = LayoutInflater.from(mContext).inflate(R.layout.title_popup, null));

        initUI();
    }

    /**
     * 初始化弹窗列表
     */
    private void initUI() {
        mListView = (ListView) getContentView().findViewById(R.id.title_list);

        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
                //点击子类项后，弹窗消失
                dismiss();

                if (mItemOnClickListener != null)
                    mItemOnClickListener.onItemClick(mActionItems.get(index), index);
            }
        });
    }

    /**
     * 显示弹窗列表界面
     */
    public void show(View view) {
        //获得点击屏幕的位置坐标
        view.getLocationOnScreen(mLocation);

        //设置矩形的大小
//		mRect.set(mLocation[0], mLocation[1], mLocation[0],mLocation[1] + view.getHeight());

        //判断是否需要添加或更新列表子类项
        if (mIsDirty) {
            populateActions();
        }
        Log.e("11111","mScreenHeight="+mScreenHeight+",mLocation="+mLocation[1]+",getHidth="+view.getHeight());
        if((mScreenHeight - view.getHeight()*4) <= mLocation[1] && (mScreenWidth - view.getWidth()*4) >= mLocation[0]){
            // 显示居中
            weizhi_type=0;
            showAsDropDown(view, view.getWidth() / 2 - getWidth() / 2, -view.getHeight() * 4);
            this.view.setBackground(mContext.getResources().getDrawable(R.drawable.bg_all_white_down));
        }else{
            // 显示居中
            weizhi_type=1;
            showAsDropDown(view, view.getWidth() / 2 - getWidth() / 2, 0);
            this.view.setBackground(mContext.getResources().getDrawable(R.drawable.bg_all_white_up));
        }
        //showAtLocation(view, popupGravity, mScreenWidth - LIST_PADDING - (getWidth() / 2), mRect.bottom);

        //显示弹窗的位置
        Log.i("viewHeight", view.getWidth() / 2 - getWidth() / 2 + view.toString());

//		showAtLocation(view, popupGravity, 0 , LIST_PADDING-mScreenHeight/2);
//		showAtLocation(view, popupGravity, 0 , mRect.bottom);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    /**
     * 设置选中点
     *
     * @param selectedPosition
     */
    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        if (selectedPosition == 0) {
           // view.setBackground(mContext.getResources().getDrawable(R.drawable.bg_all_white_up));
            view.setPadding(0, 0, 0, 8);
        } else {
            //view.setBackground(mContext.getResources().getDrawable(R.drawable.bg_all_white_up));
            view.setPadding(0, 10, 0, 8);
        }
    }

    /**
     * 设置弹窗列表子项
     */
    private void populateActions() {
        mIsDirty = false;

        //设置列表的适配器
        mListView.setAdapter(new BaseAdapter() {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = null;

                if (convertView == null) {
                    textView = new TextView(mContext);
                    textView.setTextColor(mContext.getResources().getColor(R.color.word_black));
                    textView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT));
                    textView.setTextSize(20);
                    //设置文本居中
                   // textView.setGravity(Gravity.CENTER);
                    textView.setGravity(Gravity.CENTER_HORIZONTAL);
                    //设置文本域的范围
                    textView.setPadding(25, 20, 25, 20);
                    //设置文本在一行内显示（不换行）
                    textView.setSingleLine(true);
                } else {
                    textView = (TextView) convertView;
                }

                ActionItem item = mActionItems.get(position);
                if (selectedPosition != -1) {
                    // 有选择颜色
                    if (position == selectedPosition) {
                        textView.setBackgroundColor(mContext.getResources().getColor(R.color.common_green));
                        textView.setTextColor(Color.WHITE);
                        view.setBackgroundColor(mContext.getResources().getColor(R.color.touming));
                        view.setPadding(0, 10, 0, 8);
                       // view.setBackground(mContext.getResources().getDrawable(R.drawable.bg_all_white_up));
//                        if (position == getCount() - 1) {
//                            // 选中最后一个
//                            view.setBackground(mContext.getResources().getDrawable(R.drawable.bg_bottom_selected));
//                            view.setPadding(0, 10, 0, 8);
//                        } else if (position == 0) {
//                            // 选中第一个
//                            view.setBackground(mContext.getResources().getDrawable(R.drawable.bg_top_selected));
//                            view.setPadding(0, 10, 0, 8);
//                        } else {
//                            view.setBackground(mContext.getResources().getDrawable(R.drawable.bg_all_white_up));
//                            view.setPadding(0, 10, 0, 8);
//                        }
                    } else {
                        textView.setBackgroundColor(Color.WHITE);
                        textView.setTextColor(mContext.getResources().getColor(R.color.word_black));
                        view.setBackgroundColor(mContext.getResources().getColor(R.color.touming));
                    }
                } else {
                    textView.setBackgroundColor(mContext.getResources().getColor(R.color.common_withe));
                    textView.setTextColor(mContext.getResources().getColor(R.color.word_black));
                    view.setBackgroundColor(mContext.getResources().getColor(R.color.touming));
                    view.setPadding(0, 10, 0, 8);
                }

                //设置文本文字
                textView.setText(item.mTitle);

                return textView;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public Object getItem(int position) {
                return mActionItems.get(position);
            }

            @Override
            public int getCount() {
                return mActionItems.size();
            }
        });
    }

    /**
     * 添加子类项
     */
    public void addAction(ActionItem action) {
        if (action != null) {
            mActionItems.add(action);
            mIsDirty = true;
        }
    }

    /**
     * 清除子类项
     */
    public void cleanAction() {
        if (mActionItems.isEmpty()) {
            mActionItems.clear();
            mIsDirty = true;
        }
    }

    /**
     * 根据位置得到子类项
     */
    public ActionItem getAction(int position) {
        if (position < 0 || position > mActionItems.size())
            return null;
        return mActionItems.get(position);
    }

    /**
     * 设置监听事件
     */
    public void setItemOnClickListener(OnItemOnClickListener onItemOnClickListener) {
        this.mItemOnClickListener = onItemOnClickListener;
    }

    /**
     * @author yangyu
     *         功能描述：弹窗子类项按钮监听事件
     */
    public static interface OnItemOnClickListener {
        public void onItemClick(ActionItem item, int position);
    }

    public ArrayList<ActionItem> getmActionItems() {
        return mActionItems;
    }

    public void setmActionItems(ArrayList<ActionItem> mActionItems) {
        this.mActionItems = mActionItems;
    }
}

