package fr.zlandorf.currencyconverter.activities;


import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

import fr.zlandorf.currencyconverter.AnalyticsApplication;
import fr.zlandorf.currencyconverter.R;
import fr.zlandorf.currencyconverter.models.entities.Currency;
import fr.zlandorf.currencyconverter.models.entities.Exchange;
import fr.zlandorf.currencyconverter.models.entities.Pair;
import fr.zlandorf.currencyconverter.repositories.ExchangeRepository;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.common.collect.Lists;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {
    private static final String SCREEN_NAME = "Settings_screen";
    private Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tracker = ((AnalyticsApplication) getApplication()).getDefaultTracker();

        PreferenceFragment prefsFragment = new PrefsFragment();
        prefsFragment.setArguments(getIntent().getExtras());
        getFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, prefsFragment)
            .commit();
        setupActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tracker.setScreenName(SCREEN_NAME);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                // This is what the sample code did, however this method doesn't restore the activity's state for some reason
              // NavUtils.navigateUpFromSameTask(this);
                finish();
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private Tracker tracker;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            tracker = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();

            addPreferencesFromResource(R.xml.preferences);

            List<Exchange> exchanges = new ExchangeRepository().getExchanges();
            ListPreference preferredExchangePreference = (ListPreference) findPreference(getString(R.string.pref_exchange_key));
            if (preferredExchangePreference != null) {
                CharSequence[] entries = new String[exchanges.size()];
                CharSequence[] entryValues = new String[exchanges.size()];
                int i = 0;
                for (Exchange exchange : exchanges) {
                    entries[i] = exchange.getName();
                    entryValues[i] = exchange.getName();
                    i++;
                }
                preferredExchangePreference.setEntries(entries);
                preferredExchangePreference.setEntryValues(entryValues);
            }

            PreferenceCategory pairsByExchangeCategory = (PreferenceCategory) findPreference(getString(R.string.pref_category_pairs_key));
            if (pairsByExchangeCategory != null) {
                for (Exchange exchange : exchanges) {
                    ListPreference preferredPairPreference = new ListPreference(getActivity());
                    String prefKey = String.format(getString(R.string.pref_pair_key_template), exchange.getName());

                    preferredPairPreference.setKey(prefKey);
                    preferredPairPreference.setTitle(exchange.getName());
                    preferredPairPreference.setSummary("%s");

                    initPairListPreference(exchange, preferredPairPreference);

                    pairsByExchangeCategory.addPreference(preferredPairPreference);
                }
            }
        }

        private void initPairListPreference(Exchange exchange, ListPreference listPreference) {
            List<Pair> pairs = Lists.newArrayList();

            for (Pair pair : exchange.getPairs()) {
                pairs.add(pair);
                pairs.add(pair.invert());

                Pair mBtcPair = null;
                if (pair.getFrom().equals(Currency.BTC)) {
                    mBtcPair = new Pair(Currency.mBTC, pair.getTo());
                } else if (pair.getTo().equals(Currency.BTC)) {
                    mBtcPair = new Pair(pair.getFrom(), Currency.mBTC);
                }
                if (mBtcPair != null) {
                    pairs.add(mBtcPair);
                    pairs.add(mBtcPair.invert());
                }
            }

            CharSequence[] entries = new String[pairs.size()];
            CharSequence[] entryValues = new String[pairs.size()];
            int i = 0;
            for (Pair pair : pairs) {
                entries[i] = pair.toString();
                entryValues[i] = pair.toString();
                i++;
            }
            listPreference.setEntries(entries);
            listPreference.setEntryValues(entryValues);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            ListPreference preferredPairPreference = (ListPreference) findPreference(key);
            if (preferredPairPreference != null) {
                preferredPairPreference.setSummary("dummy"); // on some devices the summary won't update without this
                preferredPairPreference.setSummary("%s");
            }
            String value = sharedPreferences.getString(key, "");
            Log.d("PREFERENCES", String.format("Changing preferences for %s : %s\n", key, value));

            tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("changed preference")
                .setLabel(key)
                .set("key", key)
                .set("value", value)
                .build()
            );
        }

        @Override
        public void onResume() {
            super.onResume();
            PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
    }

}
