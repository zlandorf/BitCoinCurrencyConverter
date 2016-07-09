package fr.zlandorf.currencyconverter.tasks.rates;

import android.util.Log;

import com.google.common.base.Joiner;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.zlandorf.currencyconverter.models.exchanges.Exchange;
import fr.zlandorf.currencyconverter.models.entities.Pair;
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

    protected static final String KRAKEN_BASE_URL = "https://api.kraken.com/0/public/Ticker?pair=";

    public KrakenRetrieveTask(Exchange exchange, RetrieveTaskListener listener, HttpService httpService) {
        super(exchange, listener, httpService);
    }

    public List<Rate> retrieveRates() throws Exception {
        List<Rate> rates = new ArrayList<>();
        String rawResponse = httpService.request(KRAKEN_BASE_URL + Joiner.on(",").join(exchange.getPairRepository().getPairIds()));
        if (rawResponse == null) return rates;
        JSONObject result = new JSONObject(rawResponse).getJSONObject("result");

        if (result == null) {
            Log.e("KrakenRetrieveTask", String.format("Failed to parse the response to JSON '%s'", rawResponse));
            return rates;
        }

        Iterator<String> pairIterator = result.keys();
        while (pairIterator.hasNext()) {
            String krakenPairId = pairIterator.next();
            Pair pair = exchange.getPairRepository().getPair(krakenPairId);
            if (pair == null) {
                Log.w("KrakenRetrieveTask", String.format("The pair %s is not recognised", krakenPairId));
                continue;
            }
            JSONObject tickerInfo = result.getJSONObject(krakenPairId);
            double value = tickerInfo.getJSONArray("c").getDouble(0);
            rates.add(new Rate(pair, value));
        }

        return rates;
    }

}
