package de.hs_kl.blesensor.ble_scanner;

import android.bluetooth.le.ScanResult;

import de.hs_kl.blesensor.util.Constants;

public class SensorData
{
    private String deviceName;
    private String macAddress;
    private long timestamp;
    private int rssi;
    private byte softwareID;
    private byte deviceID;
    private float temperature;
    private float relativeHumidity;
    private float batteryVoltage;

    public SensorData(ScanResult result)
    {
        this.deviceName = result.getScanRecord().getDeviceName();
        this.macAddress = result.getDevice().getAddress();
        this.timestamp = (System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime())
                            + result.getTimestampNanos() / 1_000_000;
        this.rssi = result.getRssi();

        byte[] rawData = result.getScanRecord().getManufacturerSpecificData(Constants.MANUFACTURER_ID);
        if (null != rawData && 7 == rawData.length)
        {
            this.softwareID = rawData[0];
            this.deviceID = rawData[1];
            this.temperature = calculateTemperature(rawData[2], rawData[3]);
            this.relativeHumidity = calculateRelativeHumidity(rawData[4], rawData[5]);
            this.batteryVoltage = calculateBatteryVoltage(rawData[6]);
        }
    }

    public SensorData(byte deviceID, String macAddress)
    {
        this.deviceID = deviceID;
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
        return (b & 0xFF) * 4f / 225f;
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

    public float getBatteryVoltage()
    {
        return batteryVoltage;
    }

    public String toString()
    {
        return "SensorData:\n" +
                "\tMAC-Address: " + this.macAddress + "\n" +
                "\tTimestamp: " + this.timestamp + "\n" +
                "\tRSSI: " + this.rssi + "dBm\n" +
                "\tSoftwareID: " + this.softwareID + "\n" +
                "\tDeviceID: " + this.deviceID + "\n" +
                "\tTemperature: " + this.temperature + "°C\n" +
                "\tRelative Humidity: " + this.relativeHumidity + "%\n" +
                "\tBattery Voltage: " + this.batteryVoltage + "V\n";
    }
}