package com.powsybl.cgmes.update;

import java.util.Objects;

import com.powsybl.iidm.network.Identifiable;

public class IidmChangeOnRemove implements IidmChange {

    public IidmChangeOnRemove(Identifiable identifiable, String variant) {
        this.identifiable = Objects.requireNonNull(identifiable);
        this.variant = Objects.requireNonNull(variant);
    }

    // TODO elena
    @Override
    public String getVariant() {
        return variant;
    }

    @Override
    public String getAttribute() {
        return null;
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
        return null;
    }

    @Override
    public String getOldValueString() {
        return null;
    }

    @Override
    public Object getNewValue() {
        return null;
    }

    @Override
    public String getNewValueString() {
        return null;
    }

    private final Identifiable identifiable;
    private final String variant;
}
