package de.hs_kl.wcn2.util;

import android.os.Environment;

import java.io.File;

public class Constants
{
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_ENABLE_LOCATION = 2;
    public static final int REQUEST_PERMISSIONS = 3;

    public static final int MANUFACTURER_ID = 0xFFFF;

    public static final float MAX_VOLTAGE = 3.8f;
    public static final float MIN_VOLTAGE = 3.1f;

    public static final float MAX_SIGNAL_STRENGTH = -40;
    public static final float MIN_SIGNAL_STRENGTH = -100;

    public static final String TRACKED_SENSORS_ID = "tracked_sensors_id";
    public static final String TRACKED_SENSORS_MNEMONIC = "tracked_sensors_mnemonic";

    public static final String DEFINED_ACTIONS = "defined_actions";

    public static final String DATA_DIRECTORY_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "WCN2";
    public static final String FILE_PROVIDER_AUTHORITY = "de.hs_kl.fileprovider";
    public static final String MEASUREMENT_DATA_TYPE = "text/plain";

    public static final String MEASUREMENT_FILENAME = "measurement_filename";
    public static final String MEASUREMENT_HEADER = "measurement_header";
    public static final String MEASUREMENT_RATE = "measurement_rate";

    public static final int MEASUREMENT_ID = Integer.MAX_VALUE;
    public static final String MEASUREMENT_CHANNEL_ID = "measurement_channel_id";
    public static final String SENSOR_DATA_CHANNEL_ID = "sensor_data_channel_id";
    public static final String SENSOR_BATTERY_LOW_CHANNEL_ID = "sensor_battery_low_channel_id";

    public static final int UI_UPDATE_INTERVAL = 1000;

    public static final int SENSOR_DATA_TIMEOUT = 10000;

    public static final int BLESCANNER_REPORT_DELAY = 0;

    public enum WCNView
    {
        SENSOR_TRACKING, SEARCH_SENSOR, MANAGE_MEASUREMENT, ACTIONS, ABOUT
    }
}
