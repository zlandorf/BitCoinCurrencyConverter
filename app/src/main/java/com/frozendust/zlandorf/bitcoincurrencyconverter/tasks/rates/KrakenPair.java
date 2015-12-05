package com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.rates;

import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Currency;
import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Pair;

public enum KrakenPair {
    BTC_EUR("XXBTZEUR", new Pair(Currency.BTC, Currency.EUR)),
    BTC_USD("XXBTZUSD", new Pair(Currency.BTC, Currency.USD)),
    BTC_CAD("XXBTZCAD", new Pair(Currency.BTC, Currency.CAD)),
    BTC_LTC("XXBTXLTC", new Pair(Currency.BTC, Currency.LTC)),
    LTC_EUR("XLTCZEUR", new Pair(Currency.LTC, Currency.EUR)),
    LTC_USD("XLTCZUSD", new Pair(Currency.LTC, Currency.USD));

    private String id;
    private Pair pair;

    KrakenPair(String id, Pair pair) {
        this.id = id;
        this.pair = pair;
    }

    public String getId() {
        return id;
    }

    public Pair getPair() {
        return pair;
    }

    public static KrakenPair getForId(String id) {
        for (KrakenPair krakenPair : KrakenPair.values()) {
            if (krakenPair.getId().equals(id)) {
                return krakenPair;
            }
        }

        return null;
    }
}
