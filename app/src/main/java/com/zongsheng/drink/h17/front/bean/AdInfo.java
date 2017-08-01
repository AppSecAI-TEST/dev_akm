package com.zongsheng.drink.h17.front.bean;

import android.graphics.Bitmap;

/**
 * Created by xunku on 16/8/30.
 */

public class AdInfo {

    /** 广告类型 1:图片 2:视频 */
    private String adType;

    /** 广告路径 */
    private String adPath;

    /** 图片bitmap */
    private Bitmap imageBit;

    public Bitmap getImageBit() {
        return imageBit;
    }

    public void setImageBit(Bitmap imageBit) {
        this.imageBit = imageBit;
    }

    public String getAdType() {
        return adType;
    }

    public void setAdType(String adType) {
        this.adType = adType;
    }

    public String getAdPath() {
        return adPath;
    }

    public void setAdPath(String adPath) {
        this.adPath = adPath;
    }
}
