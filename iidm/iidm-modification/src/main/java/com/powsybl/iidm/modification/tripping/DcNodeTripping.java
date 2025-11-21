/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DcNode;
import com.powsybl.iidm.network.DcTerminal;
import com.powsybl.iidm.network.Network;

import java.util.Objects;
import java.util.Set;

/**
 * @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
 */
public class DcNodeTripping extends AbstractTripping {

    public DcNodeTripping(String id) {
        super(id);
    }

    @Override
    public String getName() {
        return "DcNodeTripping";
    }

    @Override
    public void traverseDc(Network network, Set<DcTerminal> terminalsToDisconnect, Set<DcTerminal> traversedDcTerminals) {
        Objects.requireNonNull(network);
        DcNode dcNode = network.getDcNode(id);
        if (dcNode == null) {
            throw new PowsyblException("DcNode '" + id + "' not found");
        }

        for (DcTerminal t : dcNode.getDcTerminals()) {
            TrippingTopologyTraverser.traverse(t, terminalsToDisconnect, traversedDcTerminals);
        }
    }
}
