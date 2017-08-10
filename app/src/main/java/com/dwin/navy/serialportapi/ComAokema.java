package com.dwin.navy.serialportapi;

import android.os.SystemClock;
import android.util.Log;

import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.background.bean.BaseInfo;
import com.zongsheng.drink.h17.common.L;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.observable.SerialObservable;
import com.zongsheng.drink.h17.util.FileUtils;
import com.zongsheng.drink.h17.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by chensean on 16/9/15.
 */
public class ComAokema {

    //打印实际通信指令，使用Monitor的filter方便追踪
    private LogUtil logBasicCom;

    private static final String TAG = "ComAokema";

    private static final SimpleDateFormat formatHMS = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    private SerialPortOpt serialPort;

    private boolean isReadThreadRunning = false;

    private boolean isCheckThreadRunning = false;

    // 判断是否连接上
    public boolean isConnected = true;
    private long getVSITime = 0l;

    // 需要传给VSI的指令信息
    private byte[] toVMCPara = null;

    // 其他指令集合信息
    private List<byte[]> otherToVMCParaList = new ArrayList<>();

    // 货道数量
    private int trackNum = 21;

    public void setRoadCount(int roadCount) {
        this.trackNum = roadCount;
    }

    private static ComAokema instance = null;

    public static ComAokema getInstance2() {
        return instance;
    }


    public static ComAokema getInstance() {
        if (instance == null) {
            synchronized (ComAokema.class) {
                if (instance == null) {
                    instance = new ComAokema();
                }
            }
        }
        return instance;
    }

    private ComAokema() {

        logBasicCom = MyApplication.getInstance().getLogBasicCom();

        //(1) 串口定义
        serialPort = new SerialPortOpt();
        serialPort.mDevNum = 0;   //串口序号  ttyO2
        serialPort.mSpeed = 9600;//波特率
        serialPort.mDataBits = 8;//数据位
        serialPort.mStopBits = 1;//停止位
        serialPort.mParity = 'n';//校验位

    }

    public void openSerialPort() {
        // (2) 打开串口
        if (serialPort.mFd == null || !isReadThreadRunning) {
            serialPort.openDev(serialPort.mDevNum);
            logBasicCom.d("打开串口-------------------------");

            serialPort.setSpeed(serialPort.mFd, serialPort.mSpeed);
            serialPort.setParity(serialPort.mFd, serialPort.mDataBits, serialPort.mStopBits, serialPort.mParity);

            isReadThreadRunning = true;
            ReadThread mReadThread = new ReadThread();
            mReadThread.setName("VSI的串口线程：" + formatHMS.format(new Date(System.currentTimeMillis())));
            mReadThread.start();

            isCheckThreadRunning = true;
            CheckisConnect mCheckisConnect = new CheckisConnect();
            mCheckisConnect.setName("checkis的串口线程：" + formatHMS.format(new Date(System.currentTimeMillis())));
            mCheckisConnect.start();

            L.v(SysConfig.MT, "创建线程打开串口。。。" + mReadThread.hashCode() + "    " + mCheckisConnect.hashCode());
        }
    }

    public void closeSerialPort() {

//        if (mReadThread != null) {
//            isReadThreadRunning = false;
//            SystemClock.sleep(50);    //暂停0.05秒保证mReadThread线程结束
//        }
//
//        if (mCheckisConnect != null) {
//            isCheckThreadRunning = false;
//            SystemClock.sleep(50);    //暂停0.05秒保证mCheckisConnect线程结束
//        }
//
//        if (serialPort.mFd != null) {
//            serialPort.closeDev(serialPort.mFd);
//        }
    }

    private class CheckisConnect extends Thread {
        @Override
        public void run() {
            super.run();
            while (isCheckThreadRunning) {
                if (System.currentTimeMillis() - getVSITime > 10000) {  // 10秒没有数据,那就认为失联
                    // 说明失联了
                    if (isConnected) {
                        L.i(TAG, "1000");
                        isConnected = false;
                        returnConsumeInfo("1000", "");  // 表示失联
                    }
                }
                // 每10个毫秒去读取数据
                SystemClock.sleep(10);
            }
        }
    }

    private class ReadThread extends Thread {
        byte[] buf = new byte[512];
        byte[] rxByteArrayTemp = null;// 临时变量：接收到的字节信息

        @Override
        public void run() {
            super.run();
            while (isReadThreadRunning) {
                int size;
                if (buf == null) {
                    //线程被中止了
                    return;
                } else {
                    size = serialPort.readBytes(buf);
                }
                if (size > 0) {
                    isConnected = true;
                    // 保存收到记录的时间
                    getVSITime = System.currentTimeMillis();
                    // 发现有信息后就追加到临时变量
                    rxByteArrayTemp = ArrayAppend(rxByteArrayTemp, buf, size);
                } else {
                    // 这次发现没有信息，如果以前有信息的，那就是我们要的数据
                    if (rxByteArrayTemp != null) {
                        byte[] rxByteArray = ArrayAppend(rxByteArrayTemp, null);
                        isConnected = true;
                        analyseData(rxByteArray);
                        rxByteArrayTemp = null;
                    }
                }
                // 每10个毫秒去读取数据
                SystemClock.sleep(10);
            }
        }
    }

    // ---------- 以下为数据处理部分 ---------

    private final byte VMC_HEAD_ONE = (byte) 0xFE;
    private final byte VMC_HEAD_TWO = (byte) 0x55;
    private final byte VMC_HEAD_THREE = (byte) 0xEF;
    private final byte PC_HEAD_ONE = (byte) 0xEF;
    private final byte PC_HEAD_TWO = (byte) 0x55;
    private final byte PC_HEAD_THREE = (byte) 0xFE;

    /**
     * 签到、设备联线 0x78
     */
    private final byte CMD_CONNECT = (byte) 0x78;
    /**
     * 系统故障状态 0x79
     */
    private final byte CMD_DEVICE_ERR_STATUS = (byte) 0x79;
    /**
     * 料道故障状态 0x7A 仅对弹簧机有效
     */
    private final byte CMD_CHANNEL_ERR_STATUS = (byte) 0x7A;
    /**
     * 料道有无货信息 0x7B
     */
    private final byte CMD_CHANNEL_THINGS_INFO = (byte) 0x7B;
    /**
     * 料道设置状态 0x73
     */
    private final byte CMD_CHANNEL_SET_STATUS = (byte) 0x73;
    /**
     * 设备运行信息 0x7d
     */
    private final byte CMD_DEVICE_RUN_INFO = (byte) 0x7D;
    /**
     * 轮询指令 0x76
     */
    private final byte CMD_LOOP = (byte) 0x76;
    /**
     * VMC将接收的金额数据通知PC 0x77
     */
    private final byte CMD_BUY_INFO = (byte) 0x77;
    /**
     * 出货信息 0x7C
     */
    private final byte CMD_OUT_THINGS = (byte) 0x7C;
    /**
     * 售卖交易记录信息 0x74
     */
    private final byte CMD_STATISTICS_INFO = (byte) 0x74;

    /* 对收到的数据进行分包截取 */
    /*private void analyseProtocoleSingle(byte[] buf) {
            analyseData(buf);
    }*/

