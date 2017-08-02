package com.zongsheng.drink.h17.common;

/**
 * Created by xunku on 16/8/30.
 */


/** 系统定数信息 */
public class SysConfig {

    /*** 广告目录 */
    public static final String SD_CARD_PATH_AD                                                      = "/zongs/machine_ad";

    /** 广告图片显示时长 ms */
    public static final int IMAGE_AD_PALY_TIME                                                      = 5000;

    /**后台请求返回为错误时，再次尝试的次数*/
    public final static int ERROR_TIME                                                              = 3;

    /** 是否真机调试 */
    public static final boolean isRealMachineRun                                                    = true;

    /** 是否加密 */
    public static final boolean IS_SECRET                                                           = false;

    /**
     * 错误代码
     */
    public static final String ERROR_CODE_SUCCESS                                                   = "00000";
    /**
     * 请求失败
     */
    public static final String ERROR_CODE_REQ_ERROR                                                 = "10002";
    /**
     *系统异常
     */
    public static final String ERROR_CODE_EXCEP                                                     = "10001";
    /**
     * 参数错误
     */
    public static final String ERROR_CODE_ARGS_ERROR                                                = "10003";

    public static final String ERROR_INFO_SNEXIST                                                   = "售货机编号不正确";
    public static final String ERROR_INFO_NOEXIST                                                   = "售货机编号不存在，请添加编号！";
    public static final String ERROR_INFO_NOUSED                                                    = "售货机编号未认证";

    /**da
     * 推送广播
     */
    public static final String PUSH_MESSAGE_ACTION                                                  = "com.zongsheng.drink.h17.xg.receiver";

    public static final int    MQ_PORT                                                              = 5672;
    public static final String MQ_USERNAME                                                          = "ZongsMQ";
    public static final String MQ_PASSWORLD                                                         = "Zongs!@#";
    public static final String MQ_ADDORDER                                                          = "AddOrder";
    public static final String MQ_SHIPTODO                                                          = "ShipToDo";
    public static final String PAY_AES_KEY                                                          = "RjYyNUEwN0RBNkU0";

    //正式
//    public static final String PAY_NET_TEST_URL                                                     = "https://pay.zongs365.com/home/pay?";
//    public static final String MQ_HOST                                                              = "114.55.146.199";
//    public static final String WECHAT_REFUND_URL                                                    = "https://pay.zongs365.com/WeiXin/Refund?";
//    public static final String ALIBABA_REFUND_URL                                                   = "https://pay.zongs365.com/AiPay/Refund?";
//    public static final String JINGDONG_REFUND_URL                                                  = "https://pay.zongs365.com/JdPay/Refund?";
//    public static final String NET_SERVER_HOST_ADDRESS                                              = "http://machineapi.zongs365.com/";//.net新接口测试域名
//    public static final String VERSIONTAG                                                           = "";
//    public static final String PANDIAN_ADDRESS                                                      = "http://118.178.155.31/datacenter/InventoryServlet?password=zongs365";//正式

    //测试
    public static final String PAY_NET_TEST_URL                                                     = "https://cspay.zongs365.com/home/pay?";
    public static final String WECHAT_REFUND_URL                                                    = "https://cspay.zongs365.com/WeiXin/Refund?";
    public static final String ALIBABA_REFUND_URL                                                   = "https://cspay.zongs365.com/AiPay/Refund?";
    public static final String JINGDONG_REFUND_URL                                                  = "";
    public static final String NET_SERVER_HOST_ADDRESS                                              = "http://118.178.56.85:8082/";//测试
    public static final String PANDIAN_ADDRESS                                                      = "http://116.62.104.62/datacenter/InventoryServlet?password=zongs365";//测试
    public static final String MQ_HOST                                                              = "118.178.139.93";//测试
    public static final String VERSIONTAG                                                           = "测试";

    /**
     * 主柜子状态
     */
    public static final String MAIN_DOOR_IS_OPEN                                                    = "1";
    public static final String MAIN_DOOR_IS_CLOSE                                                   = "0";

    public static final String SHIPSTATUS_SHIP                                                      = "1";
    public static final String SHIPSTATUS_REFUND                                                    = "2";


