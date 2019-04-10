package com.damn.polito.damneatrestaurant.beans;

public class DayOfTheWeek {

    private String day;
    private String[] open;
    private String[] close;
    private boolean isClosed;


    public DayOfTheWeek(){
        open = new String[2];
        close = new String[2];
        isClosed = true;
    }

    public DayOfTheWeek(String day, boolean isClosed){
        this();
        if(day == null)
            throw new IllegalArgumentException("The day cannot be null");
        this.day = day;
        this.isClosed = isClosed;
    }

    public DayOfTheWeek(String day, boolean isClosed, String open1, String close1, String open2, String close2){
        this(day, isClosed);

        if(!isClosed)
            if(open1 == null || close1 == null)
                throw new IllegalArgumentException("You must specify at least a time slot!\n");

        open[0] = open1;
        open[2] = open2;
        close[0] = close1;
        close[1] = close2;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public String getFirstOpenTime(){
        return open[0];
    }

    public String getSecondOpenTime(){
        return open[1];
    }

    public String getFirstCloseTime(){
        return close[0];
    }

    public String getSecondCloseTime(){
        return close[1];
    }

    public String getFirstTimeSlot(){
        if(isClosed || open[0] == null || close[0] == null) return null;
        if(open[0].trim().isEmpty()|| close[0].trim().isEmpty()) return null;
        return open[0] + " - " + close[0];
    }

    public String getSecondTimeSlot(){
        if(isClosed || open[1] == null || close[1] == null) return null;
        if(open[1].trim().isEmpty()|| close[1].trim().isEmpty()) return null;
        return open[1] + "-" + close[1];
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public void setFirstTimeSlot(String open1, String close1){
        if(open1 == null || close1 == null)
            throw new IllegalArgumentException("You cannot set a null String\n");
        open[0] = open1;
        close[0] = close1;
    }


    public void setSecondTimeSlot(String open2, String close2){
        if(open2 == null || close2 == null)
            throw new IllegalArgumentException("You cannot set a null String\n");
        if(open[0] == null || close[0] == null)
            throw new RuntimeException("You must set the first time slot before set the second!\n");
        open[1] = open2;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(day + " - ");
        if(isClosed)
            sb.append("closed");
        else{
            if(open[0] == null || close[0] == null)
                throw new RuntimeException("You must set the first time slot before set the second!\n");
            sb.append(getFirstOpenTime() +" ");

            String secondSlot = getSecondTimeSlot();
            if(secondSlot != null)
                sb.append(secondSlot);
        }
        return sb.toString();
    }
}