    /**
     * 解析数据
     */
    private void analyseData(byte[] buf) {

        if (buf.length == 0 || buf.length < 5)
            return;
        if (buf[3] > (buf.length - 3))
            return;
        if (!((buf[0] == VMC_HEAD_ONE) && (buf[1] == VMC_HEAD_TWO) && (buf[2] == VMC_HEAD_THREE)))
            return;
//        L.dHex(TAG, "接收>>>       ", buf);
//        logBasicCom.d("接收 "+bytesToHexString(buf,buf.length));
        //}
        // 校验和
        byte sum = 0;
        for (int i = 3; i < (buf[3] + 3); i++) {
            sum += buf[i];
        }
        if (buf[3] + 3 >= buf.length || !(sum == buf[buf[3] + 3]))
            return;
        switch (buf[4]) {
            case CMD_CONNECT:// 签到、设备联线
                logBasicCom.d("接收 签到 = "+bytesToHexString(buf,buf.length));
                if (!isConnected) {
                    isConnected = true;
                    // 连接上了
                    returnConsumeInfo("1001", "");
                }
                responseForVMC(CMD_CONNECT);
                analyseConnect(buf);
//                FileUtils.writeStringToFile(bytesToHexString(buf, buf.length));
                break;
            case CMD_CHANNEL_SET_STATUS:// 料道设置状态
                logBasicCom.d("接收 0x73 机器料道有效状态 = "+bytesToHexString(buf,buf.length));
                responseForVMC(CMD_CHANNEL_SET_STATUS);
                analyseChannelSetStatus(buf);
                break;
            case CMD_DEVICE_RUN_INFO:// 设备运行状态信息
                logBasicCom.d("接收 0x7d 设备运行状态信息 = "+bytesToHexString(buf,buf.length));
                responseForVMC(CMD_DEVICE_RUN_INFO);
                analyseDevRunStatus(buf);
//                FileUtils.writeStringToFile(bytesToHexString(buf, buf.length));
                break;
            case CMD_DEVICE_ERR_STATUS:// 系统故障状态
                logBasicCom.d("接收 0x79 系统故障状态 = "+bytesToHexString(buf,buf.length));
                responseForVMC(CMD_DEVICE_ERR_STATUS);
                analyseSystemErr(buf);
//                FileUtils.writeStringToFile(bytesToHexString(buf, buf.length));
                break;
            case CMD_CHANNEL_ERR_STATUS:// 料道故障状态，只对弹簧机有效
                logBasicCom.d("接收 0x7a 料道故障状态 = "+bytesToHexString(buf,buf.length));
                responseForVMC(CMD_CHANNEL_ERR_STATUS);
                //analyseChannelErrStatus(buf); 没有弹簧综合机,不处理
//                FileUtils.writeStringToFile(bytesToHexString(buf, buf.length));
                break;
            case CMD_CHANNEL_THINGS_INFO:// 料道有无货信息
                logBasicCom.d("接收 0x7b 主机和格子柜的各料道有无货信息 = "+bytesToHexString(buf,buf.length));
                responseForVMC(CMD_CHANNEL_THINGS_INFO);
                analyseChannelHaveThings(buf);
//                FileUtils.writeStringToFile(bytesToHexString(buf, buf.length));
                break;
            case CMD_LOOP:// 轮询
                logBasicCom.d("接收 0x76 轮询 = "+bytesToHexString(buf,buf.length));
//                MyApplication.getInstance().getLogBuyAndShip().d("轮询 = "+bytesToHexString(buf,buf.length));
                analyseLoopData(buf);
                break;
            case CMD_OUT_THINGS:// 出货信息
                logBasicCom.d("接收 0x7c 出货信息 = "+bytesToHexString(buf,buf.length));
                responseForVMC(CMD_OUT_THINGS);
                analyseOutThingsInfo(buf);
//                FileUtils.writeStringToFile(bytesToHexString(buf, buf.length));
                break;
            case CMD_BUY_INFO:// 购买信息
                logBasicCom.d("接收 0x77 购买信息 = "+bytesToHexString(buf,buf.length));
                responseForVMC(CMD_BUY_INFO);
                analyseBuyInfo(buf);
//                FileUtils.writeStringToFile(bytesToHexString(buf, buf.length));
                break;
            case CMD_STATISTICS_INFO:// 统计信息
                logBasicCom.d("接收 0x74 统计信息 = "+bytesToHexString(buf,buf.length));
                responseForVMC(CMD_STATISTICS_INFO);
                //analyseStatisticsInfo(buf);
//                FileUtils.writeStringToFile(bytesToHexString(buf, buf.length));
                break;
            default:
                logBasicCom.d("接收 无视的VMC指令 = "+buf[4]);
//                L.e(TAG, "无视的VMC指令:" + buf[4]);
//                FileUtils.writeStringToFile(bytesToHexString(buf, buf.length));
                break;
        }
    }


    /**
     * 解析出货信息
     * fe 55 ef   2f 7c   0  c   0 0 0 32   22 82   0 0 0 0 0 0 0 0 0 0   1   0 13 10 72   0 0   0   0 0 0 0   14 0 9 c f 2d 31   0 0   0 0 0 0 0 0   b9
     */
    private void analyseOutThingsInfo(byte[] buf) {
        // 机器类型
        int boxIndex = buf[5];
        // 售卖料道编号
        int mSellChannelNum = buf[6];
        // 售卖金额
        int mSellMoney = (buf[7] & 0xFF) * 256 * 256 * 256
                + (buf[8] & 0xFF) * 256 * 256 + (buf[9] & 0xFF) * 256
                + (buf[10] & 0xFF);
        // 出货序列号
        int mOutThingsSerialNumber = (buf[11] & 0xFF) * 256 + (buf[12] & 0xFF);
        long cn = 0;
        for (int i = 0; i < 10; i++) {
            cn += (buf[22 - i] & 0xFF) * (10 ^ i);
        }
        // 卡号
        long mCardNumber = cn;
        // 支付方式
        int mPayType = buf[23];
        // 商品编号
        int mProductNumber = (buf[24] & 0xFF) * 1000 + (buf[25] & 0xFF) * 100 + (buf[26] & 0xFF) * 10 + (buf[27] & 0xFF);
        // 售货机设备编号
        int mDevNumber = (buf[28] & 0xFF) * 10 + (buf[29] & 0xFF);
        // 故障代码
        int mErrCode = buf[30];
        // 卡剩余金额
        int mRemainMoney = (buf[31] & 0xFF) * 1000 + (buf[32] & 0xFF) * 100 + (buf[33] & 0xFF) * 10 + (buf[34] & 0xFF);
        // 交易时间
        String mDealTime = "" + buf[35]
                + (buf[36] > 9 ? buf[36] : ("0" + buf[36])) + "-" + buf[37]
                + "-" + buf[38] + " " + buf[39] + ":" + buf[40] + " " + buf[41]
                + "'";
        // 控制序列号
        int mControllSerialNumber = (buf[42] & 0xFF) * 256 + (buf[43] & 0xFF);

        String ss = "" + mSellChannelNum + "," + mSellMoney + "," + mOutThingsSerialNumber
                + "," + mCardNumber + "," + mPayType + "," + mProductNumber + "," + mDevNumber
                + "," + mErrCode + "," + mRemainMoney + "," + mDealTime + "," + mControllSerialNumber + "," + boxIndex;
        Log.e("007c", ss);

        returnConsumeInfo("007C", "" + mSellChannelNum + "," + mSellMoney + "," + mOutThingsSerialNumber
                + "," + mCardNumber + "," + mPayType + "," + mProductNumber + "," + mDevNumber
                + "," + mErrCode + "," + mRemainMoney + "," + mDealTime + "," + mControllSerialNumber + "," + boxIndex);
    }


