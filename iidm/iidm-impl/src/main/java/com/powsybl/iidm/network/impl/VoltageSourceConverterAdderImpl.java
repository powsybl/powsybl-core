/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class VoltageSourceConverterAdderImpl extends AbstractAcDcConverterAdder<VoltageSourceConverterAdderImpl> implements VoltageSourceConverterAdder {

    private Boolean voltageRegulatorOn;
    private double voltageSetpoint = Double.NaN;
    private double reactivePowerSetpoint = Double.NaN;

    VoltageSourceConverterAdderImpl(VoltageLevelExt voltageLevel) {
        super(voltageLevel);
    }

    @Override
    protected String getTypeDescription() {
        return "AC/DC Voltage Source Converter";
    }

    @Override
    public VoltageSourceConverterAdder setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    @Override
    public VoltageSourceConverterAdder setVoltageSetpoint(double voltageSetpoint) {
        this.voltageSetpoint = voltageSetpoint;
        return this;
    }

    @Override
    public VoltageSourceConverterAdder setReactivePowerSetpoint(double reactivePowerSetpoint) {
        this.reactivePowerSetpoint = reactivePowerSetpoint;
        return this;
    }

    @Override
    public VoltageSourceConverter add() {
        // TODO checks
        // TODO / note: dcNodes and voltage level must be in same network
        String id = checkAndGetUniqueId();
        super.preCheck();
        NetworkImpl network = getNetwork();
        if (network.getMinValidationLevel() == ValidationLevel.EQUIPMENT && voltageRegulatorOn == null) {
            voltageRegulatorOn = false;
        }
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, voltageSetpoint,
                reactivePowerSetpoint, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));
        ValidationUtil.checkRegulatingTerminal(this, this.pccTerminal, network);
        VoltageSourceConverterImpl dcVsConverter = new VoltageSourceConverterImpl(voltageLevel.getNetworkRef(), id, getName(), isFictitious(),
                idleLoss, switchingLoss, resistiveLoss,
                pccTerminal, controlMode, targetP, targetVdc,
                voltageRegulatorOn, voltageSetpoint, reactivePowerSetpoint);
        super.checkAndAdd(dcVsConverter);
        return dcVsConverter;
    }

    @Override
    protected VoltageSourceConverterAdderImpl self() {
        return this;
    }
}
