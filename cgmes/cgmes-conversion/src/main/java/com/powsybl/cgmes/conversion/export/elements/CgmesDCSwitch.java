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

import static com.powsybl.cgmes.model.CgmesNames.IDENTIFIED_OBJECT_NAME;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public final class CgmesDCSwitch {

    private CgmesDCSwitch() {
    }

    public static void writeEq(DCSwitchEq dcSwitch, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(dcSwitch.className, dcSwitch.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(dcSwitch.name), writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record DCSwitchEq(
        String className,
        String rdfId,
        String name
    ) {
    }

}
