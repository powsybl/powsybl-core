/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class CgmesRegulatingControl {

    private static final String REGULATING_CONTROL_TERMINAL = REGULATING_CONTROL + "." + TERMINAL;
    private static final String REGULATING_CONTROL_MODE = REGULATING_CONTROL + "." + MODE;

    private CgmesRegulatingControl() {
    }

    public static void writeEq(RegulatingControlEq regulatingControl, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(REGULATING_CONTROL, regulatingControl.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(regulatingControl.name), writer);
        CgmesExportUtil.writeResource(REGULATING_CONTROL_TERMINAL, regulatingControl.terminal, writer, context);
        CgmesExportUtil.writeResource(REGULATING_CONTROL_MODE, namespace,
            namespace + regulatingControl.mode, writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public record RegulatingControlEq(
        String rdfId,
        String name,
        String terminal,
        String mode
    ) {
    }
}
