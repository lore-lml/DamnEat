package com.damn.polito.damneatrestaurant;

class TimesOfDay {
    private int hour;
    private int minutes;
    private int count;

    public TimesOfDay(int hour, int minutes) {
        this.hour = hour;
        this.minutes = minutes;
        this.count = 1;
    }

    public void add(){
        count++;
    }

    public int getCount(){
        return count;
    }
}
