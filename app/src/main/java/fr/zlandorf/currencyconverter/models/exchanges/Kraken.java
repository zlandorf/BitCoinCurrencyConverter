package fr.zlandorf.currencyconverter.models.exchanges;

import fr.zlandorf.currencyconverter.repositories.KrakenRepository;
import fr.zlandorf.currencyconverter.tasks.rates.KrakenRetrieveTask;

public class Kraken extends Exchange {
    public Kraken() {
        super("Kraken", KrakenRetrieveTask.class, new KrakenRepository());
    }
}
