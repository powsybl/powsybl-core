/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;

import java.util.Objects;

import static com.powsybl.iidm.modification.util.ModificationReports.notFoundSwitchReport;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class OpenSwitch extends AbstractNetworkModification {

    private final String switchId;

    public OpenSwitch(String switchId) {
        this.switchId = Objects.requireNonNull(switchId);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        Switch sw = network.getSwitch(switchId);
        if (sw == null) {
            notFoundSwitchReport(reportNode, switchId);
            logOrThrow(throwException, "Switch '" + switchId + "' not found");
            return;
        }
        sw.setOpen(true);
    }

    @Override
    protected boolean applyDryRun(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode) {
        if (network.getSwitch(switchId) == null) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                "OpenSwitch",
                "Switch '" + switchId + "' not found");
        }
        return dryRunConclusive;
    }

    @Override
    public boolean hasImpactOnNetwork() {
        return false;
    }

    @Override
    public boolean isLocalDryRunPossible() {
        return true;
    }
}
