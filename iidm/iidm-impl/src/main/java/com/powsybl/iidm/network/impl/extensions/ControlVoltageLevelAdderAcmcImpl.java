package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.AcmcAdder;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class ControlVoltageLevelAdderAcmcImpl extends AbstractControlVoltageLevelAdderImpl<AcmcAdder> {

    ControlVoltageLevelAdderAcmcImpl(AcmcAdderImpl parent) {
        super(parent);
    }

    @Override
    public AcmcAdderImpl add() {
        if (id == null) {
            throw new PowsyblException("Control unit ID is not set");
        }
        ((AcmcAdderImpl) parent).addControlVoltageLevel(new ControlVoltageLevelImpl(id));
        return (AcmcAdderImpl) parent;
    }
}
