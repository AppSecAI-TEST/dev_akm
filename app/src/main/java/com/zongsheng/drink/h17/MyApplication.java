package com.zongsheng.drink.h17;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;

import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.PushManager;
import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.NoHttp;
import com.zongsheng.drink.h17.background.bean.BindDesk;
import com.zongsheng.drink.h17.background.bean.BindGeZi;
import com.zongsheng.drink.h17.common.Constant;
import com.zongsheng.drink.h17.common.CrashHandler;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.L;
import com.zongsheng.drink.h17.common.SharedPreferencesUtils;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.front.bean.AdInfo;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.front.bean.PayM;
import com.zongsheng.drink.h17.front.bean.PayMethod;
import com.zongsheng.drink.h17.loading.bean.ZongsRealmMigration;
import com.zongsheng.drink.h17.service.BackGroundRequestService;
import com.zongsheng.drink.h17.service.GeTuiService;
import com.zongsheng.drink.h17.service.QueBiUploadService;
import com.zongsheng.drink.h17.service.QueHuoUploadService;
import com.zongsheng.drink.h17.service.ServerHeartBeatRequestService;
import com.zongsheng.drink.h17.util.FileUtils;
import com.zongsheng.drink.h17.util.PhoneUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;


/**
 * Created by xunku on 16/8/30.
 */

public class MyApplication extends Application {

    /** 系统参数 eg:1,23 [1:饮料机/其他综合机,货道数] */
//    private String systemParam = "";
    /**
     * 机器类型 1:饮料机 其他:综合机
     */
    private String machineType = "";
    /**
     * 货道数
     */
    private int roadCount = 0;
    /**
     * 机器时间 eg:2016083016150303
     */
    private String systemTime = "";
    /**
     * 售货机编号是否存在
     */
    private boolean isSnexist = true;
    /**
     * 最近一次在线支付的订单号
     */
    private String NoCashorderSn;
    /**
     * 系统状态
     * eg:1,1,0,1,1,0,0,0
     * 第一位: 0:VMC销售状态:停止 1:VMC销售状态:可销售
     * 第二位: 1:工作模式:待机可销售模式 2:工作模式:投币后等待操作模式 4:工作模式:维护模式 其他:售货机销售暂停状态
     * 第三位: 0:开关门状态:门关 1:开关门状态:门开
     * 第四位: 0:硬币器连接状态:未连接 1:硬币器连接状态:连接中
     * 第五位: 0:纸币器连接状态:未连接 1:纸币器连接状态:连接中
     * 第六位: 0:硬币5角:不缺币 1:硬币5角:缺币
     * 第七位: 0:硬币1元:不缺币 1:硬币1元:缺币
     * 第七位: 0:纸币器:未停用 1:纸币器:停用
     */
    private String systemStatus = "";
    /**
     * 有无故障 [空:无故障/故障编码]
     */
    private String troubleStatus = "";
    /**
     * 售空情况 eg:1,1,1,1,1,1,1,1,0,1,1,1,1,0,0,0,1,1,1,1,1,1,1 [第一货道o:有货/1:无货,第二货道……]
     */
    private String sellEmptyInfo = "";
    /**
     * 商品编码 eg:0,0,0,0,0,0,0,0,1,2,0,0,0,3,3,3,0,0,0,4,0,0,5 [第一货道编码,第二货道编码……]
     */
    private String goodsCode = "";
    /**
     * 机器认证状态 ，0没认证，1已认证
     */
    private String usedStatus = "";
    /**
     * MQ IP地址
     */
    private String mqIP = "";
    /**
     * 自动退款状态（）默认：0不启用，1全部启用；2支付宝启用；3微信启用；
     */
    private String automaticRefundState = "0";
    /**
     * VMC机器编号
     */
    private String machine_sn = "";

    private String machine_ccid = "";

    /**
     * 本地SD卡路径
     */
    private String sdCardPath = "";
    /**
     * 广告文件列表
     */
    private List<AdInfo> adFileList = new ArrayList<>();
    /**
     * 主机筛选后的产品数据
     */
    private List<GoodsInfo> goodsInfos = new ArrayList<>();

    private Map<Integer, GoodsInfo> deskGoodInfos = new HashMap<>();