    /**
     * 解析购买信息
     */
    private void analyseBuyInfo(byte[] buf) {
        // 持币或刷卡金额
        int mPayMoney = (buf[7] & 0xFF) * 256 * 256 * 256
                + (buf[8] & 0xFF) * 256 * 256 + (buf[9] & 0xFF) * 256
                + (buf[10] & 0xFF);
        // 支付方式
        int mPayType = buf[11];
        if (buf[6] == 0) {// 料道为0时
            // 运行状态
            int mRunStatus = buf[13];

            returnConsumeInfo("007700", "" + mPayMoney + "," + mPayType + "," + mRunStatus);
        } else {
            String stockhave = "";
            int k = 1;
            switch (buf[12]) {
                case 0:// 饮料机
                    for (int i = 0; i < 10; i++) {
                        for (int j = 0; j < 8; j++) {
                            stockhave = stockhave + ((buf[6 + i] >> j) & 0x01) + ",";
                            k++;
                            if (k >= trackNum) {
                                j = 8;
                                i = 10;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
            if (stockhave.length() == 0) stockhave = stockhave.substring(0, stockhave.length() - 1);
            returnConsumeInfo("007701", "" + mPayMoney + "," + mPayType + "," + stockhave);
        }
    }

    private byte[] lastVMCPara = null;
    private int nError0xFF = 0;

    /**
     * 解析对方发来的轮询数据
     * 当VMC状态变化时，会携带一些状态信息，比如用户投币
     */
    private void analyseLoopData(byte[] buf) {
        String result = "";
        switch (buf[3]) {
            case 17:
                // Y5 状态:7—轮询,1—正在出货,2—失败,4—成功
                int mLoopType = buf[5];
                // Y6 按动按键的箱号
                int mPressDevNum = buf[6];
                // Y7 按动按键的料道
                int mPressChannelNum = buf[7];
                // Y8 Y9按动按键的价格（分）
                int mPressPrice = ((buf[8] & 0xFF) * 256 + (buf[9] & 0xFF));
                // Y12 Y13 Y14 投币总金额（分）
                int mPushMoney = ((buf[12] & 0xFF) * 256 * 256
                        + (buf[13] & 0xFF) * 256 + (buf[14] & 0xFF));
                // Y15 Y16 投硬币金额（分）
                //loopBean.mPushCoinMoney = ((buf[15] & 0xFF) * 256 + (buf[16] & 0xFF));
                // Y17 Y18 投纸币金额（分）
                //loopBean.mPushPaperMoney = ((buf[17] & 0xFF) * 256 + (buf[18] & 0xFF));
                // 能否营业
                int mCanSale = 1;
                if (((buf[19] >> 0) & 0x01) == 1)
                    mCanSale = 0;
                // 门的开关
                int mDoorOpenIs = 0;
                if (((buf[19] >> 2) & 0x01) == 1)
                    mDoorOpenIs = 1;

                lastVMCPara = loopDataSend();
                //TODO:这里接收并处理用户按键购买
                returnConsumeInfo("007617", "" + mCanSale + "," + mDoorOpenIs
                        + "," + mPressDevNum + "," + mPressChannelNum + "," + mPressPrice
                        + "," + mPushMoney + "," + mLoopType);

                if (mLoopType != 7) {
//                    FileUtils.writeStringToFile(bytesToHexString(buf, buf.length));
                }
                break;
            case 3:
                switch (buf[5]) {
                    case 0x00:
                        nError0xFF = 0;
                        result = "00成功";
                        break;
                    case 0x01:
                        result = "01忙";
                        break;
                    case (byte) 0xFF:
                        if (isOpenDoorPara(lastVMCPara) && nError0xFF < SysConfig.TRY_OPEN_DOOR_TIME) {
                            otherToVMCParaList.add(0, lastVMCPara);
                            nError0xFF++;
                        } else {
                            nError0xFF = 0;
                        }
                        result = "FF失败";
                        break;
                    case (byte) 0xF1:
                        result = "F1纸币压仓失败";
                        break;
                    case (byte) 0xF2:
                        result = "F2钱不对（应为5角的倍数/钱不够/找零不够）";
                        break;
                    case (byte) 0xF3:
                        result = "F3不能卖/没货/没设料道/料道故障";
                        break;
                    case (byte) 0xF4:
                        result = "F4料道错误、开门、停止售卖";
                        break;
                    case (byte) 0xF5:
                        result = "F5交易序列号相同";
                        break;
                    case (byte) 0xF6:
                        result = "F6读卡器没准备好";
                        break;
                    case (byte) 0xF7:
                        result = "F7读卡器不能卖（钱不够、料道错）";
                        break;
                    case (byte) 0xF8:
                        result = "F8无法启动读卡器（开门、料道不对或无货、售卖价格为零）";
                        break;
                    case (byte) 0xF9:
                        result = "F9扣款失败";
                        break;
                    case (byte) 0xFA:
                        result = "FA超时，出错";
                        break;
                    case (byte) 0xFB:
                        result = "FB取消（按银联POS”取消“键）";
                        break;
                    case (byte) 0xFC:
                        result = "FC支付方式不对";
                        break;
                    case (byte) 0xFE:
                        result = "FE校验错误";
                        break;
                }
//                L.v(TAG, "------------> " + result);
                returnConsumeInfo("007603", result);
                //一键开门开始计数,到50停止
                if (MyApplication.getInstance().isCount()) {
                    MyApplication.getInstance().count++;
                }
//                FileUtils.writeStringToFile(bytesToHexString(buf, buf.length));
                break;
        }
    }

    /**
     * 轮询时将指定数据包发送给VMC
     */
    private byte[] loopDataSend() {
        if (toVMCPara != null) {
            serialPort.writeBytes(toVMCPara);
            logBasicCom.d("发送 "+bytesToHexString(toVMCPara,toVMCPara.length));
//            L.d(TAG, "发送<<<	       " + bytesToHexString(toVMCPara, toVMCPara.length));
//            FileUtils.writeStringToFile(bytesToHexString(toVMCPara, toVMCPara.length));
            toVMCPara = null;
            return null;
        } else {
            // 如果有其他需要发的指令
            byte[] otherPara = null;
            if (otherToVMCParaList.size() > 0) {
                otherPara = otherToVMCParaList.get(0);
                otherToVMCParaList.remove(0);
            }
            if (otherPara != null && otherPara.length > 1) {
                serialPort.writeBytes(otherPara);
//                L.d(TAG, "发送<<<	       " + bytesToHexString(otherPara, otherPara.length));
                logBasicCom.d("发送 "+bytesToHexString(otherPara,otherPara.length));
//                FileUtils.writeStringToFile(bytesToHexString(otherPara, otherPara.length));
                return otherPara;
            }
            byte[] lp = new byte[10];
            lp[0] = PC_HEAD_ONE;
            lp[1] = PC_HEAD_TWO;
            lp[2] = PC_HEAD_THREE;
            lp[3] = 0x06;// 数据长度
            lp[5] = 0x00;
            lp[6] = 0x00;
            lp[7] = 0x00;
            lp[8] = 0x00;
            lp[9] = getCountCheck(lp, 3, 9);
//            L.d(TAG, "发送<<<	       " + bytesToHexString(lp, lp.length));
            logBasicCom.d("发送 "+bytesToHexString(lp,lp.length));
            serialPort.writeBytes(lp);
            return lp;
        }
    }

    /**
     * 解析设备运行状态信息
     */
    private void analyseDevRunStatus(byte[] buf) {

        // 被选中按钮的商品价格（单位：分）Y5-Y8 之和
        int mPressPrice = (buf[5] & 0xFF) * 256 * 256 * 256
                + (buf[6] & 0xFF) * 256 * 256 + (buf[7] & 0xFF) * 256
                + (buf[8] & 0xFF);
        // 剩余N元硬币数量 Y13 Y14 Y15 无用
        // 剩余1元硬币数量
        int mRemainOneYuanCoinNum = buf[16];
        // 剩余5角硬币数量
        int mRemainFiveJiaoCoinNum = buf[17];
        // 被按动的按键值对应料道号
        int mPressChannelNum = buf[18];
        // 运行状态，是否暂停营业
        byte runStatus = buf[19];
        byte sale = (byte) ((runStatus >> 0) & 0x01);
        //营业状态 0：暂停营业 1：正常营业
        int mCanSale = 1;
        if (sale == 1) // 暂停营业
            mCanSale = 0;

        byte doorOpen = (byte) ((runStatus >> 2) & 0x01);
        //柜门开关状态 0：关闭状态 1：打开状态
        int mDoorOpen = 0;
        if (doorOpen == 1) // 门开着
            mDoorOpen = 1;

        // 主机料道数量 Y20
        // 附加箱设置 Y21 (食品、格子柜 1 2...6) 1有效 0无效
        String fujianguiInfo = "";
        for (int j = 0; j < 7; j++) {
            fujianguiInfo = fujianguiInfo + ((buf[21] >> j) & 0x01) + "|";
        }

//        L.e(TAG, "附加箱设置:" + fujianguiInfo);
        logBasicCom.d("---------------附加箱有效信息，箱号从1开始 = "+fujianguiInfo);

        // 售货机编号 Y22 Y23
        // 按键对应设备地址 Y24 获取箱号：0-饮料 1-食品 ，2...-格子柜
        //statusInfoBean.mPressDev = buf[24];
        // 被按动的按键值 Y25
        int keycode = buf[25];
//        L.e(TAG, "设备状态信息:" + Arrays.toString(buf) + "Y[24]:" + buf[24]);
        returnConsumeInfo("007D", "" + mCanSale + "," + mDoorOpen
                + "," + keycode + "," + mPressChannelNum + "," + mPressPrice
                + "," + mRemainOneYuanCoinNum + "," + mRemainFiveJiaoCoinNum + "," + fujianguiInfo);
    }

    /**
     * 解析料道配置信息
     */
    private void analyseChannelSetStatus(byte[] buf) {
//        L.e(TAG, "料道配置信息:" + buf[5]);
        if (0 == buf[5]) {// Y6是饮料机最大料道数
            trackNum = buf[6];
            // 附加箱设置 Y21 (食品、格子柜 1 2...6) 1有效 0无效
            String fujianguiInfo = "";
            for (int j = 0; j < 7; j++) {
                fujianguiInfo = fujianguiInfo + ((buf[7] >> j) & 0x01) + "|";
            }
//            L.e(TAG, "附加箱设置:" + fujianguiInfo);
            logBasicCom.d("---------------饮料机料道数 = "+trackNum+" ; 附加箱连接状态，箱号从1开始 = "+fujianguiInfo);
            returnConsumeInfo("0073", "" + trackNum + "," + fujianguiInfo);
        } else if (1 == buf[5]) {
            //弹簧机有效货到数
            int roadCount = 0;
            int stockHave;
            //弹簧机有效货道号列表
            String effectiveRoad = "";
            //48弹簧机 Y6-Y15   buf[3] == 0x0d 48个料道 读取6个字节即可
            //60弹簧机 Y6-Y23   buf[3] == 0x15 60个料道 读取10个字节即可
            //通过数据长度判断副柜类型
            //TODO:只考虑48弹簧机
            int floors = (buf[3] == 0x0d) ? 6 : 6;
            for (int i = 0; i < floors; i++) {
                for (int j = 0; j < 8; j++) {
                    stockHave = (buf[6 + i] >> j) & 0x01;
//                    L.e(TAG, "弹簧机料道 " + ((i + 1) * 10 + j + 1) + "是否有效:" + stockHave);
                    if (stockHave == 1) {
                        //货到有效
                        roadCount++;
                        effectiveRoad += ((i + 1) * 10 + j + 1) + ",";
                    }
                }
            }
//            L.e(TAG, "弹簧机货道:" + roadCount);
            if (effectiveRoad.endsWith(",")) {
                effectiveRoad = effectiveRoad.substring(0, effectiveRoad.length() - 1);
            }
            // 0080 弹簧机;有效货道编号 1,2,3,4……
            logBasicCom.d("---------------副柜弹簧机有效货道数 = "+roadCount+" ; 有效货道号 = "+effectiveRoad);
            returnConsumeInfo("0080", "1;" + roadCount + ";" + effectiveRoad);

        } else if (2 == buf[5]) { // 箱号为2的格子柜
            int roadCount = 0;
            int stockhave;
            String effectiveRoad = "";
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 8; j++) {
                    stockhave = ((buf[6 + i] >> j) & 0x01);
//                    L.e(TAG, "格子" + (i * 10 + j + 1) + "是否有效:" + stockhave);
                    if (stockhave == 1) {// 格子柜有设置
                        roadCount++;
                        effectiveRoad += (i * 10 + j + 1) + ",";
                    }
                }
            }
//            L.e(TAG, "格子柜格子数:" + roadCount);
            if (effectiveRoad.endsWith(",")) {
                effectiveRoad = effectiveRoad.substring(0, effectiveRoad.length() - 1);
            }
            // 0081 格子数;有效格子柜编号 1,2,3,4……
            logBasicCom.d("---------------箱号2格子柜有效货道数 = "+roadCount+" ; 有效货道号 = "+effectiveRoad);
            returnConsumeInfo("0081", "2;" + roadCount + ";" + effectiveRoad);
        } else if (3 == buf[5]) { // 格子柜
            int roadCount = 0;
            int stockhave;
            String effectiveRoad = "";
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 8; j++) {
                    stockhave = ((buf[6 + i] >> j) & 0x01);
//                    L.e(TAG, "格子" + (i * 10 + j + 1) + "是否有效:" + stockhave);
                    if (stockhave == 1) {// 格子柜有设置
                        roadCount++;
                        effectiveRoad += (i * 10 + j + 1) + ",";
                    }
                }
            }
//            L.e(TAG, "格子柜格子数:" + roadCount);
            if (effectiveRoad.endsWith(",")) {
                effectiveRoad = effectiveRoad.substring(0, effectiveRoad.length() - 1);
            }
            // 0081 格子数;有效格子柜编号 1,2,3,4……
            logBasicCom.d("---------------箱号3格子柜有效货道数 = "+roadCount+" ; 有效货道号 = "+effectiveRoad);
            returnConsumeInfo("0081", "3;" + roadCount + ";" + effectiveRoad);
        } else if (4 == buf[5]) { // 格子柜
            int roadCount = 0;
            int stockhave;
            String effectiveRoad = "";
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 8; j++) {
                    stockhave = ((buf[6 + i] >> j) & 0x01);
//                    L.e(TAG, "格子" + (i * 10 + j + 1) + "是否有效:" + stockhave);
                    if (stockhave == 1) {// 格子柜有设置
                        roadCount++;
                        effectiveRoad += (i * 10 + j + 1) + ",";
                    }
                }
            }
//            L.e(TAG, "格子柜格子数:" + roadCount);
            if (effectiveRoad.endsWith(",")) {
                effectiveRoad = effectiveRoad.substring(0, effectiveRoad.length() - 1);
            }
            // 0081 格子数;有效格子柜编号 1,2,3,4……
            logBasicCom.d("---------------箱号4格子柜有效货道数 = "+roadCount+" ; 有效货道号 = "+effectiveRoad);
            returnConsumeInfo("0081", "4;" + roadCount + ";" + effectiveRoad);
        } else if (5 == buf[5]) { // 格子柜
            int roadCount = 0;
            int stockhave;
            String effectiveRoad = "";
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 8; j++) {
                    stockhave = ((buf[6 + i] >> j) & 0x01);
//                    L.e(TAG, "格子" + (i * 10 + j + 1) + "是否有效:" + stockhave);
                    if (stockhave == 1) {// 格子柜有设置
                        roadCount++;
                        effectiveRoad += (i * 10 + j + 1) + ",";
                    }
                }
            }
