package com.zongsheng.drink.h17;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

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
import com.zongsheng.drink.h17.front.bean.PayMethod;
import com.zongsheng.drink.h17.loading.bean.ZongsRealmMigration;
import com.zongsheng.drink.h17.service.BackGroundRequestService;
import com.zongsheng.drink.h17.service.GeTuiService;
import com.zongsheng.drink.h17.service.QueBiUploadService;
import com.zongsheng.drink.h17.service.QueHuoUploadService;
import com.zongsheng.drink.h17.service.ServerHeartBeatRequestService;
import com.zongsheng.drink.h17.util.FileUtils;
import com.zongsheng.drink.h17.util.LogUtil;
import com.zongsheng.drink.h17.util.PhoneUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
     * 系统状态，VMC在BuyActivity启动之前报告
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
     * 有无故障 [空:无故障/故障编码]，VMC在BuyActivity启动之前报告
     */
    private String troubleStatus = "";
    /**
     * 主机售空情况 eg:1,1,1,1,1,1,1,1,0,1,1,1,1,0,0,0,1,1,1,1,1,1,1 [第一货道o:有货/1:无货,第二货道……]，VMC在BuyActivity启动之前报告，且定时循环报告
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
     * 自营货道比率
     */
    private String roadRatio;

    /**
     * 是否维护状态，当进入后台HomeActivity，处于维护状态，进入前台BuyActivity，处于售卖状态
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
     * 本地SD卡路径
     */
    private String sdCardPath = "";
    /**
     * 广告文件列表
     */
    private List<AdInfo> adFileList = new ArrayList<>();
    /**
     * 不包含重复商品的主机商品列表，查询本地数据库GoodsInfo，在BuyActivity启动之前初始化
     */
    private List<GoodsInfo> goodsInfos = new ArrayList<>();

    /**
     * 副柜的货道号和对应商品列表，查询本地数据库，在BuyActivity启动之前初始化，在商品售卖页面使用
     */
    private Map<Integer, GoodsInfo> deskGoodInfos = new HashMap<>();

    /**
     * 机器类型 1:饮料机 其他:综合机，VMC在启动BuyActivity之前报告
     */
    private String machineType = "";
    /**
     * 主机货道数，VMC在启动BuyActivity之前报告
     */
    private int roadCount = 0;
    /**
     * 绑定的格子柜列表，查询本地数据库BindGezi，在BuyActivity启动之前初始化
     *
     */
    private List<BindGeZi> bindGeZis = new ArrayList<>();
    /**
     * 实际连接的格子柜显示商品列表，查询本地数据库，在BuyActivity启动之前初始化，售货界面显示的就是这个列表，此列表中不包含重复商品，商品库存是格子柜所有格子该商品库存之和
     */
    private List<GoodsInfo> cabinetGoods = new ArrayList<>();
    /**
     * 实际连接的格子柜所有商品列表，查询本地数据库，在BuyActivity启动之前初始化，此列表中包含商品ID相同但属于不同格子柜或不同格子的商品
     */
    private List<GoodsInfo> cabinetTotalGoods = new ArrayList<>();

    /**
     * 附加格子柜列表，当前连接的格子柜箱号列表，从2开始，不包括副柜，VMC在启动BuyActivity之前报告
     */
    private List<Integer> geziList = new ArrayList<>();
    /**
     * 指定箱号的格子柜有效货道数，VMC在启动BuyActivity之前报告
     */
    private Map<Integer, Integer> geziRoadCount = new HashMap<>();
    /**
     * 指定箱号的格子柜有效货道号（真实货道）列表，VMC在启动BuyActivity之前报告
     */
    private Map<Integer, List<Integer>> geziRoadListMap = new HashMap<>();

    /**
     * 绑定的副柜列表，查询本地数据库，在BuyActivity启动之前初始化
     */
    private List<BindDesk> bindDeskList;

    /**
     * 弹簧机(副柜)有效货道数，VMC在启动BuyActivity之前报告
     */
    private int deskRoadCount;

    /**
     * 弹簧机(副柜)有效货道号列表（真实货道号），VMC在BuyActivity启动之前报告
     */
    private List<Integer> deskRoadList = new ArrayList<>();

    /**
     * 澳柯玛格子柜库存信息 Map<箱号, Map<货道号（非真实货道号而是1~80）, 是否有货 0:有 1:无>>，VMC在BuyActivity启动之前报告
     */
    private Map<Integer, Map<Integer, String>> aokemaGeZiKuCunMap = new HashMap<>();

    /**
     * 连接失败的格子柜列表 List<箱号>，VMC在BuyActivity启动之前报告
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
     */
    private List<PayMethod> enabledPayMethod;

    public static MyApplication getInstance() {
        return instance;
    }

    public LogUtil logUtil;
    //购买、出货流程的日志工具
    public LogUtil logBuyAndShip;
    //补货流程的日志工具
    public LogUtil logBuHuo;
    //PC、VMC底层通信的日志工具
    public LogUtil logBasicCom;
    //初始化流程的全局Log，包括各重要组件的初始化
    public LogUtil logInit;

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

        if (getPackageName().equals(getCurProcessName(this))) {
            instance = this;
            //控制全局的日志打印
            logUtil = new LogUtil(this.getClass().getSimpleName());
            logUtil.setShouldPrintLogAllCtrl(true);

            logBuHuo = new LogUtil("buHuo");
            logBasicCom = new LogUtil("pc_vmc");
            logBuyAndShip = new LogUtil("buyAndShip");
            logInit = new LogUtil("init");

            //控制是否输出日志到文件
            FileUtils.isOpen = true;
            //设置要输出到磁盘的日志TAG
            List<String> tagList = new ArrayList<>();
            tagList.add("buHuo");
            tagList.add("pc_vmc");
            tagList.add("buyAndShip");
            tagList.add("init");
            LogUtil.setLogTags(tagList);
            initAndDeleteLogFileCache();

            L.isDebug = true;
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
            MyApplication.getInstance().getLogInit().d("读取CCID =  "+machine_ccid);
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
        }
        //初始化个推
