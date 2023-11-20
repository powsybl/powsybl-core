/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.BatteryAdder;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.serializer.util.IidmXmlUtil;

import static com.powsybl.iidm.serializer.ConnectableXmlUtil.*;
import static com.powsybl.iidm.serializer.ReactiveLimitsXml.ELEM_MIN_MAX_REACTIVE_LIMITS;
import static com.powsybl.iidm.serializer.ReactiveLimitsXml.ELEM_REACTIVE_CAPABILITY_CURVE;

/**
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
class BatteryXml extends AbstractSimpleIdentifiableXml<Battery, BatteryAdder, VoltageLevel> {

    static final BatteryXml INSTANCE = new BatteryXml();

    static final String ROOT_ELEMENT_NAME = "battery";
    static final String ARRAY_ELEMENT_NAME = "batteries";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
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
    protected Battery readRootElementAttributes(BatteryAdder adder, VoltageLevel voltageLevel, NetworkXmlReaderContext context) {
        double targetP = context.getReader().readDoubleAttribute(
                IidmXmlUtil.getAttributeName("p0", "targetP", context.getVersion(), IidmXmlVersion.V_1_8));
        double targetQ = context.getReader().readDoubleAttribute(
                IidmXmlUtil.getAttributeName("q0", "targetQ", context.getVersion(), IidmXmlVersion.V_1_8));
        double minP = context.getReader().readDoubleAttribute("minP");
        double maxP = context.getReader().readDoubleAttribute("maxP");
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
    protected void readSubElements(Battery b, NetworkXmlReaderContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case ELEM_REACTIVE_CAPABILITY_CURVE -> ReactiveLimitsXml.INSTANCE.readReactiveCapabilityCurve(b, context);
                case ELEM_MIN_MAX_REACTIVE_LIMITS -> ReactiveLimitsXml.INSTANCE.readMinMaxReactiveLimits(b, context);
                default -> readSubElement(elementName, b, context);
            }
        });
    }
}
