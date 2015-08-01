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
    protected static String YAHOO_URL = "http://download.finance.yahoo.com/d/quotes.csv?s="+
            "EURUSD=X,"+
            "EURCAD=X,"+
            "EURGBP=X,"+
            "EURCNY=X,"+
            "USDCAD=X,"+
            "USDGBP=X,"+
            "USDCNY=X,"+
            "CADGBP=X,"+
            "CADCNY=X,"+
            "GBPCNY=X"+
            "&f=sl1&e=.csv";

    Map<String, Rate> mPairMap;

    public YahooRetrieveTask(RetrieveTaskListener listener) {
        super(listener);
        mPairMap = new HashMap<>();
        //!\ Beware, when changing the values below, you must change the constant YAHOO_URL
        mPairMap.put("EURUSD", new Rate("EUR", "USD"));
        mPairMap.put("EURCAD", new Rate("EUR", "CAD"));
        mPairMap.put("EURGBP", new Rate("EUR", "GBP"));
        mPairMap.put("EURCNY", new Rate("EUR", "CNY"));
        mPairMap.put("USDCAD", new Rate("USD", "CAD"));
        mPairMap.put("USDGBP", new Rate("USD", "GBP"));
        mPairMap.put("USDCNY", new Rate("USD", "CNY"));
        mPairMap.put("CADGBP", new Rate("CAD", "GBP"));
        mPairMap.put("CADCNY", new Rate("CAD", "CNY"));
        mPairMap.put("GBPCNY", new Rate("GBP", "CNY"));
    }

    public List<Rate> retrieveRates() throws Exception {
        List<Rate> rates = new ArrayList<>();
        String rawResponse = (new HttpTask()).request(YAHOO_URL);

        for (String line : rawResponse.replaceAll("\"|=X", "").split("\n")) {
            String [] data = line.split(",");
            String pair = data[0];
            double value = Double.parseDouble(data[1]);

            Rate rate = mPairMap.get(pair);
            if (rate != null) {
                rate.setValue(value);
                rates.add(rate);
            }
        }
        return rates;
    }
}
