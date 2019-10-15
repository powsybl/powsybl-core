package com.powsybl.cgmes.conversion.update;

import java.util.Objects;

import com.powsybl.iidm.network.Identifiable;

public class IidmChangeOnUpdate implements IidmChange {

    // store network changes

    public IidmChangeOnUpdate(Identifiable identifiable, String attribute, Object oldValue, Object newValue,
        String variant) {
        this.identifiable = Objects.requireNonNull(identifiable);
        this.attribute = Objects.requireNonNull(attribute);
        this.oldValue = oldValue;
        this.newValue = Objects.requireNonNull(newValue);
        this.variant = variant;
    }

    // TODO remove get methods
    @Override
    public String getVariant() {
        return variant;
    }

    @Override
    public void setVariant(String variant) {
        this.variant = variant;
    }

    @Override
    public String getAttribute() {
        return attribute;
    }

    @Override
    public Identifiable getIdentifiable() {
        return identifiable;
    }

    @Override
    public String getIdentifiableName() {
        return identifiable.getName();
    }

    @Override
    public String getIdentifiableId() {
        return identifiable.getId();
    }

    @Override
    public Object getOldValue() {
        return oldValue;
    }

    @Override
    public String getOldValueString() {
        if (oldValue != null) {
            return oldValue.toString();
        } else {
            return "";
        }
    }

    @Override
    public Object getNewValue() {
        return newValue;
    }

    @Override
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
    private String variant;

}
