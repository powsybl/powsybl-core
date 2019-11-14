package com.powsybl.cgmes.conversion.update;

import java.util.Objects;

import com.powsybl.iidm.network.Identifiable;

public class IidmChangeUpdate extends IidmChange {

    public IidmChangeUpdate(Identifiable identifiable, String attribute, Object oldValue, Object newValue) {
        super(identifiable);
        this.attribute = Objects.requireNonNull(attribute);
        this.oldValue = oldValue;
        this.newValue = Objects.requireNonNull(newValue);
    }

    public String getAttribute() {
        return attribute;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    private final String attribute;
    private final Object oldValue;
    private final Object newValue;
}