//            L.e(TAG, "格子柜格子数:" + roadCount);
            if (effectiveRoad.endsWith(",")) {
                effectiveRoad = effectiveRoad.substring(0, effectiveRoad.length() - 1);
            }
            // 0081 格子数;有效格子柜编号 1,2,3,4……
            logBasicCom.d("---------------箱号5格子柜有效货道数 = "+roadCount+" ; 有效货道号 = "+effectiveRoad);
            returnConsumeInfo("0081", "5;" + roadCount + ";" + effectiveRoad);
        } else if (6 == buf[5]) { // 格子柜
            int roadCount = 0;
            int stockhave;
            String effectiveRoad = "";
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 8; j++) {
                    stockhave = ((buf[6 + i] >> j) & 0x01);
//                    L.e(TAG, "格子" + (i * 10 + j + 1) + "是否有效:" + stockhave);
                    if (stockhave == 1) {// 格子柜有设置
                        roadCount++;
                        effectiveRoad += (i * 10 + j + 1) + ",";
                    }
                }
            }
//            L.e(TAG, "格子柜格子数:" + roadCount);
            if (effectiveRoad.endsWith(",")) {
                effectiveRoad = effectiveRoad.substring(0, effectiveRoad.length() - 1);
            }
            // 0081 格子数;有效格子柜编号 1,2,3,4……
            logBasicCom.d("---------------箱号6格子柜有效货道数 = "+roadCount+" ; 有效货道号 = "+effectiveRoad);
            returnConsumeInfo("0081", "6;" + roadCount + ";" + effectiveRoad);
        } else if (7 == buf[5]) { // 格子柜
            int roadCount = 0;
            int stockhave;
            String effectiveRoad = "";
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 8; j++) {
                    stockhave = ((buf[6 + i] >> j) & 0x01);
//                    L.e(TAG, "格子" + (i * 10 + j + 1) + "是否有效:" + stockhave);
                    if (stockhave == 1) {// 格子柜有设置
                        roadCount++;
                        effectiveRoad += (i * 10 + j + 1) + ",";
                    }
                }
            }
