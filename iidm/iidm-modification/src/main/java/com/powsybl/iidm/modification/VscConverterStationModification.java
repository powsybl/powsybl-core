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
import com.powsybl.iidm.network.VscConverterStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.OptionalDouble;

/**
 * Simple {@link NetworkModification} for vsc converter stations.
 *
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
 */
public class VscConverterStationModification extends AbstractNetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(VscConverterStationModification.class);

    private final String vscId;
    private final OptionalDouble voltageSetpoint;
    private final OptionalDouble reactivePowerSetpoint;

    public VscConverterStationModification(String vscId, OptionalDouble voltageSetpoint,
                                           OptionalDouble reactivePowerSetpoint) {
        this.vscId = Objects.requireNonNull(vscId);
        this.voltageSetpoint = Objects.requireNonNull(voltageSetpoint);
        this.reactivePowerSetpoint = Objects.requireNonNull(reactivePowerSetpoint);
        if (voltageSetpoint.isEmpty() && reactivePowerSetpoint.isEmpty()) {
            LOGGER.warn("Creating a VscConverterStationModification with no change !");
        }
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager,
                      Reporter reporter) {
        VscConverterStation vsc = network.getVscConverterStation(vscId);

        if (vsc == null) {
            logOrThrow(throwException, "VscConverterStation '" + vscId + "' not found");
            return;
        }
        voltageSetpoint.ifPresent(vsc::setVoltageSetpoint);
        reactivePowerSetpoint.ifPresent(vsc::setReactivePowerSetpoint);
    }

    public String getVscId() {
        return vscId;
    }

    public OptionalDouble getReactivePowerSetpoint() {
        return reactivePowerSetpoint;
    }

    public OptionalDouble getVoltageSetpoint() {
        return voltageSetpoint;
    }
}
