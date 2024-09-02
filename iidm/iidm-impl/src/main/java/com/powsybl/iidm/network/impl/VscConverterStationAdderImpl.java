/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.VscConverterStationAdder;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class VscConverterStationAdderImpl extends AbstractHvdcConverterStationAdder<VscConverterStationAdderImpl> implements VscConverterStationAdder {

    private Boolean voltageRegulatorOn;

    private double reactivePowerSetpoint = Double.NaN;

    private double voltageSetpoint = Double.NaN;

    private TerminalExt regulatingTerminal;

    VscConverterStationAdderImpl(VoltageLevelExt voltageLevel) {
        super(voltageLevel);
    }

    @Override
    protected String getTypeDescription() {
        return VscConverterStationImpl.TYPE_DESCRIPTION;
    }

    @Override
    public VscConverterStationAdderImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    @Override
    public VscConverterStationAdderImpl setVoltageSetpoint(double voltageSetpoint) {
        this.voltageSetpoint = voltageSetpoint;
        return this;
    }

    @Override
    public VscConverterStationAdderImpl setReactivePowerSetpoint(double reactivePowerSetpoint) {
        this.reactivePowerSetpoint = reactivePowerSetpoint;
        return this;
    }

    @Override
    public VscConverterStationAdder setRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = (TerminalExt) regulatingTerminal;
        return this;
    }

    @Override
    public VscConverterStationImpl add() {
        NetworkImpl network = getNetwork();
        if (network.getMinValidationLevel() == ValidationLevel.EQUIPMENT && voltageRegulatorOn == null) {
            voltageRegulatorOn = false;
        }
        String id = checkAndGetUniqueId();
        String name = getName();
        TerminalExt terminal = checkAndGetTerminal();
        validate();
        VscConverterStationImpl converterStation
                = new VscConverterStationImpl(id, name, isFictitious(), getLossFactor(), getNetworkRef(),
                voltageRegulatorOn, reactivePowerSetpoint, voltageSetpoint, regulatingTerminal == null ? terminal : regulatingTerminal);
        converterStation.addTerminal(terminal);
        getVoltageLevel().attach(terminal, false);
        network.getIndex().checkAndAdd(converterStation);
        network.getListeners().notifyCreation(converterStation);
        return converterStation;
    }

    @Override
    protected void validate() {
        super.validate();
        NetworkImpl network = getNetwork();
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, voltageSetpoint,
                reactivePowerSetpoint, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, network);
    }

}
