/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;
import java.util.Set;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class HvdcLineTripping extends AbstractTripping {

    private final String hvdcLineId;

    private final String voltageLevelId;

    public HvdcLineTripping(String hvdcLineId) {
        this(hvdcLineId, null);
    }

    public HvdcLineTripping(String hvdcLineId, String voltageLevelId) {
        this.hvdcLineId = Objects.requireNonNull(hvdcLineId);
        this.voltageLevelId = voltageLevelId;
    }

    @Override
    public void traverse(Network network, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect, Set<Terminal> traversedTerminals) {
        Objects.requireNonNull(network);

        HvdcLine hvdcLine = network.getHvdcLine(hvdcLineId);
        if (hvdcLine == null) {
            throw new PowsyblException("HVDC line '" + hvdcLine + "' not found");
        }

        Terminal terminal1 = hvdcLine.getConverterStation1().getTerminal();
        Terminal terminal2 = hvdcLine.getConverterStation2().getTerminal();

        if (voltageLevelId != null) {
            if (voltageLevelId.equals(terminal1.getVoltageLevel().getId())) {
                TrippingTopologyTraverser.traverse(terminal1, switchesToOpen, terminalsToDisconnect, traversedTerminals);
            } else if (voltageLevelId.equals(terminal2.getVoltageLevel().getId())) {
                TrippingTopologyTraverser.traverse(terminal2, switchesToOpen, terminalsToDisconnect, traversedTerminals);
            } else {
                throw new PowsyblException("VoltageLevel '" + voltageLevelId + "' not connected to HVDC line '" + hvdcLineId + "'");
            }
        } else {
            TrippingTopologyTraverser.traverse(terminal1, switchesToOpen, terminalsToDisconnect, traversedTerminals);
            TrippingTopologyTraverser.traverse(terminal2, switchesToOpen, terminalsToDisconnect, traversedTerminals);
        }
    }
}
