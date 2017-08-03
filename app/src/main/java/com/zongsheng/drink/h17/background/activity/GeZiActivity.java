package com.zongsheng.drink.h17.background.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Response;
import com.zongsheng.drink.h17.ComActivity;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.background.bean.BindDesk;
import com.zongsheng.drink.h17.base.BasePresenter;
import com.zongsheng.drink.h17.observable.MyObservable;
import com.zongsheng.drink.h17.R;
import com.zongsheng.drink.h17.background.MarkLog;
import com.zongsheng.drink.h17.background.bean.BindGeZi;
import com.zongsheng.drink.h17.background.bean.MachineInfo;
import com.zongsheng.drink.h17.common.Constant;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.HideKeyBoard;
import com.zongsheng.drink.h17.common.NetWorkRequImpl;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.common.ToastUtils;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.interfaces.INetWorkRequCallBackListener;
import com.zongsheng.drink.h17.interfaces.INetWorkRequInterface;
import com.zongsheng.drink.h17.interfaces.IVSICallback2View;
import com.zongsheng.drink.h17.observable.SerialObservable;
import com.zongsheng.drink.h17.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


/**
 * Created by 谢家勋 on 2016/9/13.
 * 用于管理格子柜
 */
public class GeZiActivity extends ComActivity implements View.OnTouchListener, IVSICallback2View<String>, INetWorkRequCallBackListener {
    /**
     * 显示格子柜的ListView
     */
    @BindView(R.id.lv_gezi)
    ListView lvGezi;
    /**
     * 显示副柜的ListView
     */
    @BindView(R.id.lv_desk)
    ListView lvDesk;
    @BindView(R.id.ll_gezi_list)
    LinearLayout llGeziList;
    @BindView(R.id.rl_no_gezilist)
    RelativeLayout rlNoGezilist;
    @BindView(R.id.et_search_gezi)
    EditText etSearchGezi;
    @BindView(R.id.lv_search_list)
    ListView lvSearchList;
    @BindView(R.id.ll_gezi_search)
    RelativeLayout llGeziSearch;
    @BindView(R.id.ll_list)
    LinearLayout llSearch;

    private static final int GEZI_INFO = 0;
    private static final int GEZI_ADD = 1;
    private static final int GEZI_DEL = 2;
    private static final int GEZI_TAG = 3;
    private int xll;
    private int yll;
    private int w;
    private int h;
    private int nVSIGiziSize = 0;//vsi绑定的格子柜个数
    private int bindGeziSize = 0;
    private int nVSIDeskSize = 0;
    private int bindDeskSize = 0;

    private List<BindGeZi> geziList;
    private List<BindDesk> deskList;
    private Realm realm;
    // 添加的的list
    private List<MachineInfo> searchList = new ArrayList<>();
    private GeZiAdapter geziAdapter;
    private DeskAdapter deskAdapter;
    private CharSequence temp;
    private boolean btmp = false;
    private MyAdapter searchAdapter;
    private INetWorkRequInterface iNetWorkRequInterface = null;
    private String delMachineSn;
    private String addMachineSn;

