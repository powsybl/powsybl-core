/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.util.VoltageRegulationUtils;
import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * Simple {@link NetworkModification} to (dis)connect a shunt compensator and/or change its section,
 * if the model is linear you can modify the b per section.
 *
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
 */
public class ShuntCompensatorModification extends AbstractNetworkModification {

    private final String shuntCompensatorId;
    private final Boolean connect;
    private final Integer sectionCount;
    private final Double bPerSection;

    public ShuntCompensatorModification(String shuntCompensatorId, Boolean connect, Integer sectionCount) {
        this(shuntCompensatorId, connect, sectionCount, null);
    }

    public ShuntCompensatorModification(String shuntCompensatorId, Boolean connect, Integer sectionCount,
                                        Double bPerSection) {
        this.shuntCompensatorId = Objects.requireNonNull(shuntCompensatorId);
        this.connect = connect;
        this.sectionCount = sectionCount;
        this.bPerSection = bPerSection;
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager,
                      Reporter reporter) {
        ShuntCompensator shuntCompensator = network.getShuntCompensator(shuntCompensatorId);

        if (shuntCompensator == null) {
            logOrThrow(throwException, "Shunt Compensator '" + shuntCompensatorId + "' not found");
            return;
        }

        if (shuntCompensator.getModelType().equals(ShuntCompensatorModelType.LINEAR) && Objects.nonNull(bPerSection)) {
            ((ShuntCompensatorLinearModel) (shuntCompensator.getModel())).setBPerSection(bPerSection);
        } else if (Objects.nonNull(bPerSection)) {
            logOrThrow(throwException,
                "Shunt compensator model is non linear: bPerSection cannot be changed");
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
            VoltageRegulationUtils.getTargetVForRegulatingElement(shuntCompensator.getNetwork(), shuntCompensator.getRegulatingTerminal().getBusView().getBus(),
                    shuntCompensator.getId(), IdentifiableType.SHUNT_COMPENSATOR).ifPresent(shuntCompensator::setTargetV);
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
