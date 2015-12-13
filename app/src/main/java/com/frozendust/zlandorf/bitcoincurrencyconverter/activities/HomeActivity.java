package com.frozendust.zlandorf.bitcoincurrencyconverter.activities;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.frozendust.zlandorf.bitcoincurrencyconverter.AnalyticsApplication;
import com.frozendust.zlandorf.bitcoincurrencyconverter.R;
import com.frozendust.zlandorf.bitcoincurrencyconverter.fragments.ConverterFragment;
import com.frozendust.zlandorf.bitcoincurrencyconverter.fragments.RateListFragment;
import com.frozendust.zlandorf.bitcoincurrencyconverter.fragments.RatesTaskFragment;
import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Pair;
import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Rate;
import com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.RetrieveTask;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements ConverterFragment.OnFragmentInteractionListener, RetrieveTask.RetrieveTaskListener, RateListFragment.RateListListener {
    public static final String AVAILABLE_PAIRS_EXTRA = "available_pairs";
    private static final String RATES_TASK_FRAGMENT = "rates_task_fragment";
    private static final String SCREEN_NAME = "Home_screen";

    private RatesTaskFragment mRatesTaskFragment;
    private Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tracker = ((AnalyticsApplication) getApplication()).getDefaultTracker();

        // Set default preferences values if they have never been set before
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setLogo(R.drawable.ic_action_bar);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        FragmentManager fm = getSupportFragmentManager();
        mRatesTaskFragment = (RatesTaskFragment) fm.findFragmentByTag(RATES_TASK_FRAGMENT);
        if (mRatesTaskFragment == null) {
            // Show the progress bar while retrieving the rates for the first time
            findViewById(R.id.progress_bar_container).setVisibility(View.VISIBLE);

            // If there is no network available, show an error message
            if (!isNetworkAvailable()) {
                findViewById(R.id.spinner_progress_bar).setVisibility(View.GONE);
                findViewById(R.id.no_internet_text).setVisibility(View.VISIBLE);
            }

            // Create the task fragment that will retrieve rates
            mRatesTaskFragment = RatesTaskFragment.newInstance();
            fm.beginTransaction().add(mRatesTaskFragment, RATES_TASK_FRAGMENT).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        tracker.setScreenName(SCREEN_NAME);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            ArrayList<Pair> availablePairs = ((ConverterFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_converter)).getAvailablePairs();
            intent.putParcelableArrayListExtra(AVAILABLE_PAIRS_EXTRA, availablePairs);
            startActivity(intent);

            return true;
        }

        if (id == R.id.action_refresh) {
            if (!isNetworkAvailable()) {
                Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_LONG).show();
            } else if (mRatesTaskFragment != null) {
                Toast.makeText(getApplicationContext(), R.string.refreshing, Toast.LENGTH_SHORT).show();
                mRatesTaskFragment.execute();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // Do nothing at the moment
    }

    @Override
    public void onTaskFinished(List<Rate> rates) {
        Log.d("RATE_RETRIEVAL", String.format("Rates received : %s\n", rates.size()));
        // Hide the progress bar
        findViewById(R.id.progress_bar_container).setVisibility(View.GONE);

        FragmentManager fm = getSupportFragmentManager();
        ConverterFragment converterFragment = (ConverterFragment) fm.findFragmentById(R.id.fragment_converter);
        if (converterFragment != null) {
            converterFragment.onRatesRetrieved(rates);
        }
        RateListFragment rateListFragment = (RateListFragment) fm.findFragmentById(R.id.fragment_rate_list);
        if (rateListFragment != null) {
            rateListFragment.onRatesRetrieved(rates);
        } else {
            Log.e("RATE_RETRIEVAL", "RateListFragment not found !");
        }
    }

    @Override
    public void onTaskFailed(final String provider) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            Toast.makeText(getApplicationContext(), getString(R.string.rate_retrieval_failed, provider), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRateSelected(Rate rate) {
        // TODO !
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
