package com.zongsheng.drink.h17.common;

import com.zongsheng.drink.h17.front.bean.PayModel;
import com.zongsheng.drink.h17.front.bean.ShipStatusModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import static org.apache.http.client.methods.RequestBuilder.put;

/**
 * json处理
 */
public class JsonControl {

    public static String payModels2Json(List<PayModel> payModels){
        StringBuffer sb = new StringBuffer();
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        try {
            for(PayModel payModel : payModels){
                jsonObject = new JSONObject();
                jsonObject.put("PushMachineSn",payModel.getPushMachineSn());
                jsonObject.put("OrderSn",payModel.getOrderSn());
                jsonObject.put("CreateTime",payModel.getCreateTime());
                jsonObject.put("OrderStatus",payModel.getOrderStatus());
                jsonObject.put("GoodsCode",payModel.getGoodsCode());
                jsonObject.put("GoodsNum",payModel.getGoodsNum());
                jsonObject.put("GoodsId",payModel.getGoodsId());
                jsonObject.put("GoodsBelong",payModel.getGoodsBelong());
                jsonObject.put("GoodsPrice",payModel.getGoodsPrice());
                jsonObject.put("MachineSn",payModel.getMachineSn());
                jsonObject.put("PayTime",payModel.getPayTime());
                jsonObject.put("PayType",payModel.getPayType());
                jsonObject.put("DeliveryTime",payModel.getDeliveryTime());
                jsonObject.put("MachineTradeNo",payModel.getMachineTradeNo());
                jsonObject.put("MachineRoadNo",payModel.getMachineRoadNo());
                jsonObject.put("DeliveryStatus",payModel.getDeliveryStatus());
                jsonArray.put(jsonObject);
            }
            sb.append(jsonArray.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String ShipStatusModel2Json(ShipStatusModel shipStatusModel){
        String res = "";
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("OrderSn",shipStatusModel.getOrderSn());
            jsonObject.put("MachineTime",shipStatusModel.getMachineTime());
            jsonObject.put("ShipStatus",shipStatusModel.getShipStatus());
            res = jsonObject.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        L.v(SysConfig.ZPush,"json---> " + res);
        return res;
    }
}
