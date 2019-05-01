package com.damn.polito.damneatdeliver.beans;

import com.damn.polito.commonresources.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class DayOfTheWeek {

    private String day;
    private String[] open;
    private String[] close;
    private boolean isClosed;


    public DayOfTheWeek(String day){
        if(day == null)
            throw new IllegalArgumentException("The day cannot be null");
        this.day = day;
        open = new String[2];
        close = new String[2];
        isClosed = false;
    }

    public DayOfTheWeek(String day, boolean isClosed) {
        this(day);
        this.isClosed = isClosed;
    }

    public DayOfTheWeek(String day, boolean isClosed, String open1, String close1, String open2, String close2){
        this(day,isClosed);

        if(open1 != null && open1.matches(Utility.Regex.TIME))
            open[0] = open1;
        else if(!isClosed)
            throw new IllegalArgumentException("You should set at least a valid time slot");

        if(close1 != null && close1.matches(Utility.Regex.TIME))
            close[0] = close1;
        else if(!isClosed)
            throw new IllegalArgumentException("You should set at least a valid time slot");

        if(open2 != null && open2.matches(Utility.Regex.TIME))
            open[1] = open2;
        else open[1]=null;

        if(close2 != null && close2.matches(Utility.Regex.TIME))
            close[1] = close2;
        else close[1] = null;
    }

    public DayOfTheWeek(JSONObject json){
        this("");
        try {
            day = json.getString("day");
            isClosed = json.getBoolean("closed");
            if(!isClosed){
                open[0] = json.getString("open1");
                close[0] = json.getString("close1");
                if(!open[0].isEmpty() || !close[0].isEmpty()){
                    open[1] = json.getString("open2");
                    if(open[1].isEmpty()) open[1] = null;
                    close[1] = json.getString("close2");
                    if(close[1].isEmpty()) close[1] = null;
                }
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Incorrect Json");
        }
    }

    public String getDay(){
        return day;
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
        return open[0] + "-" + close[0];
    }

    public String getSecondTimeSlot(){
        if(isClosed || open[1] == null || close[1] == null) return null;
        if(open[1].trim().isEmpty()|| close[1].trim().isEmpty()) return null;
        return open[1] + "-" + close[1];
    }

    public void setDay(String day){
        if(day == null)
            throw new IllegalArgumentException("The day cannot be null");
        this.day = day;
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
        close[1] = close2;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(day + ": ");
        if(isClosed)
            sb.append("closed");
        else{
            if(open[0] == null || close[0] == null)
                throw new RuntimeException("You must set the first time slot before set the second!\n");
            sb.append(getFirstTimeSlot()).append(" ");

            String secondSlot = getSecondTimeSlot();
            if(secondSlot != null)
                sb.append(secondSlot);
        }
        return sb.toString();
    }

    public JSONObject toJson(){
        JSONObject json = new JSONObject();
        try {
            json.put("day", day);
            json.put("closed", isClosed);
            if(!isClosed){
                json.put("open1", (open[0] == null || open[0].isEmpty()) ? "" : open[0]);
                json.put("close1", (close[0] == null || close[0].isEmpty()) ? "" : close[0]);
                json.put("open2", (open[1] == null || open[1].isEmpty()) ? "" : open[1]);
                json.put("close2", (close[1] == null || close[1].isEmpty()) ? "" : close[1]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
