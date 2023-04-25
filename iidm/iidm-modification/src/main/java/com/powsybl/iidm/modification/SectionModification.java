/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
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
public class SectionModification extends AbstractNetworkModification {

    private final String shuntCompensatorId;
    private final int sectionCount;

    public SectionModification(String shuntCompensatorId, int sectionCount) {
        this.shuntCompensatorId = Objects.requireNonNull(shuntCompensatorId);
        this.sectionCount = sectionCount;
        if (sectionCount < 0) {
            throw new PowsyblException("Section count of a Shunt can't be negative");
        }
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager,
                      Reporter reporter) {
        ShuntCompensator shuntCompensator = network.getShuntCompensator(shuntCompensatorId);
        if (shuntCompensator == null) {
            logOrThrow(throwException, "Shunt Compensator '" + shuntCompensatorId + "' not found");
            return;
        } else if (shuntCompensator.getMaximumSectionCount() < sectionCount) {
            logOrThrow(throwException,
                    "Section Count to apply is greater than the maximum value of the shunt compensator." + sectionCount + " > " + shuntCompensator.getMaximumSectionCount());
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
