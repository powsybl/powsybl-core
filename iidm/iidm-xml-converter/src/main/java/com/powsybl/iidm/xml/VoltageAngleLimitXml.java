/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageAngleLimit;
import com.powsybl.iidm.network.VoltageAngleLimit.FlowDirection;
import com.powsybl.iidm.network.VoltageAngleLimitAdder;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public final class VoltageAngleLimitXml {

    private static final String VOLTAGE_ANGLE_LIMIT = "voltageAngleLimit";
    private static final String LIMIT = "limit";
    private static final String FLOW_DIRECTION = "flowDirection";
    private static final String FROM = "from";
    private static final String TO = "to";

    public static void write(VoltageAngleLimit voltageAngleLimit, NetworkXmlWriterContext context) {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_11, context, () -> {

            context.getWriter().writeStartElement(context.getVersion().getNamespaceURI(context.isValid()), VOLTAGE_ANGLE_LIMIT);
            XmlUtil.writeDouble(LIMIT, voltageAngleLimit.getLimit(), context.getWriter());
            context.getWriter().writeAttribute(FLOW_DIRECTION, voltageAngleLimit.getFlowDirection().name());

            TerminalRefXml.writeTerminalRef(voltageAngleLimit.getTerminalFrom(), context, FROM);
            TerminalRefXml.writeTerminalRef(voltageAngleLimit.getTerminalTo(), context, TO);

            context.getWriter().writeEndElement();
        });
    }

    public static void read(Network network, NetworkXmlReaderContext context) {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_11, context, () -> {

            double limit = XmlUtil.readDoubleAttribute(context.getReader(), LIMIT);
            String flowDirectionString = context.getReader().getAttributeValue(null, FLOW_DIRECTION);

            VoltageAngleLimitAdder adder = network.newVoltageAngleLimit();
            adder
                .withLimit(limit)
                .withFlowDirection(FlowDirection.valueOf(flowDirectionString));

            XmlUtil.readUntilEndElement(VOLTAGE_ANGLE_LIMIT, context.getReader(), () -> {
                if (context.getReader().getLocalName().equals(FROM)) {
                    adder.from(TerminalRefXml.readTerminalRef(context));
                } else if (context.getReader().getLocalName().equals(TO)) {
                    adder.to(TerminalRefXml.readTerminalRef(context));
                }
            });

            adder.add();
        });
    }

    private VoltageAngleLimitXml() {
    }
}
