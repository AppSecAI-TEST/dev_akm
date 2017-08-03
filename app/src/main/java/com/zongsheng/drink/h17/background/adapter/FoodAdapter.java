package com.zongsheng.drink.h17.background.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.bumptech.glide.Glide;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.common.OpenGeziDoorListener;
import com.zongsheng.drink.h17.background.dialog.DialogUtil;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.popupwindow.ActionItem;
import com.zongsheng.drink.h17.common.popupwindow.TitlePopup;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 饮料商品
 * Created by 谢家勋 on 2016/8/23.
 */
public class FoodAdapter extends BaseAdapter {
    private List<GoodsInfo> goodsInfoList;
    private LayoutInflater mInflater;
    private Context context;
    private AlertView alertView;

    /**
     * 开门监听
     */
    private OpenGeziDoorListener openGeziDoorListener;

    private int position5 = -1;

    public FoodAdapter(Context context, List<GoodsInfo> goodsInfoList) {
        super();
        this.mInflater = LayoutInflater.from(context);
        this.goodsInfoList = goodsInfoList;
        this.context = context;
    }

    public FoodAdapter(Context context, List<GoodsInfo> goodsInfoList, OpenGeziDoorListener openGeziDoorListener) {
        this.mInflater = LayoutInflater.from(context);
        this.goodsInfoList = goodsInfoList;
        this.context = context;
        this.openGeziDoorListener = openGeziDoorListener;
    }

    @Override
    public int getCount() {
        return goodsInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return goodsInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_food, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final GoodsInfo goodsInfo = goodsInfoList.get(position);

        holder.tvHuodao.setText("货道-" + goodsInfo.getRoad_no());
        // 进来如果发现 最大库存数小于本地库存数 一般指的是在外面修改了最大库存
        holder.tvStatus.setText("(" + " 缺货 --)");

        if (goodsInfo != null && goodsInfo.getGoodsID() != null && !"".equals(goodsInfo.getGoodsID())) {

            // 进来如果发现 最大库存数小于本地库存数 一般指的是在外面修改了最大库存
            holder.tvStatus.setText("(" + "缺货 " + (goodsInfo.getMaxKucun() - Integer.parseInt(goodsInfo.getKuCun())) + ")");

            holder.rlShow.setVisibility(View.VISIBLE);
            holder.tvChoose.setVisibility(View.GONE);
            //商品名称
            holder.tvGoodsName.setText(goodsInfo.getGoodsName());
            // 本地最大库存
            holder.tvGoodsLocal.setText("现本地库存：" + goodsInfo.getKuCun());
            // 线上库存
            holder.tvGoodsOnline.setText("线上库存：" + goodsInfo.getOnlineKuCun());
            // 商品价格
            // 格式转换
            holder.tvGoodsPrice.setText("¥ " + (Float.parseFloat(goodsInfo.getPrice()) * 0.1));

            //开门！！！！！！
            holder.tvOpenDoor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openGeziDoorListener.openGeziDoor(goodsInfo.getRoad_no());
                }
            });

            // 设置图片
            Glide.with(context)
                    .load(goodsInfo.getGoodsImage())
                    .dontAnimate()
                    .into(holder.ivGoodsImg);

            // 图片点击事件
            holder.ivGoodsImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chooseGoodsListener != null) {
                        chooseGoodsListener.onClick(v, position);
                    }
                }
            });

            if (goodsInfo.getMaxKucun() >= 0) {
                holder.tvBuman.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //if ("aokema".equals(VersonSetting.versionType) || "aokema23".equals(VersonSetting.versionType)) {
                        if ("清零".equals(holder.tvBuman.getText().toString())) {
                            if (goodsInfo.getLocalKuCunForCheck() > 0) {
                                // 不能修改
                                alertView = new AlertView("提示", "该货道没有售完,不能清零",
                                        null, new String[]{"确认"}, null, context, AlertView.Style.Alert, DataUtil.dip2px(context, Double.parseDouble(context.getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(Object o, int position) {
                                        if (-1 == position) {
                                            alertView.dismiss();
                                        } else {
                                            alertView.dismiss();
                                        }
                                    }
                                }).setCancelable(true).setOnDismissListener(null);
                                alertView.show();
                                return;
                            }
                        }
                        //}

                        changeInfoListener.onCHange();
                        if ("补满".equals(holder.tvBuman.getText().toString())) {
                            goodsInfo.setKuCun(String.valueOf(goodsInfo.getMaxKucun()));
                        } else {
                            goodsInfo.setKuCun(String.valueOf(0));
                        }

                        holder.tvGoodsLocal.setText("现本地库存：" + goodsInfo.getKuCun());
                        // 设置缺货数
                        holder.tvStatus.setText("(" + "缺货 " + (goodsInfo.getMaxKucun() - Integer.parseInt(goodsInfo.getKuCun())) + ")");

                        // 设置补满按钮
                        if (goodsInfo.getMaxKucun() == Integer.parseInt(goodsInfo.getKuCun())) {
                            holder.tvBuman.setText("清零");
                            holder.tvBuman.setBackgroundResource(R.drawable.bg_button_red);
                        } else {
                            holder.tvBuman.setText("补满");
                            holder.tvBuman.setBackgroundResource(R.drawable.bg_green);
                        }

                    }
                });

            }

            // 设置补满按钮
            if (1 == Integer.parseInt(goodsInfo.getKuCun())) {
                holder.tvBuman.setText("清零");
                holder.tvBuman.setBackgroundResource(R.drawable.bg_button_red);
            } else {
                holder.tvBuman.setText("补满");
                holder.tvBuman.setBackgroundResource(R.drawable.bg_green);
            }

