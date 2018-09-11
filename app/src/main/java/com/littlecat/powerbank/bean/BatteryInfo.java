package com.littlecat.powerbank.bean;

import com.littlecat.powerbank.util.Constant;

/**
 * Created by Administrator on 2018/2/5.
 */

public class BatteryInfo {
    private int bat_voltage;
    private int voltage_percent;
    private int temperature;
    private String sensor_str;
    private int status_communication;
    public int getTemperature(){
        return temperature;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public byte getSensors() {
        return sensors;
    }

    public void setSensors(byte sensors) {
        this.sensors = sensors;
        int value_int = sensors & 0xFF;
        value_int += 0x100;
        sensor_str = Integer.toBinaryString(value_int).substring(1);
    }

    public String getSensor_str(){
        return sensor_str;
    }

    public long getBat_id() {
        return bat_id;
    }

    public void setBat_id(long bat_id) {
        this.bat_id = bat_id;
    }

    public int getEmpty() {
        return empty;
    }

    public void setEmpty(int empty) {
        this.empty = empty;
    }

    public int getIs_locked() {
        return is_locked;
    }

    public void setIs_locked(int is_locked) {
        this.is_locked = is_locked;
    }

    public int getError_count() {
        return error_count;
    }

    public void setError_count(int error_count) {
        this.error_count = error_count;
    }

    public int getStatus_error_count() {
        return status_error_count;
    }

    public void setStatus_error_count(int status_error_count) {
        this.status_error_count = status_error_count;
    }

    private int status;//slot_status
    private byte sensors;
    private long bat_id;
    private int empty;
    private int is_locked;
    private int error_count;
    private int status_error_count;
    private byte bat_status;
    private long borrow_time = 0;
    private int borrow_status = 0;

    public void setBat_voltage(int bat_voltage) {
        this.bat_voltage = bat_voltage;
		if(bat_voltage < Constant.LOW_VOLTAGE)
		{
			voltage_percent = 0;
		}
		else if(bat_voltage > Constant.HIGH_VOLTAGE)
		{
			voltage_percent = 100;
		}
		else
		{
			voltage_percent = ((bat_voltage- Constant.LOW_VOLTAGE) * 100)/(Constant.HIGH_VOLTAGE - Constant.LOW_VOLTAGE);
		}
    }

    public int getBat_voltage()
    {
        return bat_voltage;
    }

    public int getBat_percent(){

        return voltage_percent;
    }

    public byte getBat_status() {
        return bat_status;
    }

    public int getBat_switch(){
        return (bat_status>>7) & 0x1;
    }

    public int getBat_temperature(){
        return temperature;
    }

    public int getBat_tamper(){
        return (bat_status>>5) & 0x1;
    }

    public int getBat_lock5v(){
        return (bat_status>>4) & 0x1;
    }

    public void setBat_status(byte bat_status) {
        this.bat_status = bat_status;
        if((Constant.BAT_STATUS_TEMPERATURE & bat_status) == Constant.BAT_STATUS_TEMPERATURE)
        {
            this.temperature = 1;
        }
        else
        {
            this.temperature = 0;
        }
    }

    public long getBorrow_time() {
        return borrow_time;
    }

    public void setBorrow_time(long borrow_time) {
        this.borrow_time = borrow_time;
    }

	public int getBorrow_status(){
		return borrow_status;
	}

	public void setBorrow_status(int status){
		this.borrow_status = status;
	}

    public int getStatus_communication() {
        return status_communication;
    }

    public void setStatus_communication(int status_communication) {
        this.status_communication = status_communication;
    }
}
