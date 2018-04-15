package com.example.android.ginaweatherapp.data;

import android.provider.BaseColumns;

public class WeatherRecord {

    public class WeatherEntry implements BaseColumns {
        public static final String DAY_NUMBER = "dt";
        public static final String MINIMUM_TEMPERATURE = "temp_min";
        public static final String MAXIMUM_TEMPERATURE = "temp_max";
        public static final String AVERAGE_HUMIDITY = "ave_humidity";
        public static final String TOTAL_HUMIDITY = "total_humidity";
    }

}
