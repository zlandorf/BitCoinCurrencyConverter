package fr.zlandorf.currencyconverter.models.exchanges;

import fr.zlandorf.currencyconverter.repositories.PairRepository;
import fr.zlandorf.currencyconverter.tasks.RetrieveTask;

public class Exchange {
    private String name;
    private Class<? extends RetrieveTask> retrieveTaskClass;
    private PairRepository pairRepository;

    public Exchange(String name, Class<? extends RetrieveTask> retrieveTaskClass, PairRepository pairRepository) {
        this.name = name;
        this.retrieveTaskClass = retrieveTaskClass;
        this.pairRepository = pairRepository;
    }

    public String getName() {
        return name;
    }

    public Class<? extends RetrieveTask> getRetrieveTaskClass() {
        return retrieveTaskClass;
    }

    public PairRepository getPairRepository() {
        return pairRepository;
    }

    @Override
    public String toString() {
        return getName();
    }
}
