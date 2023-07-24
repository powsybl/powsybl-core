/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * Simple {@link NetworkModification} to (dis)connect a shunt compensator and/or change its section.
 *
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
 */
public class ShuntCompensatorModification extends AbstractNetworkModification {

    private final String shuntCompensatorId;
    private final Boolean connect;
    private final Integer sectionCount;

    public ShuntCompensatorModification(String shuntCompensatorId, Boolean connect, Integer sectionCount) {
        this.shuntCompensatorId = Objects.requireNonNull(shuntCompensatorId);
        this.connect = connect;
        this.sectionCount = sectionCount;
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager,
                      Reporter reporter) {
        ShuntCompensator shuntCompensator = network.getShuntCompensator(shuntCompensatorId);

        if (shuntCompensator == null) {
            logOrThrow(throwException, "Shunt Compensator '" + shuntCompensatorId + "' not found");
            return;
        }

        if (connect != null) {
            Terminal t = shuntCompensator.getTerminal();
            if (connect.booleanValue()) {
                t.connect();
                setTargetV(shuntCompensator);
            } else {
                t.disconnect();
            }
        }
        if (sectionCount != null) {
            shuntCompensator.setSectionCount(sectionCount);
        }
    }

    private static void setTargetV(ShuntCompensator shuntCompensator) {
        if (shuntCompensator.isVoltageRegulatorOn()) {
            Bus bus = shuntCompensator.getRegulatingTerminal().getBusView().getBus();
            if (bus != null) {
                // set voltage setpoint to the same as other generators regulating this bus
                double targetV = shuntCompensator.getNetwork().getGeneratorStream()
                    .filter(gen -> bus.equals(gen.getRegulatingTerminal().getBusView().getBus()))
                    .findFirst().map(Generator::getTargetV).orElse(Double.NaN);
                if (!Double.isNaN(targetV)) {
                    shuntCompensator.setTargetV(targetV);
                } else if (!Double.isNaN(bus.getV())) {
                    // if no generators are connected to the bus, set voltage setpoint to network voltage
                    shuntCompensator.setTargetV(bus.getV());
                }
            }
        }
    }

    public Boolean getConnect() {
        return connect;
    }

    public Integer getSectionCount() {
        return sectionCount;
    }

    public String getShuntCompensatorId() {
        return shuntCompensatorId;
    }

}
