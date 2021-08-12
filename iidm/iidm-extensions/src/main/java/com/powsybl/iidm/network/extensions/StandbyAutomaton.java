package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.StaticVarCompensator;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface StandbyAutomaton extends Extension<StaticVarCompensator> {

    @Override
    default String getName() {
        return "standbyAutomaton";
    }

    /**
     * @return true if the static var compensator behaves like a shunt compensator and monitors voltage
     * at regulating terminal.
     */
    boolean isStandby();

    /**
     * Set to true if the static var compensator behaves like a shunt compensator and monitors voltage
     * at regulating terminal, false otherwise.
     */
    StandbyAutomaton setStandby(boolean standby);

    /**
     * @return the susceptance of the static var compensator when it behaves like a shunt compensator (in S).
     */
    double getB0();

    /**
     * Set the susceptance of the static var compensator when it behaves like a shunt compensator (in S).
     */
    StandbyAutomaton setB0(double b0);

    /**
     * @return the voltage target when the voltage at regulating terminal becomes greater that the high voltage threshold
     * in stand by mode.
     */
    double getHighVoltageSetPoint();

    /**
     * @return the voltage target when the voltage at regulating terminal becomes greater that the high voltage threshold
     * in stand by mode.
     */
    StandbyAutomaton setHighVoltageSetPoint(double highVoltageSetPoint);

    /**
     * @return the high voltage threshold in kV.
     */
    double getHighVoltageThreshold();

    /**
     * Set the high voltage threshold in kV.
     */
    StandbyAutomaton setHighVoltageThreshold(double highVoltageThreshold);

    /**
     * @return the voltage target when the voltage at regulating terminal becomes lower that the low voltage threshold.
     */
    double getLowVoltageSetPoint();

    /**
     * Set the voltage target when the voltage at regulating terminal becomes lower that the low voltage threshold
     * in stand by mode.
     */
    StandbyAutomaton setLowVoltageSetPoint(double lowVoltageSetPoint);

    /**
     * @return the low voltage threshold in kV.
     */
    double getLowVoltageThreshold();

    /**
     * Set the low voltage threshold in kV.
     */
    StandbyAutomaton setLowVoltageThreshold(double lowVoltageThreshold);
}
