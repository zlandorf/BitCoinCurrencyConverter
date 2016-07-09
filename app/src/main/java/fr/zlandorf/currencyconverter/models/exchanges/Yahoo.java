package fr.zlandorf.currencyconverter.models.exchanges;

import fr.zlandorf.currencyconverter.repositories.YahooRepository;
import fr.zlandorf.currencyconverter.tasks.rates.YahooRetrieveTask;

public class Yahoo extends Exchange {
    public Yahoo() {
        super("Yahoo", YahooRetrieveTask.class, new YahooRepository());
    }
}
