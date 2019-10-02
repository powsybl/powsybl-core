package com.powsybl.cgmes.update;

import java.util.Objects;

import com.powsybl.iidm.network.Identifiable;

public class IidmChangeOnCreate implements IidmChange {

    public IidmChangeOnCreate(Identifiable identifiable, String variant) {
        this.identifiable = Objects.requireNonNull(identifiable);
        this.variant = variant;
    }

    // TODO elena
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
    private String variant;
}
