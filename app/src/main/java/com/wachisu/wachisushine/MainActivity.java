package com.wachisu.wachisushine;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private CurrentWeatherData mCurrentWeather;

    @InjectView(R.id.timeLabel)         TextView mTimeLabel;
    @InjectView(R.id.temperatureLabel)  TextView mTemperatureLabel;
    @InjectView(R.id.humidityValue)     TextView mHumidityValue;
    @InjectView(R.id.precipValue)       TextView mPrecipValue;
    @InjectView(R.id.summaryLabel)      TextView mSummaryLabel;
    @InjectView(R.id.iconImageView)     ImageView mIconImageView;
    @InjectView(R.id.refreshImageView)  ImageView mRefreshImageView;
    @InjectView(R.id.progressBar)       ProgressBar mProgressBar;
    @InjectView(R.id.localTimeTag)      TextView mLocalTime;
    @InjectView(R.id.gradenLabel)       TextView mGradenLabel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        final double latitude = 51.924420;
        final double longitude = 4.477733;

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getForecast(latitude, longitude);

            }
        });

        getForecast(latitude, longitude);
    }

    private void getForecast(double latitude, double longitude) {
        String apiKey = "65f23d7753967916559aa65d1169474a";
        String forecastURL = ("https://api.forecast.io/forecast/" + apiKey + "/" + latitude + "," + longitude + "");

        if (isnetworkavaible()) {

            toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastURL)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {

                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    errorAlert();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);

                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {

                            errorAlert();

                        }

                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }

                }
            });
        } else {
            errorNoNetworkAlert();
        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE){
        mProgressBar.setVisibility(View.VISIBLE);
        mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDisplay() {

        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());

        mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
        mTimeLabel.setText(getString(R.string.time_label_now));
        mLocalTime.setText(mCurrentWeather.getFormattedTime());
        mHumidityValue.setText(mCurrentWeather.getHumidity() + "");
        mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "%");
        mSummaryLabel.setText(mCurrentWeather.getSummary());
        mIconImageView.setImageDrawable(drawable);
        mGradenLabel.setText(getString(R.string.gradenLabel));
    }

    private CurrentWeatherData getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");

        CurrentWeatherData currentWeatherData = new CurrentWeatherData();
        currentWeatherData.setHumidity(currently.getDouble("humidity"));
        currentWeatherData.setTime(currently.getLong("time"));
        currentWeatherData.setIcon(currently.getString("icon"));
        currentWeatherData.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeatherData.setSummary(currently.getString("summary"));
        currentWeatherData.setTemperature(currently.getDouble("temperature"));
        currentWeatherData.setTimeZone(timezone);

        return currentWeatherData;
    }

    private boolean isnetworkavaible() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvaible = false;

        if (networkInfo != null && networkInfo.isConnected()) {

            isAvaible = true;
        }

        return isAvaible;
    }

    private void errorAlert() {
        AlertDialogFragment dialog = new AlertDialogFragment();

        dialog.setDialogTitle(getString(R.string.error_dialog_one));
        dialog.setDialogMessage(getString(R.string.error_dialog_one_message));
        dialog.setDialogButtonText(getString(R.string.error_dialog_one_button_text));

        dialog.show(getFragmentManager(), "error_dialog");
    }

    private void errorNoNetworkAlert() {

        AlertDialogFragment dialog = new AlertDialogFragment();

        dialog.setDialogTitle(getString(R.string.network_error_title));
        dialog.setDialogMessage(getString(R.string.network_error_message));
        dialog.setDialogButtonText(getString(R.string.network_error_button_text));

        dialog.show(getFragmentManager(), "error_dialog_network");
    }
}