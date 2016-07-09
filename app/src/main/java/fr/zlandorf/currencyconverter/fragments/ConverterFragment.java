package fr.zlandorf.currencyconverter.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fr.zlandorf.currencyconverter.R;
import fr.zlandorf.currencyconverter.models.entities.Currency;
import fr.zlandorf.currencyconverter.models.exchanges.Exchange;
import fr.zlandorf.currencyconverter.models.entities.Pair;
import fr.zlandorf.currencyconverter.models.entities.Rate;
import fr.zlandorf.currencyconverter.tasks.RetrieveTask;

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

    private Exchange currentExchange = null;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onRatesRetrieved(Exchange exchange, List<Rate> rates) {
        if (rates == null || rates.isEmpty()) {
            return;
        }
        boolean newExchangeSelected = currentExchange == null || !currentExchange.getName().equals(exchange.getName());
        currentExchange = exchange;
        List<Rate> completedRates = getCompletedRates(rates);
        setFromSpinner(completedRates);
        setPairToRateMap(completedRates);

        Pair preferredPair = getPreferredPair(exchange);

        if (newExchangeSelected && preferredPair != null && mPairToRateMap.containsKey(preferredPair.hashCode())) {
            selectSpinnerCurrency(mFromSpinner, preferredPair.getFrom());
        }

        updateToSpinner();

        if (newExchangeSelected && preferredPair != null && mPairToRateMap.containsKey(preferredPair.hashCode())) {
            selectSpinnerCurrency(mToSpinner, preferredPair.getTo());
        }

        updateConversion();
    }

    private Pair getPreferredPair(Exchange exchange) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String prefKey = String.format(getString(R.string.pref_pair_key_template), exchange.getName());
        if (preferences != null) {
            String prefValue = preferences.getString(prefKey, null);
            if (prefValue != null) {
                return Pair.valueOf(prefValue);
            }
        }
        return null;
    }

    private void createFromItemsViews(View view) {
        mFromAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mFromCurrencies);
        mFromAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

        mFromSpinner = (Spinner) view.findViewById(R.id.convertFromSpinner);
        mFromSpinner.setAdapter(mFromAdapter);

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
                updateConversion();
            }
        });
    }

    private void createToItemsViews(View view) {
        mToAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mToCurrencies);
        mToAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

        mToSpinner = (Spinner) view.findViewById(R.id.convertToSpinner);
        mToSpinner.setAdapter(mToAdapter);

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

    private void setFromSpinner(List<Rate> rates) {
        mFromCurrencies.clear();
        for (Rate rate : rates) {
            Currency from = rate.getPair().getFrom();
            if (!mFromCurrencies.contains(from)) {
                mFromCurrencies.add(from);
            }
        }
        mFromAdapter.notifyDataSetChanged();
    }

    private void setPairToRateMap(List<Rate> rates) {
        mPairToRateMap.clear();
        for (Rate rate : rates) {
            mPairToRateMap.put(rate.getPair().hashCode(), rate);
        }
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
        if (spinner.getCount() > 0) {
            spinner.setSelection(0);
        } else {
            spinner.setSelection(Spinner.INVALID_POSITION);
        }
    }

    /**
     * Complete list of rates by adding inverted rates and by adding conversions to and from mBTC
     * @param rates the original rates list returned by a {@link RetrieveTask}
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

}