    /**
     * 格子柜列表
     */
    private List<BindGeZi> bindGeZis = new ArrayList<>();
    /**
     * 格子柜显示商品列表
     */
    private List<GoodsInfo> cabinetGoods = new ArrayList<>();
    /**
     * 格子柜所有商品列表
     */
    private List<GoodsInfo> cabinetTotalGoods = new ArrayList<>();
    /**
     * 自营货道比率
     */
    private String roadRatio;

    /**
     * 是否维护状态
     */
    private boolean isWeihu = false;

    /**
     * 一键开门计数-标志位
     */
    private boolean isCount = false;

    /**
     * 一键开门计数-count
     */
    public int count = 0;

    /**
     * 附加格子柜列表
     */
    private List<Integer> geziList = new ArrayList<>();
    /**
     * 格子柜货道数
     */
    private Map<Integer, Integer> geziRoadCount = new HashMap<>();
    /**
     * 格子柜有效货道列表
     */
    private Map<Integer, List<Integer>> geziRoadListMap = new HashMap<>();

    /**
     * 绑定的格子柜列表
     */
    private List<BindDesk> bindDeskList;

    /**
     * 弹簧机(副柜)货道数
     */
    private int deskRoadCount;

    /**
     * 弹簧机(副柜)有效货道数
     */
    private List<Integer> deskRoadList = new ArrayList<>();

    /**
     * 澳柯玛格子柜库存信息 Map<箱号, Map<货道号, 是否有货 0:有 1:无>>
     */
    private Map<Integer, Map<Integer, String>> aokemaGeZiKuCunMap = new HashMap<>();

    /**
     * 连接失败的格子柜列表 List<箱号>
     */
    private List<Integer> connetFailGeziList = new ArrayList<>();

    private List<String> orderSns = new ArrayList<>();

    private boolean MQstate = true;

    private boolean deskConnState = false;

    private static MyApplication instance = null;

    private String versionType = "";

    RealmConfiguration config;

    /**
     * 本机启用的网络支付方式，应该在getMqServerIP()中初始化
     * TODO: 应该和MQIP一起从服务器获取支持的网络支付方式，在LoadingActivity中被执行，在BuyGoodsPopWindow中动态显示
     */
    private List<PayM> enabledPayMethod;

