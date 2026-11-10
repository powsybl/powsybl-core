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
public final class CgmesTieFlow {

    private static final String TIE_FLOW_CONTROL_AREA = TIE_FLOW + "." + CONTROL_AREA;
    private static final String TIE_FLOW_TERMINAL = TIE_FLOW + "." + TERMINAL;
    private static final String TIE_FLOW_POSITIVE_FLOW_IN = TIE_FLOW + ".positiveFlowIn";

    private CgmesTieFlow() {
    }

    public static void writeEq(TieFlowEq tieFlow, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(TIE_FLOW, tieFlow.rdfId, false, writer, context);
        CgmesExportUtil.writeResource(TIE_FLOW_CONTROL_AREA, tieFlow.controlArea, writer, context);
        CgmesExportUtil.writeResource(TIE_FLOW_TERMINAL, tieFlow.terminal, writer, context);
        CgmesExportUtil.writeAttribute(namespace, TIE_FLOW_POSITIVE_FLOW_IN, Boolean.toString(true), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public record TieFlowEq(
        String rdfId,
        String controlArea,
        String terminal
    ) {
    }

}
