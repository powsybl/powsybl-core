/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.Set;

/**
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
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
            throw new PowsyblException("Tie line '" + tieLine + "' not found");
        }

        Terminal terminal1 = tieLine.getHalf1().getTerminal();
        Terminal terminal2 = tieLine.getHalf2().getTerminal();

        if (voltageLevelId != null) {
            if (voltageLevelId.equals(terminal1.getVoltageLevel().getId())) {
                TrippingTopologyTraverser.traverse(terminal1, switchesToOpen, terminalsToDisconnect, traversedTerminals);
            } else if (voltageLevelId.equals(terminal2.getVoltageLevel().getId())) {
                TrippingTopologyTraverser.traverse(terminal2, switchesToOpen, terminalsToDisconnect, traversedTerminals);
            } else {
                throw new PowsyblException("VoltageLevel '" + voltageLevelId + "' not connected to tie line '" + id + "'");
            }
        } else {
            TrippingTopologyTraverser.traverse(terminal1, switchesToOpen, terminalsToDisconnect, traversedTerminals);
            TrippingTopologyTraverser.traverse(terminal2, switchesToOpen, terminalsToDisconnect, traversedTerminals);
        }
    }
}
