package com.frozendust.zlandorf.bitcoincurrencyconverter.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.frozendust.zlandorf.bitcoincurrencyconverter.R;
import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Rate;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConverterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConverterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConverterFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    private List<String> mFromCurrencies;
    private List<String> mToCurrencies;

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
        mFromCurrencies = new ArrayList<>();
        mToCurrencies = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_converter, container, false);

        Spinner fromSpinner = (Spinner) view.findViewById(R.id.convertFromSpinner);
        fromSpinner.setAdapter(new ArrayAdapter<>(view.getContext(), R.layout.support_simple_spinner_dropdown_item, mFromCurrencies));

        Spinner toSpinner = (Spinner) view.findViewById(R.id.convertToSpinner);
        toSpinner.setAdapter(new ArrayAdapter<>(view.getContext(), R.layout.support_simple_spinner_dropdown_item, mToCurrencies));

        TextView fromText = (TextView) view.findViewById(R.id.convertFromText);
        fromText.setText("1.0");

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
        Spinner fromSpinner = (Spinner) getView().findViewById(R.id.convertFromSpinner);
        Spinner toSpinner = (Spinner) getView().findViewById(R.id.convertToSpinner);

        if (fromSpinner != null && toSpinner != null && rates != null) {
            ArrayAdapter<String> fromAdapter = (ArrayAdapter<String>) fromSpinner.getAdapter();
            ArrayAdapter<String> toAdapter = (ArrayAdapter<String>) toSpinner.getAdapter();

            for (Rate rate : rates) {
                if (!mFromCurrencies.contains(rate.getFrom())) {
                    mFromCurrencies.add(rate.getFrom());
                }
                if (!mToCurrencies.contains(rate.getTo())) {
                    mToCurrencies.add(rate.getTo());
                }
            }

            fromAdapter.notifyDataSetChanged();
            toAdapter.notifyDataSetChanged();
        }
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
        public void onFragmentInteraction(Uri uri);
    }

}
