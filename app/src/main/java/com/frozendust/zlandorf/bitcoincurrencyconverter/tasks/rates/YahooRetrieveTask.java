package com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.rates;

import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Rate;
import com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.HttpTask;
import com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.RetrieveTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This task retrieves fiat exchange rates from Yahoo
 *
 * Yahoo returns exchange rates in a CSV format
 *
 * @see <a href="http://download.finance.yahoo.com/d/quotes.csv?s=EURUSD=X,EURCAD=X,EURGBP=X,EURCNY=X,USDCAD=X,USDGBP=X,USDCNY=X,CADGBP=X,CADCNY=X,GBPCNY=X&f=sl1&e=.csv">http://download.finance.yahoo.com/d/quotes.csv?s=EURUSD=X,EURCAD=X,EURGBP=X,EURCNY=X,USDCAD=X,USDGBP=X,USDCNY=X,CADGBP=X,CADCNY=X,GBPCNY=X&f=sl1&e=.csv</a>
 *
 */
public class YahooRetrieveTask extends RetrieveTask {
    //EUR USD , EUR CNY , EUR GBP , USD CNY , USD GBP , GBP CNY
    protected static String YAHOO_URL = "http://download.finance.yahoo.com/d/quotes.csv?s=EURUSD=X,EURCAD=X,EURGBP=X,EURCNY=X,USDCAD=X,USDGBP=X,USDCNY=X,CADGBP=X,CADCNY=X,GBPCNY=X&f=sl1&e=.csv";

    Map<String, Rate> pairMap;

    public YahooRetrieveTask(RetrieveTaskListener listener) {
        super(listener);
        pairMap = new HashMap<>();
        //!\ Beware, when changing the values below, you must change the constant YAHOO_URL
        pairMap.put("EURUSD", new Rate("EUR", "USD"));
        pairMap.put("EURCAD", new Rate("EUR", "CAD"));
        pairMap.put("EURGBP", new Rate("EUR", "GBP"));
        pairMap.put("EURCNY", new Rate("EUR", "CNY"));
        pairMap.put("USDCAD", new Rate("USD", "CAD"));
        pairMap.put("USDGBP", new Rate("USD", "GBP"));
        pairMap.put("USDCNY", new Rate("USD", "CNY"));
        pairMap.put("CADBGP", new Rate("CAD", "GBP"));
        pairMap.put("CADCNY", new Rate("CAD", "CNY"));
        pairMap.put("GBPCNY", new Rate("GBP", "CNY"));
    }

    public List<Rate> retrieveRates() throws Exception {
        List<Rate> rates = new ArrayList<>();
        String rawResponse = (new HttpTask()).request(YAHOO_URL);

        for (String line : rawResponse.replaceAll("\"|=X", "").split("\n")) {
            String [] data = line.split(",");
            String pair = data[0];
            String from = pair.substring(0, 3);
            String to = pair.substring(3, 6);
            double value = Double.parseDouble(data[1]);

            rates.add(new Rate(from, to, value));
        }

        return rates;
    }
}
