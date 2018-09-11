package com.littlecat.powerbank.bean;

/**
 * Created by Administrator on 2018/1/24.
 */

public class DeviceBean {
    /**
     * soft_ver : 1
     * device_ver : 1
     * push_id : 1
     */

    private String soft_ver;
    private String device_ver;
    private String push_id;

    public String getSoft_ver() {
        return soft_ver;
    }

    public void setSoft_ver(String soft_ver) {
        this.soft_ver = soft_ver;
    }

    public String getDevice_ver() {
        return device_ver;
    }

    public void setDevice_ver(String device_ver) {
        this.device_ver = device_ver;
    }

    public String getPush_id() {
        return push_id;
    }

    public void setPush_id(String push_id) {
        this.push_id = push_id;
    }
}

