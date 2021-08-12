package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import com.powsybl.iidm.network.extensions.StandbyAutomatonAdder;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public class StandbyAutomatonAdderImpl extends AbstractExtensionAdder<StaticVarCompensator, StandbyAutomaton>
        implements StandbyAutomatonAdder {

    private double b0;

    private boolean standby;

    private double lowVoltageSetPoint;

    private double highVoltageSetPoint;

    private double lowVoltageThreshold;

    private double highVoltageThreshold;

    public StandbyAutomatonAdderImpl(StaticVarCompensator svc) {
        super(svc);
    }

    @Override
    protected StandbyAutomaton createExtension(StaticVarCompensator staticVarCompensator) {
        return new StandbyAutomatonImpl(staticVarCompensator, b0, standby,
                lowVoltageSetPoint, highVoltageSetPoint, lowVoltageThreshold, highVoltageThreshold);
    }

    @Override
    public StandbyAutomatonAdderImpl withStandbyStatus(boolean standby) {
        this.standby = standby;
        return this;
    }

    @Override
    public StandbyAutomatonAdderImpl withB0(double b0) {
        this.b0 = b0;
        return this;
    }

    @Override
    public StandbyAutomatonAdderImpl withHighVoltageSetPoint(double highVoltageSetPoint) {
        this.highVoltageSetPoint = highVoltageSetPoint;
        return this;
    }

    @Override
    public StandbyAutomatonAdderImpl withHighVoltageThreshold(double highVoltageThreshold) {
        this.highVoltageThreshold = highVoltageThreshold;
        return this;
    }

    @Override
    public StandbyAutomatonAdderImpl withLowVoltageSetPoint(double lowVoltageSetPoint) {
        this.lowVoltageSetPoint = lowVoltageSetPoint;
        return this;
    }

    @Override
    public StandbyAutomatonAdderImpl withLowVoltageThreshold(double lowVoltageThreshold) {
        this.lowVoltageThreshold = lowVoltageThreshold;
        return this;
    }
}
