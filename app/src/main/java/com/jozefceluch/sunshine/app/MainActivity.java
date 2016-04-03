package com.jozefceluch.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.jozefceluch.sunshine.app.sync.SunshineSyncAdapter;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {

    private static final String DETAIL_FRAGMENT_TAG = "detail_fragment";
    private String location;
    private boolean twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);
        location = Utility.getPreferredLocation(this);
        if (findViewById(R.id.weather_detail_container) != null) {
            twoPane = true;
        } else {
            twoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        ForecastFragment forecastFragment = ((ForecastFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));
        forecastFragment.setTwoPaneMode(twoPane);
        SunshineSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        String newLocation = Utility.getPreferredLocation(this);
        // update the location in our second pane using the fragment manager
        if (newLocation != null && !newLocation.equals(location)) {
            ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (null != ff) {
                ff.onLocationChanged();
                ff.setTwoPaneMode(twoPane);
            }
            DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if (null != df) {
                df.onLocationChanged(newLocation);
            }
            location = newLocation;
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
            SettingsActivity.showSettings(this);
            return true;
        } else if (id == R.id.action_view_on_map) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String location = sp.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        Uri data = Uri.parse("geo:0,0")
                .buildUpon()
                .appendQueryParameter("q", location)
                .build();
        intent.setData(data);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Unable to show map", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if (twoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, DetailFragment.newInstance(dateUri), DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.setData(dateUri);
            startActivity(intent);
        }

    }
}