//        PushManager.getInstance().initialize(this,null);
        //注册接收信息的Service
//        PushManager.getInstance().registerPushIntentService(this, GeTuiService.class);
        logInit.d("----------------Application启动---------------");
    }

    /**
     * 初始化和定时删除生成的日志文件
     */
    private void initAndDeleteLogFileCache() {
        String fileName = FileUtils.getTodayLogFileName();
        File logDir = new File(Constant.PATH_NAME);
        File logFile;
        if (!logDir.exists()){
            logDir.mkdir();
        }
        File[] files = logDir.listFiles();
        if (files.length == 0){
            logFile = new File(logDir,fileName);
        }else {
            logFile = new File(logDir,fileName);
            //如果在目录中找到今天的日志文件，说明不用生成新的日志文件
            for (File file : files){
                String currentFileName = file.getName();
                if (!currentFileName.startsWith("command_")){
                    continue;
                }
                String dateString = currentFileName.substring(8,currentFileName.length()-4);
                //如果记录早于20天，删除该记录
                long currentTime = new Date().getTime();
                long fileCreateTime = FileUtils.fileNameFormat.parse(dateString,new ParsePosition(0)).getTime();
                if (currentTime - fileCreateTime > 20*24*60*60*1000){
                    file.delete();
                }

                if (file.getName().equals(fileName)){
                    logFile = file;
                    break;
                }
            }


        }

        //设置本次开机日志保存的文件
        FileUtils.setLogFile(logFile);

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
            //TODO:如果这里读不到机器编码，应该提示
            secretInfo = result.toString();
//            L.e("读取加密的数据:", secretInfo);
            if (secretInfo != null && !"".equals(secretInfo)) {
                String machineSn = DataUtil.aesdecrypt(secretInfo);
                MyApplication.getInstance().getLogInit().d("读取加密的机器编码 = "+machineSn);
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
//        L.e("hah", "本地数据库文件大小:" + size);
        MyApplication.getInstance().getLogInit().d("Realm数据库大小 = "+size/1024/1024+"M");
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

    public List<PayMethod> getEnabledPayMethod() {
        return enabledPayMethod;
    }

    public void setEnabledPayMethod(List<PayMethod> enabledPayMethod) {
        this.enabledPayMethod = enabledPayMethod;
    }

    /**
     * 用于支付、出货的全局Log工具
     * @return LogUtil
     */
    public LogUtil getLogBuyAndShip() {
        return logBuyAndShip;
    }

    /**
     * 用于PC和VMC底层通信的全局Log工具
     * @return LogUtil
     */
    public LogUtil getLogBasicCom() {
        return logBasicCom;
    }

    /**
     * 用于补货流程的全局Log工具
     * @return LogUtil
     */
    public LogUtil getLogBuHuo() {
        return logBuHuo;
    }

    /**
     * 初始化流程的全局Log，包括各重要组件的初始化
     * @return LogUtil
     */
    public LogUtil getLogInit() {
        return logInit;
    }
}
