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
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.MarkLog;
import com.zongsheng.drink.h17.background.adapter.DrinkAdapter;
import com.zongsheng.drink.h17.background.adapter.MubanAdapter;
import com.zongsheng.drink.h17.background.adapter.PandianAdapter;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * 饮料柜
 * Created by 谢家勋 on 2016/8/23.
 */
public class DrinkFragment extends Fragment implements PullToRefreshLayout.OnRefreshListener, IDrinkFragmentInterface {

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

    private View view;
    private DrinkAdapter drinkAdapter;
    private List<GoodsInfo> goodsInfoList = new ArrayList<>();
    private PandianAdapter pandianAdapter;
    private List<PanDInfo> pandianInfoList = new ArrayList<>();
    private MubanAdapter mubanAdapter;
    private List<RoadTemple> mubanInfoList = new ArrayList<>();
    private List<GoodsInfo> tempList = new ArrayList();
    private int totalPage = 0;
    private int currentPage = 1;

    /**
     * popupwindow
     */
    private int width = 0;
    private int height = 0;

    private AlertView alertView;
    /**
     * 修改价格
     */
    private String code = "";
    private int pricePosition = -1;

    /**
     * 选择列表相关
     */
    private SortAdapter sortAdapter;

    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    private List<SortModel> sortModel = new ArrayList<>();

    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;


    private TitlePopup goodsPop;
    private TitlePopup typePop;
    // 数据库
    private Realm realm;
    private List<ShopGoods> sGoods;
    private int goodInfoPosition = -1;
    private RealmResults<ShopType> shopTypes;

    /**
     * 自定义的Dialog
     */
    private Dialog loadingDialog;// 加载中

    private IDrinkFragmentPresent iDrinkFragmentPresent;

    private int x = 0;
    private int y = 0;

    /**
     * 是否修改过
     */
    public boolean isChanged = false;

