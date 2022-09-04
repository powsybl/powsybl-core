/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.BatteryAdder;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
class BatteryXml extends AbstractConnectableXml<Battery, BatteryAdder, VoltageLevel> {

    static final BatteryXml INSTANCE = new BatteryXml();

    static final String ROOT_ELEMENT_NAME = "battery";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(Battery b) {
        return true;
    }

    @Override
    protected void writeRootElementAttributes(Battery b, VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeDoubleAttribute(IidmXmlUtil.getAttributeName("p0", "targetP", context.getVersion(), IidmXmlVersion.V_1_8),
                b.getTargetP());
        context.getWriter().writeDoubleAttribute(IidmXmlUtil.getAttributeName("q0", "targetQ", context.getVersion(), IidmXmlVersion.V_1_8),
                b.getTargetQ());
        context.getWriter().writeDoubleAttribute("minP", b.getMinP());
        context.getWriter().writeDoubleAttribute("maxP", b.getMaxP());
        writeNodeOrBus(null, b.getTerminal(), context);
        writePQ(null, b.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(Battery b, VoltageLevel vl, NetworkXmlWriterContext context) {
        ReactiveLimitsXml.INSTANCE.write(b, context);
    }

    @Override
    protected BatteryAdder createAdder(VoltageLevel vl) {
        return vl.newBattery();
    }

    @Override
    protected Battery readRootElementAttributes(BatteryAdder adder, NetworkXmlReaderContext context) {
        double targetP = XmlUtil.readOptionalDoubleAttribute(context.getReader(),
                IidmXmlUtil.getAttributeName("p0", "targetP", context.getVersion(), IidmXmlVersion.V_1_8));
        double targetQ = XmlUtil.readOptionalDoubleAttribute(context.getReader(),
                IidmXmlUtil.getAttributeName("q0", "targetQ", context.getVersion(), IidmXmlVersion.V_1_8));
        double minP = XmlUtil.readDoubleAttribute(context.getReader(), "minP");
        double maxP = XmlUtil.readDoubleAttribute(context.getReader(), "maxP");
        readNodeOrBus(adder, context);
        Battery b = adder.setTargetP(targetP)
                .setTargetQ(targetQ)
                .setMinP(minP)
                .setMaxP(maxP)
                .add();
        readPQ(null, b.getTerminal(), context.getReader());
        return b;
    }

    @Override
    protected void readSubElements(Battery b, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "reactiveCapabilityCurve":
                case "minMaxReactiveLimits":
                    ReactiveLimitsXml.INSTANCE.read(b, context);
                    break;

                default:
                    super.readSubElements(b, context);
            }
        });
    }
}
