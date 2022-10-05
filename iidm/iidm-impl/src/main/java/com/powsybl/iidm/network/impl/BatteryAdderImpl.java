/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.BatteryAdder;
import com.powsybl.iidm.network.ValidationUtil;

import java.util.Objects;

import static com.powsybl.iidm.network.util.CopyUtil.copyIdNameFictitiousConnectivity;

/**
 * {@inheritDoc}
 *
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public class BatteryAdderImpl extends AbstractInjectionAdder<BatteryAdderImpl> implements BatteryAdder {

    private final VoltageLevelExt voltageLevel;

    private double targetP = Double.NaN;

    private double targetQ = Double.NaN;

    private double minP = Double.NaN;

    private double maxP = Double.NaN;

    public BatteryAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = Objects.requireNonNull(voltageLevel);
    }

    BatteryAdderImpl(Battery battery, VoltageLevelExt voltageLevel) {
        this(voltageLevel);
        targetP = battery.getTargetP();
        targetQ = battery.getTargetQ();
        minP = battery.getMinP();
        maxP = battery.getMaxP();
        copyIdNameFictitiousConnectivity(battery, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
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
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkP0(this, targetP, network.getMinValidationLevel()));
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkQ0(this, targetQ, network.getMinValidationLevel()));
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerLimits(this, minP, maxP);

        BatteryImpl battery = new BatteryImpl(network.getRef(), id, getName(), isFictitious(), targetP, targetQ, minP, maxP);

        battery.addTerminal(terminal);
        voltageLevel.attach(terminal, false);
        network.getIndex().checkAndAdd(battery);
        network.getListeners().notifyCreation(battery);
        return battery;
    }
}
