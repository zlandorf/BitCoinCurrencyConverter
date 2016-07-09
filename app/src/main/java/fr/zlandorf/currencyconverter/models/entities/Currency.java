package fr.zlandorf.currencyconverter.models.entities;

public enum  Currency {
    BTC("BTC"),
    EUR("EUR"),
    USD("USD"),
    CAD("CAD"),
    LTC("LTC"),
    GBP("GBP"),
    CNY("CNY"),
    ETH("ETC"),
    JPY("JPY"),
    mBTC("mBTC");

    private String value;

    Currency(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
