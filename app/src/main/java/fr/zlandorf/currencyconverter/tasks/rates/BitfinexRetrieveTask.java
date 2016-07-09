package fr.zlandorf.currencyconverter.tasks.rates;

import com.google.common.collect.Lists;

import org.json.JSONObject;

import java.util.List;

import fr.zlandorf.currencyconverter.models.entities.Exchange;
import fr.zlandorf.currencyconverter.models.entities.Rate;
import fr.zlandorf.currencyconverter.services.HttpService;
import fr.zlandorf.currencyconverter.tasks.RetrieveTask;

public class BitfinexRetrieveTask extends RetrieveTask {

    private static final String BITFINEX_URL = "https://api.bitfinex.com/v1/pubticker/";

    public BitfinexRetrieveTask(RetrieveTaskListener listener, HttpService httpService) {
        super(listener, httpService);
    }

    @Override
    public Exchange getExchange() {
        return Exchange.Bitfinex;
    }

    @Override
    public List<Rate> retrieveRates() throws Exception {
        List<Rate> rates = Lists.newArrayList();
        for (BitfinexPair bitfinexPair : BitfinexPair.values()) {
            String rawResponse = httpService.request(BITFINEX_URL + bitfinexPair.getId());
            if (rawResponse != null) {
                double value = Double.valueOf(new JSONObject(rawResponse).getString("last_price"));
                rates.add(new Rate(bitfinexPair.getPair(), value));
            }
        }
        return rates;
    }
}
