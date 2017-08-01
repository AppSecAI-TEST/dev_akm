package com.zongsheng.drink.h17.background.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.bumptech.glide.Glide;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.dialog.DialogUtil;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.popupwindow.ActionItem;
import com.zongsheng.drink.h17.common.popupwindow.TitlePopup;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by torry on 2017-07-20.
 */

public class DeskAdapter extends BaseAdapter {

    private List<GoodsInfo> tempList;
    private List<GoodsInfo> goodsInfoList;
    private LayoutInflater inflater;
    private Context context;
    private AlertView alertView;
    private int position5 = -1;

    public DeskAdapter(Context context, List<GoodsInfo> goodsInfoList, List<GoodsInfo> tempList) {
        this.context = context;
        this.goodsInfoList = goodsInfoList;
        this.tempList = tempList;
        inflater = LayoutInflater.from(context);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_desk, null);
            holder = new ViewHolder(convertView);
            holder.tvZhiding.addTextChangedListener(new ZhidingTextWatcher(holder));
            holder.tvZhiding.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final GoodsInfo goodsInfo = goodsInfoList.get(position);
        holder.tvHuodao.setText("货道-" + goodsInfo.getRoad_no());
        holder.tvStatus.setText("（缺货--）");
        holder.tvZhiding.setTag(position);

