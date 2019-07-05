package com.powsybl.cgmes.update;

import java.util.Objects;

import com.powsybl.iidm.network.Identifiable;

public class IidmChangesObject {

    // store network changes

    public IidmChangesObject(Identifiable identifiable, String attribute, Object oldValue, Object newValue,
        String variant) {
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

    public Identifiable getIdentifiable() {
        return identifiable;
    }

    public String getIdentifiableId() {
        return identifiable.getId();
    }

    public Object getOldValue() {
        return oldValue;
    }

    public String getOldValueString() {
        if (oldValue != null) {
            return oldValue.toString();
        } else {
            return "";
        }
    }

    public Object getNewValue() {
        return newValue;
    }

    public String getNewValueString() {
        if (newValue != null) {
            return newValue.toString();
        } else {
            return "";
        }
    }

    private final Identifiable identifiable;
    private String attribute;
    private Object oldValue;
    private Object newValue;
    private final String variant;

}
