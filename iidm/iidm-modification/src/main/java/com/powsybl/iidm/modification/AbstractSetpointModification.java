/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.VscConverterStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.OptionalDouble;

/**
 * Simple {@link NetworkModification} for elements that needs to modify
 * their voltage and reactive setpoints. This is used for SVCs and for VSC
 * converter stations. Note that a VSC converter station follows a generator
 * convention but SVCs follow a load convention.
 *
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 */
public abstract class AbstractSetpointModification<T> extends AbstractNetworkModification {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSetpointModification.class);

    private final String elementId;
    private final Double voltageSetpoint;
    private final Double reactivePowerSetpoint;

    protected AbstractSetpointModification(String elementId, Double voltageSetpoint, Double reactivePowerSetpoint) {
        if (voltageSetpoint == null && reactivePowerSetpoint == null) {
            LOGGER.warn("Creating a {} modification with no modification", getElementName());
        }
        this.elementId = Objects.requireNonNull(elementId);
        this.voltageSetpoint = voltageSetpoint;
        this.reactivePowerSetpoint = reactivePowerSetpoint;
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager,
                      ReportNode reportNode) {
        T networkElement = getNetworkElement(network, elementId);

        if (networkElement == null) {
            logOrThrow(throwException, getElementName() + " '" + elementId + "' not found");
            return;
        }
        if (voltageSetpoint != null) {
            setVoltageSetpoint(networkElement, voltageSetpoint);
        }
        if (reactivePowerSetpoint != null) {
            setReactivePowerSetpoint(networkElement, reactivePowerSetpoint);
        }
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        T networkElement = getNetworkElement(network, elementId);
        if (networkElement == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else if ((voltageSetpoint == null
            || networkElement instanceof StaticVarCompensator staticVarCompensator && Math.abs(voltageSetpoint - staticVarCompensator.getVoltageSetpoint()) < EPSILON
            || networkElement instanceof VscConverterStation vscConverterStation && Math.abs(voltageSetpoint - vscConverterStation.getVoltageSetpoint()) < EPSILON)
            && (reactivePowerSetpoint == null
            || networkElement instanceof StaticVarCompensator staticVarCompensator && Math.abs(reactivePowerSetpoint - staticVarCompensator.getReactivePowerSetpoint()) < EPSILON
            || networkElement instanceof VscConverterStation vscConverterStation && Math.abs(reactivePowerSetpoint - vscConverterStation.getReactivePowerSetpoint()) < EPSILON)) {
            impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
        }
        return impact;
    }

    public abstract String getElementName();

    protected abstract void setVoltageSetpoint(T networkElement, Double voltageSetpoint);

    protected abstract void setReactivePowerSetpoint(T networkElement, Double reactivePowerSetpoint);

    public abstract T getNetworkElement(Network network, String elementID);

    protected String getElementId() {
        return elementId;
    }

    public Double getReactivePowerSetpoint() {
        return reactivePowerSetpoint;
    }

    public OptionalDouble getOptionalReactivePowerSetpoint() {
        return reactivePowerSetpoint == null ? OptionalDouble.empty() : OptionalDouble.of(reactivePowerSetpoint);
    }

    public Double getVoltageSetpoint() {
        return voltageSetpoint;
    }

    public OptionalDouble getOptionalVoltageSetpoint() {
        return voltageSetpoint == null ? OptionalDouble.empty() : OptionalDouble.of(voltageSetpoint);
    }
}
