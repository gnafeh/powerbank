package com.littlecat.powerbank.bean;

/**
 * Created by hero on 2018/2/25.
 */

public class ReturnInfoBean {

    private ReturnBatteryBean battery;

    public ReturnBatteryBean getBatteryBean(){
        return battery;
    }

    public void setBatteryBean(ReturnBatteryBean bat_bean){
        this.battery = bat_bean;
    }

}
