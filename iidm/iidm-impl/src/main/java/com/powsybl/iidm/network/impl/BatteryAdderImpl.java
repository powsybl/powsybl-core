/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.BatteryAdder;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public class BatteryAdderImpl extends AbstractInjectionAdder<BatteryAdderImpl> implements BatteryAdder {

    private final VoltageLevelExt voltageLevel;

    private double p0 = Double.NaN;

    private double q0 = Double.NaN;

    private double minP = Double.NaN;

    private double maxP = Double.NaN;

    public BatteryAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    @Override
    protected String getTypeDescription() {
        return "Battery";
    }

    @Override
    public BatteryAdderImpl setP0(double p0) {
        this.p0 = p0;
        return this;
    }

    @Override
    public BatteryAdderImpl setQ0(double q0) {
        this.q0 = q0;
        return this;
    }

    @Override
    public BatteryAdderImpl setMinP(double minP) {
        this.minP = minP;
        return this;
    }

    @Override
    public BatteryAdderImpl setMaxP(double maxP) {
        this.maxP = maxP;
        return this;
    }

    @Override
    public BatteryImpl add() {
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal();
        ValidationUtil.checkP0(this, p0);
        ValidationUtil.checkQ0(this, q0);
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActiveLimits(this, minP, maxP);
        BatteryImpl battery
                = new BatteryImpl(getNetwork().getRef(),
                id, getName(),
                p0, q0,
                minP, maxP);
        battery.addTerminal(terminal);
        voltageLevel.attach(terminal, false);
        getNetwork().getObjectStore().checkAndAdd(battery);
        getNetwork().getListeners().notifyCreation(battery);
        return battery;
    }
}
