package com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities;

public class Pair {
    private Currency from;
    private Currency to;

    public Pair(Currency from, Currency to) {
        this.from = from;
        this.to = to;
    }

    public Currency getFrom() {
        return from;
    }

    public Currency getTo() {
        return to;
    }

    @Override
    public int hashCode() {
        return (from.getValue() + to.getValue()).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Pair) {
            Pair other = (Pair) o;
            return from.equals(other.from) && to.equals(other.to);
        }
        return false;
    }

    public Pair invert() {
        return new Pair(to, from);
    }
}
