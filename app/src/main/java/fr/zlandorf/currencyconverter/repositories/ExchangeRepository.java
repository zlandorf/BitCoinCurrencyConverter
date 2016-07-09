package fr.zlandorf.currencyconverter.repositories;

import com.google.common.collect.Lists;

import java.util.List;

import fr.zlandorf.currencyconverter.models.entities.Exchange;
import fr.zlandorf.currencyconverter.models.entities.Provider;
import fr.zlandorf.currencyconverter.tasks.rates.BitfinexRetrieveTask;
import fr.zlandorf.currencyconverter.tasks.rates.KrakenRetrieveTask;
import fr.zlandorf.currencyconverter.tasks.rates.YahooRetrieveTask;

public class ExchangeRepository {
    private List<Exchange> exchanges;

    public ExchangeRepository() {
        this.exchanges = Lists.newArrayList(
            new Exchange(Provider.Kraken, KrakenRetrieveTask.class),
            new Exchange(Provider.Bitfinex, BitfinexRetrieveTask.class),
            new Exchange(Provider.Yahoo, YahooRetrieveTask.class)
        );
    }

    public List<Exchange> getExchanges() {
        return exchanges;
    }
}
