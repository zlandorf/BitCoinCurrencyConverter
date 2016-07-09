package fr.zlandorf.currencyconverter.models.entities;

import java.util.List;

import fr.zlandorf.currencyconverter.tasks.rates.BitfinexPair;
import fr.zlandorf.currencyconverter.tasks.rates.KrakenPair;
import fr.zlandorf.currencyconverter.tasks.rates.YahooPair;

public enum  Provider {

    Kraken("Kraken", KrakenPair.getPairs()),
    Bitfinex("Bitfinex", BitfinexPair.getPairs()),
    Yahoo("Yahoo", YahooPair.getPairs());

    private String value;
    private List<Pair> pairs;

    Provider(String value, List<Pair> pairs) {
        this.value = value;
        this.pairs = pairs;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
