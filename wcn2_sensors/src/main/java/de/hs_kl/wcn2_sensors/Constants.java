package de.hs_kl.wcn2_sensors;

class Constants
{
    static final int REQUEST_ENABLE_BT = 1;
    static final int REQUEST_ENABLE_LOCATION = 2;

    static final int REQUEST_LOCATION_PERMISSION = 1;

    static final int MANUFACTURER_ID = 0xFFFF;

    static final float MAX_SIGNAL_STRENGTH = -40f;
    static final float MIN_SIGNAL_STRENGTH = -100f;
    static final float MAX_VOLTAGE = 3.8f;
    static final float MIN_VOLTAGE = 3.1f;

    static final int BLESCANNER_REPORT_DELAY = 0;
    static final int SENSOR_DATA_TIMEOUT = 10000;
}
