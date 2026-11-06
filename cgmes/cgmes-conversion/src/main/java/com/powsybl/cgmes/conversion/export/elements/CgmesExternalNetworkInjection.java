/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
public final class CgmesExternalNetworkInjection {

    private static final String EXTERNAL_NETWORK_INJECTION_GOVERNOR_SCD = EXTERNAL_NETWORK_INJECTION + ".governorSCD";
    private static final String EXTERNAL_NETWORK_INJECTION_MAX_P = EXTERNAL_NETWORK_INJECTION + ".maxP";
    private static final String EXTERNAL_NETWORK_INJECTION_MAX_Q = EXTERNAL_NETWORK_INJECTION + ".maxQ";
    private static final String EXTERNAL_NETWORK_INJECTION_MIN_P = EXTERNAL_NETWORK_INJECTION + ".minP";
    private static final String EXTERNAL_NETWORK_INJECTION_MIN_Q = EXTERNAL_NETWORK_INJECTION + ".minQ";

    private CgmesExternalNetworkInjection() {
    }

    public static void writeEq(ExternalNetworkInjectionEq externalNetworkInjection, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(EXTERNAL_NETWORK_INJECTION, externalNetworkInjection.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(externalNetworkInjection.name), writer, context);
        CgmesExportUtil.writeResource(EQUIPMENT_EQUIPMENT_CONTAINER, externalNetworkInjection.equipmentContainer, writer, context);
        CgmesExportUtil.writeAttribute(EXTERNAL_NETWORK_INJECTION_GOVERNOR_SCD, CgmesExportUtil.format(externalNetworkInjection.governorScd), writer, context);
        CgmesExportUtil.writeAttribute(EXTERNAL_NETWORK_INJECTION_MAX_P, CgmesExportUtil.format(externalNetworkInjection.maxP), writer, context);
        CgmesExportUtil.writeAttribute(EXTERNAL_NETWORK_INJECTION_MAX_Q, CgmesExportUtil.format(externalNetworkInjection.maxQ), writer, context);
        CgmesExportUtil.writeAttribute(EXTERNAL_NETWORK_INJECTION_MIN_P, CgmesExportUtil.format(externalNetworkInjection.minP), writer, context);
        CgmesExportUtil.writeAttribute(EXTERNAL_NETWORK_INJECTION_MIN_Q, CgmesExportUtil.format(externalNetworkInjection.minQ), writer, context);
        if (externalNetworkInjection.regulatingControl != null) {
            CgmesExportUtil.writeResource(REGULATING_COND_EQ_REGULATING_CONTROL, externalNetworkInjection.regulatingControl, writer, context);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    public record ExternalNetworkInjectionEq(
        String rdfId,
        String name,
        String equipmentContainer,
        double governorScd,
        double maxP,
        double maxQ,
        double minP,
        double minQ,
        String regulatingControl
    ) {
    }

}