//            L.e(TAG, "格子柜格子数:" + roadCount);
            if (effectiveRoad.endsWith(",")) {
                effectiveRoad = effectiveRoad.substring(0, effectiveRoad.length() - 1);
            }
            // 0081 格子数;有效格子柜编号 1,2,3,4……
            logBasicCom.d("---------------箱号7格子柜有效货道数 = "+roadCount+" ; 有效货道号 = "+effectiveRoad);
            returnConsumeInfo("0081", "7;" + roadCount + ";" + effectiveRoad);
        }
    }


    /**
     * 解析各料道是否有货
     */
    private void analyseChannelHaveThings(byte[] buf) {//fe 55 ef d 7b 0 f1 f9 1f 0 0 0 0 0 0 0 91
        //各料道是否有货，从第1料道开始，0表示缺货，1表示有货
        String stockhave = "";
        int k = 1;
//        L.e(TAG, "是否有货货道类型:" + buf[5] + "货道有货信息" + Arrays.toString(buf));
        switch (buf[5]) {
            case 0x00:// 主机
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 8; j++) {
                        stockhave = stockhave + ((buf[6 + i] >> j) & 0x01) + ",";

                        k++;
                        if (k > trackNum) {
                            j = 8;
                            i = 10;
                        }
                    }
                }
                if (stockhave.length() > 0) {
                    stockhave = stockhave.substring(0, stockhave.length() - 1);
                }
                logBasicCom.d("---------------主机料道是否有货（从1开始） = "+stockhave);
                returnConsumeInfo("007B", stockhave);
                break;
            //TODO:添加副柜缺货检查
