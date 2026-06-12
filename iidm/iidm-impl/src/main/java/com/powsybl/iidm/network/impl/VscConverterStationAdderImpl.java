/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;

import static com.powsybl.iidm.network.util.VoltageRegulationUtils.createVoltageRegulationBackwardCompatibility;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class VscConverterStationAdderImpl extends AbstractHvdcConverterStationAdder<VscConverterStationAdderImpl> implements VscConverterStationAdder {

    private Boolean voltageRegulatorOn;

    private double reactivePowerSetpoint = Double.NaN;

    private double voltageSetpoint = Double.NaN;

    private TerminalExt regulatingTerminal;

    private double localTargetQ = Double.NaN;

    private double localTargetV = Double.NaN;

    private VoltageRegulation.AttributesWithTerminal voltageRegulationAttributes = null;

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
    public VscConverterStationAdder setLocalTargetV(double localTargetV) {
        this.localTargetV = localTargetV;
        return this;
    }

    private void setVoltageRegulationAttributes(VoltageRegulation.AttributesWithTerminal voltageRegulationAttributes) {
        this.voltageRegulationAttributes = voltageRegulationAttributes;
    }

    @Override
    public VscConverterStationAdder setLocalTargetQ(double localTargetQ) {
        this.localTargetQ = localTargetQ;
        return this;
    }

    @Override
    public double getLocalTargetQ() {
        return this.localTargetQ;
    }

    @Override
    public VoltageRegulationAdder<VscConverterStationAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(VscConverterStation.class, this, this, getNetworkRef(), this::setVoltageRegulationAttributes);
    }

    @Override
    public VscConverterStationImpl add() {
        NetworkImpl network = getNetwork();

        if (network.getMinValidationLevel() == ValidationLevel.EQUIPMENT && voltageRegulatorOn == null && voltageRegulationAttributes == null) {
            voltageRegulatorOn = false;
            reactivePowerSetpoint = localTargetQ;
        }
        if (voltageRegulationAttributes == null && voltageRegulatorOn != null) {
            createVoltageRegulationBackwardCompatibility(this, voltageSetpoint, reactivePowerSetpoint, voltageRegulatorOn, regulatingTerminal);
        }

        String id = checkAndGetUniqueId();
        String name = getName();
        TerminalExt terminal = checkAndGetTerminal();
        validate();
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkLocalTargetQandV(this, VscConverterStation.class, localTargetV, localTargetQ, voltageRegulationAttributes, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));
        VscConverterStationImpl converterStation
                = new VscConverterStationImpl(id, name, isFictitious(), getLossFactor(), getNetworkRef(),
            localTargetQ, localTargetV, voltageRegulationAttributes);
        converterStation.addTerminal(terminal);
        getVoltageLevel().getTopologyModel().attach(terminal, false);
        network.getIndex().checkAndAdd(converterStation);
        network.getListeners().notifyCreation(converterStation);
        return converterStation;
    }

}
