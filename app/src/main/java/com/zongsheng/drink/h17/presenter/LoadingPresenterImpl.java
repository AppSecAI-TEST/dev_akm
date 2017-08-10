package com.zongsheng.drink.h17.presenter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yolanda.nohttp.Headers;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.download.DownloadListener;
import com.yolanda.nohttp.rest.Response;
import com.zongsheng.drink.h17.Model.ILoadingModel;
import com.zongsheng.drink.h17.Model.LoadingModelImpl;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.background.bean.BindDesk;
import com.zongsheng.drink.h17.background.bean.BindGeZi;
import com.zongsheng.drink.h17.base.BasePresenter;
import com.zongsheng.drink.h17.common.Constant;
import com.zongsheng.drink.h17.common.DataUtil;
import com.zongsheng.drink.h17.common.L;
import com.zongsheng.drink.h17.common.NetWorkRequImpl;
import com.zongsheng.drink.h17.common.SharedPreferencesUtils;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.front.bean.AdInfo;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.front.bean.PayMethod;
import com.zongsheng.drink.h17.front.bean.ServerHelpInfo;
import com.zongsheng.drink.h17.interfaces.ILoadingInterface;
import com.zongsheng.drink.h17.interfaces.INetWorkRequCallBackListener;
import com.zongsheng.drink.h17.interfaces.INetWorkRequInterface;
import com.zongsheng.drink.h17.loading.LoadingActivity;
import com.zongsheng.drink.h17.service.HexinService;
import com.zongsheng.drink.h17.util.FileUtils;
import com.zongsheng.drink.h17.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;


/**
 * Created by Suchengjian on 2017.3.22.
 */

public class LoadingPresenterImpl extends BasePresenter<ILoadingInterface> implements ILoadingPresenter, INetWorkRequCallBackListener {
    private static final int HELP_TAG = 0;
    private static final int GEZI_TAG = 1;
    private static final int MQIP_TAG = 2;

    private ILoadingInterface iLoadingInterface = null;
    private INetWorkRequInterface iNetWorkRequInterface = null;
    private ILoadingModel iLoadingModel = null;
    private LogUtil logUtil;

    public LoadingPresenterImpl(ILoadingInterface iLoadingInterface) {
        bindView(iLoadingInterface);
        if (iNetWorkRequInterface == null) {
            iNetWorkRequInterface = new NetWorkRequImpl(this);
        }
        if (iLoadingModel == null) {
            iLoadingModel = new LoadingModelImpl();
        }

        logUtil = new LogUtil(this.getClass().getSimpleName());
        //这里控制是否打印Log
        logUtil.setShouldPrintLog(false);
    }

