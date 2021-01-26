/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControlAdder;

/**
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
public class HvdcAngleDroopActivePowerControlAdderImpl extends AbstractExtensionAdder<HvdcLine, HvdcAngleDroopActivePowerControl>
        implements HvdcAngleDroopActivePowerControlAdder {

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

    public HvdcAngleDroopActivePowerControlAdderImpl(HvdcLine hvdcLine) {
        super(hvdcLine);
    }

    @Override
    public HvdcAngleDroopActivePowerControlAdder withP0(float p0) {
        this.p0 = p0;
        return this;
    }

    @Override
    public HvdcAngleDroopActivePowerControlAdder withDroop(float droop) {
        this.droop = droop;
        return this;
    }

    @Override
    public HvdcAngleDroopActivePowerControlAdder withEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    protected HvdcAngleDroopActivePowerControl createExtension(HvdcLine extendable) {
        return new HvdcAngleDroopActivePowerControlImpl(extendable, p0, droop, enabled);
    }
}
