package de.hs_kl.blesensor;

public class DatasetEntry
{
    private byte sensorID;
    private float temperature;
    private float humidity;
    private String activity;
    private long timestamp;

    public DatasetEntry(byte sensorID, float temperature, float humidity, String activity, long timestamp)
    {
        this.sensorID = sensorID;
        this.temperature = temperature;
        this.humidity = humidity;
        this.activity = activity;
        this.timestamp = timestamp;
    }

    public byte getSensorID()
    {
        return this.sensorID;
    }

    public float getTemperature()
    {
        return this.temperature;
    }

    public float getHumidity()
    {
        return this.humidity;
    }

    public String getActivity()
    {
        return this.activity;
    }

    public long getTimestamp()
    {
        return this.timestamp;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof DatasetEntry)) return false;

        DatasetEntry other = (DatasetEntry)obj;

        return (this.timestamp == other.timestamp) && (this.sensorID == other.sensorID);
    }
}