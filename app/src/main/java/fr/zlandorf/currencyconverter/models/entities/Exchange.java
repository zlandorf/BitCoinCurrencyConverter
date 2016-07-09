package fr.zlandorf.currencyconverter.models.entities;

import java.util.List;

import fr.zlandorf.currencyconverter.tasks.RetrieveTask;
import fr.zlandorf.currencyconverter.tasks.rates.BitfinexPair;
import fr.zlandorf.currencyconverter.tasks.rates.BitfinexRetrieveTask;
import fr.zlandorf.currencyconverter.tasks.rates.KrakenPair;
import fr.zlandorf.currencyconverter.tasks.rates.KrakenRetrieveTask;
import fr.zlandorf.currencyconverter.tasks.rates.YahooPair;
import fr.zlandorf.currencyconverter.tasks.rates.YahooRetrieveTask;

public enum Exchange {
    Kraken("Kraken", KrakenRetrieveTask.class, KrakenPair.getPairs()),
    Bitfinex("Bitfinex", BitfinexRetrieveTask.class, BitfinexPair.getPairs()),
    Yahoo("Yahoo", YahooRetrieveTask.class, YahooPair.getPairs());

    private String name;
    private Class<? extends RetrieveTask> retrieveTaskClass;
    private List<Pair> pairs;

    Exchange(String name, Class<? extends RetrieveTask> retrieveTaskClass, List<Pair> pairs) {
        this.name = name;
        this.retrieveTaskClass = retrieveTaskClass;
        this.pairs = pairs;
    }

    public String getName() {
        return name;
    }

    public Class<? extends RetrieveTask> getRetrieveTaskClass() {
        return retrieveTaskClass;
    }

    public List<Pair> getPairs() {
        return pairs;
    }

    @Override
    public String toString() {
        return getName();
    }
}
