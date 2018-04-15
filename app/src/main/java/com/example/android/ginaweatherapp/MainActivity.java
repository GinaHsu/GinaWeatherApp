package com.example.android.ginaweatherapp;

import android.content.ContentValues;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.ginaweatherapp.data.WeatherRecord;
import com.example.android.ginaweatherapp.utilities.JsonUtils;
import com.example.android.ginaweatherapp.utilities.NetworkUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements
        LoaderCallbacks<ContentValues[]> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int FORECAST_LOADER_ID = 0;

    private EditText mZipCodeInput;
    private ImageButton mSearchButton;
    private TextView mForecastDisplay;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    private ContentValues[] mWeatherData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mZipCodeInput = findViewById(R.id.et_zipcode);
        mSearchButton = findViewById(R.id.search_button);
        mForecastDisplay = findViewById(R.id.tv_forecast_display);
        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportLoaderManager().restartLoader(FORECAST_LOADER_ID, null, MainActivity.this);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getSupportLoaderManager().getLoader(FORECAST_LOADER_ID) == null) {
            getSupportLoaderManager().initLoader(FORECAST_LOADER_ID, null, MainActivity.this);
        } else {
            getSupportLoaderManager().restartLoader(FORECAST_LOADER_ID, null, MainActivity.this);
        }
    }

    @Override
    public Loader<ContentValues[]> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<ContentValues[]>(this) {

            /**
             * Subclasses of AsyncTaskLoader must implement this to take care of loading their data.
             */
            @Override
            protected void onStartLoading() {
                mLoadingIndicator.setVisibility(View.VISIBLE);
                forceLoad();
            }

            /**
             * This is the method of the AsyncTaskLoader that will load and parse the JSON data
             * from open weather map in the background.
             *
             * @return Weather data from open weather map as an array of ContentValues.
             *         null if an error occurs
             */
            @Override
            public ContentValues[] loadInBackground() {

                try {
                    String zipCode = mZipCodeInput.getText().toString();
                    if (!TextUtils.isEmpty(zipCode)) {
                        URL url = NetworkUtils.buildUrl(zipCode);
                        String jsonWeatherResponse = NetworkUtils
                                .getResponseFromHttpUrl(url);
                        ContentValues[] weatherData = JsonUtils
                                .getWeatherDataFromJson(MainActivity.this, jsonWeatherResponse);
                        return weatherData;
                    } else {
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

        };
    }

    @Override
    public void onLoadFinished(Loader<ContentValues[]> loader, ContentValues[] data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mWeatherData = data;
        String zipCode = mZipCodeInput.getText().toString();
        if (TextUtils.isEmpty(zipCode)) {
            showInstruction();
        } else if (null == data) {
            showErrorMessage();
        } else {
            showWeatherDataView();
        }
    }

    @Override
    public void onLoaderReset(Loader<ContentValues[]> loader) {

    }

    /**
     * This method will make the View for the weather data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showWeatherDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mForecastDisplay.setVisibility(View.VISIBLE);
        if (mWeatherData != null) {
            StringBuffer weatherString = new StringBuffer();
            weatherString.append("Date -- Min/ Max Temperature -- Ave Humidity \n\n");

            for (ContentValues weatherData : mWeatherData) {
                String date = weatherData.getAsString(WeatherRecord.WeatherEntry.DAY_NUMBER);
                int tempmin = weatherData.getAsInteger(WeatherRecord.WeatherEntry.MINIMUM_TEMPERATURE);
                int tempmax = weatherData.getAsInteger(WeatherRecord.WeatherEntry.MAXIMUM_TEMPERATURE);
                int humidity = weatherData.getAsInteger(WeatherRecord.WeatherEntry.AVERAGE_HUMIDITY);

                weatherString.append("Day "+date+" -- "+tempmin+"F / "+tempmax+"F"+" -- "+humidity+"%\n");
            }
            mForecastDisplay.setText(weatherString.toString());
        } else {
            showInstruction();
        }
    }

    private void showInstruction() {
        mForecastDisplay.setText(getResources().getString(R.string.instruction_message));
    }

    /**
     * This method will make the error message visible and hide the weather
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mForecastDisplay.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

}
