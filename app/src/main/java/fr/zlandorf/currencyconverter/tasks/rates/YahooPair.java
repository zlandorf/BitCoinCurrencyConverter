package fr.zlandorf.currencyconverter.tasks.rates;

import com.google.common.collect.Lists;

import java.util.List;

import fr.zlandorf.currencyconverter.models.entities.Currency;
import fr.zlandorf.currencyconverter.models.entities.Pair;

public enum YahooPair {

    EUR_USD("EURUSD=X", new Pair(Currency.EUR, Currency.USD)),
    EUR_CAD("EURCAD=X", new Pair(Currency.EUR, Currency.CAD)),
    EUR_GBP("EURGBP=X", new Pair(Currency.EUR, Currency.GBP)),
    EUR_CNY("EURCNY=X", new Pair(Currency.EUR, Currency.CNY)),
    EUR_JPY("EURJPY=X", new Pair(Currency.EUR, Currency.JPY)),
    USD_CAD("USDCAD=X", new Pair(Currency.USD, Currency.CAD)),
    USD_GBP("USDGBP=X", new Pair(Currency.USD, Currency.GBP)),
    USD_CNY("USDCNY=X", new Pair(Currency.USD, Currency.CNY)),
    USD_JPY("USDJPY=X", new Pair(Currency.USD, Currency.JPY)),
    CAD_GBP("CADGBP=X", new Pair(Currency.CAD, Currency.GBP)),
    CAD_CNY("CADCNY=X", new Pair(Currency.CAD, Currency.CNY)),
    CAD_JPY("CADJPY=X", new Pair(Currency.CAD, Currency.JPY)),
    GBP_CNY("GBPCNY=X", new Pair(Currency.GBP, Currency.CNY)),
    GBP_JPY("GBPJPY=X", new Pair(Currency.GBP, Currency.JPY)),
    CNY_JPY("CNYJPY=X", new Pair(Currency.CNY, Currency.JPY));

    private Pair pair;
    private String id;

    YahooPair(String id, Pair pair) {
        this.id = id;
        this.pair = pair;
    }

    public String getId() {
        return id;
    }

    public Pair getPair() {
        return pair;
    }

    public static YahooPair getForId(String yahooId) {
        for (YahooPair yahooPair : YahooPair.values()) {
            if (yahooPair.getId().equals(yahooId)) {
                return yahooPair;
            }
        }
        return null;
    }

    public static List<Pair> getPairs() {
        List<Pair> pairs = Lists.newArrayList();

        for (YahooPair yahooPair : YahooPair.values())  {
            pairs.add(yahooPair.getPair());
        }

        return pairs;
    }
}
