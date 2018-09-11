package com.littlecat.powerbank.bean;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/1/24.
 */

public class MachineBean {
    private DeviceBean device;


    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    private String mac;

    public DeviceBean getDeviceBean() {
        return device;
    }

    public void setDeviceBean(DeviceBean deviceBean) {
        this.device = deviceBean;
    }


}
