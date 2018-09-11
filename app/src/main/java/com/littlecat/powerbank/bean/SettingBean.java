package com.littlecat.powerbank.bean;

/**
 * Created by Administrator on 2018/1/27.
 */

public class SettingBean {
    private String sync_interval;
    private String adv_url;
    private String app_package;
    private String app_url;
    private String app_version;
    private String app_start_class;
    private String device_id;
    private String server_time;
    private String time_interval;
    private String ip;

    public String getSync_interval() {
        return sync_interval;
    }

    public void setSync_interval(String sync_interval) {
        this.sync_interval = sync_interval;
    }

    public String getAdv_url() {
        return adv_url;
    }

    public void setAdv_url(String adv_url) {
        this.adv_url = adv_url;
    }

    public String getApp_package() {
        return app_package;
    }

    public void setApp_package(String app_package) {
        this.app_package = app_package;
    }

    public String getApp_url() {
        return app_url;
    }

    public void setApp_url(String app_url) {
        this.app_url = app_url;
    }

    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }

    public String getApp_start_class() {
        return app_start_class;
    }

    public void setApp_start_class(String app_start_class) {
        this.app_start_class = app_start_class;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getServer_time() {
        return server_time;
    }

    public void setServer_time(String server_time) {
        this.server_time = server_time;
    }

    public String getTime_interval() {
        return time_interval;
    }

    public void setTime_interval(String time_interval) {
        this.time_interval = time_interval;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    private String port;

}
