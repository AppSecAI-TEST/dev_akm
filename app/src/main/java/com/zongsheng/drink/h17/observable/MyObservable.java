package com.zongsheng.drink.h17.observable;


import java.util.Observable;
import java.util.Observer;

/**
 * Created by Suchengjian on 2017.3.17.
 */

public class MyObservable extends Observable {

    private static volatile MyObservable instance = null;

    public MyObservable() {
    }
    public static MyObservable getInstance(){
        if(instance == null){
            synchronized (MyObservable.class){
                if(instance == null){
                    instance = new MyObservable();
                }
            }
        }
        return instance;
    }

    public void registObserver(Observer observer){
        addObserver(observer);
    }

    public void unregistObserver(Observer observer){
        deleteObserver(observer);
    }

    public void notifyChange(){
        setChanged();
        notifyObservers();
    }

    public void notifyChange2(Object object){
        setChanged();
        notifyObservers(object);
    }

}
