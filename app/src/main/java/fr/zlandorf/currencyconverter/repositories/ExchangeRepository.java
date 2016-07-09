package fr.zlandorf.currencyconverter.repositories;

import com.google.common.collect.Lists;

import java.util.List;

import fr.zlandorf.currencyconverter.models.entities.Exchange;

public class ExchangeRepository {
    private List<Exchange> exchanges;

    public ExchangeRepository() {
        this.exchanges = Lists.newArrayList(Exchange.values());
    }

    public List<Exchange> getExchanges() {
        return exchanges;
    }
}
