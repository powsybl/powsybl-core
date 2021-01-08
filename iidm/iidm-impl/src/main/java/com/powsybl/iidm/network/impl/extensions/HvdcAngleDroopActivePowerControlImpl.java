/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;

/**
 * Active power control mode based on an offset in MW and a droop in MW/degree
 * ActivePowerSetpoint = p0 + droop * (angle1 - angle2)
 *
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class HvdcAngleDroopActivePowerControlImpl extends AbstractExtension<HvdcLine> implements HvdcAngleDroopActivePowerControl {

    /**
     * Active power offset in MW
     */
    private float p0;

    /**
     * Droop in MW/degree
     */
    private float droop;

    /**
     * Enables or disables this active power control mode.
     * If this active power control mode is disabled, use the setpoint mode by default.
     */
    private boolean enabled;

    public HvdcAngleDroopActivePowerControlImpl(HvdcLine hvdcLine, float p0, float droop, boolean enabled) {
        super(hvdcLine);
        this.p0 = checkP0(p0);
        this.droop = checkDroop(droop);
        this.enabled = enabled;
    }

    @Override
    public float getP0() {
        return p0;
    }

    @Override
    public float getDroop() {
        return droop;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public HvdcAngleDroopActivePowerControl setP0(float p0) {
        this.p0 = p0;
        return this;
    }

    @Override
    public HvdcAngleDroopActivePowerControl setDroop(float droop) {
        this.droop = droop;
        return this;
    }

    @Override
    public HvdcAngleDroopActivePowerControl setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    private float checkP0(float p0) {
        if (Float.isNaN(p0)) {
            throw new IllegalArgumentException("p0 is not set");
        }

        return p0;
    }

    private float checkDroop(float droop) {
        if (Float.isNaN(droop)) {
            throw new IllegalArgumentException("droop is not set");
        }

        return droop;
    }
}
