package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.StaticVarCompensator;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public interface StandbyAutomatonAdder extends ExtensionAdder<StaticVarCompensator, StandbyAutomaton> {

    @Override
    default Class<StandbyAutomaton> getExtensionClass() {
        return StandbyAutomaton.class;
    }

    StandbyAutomatonAdder withStandbyStatus(boolean standby);

    StandbyAutomatonAdder withB0(double b0);

    StandbyAutomatonAdder withHighVoltageSetPoint(double highVoltageSetPoint);

    StandbyAutomatonAdder withHighVoltageThreshold(double highVoltageThreshold);

    StandbyAutomatonAdder withLowVoltageSetPoint(double lowVoltageSetPoint);

    StandbyAutomatonAdder withLowVoltageThreshold(double lowVoltageThreshold);
}
