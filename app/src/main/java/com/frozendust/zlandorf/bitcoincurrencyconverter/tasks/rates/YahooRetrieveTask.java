package com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.rates;

import android.util.Log;

import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Rate;
import com.frozendust.zlandorf.bitcoincurrencyconverter.services.HttpService;
import com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.RetrieveTask;
import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

/**
 * This task retrieves fiat exchange rates from Yahoo
 *
 * Yahoo returns exchange rates in a CSV format
 *
 * @see <a href="http://download.finance.yahoo.com/d/quotes.csv?s=EURUSD=X,EURCAD=X,EURGBP=X,EURCNY=X,USDCAD=X,USDGBP=X,USDCNY=X,CADGBP=X,CADCNY=X,GBPCNY=X&f=sl1&e=.csv">http://download.finance.yahoo.com/d/quotes.csv?s=EURUSD=X,EURCAD=X,EURGBP=X,EURCNY=X,USDCAD=X,USDGBP=X,USDCNY=X,CADGBP=X,CADCNY=X,GBPCNY=X&f=sl1&e=.csv</a>
 *
 */
public class YahooRetrieveTask extends RetrieveTask {

    protected static final String[] PAIRS_TO_RETRIEVE = new String[]{
        YahooPair.EUR_USD.getId(),
        YahooPair.EUR_CAD.getId(),
        YahooPair.EUR_GBP.getId(),
        YahooPair.EUR_CNY.getId(),
        YahooPair.USD_CAD.getId(),
        YahooPair.USD_GBP.getId(),
        YahooPair.USD_CNY.getId(),
        YahooPair.CAD_GBP.getId(),
        YahooPair.CAD_CNY.getId(),
        YahooPair.GBP_CNY.getId(),
    };

    //EUR USD , EUR CNY , EUR GBP , USD CNY , USD GBP , GBP CNY
    protected static String YAHOO_URL = "http://download.finance.yahoo.com/d/quotes.csv?s=" + Joiner.on(",").join(PAIRS_TO_RETRIEVE) + "&f=sl1&e=.csv";

    private HttpService httpService;

    public YahooRetrieveTask(RetrieveTaskListener listener, HttpService httpService) {
        super(listener);
        this.httpService = httpService;
    }

    @Override
    public String getProviderName() {
        return "Yahoo";
    }

    public List<Rate> retrieveRates() throws Exception {
        List<Rate> rates = new ArrayList<>();
        String rawResponse = httpService.request(YAHOO_URL);
        if (rawResponse == null) return rates;

        for (String line : rawResponse.split("\n")) {
            String [] data = line.split(",");
            if (data.length < 2) {
                Log.w("YahooRetrieveTask", String.format("Yahoo data should have at least two columns '%s'", line));
                continue;
            }
            String yahooPairId = data[0].replace("\"", "");
            YahooPair yahooPair = YahooPair.getForId(yahooPairId);

            if (yahooPair == null) {
                Log.w("YahooRetrieveTask", String.format("The pair %s is not recognised", yahooPairId));
                continue;
            }

            double value = Double.parseDouble(data[1]);
            rates.add(new Rate(yahooPair.getPair(), value));
        }
        return rates;
    }

}
