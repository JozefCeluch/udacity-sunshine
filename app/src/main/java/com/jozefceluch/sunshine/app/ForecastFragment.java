package com.jozefceluch.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> forecastAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);

        List<String> forecastItems = new ArrayList<>();
        forecastItems.add("Today - Sunny - 54/64");
        forecastItems.add("Tomorrow - Sunny - 54/24");
        forecastItems.add("Sunday - Raining - 43/64");

        forecastAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                forecastItems
        );
        ListView forecastList = (ListView) rootView.findViewById(R.id.listview_forecast);
        forecastList.setAdapter(forecastAdapter);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            new FetchWeatherTask().execute("London");
        }

        return super.onOptionsItemSelected(item);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

        private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
        private static final String QUERY_PARAM = "q";
        private static final String MODE_PARAM = "mode";
        private static final String UNITS_PARAM = "units";
        private static final String COUNT_PARAM = "cnt";
        private static final String APP_ID_PARAM = "appid";
        private final String TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... params) {
            String location = params[0];
            String forecastData = retrieveForecastDataForLocation(location);
            Log.d(TAG, forecastData);
            return null;
        }

        private String retrieveForecastDataForLocation(String location) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(buildRequestUri(location));

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                return buffer.toString();
            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                return null;
            } finally {
                closeUrlConnection(urlConnection);
                closeReader(reader);
            }
        }

        private String buildRequestUri(String location) {
            String mode = "json";
            String units = "metric";
            String days = "7";
            String apiKey = "a81f2d1c3789ac0043ab476183c3c2da";
            return Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, location)
                    .appendQueryParameter(MODE_PARAM, mode)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(COUNT_PARAM, days)
                    .appendQueryParameter(APP_ID_PARAM, apiKey)
                    .build().toString();
        }

        private void closeReader(BufferedReader reader) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }

        private void closeUrlConnection(HttpURLConnection urlConnection) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
