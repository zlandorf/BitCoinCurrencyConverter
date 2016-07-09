package fr.zlandorf.currencyconverter.tasks.rates;

import android.util.Log;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

import fr.zlandorf.currencyconverter.models.exchanges.Exchange;
import fr.zlandorf.currencyconverter.models.entities.Pair;
import fr.zlandorf.currencyconverter.models.entities.Rate;
import fr.zlandorf.currencyconverter.services.HttpService;
import fr.zlandorf.currencyconverter.tasks.RetrieveTask;

/**
 * This task retrieves fiat exchange rates from Yahoo
 *
 * Yahoo returns exchange rates in a CSV format
 *
 * @see <a href="http://download.finance.yahoo.com/d/quotes.csv?s=EURUSD=X,EURCAD=X,EURGBP=X,EURCNY=X,USDCAD=X,USDGBP=X,USDCNY=X,CADGBP=X,CADCNY=X,GBPCNY=X&f=sl1&e=.csv">http://download.finance.yahoo.com/d/quotes.csv?s=EURUSD=X,EURCAD=X,EURGBP=X,EURCNY=X,USDCAD=X,USDGBP=X,USDCNY=X,CADGBP=X,CADCNY=X,GBPCNY=X&f=sl1&e=.csv</a>
 *
 */
public class YahooRetrieveTask extends RetrieveTask {

    protected static String YAHOO_URL_TEMPLATE = "http://download.finance.yahoo.com/d/quotes.csv?s=%s&f=sl1&e=.csv";

    public YahooRetrieveTask(Exchange exchange, RetrieveTaskListener listener, HttpService httpService) {
        super(exchange, listener, httpService);
    }

    public List<Rate> retrieveRates() throws Exception {
        List<Rate> rates = new ArrayList<>();
        String rawResponse = httpService.request(String.format(YAHOO_URL_TEMPLATE, Joiner.on(",").join(exchange.getPairRepository().getPairIds())));
        if (rawResponse == null) return rates;

        for (String line : rawResponse.split("\n")) {
            String [] data = line.split(",");
            if (data.length < 2) {
                Log.w("YahooRetrieveTask", String.format("Yahoo data should have at least two columns '%s'", line));
                continue;
            }
            String yahooPairId = data[0].replace("\"", "");
            Pair pair = exchange.getPairRepository().getPair(yahooPairId);

            if (pair == null) {
                Log.w("YahooRetrieveTask", String.format("The pair %s is not recognised", yahooPairId));
                continue;
            }

            double value = Double.parseDouble(data[1]);
            rates.add(new Rate(pair, value));
        }
        return rates;
    }

}
