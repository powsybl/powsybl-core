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
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.ReactiveLimitsHolder;
import com.powsybl.iidm.network.VscConverterStation;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class ReactiveCapabilityCurveEq {

    public static final String CURVE_STYLE_STRAIGHTLINEYVALUES = "CurveStyle.straightLineYValues";
    public static final String UNITSYMBOL_W = "UnitSymbol.W";
    public static final String UNITSYMBOL_VAR = "UnitSymbol.VAr";

    public static void write(String id, String reactiveCapabilityCurveName, ReactiveLimitsHolder holder, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName(holderClassName(holder), id, reactiveCapabilityCurveName, cimNamespace, writer, context);
        writer.writeEmptyElement(cimNamespace, "Curve.curveStyle");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + CURVE_STYLE_STRAIGHTLINEYVALUES);
        writer.writeEmptyElement(cimNamespace, "Curve.xUnit");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + UNITSYMBOL_W);
        writer.writeEmptyElement(cimNamespace, "Curve.y1Unit");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + UNITSYMBOL_VAR);
        writer.writeEmptyElement(cimNamespace, "Curve.y2Unit");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + UNITSYMBOL_VAR);
        writer.writeEndElement();
    }

    private static String holderClassName(ReactiveLimitsHolder holder) {
        if (holder instanceof Generator || holder instanceof Battery) {
            return "ReactiveCapabilityCurve";
        } else if (holder instanceof VscConverterStation) {
            return "VsCapabilityCurve";
        }
        throw new PowsyblException("Unexpected holder type " + holder.getClass().toString());
    }

    private ReactiveCapabilityCurveEq() {
    }
}
