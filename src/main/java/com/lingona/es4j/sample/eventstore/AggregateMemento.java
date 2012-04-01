package com.lingona.es4j.sample.eventstore;

public class AggregateMemento {

    private String value; // { get; set; }

    public AggregateMemento(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
