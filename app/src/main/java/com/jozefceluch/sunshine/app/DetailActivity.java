package com.jozefceluch.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.jozefceluch.sunshine.app.data.WeatherContract.LocationEntry;
import static com.jozefceluch.sunshine.app.data.WeatherContract.WeatherEntry;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        public static final String SHARE_HASHTAG = " #SunshineApp";
        private static final String TAG = DetailFragment.class.getSimpleName();
        private static final int DETAIL_LOADER_ID = 1;

        private static final String[] DETAIL_COLUMNS = {
                // In this case the id needs to be fully qualified with a table name, since
                // the content provider joins the location & weather tables in the background
                // (both have an _id column)
                // On the one hand, that's annoying.  On the other, you can search the weather table
                // using the location set by the user, which is only in the Location table.
                // So the convenience is worth it.
                WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
                WeatherEntry.COLUMN_DATE,
                WeatherEntry.COLUMN_SHORT_DESC,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP,
                LocationEntry.COLUMN_LOCATION_SETTING,
                WeatherEntry.COLUMN_WEATHER_ID,
                LocationEntry.COLUMN_COORD_LAT,
                LocationEntry.COLUMN_COORD_LONG
        };

        // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
        // must change.
        static final int COL_WEATHER_ID = 0;
        static final int COL_WEATHER_DATE = 1;
        static final int COL_WEATHER_DESC = 2;
        static final int COL_WEATHER_MAX_TEMP = 3;
        static final int COL_WEATHER_MIN_TEMP = 4;
        static final int COL_LOCATION_SETTING = 5;
        static final int COL_WEATHER_CONDITION_ID = 6;
        static final int COL_COORD_LAT = 7;
        static final int COL_COORD_LONG = 8;

        private ShareActionProvider shareActionProvider;
        private TextView forecastView;
        private String forecastString;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            forecastView = (TextView) rootView.findViewById(R.id.detail_text);

            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
            getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detail, menu);

            MenuItem item = menu.findItem(R.id.menu_item_share);

            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(createShareIntent());
            } else {
                Log.d(TAG, "Share Action provider is not available");
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                SettingsActivity.showSettings(getActivity());
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        private Intent createShareIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            int flag;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                flag = Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
            } else {
                @SuppressWarnings("deprecation")
                int deprecatedFlag = Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
                flag = deprecatedFlag;
            }
            shareIntent.addFlags(flag);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, forecastString + SHARE_HASHTAG);
            return shareIntent;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Intent intent = getActivity().getIntent();
            if (intent == null) {
                return null;
            }
            return new CursorLoader(
                    getActivity(),
                    intent.getData(),
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.v(TAG, "In onLoadFinished");
            if (!data.moveToFirst()) {
                return;
            }

            String dateString = Utility.formatDate(
                    data.getLong(COL_WEATHER_DATE));

            String weatherDescription =
                    data.getString(COL_WEATHER_DESC);

            boolean isMetric = Utility.isMetric(getActivity());

            String high = Utility.formatTemperature(
                    data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);

            String low = Utility.formatTemperature(
                    data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

            forecastString = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

            forecastView.setText(forecastString);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(createShareIntent());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
