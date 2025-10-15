package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.TapChangerBlockingAdder;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class MeasurementPointAdderTapChangerBlockingImpl extends AbstractMeasurementPointAdderImpl<TapChangerBlockingAdder> {

    MeasurementPointAdderTapChangerBlockingImpl(TapChangerBlockingAdderImpl parent) {
        super(parent);
    }

    @Override
    public TapChangerBlockingAdderImpl add() {
        if (busbarSectionsOrBusesIds.isEmpty()) {
            throw new PowsyblException("Empty busbar section or bus ID list");
        }
        if (id == null) {
            throw new PowsyblException("Measurement point ID is not set");
        }
        for (String busbarSectionsOrBusesId : busbarSectionsOrBusesIds) {
            if (busbarSectionsOrBusesId == null) {
                throw new PowsyblException("Null busbar section or bus ID");
            }
        }
        ((TapChangerBlockingAdderImpl) parent).setMeasurementPoint(new MeasurementPointImpl(busbarSectionsOrBusesIds, id));
        return (TapChangerBlockingAdderImpl) parent;
    }
}
