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
import com.powsybl.iidm.modification.util.VoltageRegulationUtils;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;

/**
 * Simple {@link NetworkModification} to (dis)connect a shunt compensator and/or change its section.
 *
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 */
public class ShuntCompensatorModification extends AbstractSingleNetworkModification {

    private final String shuntCompensatorId;
    private final Boolean connect;
    private final Integer sectionCount;

    public ShuntCompensatorModification(String shuntCompensatorId, Boolean connect, Integer sectionCount) {
        this.shuntCompensatorId = Objects.requireNonNull(shuntCompensatorId);
        this.connect = connect;
        this.sectionCount = sectionCount;
    }

    @Override
    public void doApply(Network network, NamingStrategy namingStrategy, boolean throwException,
                        ComputationManager computationManager, ReportNode reportNode, boolean dryRun) {
        ShuntCompensator shuntCompensator = network.getShuntCompensator(shuntCompensatorId);

        if (shuntCompensator == null) {
            logOrThrow(throwException, "Shunt Compensator '" + shuntCompensatorId + "' not found");
            return;
        }

        if (connect != null) {
            Terminal t = shuntCompensator.getTerminal();
            if (connect.booleanValue()) {
                t.connect(dryRun);
                setTargetV(shuntCompensator, dryRun);
            } else {
                t.disconnect(dryRun);
            }
        }
        if (sectionCount != null) {
            shuntCompensator.setSectionCount(sectionCount, dryRun);
        }
    }

    @Override
    public String getName() {
        return "ShuntCompensatorModification";
    }

    @Override
    public boolean hasImpactOnNetwork() {
        return false;
    }

    @Override
    public boolean isLocalDryRunPossible() {
        return true;
    }

    private static void setTargetV(ShuntCompensator shuntCompensator, boolean dryRun) {
        if (shuntCompensator.isVoltageRegulatorOn()) {
            VoltageRegulationUtils.getTargetVForRegulatingElement(shuntCompensator.getNetwork(), shuntCompensator.getRegulatingTerminal().getBusView().getBus(),
                    shuntCompensator.getId(), IdentifiableType.SHUNT_COMPENSATOR).ifPresent(v -> shuntCompensator.setTargetV(v, dryRun));
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
