package fr.zlandorf.currencyconverter.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

import fr.zlandorf.currencyconverter.AnalyticsApplication;
import fr.zlandorf.currencyconverter.R;
import fr.zlandorf.currencyconverter.fragments.ConverterFragment;
import fr.zlandorf.currencyconverter.fragments.RateListFragment;
import fr.zlandorf.currencyconverter.fragments.RatesTaskFragment;
import fr.zlandorf.currencyconverter.models.entities.Exchange;
import fr.zlandorf.currencyconverter.models.entities.Pair;
import fr.zlandorf.currencyconverter.models.entities.Rate;
import fr.zlandorf.currencyconverter.repositories.ExchangeRepository;
import fr.zlandorf.currencyconverter.tasks.RetrieveTask;

public class HomeActivity extends AppCompatActivity implements ConverterFragment.OnFragmentInteractionListener, RetrieveTask.RetrieveTaskListener, RateListFragment.RateListListener {
    public static final String AVAILABLE_PAIRS_EXTRA = "available_pairs";
    private static final String RATES_TASK_FRAGMENT = "rates_task_fragment";
    private static final String SCREEN_NAME = "Home_screen";

    private Spinner exchangeSelector;
    private RatesTaskFragment mRatesTaskFragment;
    private Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tracker = ((AnalyticsApplication) getApplication()).getDefaultTracker();

        // Set default preferences values if they have never been set before
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        ExchangeRepository exchangeRepository = new ExchangeRepository();
        exchangeSelector = (Spinner) findViewById(R.id.exchange_selector);
        ArrayAdapter<Exchange> exchangeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exchangeRepository.getExchanges());
        exchangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exchangeSelector.setAdapter(exchangeAdapter);
        exchangeSelector.setOnItemSelectedListener(new ExchangesItemSelector());
        selectPreferredExchange();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setLogo(R.drawable.ic_action_bar);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        FragmentManager fm = getSupportFragmentManager();
        mRatesTaskFragment = (RatesTaskFragment) fm.findFragmentByTag(RATES_TASK_FRAGMENT);
        if (mRatesTaskFragment == null) {
            // Create the task fragment that will retrieve rates
            mRatesTaskFragment = RatesTaskFragment.newInstance();
            fm.beginTransaction().add(mRatesTaskFragment, RATES_TASK_FRAGMENT).commit();
        }
    }

    public void retrieveRates() {
        // Show the progress bar while retrieving the rates for the first time
        findViewById(R.id.progress_bar_container).setVisibility(View.VISIBLE);
        findViewById(R.id.fragments_container).setVisibility(View.GONE);

        // If there is no network available, show an error message
        if (!isNetworkAvailable()) {
            findViewById(R.id.spinner_progress_bar).setVisibility(View.GONE);
            findViewById(R.id.no_internet_text).setVisibility(View.VISIBLE);
        } else {

            Exchange exchange = (Exchange) exchangeSelector.getSelectedItem();
            if (exchange != null) {
                try {
                    mRatesTaskFragment.execute(exchange);
                } catch (Exception e) {
                    Log.e("RATE_RETRIEVAL", "Failed to retrieve tasks for " + exchange.getName() + " : " + e.getMessage());
                    onTaskFailed(exchange);
                }
            }
        }

    }

    private void selectPreferredExchange() {
        String preferredExchange = getPreferredExchangeName();
        if (preferredExchange != null) {
            for (int i = 0; i < exchangeSelector.getCount(); i++) {
                Exchange exchange = (Exchange) exchangeSelector.getItemAtPosition(i);
                if (exchange.getName().equals(preferredExchange)) {
                    exchangeSelector.setSelection(i, false);
                    break;
                }
            }
        }
    }

    private String getPreferredExchangeName() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences != null) {
            return preferences.getString(getString(R.string.pref_exchange_key), null);
        }
        return null;
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
                retrieveRates();
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
    public void onTaskFinished(Exchange exchange, List<Rate> rates) {
        Log.d("RATE_RETRIEVAL", String.format("Rates received : %s\n", rates.size()));
        // Hide the progress bar
        findViewById(R.id.progress_bar_container).setVisibility(View.GONE);
        findViewById(R.id.fragments_container).setVisibility(View.VISIBLE);

        FragmentManager fm = getSupportFragmentManager();
        ConverterFragment converterFragment = (ConverterFragment) fm.findFragmentById(R.id.fragment_converter);
        if (converterFragment != null) {
            converterFragment.onRatesRetrieved(exchange, rates);
        }
        RateListFragment rateListFragment = (RateListFragment) fm.findFragmentById(R.id.fragment_rate_list);
        if (rateListFragment != null) {
            rateListFragment.onRatesRetrieved(rates);
        } else {
            Log.e("RATE_RETRIEVAL", "RateListFragment not found !");
        }
    }

    @Override
    public void onTaskFailed(final Exchange exchange) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            Toast.makeText(getApplicationContext(), getString(R.string.rate_retrieval_failed, exchange), Toast.LENGTH_SHORT).show();
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

    private class ExchangesItemSelector implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            retrieveRates();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }
}
