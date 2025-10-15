package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.extensions.ControlVoltageLevel;

import java.util.Objects;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
class ControlVoltageLevelImpl implements ControlVoltageLevel {

    private final String id;

    private boolean forceOneTransformerLoads;

    public ControlVoltageLevelImpl(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public ControlVoltageLevelImpl(String id, boolean forceOneTransformerLoads) {
        this.id = Objects.requireNonNull(id);
        this.forceOneTransformerLoads = forceOneTransformerLoads;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean forceOneTransformerLoads() {
        return forceOneTransformerLoads;
    }
}
