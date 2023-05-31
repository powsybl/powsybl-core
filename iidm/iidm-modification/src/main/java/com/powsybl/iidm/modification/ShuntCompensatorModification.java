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
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;

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
                // FIXME just as for generators, when reconnecting a shunt with voltage regulation on,
                // the voltage setpoint must be set
                t.connect();
            } else {
                t.disconnect();
            }
        }
        if (sectionCount != null) {
            shuntCompensator.setSectionCount(sectionCount);
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
