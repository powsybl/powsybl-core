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

/**
 * @author Jérémy Labous {@literal <jlabous at silicom.fr>}
 */
public class StandbyAutomatonAdderImpl extends AbstractExtensionAdder<StaticVarCompensator, StandbyAutomaton>
        implements StandbyAutomatonAdder {

    private double b0;

    private boolean standby;

    private double lowVoltageSetpoint;

    private double highVoltageSetpoint;

    private double lowVoltageThreshold;

    private double highVoltageThreshold;

    public StandbyAutomatonAdderImpl(StaticVarCompensator svc) {
        super(svc);
    }

    @Override
    protected StandbyAutomaton createExtension(StaticVarCompensator staticVarCompensator) {
        return new StandbyAutomatonImpl(staticVarCompensator, b0, standby,
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
