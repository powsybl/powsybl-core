package com.powsybl.iidm.network.extensions;

import java.util.List;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface MeasurementPoint {

    List<String> getBusbarSectionsOrBusesIds();

    String getId();
}
