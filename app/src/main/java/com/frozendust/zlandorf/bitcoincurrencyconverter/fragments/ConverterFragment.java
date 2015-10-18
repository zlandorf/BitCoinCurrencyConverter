package com.frozendust.zlandorf.bitcoincurrencyconverter.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
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

import com.frozendust.zlandorf.bitcoincurrencyconverter.R;
import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Rate;

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
    private static final String BITCOIN = "BTC";
    private static final String EURO = "EUR";
    private static final String BTC_TO_EURO_PAIR = BITCOIN + EURO;

    private static final int BITCOIN_TO_MBTC_RATIO = 1000;

    private OnFragmentInteractionListener mListener;

    private Spinner mFromSpinner;
    private Spinner mToSpinner;

    private List<String> mFromCurrencies;
    private List<String> mToCurrencies;

    private ArrayAdapter<String> mFromAdapter;
    private ArrayAdapter<String> mToAdapter;

    private Map<String, Rate> mPairToRateMap;

    private TextView mFromTextInput;
    private TextView mToText;

    private DecimalFormat mDecimalFormatter;

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
        boolean doesBTCToEURExist = mPairToRateMap.containsKey(BTC_TO_EURO_PAIR);
        updateFromSpinner(getCompletedRates(rates));

        boolean hasBTCToEURBeenAdded = !doesBTCToEURExist && mPairToRateMap.containsKey(BTC_TO_EURO_PAIR);
        if (hasBTCToEURBeenAdded) {
            // when the spinners are initialised, set the currency rates to BTC/EUR
            selectSpinnerCurrency(mFromSpinner, BITCOIN);
        }
        updateToSpinner();
        if (hasBTCToEURBeenAdded) {
            // when the spinners are initialised, set the currency rates to BTC/EUR
            selectSpinnerCurrency(mToSpinner, EURO);
        }
        updateConversion();
    }

    private void createFromItemsViews(View view) {
        mFromAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mFromCurrencies);
        mFromAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

        mFromSpinner = (Spinner) view.findViewById(R.id.convertFromSpinner);
        mFromSpinner.setAdapter(mFromAdapter);
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
        mToSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) {}
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateConversion();
            }
        });

        mToText = (TextView) view.findViewById(R.id.convertToText);
        mToText.setText("0.0");
    }

    /**
     * Converts the user's input value by using the selected exchange rate
     */
    private void updateConversion() {
        int fromItemPosition = mFromSpinner.getSelectedItemPosition();
        int toItemPosition = mToSpinner.getSelectedItemPosition();

        double convertedValue = 0;

        if (
                fromItemPosition != Spinner.INVALID_POSITION
                        && toItemPosition != Spinner.INVALID_POSITION
                        && fromItemPosition < mFromSpinner.getCount()
                        && toItemPosition < mToSpinner.getCount()
                ) {
            String selectedFrom = (String) mFromSpinner.getItemAtPosition(fromItemPosition);
            String selectedTo = (String) mToSpinner.getItemAtPosition(toItemPosition);

            Rate selectedRate = mPairToRateMap.get(selectedFrom + selectedTo);
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
            if (!mFromCurrencies.contains(rate.getFrom())) {
                mFromCurrencies.add(rate.getFrom());
            }
            // This will update "old" rates in case of a refresh
            mPairToRateMap.put(rate.getPair(), rate);
        }
        mFromAdapter.notifyDataSetChanged();
    }

    /**
     * Update the "To" Spinner to only display currencies the selected currency can convert to
     */
    private void updateToSpinner() {
        int fromItemPosition = mFromSpinner.getSelectedItemPosition();
        String previouslySelectedTo = (String) mToSpinner.getSelectedItem();

        mToAdapter.clear();
        if (fromItemPosition != Spinner.INVALID_POSITION && fromItemPosition < mFromSpinner.getCount()) {
            String selectedFrom = (String) mFromSpinner.getItemAtPosition(fromItemPosition);
            // fill the "to" spinner with possible rates
            for (Rate rate : mPairToRateMap.values()) {
                if (rate.getFrom().equals(selectedFrom)) {
                    mToCurrencies.add(rate.getTo());
                }
            }
        }
        mToAdapter.notifyDataSetChanged();

        if (previouslySelectedTo != null) {
            // Try to keep the selected "to" rate if possible
            selectSpinnerCurrency(mToSpinner, previouslySelectedTo);
        }
    }

    private void selectSpinnerCurrency(Spinner spinner, String currency) {
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
                completedRates.add(new Rate(rate.getTo(), rate.getFrom(), 1. / rate.getValue()));
            }
        }

        int size = completedRates.size();
        for (int i = 0; i < size; i++) {
            Rate rate = completedRates.get(i);

            // If a rate is from or to BTC, add rates with mBTC
            if (rate.getFrom().equals(BITCOIN)) {
                completedRates.add(new Rate("mBTC", rate.getTo(), rate.getValue() / BITCOIN_TO_MBTC_RATIO));
            }
            if (rate.getTo().equals(BITCOIN)) {
                completedRates.add(new Rate(rate.getFrom(), "mBTC", rate.getValue() * BITCOIN_TO_MBTC_RATIO));
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
