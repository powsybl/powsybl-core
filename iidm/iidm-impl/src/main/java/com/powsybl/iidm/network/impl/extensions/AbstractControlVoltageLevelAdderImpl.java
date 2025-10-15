package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.extensions.ControlVoltageLevelAdder;

import java.util.Objects;

public abstract class AbstractControlVoltageLevelAdderImpl<T> implements ControlVoltageLevelAdder<T> {

    protected final T parent;

    protected String id;

    protected boolean forceOneTransformerLoads = false;

    AbstractControlVoltageLevelAdderImpl(T parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public AbstractControlVoltageLevelAdderImpl<T> withId(String id) {
        this.id = Objects.requireNonNull(id);
        return this;
    }

    @Override
    public AbstractControlVoltageLevelAdderImpl<T> withForceOneTransformerLoads() {
        this.forceOneTransformerLoads = true;
        return this;
    }

}
