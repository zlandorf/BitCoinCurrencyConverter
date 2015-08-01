package com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.rates;

import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Rate;
import com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.HttpTask;
import com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.RetrieveTask;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This Task retrieves crypto/fiat exchange rates from Kraken's public ticker
 *
 * @see <a href="https://www.kraken.com/help/api#get-ticker-info">https://www.kraken.com/help/api#get-ticker-info</a>
 *
 */
public class KrakenRetrieveTask extends RetrieveTask {
    protected static final String KRAKEN_URL =
        "https://api.kraken.com/0/public/Ticker?pair="+
            "XXBTZEUR,"+
            "XXBTZUSD,"+
            "XXBTZCAD,"+
            "XXBTXLTC,"+
            "XLTCZEUR,"+
            "XLTCZUSD";

    Map<String, Rate> pairMap;

    public KrakenRetrieveTask(RetrieveTaskListener listener) {
        super(listener);
        pairMap = new HashMap<>();
        //!\ Beware, when changing the values below, you must change the constant KRAKEN_URL
        pairMap.put("XXBTZEUR", new Rate("BTC", "EUR"));
        pairMap.put("XXBTZUSD", new Rate("BTC", "USD"));
        pairMap.put("XXBTZCAD", new Rate("BTC", "CAD"));
        pairMap.put("XXBTXLTC", new Rate("BTC", "LTC"));
        pairMap.put("XLTCZEUR", new Rate("LTC", "EUR"));
        pairMap.put("XLTCZUSD", new Rate("LTC", "USD"));
    }

    public List<Rate> retrieveRates() throws Exception {
        List<Rate> rates = new ArrayList<>();
        String rawResponse = (new HttpTask()).request(KRAKEN_URL);
        JSONObject result = new JSONObject(rawResponse).getJSONObject("result");

        if (result == null) {
            return rates;
        }

        Iterator<String> pairIterator = result.keys();
        while (pairIterator.hasNext()) {
            String krakenPairName = pairIterator.next();
            Rate rate = pairMap.get(krakenPairName);

            if (null == rate) {
                // Kraken Pair not mapped to a Pair
                continue;
            }

            JSONObject tickerInfo = result.getJSONObject(krakenPairName);
            rate.setValue(tickerInfo.getJSONArray("c").getDouble(0));
            rates.add(rate);
        }

        return rates;
    }
}
