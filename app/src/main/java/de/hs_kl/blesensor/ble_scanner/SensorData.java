package de.hs_kl.blesensor.ble_scanner;

import android.bluetooth.le.ScanResult;

import de.hs_kl.blesensor.util.Constants;

public class SensorData
{
    private String sensorName;
    private String macAddress;
    private long timestamp;
    private int rssi;
    private byte softwareID;
    private byte sensorID;
    private float temperature;
    private float relativeHumidity;
    private float batteryVoltage;

    public SensorData(ScanResult result)
    {
        this.sensorName = result.getScanRecord().getDeviceName();
        this.macAddress = result.getDevice().getAddress();
        this.timestamp = (System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime())
                            + result.getTimestampNanos() / 1_000_000;
        this.rssi = result.getRssi();

        byte[] rawData = result.getScanRecord().getManufacturerSpecificData(Constants.MANUFACTURER_ID);
        if (null != rawData && 7 == rawData.length)
        {
            this.softwareID = rawData[0];
            this.sensorID = rawData[1];
            this.temperature = calculateTemperature(rawData[2], rawData[3]);
            this.relativeHumidity = calculateRelativeHumidity(rawData[4], rawData[5]);
            this.batteryVoltage = calculateBatteryVoltage(rawData[6]);
        }
    }

    public SensorData(byte sensorID, String macAddress)
    {
        this.sensorID = sensorID;
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

    public String getSensorName()
    {
        return this.sensorName;
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

    public byte getSensorID()
    {
        return this.sensorID;
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
                "\tSensorID: " + this.sensorID + "\n" +
                "\tTemperature: " + this.temperature + "°C\n" +
                "\tRelative Humidity: " + this.relativeHumidity + "%\n" +
                "\tBattery Voltage: " + this.batteryVoltage + "V\n";
    }
}
