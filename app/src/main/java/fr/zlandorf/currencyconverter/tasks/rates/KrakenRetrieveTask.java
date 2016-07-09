package fr.zlandorf.currencyconverter.tasks.rates;

import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.zlandorf.currencyconverter.models.entities.Exchange;
import fr.zlandorf.currencyconverter.models.entities.Rate;
import fr.zlandorf.currencyconverter.services.HttpService;
import fr.zlandorf.currencyconverter.tasks.RetrieveTask;

/**
 * This Task retrieves crypto/fiat exchange rates from Kraken's public ticker
 *
 * @see <a href="https://www.kraken.com/help/api#get-ticker-info">https://www.kraken.com/help/api#get-ticker-info</a>
 *
 */
public class KrakenRetrieveTask extends RetrieveTask {

    protected static List<String> getPairIds() {
        List<String> ids = Lists.newArrayList();
        for (KrakenPair krakenPair : KrakenPair.values()) {
            ids.add(krakenPair.getId());
        }
        return ids;
    }

    protected static final String KRAKEN_URL = "https://api.kraken.com/0/public/Ticker?pair=" + Joiner.on(",").join(getPairIds());

    public KrakenRetrieveTask(RetrieveTaskListener listener, HttpService httpService) {
        super(listener, httpService);
    }

    @Override
    public Exchange getExchange() {
        return Exchange.Kraken;
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
