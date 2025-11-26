/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.DcSwitch;
import com.powsybl.iidm.network.DcTerminal;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import com.powsybl.math.graph.TraverseResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
 */
class DcTopologyTraverseTest {
    private static final class ConverterTraverser implements DcTerminal.TopologyTraverser {
        private final boolean onlyConnectedDcNode;
        private String firstTraversedConverterId;

        public ConverterTraverser(boolean onlyConnectedDcNode) {
            this.onlyConnectedDcNode = onlyConnectedDcNode;
        }

        @Override
        public TraverseResult traverse(DcTerminal terminal, boolean connected) {
            if (terminal.getDcConnectable().getType() == IdentifiableType.VOLTAGE_SOURCE_CONVERTER) {
                firstTraversedConverterId = terminal.getDcConnectable().getId();
                return TraverseResult.TERMINATE_TRAVERSER;
            }
            return TraverseResult.CONTINUE;
        }

        @Override
        public TraverseResult traverse(DcSwitch aSwitch) {
            if (onlyConnectedDcNode && aSwitch.isOpen()) {
                return TraverseResult.TERMINATE_PATH;
            }
            return TraverseResult.CONTINUE;
        }

        public String getFirstTraversedConverterId() {
            return firstTraversedConverterId;
        }
    }

    private static String getConverterSectionId(DcTerminal terminal) {
        ConverterTraverser connectedConverter = new ConverterTraverser(terminal.isConnected());
        terminal.traverse(connectedConverter);
        return connectedConverter.getFirstTraversedConverterId();
    }

    private static final class DcLineTraverser implements DcTerminal.TopologyTraverser {
        private final boolean onlyConnectedDcNode;
        private String firstTraversedDcLineId;

        public DcLineTraverser(boolean onlyConnectedDcNode) {
            this.onlyConnectedDcNode = onlyConnectedDcNode;
        }

        @Override
        public TraverseResult traverse(DcTerminal terminal, boolean connected) {
            if (terminal.getDcConnectable().getType() == IdentifiableType.DC_LINE) {
                firstTraversedDcLineId = terminal.getDcConnectable().getId();
                return TraverseResult.TERMINATE_TRAVERSER;
            }
            return TraverseResult.CONTINUE;
        }

        @Override
        public TraverseResult traverse(DcSwitch aSwitch) {
            if (onlyConnectedDcNode && aSwitch.isOpen()) {
                return TraverseResult.TERMINATE_PATH;
            }
            return TraverseResult.CONTINUE;
        }

        public String getFirstTraversedDcLineId() {
            return firstTraversedDcLineId;
        }
    }

    private static String getDcLineSectionId(DcTerminal terminal) {
        DcLineTraverser connectedDcLine = new DcLineTraverser(terminal.isConnected());
        terminal.traverse(connectedDcLine);
        return connectedDcLine.getFirstTraversedDcLineId();
    }

    @Test
    void testVscSymmetricalMonopole() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        assertEquals("VscGb", getConverterSectionId(network.getDcNode("dcNodeGbNeg").getDcTerminals().getFirst()));
        assertEquals("VscGb", getConverterSectionId(network.getDcNode("dcNodeGbPos").getDcTerminals().getFirst()));
        assertEquals("VscFr", getConverterSectionId(network.getDcNode("dcNodeFrNeg").getDcTerminals().getFirst()));
        assertEquals("VscFr", getConverterSectionId(network.getDcNode("dcNodeFrPos").getDcTerminals().getFirst()));
        assertEquals("VscFr", getConverterSectionId(network.getDcLine("dcLineNeg").getDcTerminal1()));
        assertEquals("VscGb", getConverterSectionId(network.getDcLine("dcLineNeg").getDcTerminal2()));
        assertEquals("VscFr", getConverterSectionId(network.getDcLine("dcLinePos").getDcTerminal1()));
        assertEquals("VscGb", getConverterSectionId(network.getDcLine("dcLinePos").getDcTerminal2()));
        assertEquals("VscFr", getConverterSectionId(network.getVoltageSourceConverter("VscFr").getDcTerminal1()));
        assertEquals("VscGb", getConverterSectionId(network.getVoltageSourceConverter("VscGb").getDcTerminal1()));

        assertEquals("dcLineNeg", getDcLineSectionId(network.getDcNode("dcNodeGbNeg").getDcTerminals().getFirst()));
        assertEquals("dcLinePos", getDcLineSectionId(network.getDcNode("dcNodeGbPos").getDcTerminals().getFirst()));
        assertEquals("dcLineNeg", getDcLineSectionId(network.getDcNode("dcNodeFrNeg").getDcTerminals().getFirst()));
        assertEquals("dcLinePos", getDcLineSectionId(network.getDcNode("dcNodeFrPos").getDcTerminals().getFirst()));
        assertEquals("dcLineNeg", getDcLineSectionId(network.getDcLine("dcLineNeg").getDcTerminal1()));
        assertEquals("dcLineNeg", getDcLineSectionId(network.getDcLine("dcLineNeg").getDcTerminal2()));
        assertEquals("dcLinePos", getDcLineSectionId(network.getDcLine("dcLinePos").getDcTerminal1()));
        assertEquals("dcLinePos", getDcLineSectionId(network.getDcLine("dcLinePos").getDcTerminal2()));
        assertEquals("dcLineNeg", getDcLineSectionId(network.getVoltageSourceConverter("VscFr").getDcTerminal1()));
        assertEquals("dcLineNeg", getDcLineSectionId(network.getVoltageSourceConverter("VscGb").getDcTerminal1()));
    }

    @Test
    void testLccBipoleGroundReturn() {
        Network network = DcDetailedNetworkFactory.createLccBipoleGroundReturn();

        network.getDcSwitch("dcSwitchGbPosBypass").setOpen(false);
        network.getDcSwitch("dcSwitchFrPosBypass").setOpen(false);

        assertEquals("dcLine2", getDcLineSectionId(network.getDcNode("dcNodeGbNeg").getDcTerminals().getFirst()));
        assertEquals("dcLine1", getDcLineSectionId(network.getDcNode("dcNodeGbPos").getDcTerminals().getFirst()));
        assertEquals("dcLine2", getDcLineSectionId(network.getDcNode("dcNodeFrNeg").getDcTerminals().getFirst()));
        assertEquals("dcLine1", getDcLineSectionId(network.getDcNode("dcNodeFrPos").getDcTerminals().getFirst()));
        assertEquals("dcLine1", getDcLineSectionId(network.getDcLine("dcLine1").getDcTerminal1()));
        assertEquals("dcLine1", getDcLineSectionId(network.getDcLine("dcLine1").getDcTerminal2()));
        assertEquals("dcLine2", getDcLineSectionId(network.getDcLine("dcLine2").getDcTerminal1()));
        assertEquals("dcLine2", getDcLineSectionId(network.getDcLine("dcLine2").getDcTerminal2()));
        assertEquals("dcLine1", getDcLineSectionId(network.getDcNode("dcNodeGbMid").getDcTerminals().getFirst()));
        assertEquals("dcLine1", getDcLineSectionId(network.getDcNode("dcNodeFrMid").getDcTerminals().getFirst()));
    }
}
