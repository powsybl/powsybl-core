/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class EquivalentBranchEq {

    private static final String EQ_EQUIVALENT_BRANCH_R = "EquivalentBranch.r";
    private static final String EQ_EQUIVALENT_BRANCH_X = "EquivalentBranch.x";

    public static void write(String id, String equivalentBranchName, String baseVoltage, double r, double x,
                             String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("EquivalentBranch", id, equivalentBranchName, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, EQ_EQUIVALENT_BRANCH_R);
        writer.writeCharacters(CgmesExportUtil.format(r));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_EQUIVALENT_BRANCH_X);
        writer.writeCharacters(CgmesExportUtil.format(x));
        writer.writeEndElement();
        if (baseVoltage != null) {
            CgmesExportUtil.writeReference("ConductingEquipment.BaseVoltage", baseVoltage, cimNamespace, writer, context);
        }
        writer.writeEndElement();
    }

    private EquivalentBranchEq() {
    }
}
