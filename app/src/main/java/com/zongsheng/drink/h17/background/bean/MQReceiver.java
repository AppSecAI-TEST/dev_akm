package com.zongsheng.drink.h17.background.bean;

import io.realm.RealmObject;

/**
 * Created by Suchnegjian on 2017.3.25.
 * 为了测试MQ收到的数据是否出现重复
 */

public class MQReceiver extends RealmObject {

    // 操作类型 1：出货 2：更新订单成功
    public String operationtype;
    // 订单编号
    public String order_sn;
    // 商品ID
    public String goods_id;
    // 机器编码
    public String machine_sn;
    // 商品所属 1：饮料机 2：格子柜
    public String goods_belong;
    // 商品价格
    public String goods_price;
    // 支付时间
    public String pay_time;
    // 推送主机
    public String push_machine_sn;
    // 机器端流水号
    public String machine_tradeno;

    public String trade_no;

    // 0现金，1微信，2支付宝
    public String pay_type;

    @Override
    public String toString() {
        return "MQReceiver{" +
                "operationtype=" + operationtype +
                ", order_sn='" + order_sn + '\'' +
                ", goods_id='" + goods_id + '\'' +
                ", machine_sn='" + machine_sn + '\'' +
                ", goods_belong='" + goods_belong + '\'' +
                ", goods_price='" + goods_price + '\'' +
                ", pay_time='" + pay_time + '\'' +
                ", push_machine_sn='" + push_machine_sn + '\'' +
                ", machine_tradeno='" + machine_tradeno + '\'' +
                ", trade_no='" + trade_no + '\'' +
                ", pay_type='" + pay_type + '\'' +
                '}';
    }

    public String getOperationtype() {
        return operationtype;
    }

    public void setOperationtype(String operationtype) {
        this.operationtype = operationtype;
    }

    public String getOrder_sn() {
        return order_sn;
    }

    public void setOrder_sn(String order_sn) {
        this.order_sn = order_sn;
    }

    public String getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(String goods_id) {
        this.goods_id = goods_id;
    }

    public String getMachine_sn() {
        return machine_sn;
    }

    public void setMachine_sn(String machine_sn) {
        this.machine_sn = machine_sn;
    }

    public String getGoods_belong() {
        return goods_belong;
    }

    public void setGoods_belong(String goods_belong) {
        this.goods_belong = goods_belong;
    }

    public String getGoods_price() {
        return goods_price;
    }

    public void setGoods_price(String goods_price) {
        this.goods_price = goods_price;
    }

    public String getPay_time() {
        return pay_time;
    }

    public void setPay_time(String pay_time) {
        this.pay_time = pay_time;
    }

    public String getPush_machine_sn() {
        return push_machine_sn;
    }

    public void setPush_machine_sn(String push_machine_sn) {
        this.push_machine_sn = push_machine_sn;
    }

    public String getMachine_tradeno() {
        return machine_tradeno;
    }

    public void setMachine_tradeno(String machine_tradeno) {
        this.machine_tradeno = machine_tradeno;
    }

    public String getTrade_no() {
        return trade_no;
    }

    public void setTrade_no(String trade_no) {
        this.trade_no = trade_no;
    }

    public String getPay_type() {
        return pay_type;
    }

    public void setPay_type(String pay_type) {
        this.pay_type = pay_type;
    }
}
