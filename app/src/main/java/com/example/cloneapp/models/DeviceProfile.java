package com.example.cloneapp.models;

import java.io.Serializable;

/**
 * Model representing virtual device identity parameters for spoofing per clone.
 */
public class DeviceProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String androidId;
    private String imei;
    private String model;
    private String manufacturer;
    private String wifiName;
    private String bluetoothName;

    public DeviceProfile() {
    }

    public DeviceProfile(String androidId, String imei, String model, String manufacturer, String wifiName, String bluetoothName) {
        this.androidId = androidId;
        this.imei = imei;
        this.model = model;
        this.manufacturer = manufacturer;
        this.wifiName = wifiName;
        this.bluetoothName = bluetoothName;
    }

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public String getBluetoothName() {
        return bluetoothName;
    }

    public void setBluetoothName(String bluetoothName) {
        this.bluetoothName = bluetoothName;
    }
}
