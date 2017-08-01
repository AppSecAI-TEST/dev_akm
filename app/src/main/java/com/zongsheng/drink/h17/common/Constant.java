package com.zongsheng.drink.h17.common;

import android.os.Environment;

/**
 * Created by Suchengjian on 2017.3.4.
 */

public class Constant {
    /**提示信息*/
    public static final String MAIN_MACHINA_BUHUO_FAIL                                              = "主机补货通讯失败,请重试";
    public static final String MAIN_MACHINA_COMM_FAIL                                               = "主机通讯失败,请稍后重试!";
    public static final String MAIN_MACHINA_DONOT_CON                                               = "主机未连接,请稍后重试!";
    public static final String GEZIGUI_COMM_FAIL                                                    = "格子柜通讯失败,请稍后重试!";
    public static final String GEZIGUI_DONOT_FAIL                                                   = "格子柜未连接,请确认后重试";
    public static final String COMMIT_FAIL_NET                                                      = "提交失败,请检查网络连接";
    public static final String GOODS_DATA_ERROR                                                     = "商品数据不正确";
    public static final String GOODS_RATE_ERROR                                                     = "货道比率获取失败,请重新登录后重试!";
    public static final String DEALING                                                              = "处理中...";
    public static final String SYNCHING                                                             = "正在从平台同步...";

    public static final String VSI_ERROR_01                                                         = "主机未连接,请稍后重试!";
    public static final String VSI_ERROR_02                                                         = "主机通讯失败,请稍后重试!";

    public static final String NETWORK_ERROR1                                                       = "处理失败,请检查网络连接";
    public static final String NETWORK_ERROR2                                                       = "网络连接出错，可以在APP端完成补货\nAPP首页-右上角加号-\n-查看今日运营报告-完成补货";
    public static final String NETWORK_ERROR3                                                       = "网络请求失败,请重试";

    public static final String GEZHI_DATA_ERROR                                                     = "格子柜数据错误,请退出重试";
    public static final String BUHUO_NOSUBMIT                                                       = "修改信息尚未提交,确定离开？";

    public static final String NO_ERROR_MSG                                                         = "没有返回错误信息！";
    public static final String ERROR_INFO_GEZI_01                                                   = "遥控多格子柜设置不合理，请重新设置";
    public static final String ERROR_INFO_GEZI_02                                                   = "绑定格子柜的数量已经达到了设置的上限";
    public static final String ERROR_INFO_GEZI_03                                                   = "获取失败,请检查网络连接";
    public static final String APP_KILLED                                                           = "很抱歉,程序出现异常,即将退出.";

    public static final String RECENT_VERSION                                                       = "已经是最新版本";
    public static final String UPLOAD_VERSION01                                                     = "发现新版本，建议您立即更新！";
    public static final String START_DOWNLOAD                                                       = "开始下载";

    public static final String CALLBACK_PAYTYPE_ALI                                                 = "2";
    public static final String CALLBACK_PAYTYPE_WECHAT                                              = "1";

    public static final String SHIPMENTSUCCESS                                                      = "0"; //出货成功

    public static final String DELIVERYSUCCESS                                                      = "1";
    public static final String DELIVERYFAIL                                                         = "2";

    public static final int BACKGROUND_WHAT_0                                                       = 0;
    public static final int BACKGROUND_WHAT_MQ                                                      = 1;

    public static final String AUTOREFUNDSTATE_NOUSED                                               = "0";
    public static final String AUTOREFUNDSTATE_ALLUSED                                              = "1";
    public static final String AUTOREFUNDSTATE_ALIPAY                                               = "2";
    public static final String AUTOREFUNDSTATE_WETCH                                                = "3";

    //支付相关key值
    public static final String PAY_ORDER_SN                                                         = "order_sn";
    public static final String PAY_TYPE                                                             = "pay_type";

    //保存机器指令相关变量
    public static final String PATH_NAME                                                            = Environment.getExternalStorageDirectory() + "/zongsheng_machine/";
    public static final String FILE_NAME                                                            = "command.txt";
    public static final long DELETE_PERIOD                                                          = 5 * 24 * 60 * 60 * 1000;
    public static final String SP_MACHINE_COMMAND                                                   = "machine-command";
    public static final String SP_IS_FIRST_IN                                                       = "isFirstIn";

    //一键开门超时时间
    public static final long ONE_KEY_OPEN_DOOR                                                      = 4 * 60 * 1000;
    public static final long DIALOG_DISMISS_TIME                                                    = 30 * 1000;
}