    public static MyApplication getInstance() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        if (getPackageName().equals(getCurProcessName(this))) {
            L.isDebug = true;
            FileUtils.isOpen = true;
            instance = this;
            // 获取SD卡的路径
            getSDCardPath();
            // 初始化realm数据库
            initRealm();
            NoHttp.initialize(this);
            Logger.setDebug(true);
            Logger.setTag("NoHttpSample");
            // 获取本地存储的machineSn
            getMachineSn();
            roadRatio = SharedPreferencesUtils.getParam(this, "roadRatio", "").toString();
            versionType = getResources().getString(R.string.versionType);
            machine_ccid = new PhoneUtil(this).getIccid();
            Intent intent = new Intent();
            intent.setClass(this, BackGroundRequestService.class);
            startService(intent);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent1 = new Intent(MyApplication.this, ServerHeartBeatRequestService.class);
                    startService(intent1);
                }
            }, 2000);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent1 = new Intent(MyApplication.this, QueBiUploadService.class);
                    startService(intent1);
                }
            }, 4000);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent1 = new Intent(MyApplication.this, QueHuoUploadService.class);
                    startService(intent1);
                }
            }, 6000);
            deleteCache();
        }
        //初始化个推
        PushManager.getInstance().initialize(this,null);
        //注册接收信息的Service
        PushManager.getInstance().registerPushIntentService(this, GeTuiService.class);
    }

    private void deleteCache() {
        if ((boolean) SharedPreferencesUtils.getParam(this, Constant.SP_IS_FIRST_IN, true)) {
            SharedPreferencesUtils.setParam(this, Constant.SP_MACHINE_COMMAND, System.currentTimeMillis());
            SharedPreferencesUtils.setParam(this, Constant.SP_IS_FIRST_IN, false);
        }
        if ((System.currentTimeMillis() - (long) SharedPreferencesUtils.getParam(this, Constant.SP_MACHINE_COMMAND, System.currentTimeMillis())) > Constant.DELETE_PERIOD) {
            FileUtils.deleteFile(Constant.PATH_NAME, Constant.FILE_NAME);
            SharedPreferencesUtils.setParam(this, Constant.SP_MACHINE_COMMAND, System.currentTimeMillis());
        }
    }

    private String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : activityManager.getRunningAppProcesses()) {
            if (appProcessInfo.pid == pid) {
                return appProcessInfo.processName;
            }
        }
        return "";
    }

    /**
     * 取得机器编码(加密文件)
     */
    public void getMachineSn() {
        File file = new File(sdCardPath + "/zongs/secret");
        String secretInfo;
        if (file.exists()) {
            StringBuilder result = new StringBuilder();
            try {

                BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
                String s;
                while ((s = br.readLine()) != null) {//使用readLine方法，一次读一行
                    result.append(System.lineSeparator() + s);
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            secretInfo = result.toString();
            L.e("读取加密的数据:", secretInfo);
            if (secretInfo != null && !"".equals(secretInfo)) {
                String machineSn = DataUtil.aesdecrypt(secretInfo);
                L.e("读取加密的机器编码:", machineSn);
                if (machineSn != null && !"".equals(machineSn)) {
                    this.machine_sn = machineSn;
                }
            }
        }
    }

    /**
     * 初始化realm
     */
    private void initRealm() {
        Realm.init(this);
        config = new RealmConfiguration.Builder()
                .name(SysConfig.MYREALM_NAME)
                .schemaVersion(SysConfig.REALM_VERSION)
                .migration(new ZongsRealmMigration())
                .build();
        Realm.compactRealm(config);
        Realm.setDefaultConfiguration(config);
    }

    /**
     * 清理realm备份缓存
     */
    public void clearRealCache() {
        File file = getFilesDir();
        String path = file.getAbsolutePath();

        file = new File(path + "/myrealm.realm");
        if (!file.exists()) {
            return;
        }
        int size = 0;
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(file);
            size = inStream.available();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (Exception e) {
            }

        }
        L.e("hah", "本地数据库文件大小:" + size);
        if (size > 1024 * 1024 * 50) {
            // 如果数据库文件大于50M 清理备份的缓存
            Realm.compactRealm(config);
        }
    }

    /**
     * 获取SD卡路径
     */
    private void getSDCardPath() {
        String path = Environment.getExternalStorageDirectory().getPath();
        if ("".equals(path)) {
            path = "/storage/emulated/0";
        }
        sdCardPath = path;
    }

    public List<String> getOrderSns() {
        return orderSns;
    }

    public String getVersionType() {
        return versionType;
    }

    public String getMachine_ccid() {
        return machine_ccid;
    }

    public String getAutomaticRefundState() {
        return automaticRefundState;
    }

    public void setAutomaticRefundState(String automaticRefundState) {
        this.automaticRefundState = automaticRefundState;
    }

    public boolean isSnexist() {
        return isSnexist;
    }

    public void setSnexist(boolean snexist) {
        isSnexist = snexist;
    }

    public String getNoCashorderSn() {
        return NoCashorderSn;
    }

    public void setNoCashorderSn(String noCashorderSn) {
        NoCashorderSn = noCashorderSn;
    }

    public boolean isMQstate() {
        return MQstate;
    }

    public void setMQstate(boolean MQstate) {
        this.MQstate = MQstate;
    }

    public String getRoadRatio() {
        return roadRatio;
    }

    public void setRoadRatio(String roadRatio) {
        this.roadRatio = roadRatio;
    }

    public String getSdCardPath() {
        return sdCardPath;
    }

    public void setSdCardPath(String sdCardPath) {
        this.sdCardPath = sdCardPath;
    }

    public List<AdInfo> getAdFileList() {
        return adFileList;
    }

    public void setAdFileList(List<AdInfo> adFileList) {
        this.adFileList = adFileList;
    }

    public String getSystemTime() {
        return systemTime;
    }

    public void setSystemTime(String systemTime) {
        this.systemTime = systemTime;
    }

    public String getSystemStatus() {
        return systemStatus;
    }

    public void setSystemStatus(String systemStatus) {
        this.systemStatus = systemStatus;
    }

    public String getTroubleStatus() {
        return troubleStatus;
    }

    public void setTroubleStatus(String troubleStatus) {
        this.troubleStatus = troubleStatus;
    }

    public String getSellEmptyInfo() {
        return sellEmptyInfo;
    }

    public void setSellEmptyInfo(String sellEmptyInfo) {
        this.sellEmptyInfo = sellEmptyInfo;
    }

    public String getGoodsCode() {
        return goodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        this.goodsCode = goodsCode;
    }

    public List<GoodsInfo> getGoodsInfos() {
        return goodsInfos;
    }

    public void setGoodsInfos(List<GoodsInfo> goodsInfos) {
        this.goodsInfos = goodsInfos;
    }

    public Map<Integer, GoodsInfo> getDeskGoodsInfo() {
        return deskGoodInfos;
    }

    public void setDeskGoodsInfo(Map<Integer, GoodsInfo> deskGoodInfos) {
        this.deskGoodInfos = deskGoodInfos;
    }

    public List<GoodsInfo> getCabinetGoods() {
        return cabinetGoods;
    }


    public void setCabinetGoods(List<GoodsInfo> cabinetGoods) {
        this.cabinetGoods = cabinetGoods;
    }

    public List<GoodsInfo> getCabinetTotalGoods() {
        return cabinetTotalGoods;
    }

    public void setCabinetTotalGoods(List<GoodsInfo> cabinetTotalGoods) {
        this.cabinetTotalGoods = cabinetTotalGoods;
    }

    public List<BindGeZi> getBindGeZis() {
        return bindGeZis;
    }

    public void setBindGeZis(List<BindGeZi> bindGeZis) {
        this.bindGeZis = bindGeZis;
    }

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public int getRoadCount() {
        return roadCount;
    }

    public void setRoadCount(int roadCount) {
        this.roadCount = roadCount;
    }

    public String getMachine_sn() {
        return machine_sn;
    }

    public void setMachine_sn(String machine_sn) {
        this.machine_sn = machine_sn;
    }

    public boolean isWeihu() {
        return isWeihu;
    }

    public void setWeihu(boolean weihu) {
        isWeihu = weihu;
    }

    public String getUsedStatus() {
        return usedStatus;
    }

    public void setUsedStatus(String usedStatus) {
        this.usedStatus = usedStatus;
    }

    public String getMqIP() {
        return mqIP;
    }

    public void setMqIP(String mqIP) {
        this.mqIP = mqIP;
    }

    public List<Integer> getGeziList() {
        return geziList;
    }

    public void setGeziList(List<Integer> geziList) {
        this.geziList = geziList;
    }

    public Map<Integer, Integer> getGeziRoadCount() {
        return geziRoadCount;
    }

    public void setGeziRoadCount(Map<Integer, Integer> geziRoadCount) {
        this.geziRoadCount = geziRoadCount;
    }

    public Map<Integer, List<Integer>> getGeziRoadListMap() {
        return geziRoadListMap;
    }

    public void setGeziRoadListMap(Map<Integer, List<Integer>> geziRoadListMap) {
        this.geziRoadListMap = geziRoadListMap;
    }

    public Map<Integer, Map<Integer, String>> getAokemaGeZiKuCunMap() {
        return aokemaGeZiKuCunMap;
    }

    public void setAokemaGeZiKuCunMap(Map<Integer, Map<Integer, String>> aokemaGeZiKuCunMap) {
        this.aokemaGeZiKuCunMap = aokemaGeZiKuCunMap;
    }

    public List<Integer> getConnetFailGeziList() {
        return connetFailGeziList;
    }

    public void setConnetFailGeziList(List<Integer> connetFailGeziList) {
        this.connetFailGeziList = connetFailGeziList;
    }

    public boolean isCount() {
        return isCount;
    }

    public void setIsCount(boolean count) {
        isCount = count;
    }

    /**
     * 设置副柜的货道总数
     *
     * @param deskRoadCount
     */
    public void setDeskRoadCount(int deskRoadCount) {
        this.deskRoadCount = deskRoadCount;
    }

    public int getDeskRoadCount() {
        return deskRoadCount;
    }

    public List<Integer> getDeskRoadList() {
        return deskRoadList;
    }

    public void setDeskRoadList(List<Integer> deskRoadList) {
        this.deskRoadList = deskRoadList;
    }

    public List<BindDesk> getBindDeskList() {

        return bindDeskList;
    }

    public void setBindDeskList(List<BindDesk> bindDeskList) {
        this.bindDeskList = bindDeskList;
    }

    public void setDeskConnState(boolean connState) {
        deskConnState = connState;
    }

    public boolean getDeskConnState() {
        return deskConnState;
    }

    public List<PayM> getEnabledPayMethod() {
        return enabledPayMethod;
    }

    public void setEnabledPayMethod(List<PayM> enabledPayMethod) {
        this.enabledPayMethod = enabledPayMethod;
    }
}
