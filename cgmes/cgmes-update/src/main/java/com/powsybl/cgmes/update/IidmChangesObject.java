package com.powsybl.cgmes.update;

import java.util.Objects;

import com.powsybl.iidm.network.Identifiable;

public class IidmChangesObject {

    // store network changes

    public IidmChangesObject(Identifiable identifiable, String attribute, Object oldValue, Object newValue, String variant) {
        this.identifiable = Objects.requireNonNull(identifiable);
        this.attribute = Objects.requireNonNull(attribute);
        this.oldValue = Objects.requireNonNull(oldValue);
        this.newValue = Objects.requireNonNull(newValue);
        this.variant = Objects.requireNonNull(variant);
    }

    public IidmChangesObject(Identifiable identifiable, String variant) {
        this.identifiable = Objects.requireNonNull(identifiable);
        this.variant = Objects.requireNonNull(variant);
    }

    // TODO remove get methods
    public String getVariant() {
        return variant;
    }

    public String getAttribute() {
        return attribute;
    }

    private final Identifiable identifiable;
    private String attribute;
    private Object oldValue;
    private Object newValue;
    private final String variant;

}
