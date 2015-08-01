package com.frozendust.zlandorf.bitcoincurrencyconverter.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Rate;

import java.util.List;

public abstract class RetrieveTask extends AsyncTask<Void, Void, List<Rate>> {

    public interface RetrieveTaskListener {
        void onTaskFinished(List<Rate> rates);
    }

    protected RetrieveTaskListener mListener;

    protected RetrieveTask(RetrieveTaskListener listener) {
        this.mListener = listener;
    }

    public void setListener(RetrieveTaskListener listener) {
        this.mListener = listener;
    }

    /**
     * Retrieves a list of exchange rates from an external server
     *
     * Retrieve tasks must implement this method
     *
     * @return a list {@link Rate}
     * @throws Exception
     */
    public abstract List<Rate> retrieveRates() throws Exception;

    @Override
    protected void onPostExecute(List<Rate> rates) {
        super.onPostExecute(rates);
        if (rates != null && mListener != null) {
            mListener.onTaskFinished(rates);
        }
    }

    @Override
    protected List<Rate> doInBackground(Void... params) {
        try {
            return retrieveRates();
        } catch (Exception e) {
            Log.e("RETRIEVE_TAKS", "Exception retrieving rates : [" + e.getMessage() + "] task : ["+getClass().getSimpleName()+"]");
            e.printStackTrace();
        }
        return null;
    }
}
