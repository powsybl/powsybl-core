/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TieLine;

import java.util.Objects;
import java.util.Set;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public class TieLineTripping extends AbstractTripping {

    private final String voltageLevelId;

    public TieLineTripping(String tieLineId) {
        this(tieLineId, null);
    }

    public TieLineTripping(String tieLineId, String voltageLevelId) {
        super(tieLineId);
        this.voltageLevelId = voltageLevelId;
    }

    @Override
    public void traverse(Network network, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect, Set<Terminal> traversedTerminals) {
        Objects.requireNonNull(network);

        TieLine tieLine = network.getTieLine(id);
        if (tieLine == null) {
            throw new PowsyblException("Tie line '" + id + "' not found");
        }

        Terminal terminal1 = tieLine.getDanglingLine1().getTerminal();
        Terminal terminal2 = tieLine.getDanglingLine2().getTerminal();

        traverseDoubleSidedEquipment(voltageLevelId, terminal1, terminal2, switchesToOpen, terminalsToDisconnect, traversedTerminals, tieLine.getType().name());
    }

    @Override
    public String getName() {
        return "TieLineTripping";
    }
}
