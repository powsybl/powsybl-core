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

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class CgmesCurveData {

    private static final String CURVE_DATA = "CurveData";
    private static final String CURVE_DATA_XVALUE = CURVE_DATA + ".xvalue";
    private static final String CURVE_DATA_Y1VALUE = CURVE_DATA + ".y1value";
    private static final String CURVE_DATA_Y2VALUE = CURVE_DATA + ".y2value";
    private static final String CURVE_DATA_CURVE = CURVE_DATA + ".Curve";

    private CgmesCurveData() {
    }

    public static void writeEq(CurveDataEq curveData, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(CURVE_DATA, curveData.rdfId, false, writer, context);
        CgmesExportUtil.writeAttribute(CURVE_DATA_XVALUE, CgmesExportUtil.format(curveData.xvalue), writer, context);
        CgmesExportUtil.writeAttribute(CURVE_DATA_Y1VALUE, CgmesExportUtil.format(curveData.y1value), writer, context);
        CgmesExportUtil.writeAttribute(CURVE_DATA_Y2VALUE, CgmesExportUtil.format(curveData.y2value), writer, context);
        CgmesExportUtil.writeResource(CURVE_DATA_CURVE, curveData.curve, writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record CurveDataEq(
        String rdfId,
        double xvalue,
        double y1value,
        double y2value,
        String curve
    ) {
    }
}
