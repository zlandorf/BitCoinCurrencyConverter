package fr.zlandorf.currencyconverter.models.entities;

public class Rate {
    private Pair pair;
    private double value;

    public Rate(Pair pair, double value) {
        this.pair = pair;
        this.value = value;
    }

    public Pair getPair() {
        return pair;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s/%s : %f", pair.getFrom(), pair.getTo(), value);
    }
}
