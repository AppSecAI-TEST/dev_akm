package com.zongsheng.drink.h17.Model;

import android.util.Log;

import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.background.bean.BindDesk;
import com.zongsheng.drink.h17.background.bean.BindGeZi;
import com.zongsheng.drink.h17.common.SysConfig;
import com.zongsheng.drink.h17.front.bean.GoodsInfo;
import com.zongsheng.drink.h17.front.bean.HelpInfo;
import com.zongsheng.drink.h17.front.bean.QueHuoRecord;
import com.zongsheng.drink.h17.front.bean.ServerHelpInfo;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by Suchengjian on 2017.3.22.
 */

public class LoadingModelImpl implements ILoadingModel {
    private Realm realm;

    public LoadingModelImpl() {
        this.realm = Realm.getDefaultInstance();
    }

    @Override
    public void cancel() {
        if (realm != null) {
            realm.close();
        }
    }

    @Override
    public void delHelpInfo2Realm() {
        final RealmResults<HelpInfo> results = realm.where(HelpInfo.class).findAll();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                results.deleteAllFromRealm();
            }
        });
    }

    @Override
    public void addHelpInfos2Realm(final List<ServerHelpInfo> helpinfos) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                int i = 0;
                for (ServerHelpInfo serverHelpInfo : helpinfos) {
                    HelpInfo helpInfo = new HelpInfo();
                    helpInfo.setHelpID(serverHelpInfo.getHelpId());
                    helpInfo.setHelpType(serverHelpInfo.getQuestionType());
                    helpInfo.setQuestion(serverHelpInfo.getQuestionDesc());
                    helpInfo.setReason(serverHelpInfo.getQuestionReason());
                    helpInfo.setAnswer(serverHelpInfo.getResolvent());
                    helpInfo.setHelpIntro(serverHelpInfo.getResolvent());
                    helpInfo.setShowSort(i);
                    realm.copyToRealm(helpInfo);
                    i++;
                }
            }
        });
    }

    @Override
    public void addBindGeZi2Realm(final List<BindGeZi> bindgezi) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(bindgezi);
            }
        });
    }


    private void addGeZiQuehuo2Realm(String machine_Sn) {
        StringBuffer quehuoRoads = new StringBuffer();
        RealmResults<GoodsInfo> cabinetGoods = realm.where(GoodsInfo.class).equalTo("goodsBelong", "2").equalTo("machineID", machine_Sn).findAll();
        if (cabinetGoods != null && cabinetGoods.size() > 0) {
            for (GoodsInfo goodsInfo : cabinetGoods) {
                if (goodsInfo.getGoodsCode() != null && !"".equals(goodsInfo.getGoodsCode())) {
                    // 库存为0
                    if (Integer.parseInt(goodsInfo.getKuCun()) == 0) {
                        quehuoRoads.append(goodsInfo.getRoad_no() + ";");
                    }
                }
            }
        }
        String quehuoRoad = "";
        if (quehuoRoads.toString().endsWith(";")) {
            quehuoRoad = quehuoRoads.substring(0, quehuoRoads.length() - 1);
        }
        final QueHuoRecord queHuoRecord = new QueHuoRecord();
        if ("".equals(quehuoRoad)) {
            queHuoRecord.setIsQueHuo("0");
        } else {
            queHuoRecord.setIsQueHuo("1");
        }
        queHuoRecord.setMachineSn(machine_Sn);
        queHuoRecord.setRoad_no(quehuoRoad);
        queHuoRecord.setCreateTime(new Date().getTime());
        queHuoRecord.setIsUploaded("0");

        // 取得最后一条故障信息, 如果相同就不要再去处理了
        RealmResults<QueHuoRecord> results = realm.where(QueHuoRecord.class).equalTo("machineSn", machine_Sn).findAll();
        results = results.sort("createTime", Sort.DESCENDING);
        if (results != null && results.size() > 0) {
            QueHuoRecord record = results.first();
            if (record != null) {
                if (record.getIsQueHuo().equals(queHuoRecord.getIsQueHuo())
                        && record.getMachineSn().equals(queHuoRecord.getMachineSn())
                        && record.getRoad_no().equals(queHuoRecord.getRoad_no()) && "0".equals(record.getIsUploaded())) {
                    return;
                }
            }
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(queHuoRecord);
            }
        });
    }

    @Override
    public void updateLocalKucun(String road_no) {
        RealmResults<GoodsInfo> goodsInfos = realm.where(GoodsInfo.class).equalTo("goodsBelong", "1").equalTo("road_no", Integer.parseInt(road_no)).findAll();
        final GoodsInfo goodsInfo = goodsInfos.where().findFirst();
        if (goodsInfo != null && goodsInfo.getKuCun() != null && !"".equals(goodsInfo.getKuCun())) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    if (Integer.parseInt(goodsInfo.getKuCun()) >= 2) {
                        goodsInfo.setKuCun(String.valueOf(Integer.parseInt(goodsInfo.getKuCun()) - 1));
                    }
                    if (goodsInfo.getOnlineKuCun() >= 2) {
                        goodsInfo.setOnlineKuCun(goodsInfo.getOnlineKuCun() - 1);
                    }
                }
            });
        }
    }

    @Override
    public void getGoods4Realm() {
        final RealmResults<GoodsInfo> result = realm.where(GoodsInfo.class).equalTo("goodsBelong", "1").findAll().sort("road_no", Sort.ASCENDING);
        final Map<String, String> checkMap = new HashMap<>();
        if (result.size() > 0) {
            Log.i(TAG, "取得本地保存产品数据:" + result.size());
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (GoodsInfo goodsInfo : result) {
                        if (goodsInfo == null || goodsInfo.getGoodsID() == null || "".equals(goodsInfo.getGoodsID()) || "0".equals(goodsInfo.getGoodsID())) {
                            continue;
                        }
                        goodsInfo.setPrice((int) Double.parseDouble(goodsInfo.getPrice()) + "");
                        if (checkMap.containsKey(goodsInfo.getGoodsCode())) {
                            continue;
                        }
                        checkMap.put(goodsInfo.getGoodsCode(), "");
                        MyApplication.getInstance().getGoodsInfos().add(goodsInfo);
                    }
                }
            });
        }
    }

    @Override
    public void getGeZiInfo() {
        RealmResults<BindGeZi> cabinetInfos = realm.where(BindGeZi.class).findAll();
        cabinetInfos = cabinetInfos.sort("createTime", Sort.ASCENDING);
        MyApplication.getInstance().setBindGeZis(realm.copyFromRealm(cabinetInfos));
        Log.e("失联格子柜数:", MyApplication.getInstance().getConnetFailGeziList().size() + "");
        if (cabinetInfos.size() > 0) {
            // 格子柜的map 机器编码, 箱号
            Map<String, Integer> bindGeziMap = new HashMap<>();
            int i = 1;
            for (BindGeZi bindGeZi : MyApplication.getInstance().getBindGeZis()) {
                boolean isFail = false;
                for (int boxNum : MyApplication.getInstance().getConnetFailGeziList()) {
                    if (boxNum == i + 1) {
                        isFail = true;
                        break;
                    }
                }
                if (!isFail) {
                    bindGeziMap.put(bindGeZi.getMachineSn(), i + 1);
                }
                i++;
            }
            // 加载本地数据库中的商品信息
            RealmResults<GoodsInfo> results = realm.where(GoodsInfo.class).equalTo("goodsBelong", "2").findAll();
            results = results.sort("road_no", Sort.ASCENDING);
            List<GoodsInfo> cabinetGoods = results;
            Map<String, Integer> checkMap1 = new HashMap<>();
            if (cabinetGoods.size() > 0) {
                Log.i(TAG, "取得本地保存格子柜产品数据:" + cabinetGoods.size());
                if (!realm.isInTransaction()) {
                    realm.beginTransaction();
                }
                for (GoodsInfo goodsInfo : cabinetGoods) {
                    if (goodsInfo == null || goodsInfo.getGoodsID() == null || "".equals(goodsInfo.getGoodsID()) || "0".equals(goodsInfo.getGoodsID())) {
                        continue;
                    }
                    if (!bindGeziMap.containsKey(goodsInfo.getMachineID())) {
                        continue;
                    }
                    // 无此货道的时候 continue
                    if (MyApplication.getInstance().getGeziRoadListMap().containsKey(bindGeziMap.get(goodsInfo.getMachineID()))) {
                        if (!MyApplication.getInstance().getGeziRoadListMap().get(bindGeziMap.get(goodsInfo.getMachineID())).contains(goodsInfo.getRoad_no())) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    // format价格
                    goodsInfo.setPrice((int) Double.parseDouble(goodsInfo.getPrice()) + "");
                    int kucun = 0;
                    if (MyApplication.getInstance().getAokemaGeZiKuCunMap().containsKey(bindGeziMap.get(goodsInfo.getMachineID()))) {
                        // 这里直接用货道号是不对的。 getAokemaGeZiKuCunMap里面对应的是1~80  这里的road_no 是真实的
                        int road_no = goodsInfo.getRoad_no();
                        int index = 0;
                        if (road_no > 10) {
                            index = road_no - (((int) (road_no * 0.1)) * 2);
                        } else {
                            index = road_no;
                        }
                        if (MyApplication.getInstance().getAokemaGeZiKuCunMap().get(bindGeziMap.get(goodsInfo.getMachineID())).containsKey(index)) {
                            if ("0".equals(MyApplication.getInstance().getAokemaGeZiKuCunMap().get(bindGeziMap.get(goodsInfo.getMachineID())).get(index))) {
                                // 有货的时候
                                kucun = 1;
                            }
                        }
                    }
                    goodsInfo.setKuCun(String.valueOf(kucun));
                    if (checkMap1.containsKey(goodsInfo.getGoodsID())) {
                        MyApplication.getInstance().getCabinetTotalGoods().add(goodsInfo);
                        checkMap1.put(goodsInfo.getGoodsID(), checkMap1.get(goodsInfo.getGoodsID()) + kucun);
                        continue;
                    }
                    MyApplication.getInstance().getCabinetTotalGoods().add(goodsInfo);
                    checkMap1.put(goodsInfo.getGoodsID(), kucun);
                }
                for (String str : checkMap1.keySet()) {
                    for (GoodsInfo goodsInfo : cabinetGoods) {
                        if (checkMap1.get(str) == 0) {
                            if (str.equals(goodsInfo.getGoodsID())) {
                                MyApplication.getInstance().getCabinetGoods().add(goodsInfo);
                                break;
                            }
                        } else if (checkMap1.get(str) > 0) {
                            if (str.equals(goodsInfo.getGoodsID()) && Integer.parseInt(goodsInfo.getKuCun()) > 0) {
                                MyApplication.getInstance().getCabinetGoods().add(goodsInfo);
                                break;
                            }
                        }
                    }
                }
                // 计算商品库存
                for (GoodsInfo goodsInfo : MyApplication.getInstance().getCabinetGoods()) {
                    if (checkMap1.get(goodsInfo.getGoodsID()) > 0) {
                        goodsInfo.setIsSoldOut(SysConfig.NOTSOLDOUT_FLAG); //0:未空 1:售空
                    } else {
                        goodsInfo.setIsSoldOut(SysConfig.ISSOLDOUT_FLAG);
                    }
                }
                realm.commitTransaction();
            }
            // 处理以勒格子柜的缺货信息
            for (BindGeZi bindGeZi : MyApplication.getInstance().getBindGeZis()) {
                addGeZiQuehuo2Realm(bindGeZi.getMachineSn());
            }
        }
    }

    @Override
    public boolean isBindGeZiInfo() {
        RealmResults<BindGeZi> cabinetInfos = realm.where(BindGeZi.class).findAll();
        return cabinetInfos.size() > 0;
    }


    @Override
    public void getDeskGoods4Realm() {
        final RealmResults<GoodsInfo> result = realm.where(GoodsInfo.class).equalTo("goodsBelong", "3").findAll().sort("road_no", Sort.ASCENDING);
        final Map<String, String> checkMap = new HashMap<>();
        if (result.size() > 0) {
            Log.i(TAG, "取得本地副柜保存产品数据:" + result.size());
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (GoodsInfo goodsInfo : result) {
                        if (goodsInfo == null || goodsInfo.getGoodsID() == null || "".equals(goodsInfo.getGoodsID()) || "0".equals(goodsInfo.getGoodsID())) {
                            continue;
                        }
                        goodsInfo.setPrice((int) Double.parseDouble(goodsInfo.getPrice()) + "");
//                        if (checkMap.containsKey(goodsInfo.getGoodsCode())) {
//                            continue;
//                        }
//                        checkMap.put(goodsInfo.getGoodsCode(), "");
                        MyApplication.getInstance().getDeskGoodsInfo().put(goodsInfo.getRoad_no(), goodsInfo);
                        Log.i("本地副柜保存产品数据:", goodsInfo.getRoad_no() + goodsInfo.getGoodsName());
                    }
                }
            });
        }
    }

    @Override
    public void getDeskInfo() {
        RealmResults<BindDesk> deskInfos = realm.where(BindDesk.class).findAll().sort("createTime", Sort.ASCENDING);
        if (deskInfos != null && deskInfos.size() > 0) {
            MyApplication.getInstance().setBindDeskList(realm.copyFromRealm(deskInfos));
        }
    }
}
