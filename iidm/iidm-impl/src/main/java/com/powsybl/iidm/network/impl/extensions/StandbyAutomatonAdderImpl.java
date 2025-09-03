/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import com.powsybl.iidm.network.extensions.StandbyAutomatonAdder;
import com.powsybl.iidm.network.impl.StaticVarCompensatorImpl;

/**
 * @author Jérémy Labous {@literal <jlabous at silicom.fr>}
 */
public class StandbyAutomatonAdderImpl extends AbstractExtensionAdder<StaticVarCompensator, StandbyAutomaton>
        implements StandbyAutomatonAdder {

    private double b0 = Double.NaN;

    private boolean standby = false;

    private double lowVoltageSetpoint = Double.NaN;

    private double highVoltageSetpoint = Double.NaN;

    private double lowVoltageThreshold = Double.NaN;

    private double highVoltageThreshold = Double.NaN;

    public StandbyAutomatonAdderImpl(StaticVarCompensator svc) {
        super(svc);
    }

    @Override
    protected StandbyAutomaton createExtension(StaticVarCompensator staticVarCompensator) {
        return new StandbyAutomatonImpl((StaticVarCompensatorImpl) staticVarCompensator, b0, standby,
                lowVoltageSetpoint, highVoltageSetpoint, lowVoltageThreshold, highVoltageThreshold);
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
    public StandbyAutomatonAdderImpl withHighVoltageSetpoint(double highVoltageSetpoint) {
        this.highVoltageSetpoint = highVoltageSetpoint;
        return this;
    }

    @Override
    public StandbyAutomatonAdderImpl withHighVoltageThreshold(double highVoltageThreshold) {
        this.highVoltageThreshold = highVoltageThreshold;
        return this;
    }

    @Override
    public StandbyAutomatonAdderImpl withLowVoltageSetpoint(double lowVoltageSetpoint) {
        this.lowVoltageSetpoint = lowVoltageSetpoint;
        return this;
    }

    @Override
    public StandbyAutomatonAdderImpl withLowVoltageThreshold(double lowVoltageThreshold) {
        this.lowVoltageThreshold = lowVoltageThreshold;
        return this;
    }
}
