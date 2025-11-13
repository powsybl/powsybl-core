/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.DcTerminal;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import com.powsybl.math.graph.TraversalType;
import com.powsybl.math.graph.TraverseResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
 */
class DcTopologyTraverseTest {

    private final Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();

    private static final class ConverterTraverser implements DcTerminal.TopologyTraverser {
        private String firstTraversedConverterId;

        @Override
        public TraverseResult traverse(DcTerminal terminal, boolean connected) {
            if (terminal.getDcConnectable().getType() == IdentifiableType.VOLTAGE_SOURCE_CONVERTER) {
                firstTraversedConverterId = terminal.getDcConnectable().getId();
                return TraverseResult.TERMINATE_TRAVERSER;
            }
            return TraverseResult.CONTINUE;
        }

        public String getFirstTraversedConverterId() {
            return firstTraversedConverterId;
        }
    }

    private static String getConverterSectionId(DcTerminal terminal) {
        ConverterTraverser connectedConverter = new ConverterTraverser();
        terminal.traverse(connectedConverter, TraversalType.DEPTH_FIRST);
        return connectedConverter.getFirstTraversedConverterId();
    }

    @Test
    void test() {
        assertEquals("VscFr", getConverterSectionId(network.getDcLine("dcLineNeg").getDcTerminal1()));
        assertEquals("VscGb", getConverterSectionId(network.getDcLine("dcLineNeg").getDcTerminal2()));
        assertEquals("VscFr", getConverterSectionId(network.getDcLine("dcLinePos").getDcTerminal1()));
        assertEquals("VscGb", getConverterSectionId(network.getDcLine("dcLinePos").getDcTerminal2()));
        assertEquals("VscFr", getConverterSectionId(network.getVoltageSourceConverter("VscFr").getDcTerminal1()));
        assertEquals("VscGb", getConverterSectionId(network.getVoltageSourceConverter("VscGb").getDcTerminal1()));
    }
}
