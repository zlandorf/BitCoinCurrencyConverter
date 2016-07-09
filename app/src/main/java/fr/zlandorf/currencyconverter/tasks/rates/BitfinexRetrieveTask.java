package fr.zlandorf.currencyconverter.tasks.rates;

import com.google.common.collect.Lists;

import org.json.JSONObject;

import java.util.List;

import fr.zlandorf.currencyconverter.models.entities.Provider;
import fr.zlandorf.currencyconverter.models.entities.Rate;
import fr.zlandorf.currencyconverter.services.HttpService;
import fr.zlandorf.currencyconverter.tasks.RetrieveTask;

public class BitfinexRetrieveTask extends RetrieveTask {

    private static final String BITFINEX_URL = "https://api.bitfinex.com/v1/pubticker/";

    protected static final BitfinexPair[] PAIRS_TO_RETRIEVE = new BitfinexPair[]{
        BitfinexPair.BTC_USD,
        BitfinexPair.LTC_USD,
        BitfinexPair.LTC_BTC
    };

    public BitfinexRetrieveTask(RetrieveTaskListener listener, HttpService httpService) {
        super(listener, httpService);
    }

    @Override
    public Provider getProvider() {
        return Provider.Bitfinex;
    }

    @Override
    public List<Rate> retrieveRates() throws Exception {
        List<Rate> rates = Lists.newArrayList();
        for (BitfinexPair bitfinexPair : PAIRS_TO_RETRIEVE) {
            String rawResponse = httpService.request(BITFINEX_URL + bitfinexPair.getId());
            if (rawResponse != null) {
                double value = Double.valueOf(new JSONObject(rawResponse).getString("last_price"));
                rates.add(new Rate(bitfinexPair.getPair(), value));
            }
        }
        return rates;
    }
}
