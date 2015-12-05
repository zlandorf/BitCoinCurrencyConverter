package com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.rates;

import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Currency;
import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Pair;

public enum YahooPair {

    EUR_USD("EURUSD=X", new Pair(Currency.EUR, Currency.USD)),
    EUR_CAD("EURCAD=X", new Pair(Currency.EUR, Currency.CAD)),
    EUR_GBP("EURGBP=X", new Pair(Currency.EUR, Currency.GBP)),
    EUR_CNY("EURCNY=X", new Pair(Currency.EUR, Currency.CNY)),
    USD_CAD("USDCAD=X", new Pair(Currency.USD, Currency.CAD)),
    USD_GBP("USDGBP=X", new Pair(Currency.USD, Currency.GBP)),
    USD_CNY("USDCNY=X", new Pair(Currency.USD, Currency.CNY)),
    CAD_GBP("CADGBP=X", new Pair(Currency.CAD, Currency.GBP)),
    CAD_CNY("CADCNY=X", new Pair(Currency.CAD, Currency.CNY)),
    GBP_CNY("GBPCNY=X", new Pair(Currency.GBP, Currency.CNY));

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
}
