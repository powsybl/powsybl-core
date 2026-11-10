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

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class CgmesEquivalentBranch {

    private static final String EQUIVALENT_BRANCH_R = EQUIVALENT_BRANCH + "." + R;
    private static final String EQUIVALENT_BRANCH_X = EQUIVALENT_BRANCH + "." + X;

    private CgmesEquivalentBranch() {
    }

    public static void writeEq(EquivalentBranchEq branch, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(EQUIVALENT_BRANCH, branch.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(branch.name), writer);
        CgmesExportUtil.writeAttribute(namespace, EQUIVALENT_BRANCH_R, CgmesExportUtil.format(branch.r), writer);
        CgmesExportUtil.writeAttribute(namespace, EQUIVALENT_BRANCH_X, CgmesExportUtil.format(branch.x), writer);
        if (branch.baseVoltage != null) {
            CgmesExportUtil.writeResource(CONDUCTING_EQUIPMENT_BASE_VOLTAGE, branch.baseVoltage, writer, context);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    public record EquivalentBranchEq(
        String rdfId,
        String name,
        double r,
        double x,
        String baseVoltage
    ) {
    }

}