    @Override
    public void onSucceed(int what, Response<String> response) {
        int responseCode = response.getHeaders().getResponseCode();// 服务器响应码
        if (responseCode == 200) {
            if (RequestMethod.HEAD != response.getRequestMethod()) {
                JSONObject jsonResult = null;
                try {
                    jsonResult = new JSONObject(response.get());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (what) {
                    case HELP_TAG://
                        try {
                            // 如果成功
                            if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                // 清空本地
                                Gson gson = new Gson();
                                Type type = new TypeToken<ArrayList<ServerHelpInfo>>() {
                                }.getType();
                                List<ServerHelpInfo> rs = gson.fromJson(jsonResult.getString("helpInfoList"), type);
                                if (rs != null && rs.size() > 0) {
                                    // obtain the results of a query
                                    iLoadingModel.delHelpInfo2Realm();
                                } else {
                                    return;
                                }
                                // 插入最新的帮助
                                iLoadingModel.addHelpInfos2Realm(rs);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case GEZI_TAG://geziList
                        try {
                            // 如果成功
                            if (jsonResult != null && jsonResult.getString("error_code").equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                // 清空本地
                                Gson gson = new Gson();
                                Type type = new TypeToken<ArrayList<BindGeZi>>() {
                                }.getType();
                                List<BindGeZi> rs = gson.fromJson(jsonResult.getString("machineList"), type);
                                for (BindGeZi bindGeZi : rs) {
                                    if (bindGeZi != null) {
                                        if (bindGeZi.getMachineSn().startsWith("22")) {
                                            rs.remove(bindGeZi);
                                        } else {
                                            bindGeZi.setCreateTime(new Date().getTime() + "");
                                        }
                                    }
                                }
                                if (rs.size() > 0) {
                                    // obtain the results of a query
                                    iLoadingModel.addBindGeZi2Realm(rs);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case MQIP_TAG:
                        try {
                            if (jsonResult != null && jsonResult.getString(SysConfig.JSON_KEY_ERROR_CODE).equals(SysConfig.ERROR_CODE_SUCCESS)) {
                                JSONObject jsonObject = new JSONObject(jsonResult.getString(SysConfig.JSON_KEY_MACHINEINFOFOEMQ));
                                MyApplication.getInstance().setUsedStatus(jsonObject.getString(SysConfig.JSON_KEY_USEDSTATUS));
                                MyApplication.getInstance().setMqIP(jsonObject.getString(SysConfig.JSON_KEY_MQIP));
                                MyApplication.getInstance().setAutomaticRefundState(jsonObject.getString(SysConfig.JSON_KEY_REFUNDSTATE));
                                SharedPreferencesUtils.setParam((Context) iLoadingInterface, SysConfig.JSON_KEY_MQIP, jsonObject.getString(SysConfig.JSON_KEY_MQIP));
                                SharedPreferencesUtils.setParam((Context) iLoadingInterface, SysConfig.JSON_KEY_USEDSTATUS, jsonObject.getString(SysConfig.JSON_KEY_USEDSTATUS));

                                //取出服务器定义的支付方式，下载图片
//                                if (jsonObject.has(SysConfig.AUTO_PAY_PICTURE)){
//                                    parsePayMethod(jsonObject.getString(SysConfig.AUTO_PAY_PICTURE),jsonObject.getString(SysConfig.PAY_TYPE));
//                                }

                                Intent intent = new Intent();
                                intent.setClass((Context) iLoadingInterface, HexinService.class);
                                MyApplication.getInstance().startService(intent);
                            } else {
                                String mqIp = SharedPreferencesUtils.getParam((Context) iLoadingInterface, SysConfig.JSON_KEY_MQIP, "").toString();
                                if (!mqIp.equals("")) {
                                    MyApplication.getInstance().setUsedStatus(SharedPreferencesUtils.getParam((Context) iLoadingInterface, SysConfig.JSON_KEY_USEDSTATUS, "").toString());
                                    MyApplication.getInstance().setMqIP(mqIp);
                                    MyApplication.getInstance().sendBroadcast(new Intent(SysConfig.RECEIVER_ACTION_DEAMON));
                                }
                                String requestinterface = "api/machine/" + MyApplication.getInstance().getMachine_sn() + "/mqserverip?simCode=" + MyApplication.getInstance().getMachine_ccid();
                                DataUtil.addBackGroundRequest(requestinterface, "", Constant.BACKGROUND_WHAT_MQ, true);
                            }
                            MyApplication.getInstance().getLogInit().d("初始化 MQIP = "+MyApplication.getInstance().getMqIP());
                            MyApplication.getInstance().getLogInit().d("自动退款状态 = "+MyApplication.getInstance().getAutomaticRefundState());
                            MyApplication.getInstance().getLogInit().d("认证状态(0:未认证) = "+MyApplication.getInstance().getUsedStatus());
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
    public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {

    }

    @Override
    protected void bindView(ILoadingInterface IView) {
        this.iLoadingInterface = IView;
    }

    @Override
    public void cancel() {
        if (iNetWorkRequInterface != null) {
            iNetWorkRequInterface.cancel();
            iNetWorkRequInterface = null;
        }
        if (iLoadingModel != null) {
            iLoadingModel.cancel();
            iLoadingModel = null;
        }
    }

    @Override
    public void getServiceMQIP() {
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/" + MyApplication.getInstance().getMachine_sn() + "/mqserverip?simCode=" + MyApplication.getInstance().getMachine_ccid();
        if (iNetWorkRequInterface == null) {
            iNetWorkRequInterface = new NetWorkRequImpl(this);
        }
        iNetWorkRequInterface.request(url, MQIP_TAG, RequestMethod.GET);
    }

    @Override
    public void getBindGeziInfo() {
        if (iLoadingModel.isBindGeZiInfo()) {
            return;
        }
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/machine/" + MyApplication.getInstance().getMachine_sn() + "/geziguilist";
        if (iNetWorkRequInterface == null) {
            iNetWorkRequInterface = new NetWorkRequImpl(this);
        }
        iNetWorkRequInterface.request(url, GEZI_TAG, RequestMethod.GET);
    }

    @Override
    public void updataHelpInfo() {
        String url = SysConfig.NET_SERVER_HOST_ADDRESS + "api/help/helplist";
        if (iNetWorkRequInterface == null) {
            iNetWorkRequInterface = new NetWorkRequImpl(this);
        }
        iNetWorkRequInterface.request(url, HELP_TAG, RequestMethod.GET);
    }

    @Override
    public void updateLocalKucun(String road_sn) {
        iLoadingModel.updateLocalKucun(road_sn);
    }

    @Override
    public void AfterMachineInfoGetOver() {
//        L.v(SysConfig.ZPush, "AfterMachineInfoGetOver....");
        if ("".equals(MyApplication.getInstance().getMachine_sn())) {
            MyApplication.getInstance().getMachineSn();
        }
        // 判断机器编号是否存在
        if ("".equals(MyApplication.getInstance().getMachine_sn())) {
            iLoadingInterface.showAlertView(SysConfig.NO_MACHINE_SN);
            return;
        }
        if (0 == MyApplication.getInstance().getRoadCount() || "".equals(MyApplication.getInstance().getMachineType())) {
            iLoadingInterface.showAlertView(SysConfig.MACHINE_CONNECT_ERROR);
            return;
        }
        // 判断机器是否正常
        if (!"".equals(MyApplication.getInstance().getSystemStatus())) {
            String[] status = MyApplication.getInstance().getSystemStatus().split(",");
            if ("0".equals(status[0])) {
                // 停止销售
                String rst = ((LoadingActivity) iLoadingInterface).machineStartSale();
                if (!"".equals(rst)) {
                    Toast.makeText((Context) iLoadingInterface, "主机停止销售", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            if (!"1".equals(status[1]) && !"2".equals(status[1]) && !"4".equals(status[1])) {
                // 售货机销售暂停状态
                String rst = ((LoadingActivity) iLoadingInterface).machineStartSale();
                if (!"".equals(rst)) {
                    Toast.makeText((Context) iLoadingInterface, "主机暂停销售", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        } else {
            ((LoadingActivity) iLoadingInterface).getMachineData();
            return;
        }
        MyApplication.getInstance().setAdFileList(new ArrayList<AdInfo>());
        // 取得本地的广告信息
        String path = MyApplication.getInstance().getSdCardPath() + SysConfig.SD_CARD_PATH_AD;
//        Log.i(TAG, path + "  ");
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        List<File> fileList = sortFileByName(path);
        if (fileList != null && fileList.size() > 0) {
            for (File adFile : fileList) {
                if (adFile == null || adFile.length() == 0 || adFile.getName().startsWith("._")) {
                    continue;
                }
//                Log.i(TAG, adFile.getAbsolutePath());
                String filePath = adFile.getAbsolutePath();
                AdInfo adInfo = new AdInfo();
                adInfo.setAdPath(filePath);
                if (filePath.toLowerCase().endsWith("mp4") || filePath.toLowerCase().endsWith("flv")) {
                    adInfo.setAdType("2");
                } else if (filePath.toLowerCase().endsWith("jpg") || filePath.toLowerCase().endsWith("jpeg") || filePath.toLowerCase().endsWith("png")) {
                    long s = 0;
                    if (adFile.exists()) {
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(adFile);
                            s = fis.available();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                fis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (s / 1048576 > 10) { // 图片大于10 跳过
                        continue;
                    }
                    adInfo.setAdType("1");
                } else {
                    continue;
                }
                MyApplication.getInstance().getAdFileList().add(adInfo);
            }
        }
        MyApplication.getInstance().getLogInit().d("初始化 本地广告数量 = "+MyApplication.getInstance().getAdFileList().size());
        MyApplication.getInstance().setCabinetGoods(new ArrayList<GoodsInfo>());
        MyApplication.getInstance().setCabinetTotalGoods(new ArrayList<GoodsInfo>());
        MyApplication.getInstance().setGoodsInfos(new ArrayList<GoodsInfo>());
        MyApplication.getInstance().setBindGeZis(new ArrayList<BindGeZi>());
        MyApplication.getInstance().setBindDeskList(new ArrayList<BindDesk>());
        MyApplication.getInstance().setDeskGoodsInfo(new HashMap<Integer, GoodsInfo>());
        // 加载本地数据库中的商品信息
        iLoadingModel.getGoods4Realm();
        // 处理商品的缺货信息
        ((LoadingActivity) iLoadingInterface).handleGoodsKuCun();
        // 取得格子柜信息
        iLoadingModel.getGeZiInfo();
        // 取得副柜信息
        iLoadingModel.getDeskInfo();
        iLoadingModel.getDeskGoods4Realm();
    }

    //按照文件名称排序
    private List<File> sortFileByName(String fliePath) {
        List<File> files = Arrays.asList(new File(fliePath).listFiles());
        try {
            Collections.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.isDirectory() && o2.isFile())
                        return -1;
                    if (o1.isFile() && o2.isDirectory())
                        return 1;
                    return o1.getName().compareTo(o2.getName());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }

    /**
     * 初始化支持的网络支付方式
     * @param allMethodJson 所有的支付方式
     * @param enabledMethodStr 支持的支付方式
     */
    private void parsePayMethod(String allMethodJson,String enabledMethodStr){
        List<PayMethod> enabledPayMethods = new ArrayList<>();
        Gson gson = new Gson();
        Type type = new TypeToken<List<PayMethod>>(){}.getType();
        List<PayMethod> allPayMethods = gson.fromJson(allMethodJson,type);
        MyApplication.getInstance().getLogInit().d("所有的网络支付方式 : "+allPayMethods);
        //下载所有的支付方式图片，目录是/sdcard/zongs/pay_icon
        String dir = MyApplication.getInstance().getSdCardPath()+"/zongs/pay_icon/";
        for (PayMethod payMethod : allPayMethods){
            String fileName = FileUtils.getPayIconFileName(payMethod.getId());
            //这里配置了默认不覆盖已有的文件
            iNetWorkRequInterface.downLoadRequest(payMethod.getPicUrl(),Integer.parseInt(payMethod.getId()),dir,fileName,true,false,new DownLoadListener());
        }
        //过滤支持的支付方式
        String[] enabledMethodID = enabledMethodStr.split(",");
        for (String idStr : enabledMethodID){
            for (PayMethod payMethod : allPayMethods){
                if (payMethod.getId().equals(idStr)){
                    //说明支持此支付方式，把它添加到支持列表中
                    enabledPayMethods.add(payMethod);
                }
            }
        }
        MyApplication.getInstance().getLogInit().d("支持的网络支付方式 : "+enabledPayMethods);
        MyApplication.getInstance().setEnabledPayMethod(enabledPayMethods);
    }

    private class DownLoadListener implements DownloadListener{
        @Override
        public void onDownloadError(int what, Exception exception) {
            logUtil.d("下载支付方式图标失败 id = "+what);
        }

        @Override
        public void onStart(int what, boolean isResume, long rangeSize, Headers responseHeaders, long allCount) {
            logUtil.d("开始下载支付方式图标 id = "+what);

        }

        @Override
        public void onProgress(int what, int progress, long fileCount) {

        }

        @Override
        public void onFinish(int what, String filePath) {
            logUtil.d("下载支付方式图标成功 id = "+what+" 路径为 : "+filePath);
        }

        @Override
        public void onCancel(int what) {

        }
    }
}
