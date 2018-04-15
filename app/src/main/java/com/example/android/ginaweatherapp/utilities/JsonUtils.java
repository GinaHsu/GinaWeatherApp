package com.example.android.ginaweatherapp.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.android.ginaweatherapp.utilities.NetworkUtils;
import com.example.android.ginaweatherapp.data.WeatherRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public class JsonUtils extends WeatherRecord {

    private static final String TAG = JsonUtils.class.getSimpleName();

    private static final String OWM_MESSAGE_CODE = "cod";
    private static final String OWM_LIST = "list";
    private static final String OWM_MAIN = "main";

    private static final String OWM_DATE = "dt";
    private static final String OWM_TEMPMIN = "temp_min";
    private static final String OWM_TEMPMAX = "temp_max";
    private static final String OWM_HUMIDITY = "humidity";

    /**
     * This method parses JSON from a web response and returns an array of ContentValues.
     *
     * @param forecastJsonStr JSON response from server
     * @return an array of ContentValues describing weather forecast
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static ContentValues[] getWeatherDataFromJson(Context context, String forecastJsonStr)
            throws JSONException {

        Log.d(TAG, "json = "+forecastJsonStr);
        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        /* Is there an error? */
        if (forecastJson.has(OWM_MESSAGE_CODE)) {
            int errorCode = forecastJson.getInt(OWM_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray jsonWeatherArray = forecastJson.getJSONArray(OWM_LIST);
        ContentValues[] dateArray = new ContentValues[NetworkUtils.DAYS_TO_FORECAST];
        int day = 0;
        int numRecord = 0;
        for (int i = 0; i < jsonWeatherArray.length(); i++) {
            JSONObject dayForecast = jsonWeatherArray.getJSONObject(i);
            long utcdate = dayForecast.getLong(OWM_DATE);

            ContentValues currentCV = null;

            if (dateArray[day] == null) {
                currentCV = new ContentValues();
                currentCV.put(WeatherEntry.DAY_NUMBER, day+1);
                dateArray[day] = currentCV;
            } else {
                currentCV = dateArray[day];
            }

            JSONObject main = dayForecast.getJSONObject(OWM_MAIN);
            double tempMin = main.getDouble(OWM_TEMPMIN);
            double tempMax = main.getDouble(OWM_TEMPMAX);
            int humidity = main.getInt(OWM_HUMIDITY);

            if (!currentCV.containsKey(WeatherEntry.MINIMUM_TEMPERATURE) || tempMin < currentCV.getAsDouble(WeatherEntry.MINIMUM_TEMPERATURE)) {
                currentCV.put(WeatherEntry.MINIMUM_TEMPERATURE, tempMin);
            }
            if (!currentCV.containsKey(WeatherEntry.MAXIMUM_TEMPERATURE) || tempMax > currentCV.getAsDouble(WeatherEntry.MAXIMUM_TEMPERATURE)) {
                currentCV.put(WeatherEntry.MAXIMUM_TEMPERATURE, tempMax);
            }
            if (!currentCV.containsKey(WeatherEntry.TOTAL_HUMIDITY)) {
                currentCV.put(WeatherEntry.TOTAL_HUMIDITY, humidity);
            } else {
                double totalHumidity = currentCV.getAsDouble(WeatherEntry.TOTAL_HUMIDITY) + humidity;
                currentCV.put(WeatherEntry.TOTAL_HUMIDITY, totalHumidity);
            }

            numRecord++;
            Log.d(TAG,"Number record is "+ numRecord);



            if (numRecord == NetworkUtils.NUMBER_OF_FORECASTS_PER_DAY || i == jsonWeatherArray.length()-1 ) {
                double avgHumidity = currentCV.getAsDouble(WeatherEntry.TOTAL_HUMIDITY) / NetworkUtils.NUMBER_OF_FORECASTS_PER_DAY;
                currentCV.put(WeatherEntry.AVERAGE_HUMIDITY, avgHumidity);
                day++;
                numRecord = 0;
            }
        }


        return dateArray;
    }

}
