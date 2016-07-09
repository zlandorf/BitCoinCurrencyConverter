package fr.zlandorf.currencyconverter.tasks.rates;

import com.google.common.collect.Lists;

import java.util.List;

import fr.zlandorf.currencyconverter.models.entities.Currency;
import fr.zlandorf.currencyconverter.models.entities.Pair;

public enum KrakenPair {
    BTC_EUR("XXBTZEUR", new Pair(Currency.BTC, Currency.EUR)),
    BTC_USD("XXBTZUSD", new Pair(Currency.BTC, Currency.USD)),
    BTC_CAD("XXBTZCAD", new Pair(Currency.BTC, Currency.CAD)),
    BTC_GBP("XXBTZGBP", new Pair(Currency.BTC, Currency.GBP)),
    BTC_JPY("XXBTZJPY", new Pair(Currency.BTC, Currency.JPY)),
    BTC_LTC("XXBTXLTC", new Pair(Currency.BTC, Currency.LTC)),
    LTC_EUR("XLTCZEUR", new Pair(Currency.LTC, Currency.EUR)),
    LTC_USD("XLTCZUSD", new Pair(Currency.LTC, Currency.USD)),
    LTC_CAD("XLTCZCAD", new Pair(Currency.LTC, Currency.CAD)),
    ETH_BTC("XETHXXBT", new Pair(Currency.ETH, Currency.BTC)),
    ETH_CAD("XETHZCAD", new Pair(Currency.ETH, Currency.CAD)),
    ETH_EUR("XETHZEUR", new Pair(Currency.ETH, Currency.EUR)),
    ETH_GBP("XETHZGBP", new Pair(Currency.ETH, Currency.GBP)),
    ETH_JPY("XETHZJPY", new Pair(Currency.ETH, Currency.JPY)),
    ETH_USD("XETHZUSD", new Pair(Currency.ETH, Currency.USD));

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

    public static List<Pair> getPairs() {
        List<Pair> pairs = Lists.newArrayList();

        for (KrakenPair krakenPair : KrakenPair.values())  {
            pairs.add(krakenPair.getPair());
        }

        return pairs;
    }
}
