/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;

import java.util.Objects;

import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class CloseSwitch extends AbstractNetworkModification {
    private final String switchId;

    public CloseSwitch(String switchId) {
        this.switchId = Objects.requireNonNull(switchId);
    }

    @Override
    public String getName() {
        return "CloseSwitch";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        Switch sw = network.getSwitch(switchId);
        if (sw == null) {
            logOrThrow(throwException, "Switch '" + switchId + "' not found");
            return;
        } else {
            sw.setOpen(false);
        }
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        Switch sw = network.getSwitch(switchId);
        if (sw == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else if (!sw.isOpen()) {
            impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
        }
        return impact;
    }
}
