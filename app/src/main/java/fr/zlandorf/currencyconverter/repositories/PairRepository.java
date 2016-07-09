package fr.zlandorf.currencyconverter.repositories;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import fr.zlandorf.currencyconverter.models.entities.Pair;

public abstract class PairRepository {

    protected Map<String, Pair> pairsById;

    protected PairRepository() {
        this.pairsById = Maps.newHashMap();
        buildPairs();
    }

    protected abstract void buildPairs();

    public Pair getPair(String id) {
        if (pairsById.containsKey(id)) {
            return pairsById.get(id);
        }
        return null;
    }

    public Set<String> getPairIds() {
        return pairsById.keySet();
    }

    public Collection<Pair> getPairs() {
        return pairsById.values();
    }
}
