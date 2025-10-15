package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.SmaccAdder;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class LowVoltageControlVoltageLevelAdderSmaccImpl extends AbstractControlVoltageLevelAdderImpl<SmaccAdder> {

    LowVoltageControlVoltageLevelAdderSmaccImpl(SmaccAdderImpl parent) {
        super(parent);
    }

    @Override
    public SmaccAdderImpl add() {
        if (id == null) {
            throw new PowsyblException("Control unit ID is not set");
        }
        ((SmaccAdderImpl) parent).addLowVoltageControlVoltageLevel(new ControlVoltageLevelImpl(id));
        return (SmaccAdderImpl) parent;
    }
}
