package com.frozendust.zlandorf.bitcoincurrencyconverter.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
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

    private static final int BITCOIN_TO_MBTC = 1000;

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

    /**
     * Converts the user's input value by using the selected exchange rate
     */
    public void updateConversion() {
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
                        // do nothing
                    }
                }
            }
        }
        mToText.setText(mDecimalFormatter.format(convertedValue));
    }

    /**
     * Update the "To" Spinner to only display currencies the selected currency can convert to
     */
    public void updateToSpinner() {
        int fromItemPosition = mFromSpinner.getSelectedItemPosition();
        String previouslySelectTo = (String) mToSpinner.getSelectedItem();

        if (!hasUserInteracted) {
            // The spinners have been loaded and the user has not yet interacted with them
            // In this case, we initialise the position of the from spinner on BTC
            for (int i = 0; i < mFromSpinner.getCount(); i++) {
                if (mFromSpinner.getItemAtPosition(i).equals(BITCOIN)) {
                    mFromSpinner.setSelection(i);
                    fromItemPosition = i;
                    break;
                }
            }
        }

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

        if (!hasUserInteracted) {
            // The spinners have been loaded and the user has not yet interacted with them
            // In this case, we initialise the position of the to spinner on EUR
            for (int i = 0; i < mToSpinner.getCount(); i++) {
                if (mToSpinner.getItemAtPosition(i).equals(EURO)) {
                    mToSpinner.setSelection(i);
                    break;
                }
            }
        } else if (previouslySelectTo != null) {
            // Try to keep the selected "to" rate if possible
            for (int i = 0; i < mToSpinner.getCount(); i++) {
                if (mToSpinner.getItemAtPosition(i).equals(previouslySelectTo)) {
                    // reset the previously selected "To" currency
                    mToSpinner.setSelection(i);
                    break;
                }
            }
        }
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

        /* ----- FROM ITEMS ----- */
        /* From Adapter */
        mFromAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mFromCurrencies);
        mFromAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        /* From Spinner */
        mFromSpinner = (Spinner) view.findViewById(R.id.convertFromSpinner);
        mFromSpinner.setAdapter(mFromAdapter);
        mFromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) {}
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hasUserInteracted = true;
                updateToSpinner();
                updateConversion();
            }
        });
        /* From Text Input */
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

        /* ----- TO ITEMS ----- */
        /* To Adapter */
        mToAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mToCurrencies);
        mToAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        /* To Spinner */
        mToSpinner = (Spinner) view.findViewById(R.id.convertToSpinner);
        mToSpinner.setAdapter(mToAdapter);
        mToSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) {}
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hasUserInteracted = true;
                updateConversion();
            }
        });
        /* To Text */
        mToText = (TextView) view.findViewById(R.id.convertToText);
        mToText.setText("0.0");

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        if (rates != null) {
            List<Rate> ratesWithInverted = new ArrayList<>(rates);
            for (Rate rate : rates) {
                if (rate.getValue() > 0) {
                    // add inverted rates to be able to convert both ways
                    ratesWithInverted.add(new Rate(rate.getTo(), rate.getFrom(), 1. / rate.getValue()));
                }
            }

            for (Rate rate : ratesWithInverted) {
                addRate(rate);

                // If a rate is from or to BTC, add rates with mBTC
                if (rate.getFrom().equals(BITCOIN)) {
                    addRate(new Rate("mBTC", rate.getTo(), rate.getValue() / BITCOIN_TO_MBTC));
                } else if (rate.getTo().equals(BITCOIN)) {
                    addRate(new Rate(rate.getFrom(), "mBTC", rate.getValue() * BITCOIN_TO_MBTC));
                }
            }
            mFromAdapter.notifyDataSetChanged();

            updateToSpinner();
            updateConversion();
        }
    }

    private void addRate(Rate rate) {
        if (!mFromCurrencies.contains(rate.getFrom())) {
            mFromCurrencies.add(rate.getFrom());
        }
        // This will update "old" rates in case of a refresh
        mPairToRateMap.put(rate.getPair(), rate);
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

}
