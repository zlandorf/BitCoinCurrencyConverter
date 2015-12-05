package com.frozendust.zlandorf.bitcoincurrencyconverter.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.frozendust.zlandorf.bitcoincurrencyconverter.services.HttpService;
import com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.RetrieveTask;
import com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.rates.KrakenRetrieveTask;
import com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.rates.YahooRetrieveTask;

import java.util.ArrayList;
import java.util.List;

public class RatesTaskFragment extends Fragment {
    protected RetrieveTask.RetrieveTaskListener mListener;
    protected List<RetrieveTask> mTasks;
    protected HttpService httpService;

    public static RatesTaskFragment newInstance() {
        return new RatesTaskFragment();
    }

    public RatesTaskFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (RetrieveTask.RetrieveTaskListener) activity;

            if (mTasks != null) {
                for (RetrieveTask task : mTasks) {
                    task.setListener(mListener);
                }
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

        mTasks = new ArrayList<>();
        httpService = new HttpService();
        execute();
    }

    public void execute() {
        if (mTasks != null) {
            // cancel previously running tasks
            for (RetrieveTask task : mTasks) {
                if (task.getStatus() != AsyncTask.Status.FINISHED) {
                    task.cancel(true);
                }
            }
            // clear tasks and run anew
            mTasks.clear();

            mTasks.add((RetrieveTask) new KrakenRetrieveTask(mListener, httpService).execute());
            mTasks.add((RetrieveTask) new YahooRetrieveTask(mListener, httpService).execute());
        } else {
            Log.e("TEST", "TASKS ARE NULL");
        }
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
