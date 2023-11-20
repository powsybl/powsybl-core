/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusAdder;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class BusXml extends AbstractSimpleIdentifiableXml<Bus, BusAdder, VoltageLevel> {

    static final BusXml INSTANCE = new BusXml();

    static final String ROOT_ELEMENT_NAME = "bus";
    static final String ARRAY_ELEMENT_NAME = "buses";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(Bus b, VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeDoubleAttribute("v", b.getV());
        context.getWriter().writeDoubleAttribute("angle", b.getAngle());
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_8, context, () -> {
            context.getWriter().writeDoubleAttribute("fictitiousP0", b.getFictitiousP0(), 0.0);
            context.getWriter().writeDoubleAttribute("fictitiousQ0", b.getFictitiousQ0(), 0.0);
        });
    }

    @Override
    protected void writeSubElements(Bus b, VoltageLevel vl, NetworkXmlWriterContext context) {
        // no sub elements to write
    }

    @Override
    protected BusAdder createAdder(VoltageLevel vl) {
        return vl.getBusBreakerView().newBus();
    }

    @Override
    protected Bus readRootElementAttributes(BusAdder adder, VoltageLevel voltageLevel, NetworkXmlReaderContext context) {
        double v = context.getReader().readDoubleAttribute("v");
        double angle = context.getReader().readDoubleAttribute("angle");
        Bus b = adder.add();
        b.setV(v);
        b.setAngle(angle);
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_8, context, () -> {
            double p0 = context.getReader().readDoubleAttribute("fictitiousP0");
            double q0 = context.getReader().readDoubleAttribute("fictitiousQ0");
            if (!Double.isNaN(p0)) {
                b.setFictitiousP0(p0);
            }
            if (!Double.isNaN(q0)) {
                b.setFictitiousQ0(q0);
            }
        });
        return b;
    }

    @Override
    protected void readSubElements(Bus b, NetworkXmlReaderContext context) {
        context.getReader().readChildNodes(elementName -> readSubElement(elementName, b, context));
    }
}
