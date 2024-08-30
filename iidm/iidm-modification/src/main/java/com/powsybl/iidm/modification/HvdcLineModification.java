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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Simple {@link NetworkModification} for an HVDC line (also potentially modifying its {@link HvdcAngleDroopActivePowerControl} extension).
 *
 * @author Pauline JEAN-MARIE {@literal <pauline.jean-marie at artelys.com>}
 */
public class HvdcLineModification extends AbstractNetworkModification {

    private static final Logger LOG = LoggerFactory.getLogger(HvdcLineModification.class);
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

    @Override
    public String getName() {
        return "HvdcLineModification";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager,
                      ReportNode reportNode) {
        HvdcLine hvdcLine = network.getHvdcLine(hvdcId);
        if (hvdcLine == null) {
            logOrThrow(throwException, "HvdcLine '" + hvdcId + "' not found");
            return;
        }
        if (activePowerSetpoint != null) {
            double newActivePowerSetpoint = activePowerSetpoint;
            if (relativeValue != null && relativeValue) {
                newActivePowerSetpoint = hvdcLine.getActivePowerSetpoint() + activePowerSetpoint;
            }
            hvdcLine.setActivePowerSetpoint(newActivePowerSetpoint);
        } else {
            if (relativeValue != null && relativeValue) {
                LOG.warn("Relative value is set to true but it will not be applied since active power setpoint is undefined (null)");
            }
        }
        if (converterMode != null) {
            hvdcLine.setConvertersMode(converterMode);
        }
        applyToHvdcAngleDroopActivePowerControlExtension(hvdcLine);
    }

    private void applyToHvdcAngleDroopActivePowerControlExtension(HvdcLine hvdcLine) {
        HvdcAngleDroopActivePowerControl hvdcAngleDroopActivePowerControl = hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class);
        if (acEmulationEnabled != null) {
            if (hvdcAngleDroopActivePowerControl != null) {
                hvdcAngleDroopActivePowerControl.setEnabled(acEmulationEnabled);
            } else {
                LOG.warn("AV emulation enable is define with value {}, but it will not be apply since the hvdc line {} do not have a HvdcAngleDroopActivePowerControl extension", acEmulationEnabled, hvdcId);
            }
        }
        if (p0 != null) {
            if (hvdcAngleDroopActivePowerControl != null) {
                hvdcAngleDroopActivePowerControl.setP0(p0.floatValue());
            } else {
                LOG.warn("P0 is define with value {}, but it will not be apply since the hvdc line {} do not have a HvdcAngleDroopActivePowerControl extension", p0, hvdcId);
            }
        }
        if (droop != null) {
            if (hvdcAngleDroopActivePowerControl != null) {
                hvdcAngleDroopActivePowerControl.setDroop(droop.floatValue());
            } else {
                LOG.warn("Droop is define with value {}, but it will not be apply since the hvdc line {} do not have a HvdcAngleDroopActivePowerControl extension", droop, hvdcId);
            }
        }
    }
}
