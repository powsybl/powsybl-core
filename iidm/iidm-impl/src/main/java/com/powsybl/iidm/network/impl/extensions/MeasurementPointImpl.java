package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.extensions.MeasurementPoint;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
class MeasurementPointImpl implements MeasurementPoint {

    private final List<String> busbarSectionsOrBusesIds;

    private final String id;

    MeasurementPointImpl(List<String> busbarSectionsOrBusesIds, String id) {
        this.busbarSectionsOrBusesIds = Objects.requireNonNull(busbarSectionsOrBusesIds);
        this.id = id;
    }

    /**
     * Get pilot point busbar section ID or bus ID of the bus/breaker view.
     */
    @Override
    public List<String> getBusbarSectionsOrBusesIds() {
        return Collections.unmodifiableList(busbarSectionsOrBusesIds);
    }

    @Override
    public String getId() {
        return id;
    }
}
