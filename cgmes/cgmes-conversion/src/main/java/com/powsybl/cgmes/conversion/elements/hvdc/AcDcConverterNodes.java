/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.*;

import com.powsybl.cgmes.model.CgmesDcTerminal;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.triplestore.api.PropertyBag;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class AcDcConverterNodes {
    private final Map<String, AcDcConverterNode> converterNodes;

    AcDcConverterNodes(CgmesModel cgmesModel) {
        this.converterNodes = new HashMap<>();

        cgmesModel.acDcConverters().forEach(c -> computeAcDcConverter(cgmesModel, c));
        cgmesModel.dcTerminals().forEach(c -> computeDcTerminalToAcDcConverter(cgmesModel, c));
    }

    private void computeAcDcConverter(CgmesModel cgmesModel, PropertyBag c) {
        String id = c.getId("ACDCConverter");
        CgmesTerminal t = cgmesModel.terminal(c.getId("Terminal"));
        String acNode = CgmesDcConversion.getAcNode(cgmesModel, t);

        converterNodes.computeIfAbsent(id, k -> new AcDcConverterNode(id, acNode));
    }

    private void computeDcTerminalToAcDcConverter(CgmesModel cgmesModel, PropertyBag t) {
        CgmesDcTerminal dcTerminal = new CgmesDcTerminal(t);
        if (dcTerminal.dcConductingEquipmentType().equals("CsConverter") ||
            dcTerminal.dcConductingEquipmentType().equals("VsConverter")) {

            AcDcConverterNode acDcConverter = converterNodes.get(dcTerminal.dcConductingEquipment());
            if (acDcConverter != null) {
                acDcConverter.addDcNode(CgmesDcConversion.getDcNode(cgmesModel, dcTerminal));
            }
        }
    }

    Map<String, AcDcConverterNode> getConverterNodes() {
        return converterNodes;
    }

    List<String> getDcNodes(String acDcConverterId) {
        return Optional.ofNullable(converterNodes.get(acDcConverterId).dcNode).orElse(Collections.emptyList());
    }

    static class AcDcConverterNode {
        final String id;
        final String acNode;
        final List<String> dcNode;

        AcDcConverterNode(String id, String acNode) {
            Objects.requireNonNull(id);
            Objects.requireNonNull(acNode);
            this.id = id;
            this.acNode = acNode;
            this.dcNode = new ArrayList<>();
        }

        void addDcNode(String dcNode) {
            this.dcNode.add(dcNode);
        }
    }
}
