/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Simple {@link NetworkModification} for a hdvc line or its extension with angle droop active power control.
 *
 * @author Pauline JEAN-MARIE {@literal <pauline.jean-marie at artelys.com>}
 */
public class HvdcLineModification extends AbstractNetworkModification {

    private final String hvdcId;
    private final Boolean acEmulationEnabled;
    private final Double activePowerSetpoint;
    private final HvdcLine.ConvertersMode converterMode;
    private final Double droop;
    private final Double p0;
    private final Boolean relativeValue;

    public HvdcLineModification(String hvdcId, Boolean acEmulationEnabled, Double activePowerSetpoint, HvdcLine.ConvertersMode converterMode, Double droop, Double p0, Boolean relativeValue) {
        this.hvdcId = Objects.requireNonNull(hvdcId);
        this.acEmulationEnabled = acEmulationEnabled;
        this.activePowerSetpoint = activePowerSetpoint;
        this.converterMode = converterMode;
        this.droop = droop;
        this.p0 = p0;
        this.relativeValue = relativeValue;
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

    public boolean isRelativeValue() {
        return relativeValue != null && relativeValue;
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager,
                      ReportNode reportNode) {
        HvdcLine hvdcLine = network.getHvdcLine(getHvdcId());
        if (hvdcLine == null) {
            logOrThrow(throwException, "HvdcLine '" + getHvdcId() + "' not found");
            return;
        }
        getActivePowerSetpoint().ifPresent(value -> hvdcLine.setActivePowerSetpoint((isRelativeValue() ? hvdcLine.getActivePowerSetpoint() : 0) + value));
        getConverterMode().ifPresent(hvdcLine::setConvertersMode);
        HvdcAngleDroopActivePowerControl hvdcAngleDroopActivePowerControl = hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class);
        if (hvdcAngleDroopActivePowerControl != null) {
            isAcEmulationEnabled().ifPresent(hvdcAngleDroopActivePowerControl::setEnabled);
            getP0().ifPresent(value -> hvdcAngleDroopActivePowerControl.setP0((float) value));
            getDroop().ifPresent(value -> hvdcAngleDroopActivePowerControl.setDroop((float) value));
        }
    }
}