//            case 0x01:
//                //只考虑48弹簧机，各料道中间可能有的料道无效
//                for (int i = 0; i<6; i++){
//                    for (int j= 0; j< 8; j++){
//                        stockhave = stockhave + ((buf[6 + i] >> j) & 0x01) + ",";
//                    }
//                }
//                logBasicCom.d("---------------箱号1副柜料道是否有货（从11开始） = "+stockhave);
//                returnConsumeInfo("007X","1;"+stockhave);
            case 0x02:// 格子柜
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 8; j++) {
                        stockhave = stockhave + ((buf[6 + i] >> j) & 0x01) + ",";
                        k++;
                    }
                }
                if (stockhave.length() > 0) {
                    stockhave = stockhave.substring(0, stockhave.length() - 1);
                }
                logBasicCom.d("---------------箱号2格子柜料道是否有货（从1开始） = "+stockhave);
                returnConsumeInfo("007X", "2;" + stockhave);
                break;
            case 0x03:// 格子柜
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 8; j++) {
                        stockhave = stockhave + ((buf[6 + i] >> j) & 0x01) + ",";
                        k++;
                    }
                }
                if (stockhave.length() > 0) {
                    stockhave = stockhave.substring(0, stockhave.length() - 1);
                }
                logBasicCom.d("---------------箱号3格子柜料道是否有货（从1开始） = "+stockhave);
                returnConsumeInfo("007X", "3;" + stockhave);
                break;
            case 0x04:// 格子柜
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 8; j++) {
                        stockhave = stockhave + ((buf[6 + i] >> j) & 0x01) + ",";
                        k++;
                    }
                }
                if (stockhave.length() > 0) {
                    stockhave = stockhave.substring(0, stockhave.length() - 1);
                }
                logBasicCom.d("---------------箱号4格子柜料道是否有货（从1开始） = "+stockhave);
                returnConsumeInfo("007X", "4;" + stockhave);
                break;
            case 0x05:// 格子柜
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 8; j++) {
                        stockhave = stockhave + ((buf[6 + i] >> j) & 0x01) + ",";
                        k++;
                    }
                }
                if (stockhave.length() > 0) {
                    stockhave = stockhave.substring(0, stockhave.length() - 1);
                }
                logBasicCom.d("---------------箱号5格子柜料道是否有货（从1开始） = "+stockhave);
                returnConsumeInfo("007X", "5;" + stockhave);
                break;
            case 0x06:// 格子柜
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 8; j++) {
                        stockhave = stockhave + ((buf[6 + i] >> j) & 0x01) + ",";
                        k++;
                    }
                }
                if (stockhave.length() > 0) {
                    stockhave = stockhave.substring(0, stockhave.length() - 1);
                }
                logBasicCom.d("---------------箱号6格子柜料道是否有货（从1开始） = "+stockhave);
                returnConsumeInfo("007X", "6;" + stockhave);
                break;
            case 0x07:// 格子柜
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 8; j++) {
                        stockhave = stockhave + ((buf[6 + i] >> j) & 0x01) + ",";
                        k++;
                    }
                }
                if (stockhave.length() > 0) {
                    stockhave = stockhave.substring(0, stockhave.length() - 1);
                }
                logBasicCom.d("---------------箱号7格子柜料道是否有货（从1开始） = "+stockhave);
                returnConsumeInfo("007X", "7;" + stockhave);
                break;
            default:
                // 弹簧综合机和格子柜不处理
                break;
        }

    }

    private boolean isOpenDoorPara(byte[] bytes) {
        return bytes[3] == 9 && bytes[4] == CMD_LOOP && bytes[5] == 0x07;
    }

    /**
     * 解析系统故障信息
     */
    private void analyseSystemErr(byte[] buf) {
        String errorstr = "";

        // 系统故障
        for (int i = 0; i < 8; i++) {
            if (i == 6) continue;
            if ((byte) ((buf[5] >> i) & 0x1) == (byte) 0x01) {
                errorstr = errorstr + "Y5-" + i + ",";
            }
        }

        // 纸币器故障
        for (int i = 0; i < 8; i++) {
            if ((byte) ((buf[6] >> i) & 0x1) == (byte) 0x01) {
                errorstr = errorstr + "Y6-" + i + ",";
            }
        }

        // 硬币器故障Y7
        for (int i = 0; i < 8; i++) {
            if ((byte) ((buf[7] >> i) & 0x1) == (byte) 0x01) {
                errorstr = errorstr + "Y7-" + i + ",";
            }
        }

        // 硬币器故障Y8
        for (int i = 0; i < 8; i++) {
            if (i == 4) continue;
            if (i == 5) continue;
            if ((byte) ((buf[8] >> i) & 0x1) == (byte) 0x01) {
                errorstr = errorstr + "Y8-" + i + ",";
            }
        }

        // 通讯故障
        for (int i = 0; i < 8; i++) {
            if ((byte) ((buf[9] >> i) & 0x1) == (byte) 0x01) {
                errorstr = errorstr + "Y9-" + i + ",";
            }
        }

        // 弹簧机故障
        for (int i = 0; i < 3; i++) {
            if ((byte) ((buf[10] >> i) & 0x1) == (byte) 0x01) {
                errorstr = errorstr + "Y10-" + i + ",";
            }
        }

        if (errorstr.length() > 0) errorstr = errorstr.substring(0, errorstr.length() - 1);

        returnConsumeInfo("0079", errorstr);
    }

    /**
     * 解析签到数据
     */
    private void analyseConnect(byte[] buf) {
        if (buf.length == 7) {

        } else {
            // 主控板版本号
            int mVMCmainVersion = (buf[5] & 0xFF) * 256 + (buf[6] & 0xFF);
            // 驱动板版本号
            int mQDversion = buf[7];
            // 售货机编号
            int mDeviceNum = (buf[8] & 0xFF) * 10 + (buf[9] & 0xFF);
            // 主机类型
            int mDeviceType = buf[10];
            // 协议方式
//        int mVMCprotocolType = buf[11];
            // Y12保留
            // Y13--Y20各附柜驱动板版本号
//            L.e(TAG, "签到结果:" + Arrays.toString(buf));
            logBasicCom.d("---------------签到信息 : "+"主控板本 = "+mVMCmainVersion+" ; 驱动版本号 = "+mQDversion+" ; 售货机编号 = "+mDeviceNum+" ; 主机类型 = "+mDeviceType);
            returnConsumeInfo("0078", "" + mVMCmainVersion + ","
                    + mQDversion + ","
                    + mDeviceNum + "," + mDeviceType);
        }
    }


    /**
     * 响应VMC
     */
    private void responseForVMC(byte cmd) {
        byte[] resp = new byte[7];
        resp[0] = PC_HEAD_ONE;
        resp[1] = PC_HEAD_TWO;
        resp[2] = PC_HEAD_THREE;
        resp[3] = 0x03;// 数据长度
        resp[4] = cmd;
        resp[5] = 0x00;
        resp[6] = getCountCheck(resp, 3, 6);
        //Contents.mSS.sendBuf(resp);
        logBasicCom.d("发送 "+bytesToHexString(resp,resp.length));
        serialPort.writeBytes(resp);
        L.d(TAG, "w<<<	       " + bytesToHexString(resp, resp.length));
//        FileUtils.writeStringToFile(bytesToHexString(resp, resp.length));
    }

    /**
     * 校验和
     */
    private byte getCountCheck(byte[] buf, int startIndex, int endIndex) {
        byte sum = 0;
        for (int i = startIndex; i < endIndex; i++) {
            sum += buf[i];
        }
        return sum;
    }

    public String deskChannelSet(int boxIndex, byte channelNo, int set) {
        byte cn;
        cn = (byte) Integer.parseInt(channelNo + "", 16);
        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[10];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 6;
            resp[4] = 0x76;
            resp[5] = 0x0A;
            resp[6] = (byte) boxIndex;
            resp[7] = cn;
            resp[8] = 1;

            resp[9] = getCountCheck(resp, 3, 8);
            toVMCPara = new byte[10];
            System.arraycopy(resp, 0, toVMCPara, 0, 10);
        }

        return null;
    }

    /**
     * 模块发来的料道测试请求 xuhao:箱号，channelNumber：料道值
     */
    public String channelTest(int xuhao, byte channelNumber) {

        //如果是格子柜 ：11号料道需要转换为0x11
        byte cn;
        if (xuhao > 1) {
            cn = (byte) Integer.parseInt(channelNumber + "", 16);
        } else {
            cn = channelNumber;
        }
        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[13];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 9;
            resp[4] = CMD_LOOP;
            resp[5] = 0x07;
            // 交易序列号
            resp[6] = 0;
            resp[7] = 0;
            // 箱号
            resp[8] = (byte) xuhao;
            // 料道值
            resp[9] = cn;
            resp[10] = 0x00;
            resp[11] = 0x00;
            resp[12] = getCountCheck(resp, 3, 12);

            byte[] newByte = new byte[13];
            System.arraycopy(resp, 0, newByte, 0, 13);
            otherToVMCParaList.add(newByte);

            L.e(TAG, "料道测试命令:" + Arrays.toString(resp));
            return "";
        } else {
            return "9999/正忙";
        }
    }

    /**
     * 给机器加货
     */
    public String addKucun(int boxIndex, byte roadNum) {

        //如果是格子柜 ：11号料道需要转换为0x11
        byte cn;
        if (boxIndex > 1) {
            cn = (byte) Integer.parseInt(roadNum + "", 16);
        } else {
            cn = roadNum;
        }
        byte[] resp = new byte[13];
        resp[0] = PC_HEAD_ONE;
        resp[1] = PC_HEAD_TWO;
        resp[2] = PC_HEAD_THREE;
        resp[3] = 6;
        resp[4] = CMD_LOOP;
        resp[5] = 0x01;
        resp[6] = (byte) boxIndex;
        resp[7] = cn;
        resp[8] = 0x01;
        resp[9] = getCountCheck(resp, 3, 9);

        byte[] newByte = new byte[10];
        System.arraycopy(resp, 0, newByte, 0, 10);
        otherToVMCParaList.add(newByte);

//        Log.e("机器加货详情", "箱号=" + boxIndex + ";" + "货道" + roadNum);
//
//        L.e(TAG, "给机器加货:" + Arrays.toString(resp));
        return "";
    }


    /**
     * 给机器加货
     */
    public String addKucunSingle(int boxIndex, byte roadNum) {

        //如果是格子柜 ：11号料道需要转换为0x11
        byte cn;
        if (boxIndex > 1) {
            cn = (byte) Integer.parseInt(roadNum + "", 16);
        } else {
            cn = roadNum;
        }

        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[13];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 6;
            resp[4] = CMD_LOOP;
            resp[5] = 0x01;
            resp[6] = (byte) boxIndex;
            resp[7] = cn;
            resp[8] = 0x01;
            resp[9] = getCountCheck(resp, 3, 9);

            toVMCPara = new byte[10];
            System.arraycopy(resp, 0, toVMCPara, 0, 10);

//            L.e(TAG, "给机器加货:" + Arrays.toString(resp));
            return "";
        } else {
            return "9999/正忙";
        }
    }

    /**
     * 发送空指令
     */
    public void sendEmpty() {
        byte[] lp = new byte[10];
        lp[0] = PC_HEAD_ONE;
        lp[1] = PC_HEAD_TWO;
        lp[2] = PC_HEAD_THREE;
        lp[3] = 0x06;// 数据长度
        lp[5] = 0x00;
        lp[6] = 0x00;
        lp[7] = 0x00;
        lp[8] = 0x00;
        lp[9] = getCountCheck(lp, 3, 9);
        otherToVMCParaList.add(lp);
    }

    /**
     * 设置指定箱号机器的货道价格
     *
     * @param boxNo
     * @param roadNo
     * @return EF　55 FE 09 76 08 00 03 00 00 00 C8 52
     */
    public String setGeziChannelPrice(int boxNo, int roadNo, int price) {
        if (!isConnected){
            MyApplication.getInstance().getLogBuHuo().d("补货设置货道价格 机器主控失联");
            return "1000/机器主控失联";
        }
        if (toVMCPara == null) {
            byte[] resp = new byte[13];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 9;
            resp[4] = CMD_LOOP;
            resp[5] = 0x08;
            // 箱号
            resp[6] = (byte) boxNo;
            // 料道号
            if (boxNo > 0) {
                resp[7] = (byte) Integer.parseInt((byte) roadNo + "", 16);
            } else {
                resp[7] = (byte) roadNo;
            }
            //resp[7] = (byte)roadNo;
            resp[8] = 0x00;
            resp[9] = 0x00;
            // 价格
            resp[10] = (byte) (price >> 8);
            resp[11] = (byte) price;
            resp[12] = getCountCheck(resp, 3, 12);

            toVMCPara = new byte[13];

            System.arraycopy(resp, 0, toVMCPara, 0, 13);
            //otherToVMCParaList.add(toVMCPara);
            MyApplication.getInstance().getLogBuHuo().d("补货设置货道价格 = 货道 : "+roadNo+" ; 价格 : "+price+" ; 箱号 : "+boxNo);
            return "";
        } else {
            return "9999/正忙";
        }

    }

    /**
     * 设置售货机时间
     */
    public String setVMCtime(byte year, byte month, byte day, byte hour,
                             byte min, byte sec) {
        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[13];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 0x09;
            resp[4] = CMD_LOOP;
            resp[5] = 0x04;
            resp[6] = year;
            resp[7] = month;
            resp[8] = day;
            resp[9] = hour;
            resp[10] = min;
            resp[11] = sec;
            resp[12] = getCountCheck(resp, 3, 12);

            toVMCPara = new byte[13];
            System.arraycopy(resp, 0, toVMCPara, 0, 13);

            return "";
        } else {
            return "9999/正忙";
        }
    }

    /**
     * 安卓工控机发起扣款请求 dealSerialNumber:流水号,channelNum:料道值 ,PAY_WAY支付方式
     * 现金出货
     * 1-钱币 2-刷卡 3-支付宝 4-微信
     */
    public String toPay(int dealSerialNumber, byte channelNum, byte PAY_WAY, long payMoney, int boxIndex) {

        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[27];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 23;
            resp[4] = CMD_LOOP;
            resp[5] = 0x05;
            // 交易序列号
            resp[6] = (byte) (dealSerialNumber >> 8);
            resp[7] = (byte) dealSerialNumber;
            // 箱号
            resp[8] = (byte) boxIndex;
            // 料道值
            if (boxIndex > 0) {
                resp[9] = (byte) Integer.parseInt(channelNum + "", 16);
            } else {
                resp[9] = channelNum;
            }
            // 支付方式
            resp[10] = PAY_WAY;
            // 支付金额
            resp[11] = (byte) (payMoney >> 40);
            resp[12] = (byte) (payMoney >> 32);
            resp[13] = (byte) (payMoney >> 24);
            resp[14] = (byte) (payMoney >> 16);
            resp[15] = (byte) (payMoney >> 8);
            resp[16] = (byte) payMoney;
            // 扣款出货方式
            resp[17] = 0x01;
            resp[18] = 0x00;
            // 金额变价0元时按原价计入
            switch (PAY_WAY) {
                case 1:
                    resp[19] = 0x01;
                    break;
                case 2:
                    resp[19] = 0x03;
                    break;
                case 3:
                    resp[19] = 0x04;
                    break;
                case 4:
                    resp[19] = 0x05;
                    break;
                default:
                    resp[19] = 0x00;
                    break;
            }
            resp[20] = 0x00;
            resp[21] = 0x00;
            resp[22] = 0x00;
            resp[23] = 0x00;
            resp[24] = 0x00;
            resp[25] = 0x00;
            resp[26] = getCountCheck(resp, 3, 26);

//            L.e(TAG, "格子柜出货指令:" + Arrays.toString(resp));
            MyApplication.getInstance().getLogBuyAndShip().d("发送扣款出货请求 = "+"流水号 : "+dealSerialNumber+" ; 货道号 : "+channelNum+" ; 箱号 : "+boxIndex+" ; 支付方式 : 现金 ; 价格 : "+payMoney);
            toVMCPara = new byte[27];
            System.arraycopy(resp, 0, toVMCPara, 0, 27);

            return "";
        } else {
            return "9999/正忙";
        }
    }

    /**
     * 安卓工控机发起扣款请求 dealSerialNumber:交易序列号,channelNum:料道值 ,PAY_WAY支付方式
     * 非现金出货
     * 1-钱币 2-刷卡 3-支付宝 4-微信
     */
    public String toPayForNoCash(int dealSerialNumber, byte channelNum, byte PAY_WAY, long payMoney, int boxIndex) {

        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[27];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 23;
            resp[4] = CMD_LOOP;
            resp[5] = 0x05;
            // 交易序列号
            resp[6] = (byte) (dealSerialNumber >> 8);
            resp[7] = (byte) dealSerialNumber;
            // 箱号
            resp[8] = (byte) boxIndex;
            // 料道值
            if (boxIndex > 0) {
                resp[9] = (byte) Integer.parseInt(channelNum + "", 16);
            } else {
                resp[9] = channelNum;
            }
            // 支付方式
            resp[10] = PAY_WAY;
            // 支付金额
            resp[11] = (byte) (payMoney >> 40);
            resp[12] = (byte) (payMoney >> 32);
            resp[13] = (byte) (payMoney >> 24);
            resp[14] = (byte) (payMoney >> 16);
            resp[15] = (byte) (payMoney >> 8);
            resp[16] = (byte) payMoney;
            // 扣款出货方式
            resp[17] = 0x01;
            resp[18] = 0x00;
            // 金额变价0元时按原价计入
            resp[19] = 0x04;
            resp[20] = 0x00;
            resp[21] = 0x00;
            resp[22] = 0x00;
            resp[23] = 0x00;
            resp[24] = 0x00;
            resp[25] = 0x00;
            resp[26] = getCountCheck(resp, 3, 26);

            toVMCPara = new byte[27];
            MyApplication.getInstance().getLogBuyAndShip().d("发送出货请求 = 流水号 : "+dealSerialNumber+" ; 货道号 : "+channelNum+" ; 箱号 : "+boxIndex+" ; 支付方式 : 非现金 ; 价格 : "+payMoney);
            System.arraycopy(resp, 0, toVMCPara, 0, 27);

            return "";
        } else {
            return "9999/正忙";
        }
    }

    /**
     * 安卓工控机发起扣款请求 dealSerialNumber:交易序列号,channelNum:料道值 ,PAY_WAY支付方式
     * 1-钱币 2-刷卡 3-支付宝 4-微信
     */
    public String toPayForGezi(int dealSerialNumber, byte channelNum, byte PAY_WAY, long payMoney) {

        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[27];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 23;
            resp[4] = CMD_LOOP;
            resp[5] = 0x05;
            // 交易序列号
            resp[6] = (byte) (dealSerialNumber >> 8);
            resp[7] = (byte) dealSerialNumber;
            // 箱号
            resp[8] = 0x00;
            // 料道值
            resp[9] = channelNum;
            // 支付方式
            resp[10] = PAY_WAY;
            // 支付金额
            resp[11] = (byte) (payMoney >> 40);
            resp[12] = (byte) (payMoney >> 32);
            resp[13] = (byte) (payMoney >> 24);
            resp[14] = (byte) (payMoney >> 16);
            resp[15] = (byte) (payMoney >> 8);
            resp[16] = (byte) payMoney;
            // 扣款出货方式
            resp[17] = 0x02;
            resp[18] = 0x00;
            // 金额变价0元时按原价计入
            resp[19] = 0x01;
            resp[20] = 0x00;
            resp[21] = 0x00;
            resp[22] = 0x00;
            resp[23] = 0x00;
            resp[24] = 0x00;
            resp[25] = 0x00;
            resp[26] = getCountCheck(resp, 3, 26);

            toVMCPara = new byte[27];
            System.arraycopy(resp, 0, toVMCPara, 0, 27);

            return "";
        } else {
            return "9999/正忙";
        }
    }


    /**
     * 售货机料道价格设定 channelNumbe:料道号，
     */
    public String setChannelPrice(byte channelNumber, int price) {
        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[13];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 9;
            resp[4] = CMD_LOOP;
            resp[5] = 0x08;
            // 箱号
            resp[6] = 0x00;
            // 料道号
            resp[7] = channelNumber;
            resp[8] = 0x00;
            resp[9] = 0x00;
            // 价格
            resp[10] = (byte) (price >> 8);
            resp[11] = (byte) price;
            resp[12] = getCountCheck(resp, 3, 12);

            toVMCPara = new byte[13];
            System.arraycopy(resp, 0, toVMCPara, 0, 13);

            return "";
        } else {
            return "9999/正忙";
        }
    }

    /**
     * 售货机料道价格设定 channelNumbe:料道号，
     */
    public String setChannelGoodscode(byte channelNumber, int code) {
        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[14];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 10;
            resp[4] = CMD_LOOP;
            resp[5] = 0x02;
            // 箱号
            resp[6] = 0x00;
            // 料道号
            resp[7] = channelNumber;
            // 商品编号
            resp[8] = (byte) (code >> 32);
            resp[9] = (byte) (code >> 24);
            resp[10] = (byte) (code >> 16);
            resp[11] = (byte) (code >> 8);
            resp[12] = (byte) code;

            resp[13] = getCountCheck(resp, 3, 13);

            toVMCPara = new byte[14];
            System.arraycopy(resp, 0, toVMCPara, 0, 14);

            return "";
        } else {
            return "9999/正忙";
        }
    }

    public String returnCoin(int fen) {
        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[9];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 5;
            resp[4] = CMD_LOOP;
            resp[5] = 0x09;
            // 价格
            resp[6] = (byte) (fen >> 8);
            resp[7] = (byte) fen;
            resp[8] = getCountCheck(resp, 3, 8);

            toVMCPara = new byte[9];
            System.arraycopy(resp, 0, toVMCPara, 0, 9);

            return "";
        } else {
            return "9999/正忙";
        }
    }

    /**
     * 查询主机或某一附机有无货信息 0x7B DEV_TYPE：箱号
     */
    public String checkThingsHaveOrNot(int boxNo) {
        byte[] resp = new byte[10];
        resp[0] = PC_HEAD_ONE;
        resp[1] = PC_HEAD_TWO;
        resp[2] = PC_HEAD_THREE;
        resp[3] = 0x06;
        resp[4] = CMD_LOOP;
        resp[5] = CMD_CHANNEL_THINGS_INFO;// 7B
        resp[6] = (byte) boxNo;
        resp[7] = 0x00;
        resp[8] = 0x00;
        resp[9] = getCountCheck(resp, 3, 9);
        byte[] newByte = new byte[10];
        System.arraycopy(resp, 0, newByte, 0, 10);
        otherToVMCParaList.add(newByte);
        return "";
    }

    /**
     * 查询主机或某一附机有无货信息 0x7B DEV_TYPE：箱号
     */
    public String checkThingsHaveOrNotForNow(int boxNo) {
        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[10];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 0x06;
            resp[4] = CMD_LOOP;
            resp[5] = CMD_CHANNEL_THINGS_INFO;// 7B
            resp[6] = (byte) boxNo;
            resp[7] = 0x00;
            resp[8] = 0x00;
            resp[9] = getCountCheck(resp, 3, 9);
            toVMCPara = new byte[10];
            System.arraycopy(resp, 0, toVMCPara, 0, 10);
            //        byte[] newByte = new byte[10];
            //        System.arraycopy(resp, 0, newByte, 0, 10);
            otherToVMCParaList.add(toVMCPara);
            return "";
        } else {
            return "9999/正忙";
        }
    }


    /**
     * 获取饮料机配置
     */
    public String getMachineSetting() {
        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[10];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 0x06;
            resp[4] = CMD_LOOP;
            resp[5] = CMD_CHANNEL_SET_STATUS;// 73
            resp[6] = 0x00;
            resp[7] = 0x00;
            resp[8] = 0x00;
            resp[9] = getCountCheck(resp, 3, 9);

            toVMCPara = new byte[10];
            System.arraycopy(resp, 0, toVMCPara, 0, 10);

            return "";
        } else {
            return "9999/正忙";
        }
    }

    public String checkMachineStatus() {
        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[10];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 0x06;
            resp[4] = CMD_LOOP;
            resp[5] = CMD_DEVICE_RUN_INFO;// 7D
            resp[6] = 0x00;
            resp[7] = 0x00;
            resp[8] = 0x00;
            resp[9] = getCountCheck(resp, 3, 9);

            toVMCPara = new byte[10];
            System.arraycopy(resp, 0, toVMCPara, 0, 10);
            otherToVMCParaList.add(toVMCPara);
            return "";
        } else {
            return "9999/正忙";
        }
    }

    public String checkSystemInfo() {
        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[10];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 0x06;
            resp[4] = CMD_LOOP;
            resp[5] = CMD_CONNECT;// 78
            resp[6] = 0x00;
            resp[7] = 0x00;
            resp[8] = 0x00;
            resp[9] = getCountCheck(resp, 3, 9);

            toVMCPara = new byte[10];
            System.arraycopy(resp, 0, toVMCPara, 0, 10);

            return "";
        } else {
            return "9999/正忙";
        }
    }

    public String checkSystemErrorInfo() {
        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[10];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 0x06;
            resp[4] = CMD_LOOP;
            resp[5] = CMD_DEVICE_ERR_STATUS;// 79
            resp[6] = 0x00;
            resp[7] = 0x00;
            resp[8] = 0x00;
            resp[9] = getCountCheck(resp, 3, 9);

            toVMCPara = new byte[10];
            System.arraycopy(resp, 0, toVMCPara, 0, 10);

            return "";
        } else {
            return "9999/正忙";
        }
    }

    public String checkSystemErrorInfo2() {
        byte[] resp = new byte[10];
        resp[0] = PC_HEAD_ONE;
        resp[1] = PC_HEAD_TWO;
        resp[2] = PC_HEAD_THREE;
        resp[3] = 0x06;
        resp[4] = CMD_LOOP;
        resp[5] = CMD_DEVICE_ERR_STATUS;// 79
        resp[6] = 0x00;
        resp[7] = 0x00;
        resp[8] = 0x00;
        resp[9] = getCountCheck(resp, 3, 9);

        byte[] newByte = new byte[10];
        System.arraycopy(resp, 0, newByte, 0, 10);
        otherToVMCParaList.add(newByte);
        return "";
    }

    public String checkBuy_Money() {
        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[10];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 0x06;
            resp[4] = CMD_LOOP;
            resp[5] = CMD_BUY_INFO;// 77
            resp[6] = 0x00;
            resp[7] = 0x00;
            resp[8] = 0x00;
            resp[9] = getCountCheck(resp, 3, 9);

            toVMCPara = new byte[10];
            System.arraycopy(resp, 0, toVMCPara, 0, 10);

            return "";
        } else {
            return "9999/正忙";
        }
    }

    public String checkBuy_HaveOrNot() {
        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[10];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 0x06;
            resp[4] = CMD_LOOP;
            resp[5] = CMD_BUY_INFO;// 77
            resp[6] = 0x00;
            resp[7] = 0x01;
            resp[8] = 0x00;
            resp[9] = getCountCheck(resp, 3, 9);

            toVMCPara = new byte[10];
            System.arraycopy(resp, 0, toVMCPara, 0, 10);

            return "";
        } else {
            return "9999/正忙";
        }
    }

    public String pause() {
        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[10];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 0x04;
            resp[4] = CMD_LOOP;
            resp[5] = 0x03;// 77
            resp[6] = 0x00;
            resp[7] = getCountCheck(resp, 3, 7);

            toVMCPara = new byte[8];
            System.arraycopy(resp, 0, toVMCPara, 0, 8);

            return "";
        } else {
            return "9999/正忙";
        }
    }

    public String sale() {
        if (!isConnected)
            return "1000/机器主控失联";
        if (toVMCPara == null) {
            byte[] resp = new byte[10];
            resp[0] = PC_HEAD_ONE;
            resp[1] = PC_HEAD_TWO;
            resp[2] = PC_HEAD_THREE;
            resp[3] = 0x04;
            resp[4] = CMD_LOOP;
            resp[5] = 0x03;// 77
            resp[6] = (byte) 0xFF;
            resp[7] = getCountCheck(resp, 3, 7);

            toVMCPara = new byte[8];
            System.arraycopy(resp, 0, toVMCPara, 0, 8);

            return "";
        } else {
            return "9999/正忙";
        }
    }

    /**
     * 将源数组追加到目标数组
     *
     * @param byte_1
     * @param byte_2
     * @param size
     * @return:<br>返回一个新的数组，包括了原数组1和原数组2
     */
    private byte[] ArrayAppend(byte[] byte_1, byte[] byte_2, int size) {
        // java 合并两个byte数组
        if (byte_1 == null && byte_2 == null) {
            return null;
        } else if (byte_1 == null) {
            if (byte_2.length == size) {
                return byte_2;
            } else { //byte_2.length > size
                return Arrays.copyOf(byte_2, size);
            }
            //return byte_2;
        } else if (byte_2 == null) {
            // byte[] byte_3 = new byte[byte_1.length];
            // System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
            return byte_1;
            //return byte_1;
        } else {
            byte[] byte_3 = new byte[byte_1.length + size];
            System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
            System.arraycopy(byte_2, 0, byte_3, byte_1.length, size);
            return byte_3;
        }
    }

    /**
     * 将源数组追加到目标数组
     *
     * @param byte_1
     * @param byte_2
     * @return:<br>返回一个新的数组，包括了原数组1和原数组2
     */
    private byte[] ArrayAppend(byte[] byte_1, byte[] byte_2) {
        if (byte_1 == null && byte_2 == null) {
            return null;
        } else if (byte_1 == null) {
            //  byte[] byte_3 = new byte[byte_2.length];
            // System.arraycopy(byte_2, 0, byte_3, 0, byte_2.length);
            return byte_2;
        } else if (byte_2 == null) {
            //byte[] byte_3 = new byte[byte_1.length];
            //System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
            return byte_1;
        } else {
            byte[] byte_3 = new byte[byte_1.length + byte_2.length];
            System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
            System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
            return byte_3;
        }
    }

    /**
     * 转换字节为十六进制
     *
     * @param src  字节数组
     * @param size 长度
     * @return 字符串
     */
    protected String bytesToHexString(byte[] src, int size) {
        String ret = "";
        if (src == null || size <= 0) {
            return null;
        }
        for (int i = 0; i < size; i++) {
            String hex = Integer.toHexString(src[i] & 0xFF);
            ret = ret + " " + hex;
        }
        return ret;
    }

    private BaseInfo baseInfo = new BaseInfo();

    private void returnConsumeInfo(String code, String info) {
        if (baseInfo == null) {
            baseInfo = new BaseInfo(code, info);
        } else {
            baseInfo.setCode(code);
            baseInfo.setInfo(info);
        }
        SerialObservable.getInstance().notifyChange(baseInfo);
    }
}
