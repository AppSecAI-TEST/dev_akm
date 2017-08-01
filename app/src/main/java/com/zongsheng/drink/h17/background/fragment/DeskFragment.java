package com.zongsheng.drink.h17.background.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Response;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.MarkLog;
import com.zongsheng.drink.h17.background.activity.BuhuoActivity;
import com.zongsheng.drink.h17.background.adapter.DeskAdapter;
import com.zongsheng.drink.h17.background.adapter.MubanAdapter;
import com.zongsheng.drink.h17.background.adapter.PandianAdapter;
import com.zongsheng.drink.h17.background.bean.BindDesk;
import com.zongsheng.drink.h17.background.bean.PanDInfo;
import com.zongsheng.drink.h17.background.bean.RoadTemple;
import com.zongsheng.drink.h17.background.bean.RoadTempleDt;
import com.zongsheng.drink.h17.background.bean.ShopGoods;
import com.zongsheng.drink.h17.background.bean.ShopType;
import com.zongsheng.drink.h17.background.pullrefresh.PullToRefreshLayout;
import com.zongsheng.drink.h17.common.Constant;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.LoadingUtil;
import com.zongsheng.drink.h17.common.MyListView;
import com.zongsheng.drink.h17.common.NetWorkRequImpl;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.common.ToastUtils;
import com.zongsheng.drink.h17.common.mylistener.ListenerManager;
import com.zongsheng.drink.h17.common.popupwindow.ActionItem;
import com.zongsheng.drink.h17.common.popupwindow.TitlePopup;
import com.zongsheng.drink.h17.common.sortlistview.CharacterParser;
import com.zongsheng.drink.h17.common.sortlistview.ClearEditText;
import com.zongsheng.drink.h17.common.sortlistview.PinyinComparator;
import com.zongsheng.drink.h17.common.sortlistview.SideBar;
import com.zongsheng.drink.h17.common.sortlistview.SortAdapter;
import com.zongsheng.drink.h17.common.sortlistview.SortModel;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.interfaces.INetWorkRequCallBackListener;
import com.zongsheng.drink.h17.interfaces.INetWorkRequInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * 副柜
 * Created by 谢家勋 on 2016/8/23.
 */
public class DeskFragment extends Fragment implements INetWorkRequCallBackListener, PullToRefreshLayout.OnRefreshListener {