    private LogUtil logUtil;
    public GeZiActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gezi);
        ButterKnife.bind(this);
        // 从数据库里获取本机绑定的所有的格子柜
        realm = Realm.getDefaultInstance();
        //填充列表，显示连接的格子柜和副柜
        initView();
        //从服务器获取绑定的格子柜，并更新本地数据，更新显示的格子柜和副柜
        requestBindGeZiList();
        MyObservable.getInstance().registObserver(this);

        logUtil = new LogUtil(this.getClass().getSimpleName());
        //这里控制是否打印Log
        logUtil.setShouldPrintLog(false);

        searchAdapter = new MyAdapter(searchList);
        lvSearchList.setAdapter(searchAdapter);
        lvSearchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final MachineInfo info = searchList.get(position);
                //以22开头的是副柜
                if (info.getMachineSn().startsWith("22")) {
                    if (bindDeskSize < nVSIDeskSize) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                final BindDesk desk = new BindDesk();
                                desk.setMachineSn(info.getMachineSn());
                                desk.setMachineName(info.getMachineName());
                                desk.setRoadCount(info.getRoadCount());
                                desk.setMachineType(info.getMachineType());
                                desk.setCreateTime(new Date().getTime() + "");
                                realm.copyToRealmOrUpdate(desk);
                            }
                        });
                        initView();
                        putBinding(searchList.get(position).getMachineSn());
                        searchList.clear();
                        searchAdapter.notifyDataSetChanged();
                        lvSearchList.removeAllViewsInLayout();
                    } else if (nVSIDeskSize != -1 && bindDeskSize >= nVSIDeskSize) {
                        ToastUtils.showToast(GeZiActivity.this, "绑定的副柜数量已经超过限制！");
                    }
                } else {
                    if (bindGeziSize < nVSIGiziSize) {
                        // 点击条目后关掉选择格子柜的页面
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                // 把点击的格子柜 添加到本地绑定的数据库里面
                                final BindGeZi gezi = new BindGeZi();
                                gezi.setMachineSn(info.getMachineSn());//编号
                                gezi.setMachineName(info.getMachineName());//名称
                                gezi.setRoadCount(info.getRoadCount());//格子数
                                gezi.setMachineType(info.getMachineType());//类型;
                                gezi.setCreateTime(new Date().getTime() + "");
                                realm.copyToRealmOrUpdate(gezi);
                            }
                        });
                        initView();
                        putBinding(searchList.get(position).getMachineSn());
                        searchList.clear();
                        searchAdapter.notifyDataSetChanged();
                        lvSearchList.removeAllViewsInLayout();
                    } else if (nVSIGiziSize != -1 && bindGeziSize >= nVSIGiziSize) {
                        ToastUtils.showToast(GeZiActivity.this, Constant.ERROR_INFO_GEZI_02);
                    }
                }
                llGeziSearch.setVisibility(View.GONE);
            }
        });

        etSearchGezi.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                logUtil.d("按键actionId:" + actionId + " event:" + event);
                //回车键
                if (actionId == 5) {
                    return true;
                }
                return false;
            }
        });

        etSearchGezi.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                temp = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (temp.length() == 0) {
                    RealmResults<MachineInfo> quanbu = realm.where(MachineInfo.class).findAll();
                    searchList.clear();
                    searchList.addAll(realm.copyFromRealm(quanbu));
                    searchAdapter.notifyDataSetChanged();
                    return;
                }

                RealmResults<MachineInfo> geZiInfo = realm.where(MachineInfo.class)
                        .contains("machineSn", temp.toString())
                        .findAll();
                searchList.clear();
                searchList.addAll(realm.copyFromRealm(geZiInfo));
                searchAdapter.notifyDataSetChanged();

            }
        });
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    private void putDeleteBindBackground(String machineSn) {
        String requestInterface = "api/machine/delete/gezigui/" + machineSn;
        if (machineSn.startsWith("22")) {
            MarkLog.markLog("从饮料机" + MyApplication.getInstance().getMachine_sn() + "删除副柜" + machineSn,
                    SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
        } else {
            MarkLog.markLog("从饮料机" + MyApplication.getInstance().getMachine_sn() + "删除格子柜" + machineSn,
                    SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
        }
        DataUtil.addBackGroundRequest(requestInterface, "", Constant.BACKGROUND_WHAT_0, false);
    }

    private void putBindingBackground(String machineSn) {
        String requestInterface = "api/machine/put/" + MyApplication.getInstance().getMachine_sn() + "/gezigui/" + machineSn;
        if (machineSn.startsWith("22")) {
            MarkLog.markLog("给饮料机" + MyApplication.getInstance().getMachine_sn() + "添加副柜" + machineSn,
                    SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
        } else {
            MarkLog.markLog("给饮料机" + MyApplication.getInstance().getMachine_sn() + "添加格子柜" + machineSn,
                    SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
        }
        DataUtil.addBackGroundRequest(requestInterface, "", Constant.BACKGROUND_WHAT_0, false);
    }

    // 提交绑定的格子柜
    private void putBinding(String machineSn) {
        addMachineSn = machineSn;
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/put/" + MyApplication.getInstance().getMachine_sn() + "/gezigui/" + machineSn;
        if (iNetWorkRequInterface == null) {
            iNetWorkRequInterface = new NetWorkRequImpl(this);
        }
        iNetWorkRequInterface.request(url, GEZI_ADD, RequestMethod.GET);
    }

    private void putDeleteBind(String machineSn) {
        delMachineSn = machineSn;
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/delete/gezigui/" + machineSn;
        if (iNetWorkRequInterface == null) {
            iNetWorkRequInterface = new NetWorkRequImpl(this);
        }
        iNetWorkRequInterface.request(url, GEZI_DEL, RequestMethod.GET);
    }

    private void initView() {
        //从本地获得连接的所有副柜信息
        RealmResults<BindDesk> bindDesks = realm.where(BindDesk.class).findAll().sort("createTime", Sort.ASCENDING);
        MyApplication.getInstance().setBindDeskList(realm.copyFromRealm(bindDesks));

        // 从本地数据库里取出绑定的格子柜
        RealmResults<BindGeZi> bindGezis = realm.where(BindGeZi.class).findAll();
        bindGezis = bindGezis.sort("createTime", Sort.ASCENDING);
        MyApplication.getInstance().setBindGeZis(realm.copyFromRealm(bindGezis));

        bindGeziSize = bindGezis.size();
        bindDeskSize = bindDesks.size();
        // 如果格子柜的数量为不为空
        if (bindGezis.size() == 0 && bindDesks.size() == 0) {
            // 如果当前的查询不到数据,显示空布局来填充activity
            rlNoGezilist.setVisibility(View.VISIBLE);
            llGeziList.setVisibility(View.GONE);
        } else {
            // 如果查到数据,给geziList设置数据
            rlNoGezilist.setVisibility(View.GONE);
            llGeziList.setVisibility(View.VISIBLE);
            geziList = realm.copyFromRealm(bindGezis);
            deskList = realm.copyFromRealm(bindDesks);
            geziAdapter = new GeZiAdapter();
            deskAdapter = new DeskAdapter();
            lvGezi.setAdapter(geziAdapter);
            lvDesk.setAdapter(deskAdapter);
            logUtil.d("更新绑定的格子柜和副柜列表");
        }
    }

    /**
     * 获取格子柜的列表
     */
    private void requestBindGeZiList() {
        logUtil.d("请求获得服务端定义的绑定的格子柜和副柜信息");
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/" + MyApplication.getInstance().getMachine_sn() + "/geziguilist";
        if (iNetWorkRequInterface == null) {
            iNetWorkRequInterface = new NetWorkRequImpl(this);
        }
        iNetWorkRequInterface.request(url, GEZI_TAG, RequestMethod.GET);
    }

    /**
     * 获取可以添加的格子柜的列表
     */
    private void requestGeZiList() {
        logUtil.d("从服务器请求可以添加的格子柜和副柜列表");
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/" + MyApplication.getInstance().getMachine_sn() + "/geziguiinfolist";
        if (iNetWorkRequInterface == null) {
            iNetWorkRequInterface = new NetWorkRequImpl(this);
        }
        iNetWorkRequInterface.request(url, GEZI_INFO, RequestMethod.GET);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();

                int left = llSearch.getLeft();
                int right = llSearch.getRight();
                int top = llSearch.getTop();
                int bottom = llSearch.getBottom();

                if (x < left || x > right || y < top || y > bottom && llGeziSearch.getVisibility() == View.VISIBLE) {
                    llGeziSearch.setVisibility(View.GONE);
                }
                break;
        }
        return true;
    }

    @Override
    public void MsgCallback(String msg) {//运行状态!: 1,0,0,0,0,14,10,1|1|0|0|0|0|0|
        if (!btmp) {
            btmp = true;
            String str[] = msg.split(",");
            String string = str[str.length - 1];
            nVSIGiziSize = getGeziSizeByVSI(string);
            nVSIDeskSize = getDeskSizeByVSI(string);
            logUtil.d("VMC返回实际连接的格子柜数 "+nVSIGiziSize);
            logUtil.d("VMC返回实际连接的副柜数 "+nVSIDeskSize);
            if (nVSIGiziSize == -1) {
                ToastUtils.showToast(GeZiActivity.this, Constant.ERROR_INFO_GEZI_01);
            }
            if (nVSIDeskSize == -1) {
                ToastUtils.showToast(GeZiActivity.this, "副柜遥控器控制不合理！");
            }
        }
    }

    /**
     * 获取澳柯玛遥控所设置的格子柜个数
     *
     * @param str
     * @return
     */
    private int getGeziSizeByVSI(String str) {//  1|1|0|0|0|0|0|
        String string[] = str.split("\\|");
        for (int i = 1; i < string.length - 1; i++) {
            if (Integer.parseInt(string[i + 1]) > Integer.parseInt(string[i])) {
                return -1;
            }
        }
        int temp = 0;
        for (int i = 1; i < string.length; i++) {
            if (Integer.parseInt(string[i]) == 1) {
                temp++;
            }
        }
        return temp;
    }

    /**
     * 获取澳柯玛遥控所设置的副柜个数
     *
     * @param str
     * @return
     */
    private int getDeskSizeByVSI(String str) {
        int temp = 0;
        String string[] = str.split("\\|");
        if (Integer.parseInt(string[0]) == 1) {
            temp = 1;
        }
        return temp;
    }

    @Override
    public void onSucceed(int what, Response<String> response) throws JSONException {
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
                    //从服务器下载所有可以添加的格子柜和副柜信息
                    case GEZI_INFO:
                        try {
                            if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                Gson gson = new Gson();
                                Type type = new TypeToken<ArrayList<MachineInfo>>() {
                                }.getType();
                                List<MachineInfo> result = gson.fromJson(jsonResult.getString("machineList"), type);
                                if (result != null) {
                                    searchList.clear();
                                    searchList.addAll(result);
                                    searchAdapter.notifyDataSetChanged();
                                    if (!realm.isInTransaction()) {
                                        realm.beginTransaction();
                                    }
                                    //数据请求成功之后  把所有的格子柜信息存到一个格子柜表里面去
                                    // 方便按机器号查询
                                    for (int i = 0; i < searchList.size(); i++) {
                                        final MachineInfo info = new MachineInfo();
                                        info.setMachineSn(searchList.get(i).getMachineSn());
                                        info.setMachineName(searchList.get(i).getMachineName());
                                        info.setMachineType(searchList.get(i).getMachineType());
                                        info.setRoadCount(searchList.get(i).getRoadCount());
                                        realm.copyToRealmOrUpdate(info);
                                    }
                                    realm.commitTransaction();
                                    logUtil.d("从服务端成功获取到绑定的格子柜和副柜信息并更新列表");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case GEZI_ADD: //添加成功
                        if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                            if (addMachineSn.startsWith("22")) {
                                //写入操作日志
                                MarkLog.markLog("给饮料机" + MyApplication.getInstance().getMachine_sn() + "添加副柜" + addMachineSn,
                                        SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
                            } else {
                                //写入操作日志
                                MarkLog.markLog("给饮料机" + MyApplication.getInstance().getMachine_sn() + "添加格子柜" + addMachineSn,
                                        SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
                            }

                        } else {
                            putBindingBackground(addMachineSn);
                        }

                        break;
                    case GEZI_DEL: //删除成功
                        if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                            if (addMachineSn.startsWith("22")) {
                                //写入操作日志
                                MarkLog.markLog("从饮料机" + MyApplication.getInstance().getMachine_sn() + "删除副柜" + delMachineSn,
                                        SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
                            } else {
                                //写入操作日志
                                MarkLog.markLog("从饮料机" + MyApplication.getInstance().getMachine_sn() + "删除格子柜" + delMachineSn,
                                        SysConfig.LOG_LEVEL_IMPORTANT, MyApplication.getInstance().getMachine_sn());
                            }

                        } else {
                            putDeleteBindBackground(delMachineSn);
                        }
                        break;
                    case GEZI_TAG:
                        //获取主机绑定的格子柜
                        logUtil.d("从服务器获取主机绑定的格子柜和副柜信息");
                        if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                            Gson gson = new Gson();

                            Type machineInfoType = new TypeToken<List<MachineInfo>>() {
                            }.getType();
                            final List<MachineInfo> machineInfoList = gson.fromJson(jsonResult.getString("machineList"), machineInfoType);
                            if (machineInfoList.size() == 0) {
                                //删除本地所有的格子柜和副柜信息
                                deleteAllBindGeZiOrDesk();
                                break;
                            }
                            //存储获取到的格子柜或者副柜编号
                            List<String> machinesnList = new ArrayList();
                            for (int i = 0; i < machineInfoList.size(); i++) {
                                machinesnList.add(machineInfoList.get(i).getMachineSn());
                                logUtil.d("服务器获得的格子柜编号 "+machineInfoList.get(i).getMachineSn());
                            }
                            deleteBindGeziOrDesk(machinesnList);

//                            Type type = new TypeToken<ArrayList<BindGeZi>>() {
//                            }.getType();
//                            final List<BindGeZi> machineList = gson.fromJson(jsonResult.getString("machineList"), type);
//                            if (machineList.size() == 0) {
//                                deleteAllBindGeZiOrDesk();   // 删除本地所有的格子柜
//                                break;
//                            }
//                            List machineSnList = new ArrayList<>();  // 格子柜编号
//                            for (int i = 0; i < machineList.size(); i++) {
//                                machineSnList.add(machineList.get(i).getMachineSn());
//                                Log.e("柜型列表", machineList.get(i).getMachineSn());
//                            }
//                            deleteBindGeziOrDesk(machineSnList);
                        }
                        break;
                }

            }
        }
    }

    @Override
    public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {
        logUtil.d("网络请求失败");
        if (networkMillis > 5000) {
//                ToastUtils.showToast(GeZiActivity.this, "服务器繁忙,请稍后再试");
//                finish();
            ToastUtils.showToast(GeZiActivity.this, Constant.ERROR_INFO_GEZI_03);
        }
        ToastUtils.showToast(GeZiActivity.this, Constant.ERROR_INFO_GEZI_03);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof SerialObservable) {
            super.update(observable, o);
        } else if (observable instanceof MyObservable) {
            finish();
        }
    }


    private class MyAdapter extends BaseAdapter {
        private List<MachineInfo> list;

        public MyAdapter(List<MachineInfo> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public MachineInfo getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(GeZiActivity.this, R.layout.item_gezi_search_list, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            MachineInfo info = getItem(position);
            if (info == null) {
                return null;
            }
            holder.tvGeziSn.setText(info.getMachineSn());
            holder.tvGeziName.setText(info.getMachineName());
            return convertView;
        }
    }

    static class ViewHolder {
        @BindView(R.id.tv_gezi_sn)
        TextView tvGeziSn;
        @BindView(R.id.tv_gezi_name)
        TextView tvGeziName;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

    }

    @OnClick({R.id.rl_back, R.id.iv_add, R.id.iv_search, R.id.iv_bg_add, R.id.ll_gezi_search, R.id.ll_list})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_back:

                finish();
                break;
            case R.id.iv_add:
            case R.id.iv_bg_add:
                logUtil.d("请求VMC报告实际连接的格子柜和副柜数信息");
                btmp = false;
                //请求VMC报告实际连接的格子柜信息，信息被保存到nVSIGeziSize,nVSIDeskSize
                getMachineStatus(this);
                if (searchList.size() > 0) {
                    searchList.clear();
                }
                //显示空白列表
                searchAdapter.notifyDataSetChanged();
                llGeziSearch.setVisibility(View.VISIBLE);
                etSearchGezi.setText("");
                final RealmResults<MachineInfo> quanbu = realm.where(MachineInfo.class).findAll();
                // All changes to data must happen in a transaction
                // Delete all matches
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        quanbu.deleteAllFromRealm();
                    }
                });
                requestGeZiList();
                break;
            case R.id.iv_search:
                break;
            case R.id.ll_gezi_search:
                llGeziSearch.setVisibility(View.GONE);
                searchList.clear();
                searchAdapter.notifyDataSetChanged();
                lvSearchList.removeAllViewsInLayout();
                break;
            case R.id.ll_list:
                // do nothing
                break;
            default:
                break;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // TODO：这个可以用AlertDialog代替
            float y = event.getRawY();
            float x = event.getRawX();
            int[] locations = new int[2];
            llGeziSearch.getLocationOnScreen(locations);
            xll = locations[0];//获取组件当前位置的横坐标
            yll = locations[1];//获取组件当前位置的纵坐标
            // 每次进来的时候 获取最新的格子柜列表
            // 从relm里面获取格子柜的列表
            w = llGeziSearch.getWidth();
            h = llGeziSearch.getHeight();
            if (y > yll + h || y < yll || x > xll + w || x < xll) {
                llGeziSearch.setVisibility(View.GONE);
            }
        }
        return true;
    }

    private class GeZiAdapter extends BaseAdapter {

        AlertView alertView;

        @Override
        public int getCount() {
            return geziList.size();
        }

        @Override
        public BindGeZi getItem(int position) {
            return geziList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final GeZiViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(GeZiActivity.this, R.layout.item_gezi_list, null);
                holder = new GeZiViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (GeZiViewHolder) convertView.getTag();
            }

            final BindGeZi zi = getItem(position);

            if (deskList.size() > 0) {
                holder.tvNum.setText("" + (position + 2));
            } else {
                holder.tvNum.setText("" + (position + 1));
            }
            holder.tvGeziSn.setText(zi.getMachineSn());
            holder.tvGeziName.setText("格子柜");
            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertView = new AlertView("提示", "确定要删除格子柜(" + zi.getMachineSn() + ")吗？", "取消",
                            new String[]{"确认"}, null, GeZiActivity.this, AlertView.Style.Alert, DataUtil.dip2px(GeZiActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            if (-1 == position) {
                                alertView.dismiss();
                            } else {
                                delete(zi.getMachineSn(), "gezi");
                            }
                        }
                    }).setCancelable(true).setOnDismissListener(null);
                    alertView.show();
                }
            });

            return convertView;
        }

    }

    private class DeskAdapter extends BaseAdapter {

        AlertView alertView;

        @Override
        public int getCount() {
            return deskList.size();
        }

        @Override
        public BindDesk getItem(int position) {
            return deskList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final GeZiViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(GeZiActivity.this, R.layout.item_gezi_list, null);
                holder = new GeZiViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (GeZiViewHolder) convertView.getTag();
            }

            final BindDesk desk = getItem(position);

            holder.tvNum.setText(position + 1 + "");
            holder.tvGeziSn.setText(desk.getMachineSn());
            holder.tvGeziName.setText("副柜");
            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertView = new AlertView("提示", "确定要删除副柜(" + desk.getMachineSn() + ")吗？", "取消",
                            new String[]{"确认"}, null, GeZiActivity.this, AlertView.Style.Alert, DataUtil.dip2px(GeZiActivity.this, Double.parseDouble(getResources().getString(R.string.margin_alert_left_right))), new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            if (-1 == position) {
                                alertView.dismiss();
                            } else {
                                delete(desk.getMachineSn(), "desk");
                            }
                        }
                    }).setCancelable(true).setOnDismissListener(null);
                    alertView.show();
                }
            });

            return convertView;
        }

    }

    /**
     * 删除本地所有的格子柜和副柜信息
     */
    private void deleteAllBindGeZiOrDesk() {
        final List<BindGeZi> bindGeZis = realm.where(BindGeZi.class).findAll();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (int i = 0; i < bindGeZis.size(); i++) {
                    bindGeZis.get(i).deleteFromRealm();
                }
            }
        });

        final BindDesk bindDesk = realm.where(BindDesk.class).findFirst();
        if (bindDesk != null) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    bindDesk.deleteFromRealm();
                }
            });
        }

        // 删除格子柜和副柜中的商品
        final RealmResults<GoodsInfo> goodsInfos = realm.where(GoodsInfo.class).equalTo("goodsBelong", "2")
                .equalTo("goodsBelong", "3").findAll();
        //从数据库删除
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (goodsInfos != null && goodsInfos.size() > 0) {
                    goodsInfos.deleteAllFromRealm();
                }
            }
        });
        initView();
    }

    /**
     * 从PC获取所有本地的格子柜，本地多余的删掉
     *
     * @param list 服务器获得的格子柜和副柜编码列表
     */
    private void deleteBindGeziOrDesk(List<String> list) {
        //可以有多个格子柜
        List<BindGeZi> bindGeZis = realm.where(BindGeZi.class).findAll();
        //只有一个副柜
        BindDesk bindDesk = realm.where(BindDesk.class).findFirst();

        for (int i = 0; i < bindGeZis.size(); i++) {
            BindGeZi bindGeZi = bindGeZis.get(i);
            String machineSn = bindGeZi.getMachineSn();
            if (!list.contains(machineSn)) {
                if (!realm.isInTransaction()) {
                    realm.beginTransaction();
                }
                //如果服务端的List中不包含本地的格子柜，则删除本地的格子柜
                bindGeZi.deleteFromRealm();
                realm.commitTransaction();
                // 删除格子柜的商品
                final RealmResults<GoodsInfo> cabinetGoods = realm.where(GoodsInfo.class).equalTo("goodsBelong", "2").equalTo("machineID", machineSn).findAll();
                //从数据库删除
                if (cabinetGoods != null && cabinetGoods.size() > 0) {
                    if (!realm.isInTransaction()) {
                        realm.beginTransaction();
                    }
                    cabinetGoods.deleteAllFromRealm();
                    realm.commitTransaction();
                }
            }
        }

//        for (int count = 0; count < list.size(); count++) {
//            String sn = (String) list.get(count);
//            if (sn.startsWith("22")) {
//                BindGeZi bindGeZi = realm.where(BindGeZi.class).equalTo("machineSn", sn).findFirst();
//                if (!realm.isInTransaction()) {
//                    realm.beginTransaction();
//                }
//                bindGeZi.deleteFromRealm();
//                realm.commitTransaction();
//            }
//        }
        //如果服务端的List中不包含本地的副柜，则删除本地的副柜
        String deskMachineSn = bindDesk.getMachineSn();
        if (!list.contains(deskMachineSn)) {
            if (!realm.isInTransaction()) {
                realm.beginTransaction();
            }
            bindDesk.deleteFromRealm();
            realm.commitTransaction();

            final RealmResults<GoodsInfo> deskGoods = realm.where(GoodsInfo.class).equalTo("goodsBelong", "3")
                    .equalTo("machineID", deskMachineSn).findAll();
            if (deskGoods != null && deskGoods.size() > 0) {
                if (!realm.isInTransaction()) {
                    realm.beginTransaction();
                }
                deskGoods.deleteAllFromRealm();
                realm.commitTransaction();
            }
        }
        //TODO:这里直接更新列表会好一些，不用重复创建Adapter
        initView();
    }


    private void delete(String s, String machineType) {
        String goodsBelong = "-1";
        if ("gezi".equals(machineType)) {
            final BindGeZi machineSn = realm.where(BindGeZi.class).equalTo("machineSn", s).findFirst();
            goodsBelong = "2";
            if (machineSn != null) {
                if (!realm.isInTransaction()) {
                    realm.beginTransaction();
                }
                machineSn.deleteFromRealm();
                realm.commitTransaction();
            }
        }
        if ("desk".equals(machineType)) {
            final BindDesk machineSn = realm.where(BindDesk.class).equalTo("machineSn", s).findFirst();
            goodsBelong = "3";
            if (machineSn != null) {
                if (!realm.isInTransaction()) {
                    realm.beginTransaction();
                }
                machineSn.deleteFromRealm();
                realm.commitTransaction();
            }
        }
        // 删除格子柜的商品
        final RealmResults<GoodsInfo> cabinetGoods = realm.where(GoodsInfo.class).equalTo("goodsBelong", goodsBelong).equalTo("machineID", s).findAll();
        //从数据库删除
        if (cabinetGoods != null && cabinetGoods.size() > 0) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    cabinetGoods.deleteAllFromRealm();
                }
            });
        }
        initView();
        putDeleteBind(s);
    }

    static class GeZiViewHolder {
        @BindView(R.id.tv_num)
        TextView tvNum;
        @BindView(R.id.tv_gezi_sn)
        TextView tvGeziSn;
        @BindView(R.id.tv_gezi_name)
        TextView tvGeziName;
        @BindView(R.id.iv_delete)
        ImageView ivDelete;

        GeZiViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    protected void onDestroy() {
        if (iNetWorkRequInterface != null) {
            iNetWorkRequInterface.cancel();
            iNetWorkRequInterface = null;
        }
        if (realm != null) {
            realm.close();
        }
        MyObservable.getInstance().unregistObserver(this);
        super.onDestroy();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (HideKeyBoard.isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        //不允许右键退出,必须使用按钮退出
        //super.onPause();
    }
}