//            // 补满
//            holder.tvBuman.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    holder.tvGoodsLocal.setText("现本地库存：" + goodsInfo.getKuCun());
//                    goodsInfo.setKuCun(String.valueOf(1));
//                    // 设置缺货数
//                    holder.tvStatus.setText("(" + "缺货 " + (goodsInfo.getMaxKucun() - Integer.parseInt(goodsInfo.getKuCun())) + ")");
//                }
//            });

            // 更多点击
            holder.ivMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    position5 = position;
                    DialogUtil dialogUtil = new DialogUtil(context);
                    dialogUtil.addItem("清空商品料道");
                    dialogUtil.addItem("修改价格");
                    //TODO:这里注释了一行，待理解
//                    dialogUtil.addItem("修改最大库存");
                    dialogUtil.setOnDialogListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:// 清空
                                    alertView = new AlertView("提示", "确定要清空商品料道吗？", "取消", new String[]{"确认"}, null, context, AlertView.Style.Alert, DataUtil.dip2px(context, Double.parseDouble(context.getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                                        @Override
                                        public void onItemClick(Object o, int position) {
                                            if (-1 == position) {
                                                alertView.dismiss();
                                            } else {
                                                changeInfoListener.onCHange();
                                                //清空商品料道 等于是把这个model的其他值设为默认
                                                GoodsInfo info = new GoodsInfo();
                                                info.setRoad_no(goodsInfo.getRoad_no());
                                                info.setZhidingCount(0);
                                                info.setLocalKuCunForCheck(0);
                                                info.setMachineID(goodsInfo.getMachineID());
                                                info.setMaxKucun(1);
                                                info.setKuCun(goodsInfo.getKuCun());
                                                goodsInfoList.set(position5, info);

//                                                notifyDataSetInvalidated();
                                                notifyDataSetChanged();
                                                alertView.dismiss();
                                            }
                                        }
                                    }).setCancelable(true).setOnDismissListener(null);
                                    alertView.show();
                                    break;
                                case 1:// 修改价格
                                    if (clickListener != null) {
                                        clickListener.onClick(v, position, 1, goodsInfo.getPrice());
                                    }
                                    break;

                                default:
                                    break;
                            }
                        }
                    });
                    dialogUtil.showDialog();
                }
            });

        } else {
            holder.rlShow.setVisibility(View.GONE);
            holder.tvChoose.setVisibility(View.VISIBLE);
            holder.tvChoose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chooseGoodsListener != null) {
                        chooseGoodsListener.onClick(v, position);
                    }
                }
            });
        }

        return convertView;
    }


    /**
     * 单击事件监听器
     */
    private onDialogShowListener clickListener = null;

    public void setOnDialogShowListener(onDialogShowListener listener) {
        clickListener = listener;
    }

    public interface onDialogShowListener {
        void onClick(View v, int position, int type, String kucun);
    }

    private OnChooseGoodsListener chooseGoodsListener = null;

    public void setOnChooseGoodsListener(OnChooseGoodsListener listener) {
        chooseGoodsListener = listener;
    }

    /**
     * 改变listener
     */
    private OnChangeInfoListener changeInfoListener = null;

    public void setOnChangeInfoListener(OnChangeInfoListener listener) {
        changeInfoListener = listener;
    }

    public interface OnChangeInfoListener {
        void onCHange();
    }

    public interface OnChooseGoodsListener {
        void onClick(View v, int position);
    }

    static class ViewHolder {
        @BindView(R.id.tv_huodao)
        TextView tvHuodao;
        @BindView(R.id.tv_status)
        TextView tvStatus;
        @BindView(R.id.iv_goods_img)
        ImageView ivGoodsImg;
        @BindView(R.id.tv_goods_name)
        TextView tvGoodsName;
        @BindView(R.id.tv_goods_price)
        TextView tvGoodsPrice;
        @BindView(R.id.tv_goods_local)
        TextView tvGoodsLocal;
        @BindView(R.id.tv_goods_online)
        TextView tvGoodsOnline;
        @BindView(R.id.tv_buman)
        TextView tvBuman;
        @BindView(R.id.tv_open_door)
        TextView tvOpenDoor;
        @BindView(R.id.rl_show)
        RelativeLayout rlShow;
        @BindView(R.id.tv_choose)
        TextView tvChoose;
        @BindView(R.id.iv_more)
        ImageView ivMore;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
