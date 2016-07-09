package fr.zlandorf.currencyconverter.models.entities;

import fr.zlandorf.currencyconverter.tasks.RetrieveTask;

public class Exchange {
    private Provider provider;
    private Class<? extends RetrieveTask> retrieveTaskClass;

    public Exchange(Provider provider, Class<? extends RetrieveTask> retrieveTaskClass) {
        this.provider = provider;
        this.retrieveTaskClass = retrieveTaskClass;
    }

    public Provider getProvider() {
        return provider;
    }

    public Class<? extends RetrieveTask> getRetrieveTaskClass() {
        return retrieveTaskClass;
    }

    @Override
    public String toString() {
        return provider.getValue();
    }
}
