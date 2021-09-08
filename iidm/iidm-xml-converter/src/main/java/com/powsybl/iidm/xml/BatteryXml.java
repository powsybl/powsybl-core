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
    protected void writeRootElementAttributes(Battery b, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("p0", b.getP0(), context.getWriter());
        XmlUtil.writeDouble("q0", b.getQ0(), context.getWriter());
        XmlUtil.writeDouble("minP", b.getMinP(), context.getWriter());
        XmlUtil.writeDouble("maxP", b.getMaxP(), context.getWriter());
        writeNodeOrBus(null, b.getTerminal(), context);
        writePQ(null, b.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(Battery b, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        ReactiveLimitsXml.INSTANCE.write(b, context);
    }

    @Override
    protected BatteryAdder createAdder(VoltageLevel vl) {
        return vl.newBattery();
    }

    @Override
    protected Battery readRootElementAttributes(BatteryAdder adder, NetworkXmlReaderContext context) {
        double p0 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "p0");
        double q0 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "q0");
        double minP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "minP");
        double maxP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "maxP");
        readNodeOrBus(adder, context);
        Battery b = adder.setP0(p0)
                .setQ0(q0)
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
