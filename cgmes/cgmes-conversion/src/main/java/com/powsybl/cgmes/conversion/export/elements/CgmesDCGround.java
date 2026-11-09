/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
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
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public final class CgmesDCGround {

    private static final String DC_GROUND_R = DC_GROUND + "." + R;

    private CgmesDCGround() {
    }

    public static void writeEq(DCGroundEq dcGround, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(DC_GROUND, dcGround.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(dcGround.name), writer, context);
        CgmesExportUtil.writeAttribute(DC_GROUND_R, CgmesExportUtil.format(dcGround.r), writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record DCGroundEq(
        String rdfId,
        String name,
        double r
    ) {
    }

}
