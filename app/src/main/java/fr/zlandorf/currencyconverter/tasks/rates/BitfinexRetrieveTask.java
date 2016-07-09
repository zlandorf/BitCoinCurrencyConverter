package fr.zlandorf.currencyconverter.tasks.rates;

import com.google.common.collect.Lists;

import org.json.JSONObject;

import java.util.List;

import fr.zlandorf.currencyconverter.models.entities.Rate;
import fr.zlandorf.currencyconverter.models.exchanges.Exchange;
import fr.zlandorf.currencyconverter.services.HttpService;
import fr.zlandorf.currencyconverter.tasks.RetrieveTask;

public class BitfinexRetrieveTask extends RetrieveTask {

    private static final String BITFINEX_URL = "https://api.bitfinex.com/v1/pubticker/";

    public BitfinexRetrieveTask(Exchange exchange, RetrieveTaskListener listener, HttpService httpService) {
        super(exchange, listener, httpService);
    }

    @Override
    public List<Rate> retrieveRates() throws Exception {
        List<Rate> rates = Lists.newArrayList();
        for (String id : exchange.getPairRepository().getPairIds()) {
            String rawResponse = httpService.request(BITFINEX_URL + id);
            if (rawResponse != null) {
                double value = Double.valueOf(new JSONObject(rawResponse).getString("last_price"));
                rates.add(new Rate(exchange.getPairRepository().getPair(id), value));
            }
        }
        return rates;
    }
}
