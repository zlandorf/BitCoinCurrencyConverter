package fr.zlandorf.currencyconverter.tasks.rates;

import android.util.Log;

import fr.zlandorf.currencyconverter.models.entities.Provider;
import fr.zlandorf.currencyconverter.models.entities.Rate;
import fr.zlandorf.currencyconverter.services.HttpService;
import fr.zlandorf.currencyconverter.tasks.RetrieveTask;
import com.google.common.base.Joiner;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This Task retrieves crypto/fiat exchange rates from Kraken's public ticker
 *
 * @see <a href="https://www.kraken.com/help/api#get-ticker-info">https://www.kraken.com/help/api#get-ticker-info</a>
 *
 */
public class KrakenRetrieveTask extends RetrieveTask {

    protected static final String[] PAIRS_TO_RETRIEVE = new String[]{
        KrakenPair.BTC_EUR.getId(),
        KrakenPair.BTC_USD.getId(),
        KrakenPair.BTC_CAD.getId(),
        KrakenPair.BTC_LTC.getId(),
        KrakenPair.LTC_EUR.getId(),
        KrakenPair.LTC_USD.getId()
    };

    protected static final String KRAKEN_URL = "https://api.kraken.com/0/public/Ticker?pair=" + Joiner.on(",").join(PAIRS_TO_RETRIEVE);

    public KrakenRetrieveTask(RetrieveTaskListener listener, HttpService httpService) {
        super(listener, httpService);
    }

    @Override
    public Provider getProvider() {
        return Provider.Kraken;
    }

    public List<Rate> retrieveRates() throws Exception {
        List<Rate> rates = new ArrayList<>();
        String rawResponse = httpService.request(KRAKEN_URL);
        if (rawResponse == null) return rates;
        JSONObject result = new JSONObject(rawResponse).getJSONObject("result");

        if (result == null) {
            Log.e("KrakenRetrieveTask", String.format("Failed to parse the response to JSON '%s'", rawResponse));
            return rates;
        }

        Iterator<String> pairIterator = result.keys();
        while (pairIterator.hasNext()) {
            String krakenPairId = pairIterator.next();
            KrakenPair krakenPair = KrakenPair.getForId(krakenPairId);
            if (krakenPair == null) {
                Log.w("KrakenRetrieveTask", String.format("The pair %s is not recognised", krakenPairId));
                continue;
            }
            JSONObject tickerInfo = result.getJSONObject(krakenPair.getId());
            double value = tickerInfo.getJSONArray("c").getDouble(0);
            rates.add(new Rate(krakenPair.getPair(), value));
        }

        return rates;
    }

}
