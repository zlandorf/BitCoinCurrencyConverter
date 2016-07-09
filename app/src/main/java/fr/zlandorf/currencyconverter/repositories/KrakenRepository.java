package fr.zlandorf.currencyconverter.repositories;

import fr.zlandorf.currencyconverter.models.entities.Currency;
import fr.zlandorf.currencyconverter.models.entities.Pair;

public class KrakenRepository extends PairRepository {
    protected void buildPairs() {
        pairsById.put("XXBTZEUR", new Pair(Currency.BTC, Currency.EUR));
        pairsById.put("XXBTZUSD", new Pair(Currency.BTC, Currency.USD));
        pairsById.put("XXBTZCAD", new Pair(Currency.BTC, Currency.CAD));
        pairsById.put("XXBTZGBP", new Pair(Currency.BTC, Currency.GBP));
        pairsById.put("XXBTZJPY", new Pair(Currency.BTC, Currency.JPY));
        pairsById.put("XXBTXLTC", new Pair(Currency.BTC, Currency.LTC));
        pairsById.put("XLTCZEUR", new Pair(Currency.LTC, Currency.EUR));
        pairsById.put("XLTCZUSD", new Pair(Currency.LTC, Currency.USD));
        pairsById.put("XLTCZCAD", new Pair(Currency.LTC, Currency.CAD));
        pairsById.put("XETHXXBT", new Pair(Currency.ETH, Currency.BTC));
        pairsById.put("XETHZCAD", new Pair(Currency.ETH, Currency.CAD));
        pairsById.put("XETHZEUR", new Pair(Currency.ETH, Currency.EUR));
        pairsById.put("XETHZGBP", new Pair(Currency.ETH, Currency.GBP));
        pairsById.put("XETHZJPY", new Pair(Currency.ETH, Currency.JPY));
        pairsById.put("XETHZUSD", new Pair(Currency.ETH, Currency.USD));
    }
}
