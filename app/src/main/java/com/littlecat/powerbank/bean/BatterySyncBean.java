package com.littlecat.powerbank.bean;

/**
 * Created by hero on 2018/2/28.
 */

import java.util.Map;

public class BatterySyncBean {
    private BatteryDeviceBean device;
    private Map<String,Object> batteries;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    private String mac;

    public BatteryDeviceBean getDeviceBean() {
        return device;
    }

    public void setDeviceBean(BatteryDeviceBean deviceBean) {
        this.device = deviceBean;
    }

    public Map<String,Object> getBatteryBeanList() {
        return batteries;
    }

    public void setBatteryBeanList( Map<String,Object> batteries) {
        this.batteries = batteries;
    }
}
