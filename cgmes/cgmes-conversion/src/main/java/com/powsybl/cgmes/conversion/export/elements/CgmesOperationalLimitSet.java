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
public final class CgmesOperationalLimitSet {

    private static final String OPERATIONAL_LIMIT_SET_TERMINAL = OPERATIONAL_LIMIT_SET + "." + TERMINAL;

    private CgmesOperationalLimitSet() {
    }

    public static void writeEq(OperationalLimitSetEq operationalLimitSet, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(OPERATIONAL_LIMIT_SET, operationalLimitSet.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(operationalLimitSet.name), writer, context);
        CgmesExportUtil.writeResource(OPERATIONAL_LIMIT_SET_TERMINAL, operationalLimitSet.terminal, writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record OperationalLimitSetEq(
        String rdfId,
        String name,
        String terminal
    ) {
    }
}
