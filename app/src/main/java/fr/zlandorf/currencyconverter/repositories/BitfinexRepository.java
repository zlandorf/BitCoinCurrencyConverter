package fr.zlandorf.currencyconverter.repositories;

import fr.zlandorf.currencyconverter.models.entities.Currency;
import fr.zlandorf.currencyconverter.models.entities.Pair;

public class BitfinexRepository extends PairRepository {
    @Override
    protected void buildPairs() {
        pairsById.put("btcusd", new Pair(Currency.BTC, Currency.USD));
        pairsById.put("ethusd", new Pair(Currency.ETH, Currency.USD));
        pairsById.put("ethbtc", new Pair(Currency.ETH, Currency.BTC));
        pairsById.put("ltcusd", new Pair(Currency.LTC, Currency.USD));
        pairsById.put("ltcbtc", new Pair(Currency.LTC, Currency.BTC));
    }
}
