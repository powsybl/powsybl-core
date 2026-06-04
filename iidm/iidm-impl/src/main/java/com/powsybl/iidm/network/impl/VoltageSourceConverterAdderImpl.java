/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;

import java.util.function.Consumer;

import static com.powsybl.iidm.network.util.VoltageRegulationUtils.createVoltageRegulationBackwardCompatibility;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class VoltageSourceConverterAdderImpl extends AbstractAcDcConverterAdder<VoltageSourceConverterAdderImpl> implements VoltageSourceConverterAdder {

    private Boolean voltageRegulatorOn;
    private double voltageSetpoint = Double.NaN;
    private double reactivePowerSetpoint = Double.NaN;
    private double localTargetQ = Double.NaN;
    private double localTargetV = Double.NaN;
    private VoltageRegulationExt voltageRegulation = null;

    VoltageSourceConverterAdderImpl(VoltageLevelExt voltageLevel) {
        super(voltageLevel);
    }

    @Override
    protected String getTypeDescription() {
        return "AC/DC Voltage Source Converter";
    }

    @Override
    public VoltageRegulationAdder<VoltageSourceConverterAdder> newVoltageRegulation() {
        Consumer<VoltageRegulationExt> voltageRegulationConsumer = vr -> this.voltageRegulation = vr;
        return new VoltageRegulationAdderImpl<>(VoltageSourceConverter.class, this, this, getNetwork().getRef(), voltageRegulationConsumer);
    }

    @Override
    public VoltageSourceConverterAdder setLocalTargetQ(double localTargetQ) {
        this.localTargetQ = localTargetQ;
        return this;
    }

    @Override
    public double getLocalTargetQ() {
        return this.localTargetQ;
    }

    @Override
    public VoltageSourceConverterAdder setLocalTargetV(double localTargetV) {
        this.localTargetV = localTargetV;
        return this;
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
        String id = checkAndGetUniqueId();
        super.preCheck();
        NetworkImpl network = getNetwork();
        if (network.getMinValidationLevel() == ValidationLevel.EQUIPMENT && voltageRegulatorOn != null && voltageRegulation == null) {
            voltageRegulatorOn = false;
        }
        if (voltageRegulation == null && voltageRegulatorOn != null) {
            createVoltageRegulationBackwardCompatibility(this, voltageSetpoint, reactivePowerSetpoint, voltageRegulatorOn, null);
        }
        ValidationUtil.checkRegulatingTerminal(this, this.pccTerminal, network);
        VoltageSourceConverterImpl dcVsConverter = new VoltageSourceConverterImpl(voltageLevel.getNetworkRef(), id, getName(), isFictitious(),
                idleLoss, switchingLoss, resistiveLoss,
                pccTerminal, controlMode, targetP, targetVdc,
            localTargetQ, localTargetV, voltageRegulation);
        super.checkAndAdd(dcVsConverter);
        return dcVsConverter;
    }

    @Override
    protected VoltageSourceConverterAdderImpl self() {
        return this;
    }

}
