package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.SmaccAdder;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class LowVoltageMeasurementPointAdderSmaccImpl extends AbstractMeasurementPointAdderImpl<SmaccAdder> {

    LowVoltageMeasurementPointAdderSmaccImpl(SmaccAdderImpl parent) {
        super(parent);
    }

    @Override
    public SmaccAdderImpl add() {
        if (busbarSectionsOrBusesIds.isEmpty()) {
            throw new PowsyblException("Empty busbar section or bus ID list");
        }
        for (String busbarSectionsOrBusesId : busbarSectionsOrBusesIds) {
            if (busbarSectionsOrBusesId == null) {
                throw new PowsyblException("Null busbar section or bus ID");
            }
        }
        ((SmaccAdderImpl) parent).setLowVoltageMeasurementPoint(new MeasurementPointImpl(busbarSectionsOrBusesIds, ""));
        return (SmaccAdderImpl) parent;
    }
}
