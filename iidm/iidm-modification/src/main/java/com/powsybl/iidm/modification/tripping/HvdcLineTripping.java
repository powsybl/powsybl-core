/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.Set;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class HvdcLineTripping extends AbstractTripping {

    private final String voltageLevelId;

    private static final String NETWORK_MODIFICATION_NAME = "HvdcLineTripping";

    public HvdcLineTripping(String hvdcLineId) {
        this(hvdcLineId, null);
    }

    public HvdcLineTripping(String hvdcLineId, String voltageLevelId) {
        super(hvdcLineId);
        this.voltageLevelId = voltageLevelId;
    }

    @Override
    public void traverse(Network network, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect, Set<Terminal> traversedTerminals) {
        Objects.requireNonNull(network);

        HvdcLine hvdcLine = network.getHvdcLine(id);
        if (hvdcLine == null) {
            throw new PowsyblException("HVDC line '" + hvdcLine + "' not found");
        }

        Terminal terminal1 = hvdcLine.getConverterStation1().getTerminal();
        Terminal terminal2 = hvdcLine.getConverterStation2().getTerminal();

        traverseDoubleSidedEquipment(voltageLevelId, terminal1, terminal2, switchesToOpen, terminalsToDisconnect, traversedTerminals, hvdcLine.getType().name());
    }

    @Override
    protected boolean applyDryRun(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode) {
        HvdcLine hvdcLine = network.getHvdcLine(id);
        if (hvdcLine == null) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                NETWORK_MODIFICATION_NAME,
                String.format("HvdcLine %s not found", id));
        }
        if (voltageLevelId == null) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                NETWORK_MODIFICATION_NAME,
                "voltageLevelId should not be null");
        }
        if (hvdcLine != null && voltageLevelId != null
            && !voltageLevelId.equals(hvdcLine.getConverterStation1().getTerminal().getVoltageLevel().getId())
            && !voltageLevelId.equals(hvdcLine.getConverterStation2().getTerminal().getVoltageLevel().getId())) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                NETWORK_MODIFICATION_NAME,
                String.format("HvdcLine %s is not connected to voltage level %s", id, voltageLevelId));
        }
        return dryRunConclusive;
    }
}
