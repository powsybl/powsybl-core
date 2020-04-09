/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.powsybl.cgmes.model.CgmesDcTerminal;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.triplestore.api.PropertyBag;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class AcDcConverterNodes {
    private final Map<String, AcDcConverterNode> converterNodes;

    AcDcConverterNodes(CgmesModel cgmesModel) {
        this.converterNodes = new HashMap<>();

        cgmesModel.acDcConverters().forEach(c -> computeAcDcConverter(cgmesModel, c));
        cgmesModel.dcTerminals().forEach(this::computeDcTerminalToAcDcConverter);
    }

    private void computeAcDcConverter(CgmesModel cgmesModel, PropertyBag c) {
        String id = c.getId("ACDCConverter");
        CgmesTerminal t = cgmesModel.terminal(c.getId("Terminal"));
        String acTopologicalNode = t.topologicalNode();

        converterNodes.computeIfAbsent(id, k -> new AcDcConverterNode(id, acTopologicalNode));
    }

    private void computeDcTerminalToAcDcConverter(PropertyBag t) {
        CgmesDcTerminal dcTerminal = new CgmesDcTerminal(t);
        if (dcTerminal.dcConductingEquipmentType().equals("CsConverter") ||
            dcTerminal.dcConductingEquipmentType().equals("VsConverter")) {

            AcDcConverterNode acDcConverter = converterNodes.get(dcTerminal.dcConductingEquipment());
            if (acDcConverter != null) {
                acDcConverter.addDcTopologicalNode(dcTerminal.dcTopologicalNode());
            }
        }
    }

    Map<String, AcDcConverterNode> getConverterNodes() {
        return converterNodes;
    }

    static class AcDcConverterNode {
        final String id;
        final String acTopologicalNode;
        final List<String> dcTopologicalNode;

        AcDcConverterNode(String id, String acTopologicalNode) {
            Objects.requireNonNull(id);
            Objects.requireNonNull(acTopologicalNode);
            this.id = id;
            this.acTopologicalNode = acTopologicalNode;
            this.dcTopologicalNode = new ArrayList<>();
        }

        void addDcTopologicalNode(String dcTopologicalNode) {
            this.dcTopologicalNode.add(dcTopologicalNode);
        }
    }
}
