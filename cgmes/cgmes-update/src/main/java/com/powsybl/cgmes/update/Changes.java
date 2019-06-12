package com.powsybl.cgmes.update;

import java.util.Objects;

import com.powsybl.iidm.network.Identifiable;

public class Changes {

    // store network changes

    public Changes(Identifiable identifiable, String attribute, Object oldValue, Object newValue, String variant) {
        this.identifiable = Objects.requireNonNull(identifiable);
        this.attribute = Objects.requireNonNull(attribute);
        this.oldValue = Objects.requireNonNull(oldValue);
        this.newValue = Objects.requireNonNull(newValue);
        this.variant = Objects.requireNonNull(variant);
    }

    public Changes(Identifiable identifiable, String variant) {
        this.identifiable = Objects.requireNonNull(identifiable);
        this.variant = Objects.requireNonNull(variant);
    }

    private final Identifiable identifiable;
    private String attribute;
    private Object oldValue;
    private Object newValue;
    private final String variant;

}
