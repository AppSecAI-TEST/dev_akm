package com.zongsheng.drink.h17.observable;


import java.util.Observable;
import java.util.Observer;

/**
 * Created by Suchengjian on 2017.3.17.
 */

public class SerialObservable extends Observable {

    private static volatile SerialObservable instance = null;

    public SerialObservable() {
    }
    public static SerialObservable getInstance(){
        if(instance == null){
            synchronized (SerialObservable.class){
                if(instance == null){
                    instance = new SerialObservable();
                }
            }
        }
        return instance;
    }

    public void regist(Observer observer){
        addObserver(observer);
    }

    public void unregist(Observer observer){
        deleteObserver(observer);
    }

    public void notifyChange(Object object){
        setChanged();
        notifyObservers(object);
    }



}
