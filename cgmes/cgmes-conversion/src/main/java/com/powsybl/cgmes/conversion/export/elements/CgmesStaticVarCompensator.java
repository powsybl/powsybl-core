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
public final class CgmesStaticVarCompensator {

    private static final String STATIC_VAR_COMPENSATOR_INDUCTIVE_RATING = STATIC_VAR_COMPENSATOR + ".inductiveRating";
    private static final String STATIC_VAR_COMPENSATOR_CAPACITIVE_RATING = STATIC_VAR_COMPENSATOR + ".capacitiveRating";
    private static final String STATIC_VAR_COMPENSATOR_SLOPE = STATIC_VAR_COMPENSATOR + ".slope";
    private static final String STATIC_VAR_COMPENSATOR_SVC_CONTROL_MODE = STATIC_VAR_COMPENSATOR + ".sVCControlMode";
    private static final String STATIC_VAR_COMPENSATOR_VOLTAGE_SET_POINT = STATIC_VAR_COMPENSATOR + ".voltageSetPoint";

    private CgmesStaticVarCompensator() {
    }

    public static void writeEq(StaticVarCompensatorEq staticVarCompensator, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(STATIC_VAR_COMPENSATOR, staticVarCompensator.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(staticVarCompensator.name), writer, context);
        CgmesExportUtil.writeResource(EQUIPMENT_EQUIPMENT_CONTAINER, staticVarCompensator.equipmentContainer, writer, context);
        if (staticVarCompensator.regulatingControl != null) {
            CgmesExportUtil.writeResource(REGULATING_COND_EQ_REGULATING_CONTROL, staticVarCompensator.regulatingControl, writer, context);
        }
        CgmesExportUtil.writeAttribute(STATIC_VAR_COMPENSATOR_INDUCTIVE_RATING, CgmesExportUtil.format(staticVarCompensator.inductiveRating), writer, context);
        CgmesExportUtil.writeAttribute(STATIC_VAR_COMPENSATOR_CAPACITIVE_RATING, CgmesExportUtil.format(staticVarCompensator.capacitiveRating), writer, context);
        CgmesExportUtil.writeAttribute(STATIC_VAR_COMPENSATOR_SLOPE, CgmesExportUtil.format(Double.isNaN(staticVarCompensator.slope) ? 0. : staticVarCompensator.slope), writer, context);
        CgmesExportUtil.writeResource(STATIC_VAR_COMPENSATOR_SVC_CONTROL_MODE, context.getCim().getNamespace(),
            context.getCim().getNamespace() + staticVarCompensator.svcControlMode, writer);
        CgmesExportUtil.writeAttribute(STATIC_VAR_COMPENSATOR_VOLTAGE_SET_POINT, CgmesExportUtil.format(staticVarCompensator.voltageSetPoint), writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record StaticVarCompensatorEq(
        String rdfId,
        String name,
        double capacitiveRating,
        double inductiveRating,
        double slope,
        String svcControlMode,
        double voltageSetPoint,
        String regulatingControl,
        String equipmentContainer
    ) {
    }

}
