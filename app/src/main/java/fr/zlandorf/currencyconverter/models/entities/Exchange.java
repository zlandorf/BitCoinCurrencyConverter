package fr.zlandorf.currencyconverter.models.entities;

import fr.zlandorf.currencyconverter.tasks.RetrieveTask;

public class Exchange {
    private String name;
    private Class<? extends RetrieveTask> retrieveTaskClass;

    public Exchange(String name, Class<? extends RetrieveTask> retrieveTaskClass) {
        this.name = name;
        this.retrieveTaskClass = retrieveTaskClass;
    }

    public String getName() {
        return name;
    }

    public Class<? extends RetrieveTask> getRetrieveTaskClass() {
        return retrieveTaskClass;
    }

    @Override
    public String toString() {
        return name;
    }
}
