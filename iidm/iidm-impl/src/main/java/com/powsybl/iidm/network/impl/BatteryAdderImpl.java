/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.BatteryAdder;
import com.powsybl.iidm.network.ValidationUtil;

import java.util.Objects;

/**
 * {@inheritDoc}
 *
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public class BatteryAdderImpl extends AbstractInjectionAdder<BatteryAdderImpl> implements BatteryAdder {

    private final VoltageLevelExt voltageLevel;

    private double p0 = Double.NaN;

    private double q0 = Double.NaN;

    private double minP = Double.NaN;

    private double maxP = Double.NaN;

    public BatteryAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = Objects.requireNonNull(voltageLevel);
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
    public BatteryAdderImpl setP0(double p0) {
        this.p0 = p0;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BatteryAdderImpl setQ0(double q0) {
        this.q0 = q0;
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
        if (network.getAddersWithDefaultValues()) {
            minP = Double.isNaN(minP) ? -Double.MAX_VALUE : minP;
            maxP = Double.isNaN(maxP) ? Double.MAX_VALUE : maxP;
            p0 = Double.isNaN(p0) ? 0.0 : p0;
            q0 = Double.isNaN(q0) ? 0.0 : q0;
        }
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkP0(this, p0, network.getMinValidationLevel()));
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkQ0(this, q0, network.getMinValidationLevel()));
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerLimits(this, minP, maxP);

        BatteryImpl battery = new BatteryImpl(network.getRef(), id, getName(), isFictitious(), p0, q0, minP, maxP);

        battery.addTerminal(terminal);
        voltageLevel.attach(terminal, false);
        network.getIndex().checkAndAdd(battery);
        network.getListeners().notifyCreation(battery);
        return battery;
    }
}
