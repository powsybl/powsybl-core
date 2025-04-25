/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.modification.HvdcLineModification;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.HvdcLine;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * An action to modify HVDC parameters and/or operating mode.
 * <ul>
 *     <li>{@code acEmulationEnabled} parameter set to true correspond to AC emulation operation,
 *     false to fixed active power setpoint.
 *     <li>Note that AC emulation is relevant only for VSC converter station.
 *     <li>{@code droop} and {@code p0} parameters are used for AC emulation only.<br>
 *     <li>{@code activePowerSetpoint} and {@code converterMode} parameters are for fixed
 *     active power setpoint operation only. The {@code relativeValue} attribute should be used only
 *     when defining fixed active power setpoint.
 * </ul>
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class HvdcAction extends AbstractAction {

    public static final String NAME = "HVDC";

    private final String hvdcId;
    private final Boolean acEmulationEnabled;
    private final Double activePowerSetpoint;
    private final HvdcLine.ConvertersMode converterMode;
    private final Double droop;
    private final Double p0;
    private final Boolean relativeValue;

    HvdcAction(String id, String hvdcId, Boolean acEmulationEnabled, Double activePowerSetpoint, HvdcLine.ConvertersMode converterMode, Double droop, Double p0, Boolean relativeValue) {
        super(id);
        this.hvdcId = Objects.requireNonNull(hvdcId);
        this.acEmulationEnabled = acEmulationEnabled;
        this.activePowerSetpoint = activePowerSetpoint;
        this.converterMode = converterMode;
        this.droop = droop;
        this.p0 = p0;
        this.relativeValue = relativeValue;
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

    public Optional<Boolean> isAcEmulationEnabled() {
        return Optional.ofNullable(acEmulationEnabled);
    }

    public Optional<Boolean> isRelativeValue() {
        return Optional.ofNullable(relativeValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        HvdcAction that = (HvdcAction) o;
        return Objects.equals(hvdcId, that.hvdcId)
                && Objects.equals(acEmulationEnabled, that.acEmulationEnabled)
                && Objects.equals(activePowerSetpoint, that.activePowerSetpoint)
                && converterMode == that.converterMode
                && Objects.equals(droop, that.droop)
                && Objects.equals(p0, that.p0)
                && Objects.equals(relativeValue, that.relativeValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), hvdcId, acEmulationEnabled, activePowerSetpoint, converterMode, droop, p0, relativeValue);
    }

    @Override
    public NetworkModification toModification() {
        return new HvdcLineModification(
                getHvdcId(),
                isAcEmulationEnabled().orElse(null),
                getActivePowerSetpoint().stream().boxed().findFirst().orElse(null),
                getConverterMode().orElse(null),
                getDroop().stream().boxed().findFirst().orElse(null),
                getP0().stream().boxed().findFirst().orElse(null),
                isRelativeValue().orElse(null)
        );
    }
}
