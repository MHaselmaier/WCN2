package de.hs_kl.wcn2_sensors;

import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class SensorData
{
    private String mnemonic;
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
        this.macAddress = result.getDevice().getAddress();
        this.timestamp = (System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime())
                            + result.getTimestampNanos() / 1_000_000;
        this.rssi = result.getRssi();

        ScanRecord record = result.getScanRecord();
        if (null != record)
        {
            byte[] rawData = record.getManufacturerSpecificData(SensorConstants.MANUFACTURER_ID);
            if (null != rawData && 7 == rawData.length)
            {
                this.softwareID = rawData[0];
                this.sensorID = rawData[1];
                this.temperature = calculateTemperature(rawData[2], rawData[3]);
                this.relativeHumidity = calculateRelativeHumidity(rawData[4], rawData[5]);
                this.batteryVoltage = calculateBatteryVoltage(rawData[6]);
            }
        }
    }

    public SensorData(byte sensorID, String mnemonic, String macAddress)
    {
        this.sensorID = sensorID;
        this.mnemonic = mnemonic;
        this.macAddress = macAddress;
        this.timestamp = Long.MAX_VALUE;
    }

    private float calculateTemperature(byte b1, byte b2)
    {
        return 175f * (((b1 & 0xFF) << 8) + (b2 & 0xFF)) / 65535f - 45;
    }

    private float calculateRelativeHumidity(byte b1, byte b2)
    {
        return 100f * (((b1 & 0xFF) << 8) + (b2 & 0xFF)) / 65535f;
    }

    private float calculateBatteryVoltage(byte b)
    {
        return (b & 0xFF) * 4f / 225f;
    }

    public String getMnemonic()
    {
        return this.mnemonic;
    }

    public String getMacAddress()
    {
        return this.macAddress;
    }

    public long getTimestamp()
    {
        return this.timestamp;
    }

    public Drawable getSignalStrengthDrawable(Resources resources)
    {
        long timeSince = System.currentTimeMillis() - this.timestamp;
        if (Long.MAX_VALUE == this.timestamp || 3000 < timeSince)
        {
            return resources.getDrawable(R.drawable.ic_signal_0);
        }

        float signalStrength = Math.max(Math.min(this.rssi, SensorConstants.MAX_SIGNAL_STRENGTH), SensorConstants.MIN_SIGNAL_STRENGTH);
        float percentage = (signalStrength - SensorConstants.MIN_SIGNAL_STRENGTH) / (SensorConstants.MAX_SIGNAL_STRENGTH - SensorConstants.MIN_SIGNAL_STRENGTH);

        if (.25 >= percentage)
        {
            return resources.getDrawable(R.drawable.ic_signal_25);
        }
        else if (.5 >= percentage)
        {
            return resources.getDrawable(R.drawable.ic_signal_50);
        }
        else if (.75 >= percentage)
        {
            return resources.getDrawable(R.drawable.ic_signal_75);
        }
        else
        {
            return resources.getDrawable(R.drawable.ic_signal_100);
        }
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

    public boolean isBatteryLow()
    {
        if (isTimedOut()) return false;

        float voltage = Math.max(Math.min(this.batteryVoltage, SensorConstants.MAX_VOLTAGE), SensorConstants.MIN_VOLTAGE);
        float percentage = (voltage - SensorConstants.MIN_VOLTAGE) / (SensorConstants.MAX_VOLTAGE - SensorConstants.MIN_VOLTAGE);

        return (.2 > percentage);
    }

    public Drawable getBatteryLevelDrawable(Resources resources)
    {
        float voltage = Math.max(Math.min(this.batteryVoltage, SensorConstants.MAX_VOLTAGE), SensorConstants.MIN_VOLTAGE);
        float percentage = (voltage - SensorConstants.MIN_VOLTAGE) / (SensorConstants.MAX_VOLTAGE - SensorConstants.MIN_VOLTAGE);

        long timeSince = System.currentTimeMillis() - this.timestamp;
        if (.2 > percentage || Long.MAX_VALUE == this.timestamp || 3000 < timeSince)
        {
            return resources.getDrawable(R.drawable.ic_battery_almost_empty);
        }
        else if (.3 > percentage)
        {
            return resources.getDrawable(R.drawable.ic_battery_20);
        }
        else if (.5 > percentage)
        {
            return resources.getDrawable(R.drawable.ic_battery_30);
        }
        else if (.6 > percentage)
        {
            return resources.getDrawable(R.drawable.ic_battery_50);
        }
        else if (.8 > percentage)
        {
            return resources.getDrawable(R.drawable.ic_battery_60);
        }
        else if (.9 > percentage)
        {
            return resources.getDrawable(R.drawable.ic_battery_80);
        }
        else if (.95 > percentage)
        {
            return resources.getDrawable(R.drawable.ic_battery_90);
        }
        else
        {
            return resources.getDrawable(R.drawable.ic_battery_full);
        }
    }

    public void setMnemonic(String mnemonic)
    {
        this.mnemonic = mnemonic;
    }

    public boolean isTimedOut()
    {
        if (Long.MAX_VALUE == this.timestamp) return true;

        return (SensorConstants.SENSOR_DATA_TIMEOUT < System.currentTimeMillis() - this.timestamp);
    }

    public String toString()
    {
        return "SensorData:\n" +
                "\tMnemonic: " + this.mnemonic + "\n" +
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