        if (goodsInfo != null && goodsInfo.getGoodsID() != null && !"".equals(goodsInfo.getGoodsID())) {
            holder.tvStatus.setText("(" + "缺货 " + (goodsInfo.getMaxKucun() - Integer.parseInt(goodsInfo.getKuCun())) + ")");

            holder.rlShow.setVisibility(View.VISIBLE);
            holder.tvChoose.setVisibility(View.GONE);
            // 商品名称
            holder.tvGoodsName.setText(goodsInfo.getGoodsName());
            // 最大库存
            holder.tvGoodsMax.setText("最大库存：" + goodsInfo.getMaxKucun());
            // 本地最大库存
            holder.tvGoodsLocal.setText("现本地库存：" + goodsInfo.getKuCun());
            // 线上库存
            holder.tvGoodsOnline.setText("线上库存：" + goodsInfo.getOnlineKuCun());
            // 商品价格
            // 格式转换
            holder.tvGoodsPrice.setText("¥ " + (Float.parseFloat(goodsInfo.getPrice()) * 0.1));
            holder.tvZhiding.setText("");
            if (Integer.parseInt(goodsInfo.getKuCun()) > goodsInfo.getLocalKuCunForCheck()) {
                holder.tvCount.setText((Integer.parseInt(goodsInfo.getKuCun()) - goodsInfo.getLocalKuCunForCheck()) + "");
            } else {
                holder.tvCount.setText("0");
            }

            Glide.with(context).load(goodsInfo.getGoodsImage()).dontAnimate().into(holder.ivGoodsImg);

            holder.ivGoodsImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chooseGoodsListener != null) {
                        chooseGoodsListener.onClick(v, position);
                    }
                }
            });

            if (goodsInfo.getMaxKucun() > 0) {
                holder.ivAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int count = Integer.parseInt(holder.tvCount.getText().toString());
                        count++;
                        if (Integer.parseInt(goodsInfo.getKuCun()) + 1 <= goodsInfo.getMaxKucun()) {
                            changeInfoListener.onChange();
                            goodsInfo.setKuCun(String.valueOf(1 + Integer.parseInt(goodsInfo.getKuCun())));
                            holder.tvCount.setText(count + "");
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
                    }
                });
            }

            // 设置补满按钮
            if (goodsInfo.getMaxKucun() == Integer.parseInt(goodsInfo.getKuCun())) {
                holder.tvBuman.setText("清零");
                holder.tvBuman.setBackgroundResource(R.drawable.bg_button_red);
            } else {
                holder.tvBuman.setText("补满");
                holder.tvBuman.setBackgroundResource(R.drawable.bg_green);
            }

            if (goodsInfo.getMaxKucun() < Integer.parseInt(goodsInfo.getKuCun())) {
                goodsInfo.setKuCun(String.valueOf(goodsInfo.getMaxKucun()));
                holder.tvGoodsLocal.setText("现本地库存：" + String.valueOf(goodsInfo.getMaxKucun()));
                holder.tvStatus.setText("(" + "缺货 " + (goodsInfo.getMaxKucun() - Integer.parseInt(goodsInfo.getKuCun())) + ")");
                int count = Integer.parseInt(goodsInfo.getKuCun()) - goodsInfo.getLocalKuCunForCheck();
                holder.tvCount.setText((count > 0 ? count : 0) + "");
            }

            //点击减号
            holder.ivReduce.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int count = Integer.parseInt(holder.tvCount.getText().toString());
                    count--;
                    if (count >= 0) {
                        changeInfoListener.onChange();
                        Toast.makeText(context, "不能超过最大库存数", Toast.LENGTH_SHORT);
                        holder.tvCount.setText(String.valueOf(count));
                        goodsInfo.setKuCun(String.valueOf((Integer.parseInt(goodsInfo.getKuCun()) - 1)));
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
                }
            });

            holder.tvZhiding.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            if (goodsInfo.getMaxKucun() >= 0) {
                holder.tvBuman.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 获取现在的最大库存,设置给本地
                        if (holder.tvGoodsMax.getText().toString() != null) {
                            changeInfoListener.onChange();
                            if ("补满".equals(holder.tvBuman.getText().toString())) {
                                goodsInfo.setKuCun(String.valueOf(goodsInfo.getMaxKucun()));
                            } else {
//                                goodsInfo.setKuCun(String.valueOf(0));
                                goodsInfo.setKuCun(String.valueOf(goodsInfo.getLocalKuCunForCheck()));
                            }
                            holder.tvGoodsLocal.setText("现本地库存：" + goodsInfo.getKuCun());
                            if (Integer.parseInt(goodsInfoList.get(position).getKuCun()) >= goodsInfoList.get(position).getLocalKuCunForCheck()) {
                                holder.tvCount.setText((Integer.parseInt(goodsInfoList.get(position).getKuCun()) - goodsInfoList.get(position).getLocalKuCunForCheck()) + "");
                            } else {
                                holder.tvCount.setText("0");
                            }
//                            holder.tvCount.setText(String.valueOf(Integer.parseInt(goodsInfo.getKuCun()) - goodsInfo.getLocalKuCunForCheck()));
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
                    }
                });
            }

            holder.tvMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    position5 = position;
                    DialogUtil dialogUtil = new DialogUtil(context);
                    dialogUtil.addItem("清空商品料道");
                    dialogUtil.addItem("修改价格");
                    dialogUtil.addItem("修改最大库存");
                    dialogUtil.setOnDialogListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    alertView = new AlertView("提示", "确认要清空商品料道吗？", "取消", new String[]{"确认"}, null, context, AlertView.Style.Alert,
                                            DataUtil.dip2px(context, Double.parseDouble(context.getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                                        @Override
                                        public void onItemClick(Object o, int position) {
                                            if (-1 == position) {
                                                alertView.dismiss();
                                            } else {
                                                changeInfoListener.onChange();
                                                GoodsInfo info = new GoodsInfo();
                                                info.setRoad_no(goodsInfo.getRoad_no());
                                                info.setGoodsCode("");
                                                info.setZhidingCount(0);
                                                info.setLocalKuCunForCheck(0);
                                                info.setMachineID(goodsInfo.getMachineID());
                                                info.setMaxKucun(goodsInfo.getMaxKucun());
                                                info.setKuCun(goodsInfo.getKuCun());
                                                goodsInfoList.set(position5, info);

                                                notifyDataSetChanged();
                                                alertView.dismiss();
                                            }
                                        }
                                    }).setCancelable(true).setOnDismissListener(null);
                                    alertView.show();
                                    break;

                                case 1:
                                    if (dialogShowListener != null) {
                                        dialogShowListener.onClick(v, position, 1, goodsInfo.getPrice());
                                    }
                                    break;
                                case 2:
                                    if (dialogShowListener != null) {
                                        dialogShowListener.onClick(v, position, 2, String.valueOf(goodsInfo.getMaxKucun()));
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

    class ZhidingTextWatcher implements TextWatcher {

        public ZhidingTextWatcher(ViewHolder holder) {
            mHolder = holder;
        }

        ViewHolder mHolder;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String temp = s.toString();
            if ("".equals(temp)) {
                return;
            }
            int result = Integer.parseInt(temp);
            temp = String.valueOf(result);
            int position = (Integer) mHolder.tvZhiding.getTag();
            if ((result + goodsInfoList.get(position).getLocalKuCunForCheck()) > goodsInfoList.get(position).getMaxKucun()) {
                temp = String.valueOf(goodsInfoList.get(position).getMaxKucun() - goodsInfoList.get(position).getLocalKuCunForCheck());
            }
            goodsInfoList.get(position).setKuCun(String.valueOf(Integer.parseInt(temp) + goodsInfoList.get(position).getLocalKuCunForCheck()));
            changeInfoListener.onChange();
            mHolder.tvGoodsLocal.setText("现本地库存：" + goodsInfoList.get(position).getKuCun());
            goodsInfoList.get(position).setZhidingCount(Integer.parseInt(temp));
            mHolder.tvCount.setText(temp);

            mHolder.tvStatus.setText("（缺货" + (goodsInfoList.get(position).getMaxKucun() - Integer.parseInt(goodsInfoList.get(position).getKuCun())) + "）");

            if (goodsInfoList.get(position).getMaxKucun() == Integer.parseInt(goodsInfoList.get(position).getKuCun())) {
                mHolder.tvBuman.setText("清零");
                mHolder.tvBuman.setBackgroundResource(R.drawable.bg_button_red);
            } else {
                mHolder.tvBuman.setText("补满");
                mHolder.tvBuman.setBackgroundResource(R.drawable.bg_green);
            }
        }
    }


    class ViewHolder {

        ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }

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
        @BindView(R.id.tv_goods_max)
        TextView tvGoodsMax;
        @BindView(R.id.tv_goods_local)
        TextView tvGoodsLocal;
        @BindView(R.id.tv_goods_online)
        TextView tvGoodsOnline;
        @BindView(R.id.iv_reduce)
        ImageButton ivReduce;
        @BindView(R.id.tv_count)
        TextView tvCount;
        @BindView(R.id.iv_add)
        ImageButton ivAdd;
        @BindView(R.id.tv_zhiding)
        EditText tvZhiding;
        @BindView(R.id.tv_buman)
        TextView tvBuman;
        @BindView(R.id.tv_more)
        TextView tvMore;
        @BindView(R.id.rl_show)
        RelativeLayout rlShow;
        @BindView(R.id.tv_choose)
        TextView tvChoose;
    }

    ChangeInfoListener changeInfoListener;

    public void setOnChangeInfoListener(ChangeInfoListener listener) {
        changeInfoListener = listener;
    }

    public interface ChangeInfoListener {
        void onChange();
    }

    ChooseGoodsListener chooseGoodsListener;

    public void setChooseGoodsListener(ChooseGoodsListener listener) {
        chooseGoodsListener = listener;
    }

    public interface ChooseGoodsListener {
        void onClick(View view, int position);
    }

    DialogShowListener dialogShowListener;

    public void setDialogShowListener(DialogShowListener dialogShowListener) {
        this.dialogShowListener = dialogShowListener;
    }

    public interface DialogShowListener {
        void onClick(View v, int position, int type, String kucun);
    }
}
