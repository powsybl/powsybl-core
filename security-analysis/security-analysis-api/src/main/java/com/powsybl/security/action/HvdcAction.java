/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.action;

import com.powsybl.iidm.network.HvdcLine;

import java.util.Optional;
import java.util.OptionalDouble;

/**
 * An action to modify hvdc parameters or how an hvdc is operated
 * parameter enabled to true correspond to ac emulation operation, false to fix active power setpoint. Note that ac
 * emulation works only with VSC converter station.
 * droop and p0 are parameters used for ac emulation only.
 * activePowerSetpoint and converterMode are for fix active power setpoint operation only. Attribute relative value should
 * be used only to define the new active power setpoint.
 *
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class HvdcAction extends AbstractAction {

    public static final String NAME = "HVDC";

    private final String hvdcId;
    private final boolean acEmulationEnabled;
    private final Double activePowerSetpoint;
    private final HvdcLine.ConvertersMode converterMode;
    private final Double droop;
    private final Double p0;
    private final Boolean relativeValue;

    public HvdcAction(String id, String hvdcId, Double activePowerSetpoint, HvdcLine.ConvertersMode converterMode, Boolean relativeValue) {
        this(id, hvdcId, false, activePowerSetpoint, converterMode, null, null, relativeValue);
    }

    public HvdcAction(String id, String hvdcId, Double droop, Double p0) {
        this(id, hvdcId, true, null, null, droop, p0, null);
    }

    public HvdcAction(String id, String hvdcId, boolean acEmulationEnabled, Double activePowerSetpoint, HvdcLine.ConvertersMode converterMode, Double droop, Double p0, Boolean relativeValue) {
        super(id);
        this.hvdcId = hvdcId;
        this.acEmulationEnabled = acEmulationEnabled;
        this.activePowerSetpoint = activePowerSetpoint;
        this.converterMode = converterMode;
        this.droop = droop;
        this.p0 = p0;
        this.relativeValue = relativeValue;
    }

    public static HvdcAction activateAcEmulationMode(String id, String hvdcId) {
        return activateAcEmulationMode(id, hvdcId, null, null);
    }

    public static HvdcAction activateAcEmulationMode(String id, String hvdcId, Double droop, Double p0) {
        return new HvdcAction(id, hvdcId, droop, p0);
    }

    public static HvdcAction activateActivePowerSetpointMode(String id, String hvdcId) {
        return activateActivePowerSetpointMode(id, hvdcId, null, null, null);
    }

    public static HvdcAction activateActivePowerSetpointMode(String id, String hvdcId, Double activePowerSetpoint, HvdcLine.ConvertersMode converterMode, Boolean relativeValue) {
        return new HvdcAction(id, hvdcId, activePowerSetpoint, converterMode, relativeValue);
    }

    @Override
    public String getType() {
        return NAME;
    }

    public String getHvdcId() {
        return hvdcId;
    }

    public Optional<HvdcLine.ConvertersMode> getConverterMode() {
        return Optional.ofNullable(converterMode);
    }

    public OptionalDouble getDroop() {
        return droop == null ? OptionalDouble.empty() : OptionalDouble.of(droop);
    }

    public OptionalDouble getActivePowerSetpoint() {
        return activePowerSetpoint == null ? OptionalDouble.empty() : OptionalDouble.of(activePowerSetpoint);
    }

    public OptionalDouble getP0() {
        return p0 == null ? OptionalDouble.empty() : OptionalDouble.of(p0);
    }

    public boolean isAcEmulationEnabled() {
        return acEmulationEnabled;
    }

    public Optional<Boolean> isRelativeValue() {
        return Optional.ofNullable(relativeValue);
    }
}
