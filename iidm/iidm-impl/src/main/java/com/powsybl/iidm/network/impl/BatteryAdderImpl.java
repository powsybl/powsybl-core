/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.BatteryAdder;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.*;

import java.util.Objects;
import java.util.Set;

/**
 * {@inheritDoc}
 *
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
public class BatteryAdderImpl extends AbstractInjectionAdder<BatteryAdderImpl> implements BatteryAdder {

    private double targetP = Double.NaN;

    private double targetQ = Double.NaN;

    private double minP = Double.NaN;

    private double maxP = Double.NaN;

    private VoltageRegulation voltageRegulation = null;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public BatteryAdderImpl setTargetQ(double targetQ) {
        this.targetQ = targetQ;
        return this;
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
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkQ0(this, targetQ, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerLimits(this, minP, maxP);

        network.setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageRegulation(this, voltageRegulation, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));
        BatteryImpl battery = new BatteryImpl(getNetworkRef(), id, getName(), isFictitious(), targetP, targetQ, (VoltageRegulationImpl) voltageRegulation, minP, maxP);
        battery.addTerminal(terminal);
        voltageLevel.getTopologyModel().attach(terminal, false);
        network.getIndex().checkAndAdd(battery);
        network.getListeners().notifyCreation(battery);

        return battery;
    }

    @Override
    public VoltageRegulationAdder<BatteryAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(this, getNetworkRef(), this::setVoltageRegulations);
    }

    @Override
    public void removeVoltageRegulation() {
        if (this.voltageRegulation != null) {
            this.voltageRegulation.setTerminal(null);
        }
        this.voltageRegulation = null;
    }

    @Override
    public VoltageRegulation getVoltageRegulation() {
        return this.voltageRegulation;
    }

    @Override
    public Set<RegulationMode> getAllowedRegulationModes() {
        return Set.of(RegulationMode.VOLTAGE, RegulationMode.REACTIVE_POWER);
    }

    public void setVoltageRegulations(VoltageRegulation voltageRegulation) {
        this.voltageRegulation = voltageRegulation;
    }
}
