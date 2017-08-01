package com.zongsheng.drink.h17.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.inputmethod.InputMethodManager;

import com.yolanda.nohttp.rest.Request;
import com.zongsheng.drink.h17.background.bean.MachineRoad;
import com.zongsheng.drink.h17.background.bean.UpateModel;
import com.zongsheng.drink.h17.common.aes.AESUtil;
import com.zongsheng.drink.h17.front.bean.BackGroundRequest;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmResults;

public class DataUtil {


    private static AESUtil aesUtil;

    /**
     * 验证是否为空
     */
    public static boolean isEmpty(String content) {
        return content == null || "".equals(content);
    }

    /**
     * 验证手机号
     *
     * @return 验证结果 true:正确 false:错误
     * @parammobiles手机号
     */
    public static boolean isMobileNO(String mobiles) {
        /*Pattern p = Pattern
				.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");*/
        Pattern p = Pattern
                .compile("^((1[3,5,8][0-9])|(14[5,7])|(17[0,6,7,8]))\\d{8}$");

        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * 验证是否为身份证号
     */
    public static boolean isIDCard(String content) {
        return content.length() == 18;
    }

    /**
     * 验证是否为正确的金额
     */
    public static boolean isMoney(String content) {
        Pattern p = Pattern
                .compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$");
        Matcher m = p.matcher(content);
        return m.matches();
    }

    /**
     * 关闭键盘
     */
    public static void closeKeyBord(Activity context) {
        if (context.getCurrentFocus() != null
                && context.getCurrentFocus().getWindowToken() != null) {
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 截取字符串
     */
    public static String subStr(String str, int subSLength) {
        subSLength = subSLength * 2;
        String subStr = "";
        try {
            if (str == null)
                return "";
            else {
                int tempSubLength = subSLength;// 截取字节数
                subStr = str.substring(0,
                        str.length() < subSLength ? str.length() : subSLength);// 截取的子串
                int subStrByetsL = subStr.getBytes("GBK").length;// 截取子串的字节长度
                // int subStrByetsL = subStr.getBytes().length;//截取子串的字节长度
                // 说明截取的字符串中包含有汉字
                while (subStrByetsL > tempSubLength) {
                    int subSLengthTemp = --subSLength;
                    subStr = str.substring(0,
                            subSLengthTemp > str.length() ? str.length()
                                    : subSLengthTemp);
                    subStrByetsL = subStr.getBytes("GBK").length;
                    // subStrByetsL = subStr.getBytes().length;
                }
                if (!subStr.equals(str)) {
                    subStr += "...";
                }
                return subStr;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return subStr;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, double dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 四舍五入 保留两位小数
     */
    public static double doubleChange(double str) {
        BigDecimal bd = new BigDecimal(str);
        return bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 四舍五入 保留两位小数
     */
    public static double doubleChange(double str, int pointCount) {
        BigDecimal bd = new BigDecimal(str);
        return bd.setScale(pointCount, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 计算距离
     */
    public static String changeDistance(String distance) {
        if ("-".equals(distance)) {
            return distance + "m";
        }

        if (distance == null || distance.equals("")) {
            return "-" + "m";
        }

        if (Double.parseDouble(distance) >= 1000) {
            return doubleChange(Double.parseDouble(distance) / 1000, 1) + "km";
        } else {
            return (int) Double.parseDouble(distance) + "m";
        }

    }

    public static Bitmap GetLocalOrNetBitmap(String url) {
        Bitmap bitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(new URL(url).openStream(), 2 * 1024);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, 2 * 1024);
            copy(in, out);
            out.flush();
            byte[] data = dataStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            data = null;
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] b = new byte[2 * 1024];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }

    public static UpateModel setUpdateModel(int type,int boxindex, GoodsInfo goodsInfo){

        if(goodsInfo != null) {
            UpateModel upateModel = new UpateModel();
            upateModel.setType(type);
            upateModel.setBoxindex(boxindex);
            upateModel.setRoad_no(goodsInfo.getRoad_no());
            upateModel.setPrice(goodsInfo.getPrice());
            upateModel.setGoodsId(goodsInfo.getGoodsID());
            return upateModel;
        }else{
           return null;
        }
    }

    public static UpateModel setUpdateModel2(int type, int boxindex, MachineRoad machineRoad){
        if(machineRoad != null) {
            UpateModel upateModel = new UpateModel();
            upateModel.setType(type);
            upateModel.setBoxindex(boxindex);
            upateModel.setRoad_no(Integer.parseInt(machineRoad.getRoadNo()));
            upateModel.setPrice(machineRoad.getGoodsPrice());
            upateModel.setGoodsId(machineRoad.getGoodsId());
            return upateModel;
        }else{
            return null;
        }
    }

    /**
     * 验证是否为邮箱
     */
    public static boolean isEmail(String email) {
        Pattern p = Pattern
                .compile("^[a-z0-9]+([._\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9" +
                        "]*[a-z0-9]+.){1,63}[a-z0-9]+$");
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * 改变手机号
     */
    public static String changeMobie(String mobile) {
        try {
            return mobile.substring(0,
                    mobile.length() - (mobile.substring(3)).length())
                    + "****" + mobile.substring(7);
        } catch (Exception e) {
        }
        return mobile;
    }

    /**
     * px to dp
     */
    public int Px2Dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    /**
     * 加载本地图片
     * http://bbs.3gstdy.com
     *
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url, int width, int height) {
        try {
//            Bitmap bmp = null;
//
//            BitmapFactory.Options opts = new BitmapFactory.Options();
//            opts.inJustDecodeBounds = true;
//            BitmapFactory.decodeFile(url, opts);
//
//            opts.inSampleSize = computeSampleSize(opts, -1, 128*128);
//            opts.inJustDecodeBounds = false;
//            try {
//                bmp = BitmapFactory.decodeFile(url, opts);
//            } catch (OutOfMemoryError err) {
//            }
            return decodeSampledBitmapFromFile(url, width, height);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            return inSampleSize;
        }

        return 0;
    }

    public static Bitmap decodeSampledBitmapFromFile(String filename,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }
        /**
         * 数据AES加密
         */
    public static String aesencrypt(String paramData) {
        if (aesUtil == null) {
            aesUtil = new AESUtil();
        }
        try {
            return aesUtil.encrypt(paramData.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 数据AES解密
     */
    public static String aesdecrypt(String paramData) {
        if (aesUtil == null) {
            aesUtil = new AESUtil();
        }
        return aesUtil.decrypt(paramData);
    }


    /**
     * 请求数据处理
     */
    public static void requestDateContrl(Map<String, String> paramMap, Request<String> request) {
        if (SysConfig.IS_SECRET) {
            String timePoint = String.valueOf(new Date().getTime());
            paramMap.put("time_point", timePoint);
            // 参数转换为json
            String paramData = mapToJson(paramMap);
            // 加密参数
            paramData = aesencrypt(paramData);
            request.add("timePoint", timePoint);// String类型
            request.add("paramData", paramData);
        } else {
            for (String key : paramMap.keySet()) {
                request.add(key, paramMap.get(key));
            }
        }
    }

    /**
     * 把HashMap转换成json
     */
    public static String mapToJson(Map<String, String> map) {
        StringBuffer strBuf = new StringBuffer("{");
        for (String key : map.keySet()) {
            strBuf.append("'");
            strBuf.append(key);
            strBuf.append("':");
            strBuf.append("'");
            strBuf.append(map.get(key));
            strBuf.append("',");
        }
        String retStr = strBuf.toString();
        retStr = retStr.substring(0, retStr.lastIndexOf(","));
        retStr += "}";
        return retStr;
    }

    /**
     * 十六进制转10进制
     */
    public static int toInt(String str) {
        if (str == null || str.length() < 1) {
            throw new RuntimeException("字符串不合法");
        }

        int sum = 0;
        int n = 0;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(str.length() - 1 - i);
            if (c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F') {
                n = Character.toUpperCase(c) - 64;
            } else if (c >= '0' && c <= '9') {
                n = c - 48;
            } else {
                throw new RuntimeException("字符串不合法");
            }
            sum += n * (1 << 4 * i);
        }
        return sum;
    }


    /**
     * 将json格式的字符串解析成Map对象 <li>
     * json格式：{"name":"admin","retries":"3fff","testname"
     * :"ddd","testretries":"fffffffff"}
     */
    public static HashMap<String, String> jsonToHashMap(String jsonStr) {
        HashMap<String, String> data = new HashMap<String, String>();
        // 将json字符串转换成jsonObject
        JSONObject jsonResult = null;
        try {
            jsonResult = new JSONObject(jsonStr);

            Iterator it = jsonResult.keys();
            // 遍历jsonObject数据，添加到Map对象
            while (it.hasNext()) {
                String key = String.valueOf(it.next());
                String value = (String) jsonResult.get(key);
                data.put(key, value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }


    /**
     * 添加请求到后台提交
     *
     * @param isOverride 是否覆盖原有的请求
     */
    public static void addBackGroundRequest(String requestInterface, String requestPara, int what, boolean isOverride) {
        Realm realm = Realm.getDefaultInstance();
        if (isOverride) { // 覆盖的时候, 删除已有的请求
            RealmResults<BackGroundRequest> backGroundRequests = realm.where(BackGroundRequest.class).equalTo("requestInterface", requestInterface).findAll();
            if(backGroundRequests != null && backGroundRequests.size() > 0){

            }else{
                final BackGroundRequest backGroundRequest = new BackGroundRequest(requestInterface, requestPara, what);
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealm(backGroundRequest);
                    }
                });
            }
        }else {
            // 插入新的请求信息
            final BackGroundRequest backGroundRequest = new BackGroundRequest(requestInterface, requestPara, what);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealm(backGroundRequest);
                }
            });
        }
    }
}
