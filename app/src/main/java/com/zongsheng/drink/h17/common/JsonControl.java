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
                jsonObject.put("pushMachineSn",payModel.getPushMachineSn());
                jsonObject.put("orderSn",payModel.getOrderSn());
                jsonObject.put("createTime",payModel.getCreateTime());
                jsonObject.put("orderStatus",payModel.getOrderStatus());
                jsonObject.put("goodsCode",payModel.getGoodsCode());
                jsonObject.put("goodsNum",payModel.getGoodsNum());
                jsonObject.put("goodsId",payModel.getGoodsId());
                jsonObject.put("goodsBelong",payModel.getGoodsBelong());
                jsonObject.put("goodsPrice",payModel.getGoodsPrice());
                jsonObject.put("machineSn",payModel.getMachineSn());
                jsonObject.put("payTime",payModel.getPayTime());
                jsonObject.put("payType",payModel.getPayType());
                jsonObject.put("deliveryTime",payModel.getDeliveryTime());
                jsonObject.put("machineTradeNo",payModel.getMachineTradeNo());
                jsonObject.put("machineRoadNo",payModel.getMachineRoadNo());
                jsonObject.put("deliveryStatus",payModel.getDeliveryStatus());
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
            jsonObject.put("orderSn",shipStatusModel.getOrderSn());
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
