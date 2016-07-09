package fr.zlandorf.currencyconverter.models.entities;

public enum  Provider {

    Kraken("Kraken"),
    Bitfinex("Bitfinex"),
    Yahoo("Yahoo");

    private String value;

    Provider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
