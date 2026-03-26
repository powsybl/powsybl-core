/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.AcDcConverter;
import com.powsybl.iidm.network.DroopCurve;
import com.powsybl.iidm.network.DroopCurveAdder;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DroopCurveSerDe {

    static final DroopCurveSerDe INSTANCE = new DroopCurveSerDe();

    static final String ELEM_DROOP_CURVE = "droopCurve";
    private static final String ATTR_MIN_V = "minV";
    private static final String ATTR_MAX_V = "maxV";
    public static final String ATTR_K = "k";
    public static final String ARRAY_ELEMENT_NAME = "segments";
    public static final String ROOT_ELEMENT_NAME = "segment";

    public void write(AcDcConverter<?> converter, NetworkSerializerContext context) {
        DroopCurve curve = converter.getDroopCurve();
        // iidm-impl ensures that curve is never null, but other IIDM implementations may decide otherwise
        if (curve == null || curve.getSegments().isEmpty()) {
            return;
        }
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), ELEM_DROOP_CURVE);
        context.getWriter().writeStartNodes();
        for (DroopCurve.Segment segment : curve.getSegments()) {
            context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), ROOT_ELEMENT_NAME);
            context.getWriter().writeDoubleAttribute(ATTR_MIN_V, segment.getMinV());
            context.getWriter().writeDoubleAttribute(ATTR_MAX_V, segment.getMaxV());
            context.getWriter().writeDoubleAttribute(ATTR_K, segment.getK());
            context.getWriter().writeEndNode();
        }
        context.getWriter().writeEndNodes();
        context.getWriter().writeEndNode();
    }

    public void read(AcDcConverter<?> converter, NetworkDeserializerContext context) {
        DroopCurveAdder curveAdder = converter.newDroopCurve();
        context.getReader().readChildNodes(elementName -> {
            if (elementName.equals(ROOT_ELEMENT_NAME)) {
                double minV = context.getReader().readDoubleAttribute(ATTR_MIN_V);
                double maxV = context.getReader().readDoubleAttribute(ATTR_MAX_V);
                double k = context.getReader().readDoubleAttribute(ATTR_K);
                context.getReader().readEndNode();
                curveAdder.beginSegment()
                        .setMinV(minV)
                        .setMaxV(maxV)
                        .setK(k)
                        .endSegment();
            } else {
                throw new PowsyblException("Unknown element name '" + elementName + "' in 'droopCurve'");
            }
        });
        curveAdder.add();
    }
}
