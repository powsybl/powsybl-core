/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.OptionalDouble;

/**
 * Simple {@link NetworkModification} for elements that needs to modify
 * their voltage and reactive setpoints.
 *
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
 */
public abstract class AbstractVoltageAndReactiveNetworkModification<T> extends AbstractNetworkModification {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractVoltageAndReactiveNetworkModification.class);

    private final String elementID;
    private final Double voltageSetpoint;
    private final Double reactivePowerSetpoint;

    protected AbstractVoltageAndReactiveNetworkModification(String elementId, Double voltageSetpoint,
                                                            Double reactivePowerSetpoint) {
        if (voltageSetpoint == null && reactivePowerSetpoint == null) {
            LOGGER.warn("Creating a ", getElementName(), " modification with no change !");
        }
        this.elementID = Objects.requireNonNull(elementId);
        this.voltageSetpoint = voltageSetpoint;
        this.reactivePowerSetpoint = reactivePowerSetpoint;
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager,
                      Reporter reporter) {
        T networkElement = getNetworkElement(network, elementID);

        if (networkElement == null) {
            logOrThrow(throwException, getElementName() + " '" + elementID + "' not found");
            return;
        }
        if (voltageSetpoint != null) {
            setVoltageSetpoint(networkElement, voltageSetpoint);
        }
        if (reactivePowerSetpoint != null) {
            setReactivePowerSetpoint(networkElement, reactivePowerSetpoint);
        }
    }

    public abstract String getElementName();

    protected abstract void setVoltageSetpoint(T networkElement, Double voltageSetpoint);

    protected abstract void setReactivePowerSetpoint(T networkElement, Double reactivePowerSetpoint);

    public abstract T getNetworkElement(Network network, String elementID);

    public String getElementID() {
        return elementID;
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
