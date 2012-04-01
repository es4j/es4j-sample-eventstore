package com.lingona.es4j.sample.eventstore;


public class SomeDomainEvent {

    private String value; // { get; set; }

    SomeDomainEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
