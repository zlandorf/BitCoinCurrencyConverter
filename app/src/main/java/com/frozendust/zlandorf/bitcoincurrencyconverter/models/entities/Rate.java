package com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities;

/**
 * Created by zlandorf on 31/07/2015.
 */
public class Rate {
    private String from;
    private String to;
    private double value;

    public Rate(String from, String to) {
        this(from, to, 0);
    }

    public Rate(String from, String to, double value) {
        this.from = from;
        this.to = to;
        this.value = value;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
