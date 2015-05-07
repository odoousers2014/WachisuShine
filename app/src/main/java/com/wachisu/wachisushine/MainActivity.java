package com.wachisu.wachisushine;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeatherData mCurrentWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String apiKey = "65f23d7753967916559aa65d1169474a";
        double latitude = 37.8267;
        double longitude = -122.423;
        String forecastURL = ("https://api.forecast.io/forecast/" + apiKey + "/" + latitude + "," + longitude + "");
        if (isnetworkavaible()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastURL)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {

                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {

                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);

                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);
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