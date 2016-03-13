package com.jozefceluch.sunshine.app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    public static class PlaceholderFragment extends Fragment {

        public static final String SHARE_HASHTAG = " #SunshineApp";
        private static final String TAG = PlaceholderFragment.class.getSimpleName();
        private ShareActionProvider shareActionProvider;
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

            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                forecastString = intent.getStringExtra(Intent.EXTRA_TEXT);
            } else {
                forecastString = "";
            }
            TextView forecastView = (TextView) rootView.findViewById(R.id.detail_text);
            forecastView.setText(forecastString);

            return rootView;
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


    }
}
