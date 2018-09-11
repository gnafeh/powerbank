package com.littlecat.powerbank.bean;

/**
 * Created by hero on 2018/2/25.
 */

public class ReturnBatteryBean {
    private long id;
    private int slot;
    private long power;
    private int temperature;
    private long voltage;
    private long current;

    public long getId(){
        return id;
    }

    public void setId(long bat_id){
        this.id = bat_id;
    }

    public int getSlot(){
        return slot;
    }

    public void setSlot(int slot){
        this.slot = slot;
    }

    public long getPower(){
        return power;
    }

    public void setPower(long power){
        this.power = power;
    }

    public int getTemperature(){
        return temperature;
    }

    public void setTemperature(int temperature){
        this.temperature = temperature;
    }

    public long getVoltage(){
        return voltage;
    }

    public void setVoltage(long voltage){
        this.voltage = voltage;
    }

    public long getCurrent(){
        return current;
    }

    public void setCurrent(long current){
        this.current = current;
    }
}
