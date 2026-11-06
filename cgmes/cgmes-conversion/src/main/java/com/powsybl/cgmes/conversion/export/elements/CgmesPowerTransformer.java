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
public final class CgmesPowerTransformer {

    private static final double POWER_TRANSFORMER_END_RATED_S_DEFAULT_VALUE = 100.0;

    private static final String TRANSFORMER_END_END_NUMBER = TRANSFORMER_END + ".endNumber";
    private static final String TRANSFORMER_END_TERMINAL = TRANSFORMER_END + "." + TERMINAL;
    private static final String TRANSFORMER_END_BASE_VOLTAGE = TRANSFORMER_END + "." + BASE_VOLTAGE;

    private static final String POWER_TRANSFORMER_END_POWER_TRANSFORMER = POWER_TRANSFORMER_END + "." + POWER_TRANSFORMER;
    private static final String POWER_TRANSFORMER_END_R = POWER_TRANSFORMER_END + "." + R;
    private static final String POWER_TRANSFORMER_END_X = POWER_TRANSFORMER_END + "." + X;
    private static final String POWER_TRANSFORMER_END_G = POWER_TRANSFORMER_END + "." + G;
    private static final String POWER_TRANSFORMER_END_B = POWER_TRANSFORMER_END + "." + B;
    private static final String POWER_TRANSFORMER_END_RATED_S = POWER_TRANSFORMER_END + "." + RATEDS;
    private static final String POWER_TRANSFORMER_END_RATED_U = POWER_TRANSFORMER_END + "." + RATEDU;

    private CgmesPowerTransformer() {
    }

    public static void writeEq(PowerTransformerEq powerTransformer, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(POWER_TRANSFORMER, powerTransformer.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(powerTransformer.name), writer, context);
        if (powerTransformer.equipmentContainer != null) {
            CgmesExportUtil.writeResource(EQUIPMENT_EQUIPMENT_CONTAINER, powerTransformer.equipmentContainer, writer, context);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeEndEq(PowerTransformerEndEq powerTransformerEnd, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(POWER_TRANSFORMER_END, powerTransformerEnd.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(powerTransformerEnd.name), writer, context);
        CgmesExportUtil.writeAttribute(TRANSFORMER_END_END_NUMBER, CgmesExportUtil.format(powerTransformerEnd.endNumber), writer, context);
        CgmesExportUtil.writeAttribute(POWER_TRANSFORMER_END_R, CgmesExportUtil.format(powerTransformerEnd.r), writer, context);
        CgmesExportUtil.writeAttribute(POWER_TRANSFORMER_END_X, CgmesExportUtil.format(powerTransformerEnd.x), writer, context);
        CgmesExportUtil.writeAttribute(POWER_TRANSFORMER_END_G, CgmesExportUtil.format(powerTransformerEnd.g), writer, context);
        CgmesExportUtil.writeAttribute(POWER_TRANSFORMER_END_B, CgmesExportUtil.format(powerTransformerEnd.b), writer, context);
        CgmesExportUtil.writeAttribute(POWER_TRANSFORMER_END_RATED_S, CgmesExportUtil.format(powerTransformerEnd.ratedS, POWER_TRANSFORMER_END_RATED_S_DEFAULT_VALUE), writer, context);
        CgmesExportUtil.writeAttribute(POWER_TRANSFORMER_END_RATED_U, CgmesExportUtil.format(powerTransformerEnd.ratedU), writer, context);
        CgmesExportUtil.writeResource(POWER_TRANSFORMER_END_POWER_TRANSFORMER, powerTransformerEnd.powerTransformer, writer, context);
        CgmesExportUtil.writeResource(TRANSFORMER_END_TERMINAL, powerTransformerEnd.terminal, writer, context);
        CgmesExportUtil.writeResource(TRANSFORMER_END_BASE_VOLTAGE, powerTransformerEnd.baseVoltage, writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record PowerTransformerEq(
        String rdfId,
        String name,
        String equipmentContainer
    ) {
    }

    public record PowerTransformerEndEq(
        String rdfId,
        String name,
        int endNumber,
        double r,
        double x,
        double g,
        double b,
        double ratedS,
        double ratedU,
        String powerTransformer,
        String baseVoltage,
        String terminal
    ) {
    }

}
