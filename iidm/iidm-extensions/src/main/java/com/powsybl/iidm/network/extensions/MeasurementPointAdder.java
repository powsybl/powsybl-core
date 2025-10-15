package com.powsybl.iidm.network.extensions;

import java.util.List;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface MeasurementPointAdder<T> {

    MeasurementPointAdder<T> withBusbarSectionsOrBusesIds(List<String> busbarSectionsOrBusesIds);

    MeasurementPointAdder<T> withId(String id);

    T add();
}
