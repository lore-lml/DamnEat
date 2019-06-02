package com.damn.polito.damneatrestaurant.beans;

import android.support.annotation.NonNull;

public class TimesOfDay implements Comparable<TimesOfDay>{
    private int count;
    private String key;

    public TimesOfDay(String key) {
        this.key = key;
        this.count = 1;
    }

    public void add(){
        count++;
    }

    public int getCount(){
        return count;
    }

    @NonNull
    public String toString(){
        return key;
    }

    public String getKey(){
        return key;
    }

    @Override
    public int compareTo(TimesOfDay o) {
        return o.getCount() - this.count;
    }
}
