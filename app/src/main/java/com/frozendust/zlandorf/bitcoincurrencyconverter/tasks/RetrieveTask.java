package com.frozendust.zlandorf.bitcoincurrencyconverter.tasks;

import android.os.AsyncTask;

import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Rate;

import java.util.List;

public abstract class RetrieveTask extends AsyncTask<Void, Void, List<Rate>> {

    protected RetrieveTaskListener mListener;

    protected RetrieveTask(RetrieveTaskListener listener) {
        this.mListener = listener;
    }

    @Override
    protected void onPostExecute(List<Rate> rates) {
        super.onPostExecute(rates);
        if (rates != null && mListener != null) {
            mListener.onTaskFinished(rates);
        }
    }

    public void setListener(RetrieveTaskListener listener) {
        this.mListener = listener;
    }

    public interface RetrieveTaskListener {
        void onTaskFinished(List<Rate> rates);
    }
}
