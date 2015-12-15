package com.frozendust.zlandorf.bitcoincurrencyconverter.activities;


import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.frozendust.zlandorf.bitcoincurrencyconverter.AnalyticsApplication;
import com.frozendust.zlandorf.bitcoincurrencyconverter.R;
import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Currency;
import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Pair;
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

            ListPreference preferredPairPreference = (ListPreference) findPreference(getString(R.string.pref_exchange_pair_key));

            if (preferredPairPreference != null) {
                List<Pair> pairs = getArguments().getParcelableArrayList(HomeActivity.AVAILABLE_PAIRS_EXTRA);
                if (pairs == null) {
                    pairs = Lists.newArrayList();
                }
                if (pairs.isEmpty()) {
                    // By default, the preferred pair is BTC/EUR
                    pairs.add(new Pair(Currency.BTC, Currency.EUR));
                }

                CharSequence[] entries = new String[pairs.size()];
                CharSequence[] entryValues = new String[pairs.size()];
                int i = 0;
                for (Pair pair : pairs) {
                    entries[i] = pair.toString();
                    entryValues[i] = pair.toString();
                    i++;
                }
                preferredPairPreference.setEntries(entries);
                preferredPairPreference.setEntryValues(entryValues);
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            ListPreference preferredPairPreference = (ListPreference) findPreference(getString(R.string.pref_exchange_pair_key));
            if (preferredPairPreference != null) {
                preferredPairPreference.setSummary("%s");
            }
            tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("changed preferred pair")
                .set("pair", sharedPreferences.getString(key, ""))
                .build()
            );
        }

        @Override
        public void onResume() {
            super.onResume();
            PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
            tracker.setScreenName(SCREEN_NAME);
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        }

        @Override
        public void onPause() {
            PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
    }

}
