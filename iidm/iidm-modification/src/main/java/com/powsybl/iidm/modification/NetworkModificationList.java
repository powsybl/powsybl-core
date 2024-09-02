/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
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
import com.powsybl.iidm.network.Terminal;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NetworkModificationList extends AbstractNetworkModification {

    private final List<NetworkModification> modificationList;

    public NetworkModificationList(List<NetworkModification> modificationList) {
        this.modificationList = Objects.requireNonNull(modificationList);
    }

    public NetworkModificationList(NetworkModification... modificationList) {
        this(Arrays.asList(modificationList));
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        modificationList.forEach(modification -> modification.apply(network, namingStrategy, throwException, computationManager, reportNode));
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        modificationList.forEach(modification -> {
            // If a sub-modification cannot be applied, the whole modification cannot be applied too
            if (impact != NetworkModificationImpact.CANNOT_BE_APPLIED) {
                NetworkModificationImpact unitaryImpact = modification.hasImpactOnNetwork(network);
                if (unitaryImpact == NetworkModificationImpact.HAS_IMPACT_ON_NETWORK) {
                    impact = unitaryImpact;
                }
            }
        });
        return impact;
    }
}
