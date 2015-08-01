package com.frozendust.zlandorf.bitcoincurrencyconverter.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.RetrieveTask;
import com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.rates.KrakenRetrieveTask;
import com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.rates.YahooRetrieveTask;

import java.util.ArrayList;
import java.util.List;

public class RatesTaskFragment extends Fragment {
    protected RetrieveTask.RetrieveTaskListener mListener;
    protected List<RetrieveTask> mTasks;

    public static RatesTaskFragment newInstance() {
        return new RatesTaskFragment();
    }

    public RatesTaskFragment() {
        mTasks = new ArrayList<>();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (RetrieveTask.RetrieveTaskListener) activity;

            for (RetrieveTask task : mTasks) {
                task.setListener(mListener);
            }

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RetrieveTask.RetrieveTaskListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        //TODO : when implementing the refresh feature, make sure to not refresh an ongoing task
        mTasks.add((RetrieveTask) new KrakenRetrieveTask(mListener).execute());
        mTasks.add((RetrieveTask) new YahooRetrieveTask(mListener).execute());
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        for (RetrieveTask task : mTasks) {
            task.setListener(null);
        }
    }
}
