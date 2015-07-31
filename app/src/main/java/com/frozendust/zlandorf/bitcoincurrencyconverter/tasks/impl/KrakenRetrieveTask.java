package com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.impl;

import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Rate;
import com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.HttpTask;
import com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.RetrieveTask;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class KrakenRetrieveTask extends RetrieveTask {

    public static final String KRAKEN_URL = "https://api.kraken.com/0/public/Ticker?pair=XXBTZEUR,XXBTXLTC,XXBTZUSD,XLTCZEUR,XLTCZUSD";

    Map<String, Rate> pairMap;

    public KrakenRetrieveTask() {
        pairMap = new HashMap<>();
        //!\ Beware, when changing the values below, you must change the constant KRAKEN_URL
        pairMap.put("XXBTZEUR", new Rate("BTC", "EUR"));
        pairMap.put("XXBTXLTC", new Rate("BTC", "LTC"));
        pairMap.put("XXBTZUSD", new Rate("BTC", "USD"));
        pairMap.put("XXBTZUSD", new Rate("LTC", "EUR"));
        pairMap.put("XXBTZUSD", new Rate("LTC", "USD"));
    }

    @Override
    protected List<Rate> doInBackground(Void... params) {
        try {
            return retrieveRates();
        } catch (Exception e) {
            System.out.println("Error retrieving Kraken rates : ["+e.getMessage()+"]");
            e.printStackTrace();
        }
        return null;
    }

    protected List<Rate> retrieveRates() throws Exception {
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
