package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.TapChangerBlockingAdder;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class ControlVoltageLevelAdderTapChangerBlockingImpl extends AbstractControlVoltageLevelAdderImpl<TapChangerBlockingAdder> {

    ControlVoltageLevelAdderTapChangerBlockingImpl(TapChangerBlockingAdderImpl parent) {
        super(parent);
    }

    @Override
    public TapChangerBlockingAdderImpl add() {
        if (id == null) {
            throw new PowsyblException("Control unit ID is not set");
        }
        if (forceOneTransformerLoads) {
            ((TapChangerBlockingAdderImpl) parent).addControlVoltageLevel(new ControlVoltageLevelImpl(id, forceOneTransformerLoads));
        } else {
            ((TapChangerBlockingAdderImpl) parent).addControlVoltageLevel(new ControlVoltageLevelImpl(id));
        }

        return (TapChangerBlockingAdderImpl) parent;
    }
}