    /** 日志级别 0:一般 1:中等 2:重要 */
    public static final long L_TIME_1S                                                              = 1000;
    public static final String LOG_LEVEL_NORMAL                                                     = "0";
    public static final String LOG_LEVEL_MIDDLE                                                     = "1";
    public static final String LOG_LEVEL_IMPORTANT                                                  = "2";

    public static final long L_REQ_AG_TIME_60                                                       = 60000;
    public static final long L_REQ_AG_TIME_30                                                       = 30000;
    public static final long L_REQ_AG_TIME_10                                                       = 10000;
    public static final long L_REQ_AG_TIME_5                                                        = 5000;
    public static final long L_REQ_AG_TIME_3                                                        = 3000;
    public static final long L_REQ_AG_TIME_5M                                                       = 300000;
    public static final long L_REQ_AG_TIME_30M                                                      = L_REQ_AG_TIME_60 * 30;
    public static final long L_REQ_AG_TIME_15                                                       =15000;
    public static final long L_TIME_05                                                              = 500;
    public static final long L_TIME_03                                                              = 300;

    public static final int TRY_OPEN_DOOR_TIME                                                      = 5;
    public static final int PAGERSIZE                                                               = 5;

    /**是否售空 0:未空 1:售空*/
    public static final String ISSOLDOUT_FLAG                                                       = "1";
    public static final String NOTSOLDOUT_FLAG                                                      = "0";

    /**机器认证状态 ，0没认证，1已认证*/
    public static final String IS_AUTHENTICATION                                                    = "1";
    public static final String NOT_AUTHENTICATION                                                   = "0";

    /**库存*/
    public static final int    UPDATE_STOCK                                                         = 1;
    /**价格*/
    public static final int    UPDATE_PRICE                                                         = 2;
    /**价格和库存*/
    public static final int    UPDATE_STOCKANDPRICE                                                 = 3;
    /**code*/
    public static final int    GOODSCODE                                                            = 4;

    /** 产品所属 1:主机 2:格子柜 */
    public static final String MACHINE_TYPE_2                                                       = "2";
    public static final String MACHINE_TYPE_1                                                       = "1";


    public static final String PING_ADDRESS                                                         = "www.baidu.com";
    public static final String JSON_KEY_ERROR                                                       = "error";
    public static final String JSON_KEY_ERROR_CODE                                                  = "error_code";
    public static final String JSON_KEY_SYSTEMVERSION                                               = "systemVerson";

    public static final String JSON_KEY_MACHINEINFOFOEMQ                                            = "machineInfoForMq";
    public static final String JSON_KEY_USEDSTATUS                                                  = "usedStatus";
    public static final String JSON_KEY_MQIP                                                        = "mqIp";
    public static final String JSON_KEY_REFUNDSTATE                                                 = "automaticRefundState";

    public static final String RECEIVER_ACTION_DEAMON                                               = "com.action.restart.hexinservice";
    public static final String ENG_MODE_SWITCH                                                      = "android.intent.action.ENG_MODE_SWITCH";

    public static final String TIME_FORMAT_S                                                        = "yyyyMMddHHmmss";
    public static final String TIME_FORMAT_S_SSS                                                    = "yyyyMMddHHmmssSSS";

    public static final long SHIPMENT_TIME_LIMIT                                                    = 90;//90s出货时长限制

    public static final String MYREALM_NAME                                                         = "myrealm.realm";
    public static final long   REALM_VERSION                                                        = 13;

    public static final String AOKEMA23                                                             = "aokema23";
    public static final String AOKEMA                                                               = "aokema";

    public static final int NO_MACHINE_SN                                                           = 0;
    public static final int MACHINE_CONNECT_ERROR                                                   = 1;

    public static final int ORDERSN_CACHE_SIZE                                                      = 10;

    public static final String SHIPMENTTYPE                                                         = "1";

    public static final String ZPush                                                                = "ZPush";
    public static final String MT                                                                   = "mt";

    //支付相关
    public static final String AUTO_PAY_PICTURE                                                     = "autoPayPicture";
    public static final String PAY_TYPE                                                             = "payType";
}
