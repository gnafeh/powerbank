package com.littlecat.powerbank.bean;

/**
 * Created by Administrator on 2018/1/24.
 */

public class BatteryBean {

    /**
     * battery : {"id":123,"slot":1,"power":100,"voltage":1234,"current":222,"temperature":0," battery_status ":1,"slot_status":1,"sensors":"111111"}
     */

    private BatteryBean battery;

        /**
         * id : 123
         * slot : 1
         * power : 100
         * voltage : 1234
         * current : 222
         * temperature : 0
         *  battery_status  : 1
         * slot_status : 1
         * sensors : 111111
         */

    private long id;
    private int slot;
    private int power;
    private int voltage;
    private int current;
    private int temperature;
    private int battery_status;
    private int slot_status;
    private String sensors;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getVoltage() {
        return voltage;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getBattery_status() {
        return battery_status;
    }

    public void setBattery_status(int battery_status) {
        this.battery_status = battery_status;
    }

    public int getSlot_status() {
        return temperature;
    }

    public void setSlot_status(int slot_status) {
        this.slot_status = slot_status;
    }

    public String getSensors() {
        return sensors;
    }

    public void setSensors(String sensors) {
        this.sensors = sensors;
    }


    public void setBatteryInfo(int id, int slot, int power, int voltage, int current, int temperature, int battery_status, int slot_status, String sensors) {
        setId(id);
        setSlot(slot);
        setPower(power);
        setVoltage(voltage);
        setCurrent(current);
        setTemperature(temperature);
        setBattery_status(battery_status);
        setSlot_status(slot_status);
        setSensors(sensors);
    }
}
