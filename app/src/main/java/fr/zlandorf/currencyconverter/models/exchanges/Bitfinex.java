package fr.zlandorf.currencyconverter.models.exchanges;

import fr.zlandorf.currencyconverter.repositories.BitfinexRepository;
import fr.zlandorf.currencyconverter.tasks.rates.BitfinexRetrieveTask;

public class Bitfinex extends Exchange {

    public Bitfinex() {
        super("Bitfinex",BitfinexRetrieveTask.class, new BitfinexRepository());
    }
}
