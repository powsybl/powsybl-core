package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.extensions.MeasurementPointAdder;

import java.util.List;
import java.util.Objects;

public abstract class AbstractMeasurementPointAdderImpl<T> implements MeasurementPointAdder<T> {

    protected final T parent;

    protected List<String> busbarSectionsOrBusesIds;

    protected String id;

    AbstractMeasurementPointAdderImpl(T parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public AbstractMeasurementPointAdderImpl<T> withBusbarSectionsOrBusesIds(List<String> busbarSectionsOrBusesIds) {
        this.busbarSectionsOrBusesIds = Objects.requireNonNull(busbarSectionsOrBusesIds);
        return this;
    }

    @Override
    public AbstractMeasurementPointAdderImpl<T> withId(String id) {
        this.id = id;
        return this;
    }
}
