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

import java.util.Objects;

/**
 * Simple {@link NetworkModification} to change the section of a shunt compensator.
 *
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
 */
public class ShuntCompensatorPositionModification extends AbstractNetworkModification {

    private final String shuntCompensatorId;
    private final int sectionCount;

    public ShuntCompensatorPositionModification(String shuntCompensatorId, int sectionCount) {
        this.shuntCompensatorId = Objects.requireNonNull(shuntCompensatorId);
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
        shuntCompensator.setSectionCount(sectionCount);
    }

    public int getSectionCount() {
        return sectionCount;
    }

    public String getShuntCompensatorId() {
        return shuntCompensatorId;
    }

}
