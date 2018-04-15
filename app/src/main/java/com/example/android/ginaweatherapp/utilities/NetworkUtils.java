package com.example.android.ginaweatherapp.utilities;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the weather servers.
 */
public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String FORECAST_BASE_URL =  "http://api.openweathermap.org/data/2.5/forecast";

    private static final String ZIP_PARAM = "zip";
    private static final String ZIP_VALUE_SUFFIX = "us";

    private static final String APPID_PARAM = "appid";
    private static final String APPID_VALUE = "8164a613b8e6973b5c91067d6a5e1c25";

    private static final String UNITS_PARAM = "units";
    private static final String UNITS_VALUE_IMPERIAL = "imperial";

    private static final String CNT_PARAM = "cnt";

    public static final int DAYS_TO_FORECAST = 5;
    public static final int NUMBER_OF_HOURS_BETWEEN_EACH_FORECAST = 3;
    public static final int NUMBER_OF_FORECASTS_PER_DAY = 24 / NUMBER_OF_HOURS_BETWEEN_EACH_FORECAST;
    private static final int CNT_VALUE = DAYS_TO_FORECAST * NUMBER_OF_FORECASTS_PER_DAY;

    /**
     * Builds the URL used to talk to the open weather map server using a zip code.
     *
     * @param zipValue The zip code of location that will be queried plus country code with comma delimiter.
     * @return The URL to use to query the open weather map server.
     */
    public static URL buildUrl(String zipValue) {
        Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(ZIP_PARAM, zipValue+","+ZIP_VALUE_SUFFIX)
                .appendQueryParameter(APPID_PARAM, APPID_VALUE)
                .appendQueryParameter(UNITS_PARAM, UNITS_VALUE_IMPERIAL)
                .appendQueryParameter(CNT_PARAM, String.valueOf(CNT_VALUE))
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Built URI " + url);

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}