/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.BatteryAdder;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.*;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * {@inheritDoc}
 *
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
public class BatteryAdderImpl extends AbstractInjectionAdder<BatteryAdderImpl> implements BatteryAdder {

    private double targetP = Double.NaN;

    private double localTargetQ = Double.NaN;

    private double localTargetV = Double.NaN;

    private double minP = Double.NaN;

    private double maxP = Double.NaN;

    private Supplier<VoltageRegulation> voltageRegulationCreator = null;

    public BatteryAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = Objects.requireNonNull(voltageLevel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTypeDescription() {
        return "Battery";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BatteryAdderImpl setTargetP(double targetP) {
        this.targetP = targetP;
        return this;
    }

    @Override
    public BatteryAdder setTargetQ(double targetQ) {
        return this.setLocalTargetQ(targetQ);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BatteryAdderImpl setLocalTargetQ(double localTargetQ) {
        this.localTargetQ = localTargetQ;
        return this;
    }

    @Override
    public double getLocalTargetQ() {
        return this.localTargetQ;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BatteryAdderImpl setLocalTargetV(double localTargetV) {
        this.localTargetV = localTargetV;
        return this;
    }

    private void setVoltageRegulationCreator(Supplier<VoltageRegulation> voltageRegulationCreator) {
        this.voltageRegulationCreator = voltageRegulationCreator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BatteryAdderImpl setMinP(double minP) {
        this.minP = minP;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BatteryAdderImpl setMaxP(double maxP) {
        this.maxP = maxP;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BatteryImpl add() {
        NetworkImpl network = getNetwork();
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal();
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkP0(this, targetP, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkQ0(this, localTargetQ, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerLimits(this, minP, maxP);
        VoltageRegulationExt voltageRegulation = voltageRegulationCreator != null ?
            (VoltageRegulationExt) voltageRegulationCreator.get() : null;

        BatteryImpl battery = new BatteryImpl(getNetworkRef(), id, getName(), isFictitious(), targetP, localTargetQ, localTargetV, voltageRegulation, minP, maxP);
        battery.addTerminal(terminal);
        voltageLevel.getTopologyModel().attach(terminal, false);
        network.getIndex().checkAndAdd(battery);
        network.getListeners().notifyCreation(battery);

        return battery;
    }

    @Override
    public VoltageRegulationAdder<BatteryAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(Battery.class, this, this, getNetworkRef(), this::setVoltageRegulationCreator);
    }

}
