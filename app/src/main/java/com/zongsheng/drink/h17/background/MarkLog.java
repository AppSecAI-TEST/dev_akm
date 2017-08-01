package com.zongsheng.drink.h17.background;

import com.zongsheng.drink.h17.background.bean.LogsInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;

/**
 * 记录日志
 */
public class MarkLog {

    private static Realm realm;

    public static void markLog(final String whereMark, String level, final String machineSn) {
        realm = Realm.getDefaultInstance();

        SimpleDateFormat sp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final LogsInfo logsInfo = new LogsInfo();
        logsInfo.setLogLevel(level);
        logsInfo.setMachineSn(machineSn);
        logsInfo.setOprateContent(whereMark);
        logsInfo.setOprateTime(sp.format(new Date()));
        logsInfo.setIsUploaded("0");
        if(!realm.isInTransaction()){
            realm.beginTransaction();
        }
        realm.copyToRealm(logsInfo);
        realm.commitTransaction();
    }

}
