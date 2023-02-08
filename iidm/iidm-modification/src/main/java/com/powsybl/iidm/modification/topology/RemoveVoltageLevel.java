/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.computation.ComputationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.powsybl.iidm.modification.topology.ModificationReports.notFoundVoltageLevelReport;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class RemoveVoltageLevel extends AbstractNetworkModification {
    private static final Logger LOGGER = LoggerFactory.getLogger(com.powsybl.iidm.modification.topology.RemoveFeederBay.class);

    private final String voltageLevelId;

    public RemoveVoltageLevel(String voltageLevelId) {
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager, Reporter reporter) {
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            LOGGER.error("Voltage level {} not found", voltageLevelId);
            notFoundVoltageLevelReport(reporter, voltageLevelId);
            if (throwException) {
                throw new PowsyblException("Voltage level not found: " + voltageLevelId);
            }
        }

        voltageLevel.getConnectables(HvdcConverterStation.class).forEach(hcs -> {
            hcs.getHvdcLine().remove();
            hcs.remove();
        });

        voltageLevel.getConnectables().forEach(Connectable::remove);

        voltageLevel.remove();
    }

}
