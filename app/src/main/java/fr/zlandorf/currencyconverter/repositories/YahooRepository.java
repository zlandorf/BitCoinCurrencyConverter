package fr.zlandorf.currencyconverter.repositories;

import fr.zlandorf.currencyconverter.models.entities.Currency;
import fr.zlandorf.currencyconverter.models.entities.Pair;

public class YahooRepository extends PairRepository {
    @Override
    protected void buildPairs() {
        pairsById.put("EURUSD=X", new Pair(Currency.EUR, Currency.USD));
        pairsById.put("EURCAD=X", new Pair(Currency.EUR, Currency.CAD));
        pairsById.put("EURGBP=X", new Pair(Currency.EUR, Currency.GBP));
        pairsById.put("EURCNY=X", new Pair(Currency.EUR, Currency.CNY));
        pairsById.put("EURJPY=X", new Pair(Currency.EUR, Currency.JPY));
        pairsById.put("USDCAD=X", new Pair(Currency.USD, Currency.CAD));
        pairsById.put("USDGBP=X", new Pair(Currency.USD, Currency.GBP));
        pairsById.put("USDCNY=X", new Pair(Currency.USD, Currency.CNY));
        pairsById.put("USDJPY=X", new Pair(Currency.USD, Currency.JPY));
        pairsById.put("CADGBP=X", new Pair(Currency.CAD, Currency.GBP));
        pairsById.put("CADCNY=X", new Pair(Currency.CAD, Currency.CNY));
        pairsById.put("CADJPY=X", new Pair(Currency.CAD, Currency.JPY));
        pairsById.put("GBPCNY=X", new Pair(Currency.GBP, Currency.CNY));
        pairsById.put("GBPJPY=X", new Pair(Currency.GBP, Currency.JPY));
        pairsById.put("CNYJPY=X", new Pair(Currency.CNY, Currency.JPY));
    }
}
