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
import com.powsybl.iidm.network.StaticVarCompensator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.OptionalDouble;

/**
 * Simple {@link NetworkModification} for static var compensator.
 *
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
 */
public class StaticVarCompensatorModification extends AbstractNetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticVarCompensatorModification.class);

    private final String svcId;
    private final OptionalDouble voltageSetPoint;
    private final OptionalDouble reactivePowerSetPoint;

    public StaticVarCompensatorModification(String svcId, OptionalDouble voltageSetPoint,
                                            OptionalDouble reactivePowerSetPoint) {
        this.svcId = svcId;
        this.voltageSetPoint = voltageSetPoint;
        this.reactivePowerSetPoint = reactivePowerSetPoint;
        if (voltageSetPoint.isEmpty() && reactivePowerSetPoint.isEmpty()) {
            LOGGER.warn("Creating a StaticVarCompensatorModification with no change !");
        }
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager,
                      Reporter reporter) {
        StaticVarCompensator svc = network.getStaticVarCompensator(svcId);

        if (svc == null) {
            logOrThrow(throwException, "StaticVarcompensator '" + svcId + "' not found");
            return;
        }
        voltageSetPoint.ifPresent(svc::setVoltageSetpoint);
        reactivePowerSetPoint.ifPresent(svc::setReactivePowerSetpoint);
    }

    public String getSvcId() {
        return svcId;
    }

    public OptionalDouble getReactivePowerSetPoint() {
        return reactivePowerSetPoint;
    }

    public OptionalDouble getVoltageSetPoint() {
        return voltageSetPoint;
    }
}
