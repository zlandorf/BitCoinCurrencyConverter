package fr.zlandorf.currencyconverter.tasks.rates;

import com.google.common.collect.Lists;

import java.util.List;

import fr.zlandorf.currencyconverter.models.entities.Currency;
import fr.zlandorf.currencyconverter.models.entities.Pair;

public enum BitfinexPair {
    BTC_USD("btcusd", new Pair(Currency.BTC, Currency.USD)),
    LTC_USD("ltcusd", new Pair(Currency.LTC, Currency.USD)),
    LTC_BTC("ltcbtc", new Pair(Currency.LTC, Currency.BTC));

    private String id;
    private Pair pair;

    BitfinexPair(String id, Pair pair) {
        this.id = id;
        this.pair = pair;
    }

    public String getId() {
        return id;
    }

    public Pair getPair() {
        return pair;
    }

    public static BitfinexPair getForId(String id) {
        for (BitfinexPair krakenPair : BitfinexPair.values()) {
            if (krakenPair.getId().equals(id)) {
                return krakenPair;
            }
        }

        return null;
    }

    public static List<Pair> getPairs() {
        List<Pair> pairs = Lists.newArrayList();

        for (BitfinexPair bitfinexPair : BitfinexPair.values())  {
            pairs.add(bitfinexPair.getPair());
        }

        return pairs;
    }
}
