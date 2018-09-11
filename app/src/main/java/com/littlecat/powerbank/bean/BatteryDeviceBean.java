package com.littlecat.powerbank.bean;

/**
 * Created by hero on 2018/2/28.
 */

public class BatteryDeviceBean {

    /**
     * device : {"slot_count":8,"total":5,"usable":3,"empty":3,"sdcard":1,"status":0}
     * batteries : [{"beta1":{"id":123,"slot":1,"power":100,"voltage":1234,"current":222,"temperature":0," battery_status ":1,"slot_status":1,"sensors":"111111"}}]
     */


    /**
     * slot_count : 8
     * total : 5
     * usable : 3
     * empty : 3
     * sdcard : 1
     * status : 0
     */

    private int slot_count;
    private int total;
    private int usable;
    private int empty;
    private int sdcard;
    private int status;


    public int getSlot_count() {
        return slot_count;
    }

    public void setSlot_count(int slot_count) {
        this.slot_count = slot_count;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getUsable() {
        return usable;
    }

    public void setUsable(int usable) {
        this.usable = usable;
    }

    public int getEmpty() {
        return empty;
    }

    public void setEmpty(int empty) {
        this.empty = empty;
    }

    public int getSdcard() {
        return sdcard;
    }

    public void setSdcard(int sdcard) {
        this.sdcard = sdcard;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
