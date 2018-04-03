package de.hs_kl.blesensor;

import android.bluetooth.le.ScanResult;

public class SensorData
{
    private static final int MANUFACTURER_ID = 0xFFFF;

    private String deviceName;
    private String macAddress;
    private long timestamp;
    private int rssi;
    private short companyID;
    private byte softwareID;
    private byte deviceID;
    private float temperature;
    private float relativeHumidity;
    private float batteryVoltage;

    public SensorData(ScanResult result)
    {
        this.deviceName = result.getDevice().getName();
        this.macAddress = result.getDevice().getAddress();
        this.timestamp = result.getTimestampNanos();
        this.rssi = result.getRssi();

        byte[] rawData = result.getScanRecord().getManufacturerSpecificData(SensorData.MANUFACTURER_ID);
        this.companyID = (short)(rawData[0] << 8 | rawData[1]);
        this.softwareID = rawData[2];
        this.deviceID = rawData[3];
        this.temperature = calculateTemperature(rawData[4], rawData[5]);
        this.relativeHumidity = calculateRelativeHumidity(rawData[6], rawData[7]);
        this.batteryVoltage = calculateBatteryVoltage(rawData[8]);
    }

    public SensorData(String deviceName, String macAddress)
    {
        this.deviceName = deviceName;
        this.macAddress = macAddress;
        this.timestamp = Long.MAX_VALUE;
    }

    private float calculateTemperature(byte b1, byte b2)
    {
        return 175f * ((b1 << 8) + b2) / 65535f - 45;
    }

    private float calculateRelativeHumidity(byte b1, byte b2)
    {
        return 100f * ((b1 << 8) + b2) / 65535f;
    }

    private float calculateBatteryVoltage(byte b)
    {
        return b * 4f / 225f;
    }

    public String getDeviceName()
    {
        return this.deviceName;
    }

    public String getMacAddress()
    {
        return this.macAddress;
    }

    public long getTimestamp()
    {
        return this.timestamp;
    }

    public int getRSSI()
    {
        return this.rssi;
    }

    public byte getDeviceID()
    {
        return this.deviceID;
    }

    public float getTemperature()
    {
        return this.temperature;
    }

    public float getRelativeHumidity()
    {
        return this.relativeHumidity;
    }

    public String toString()
    {
        return "SensorData:\n" +
                "\tMAC-Address: " + this.macAddress + "\n" +
                "\tTimestamp: " + this.timestamp + "\n" +
                "\tRSSI: " + this.rssi + "dBm\n" +
                "\tCompanyID: " + this.companyID + "\n" +
                "\tSoftwareID: " + this.softwareID + "\n" +
                "\tDeviceID: " + this.deviceID + "\n" +
                "\tTemperature: " + this.temperature + "°C\n" +
                "\tRelative Humidity: " + this.relativeHumidity + "%\n" +
                "\tBattery Voltage: " + this.batteryVoltage + "V\n";
    }
}
