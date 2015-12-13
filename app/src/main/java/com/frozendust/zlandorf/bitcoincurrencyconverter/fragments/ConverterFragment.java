package com.frozendust.zlandorf.bitcoincurrencyconverter.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.frozendust.zlandorf.bitcoincurrencyconverter.R;
import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Currency;
import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Pair;
import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Rate;
import com.google.common.collect.Lists;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConverterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConverterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConverterFragment extends Fragment {
    private static final double BITCOIN_TO_MBTC_RATIO = 1000.;
    private static final Pair BTC_EUR_PAIR = new Pair(Currency.BTC, Currency.EUR);

    private OnFragmentInteractionListener mListener;

    private Spinner mFromSpinner;
    private Spinner mToSpinner;

    private List<Currency> mFromCurrencies;
    private List<Currency> mToCurrencies;

    private ArrayAdapter<Currency> mFromAdapter;
    private ArrayAdapter<Currency> mToAdapter;

    private Map<Integer, Rate> mPairToRateMap;

    private TextView mFromTextInput;
    private TextView mToText;

    private DecimalFormat mDecimalFormatter;

    private boolean hasUserInteracted = false;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ConverterFragment.
     */
    public static ConverterFragment newInstance() {
        return new ConverterFragment();
    }

    public ConverterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mFromCurrencies = new ArrayList<>();
        mToCurrencies = new ArrayList<>();
        mPairToRateMap = new ConcurrentHashMap<>();
        mDecimalFormatter = new DecimalFormat("#,##0.0####", new DecimalFormatSymbols(Locale.ENGLISH));
    }

    public ArrayList<Pair> getAvailablePairs() {
        ArrayList<Pair> pairs = Lists.newArrayList();
        for (Rate rate : mPairToRateMap.values()) {
            pairs.add(rate.getPair());
        }
        return pairs;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_converter, container, false);
        createFromItemsViews(view);
        createToItemsViews(view);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("from", mFromSpinner.getSelectedItemPosition());
        outState.putInt("to", mToSpinner.getSelectedItemPosition());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mFromSpinner.setSelection(savedInstanceState.getInt("from"));
            mToSpinner.setSelection(savedInstanceState.getInt("to"));
        }

        mFromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateToSpinner();
                updateConversion();
            }
        });

        mToSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) {}
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateConversion();
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onRatesRetrieved(List<Rate> rates) {
        if (rates == null || rates.isEmpty()) {
            return;
        }
        updateFromSpinner(getCompletedRates(rates));

        // If the user hasn't interacted yet, initialise the spinners on the user's preferred pair when
        // the rates are retrieved
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Pair preferredPair = null;
        if (preferences != null) {
            preferredPair = Pair.valueOf(
                preferences.getString(getString(R.string.pref_exchange_pair_key),BTC_EUR_PAIR.toString())
            );
        }
        if (preferredPair == null) {
            preferredPair = BTC_EUR_PAIR;
        }

        if (!hasUserInteracted && mPairToRateMap.containsKey(preferredPair.hashCode())) {
            selectSpinnerCurrency(mFromSpinner, preferredPair.getFrom());
        }

        updateToSpinner();

        if (!hasUserInteracted && mPairToRateMap.containsKey(preferredPair.hashCode())) {
            selectSpinnerCurrency(mToSpinner, preferredPair.getTo());
        }

        updateConversion();
    }

    private void createFromItemsViews(View view) {
        mFromAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mFromCurrencies);
        mFromAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

        mFromSpinner = (Spinner) view.findViewById(R.id.convertFromSpinner);
        mFromSpinner.setAdapter(mFromAdapter);
        mFromSpinner.setOnTouchListener(new SpinnerTouchListener());

        mFromTextInput = (TextView) view.findViewById(R.id.convertFromText);
        mFromTextInput.setText("1.0");
        mFromTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                hasUserInteracted = true;
                updateConversion();
            }
        });
    }

    private void createToItemsViews(View view) {
        mToAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mToCurrencies);
        mToAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

        mToSpinner = (Spinner) view.findViewById(R.id.convertToSpinner);
        mToSpinner.setAdapter(mToAdapter);
        mToSpinner.setOnTouchListener(new SpinnerTouchListener());

        mToText = (TextView) view.findViewById(R.id.convertToText);
        mToText.setText("0.0");
    }

    /**
     * Converts the user's input value by using the selected exchange rate
     */
    private void updateConversion() {
        Currency selectedFrom = (Currency) mFromSpinner.getSelectedItem();
        Currency selectedTo = (Currency) mToSpinner.getSelectedItem();
        double convertedValue = 0;

        if (selectedFrom != null && selectedTo != null) {
            Rate selectedRate = mPairToRateMap.get(new Pair(selectedFrom, selectedTo).hashCode());
            if (selectedRate != null) {
                String fromValueAsString = mFromTextInput.getText().toString();
                if (!fromValueAsString.isEmpty()) {
                    try {
                        convertedValue = Double.parseDouble(fromValueAsString) * selectedRate.getValue();
                    } catch (NumberFormatException e) {
                        Log.e("CONVERTER_FRAGMENT", "Failed to parse double " + fromValueAsString);
                    }
                }
            }
        }
        mToText.setText(mDecimalFormatter.format(convertedValue));
    }

    private void updateFromSpinner(List<Rate> rates) {
        for (Rate rate: rates) {
            if (!mFromCurrencies.contains(rate.getPair().getFrom())) {
                mFromCurrencies.add(rate.getPair().getFrom());
            }
            // This will update "old" rates in case of a refresh
            mPairToRateMap.put(rate.getPair().hashCode(), rate);
        }
        mFromAdapter.notifyDataSetChanged();
    }

    /**
     * Update the "To" Spinner to only display currencies the selected currency can convert to
     */
    private void  updateToSpinner() {
        Currency selectedFrom = (Currency) mFromSpinner.getSelectedItem();
        Currency selectedTo = (Currency) mToSpinner.getSelectedItem();

        mToAdapter.clear();
        if (selectedFrom != null) {
            // fill the "to" spinner with possible rates
            for (Rate rate : mPairToRateMap.values()) {
                if (rate.getPair().getFrom().equals(selectedFrom)) {
                    mToCurrencies.add(rate.getPair().getTo());
                }
            }
        }
        mToAdapter.notifyDataSetChanged();

        if (selectedTo != null) {
            // Try to keep the selected "to" rate if possible
            selectSpinnerCurrency(mToSpinner, selectedTo);
        }
    }

    private void selectSpinnerCurrency(Spinner spinner, Currency currency) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(currency)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    /**
     * Complete list of rates by adding inverted rates and by adding conversions to and from mBTC
     * @param rates the original rates list returned by a {@link com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.RetrieveTask}
     * @return a list containing inverted rates and added rates to and from mBTC
     */
    private List<Rate> getCompletedRates(List<Rate> rates) {
        List<Rate> completedRates = new ArrayList<>(rates);
        for (Rate rate : rates) {
            if (rate.getValue() > 0) {
                // add inverted rates to be able to convert both ways
                completedRates.add(new Rate(rate.getPair().invert(), 1. / rate.getValue()));
            }
        }

        int size = completedRates.size();
        for (int i = 0; i < size; i++) {
            Rate rate = completedRates.get(i);

            // If a rate is from or to BTC, add rates with mBTC
            if (rate.getPair().getFrom().equals(Currency.BTC)) {
                completedRates.add(new Rate(new Pair(Currency.mBTC, rate.getPair().getTo()), rate.getValue() / BITCOIN_TO_MBTC_RATIO));
            }
            if (rate.getPair().getTo().equals(Currency.BTC)) {
                completedRates.add(new Rate(new Pair(rate.getPair().getFrom(), Currency.mBTC), rate.getValue() * BITCOIN_TO_MBTC_RATIO));
            }
        }

        return completedRates;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    class SpinnerTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            hasUserInteracted = true;
            return false;
        }
    }
}
