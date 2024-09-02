/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractLineDisconnectionModification<M extends AbstractLineDisconnectionModification<M>> extends AbstractNetworkModification {

    protected String oldLine1Id;
    protected String oldLine2Id;
    protected String lineToRemoveId;

    protected AbstractLineDisconnectionModification(String oldLine1Id, String oldLine2Id, String lineToRemoveId) {
        this.oldLine1Id = Objects.requireNonNull(oldLine1Id);
        this.oldLine2Id = Objects.requireNonNull(oldLine2Id);
        this.lineToRemoveId = Objects.requireNonNull(lineToRemoveId);
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        Line line1Id = network.getLine(this.oldLine1Id);
        Line line2Id = network.getLine(this.oldLine2Id);
        Line lineToBeDeleted = network.getLine(lineToRemoveId);
        // tee point is the voltage level in common with lineToBeMerged1, lineToBeMerged2 and lineToBeDeleted
        if (line1Id == null || line2Id == null || lineToBeDeleted == null
            || TopologyModificationUtils.findTeePoint(line1Id, line2Id, lineToBeDeleted) == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else {
            impact = NetworkModificationImpact.HAS_IMPACT_ON_NETWORK;
        }
        return impact;
    }
}