    /**
     * 构造函数，防止崩溃
     */
    public DrinkFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
            return view;
        }
        view = inflater.inflate(R.layout.fragment_yinliao, null);
        ptrl = ((PullToRefreshLayout) view.findViewById(R.id.refresh_view));
        ptrl.setOnRefreshListener(this);
        ButterKnife.bind(this, view);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        height = dm.heightPixels;
        width = dm.widthPixels;
        realm = Realm.getDefaultInstance();
        shopTypes = realm.where(ShopType.class).findAll().sort("typeId", Sort.ASCENDING);
        iDrinkFragmentPresent = new IDrinkFragmentPresentImpl(getActivity(), this);
        initView();
        initChoose();
        initPop();
        return view;
    }

    /**
     * 重新加载数据
     */
    public void resetData() {
        for (GoodsInfo goodsInfo : goodsInfoList) {
            goodsInfo.setZhidingCount(0);
            goodsInfo.setLocalKuCunForCheck(Integer.parseInt(goodsInfo.getKuCun()));
            goodsInfo.setOnlineKuCun(Integer.parseInt(goodsInfo.getKuCun()));
            drinkAdapter.notifyDataSetChanged();
        }
    }


    private void initView() {
        // 从mp中获取该机器的货道数
        //TODO:这里有问题，不仅仅是可用的货道数，应该获取清晰的哪个货道可用
        int countHuoDao = MyApplication.getInstance().getRoadCount();
        for (int i = 0; i < countHuoDao; i++) {
            GoodsInfo goodsInfo = new GoodsInfo();
            // 先根据goodsInfo的modle 创建里面空的占位
            goodsInfo.setMachineID(MyApplication.getInstance().getMachine_sn());
            goodsInfo.setRoad_no(i + 1);
            goodsInfo.setZhidingCount(0);
            goodsInfo.setKuCun(String.valueOf(0));
            goodsInfoList.add(goodsInfo);
        }
        // 获取当前本地数据库存储的商品信息 goodsinfo
        RealmResults<GoodsInfo> sort = realm.where(GoodsInfo.class).equalTo("machineID", MyApplication.getInstance().getMachine_sn()).findAll().sort("road_no", Sort.ASCENDING);
        if (sort.size() != 0) {
            // 从数据库里面把集合copy出来
            tempList = realm.copyFromRealm(sort);
            for (GoodsInfo goodsInfo : tempList) {
                if ("0".equals(goodsInfo.getGoodsID()) || "".equals(goodsInfo.getGoodsID())) {
                    continue;
                }
                // 如果当前的position+1 等于数据库集合的对象存在货道数 那么这个用这个info来替代goodInfo里面的元素
                goodsInfo.setLocalKuCunForCheck(Integer.parseInt(goodsInfo.getKuCun()));
                if (goodsInfo.getRoad_no() <= goodsInfoList.size()) {
                    goodsInfoList.set(goodsInfo.getRoad_no() - 1, goodsInfo);
                }
            }
        }
        drinkAdapter = new DrinkAdapter(getContext(), goodsInfoList, tempList);
        drinkAdapter.setOnChangeInfoListener(new DrinkAdapter.OnChangeInfoListener() {
            @Override
            public void onCHange() {
                isChanged = true;
            }
        });
        // 修改价格和最大库存
        drinkAdapter.setOnDialogShowListener(new DrinkAdapter.onDialogShowListener() {
            @Override
            public void onClick(View v, final int position, int type, final String kucunOrPrice) {
                switch (type) {
                    case 1:// 修改价格
                        //发送广播通知所有注册该接口的监听器
                        ListenerManager.getInstance().sendBroadCast("show");
                        tvDialogTitle.setText("现在的价格  " + Float.parseFloat(kucunOrPrice) / 10);
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

                                    //
                                    MarkLog.markLog("饮料机" + MyApplication.getInstance().getMachine_sn() + "商品：" + goodsInfoList.get(position).getGoodsName() + "，价格从" + Float.parseFloat(kucunOrPrice) / 10 + "修改为" +
                                            edtInput.getText().toString(), SysConfig.LOG_LEVEL_MIDDLE, MyApplication.getInstance().getMachine_sn());
                                    drinkAdapter.notifyDataSetChanged();
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
                        tvDialogTitle.setText("现最大库存  " + kucunOrPrice);
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
                                    drinkAdapter.notifyDataSetChanged();
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
        // 选择商品
        drinkAdapter.setOnChooseGoodsListener(new DrinkAdapter.OnChooseGoodsListener() {
            @Override
            public void onClick(View v, int position) {
                //发送广播通知所有注册该接口的监听器
                ListenerManager.getInstance().sendBroadCast("show");
                rlChoose.setVisibility(View.VISIBLE);
                // 把gridview的position值 保存起来
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
        gvGoods.setAdapter(drinkAdapter);
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
        pandianAdapter = new PandianAdapter(getContext(), pandianInfoList);
        listView.setAdapter(pandianAdapter);
        mubanAdapter = new MubanAdapter(getContext(), mubanInfoList);
        mlvMuban.setAdapter(mubanAdapter);
        mlvMuban.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final String mobanName = mubanInfoList.get(position).getTempleName();
                alertView = new AlertView("提示", "确定使用模版(" + mobanName + ")替换货道商品吗?", "取消", new String[]{"确认"}, null,
                        getContext(), AlertView.Style.Alert, DataUtil.dip2px(getContext(), Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, int ss) {
//                        Log.e("模版", "模板点击了第" + position + "条");
                        MyApplication.getInstance().getLogBuHuo().d("点击了模板 = "+mobanName);
                        if (-1 == ss) {
                            alertView.dismiss();
                        } else {
                            isChanged = true;
                            // 替换商品
                            rlMuban.setVisibility(View.GONE);
                            alertView.dismiss();
                            ListenerManager.getInstance().sendBroadCast("close");
                            List<RoadTempleDt> roadTempleDtList = mubanInfoList.get(position).getRoadTempleDtList();
                            int countHuoDao = MyApplication.getInstance().getRoadCount();
                            for (int i = 0; i < countHuoDao; i++) {
                                GoodsInfo goodsInfo = new GoodsInfo();
                                // 先根据goodsInfo的modle 创建里面空的占位
                                goodsInfo.setMachineID(MyApplication.getInstance().getMachine_sn());
                                goodsInfo.setRoad_no(i + 1);
                                goodsInfo.setZhidingCount(0);
                                goodsInfo.setKuCun(String.valueOf(0));
                                goodsInfo.setOnlineKuCun(0);
                                goodsInfo.setLocalKuCunForCheck(0);
                                goodsInfo.setGoodsBelong("1");
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
                                //用模板里的商品信息替换原商品信息
                                goodsInfoList.set(i, goodsInfo);
                            }

                            ListenerManager.getInstance().sendBroadCast("close");
                            drinkAdapter.notifyDataSetChanged();
                        }
                    }
                }).setCancelable(false).setOnDismissListener(null);
                alertView.show();
            }
        });
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
                SortModel sortModel = DrinkFragment.this.sortModel.get(position);
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
                    drinkAdapter.notifyDataSetChanged();
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

    @OnClick({R.id.tv_submit, R.id.tv_buman, R.id.tv_more})
    public void onTopClick(View view) {
        switch (view.getId()) {
            case R.id.tv_submit:// 提交
                iDrinkFragmentPresent.submit(goodsInfoList, isChanged);
                break;
            case R.id.tv_buman:// 全部补满
                for (int i = 0; i < goodsInfoList.size(); i++) {
                    if (goodsInfoList.get(i).getGoodsID() != null && goodsInfoList.get(i).getMaxKucun() > 0) {
                        goodsInfoList.get(i).setKuCun(String.valueOf(goodsInfoList.get(i).getMaxKucun()));
                        goodsInfoList.get(i).setOnlineKuCun(goodsInfoList.get(i).getMaxKucun());
                        goodsInfoList.get(i).setZhidingCount(0);
                    }
                }
                drinkAdapter.notifyDataSetChanged();
                isChanged = true;
                break;
            case R.id.tv_more:// 更多
                // 初始化pop
                iDrinkFragmentPresent.more(width);
                break;
            default:
                break;
        }
    }

    /**
     * 退货撤机
     *
     * @param apply_type(0:退货撤机;1:移机)
     */
    private void chejiRequest(String apply_type) {
        iDrinkFragmentPresent.chejiRequest(MyApplication.getInstance().getMachine_sn(), apply_type);
    }

    /**
     * 获取模版
     */
    private void requestMoBanFromNet() {
        //写入操作日志
        MarkLog.markLog("获取主柜模版", SysConfig.LOG_LEVEL_NORMAL, MyApplication.getInstance().getMachine_sn());
        // 获取可以添加的格子柜的列表
        iDrinkFragmentPresent.requestMoBanFromNet(MyApplication.getInstance().getMachine_sn());
    }

    /**
     * 上传模版
     */
    private void uploadTemplete(String temple_name) {
        // 计算货道占比
        int ziyingCount = 0;
        int totalCount = 0;
        for (GoodsInfo goodsInfo : goodsInfoList) {
            if (goodsInfo != null && goodsInfo.getGoodsID() != null && !"".equals(goodsInfo.getGoodsID())) {
                totalCount++;
                if ("0".equals(goodsInfo.getAscription())) {
                    ziyingCount++;
                }
            }
        }
        if (MyApplication.getInstance().getRoadRatio() == null || "".equals(MyApplication.getInstance().getRoadRatio())) {
            ToastUtils.showToast(getActivity(), "数据(货道比率)错误,请重新登录后重试!");
            return;
        }
        double mustCount = Double.parseDouble(MyApplication.getInstance().getRoadRatio()) * totalCount;
        mustCount = new BigDecimal(String.valueOf(mustCount)).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (ziyingCount < (int) mustCount) {
            loadingDialog.dismiss();
            if (alertView != null)
                alertView.dismiss();
            alertView = new AlertView("提示", "自营商品不能低于" + (int) mustCount + "件,无法上传", null,
                    new String[]{"确认"}, null, getContext(), AlertView.Style.Alert, DataUtil.dip2px(getContext(), Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
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
        MarkLog.markLog("上传饮料机" + MyApplication.getInstance().getMachine_sn() + "模板", SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
        // 生成模版数据
        iDrinkFragmentPresent.uploadTemplete(temple_name, goodsInfoList);
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
            case R.id.tv_star:// .号
                if (code.length() <= 19) {
                    code += ".";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_0:// 0
                if (code.length() <= 19) {
                    code += "0";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_jing:// *号
                if (code.length() <= 19) {
                    code += "*";
                    edtCode.setText(code);
                }
                break;
            case R.id.tv_confirm:// 确认
                ListenerManager.getInstance().sendBroadCast("close");
                rlKeyboard.setVisibility(View.GONE);
                goodsInfoList.get(pricePosition).setPrice(code);
                drinkAdapter.notifyDataSetChanged();

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
     * 初始化menu
     */
    private void initPop() {

        // 初始化pop
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
        if (iDrinkFragmentPresent != null) {
            iDrinkFragmentPresent.onDestory();
            iDrinkFragmentPresent = null;
        }
    }

    @Override
    public void onLoadMore(final PullToRefreshLayout pullToRefreshLayout) {
        if (currentPage < totalPage) {
            currentPage++;
            iDrinkFragmentPresent.clickPanD(MyApplication.getInstance().getMachine_sn(), currentPage);
        } else {
            new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
                }
            }.sendEmptyMessageDelayed(0, 2000);
        }
    }

    @Override
    public void PopSetItemClick(final TitlePopup pop) {
        pop.setItemOnClickListener(new TitlePopup.OnItemOnClickListener() {
            @Override
            public void onItemClick(ActionItem item, int position) {
                pop.dismiss();
                switch (position) {
                    case 0:// 盘点
                        MarkLog.markLog("查看饮料机" + MyApplication.getInstance().getMachine_sn() + "盘点数据", SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
                        //发送广播通知所有注册该接口的监听器
                        ListenerManager.getInstance().sendBroadCast("show");
                        currentPage = 1;
                        pandianInfoList.clear();
                        pandianAdapter.notifyDataSetChanged();
                        loadingDialog = LoadingUtil.createLoadingDialog(getActivity(), Constant.DEALING, 1, R.drawable.ic_ios_juhua, true);
                        loadingDialog.show();
                        iDrinkFragmentPresent.clickPanD(MyApplication.getInstance().getMachine_sn(), currentPage);

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
                        tvDialogConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 模版名称
                                final String mubanName = edtInput.getText().toString();
                                if ("".equals(mubanName)) {
                                    ToastUtils.showToast(getActivity(), "请输入模版名称");
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
                                // 上传
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
                        MarkLog.markLog("获取饮料机" + MyApplication.getInstance().getMachine_sn() + "模板", SysConfig.LOG_LEVEL_NORMAL, MyApplication.getInstance().getMachine_sn());
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
                        alertView = new AlertView("提示", "确定要退货撤机吗？", "取消", new String[]{"确认"},
                                null, getContext(), AlertView.Style.Alert, DataUtil.dip2px(getContext(), Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                            @Override
                            public void onItemClick(Object o, int position) {
                                if (-1 == position) {
                                    alertView.dismiss();
                                } else {
                                    // TODO: 2016/8/24
                                    alertView.dismiss();
                                    // 接口请求
                                    loadingDialog = LoadingUtil.createLoadingDialog(getContext(), "提交中...", 1, R.drawable.ic_ios_juhua, true);
                                    loadingDialog.show();
                                    MarkLog.markLog("饮料机" + MyApplication.getInstance().getMachine_sn() + "撤机", SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
                                    // 退货撤机
                                    chejiRequest("0");
                                }
                            }
                        }).setCancelable(true).setOnDismissListener(null);
                        alertView.show();
                        break;
                    case 4:// 移机
                        alertView = new AlertView("提示", "确定要移机吗？", "取消", new String[]{"确认"}, null, getContext(),
                                AlertView.Style.Alert, DataUtil.dip2px(getContext(), Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                            @Override
                            public void onItemClick(Object o, int position) {
                                if (-1 == position) {
                                    alertView.dismiss();
                                } else {
                                    // TODO: 2016/8/24
                                    alertView.dismiss();
                                    // 接口请求
                                    loadingDialog = LoadingUtil.createLoadingDialog(getContext(), "提交中...", 1, R.drawable.ic_ios_juhua, true);
                                    loadingDialog.show();
                                    MarkLog.markLog("饮料机" + MyApplication.getInstance().getMachine_sn() + "移机", SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
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
                pop.show(tvMore);
            }
        }, 10);

    }

    @Override
    public void mubannotifyDataSetChanged(List<RoadTemple> roadTemples) {
        mubanInfoList.clear();
        for (RoadTemple roadTemple : roadTemples) {
            mubanInfoList.add(roadTemple);
        }
        // 显示模版信息
        mubanAdapter.notifyDataSetChanged();
    }

    @Override
    public void pandiannotifyDataSetChanged(List<PanDInfo> panDInfoList, int totalPage) {
        for (int i = 0; i < panDInfoList.size(); i++) {
            pandianInfoList.add(panDInfoList.get(i));
        }

        if (rlPandian.getVisibility() == View.GONE) {
            rlPandian.setVisibility(View.VISIBLE);
        }
        this.totalPage = totalPage;
        pandianAdapter.notifyDataSetChanged();
    }

    @Override
    public void loadmoreFinish() {
        ptrl.loadmoreFinish(PullToRefreshLayout.SUCCEED);
    }

    @Override
    public void dialogDismiss() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }
}
