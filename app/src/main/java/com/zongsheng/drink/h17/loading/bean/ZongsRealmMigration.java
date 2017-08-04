package com.zongsheng.drink.h17.loading.bean;

import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

import static com.yolanda.nohttp.RequestMethod.HEAD;

/**
 * 数据库版本更新记录
 * Created by xunku on 16/9/5.
 */

public class ZongsRealmMigration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();
        // 版本1 20160908产品信息添加商家二维码地址
        if (oldVersion == 0) {
            schema.get("GoodsInfo")
                    .addField("payQCodeUrl", String.class);
            oldVersion++;
        }
        // 添加帮助信息表
        if (oldVersion == 1) {
            schema.create("HelpInfo")
                    .addField("helpID", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("helpType", String.class)
                    .addField("question", String.class)
                    .addField("answer", String.class)
                    .addField("helpIntro", String.class);
            oldVersion++;
        }
        // 销售记录添加是否上传
        if (oldVersion == 2) {
            schema.get("SaleRecord")
                    .addField("isUploaded", String.class);
            schema.get("HelpInfo")
                    .addField("showSort", int.class);
            oldVersion++;
        }
        // 操作日志添加是否上传
        if (oldVersion == 3) {
            schema.get("LogsInfo")
                    .addField("isUploaded", String.class);
            oldVersion++;
        }
        // 添加缺币记录 缺货记录 20161009
        if (oldVersion == 4) {
            schema.create("QueBiRecord")
                    .addField("isQueBi_five", String.class)
                    .addField("isQueBi_one", String.class)
                    .addField("machineSn", String.class)
                    .addField("createTime", long.class)
                    .addField("isUploaded", String.class);

            schema.create("QueHuoRecord")
                    .addField("isQueHuo", String.class)
                    .addField("road_no", String.class)
                    .addField("machineSn", String.class)
                    .addField("createTime", long.class)
                    .addField("isUploaded", String.class);
            oldVersion++;
        }
        // 销售记录添加商品名称
        if (oldVersion == 5) {
            schema.get("SaleRecord")
                    .addField("name", String.class);
            oldVersion++;
        }
        // 订单出货信息添加出货时间
        if (oldVersion == 6) {
            schema.get("PushOrderInfo")
                    .addField("saleTime", String.class);
            oldVersion++;
        }
        // 出货记录添加在线支付金额
        if (oldVersion == 7) {
            schema.get("SaleRecord")
                    .addField("noCashPrice", String.class);
            oldVersion++;
        }
        // 在线订单记录添加交易号
        if (oldVersion == 8) {
            schema.get("PushOrderInfo")
                    .addField("trade_no", String.class);
            oldVersion++;
        }
        // 删除出货记录的主键recordNumber 交易号
        if (oldVersion == 9) {
            schema.get("SaleRecord").addField("param", String.class)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.set("param", obj.getString("recordNumber"));
                        }
                    }).removeField("recordNumber");
            schema.get("SaleRecord").addField("recordNumber", String.class)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.set("recordNumber", obj.getString("param"));
                        }
                    });
            oldVersion++;
        }
        // 删除SaleRecord，添加PayModel
        if (oldVersion == 10) {
            schema.remove("SaleRecord");
            schema.remove("PushOrderInfo");
            schema.get("BackGroundRequest")
                    .addField("what", int.class);
            schema.create("PayModel")
                    .addField("PushMachineSn", String.class)
                    .addField("OrderSn", String.class)
                    .addField("CreateTime", String.class)
                    .addField("OrderStatus", int.class)
                    .addField("GoodsCode", String.class)
                    .addField("GoodsNum", String.class)
                    .addField("GoodsId", String.class)
                    .addField("GoodsBelong", String.class)
                    .addField("GoodsPrice", String.class)
                    .addField("MachineSn", String.class)
                    .addField("PayTime", String.class)
                    .addField("PayType", String.class)
                    .addField("DeliveryTime", String.class)
                    .addField("MachineTradeNo", String.class)
                    .addField("MachineRoadNo", String.class)
                    .addField("isUploaded", String.class)
                    .addField("recordInfo", String.class)
                    .addField("goodsName", String.class)
                    .addField("param", String.class)
                    .addField("deliveryStatus", String.class);
            oldVersion++;
        }

        if (oldVersion == 11) {
            schema.get("GoodsInfo").addIndex("goodsBelong");

            schema.create("MQReceiver")
                    .addField("operationtype", String.class)
                    .addField("order_sn", String.class)
                    .addField("goods_id", String.class)
                    .addField("machine_sn", String.class)
                    .addField("goods_belong", String.class)
                    .addField("goods_price", String.class)
                    .addField("pay_time", String.class)
                    .addField("push_machine_sn", String.class)
                    .addField("machine_tradeno", String.class)
                    .addField("trade_no", String.class)
                    .addField("pay_type", String.class);

            oldVersion++;
        }

        if (oldVersion == 12) {
            schema.create("BindDesk")
                    .addField("machineSn", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("mainMachineSn", String.class)
                    .addField("machineName", String.class)
                    .addField("companyId", String.class)
                    .addField("machineType", String.class)
                    .addField("machineGroup", String.class)
                    .addField("templeId", String.class)
                    .addField("usedStatus", String.class)
                    .addField("routeId", String.class)
                    .addField("routeName", String.class)
                    .addField("positionId", String.class)
                    .addField("positionName", String.class)
                    .addField("showSort", String.class)
                    .addField("createTime", String.class)
                    .addField("roadCount", String.class)
                    .addField("isRefundRemove", boolean.class)
                    .addField("isRemoveMachine", boolean.class);
            oldVersion++;
        }
    }
}
