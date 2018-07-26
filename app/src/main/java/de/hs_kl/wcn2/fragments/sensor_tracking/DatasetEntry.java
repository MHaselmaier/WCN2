package de.hs_kl.wcn2.fragments.sensor_tracking;

public class DatasetEntry
{
    private byte sensorID;
    private String sensorMACAddress;
    private float temperature;
    private float humidity;
    private String action;
    private long timestamp;

    public DatasetEntry(byte sensorID, String sensorMACAddress, float temperature, float humidity,
                        String action, long timestamp)
    {
        this.sensorID = sensorID;
        this.sensorMACAddress = sensorMACAddress;
        this.temperature = temperature;
        this.humidity = humidity;
        this.action = action;
        this.timestamp = ((timestamp / 1000) / 60) * 100 + (((timestamp / 1000) % 60) * 100) / 60;
    }

    public byte getSensorID()
    {
        return this.sensorID;
    }

    public String getSensorMACAddress()
    {
        return this.sensorMACAddress;
    }

    public float getTemperature()
    {
        return this.temperature;
    }

    public float getHumidity()
    {
        return this.humidity;
    }

    public String getAction()
    {
        return this.action;
    }

    public float getTimestamp()
    {
        return this.timestamp / 100f;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof DatasetEntry)) return false;

        DatasetEntry other = (DatasetEntry)obj;

        return (this.timestamp == other.timestamp) && (this.sensorID == other.sensorID);
    }
}