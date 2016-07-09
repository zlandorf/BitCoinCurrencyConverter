package fr.zlandorf.currencyconverter.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import fr.zlandorf.currencyconverter.models.exchanges.Exchange;
import fr.zlandorf.currencyconverter.services.HttpService;
import fr.zlandorf.currencyconverter.tasks.RetrieveTask;

public class RatesTaskFragment extends Fragment {
    protected RetrieveTask.RetrieveTaskListener mListener;
    protected RetrieveTask mTask;
    protected HttpService httpService;

    public static RatesTaskFragment newInstance() {
        return new RatesTaskFragment();
    }

    public RatesTaskFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (RetrieveTask.RetrieveTaskListener) context;

            if (mTask != null) {
                mTask.setListener(mListener);
            }

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement RetrieveTask.RetrieveTaskListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mTask = null;
        httpService = new HttpService();
    }

    public void execute(Exchange exchange) throws Exception {
        if (mTask != null) {
            // cancel previously running tasks
            if (mTask.getStatus() != AsyncTask.Status.FINISHED) {
                mTask.cancel(true);
            }
            mTask = null;
        }
        mTask = exchange.getRetrieveTaskClass()
            .getConstructor(Exchange.class, RetrieveTask.RetrieveTaskListener.class, HttpService.class )
            .newInstance(exchange, mListener, httpService);
        mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mTask.setListener(null);
    }

}
