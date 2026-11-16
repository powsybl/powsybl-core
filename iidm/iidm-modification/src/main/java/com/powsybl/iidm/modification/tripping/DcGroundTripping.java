/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.Set;

/**
 * @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
 */
public class DcGroundTripping extends AbstractTripping {

    public DcGroundTripping(String id) {
        super(id);
    }

    @Override
    public String getName() {
        return "DcGroundTripping";
    }

    @Override
    public void traverseDc(Network network, Set<DcSwitch> dcSwitchesToOpen, Set<DcTerminal> dcTerminalsToDisconnect, Set<DcTerminal> traversedDcTerminals) {
        Objects.requireNonNull(network);
        DcGround dcGround = network.getDcGround(id);
        if (dcGround == null) {
            throw new PowsyblException("DcGround '" + id + "' not found");
        }

        for (DcTerminal t : dcGround.getDcTerminals()) {
            TrippingTopologyTraverser.traverse(t, dcSwitchesToOpen, dcTerminalsToDisconnect, traversedDcTerminals);
        }
    }
}
