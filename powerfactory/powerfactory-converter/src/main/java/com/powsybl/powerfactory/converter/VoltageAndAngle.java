/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.converter;

import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraverseResult;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;

import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

class VoltageAndAngle extends AbstractConverter {

    VoltageAndAngle(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    void update(DataObject elmTerm) {
        if (!getImportContext().elmTermIdToNode.containsKey(elmTerm.getId())) {
            return;
        }
        Optional<Float> uknom = elmTerm.findFloatAttributeValue("uknom");
        Optional<Float> u = elmTerm.findFloatAttributeValue("m:u");
        Optional<Float> phiu = elmTerm.findFloatAttributeValue("m:phiu");

        // Some voltages may come with a negative sign (DC nodes), ignore them
        if (uknom.isPresent() && u.isPresent() && phiu.isPresent() && u.get() >= 0) {
            NodeRef nodeRef = getImportContext().elmTermIdToNode.get(elmTerm.getId());
            // Registered nodes may have not led to a node in the IIDM network, check that node exists
            VoltageLevel.NodeBreakerView nb = getNetwork().getVoltageLevel(nodeRef.voltageLevelId).getNodeBreakerView();
            if (nodeRef.node <= nb.getMaximumNodeIndex()) {
                double v = u.get() * uknom.get();
                double angle = phiu.get();
                update(nb, nodeRef.node, v, angle);
            }
        }
    }

    private static void update(VoltageLevel.NodeBreakerView nb, int node, double v, double angle) {
        Terminal terminal = nb.getTerminal(node);
        if (terminal != null) {
            update(terminal, v, angle);
        } else {
            // If the node does not have a terminal,
            // try to find one at the nodes connected to it
            nb.traverse(node, (node1, sw, node2) -> {
                if (sw != null && sw.isOpen()) {
                    return TraverseResult.TERMINATE_PATH;
                }
                Terminal terminal2 = nb.getTerminal(node2);
                if (terminal2 != null) {
                    update(terminal2, v, angle);
                    return TraverseResult.TERMINATE_TRAVERSER;
                }
                return TraverseResult.CONTINUE;
            });
        }
    }

    private static void update(Terminal terminal, double v, double angle) {
        Bus bus = terminal.getBusView().getBus();
        if (bus != null) {
            bus.setV(v);
            bus.setAngle(angle);
        }
    }
}
