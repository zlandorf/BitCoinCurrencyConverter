package fr.zlandorf.currencyconverter.repositories;

import com.google.common.collect.Lists;

import java.util.List;

import fr.zlandorf.currencyconverter.models.exchanges.Bitfinex;
import fr.zlandorf.currencyconverter.models.exchanges.Exchange;
import fr.zlandorf.currencyconverter.models.exchanges.Kraken;
import fr.zlandorf.currencyconverter.models.exchanges.Yahoo;

public class ExchangeRepository {
    private List<Exchange> exchanges;

    public ExchangeRepository() {
        this.exchanges = Lists.newArrayList(
            new Kraken(),
            new Bitfinex(),
            new Yahoo()
        );
    }

    public List<Exchange> getExchanges() {
        return exchanges;
    }
}
