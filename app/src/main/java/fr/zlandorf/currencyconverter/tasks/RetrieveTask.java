package fr.zlandorf.currencyconverter.tasks;

import android.os.AsyncTask;
import android.util.Log;

import fr.zlandorf.currencyconverter.models.entities.Rate;

import java.util.List;

public abstract class RetrieveTask extends AsyncTask<Void, Void, List<Rate>> {

    public interface RetrieveTaskListener {
        void onTaskFinished(List<Rate> rates);
        void onTaskFailed(String provider);
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

    public abstract String getProviderName();

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
            mListener.onTaskFailed(getProviderName());
            Log.e("RETRIEVE_TASK", "Exception retrieving rates : [" + e.getMessage() + "] task : ["+getClass().getSimpleName()+"]");
            e.printStackTrace();
        }
        return null;
    }
}
