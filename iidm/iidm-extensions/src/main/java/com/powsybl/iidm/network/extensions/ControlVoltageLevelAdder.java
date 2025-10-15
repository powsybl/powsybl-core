package com.powsybl.iidm.network.extensions;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface ControlVoltageLevelAdder<T> {
    ControlVoltageLevelAdder<T> withId(String id);

    ControlVoltageLevelAdder<T> withForceOneTransformerLoads();

    T add();
}
