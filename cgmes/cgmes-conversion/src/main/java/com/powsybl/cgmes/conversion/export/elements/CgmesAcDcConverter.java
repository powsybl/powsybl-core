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
public final class CgmesAcDcConverter {

    private static final String ACDC_CONVERTER_RATED_UDC = ACDC_CONVERTER + "." + RATED_UDC;
    private static final String ACDC_CONVERTER_IDLE_LOSS = ACDC_CONVERTER + ".idleLoss";
    private static final String ACDC_CONVERTER_SWITCHING_LOSS = ACDC_CONVERTER + ".switchingLoss";
    private static final String ACDC_CONVERTER_RESISTIVE_LOSS = ACDC_CONVERTER + ".resistiveLoss";
    private static final String ACDC_CONVERTER_PCC_TERMINAL = ACDC_CONVERTER + ".PccTerminal";
    private static final String VS_CONVERTER_CAPABILITY_CURVE = VS_CONVERTER + ".CapabilityCurve";

    private CgmesAcDcConverter() {
    }

    public static void writeEq(AcDcConverterEq acDcConverter, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(acDcConverter.className, acDcConverter.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(acDcConverter.name), writer, context);
        CgmesExportUtil.writeAttribute(ACDC_CONVERTER_RATED_UDC, CgmesExportUtil.format(acDcConverter.ratedUdc), writer, context);
        CgmesExportUtil.writeAttribute(ACDC_CONVERTER_IDLE_LOSS, CgmesExportUtil.format(acDcConverter.idleLoss), writer, context);
        CgmesExportUtil.writeAttribute(ACDC_CONVERTER_SWITCHING_LOSS, CgmesExportUtil.format(acDcConverter.switchingLoss), writer, context);
        CgmesExportUtil.writeAttribute(ACDC_CONVERTER_RESISTIVE_LOSS, CgmesExportUtil.format(acDcConverter.resistiveLoss), writer, context);
        CgmesExportUtil.writeResource(EQUIPMENT_EQUIPMENT_CONTAINER, acDcConverter.equipmentContainer, writer, context);
        if (acDcConverter.pccTerminal != null) {
            CgmesExportUtil.writeResource(ACDC_CONVERTER_PCC_TERMINAL, acDcConverter.pccTerminal, writer, context);
        }
        if (acDcConverter.capabilityCurve != null) {
            CgmesExportUtil.writeResource(VS_CONVERTER_CAPABILITY_CURVE, acDcConverter.capabilityCurve, writer, context);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    public record AcDcConverterEq(
        String className,
        String rdfId,
        String name,
        double ratedUdc,
        double idleLoss,
        double switchingLoss,
        double resistiveLoss,
        String capabilityCurve,
        String pccTerminal,
        String equipmentContainer
    ) {
    }

}
