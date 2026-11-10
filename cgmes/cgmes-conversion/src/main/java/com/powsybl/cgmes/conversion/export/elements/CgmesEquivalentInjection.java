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
public final class CgmesEquivalentInjection {

    private static final String EQUIVALENT_INJECTION_REGULATION_CAPABILITY = EQUIVALENT_INJECTION + "." + REGULATION_CAPABILITY;
    private static final String EQUIVALENT_INJECTION_MIN_P = EQUIVALENT_INJECTION + ".minP";
    private static final String EQUIVALENT_INJECTION_MAX_P = EQUIVALENT_INJECTION + ".maxP";
    private static final String EQUIVALENT_INJECTION_MIN_Q = EQUIVALENT_INJECTION + ".minQ";
    private static final String EQUIVALENT_INJECTION_MAX_Q = EQUIVALENT_INJECTION + ".maxQ";
    private static final String EQUIVALENT_INJECTION_REACTIVE_CAPABILITY_CURVE = EQUIVALENT_INJECTION + ".ReactiveCapabilityCurve";

    private CgmesEquivalentInjection() {
    }

    public static void writeEq(EquivalentInjectionEq equivalentInjection, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(EQUIVALENT_INJECTION, equivalentInjection.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(equivalentInjection.name), writer);
        CgmesExportUtil.writeAttribute(namespace, EQUIVALENT_INJECTION_REGULATION_CAPABILITY, CgmesExportUtil.format(equivalentInjection.regulationCapability), writer);
        // To avoid Infinite values during reimport, default values are not exported
        if (!CgmesExportUtil.isMinusOrMaxValue(equivalentInjection.minP)) {
            CgmesExportUtil.writeAttribute(namespace, EQUIVALENT_INJECTION_MIN_P, CgmesExportUtil.format(equivalentInjection.minP), writer);
        }
        if (!CgmesExportUtil.isMinusOrMaxValue(equivalentInjection.maxP)) {
            CgmesExportUtil.writeAttribute(namespace, EQUIVALENT_INJECTION_MAX_P, CgmesExportUtil.format(equivalentInjection.maxP), writer);
        }
        if (equivalentInjection.minQ != null && !CgmesExportUtil.isMinusOrMaxValue(equivalentInjection.minQ)) {
            CgmesExportUtil.writeAttribute(namespace, EQUIVALENT_INJECTION_MIN_Q, CgmesExportUtil.format(equivalentInjection.minQ), writer);
        }
        if (equivalentInjection.maxQ != null && !CgmesExportUtil.isMinusOrMaxValue(equivalentInjection.maxQ)) {
            CgmesExportUtil.writeAttribute(namespace, EQUIVALENT_INJECTION_MAX_Q, CgmesExportUtil.format(equivalentInjection.maxQ), writer);
        }
        if (equivalentInjection.reactiveCapabilityCurve != null) {
            CgmesExportUtil.writeResource(EQUIVALENT_INJECTION_REACTIVE_CAPABILITY_CURVE, equivalentInjection.reactiveCapabilityCurve, writer, context);
        }
        CgmesExportUtil.writeResource(CONDUCTING_EQUIPMENT_BASE_VOLTAGE, equivalentInjection.baseVoltage, writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record EquivalentInjectionEq(
        String rdfId,
        String name,
        boolean regulationCapability,
        double maxP,
        Double maxQ,
        double minP,
        Double minQ,
        String reactiveCapabilityCurve,
        String baseVoltage
    ) {
    }

}
