package com.zongsheng.drink.h17;

import com.zongsheng.drink.h17.common.DataUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 机器故障util
 * Created by dongxiaofei on 16/9/16.
 */

public class MachineFaultUtil {

    public static final String MASTER_TAG = "master";
    public static final String PAPER_TAG = "paper";
    public static final String COIN_TAG = "coin";

    /** 获取机器故障信息 */
    public static Map<String, String> getMachineFaultInfo(String faultString) {

        Map<String, String> rstMap = new HashMap<>();
        rstMap.put(MASTER_TAG, "");
        rstMap.put(PAPER_TAG, "");
        rstMap.put(COIN_TAG, "");
        if (faultString == null || "".equals(faultString)) {
            return rstMap;
        }
        String[] faultCodes = faultString.split(",");
        String faultInfo = "";
        for (String faultCode : faultCodes) {
            // 判断是否是主控故障
            if (getMasterFaultInfo().containsKey(faultCode.toUpperCase())) {
                faultInfo = faultCode + ": " + getMasterFaultInfo().get(faultCode.toUpperCase()) + "; ";
                rstMap.put(MASTER_TAG, rstMap.get(MASTER_TAG) + faultInfo);
            } else if (getPaperFaultInfo().containsKey(faultCode.toUpperCase())) { // 纸币器故障
                faultInfo = faultCode + ": " + getPaperFaultInfo().get(faultCode.toUpperCase()) + "; ";
                rstMap.put(PAPER_TAG, rstMap.get(PAPER_TAG) + faultInfo);
            } else if (getCoinFaultInfo().containsKey(faultCode.toUpperCase())) { // 硬币器故障
                faultInfo = faultCode + ": " + getCoinFaultInfo().get(faultCode.toUpperCase()) + "; ";
                rstMap.put(COIN_TAG, rstMap.get(COIN_TAG) + faultInfo);
            }
        }
        return rstMap;
    }

    /** 主控故障信息 */
    private static Map<String, String> masterFaultInfo;

    private static Map<String, String> getMasterFaultInfo() {
        if (masterFaultInfo != null) {
            return masterFaultInfo;
        }
        masterFaultInfo = new HashMap<>();

        masterFaultInfo.put("Y5-0", "驱动板无应答");
        masterFaultInfo.put("Y5-1", "GPS通讯单元无应答");
        masterFaultInfo.put("Y5-2", "系统时钟或EEPROM不正常");
        masterFaultInfo.put("Y5-3", "左室温度传感器有故障");
        masterFaultInfo.put("Y5-4", "右室温度传感器有故障");
        masterFaultInfo.put("Y5-5", "驱动无220V电源");
        masterFaultInfo.put("Y5-7", "读卡器无应答");

        masterFaultInfo.put("Y9-0", "弹簧机无应答");
        masterFaultInfo.put("Y9-1", "附加格子柜1无应答");
        masterFaultInfo.put("Y9-2", "附加格子柜2无应答");
        masterFaultInfo.put("Y9-3", "附加格子柜3无应答");
        masterFaultInfo.put("Y9-4", "附加格子柜4无应答");
        masterFaultInfo.put("Y9-5", "附加格子柜5无应答");
        masterFaultInfo.put("Y9-6", "附加格子柜6无应答");
        masterFaultInfo.put("Y9-7", "附加其他无应答");
        return masterFaultInfo;
    }

    /** 纸币器故障信息 */
    private static Map<String, String> paperFaultInfo;

    private static Map<String, String> getPaperFaultInfo() {
        if (paperFaultInfo != null) {
            return paperFaultInfo;
        }
        paperFaultInfo = new HashMap<>();
        paperFaultInfo.put("Y6-0", "纸币器无应答");
        paperFaultInfo.put("Y6-1", "纸币器钱箱被取走");
        paperFaultInfo.put("Y6-2", "纸币器钱箱满");
        paperFaultInfo.put("Y6-3", "纸币器传感器有故障");
        paperFaultInfo.put("Y6-4", "纸币器驱动马达有故障");
        paperFaultInfo.put("Y6-5", "纸币器纸币堵塞");
        paperFaultInfo.put("Y6-6", "纸币器ROM校验错");
        paperFaultInfo.put("Y6-7", "纸币器基数值错误");

        return paperFaultInfo;
    }

    /** 硬币器故障信息 */
    private static Map<String, String> coinFaultInfo;

    private static Map<String, String> getCoinFaultInfo() {
        if (coinFaultInfo != null) {
            return coinFaultInfo;
        }
        coinFaultInfo = new HashMap<>();

        coinFaultInfo.put("Y7-0", "硬币器无应答");
        coinFaultInfo.put("Y7-1", "硬币接收堵塞");
        coinFaultInfo.put("Y7-2", "硬币支出堵塞");
        coinFaultInfo.put("Y7-3", "硬币器传感器有故障");
        coinFaultInfo.put("Y7-4", "接收器工作电压低");
        coinFaultInfo.put("Y7-5", "硬币器ROM校验错");

        coinFaultInfo.put("Y8-0", "两枚硬币靠的太近,无法识别");
        coinFaultInfo.put("Y8-1", "硬币异常移走");
        coinFaultInfo.put("Y8-2", "硬币路径错误");
        coinFaultInfo.put("Y8-3", "硬币器处于服务状态");
        coinFaultInfo.put("Y8-6", "驱动电源故障");
        coinFaultInfo.put("Y8-7", "驱动板无应答");

        return coinFaultInfo;
    }
}
