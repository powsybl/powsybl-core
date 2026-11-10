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
public final class CgmesCurve {

    private static final String CURVE_STYLE_STRAIGHT_LINE_Y_VALUES = "CurveStyle.straightLineYValues";
    private static final String UNIT_SYMBOL_W = "UnitSymbol.W";
    private static final String UNIT_SYMBOL_VAR = "UnitSymbol.VAr";
    private static final String CURVE_CURVE_STYLE = CURVE + ".curveStyle";
    private static final String CURVE_X_UNIT = CURVE + ".xUnit";
    private static final String CURVE_Y1_UNIT = CURVE + ".y1Unit";
    private static final String CURVE_Y2_UNIT = CURVE + ".y2Unit";

    private CgmesCurve() {
    }

    public static void writeEq(CurveEq curve, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(curve.className, curve.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(curve.name), writer);
        CgmesExportUtil.writeResource(CURVE_CURVE_STYLE, namespace, namespace + CURVE_STYLE_STRAIGHT_LINE_Y_VALUES, writer);
        CgmesExportUtil.writeResource(CURVE_X_UNIT, namespace, namespace + UNIT_SYMBOL_W, writer);
        CgmesExportUtil.writeResource(CURVE_Y1_UNIT, namespace, namespace + UNIT_SYMBOL_VAR, writer);
        CgmesExportUtil.writeResource(CURVE_Y2_UNIT, namespace, namespace + UNIT_SYMBOL_VAR, writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public record CurveEq(
        String className,
        String rdfId,
        String name
    ) {
    }

}
