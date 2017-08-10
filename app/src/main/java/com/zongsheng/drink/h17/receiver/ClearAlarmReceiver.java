package com.zongsheng.drink.h17.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.background.bean.LogsInfo;
import com.zongsheng.drink.h17.front.bean.MachineFaultRecord;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * 清理本地存储
 * Created by dongxiaofei on 16/9/27.
 * TODO:清理数据库的逻辑，防止数据库无限增大
 */

public class ClearAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.zongsheng.alarm.clear.action")) {
            // 每小时清除一次数据库缓存
           MyApplication.getInstance().clearRealCache();

            // 删除本地的历史数据
            Realm realm = Realm.getDefaultInstance();

            // 删除操作日志
            final RealmResults<LogsInfo> logsInfos =  realm.where(LogsInfo.class).findAll().sort("oprateTime", Sort.DESCENDING);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    if (logsInfos != null && logsInfos.size() > 300) {
                        LogsInfo logsInfo;
                        for (int i = 300; i < logsInfos.size(); i ++) {
                            logsInfo = logsInfos.get(i);
                            logsInfo.deleteFromRealm();
                        }
                    }
                }
            });

            // 删除故障信息
            final RealmResults<MachineFaultRecord> machineFaultRecords =  realm.where(MachineFaultRecord.class).findAll().sort("createTime", Sort.DESCENDING);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    if (machineFaultRecords != null && machineFaultRecords.size() > 0) {
                        MachineFaultRecord machineFaultRecord;
                        for (int i = 0; i < machineFaultRecords.size(); i ++) {
                            machineFaultRecord = machineFaultRecords.get(i);
                            if (machineFaultRecord != null && "1".equals(machineFaultRecord.getIsUploaded())) {
                                machineFaultRecord.deleteFromRealm();
                            }
                        }
                    }
                }
            });
            realm.close();
        }
    }
}