    @BindView(R.id.tv_submit)
    TextView tvSubmit;
    @BindView(R.id.tv_buman)
    TextView tvBuman;
    @BindView(R.id.tv_more)
    TextView tvMore;
    @BindView(R.id.gv_goods)
    GridView gvGoods;
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.edt_input)
    EditText edtInput;
    @BindView(R.id.tv_dialog_confirm)
    TextView tvDialogConfirm;
    @BindView(R.id.iv_dialog_close)
    ImageView ivDialogClose;
    @BindView(R.id.rl_dialog)
    RelativeLayout rlDialog;
    @BindView(R.id.iv_close_muban)
    ImageView ivCloseMuban;
    @BindView(R.id.mlv_muban)
    MyListView mlvMuban;
    @BindView(R.id.ll_muban)
    LinearLayout llMuban;
    @BindView(R.id.rl_muban)
    RelativeLayout rlMuban;
    @BindView(R.id.ll_pandian)
    LinearLayout llPandian;
    @BindView(R.id.rl_pandian)
    RelativeLayout rlPandian;
    @BindView(R.id.edt_code)
    EditText edtCode;
    @BindView(R.id.tv_1)
    TextView tv1;
    @BindView(R.id.tv_2)
    TextView tv2;
    @BindView(R.id.tv_3)
    TextView tv3;
    @BindView(R.id.iv_delete)
    ImageView ivDelete;
    @BindView(R.id.tv_4)
    TextView tv4;
    @BindView(R.id.tv_5)
    TextView tv5;
    @BindView(R.id.tv_6)
    TextView tv6;
    @BindView(R.id.tv_clear)
    TextView tvClear;
    @BindView(R.id.tv_7)
    TextView tv7;
    @BindView(R.id.tv_8)
    TextView tv8;
    @BindView(R.id.tv_9)
    TextView tv9;
    @BindView(R.id.tv_star)
    TextView tvStar;
    @BindView(R.id.tv_0)
    TextView tv0;
    @BindView(R.id.tv_jing)
    TextView tvJing;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    @BindView(R.id.rl_keyboard)
    RelativeLayout rlKeyboard;
    @BindView(R.id.tv_drink)
    TextView tvDrink;
    @BindView(R.id.tv_all)
    TextView tvAll;
    @BindView(R.id.filter_edit)
    ClearEditText filterEdit;
    @BindView(R.id.country_lvcountry)
    ListView countryLvcountry;
    @BindView(R.id.dialog)
    TextView dialog;
    @BindView(R.id.sidrbar)
    SideBar sidrbar;
    @BindView(R.id.rl_choose)
    RelativeLayout rlChoose;
    @BindView(R.id.rl_drink_click)
    RelativeLayout rlDrinkClick;
    @BindView(R.id.rl_all_click)
    RelativeLayout rlAllClick;
    @BindView(R.id.refresh_view)
    PullToRefreshLayout ptrl;
    @BindView(R.id.content_view)
    ListView listView;

    /**
     * 自定义的Dialog
     */
    private Dialog loadingDialog;// 加载中
    private Dialog loadingDialogEnd;// 加载完成
    private Dialog loadingDialogFail;// 加载失败

    View view;
    BindDesk desk;
    AlertView alertView;
    /**
     * 取得的商品列表
     */
    List<GoodsInfo> goodsInfoList = new ArrayList<>();
    Map<Integer, GoodsInfo> goodsInfoMap;
    private TitlePopup morePopup;
    private DeskAdapter deskAdapter;

    private int goodInfoPosition = -1;
    private int x = 0;
    private int y = 0;

    private List<GoodsInfo> tempList = new ArrayList<>();
    private MubanAdapter mubanAdapter;
    private List<RoadTemple> mubanInfoList = new ArrayList<>();

    private PandianAdapter pandianAdapter;
    private List<PanDInfo> pandianInfoList = new ArrayList<>();


    /**
     * 替换商品时列表
     */
    private SortAdapter sortAdapter;
    private CharacterParser characterParser;
    private List<SortModel> sortModel = new ArrayList<>();
    private PinyinComparator pinyinComparator;
    private TitlePopup goodsPop;
    private TitlePopup typePop;

    private Realm realm;
    private List<ShopGoods> sGoods;
    private RealmResults<ShopType> shopTypes;
    private int pricePosition = -1;
    public boolean isChanged = false;

    /**
     * popupwindow
     */
    private int width = 0;
    private int height = 0;

    /**
     * 修改价格
     */
    private String code = "";

    private String machineSn;
    private INetWorkRequInterface iNetWorkRequInterface;

    private int totalPage = 0;
    private int currentPage = 1;

    public DeskFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
            return view;
        }
        realm = Realm.getDefaultInstance();
        iNetWorkRequInterface = new NetWorkRequImpl(this);
        view = inflater.inflate(R.layout.fragment_desk, null);
        ButterKnife.bind(this, view);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        height = dm.heightPixels;
        width = dm.widthPixels;

        shopTypes = realm.where(ShopType.class).findAll().sort("typeId", Sort.ASCENDING);

        init();
        initChoose();
        initPop();
        return view;
    }

    /**
     * 设置副柜信息
     */
    public void setSelectDesk(BindDesk desk) {
        this.desk = desk;
        isChanged = false;
        init();
    }

    /**
     * 重新加载数据
     */
    public void resetData() {
        for (GoodsInfo goodsInfo : goodsInfoList) {
            goodsInfo.setZhidingCount(0);
            goodsInfo.setLocalKuCunForCheck(Integer.parseInt(goodsInfo.getKuCun()));
            goodsInfo.setOnlineKuCun(Integer.parseInt(goodsInfo.getKuCun()));
            deskAdapter.notifyDataSetChanged();
        }
    }

    private void init() {
        if (desk == null) {
            return;
        }
        int countHuodao = MyApplication.getInstance().getDeskRoadCount();
        List<Integer> roadNoList = MyApplication.getInstance().getDeskRoadList();
        machineSn = desk.getMachineSn();
        goodsInfoList = new ArrayList<>();
        goodsInfoMap = new HashMap<>();

        Log.e("fugui", machineSn);
        for (Integer road : roadNoList) {
            GoodsInfo goodsInfo = new GoodsInfo();
            goodsInfo.setRoad_no(road);
            goodsInfo.setKuCun(String.valueOf(0));
            goodsInfo.setMachineID(machineSn);
            goodsInfoMap.put(road, goodsInfo);
        }
        RealmResults<GoodsInfo> sort = realm.where(GoodsInfo.class).equalTo("machineID", machineSn).findAll().sort("road_no", Sort.ASCENDING);
        if (sort.size() != 0) {
            tempList = realm.copyFromRealm(sort);
            for (GoodsInfo goodsInfo : tempList) {
                if ("0".equals(goodsInfo.getGoodsID()) || "".equals(goodsInfo.getGoodsID())) {
                    continue;
                }
                goodsInfo.setLocalKuCunForCheck(Integer.parseInt(goodsInfo.getKuCun()));
                int roadNum = goodsInfo.getRoad_no();
                boolean isRoadHave = goodsInfoMap.containsKey(roadNum);
                if (isRoadHave) {
                    goodsInfoMap.put(roadNum, goodsInfo);
                } else {
                    continue;
                }
            }
        }

        Integer[] keys = goodsInfoMap.keySet().toArray(new Integer[goodsInfoMap.size()]);
        Arrays.sort(keys);

        for (int i = 0; i < keys.length; i++) {
            goodsInfoList.add(goodsInfoMap.get(keys[i]));
        }

        deskAdapter = new DeskAdapter(getContext(), goodsInfoList, tempList);
        deskAdapter.setOnChangeInfoListener(new DeskAdapter.ChangeInfoListener() {
            @Override
            public void onChange() {
                isChanged = true;
            }
        });

        deskAdapter.setDialogShowListener(new DeskAdapter.DialogShowListener() {
            @Override
            public void onClick(View v, final int position, int type, final String kucun) {
                switch (type) {
                    case 1:// 修改价格
                        //发送广播通知所有注册该接口的监听器
                        ListenerManager.getInstance().sendBroadCast("show");
                        tvDialogTitle.setText("现在的价格  " + Float.parseFloat(kucun) / 10);
                        edtInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                        edtInput.setHint("请输入新的商品价格");
                        edtInput.setText("");
                        edtInput.setKeyListener(DigitsKeyListener.getInstance("1234567890."));
                        edtInput.setInputType(8194);
                        rlDialog.setVisibility(View.VISIBLE);
                        tvDialogConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 关闭键盘
                                DataUtil.closeKeyBord(getActivity());
                                String inpuStr = edtInput.getText().toString();
                                if (!"".equals(inpuStr) && !".".equals(inpuStr)) {
                                    if (inpuStr.startsWith(".")) {
                                        inpuStr = "0" + inpuStr;
                                        edtInput.setText(inpuStr);
                                        edtInput.setSelection(edtInput.getText().length());
                                    }
                                    if (Double.parseDouble(inpuStr) <= 0) {
                                        ToastUtils.showToast(getActivity(), "请输入正确的价格");
                                        return;
                                    }
                                    if (Double.parseDouble(inpuStr) < 0.5) {
                                        ToastUtils.showToast(getActivity(), "价格不能低于0.5元");
                                        return;
                                    }
                                    if (Double.parseDouble(inpuStr) % 0.5 != 0) {
                                        ToastUtils.showToast(getActivity(), "价格必须是0.5元的倍数");
                                        return;
                                    }
                                    goodsInfoList.get(position).setPrice(String.valueOf((int) (Double.parseDouble(edtInput.getText().toString()) * 10)));
                                    MarkLog.markLog("副柜" + machineSn + "商品：" + goodsInfoList.get(position).getGoodsName() + "，价格从" + Float.parseFloat(kucun) / 10 + "修改为" +
                                            edtInput.getText().toString(), SysConfig.LOG_LEVEL_MIDDLE, machineSn);
                                    deskAdapter.notifyDataSetChanged();
                                } else {
                                    ToastUtils.showToast(getActivity(), "请输入正确的价格");
                                    return;
                                }
                                isChanged = true;
                                //发送广播通知所有注册该接口的监听器
                                ListenerManager.getInstance().sendBroadCast("close");
                                rlDialog.setVisibility(View.GONE);
                            }
                        });
                        break;
                    case 2:// 修改最大库存
                        //发送广播通知所有注册该接口的监听器
                        ListenerManager.getInstance().sendBroadCast("show");
                        tvDialogTitle.setText("现最大库存  " + kucun);
                        edtInput.setHint("请输入新的最大库存数");
                        edtInput.setText("");
                        edtInput.setKeyListener(DigitsKeyListener.getInstance("1234567890"));
                        edtInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                        edtInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                        rlDialog.setVisibility(View.VISIBLE);
                        tvDialogConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String inpuStr = edtInput.getText().toString();
                                if ("".equals(inpuStr)) {
                                    ToastUtils.showToast(getActivity(), "请输入正确的最大库存数");
                                    return;
                                }
                                if (Integer.parseInt(inpuStr) <= 0) {
                                    ToastUtils.showToast(getActivity(), "请输入正确的最大库存数");
                                    return;
                                }
                                rlDialog.setVisibility(View.GONE);
                                // 模板命名
                                //发送广播通知所有注册该接口的监听器
                                ListenerManager.getInstance().sendBroadCast("close");
                                // 关闭键盘
                                DataUtil.closeKeyBord(getActivity());
                                if (!"".equals(edtInput.getText().toString())) {
                                    if (goodsInfoList.get(position).getLocalKuCunForCheck() > Integer.parseInt(edtInput.getText().toString())) {
                                        goodsInfoList.get(position).setLocalKuCunForCheck(Integer.parseInt(edtInput.getText().toString()));
                                    }
                                    goodsInfoList.get(position).setMaxKucun(Integer.parseInt(edtInput.getText().toString()));
                                    deskAdapter.notifyDataSetChanged();
                                    isChanged = true;
                                }
                            }
                        });
                        break;
                    default:
                        break;
                }
            }
        });

        deskAdapter.setChooseGoodsListener(new DeskAdapter.ChooseGoodsListener() {
            @Override
            public void onClick(View view, int position) {
                ListenerManager.getInstance().sendBroadCast("show");
                rlChoose.setVisibility(View.VISIBLE);
                goodInfoPosition = position;
                x = 0;
                y = 0;
                tvDrink.setText(goodsPop.getAction(x).mTitle);
                typePop.setSelectedPosition(0);
                tvAll.setText(typePop.getAction(0).mTitle);
                goodsPop.setSelectedPosition(x);
                filterEdit.setText("");
                sortModel.clear();
                getTypeGoodsList();
                sortAdapter.updateListView(sortModel);
            }
        });

        gvGoods.setAdapter(deskAdapter);
        gvGoods.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //每一条数据都是一个Map
                switch (scrollState) {
                    case SCROLL_STATE_FLING:
                        Glide.with(MyApplication.getInstance()).pauseRequests();
                        //刷新
                        break;
                    case SCROLL_STATE_IDLE:
                        Glide.with(MyApplication.getInstance()).resumeRequests();
                        break;
                    case SCROLL_STATE_TOUCH_SCROLL:
                        Glide.with(MyApplication.getInstance()).resumeRequests();
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        //盘点功能说明
        pandianAdapter = new PandianAdapter(getContext(), pandianInfoList);
        listView.setAdapter(pandianAdapter);

        //模板功能说明
        mubanAdapter = new MubanAdapter(getContext(), mubanInfoList);
        mlvMuban.setAdapter(mubanAdapter);
        mlvMuban.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                String mubanName = mubanInfoList.get(position).getTempleName();
                alertView = new AlertView("提示", "确定使用模版(" + mubanName + ")替换货道商品吗?", "取消", new String[]{"确认"}, null,
                        getContext(), AlertView.Style.Alert, DataUtil.dip2px(getContext(),
                        Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, int ss) {
                        if (-1 == ss) {
                            alertView.dismiss();
                        } else {
                            isChanged = true;
                            // 替换商品
                            rlMuban.setVisibility(View.GONE);
                            alertView.dismiss();
                            ListenerManager.getInstance().sendBroadCast("close");
                            List<RoadTempleDt> roadTempleDtList = mubanInfoList.get(position).getRoadTempleDtList();
                            for (GoodsInfo goodsInfo : goodsInfoList) {
                                // 先根据goodsInfo的modle 创建里面空的占位
                                goodsInfo.setMachineID(machineSn);
                                goodsInfo.setZhidingCount(0);
                                goodsInfo.setGoodsID("");
                                goodsInfo.setGoodsCode("");
                                goodsInfo.setAscription("");
                                goodsInfo.setGoodsAbbreviation("");
                                goodsInfo.setGoodsImage("");
                                goodsInfo.setGoodsName("");
                                goodsInfo.setPrice("0.0");
                                goodsInfo.setGoodsBelong("3");
                                for (RoadTempleDt roadTempleDt : roadTempleDtList) {
                                    if (goodsInfo.getRoad_no() == Integer.parseInt(roadTempleDt.getRoadNo())) {
                                        if (roadTempleDt.getGoodsId() == null || "".equals(roadTempleDt.getGoodsId()) || "0".equals(roadTempleDt.getGoodsId())) {
                                            break;
                                        }
                                        goodsInfo.setMaxKucun(Integer.parseInt((roadTempleDt.getRoadNum() == null || "".equals(roadTempleDt.getRoadNum())) ? "0" : roadTempleDt.getRoadNum()));
                                        goodsInfo.setGoodsID(roadTempleDt.getGoodsId());
                                        goodsInfo.setGoodsCode(roadTempleDt.getGoodsId());
                                        goodsInfo.setGoodsName(roadTempleDt.getGoodsName());
                                        goodsInfo.setGoodsAbbreviation(roadTempleDt.getGoodsAbbreviation());
                                        goodsInfo.setGoodsImage(roadTempleDt.getGoodsImage());
                                        goodsInfo.setAscription(roadTempleDt.getAscription());
                                        goodsInfo.setPrice(Double.parseDouble((roadTempleDt.getGoodsPrice() == null || "".equals(roadTempleDt.getGoodsPrice()) ? "0" : roadTempleDt.getGoodsPrice())) * 10 + "");
                                        break;
                                    }
                                }
                            }
                            ListenerManager.getInstance().sendBroadCast("close");
                            deskAdapter.notifyDataSetChanged();
                        }
                    }
                }).setCancelable(false).setOnDismissListener(null);
                alertView.show();
            }
        });
    }

    @OnClick({R.id.tv_submit, R.id.tv_buman, R.id.tv_more})
    public void onTopClick(View view) {
        switch (view.getId()) {
            case R.id.tv_submit:// 提交
                // 判断是否有重复的价格不同的商品
                Map<String, String> checkMap = new HashMap<>();
                for (GoodsInfo goodsInfo : goodsInfoList) {
                    if (goodsInfo == null || goodsInfo.getGoodsCode() == null || "".equals(goodsInfo.getGoodsCode()) || "0".equals(goodsInfo.getGoodsCode())) {
                        continue;
                    }
                    if (checkMap.containsKey(goodsInfo.getGoodsCode())) {
                        if (Double.parseDouble(checkMap.get(goodsInfo.getGoodsCode())) != Double.parseDouble(goodsInfo.getPrice())) {
                            alertView = new AlertView("提示", "同种商品价格必须一致(" + goodsInfo.getGoodsName() + ")",
                                    null, new String[]{"确认"}, null, getContext(), AlertView.Style.Alert, DataUtil.dip2px(getContext(), Double.parseDouble(getContext().getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
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
                    } else {
                        checkMap.put(goodsInfo.getGoodsCode(), goodsInfo.getPrice());
                    }
                }
                if (checkMap.size() == 0) {
                    ToastUtils.showToast((Activity) getContext(), "货道未配置");
                    return;
                }
                if (!isChanged) {
                    alertView = new AlertView("提示", "货道信息没有任何修改", null,
                            new String[]{"确认"}, null, getContext(), AlertView.Style.Alert, DataUtil.dip2px(getContext(), Double.parseDouble(getContext().getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
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

                alertView = new AlertView("提示", "提交会更改主控货道设置且耗时较久\n确定继续吗?",
                        "取消", new String[]{"确认"}, null, getContext(), AlertView.Style.Alert, DataUtil.dip2px(getContext(), Double.parseDouble(getContext().getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, int position) {
                        if (-1 == position) {
                            alertView.dismiss();
                        } else {
                            //显示dialog
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ((BuhuoActivity) getContext()).confirmForDeskMachine(goodsInfoList, machineSn, 1);
                                }
                            }, 200);
                        }
                    }
                }).setCancelable(false).setOnDismissListener(null);
                alertView.show();
                break;
            case R.id.tv_buman:// 全部补满
                for (int i = 0; i < goodsInfoList.size(); i++) {
                    if (goodsInfoList.get(i).getGoodsID() != null && goodsInfoList.get(i).getMaxKucun() > 0) {
                        goodsInfoList.get(i).setKuCun(String.valueOf(goodsInfoList.get(i).getMaxKucun()));
                        goodsInfoList.get(i).setOnlineKuCun(goodsInfoList.get(i).getMaxKucun());
                        goodsInfoList.get(i).setZhidingCount(0);
                    }
                }
                deskAdapter.notifyDataSetChanged();
                isChanged = true;
                break;
            case R.id.tv_more:// 更多

                morePopup = new TitlePopup(getContext(), width / Integer.parseInt(getResources().getString(R.string.manger_Menu_wigth_percent)), ViewGroup.LayoutParams.WRAP_CONTENT);
                morePopup.addAction(new ActionItem("盘点"));
                morePopup.addAction(new ActionItem("上传模板"));
                morePopup.addAction(new ActionItem("获取模板"));
                morePopup.addAction(new ActionItem("退货撤机"));
                morePopup.addAction(new ActionItem("移机"));
                morePopup.setItemOnClickListener(new TitlePopup.OnItemOnClickListener() {
                    @Override
                    public void onItemClick(ActionItem item, int position) {
                        morePopup.dismiss();
                        switch (position) {
                            case 0:// 盘点
                                // 查看饮料机10020001盘点数据
                                MarkLog.markLog("查看副柜" + desk.getMachineSn() + "盘点数据", SysConfig.LOG_LEVEL_IMPORTANT, desk.getMachineSn());
                                //发送广播通知所有注册该接口的监听器
//                                ListenerManager.getInstance().sendBroadCast("show");
                                currentPage = 1;
                                // 设置盘点数据
                                pandianInfoList.clear();
                                pandianAdapter.notifyDataSetChanged();
                                //rlPandian.setVisibility(View.VISIBLE);
                                loadingDialog = LoadingUtil.createLoadingDialog(getActivity(), Constant.DEALING, 1, R.drawable.ic_ios_juhua, true);
                                loadingDialog.show();
                                String url = SysConfig.PANDIAN_ADDRESS + "&machineSn=" + machineSn + "&currPage=" + currentPage + "&pagerSize=" + SysConfig.PAGERSIZE;
                                iNetWorkRequInterface.request(url, 4, RequestMethod.POST);

                                break;
                            case 1:// 上传
                                //发送广播通知所有注册该接口的监听器
                                ListenerManager.getInstance().sendBroadCast("show");
                                tvDialogTitle.setText("模板命名");
                                edtInput.setHint("请输入模板名称");
                                // : 2016/8/24
                                edtInput.setText("");
                                edtInput.setKeyListener(null);
                                edtInput.setInputType(InputType.TYPE_CLASS_TEXT);
                                edtInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
                                rlDialog.setVisibility(View.VISIBLE);
                                edtInput.setFocusable(true);
                                tvDialogConfirm.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // 模板命名
                                        final String mubanName = edtInput.getText().toString();
                                        if ("".equals(mubanName)) {
                                            ToastUtils.showToast(getActivity(), "请输入模板名称");
                                            return;
                                        }

                                        if (mubanName.length() < 3) {
                                            ToastUtils.showToast(getActivity(), "模板名称太短");
                                            return;
                                        }
                                        // 模板命名
                                        //发送广播通知所有注册该接口的监听器
                                        ListenerManager.getInstance().sendBroadCast("close");
                                        DataUtil.closeKeyBord(getActivity());
                                        rlDialog.setVisibility(View.GONE);

                                        // 显示提交的dialog
                                        loadingDialog = LoadingUtil.createLoadingDialog(getContext(), "上传中...", 1, R.drawable.ic_ios_juhua, true);
                                        loadingDialog.show();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                // 上传模版
                                                uploadTemplete(mubanName);
                                            }
                                        }, 200);
                                    }
                                });
                                break;
                            case 2:// 获取
                                //发送广播通知所有注册该接口的监听器
                                // 获取饮料机模板
                                MarkLog.markLog("获取模板" + desk.getMachineSn() + "模板", SysConfig.LOG_LEVEL_NORMAL, desk.getMachineSn());
                                ListenerManager.getInstance().sendBroadCast("show");
                                mubanInfoList.clear();
                                mubanAdapter.notifyDataSetChanged();
                                loadingDialog = LoadingUtil.createLoadingDialog(getContext(), "加载中...", 1, R.drawable.ic_ios_juhua, true);
                                loadingDialog.show();
                                // 从网上获取模版列表
                                requestMoBanFromNet();

                                rlMuban.setVisibility(View.VISIBLE);
                                break;
                            case 3:// 退货
                                alertView = new AlertView("提示", "确定要退货撤机吗？", "取消", new String[]{"确认"}, null, getContext(), AlertView.Style.Alert, DataUtil.dip2px(getContext(), Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(Object o, int position) {
                                        if (-1 == position) {
                                            alertView.dismiss();
                                        } else {
                                            alertView.dismiss();

                                            // 接口请求
                                            loadingDialog = LoadingUtil.createLoadingDialog(getContext(), "提交中...", 1, R.drawable.ic_ios_juhua, true);
                                            loadingDialog.show();

                                            MarkLog.markLog("副柜" + desk.getMachineSn() + "撤机", SysConfig.LOG_LEVEL_IMPORTANT, desk.getMachineSn());
                                            // 退货撤机
                                            chejiRequest("0");
                                        }
                                    }
                                }).setCancelable((true)).setOnDismissListener(null);
                                alertView.show();
                                break;
                            case 4:// 移机
                                alertView = new AlertView("提示", "确定要移机吗？", "取消", new String[]{"确认"}, null, getContext(), AlertView.Style.Alert, DataUtil.dip2px(getContext(), Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(Object o, int position) {
                                        if (-1 == position) {
                                            alertView.dismiss();
                                        } else {
                                            alertView.dismiss();
                                            // 接口请求
                                            loadingDialog = LoadingUtil.createLoadingDialog(getContext(), "提交中...", 1, R.drawable.ic_ios_juhua, true);
                                            loadingDialog.show();

                                            MarkLog.markLog("副柜" + desk.getMachineSn() + "移机", SysConfig.LOG_LEVEL_IMPORTANT, desk.getMachineSn());
                                            // 退货撤机
                                            chejiRequest("1");
                                        }
                                    }
                                }).setCancelable(true).setOnDismissListener(null);
                                alertView.show();
                                break;
                            default:
                                break;
                        }
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //显示
                        morePopup.show(tvMore);
                    }
                }, 10);
                break;
            default:
                break;
        }
    }

    /**
     * 初始化menu
     */
    private void initPop() {
        goodsPop = new TitlePopup(getContext(), width / 3, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (shopTypes.size() == 0) {
            goodsPop.addAction(new ActionItem("全部"));
        } else {
            for (int i = 0; i < shopTypes.size(); i++) {
                goodsPop.addAction(new ActionItem(shopTypes.get(i).getTypeName()));
            }
        }
        goodsPop.setSelectedPosition(0);
        goodsPop.setItemOnClickListener(new TitlePopup.OnItemOnClickListener() {
            @Override
            public void onItemClick(ActionItem item, int position) {
                tvDrink.setText(goodsPop.getAction(position).mTitle);
                // 设置选中项目
                goodsPop.setSelectedPosition(position);

                x = position;
                // 根据标题来过滤商品列表
                getTypeGoodsList();
                sortAdapter.updateListView(sortModel);

            }
        });
        typePop = new TitlePopup(getContext(), width / 3, ViewGroup.LayoutParams.WRAP_CONTENT);
        typePop.addAction(new ActionItem("全部"));
        typePop.addAction(new ActionItem("自营"));
        typePop.addAction(new ActionItem("非自营"));
        typePop.setSelectedPosition(0);
        typePop.setItemOnClickListener(new TitlePopup.OnItemOnClickListener() {
            @Override
            public void onItemClick(ActionItem item, int position) {
                tvAll.setText(typePop.getAction(position).mTitle);
                // 设置选中项目
                typePop.setSelectedPosition(position);
                // TODO: 2016/8/24 刷新列表
                y = position;
                getTypeGoodsList();
                sortAdapter.updateListView(sortModel);
            }
        });
    }

    @OnClick({R.id.rl_drink_click, R.id.rl_all_click})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_drink_click:// 选择商品
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //显示
                        goodsPop.show(rlDrinkClick);
                    }
                }, 100);
                break;
            case R.id.rl_all_click:// 选择类型
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //显示
                        typePop.show(rlAllClick);
                    }
                }, 100);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }

    /**
     * 选择商品
     */
    private void initChoose() {
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        sidrbar.setTextView(dialog);
        //设置右侧触摸监听
        sidrbar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = sortAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    countryLvcountry.setSelection(position);
                }

            }
        });
        countryLvcountry.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //这里要利用adapter.getItem(position)来获取当前position所对应的对象
                //// 2016/8/24 选中1个商品
                //发送广播通知所有注册该接口的监听器
                SortModel sortModel = DeskFragment.this.sortModel.get(position);
                if (sortModel != null) {
                    // 给打开了这个商品清单页面的集合元素属性设置数据
                    GoodsInfo info = goodsInfoList.get(goodInfoPosition);
                    info.setGoodsID(sortModel.getGoodId());
                    info.setGoodsName(sortModel.getName());
                    info.setPrice(sortModel.getGoodPrice());
                    info.setGoodsImage(sortModel.getGoodsImage());
                    info.setGoodsAbbreviation(sortModel.getGoodsAbbreviation());
                    info.setAscription(sortModel.getAscription());
                    info.setGoodsCode(sortModel.getGoodId());
                    deskAdapter.notifyDataSetChanged();
                    // 给goodinfolist设置完值之后 把保存的值置为-1
                    goodInfoPosition = -1;
                    isChanged = true;
                }
                ListenerManager.getInstance().sendBroadCast("close");
                rlChoose.setVisibility(View.GONE);
            }
        });
        // 根据a-z进行排序源数据
        sortModel = new ArrayList<>();
        Collections.sort(sortModel, pinyinComparator);
        sortAdapter = new SortAdapter(getContext(), sortModel);
        countryLvcountry.setAdapter(sortAdapter);
        getTypeGoodsList();
        filterEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.e("TAB", "按键actionId:" + actionId + " event:" + event);
                //回车键
                if (actionId == 5) {
                    return true;
                }
                return false;
            }
        });
        //根据输入框输入值的改变来过滤搜索
        filterEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                getTypeGoodsList();
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }
        });
    }

    // 给商品选择列表设置值
    private void getTypeGoodsList() {
        String xkey;
        if (shopTypes == null || shopTypes.size() == 0 || shopTypes.size() < x + 1) {
            xkey = "";
        } else {
            xkey = shopTypes.get(x).getTypeName();
        }
        if ("全部".equals(xkey)) {
            xkey = "";
        }
        if (y == 0) {
            sGoods = realm.where(ShopGoods.class).equalTo("typeName", xkey).findAll();
            filledData(sGoods);
        } else if (y == 1) {
            sGoods = realm.where(ShopGoods.class).equalTo("typeName", xkey).equalTo("ascription", "0").findAll();
            filledData(sGoods);
        } else {
            sGoods = realm.where(ShopGoods.class).equalTo("typeName", xkey).equalTo("ascription", "1").findAll();
            filledData(sGoods);
        }
    }

    /**
     * 为ListView填充数据
     * date是商品名称
     *
     * @param sGoods
     * @return
     */
    private void filledData(List<ShopGoods> sGoods) {

        sortModel.clear();
        for (int i = 0; i < sGoods.size(); i++) {
            SortModel sort = new SortModel();
            // 给sortModel设置名称
            sort.setName(sGoods.get(i).getGoodsName());
            //汉字转换成拼音
            String pinyin = characterParser.getSelling(sGoods.get(i).getGoodsName());
            String sortString = pinyin.substring(0, 1).toUpperCase();
            // 正则表达式，判断首字母是否是英文字母 然后设置进去
            if (sortString.matches("[A-Z]")) {
                sort.setSortLetters(sortString.toUpperCase());
            } else {
                sort.setSortLetters("#");
            }
            // 传商品图片
            sort.setGoodsImage(sGoods.get(i).getGoodsImage());
            // 商品的分类
            sort.setParentTypeId(sGoods.get(i).getParentTypeId());
            sort.setGoodType(sGoods.get(i).getGoodsType());
            // 设置是否自营
            sort.setAscription(sGoods.get(i).getAscription());
            sort.setGoodType(sGoods.get(i).getAscription());
            // 设置价格
            sort.setGoodPrice(sGoods.get(i).getGoodsPrice());
            // 设置商品id
            sort.setGoodId(sGoods.get(i).getGoodsId());
            // 设置商品分类名称
            sort.setParentTypeId(sGoods.get(i).getParentTypeId());
            sort.setGoodsAbbreviation(sGoods.get(i).getGoodsAbbreviation());
            sortModel.add(sort);
        }
        filterData("");
    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<SortModel> filterDateList = new ArrayList<SortModel>();

        if (DataUtil.isEmpty(filterStr)) {
            filterDateList.addAll(sortModel);
        } else {
            filterDateList.clear();
            for (SortModel sortModel : this.sortModel) {
                String name = sortModel.getName();
                if (name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())) {
                    filterDateList.add(sortModel);
                }
            }
        }
        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        this.sortModel.clear();
        this.sortModel.addAll(filterDateList);
        sortAdapter.updateListView(this.sortModel);
    }

    @OnClick({R.id.rl_choose, R.id.ly_chooseinfo, R.id.ll_muban})
    public void OnClick(View view) {
        switch (view.getId()) {
            case R.id.rl_choose:
                ListenerManager.getInstance().sendBroadCast("close");
                rlChoose.setVisibility(View.GONE);
                break;
            case R.id.ly_chooseinfo:
                break;
            case R.id.ll_muban:
                break;
        }
    }


    @OnClick({R.id.iv_dialog_close, R.id.iv_close_muban, R.id.rl_pandian, R.id.rl_muban, R.id.rl_dialog})
    public void show(View view) {
        switch (view.getId()) {
            case R.id.iv_dialog_close:// 上传模板关闭
                //发送广播通知所有注册该接口的监听器
                ListenerManager.getInstance().sendBroadCast("close");
                DataUtil.closeKeyBord(getActivity());
                rlDialog.setVisibility(View.GONE);
                break;
            case R.id.iv_close_muban:// 模板关闭
                //发送广播通知所有注册该接口的监听器
                ListenerManager.getInstance().sendBroadCast("close");
                rlMuban.setVisibility(View.GONE);
                break;
            case R.id.rl_pandian:// 盘点关闭
                //发送广播通知所有注册该接口的监听器
                ListenerManager.getInstance().sendBroadCast("close");
                rlPandian.setVisibility(View.GONE);
                break;
            case R.id.rl_muban:// 模板关闭
                //发送广播通知所有注册该接口的监听器
                ListenerManager.getInstance().sendBroadCast("close");
                rlMuban.setVisibility(View.GONE);
                break;
            case R.id.rl_dialog:
                // do nothing
                break;
            default:
                break;
        }
    }

    @OnClick({R.id.edt_code, R.id.tv_1, R.id.tv_2, R.id.tv_3, R.id.iv_delete, R.id.tv_4, R.id.tv_5, R.id.tv_6, R.id.tv_clear, R.id.tv_7, R.id.tv_8, R.id.tv_9, R.id.tv_star, R.id.tv_0, R.id.tv_jing, R.id.tv_confirm, R.id.rl_keyboard})
    public void onKeyBoard(View view) {
        switch (view.getId()) {
            case R.id.tv_1://1
                if (code.length() <= 19) {
                    code += "1";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_2://2
                if (code.length() <= 19) {
                    code += "2";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_3://3
                if (code.length() <= 19) {
                    code += "3";
                    edtCode.setText(code);
                }
                break;
            case R.id.iv_delete://退格
                if (code.length() > 0) {
                    code = code.substring(0, code.length() - 1);
                }
                edtCode.setText(code);
                break;
            case R.id.tv_4:// 4
                if (code.length() <= 19) {
                    code += "4";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_5:// 5
                if (code.length() <= 19) {
                    code += "5";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_6:// 6
                if (code.length() <= 19) {
                    code += "6";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_clear:// 清空
                code = "";
                edtCode.setText(code);
                break;
            case R.id.tv_7:// 7
                if (code.length() <= 19) {
                    code += "7";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_8:// 8
                if (code.length() <= 19) {
                    code += "8";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_9:// 9
                if (code.length() <= 19) {
                    code += "9";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_star:// 星号
                if (code.length() <= 19) {
                    code += "＊";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_0:// 0
                if (code.length() <= 19) {
                    code += "0";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_jing:// 井号
                if (code.length() <= 19) {
                    code += "#";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_confirm:// 确认
                ListenerManager.getInstance().sendBroadCast("close");
                rlKeyboard.setVisibility(View.GONE);
                goodsInfoList.get(pricePosition).setPrice(code);
                deskAdapter.notifyDataSetChanged();

                code = "";
                edtCode.setText(code);

                break;
            case R.id.rl_keyboard:// 键盘关闭
                ListenerManager.getInstance().sendBroadCast("close");
                rlKeyboard.setVisibility(View.GONE);
                code = "";
                edtCode.setText(code);
                break;
            default:
                break;
        }
    }


    /**
     * 上传模版
     */
    private void uploadTemplete(String temple_name) {
        // 生成模版数据
        StringBuffer temple_roads = new StringBuffer();
        for (GoodsInfo goodsInfo : goodsInfoList) {
            temple_roads.append(goodsInfo.getRoad_no() + "," + goodsInfo.getMaxKucun() + "," + (goodsInfo.getGoodsID() == null ? "" : goodsInfo.getGoodsID()) +
                    "," + (goodsInfo.getPrice() == null || "".equals(goodsInfo.getPrice()) ? 0 : Double.parseDouble(goodsInfo.getPrice())) * 0.1 + ";");
        }
        Log.e("模版上传", "上传模版的货道信息:" + temple_roads.toString());
        MarkLog.markLog("上传副柜" + desk.getMachineSn() + "模板", SysConfig.LOG_LEVEL_IMPORTANT, desk.getMachineSn());
        String url = null;
        try {
            url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/post/template?machineSn=" + machineSn + "&templeName=" + URLEncoder.encode(temple_name, "UTF-8") + "&templeRoads=" + temple_roads;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        iNetWorkRequInterface.request(url, 3, RequestMethod.GET);
    }

    /**
     * 获取模版
     */
    private void requestMoBanFromNet() {
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/" + machineSn + "/roadtemplelist";
        iNetWorkRequInterface.request(url, 1, RequestMethod.GET);
    }

    /**
     * 退货撤机
     *
     * @param apply_type(0:退货撤机;1:移机)
     */
    private void chejiRequest(String apply_type) {
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/put/" + machineSn + "/remove/" + apply_type;
        iNetWorkRequInterface.request(url, 2, RequestMethod.GET);
    }

    @Override
    public void onSucceed(int what, Response<String> response) throws Exception {

        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        int responseCode = response.getHeaders().getResponseCode();// 服务器响应码
        if (responseCode == 200) {
            if (RequestMethod.HEAD != response.getRequestMethod()) {
                // 请求方法为HEAD时没有响应内容
                JSONObject jsonResult = null;
                try {
                    jsonResult = new JSONObject(response.get());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (what) {
                    case 0:
                        try {
                            if (jsonResult != null && jsonResult.getString(SysConfig.JSON_KEY_ERROR_CODE).equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                // 提交成功
                                ToastUtils.showToast(getActivity(), "成功");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        try {
                            if (jsonResult != null && jsonResult.getString(SysConfig.JSON_KEY_ERROR_CODE).equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                Gson gson = new Gson();
                                Type type = new TypeToken<ArrayList<RoadTemple>>() {
                                }.getType();
                                List<RoadTemple> roadTemples = gson.fromJson(jsonResult.getString("roadTempleList"), type);
                                mubanInfoList.clear();
                                for (RoadTemple roadTemple : roadTemples) {
                                    mubanInfoList.add(roadTemple);
                                }
                                // 显示模版信息
                                mubanAdapter.notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case 2: // 移机结果
                        try {
                            if (jsonResult != null && jsonResult.getString(SysConfig.JSON_KEY_ERROR_CODE).equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                // 处理成功
                                loadingDialogEnd = LoadingUtil.createLoadingDialog(getContext(), "处理成功", 1, R.drawable.ic_success, false);
                                loadingDialogEnd.show();
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        loadingDialogEnd.dismiss();
                                    }
                                }, 1000);
                            } else {
                                ToastUtils.showToast(getActivity(), jsonResult.getString(SysConfig.JSON_KEY_ERROR));
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case 3: // 上传模版
                        if (jsonResult != null && jsonResult.getString(SysConfig.JSON_KEY_ERROR_CODE).equals(SysConfig.ERROR_CODE_SUCCESS)) {
                            // 处理成功
                            loadingDialogEnd = LoadingUtil.createLoadingDialog(getActivity(), "上传成功", 1, R.drawable.ic_success, false);
                            loadingDialogEnd.show();
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    loadingDialogEnd.dismiss();
                                }
                            }, 1000);
                        } else {
                            // 处理失败
                            loadingDialogFail = LoadingUtil.createLoadingDialog(getContext(), "处理失败", 1, R.drawable.ic_quhuo_fail, false);
                            //显示dialog
                            loadingDialogFail.show();
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    loadingDialogFail.dismiss();
                                }
                            }, 1000);
                        }
                        break;
                    case 4:
                        if (currentPage > 1) {
                            ptrl.loadmoreFinish(PullToRefreshLayout.SUCCEED);
                        }
                        try {
                            if (jsonResult != null && jsonResult.getString("status").equals("0")) {
                                JSONArray jsonArray = jsonResult.getJSONArray("inventoryData");
                                if (jsonArray.length() == 0 && currentPage == 1) {
                                    break;
                                }
                                if (rlPandian.getVisibility() == View.GONE) {
                                    rlPandian.setVisibility(View.VISIBLE);
                                }
                                totalPage = jsonResult.getInt("totalPage");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    PanDInfo panDInfo = new PanDInfo();
                                    panDInfo.setOperateTime(jsonArray.getJSONObject(i).getString("operateTime"));
                                    panDInfo.setGrossOnline(jsonArray.getJSONObject(i).getString("grossOnline"));
                                    panDInfo.setGrossOffline(jsonArray.getJSONObject(i).getString("grossOffline"));
                                    panDInfo.setSalesVolume(jsonArray.getJSONObject(i).getString("salesVolume"));
                                    pandianInfoList.add(panDInfo);
                                }
                                pandianAdapter.notifyDataSetChanged();
                            } else {
                                loadingDialogFail = LoadingUtil.createLoadingDialog(getContext(), "处理失败", 1, R.drawable.lose1, false);
                                //显示dialog
                                loadingDialogFail.show();
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        loadingDialogFail.dismiss();
                                    }
                                }, 1000);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }

            }
        }
    }

    @Override
    public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) throws Exception {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        if (networkMillis > 5000) {
            ToastUtils.showToast(getActivity(), Constant.NETWORK_ERROR1);
        }
        if (what == 0) {
            alertView = new AlertView("提示", "网络连接出错，可以在APP端完成补货\nAPP首页-右上角加号-\n-查看今日运营报告-完成补货", null, new String[]{"确认"}, null, getContext(), AlertView.Style.Alert, DataUtil.dip2px(getContext(), Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, int position) {
                    if (-1 == position) {
                        alertView.dismiss();
                    } else {
                        // TODO: 2016/8/24
                        alertView.dismiss();
                    }
                }
            }).setCancelable(true).setOnDismissListener(null);
            alertView.show();
        } else {
            ToastUtils.showToast(getActivity(), Constant.NETWORK_ERROR1);
        }
    }

    @Override
    public void onLoadMore(final PullToRefreshLayout pullToRefreshLayout) {
        if (currentPage < totalPage) {
            currentPage++;
            String url = SysConfig.PANDIAN_ADDRESS + "&machineSn=" + machineSn + "&currPage=" + currentPage + "&pagerSize=" + SysConfig.PAGERSIZE;
            iNetWorkRequInterface.request(url, 4, RequestMethod.POST);
        } else {
            new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
                }
            }.sendEmptyMessageDelayed(0, 2000);
        }
    }
}
