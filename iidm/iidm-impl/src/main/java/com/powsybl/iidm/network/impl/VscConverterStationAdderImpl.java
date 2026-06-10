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

import java.util.function.Supplier;

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

    private Supplier<VoltageRegulation> voltageRegulationCreator = null;

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

    private void setVoltageRegulationCreator(Supplier<VoltageRegulation> voltageRegulationCreator) {
        this.voltageRegulationCreator = voltageRegulationCreator;
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
        return new VoltageRegulationAdderImpl<>(VscConverterStation.class, this, this, getNetworkRef(), this::setVoltageRegulationCreator);
    }

    @Override
    public VscConverterStationImpl add() {
        NetworkImpl network = getNetwork();

        if (network.getMinValidationLevel() == ValidationLevel.EQUIPMENT && voltageRegulatorOn == null && voltageRegulationCreator == null) {
            voltageRegulatorOn = false;
            reactivePowerSetpoint = localTargetQ;
        }
        if (voltageRegulationCreator == null && voltageRegulatorOn != null) {
            createVoltageRegulationBackwardCompatibility(this, voltageSetpoint, reactivePowerSetpoint, voltageRegulatorOn, regulatingTerminal);
        }
        VoltageRegulationExt voltageRegulation = voltageRegulationCreator != null ? (VoltageRegulationExt) voltageRegulationCreator.get() : null;
        String id = checkAndGetUniqueId();
        String name = getName();
        TerminalExt terminal = checkAndGetTerminal();
        validate();
        VscConverterStationImpl converterStation
                = new VscConverterStationImpl(id, name, isFictitious(), getLossFactor(), getNetworkRef(),
            localTargetQ, localTargetV, voltageRegulation);
        converterStation.addTerminal(terminal);
        getVoltageLevel().getTopologyModel().attach(terminal, false);
        network.getIndex().checkAndAdd(converterStation);
        network.getListeners().notifyCreation(converterStation);
        return converterStation;
    }

}
