package com.frozendust.zlandorf.bitcoincurrencyconverter.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.frozendust.zlandorf.bitcoincurrencyconverter.R;
import com.frozendust.zlandorf.bitcoincurrencyconverter.adapters.RateAdapter;
import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Rate;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link RateListListener}
 * interface.
 */
public class RateListFragment extends Fragment implements AbsListView.OnItemClickListener {

    private RateListListener mListener;
    private AbsListView mListView;
    private RateAdapter mAdapter;
    private List<Rate> mRates;

    public static RateListFragment newInstance() {
        return new RateListFragment();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RateListFragment() {
    }

    public void onRatesRetrieved(List<Rate> rates) {
        Log.d("RATE_LIST_FRAGMENT", String.format("Rates received : %s\n", rates.size()));
        mainLoop: for (Rate rate : rates) {
            for (Rate existingRate : mRates) {
                if (existingRate.getFrom().equals(rate.getFrom())
                    && existingRate.getTo().equals(rate.getTo())) {
                    Log.d("RATE_LIST_FRAGMENT", String.format("updating existing rate [%s] with new values [%s]\n", existingRate, rate));
                    // Rate already listed, update it and go to next rate
                    existingRate.setValue(rate.getValue());
                    continue mainLoop;
                }
            }
            // rate not listed yet
            mRates.add(rate);
            Log.d("RATE_LIST_FRAGMENT", String.format("added rate: %s\n", rate));
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mRates = new ArrayList<>();
        mAdapter = new RateAdapter(getActivity(), android.R.layout.simple_list_item_1, mRates);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rate_list, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(R.id.rate_list);
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(this);
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (RateListListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RateListListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onRateSelected(mRates.get(position));
        }
    }

    public interface RateListListener {
        void onRateSelected(Rate rate);
    }
}
