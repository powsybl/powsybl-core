package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.AcmcAdder;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class MeasurementPointAdderAcmcImpl extends AbstractMeasurementPointAdderImpl<AcmcAdder> {

    MeasurementPointAdderAcmcImpl(AcmcAdderImpl parent) {
        super(parent);
    }

    @Override
    public AcmcAdderImpl add() {
        if (busbarSectionsOrBusesIds.isEmpty()) {
            throw new PowsyblException("Empty busbar section or bus ID list");
        }
        for (String busbarSectionsOrBusesId : busbarSectionsOrBusesIds) {
            if (busbarSectionsOrBusesId == null) {
                throw new PowsyblException("Null busbar section or bus ID");
            }
        }
        ((AcmcAdderImpl) parent).setMeasurementPoint(new MeasurementPointImpl(busbarSectionsOrBusesIds, ""));
        return (AcmcAdderImpl) parent;
    }
}
