/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TerminalRef;
import com.powsybl.iidm.network.VoltageAngleLimit;
import com.powsybl.iidm.network.VoltageAngleLimitAdder;
import com.powsybl.iidm.network.VoltageAngleLimit.FlowDirection;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public final class VoltageAngleLimitXml {

    static final String VOLTAGE_ANGLE_LIMIT = "voltageAngleLimit";

    static final String ID = "id";
    static final String SIDE = "side";
    static final String LIMIT = "limit";
    static final String FLOW_DIRECTION = "flowDirection";
    static final String FROM = "from";
    static final String TO = "to";

    public static void write(VoltageAngleLimit voltageAngleLimit, NetworkXmlWriterContext context) {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_10, context, () -> {

            context.getWriter().writeStartElement(context.getVersion().getNamespaceURI(context.isValid()), VOLTAGE_ANGLE_LIMIT);
            XmlUtil.writeDouble(LIMIT, voltageAngleLimit.getLimit(), context.getWriter());
            context.getWriter().writeAttribute(FLOW_DIRECTION, voltageAngleLimit.getFlowDirection().name());

            writeTerminalRef(voltageAngleLimit.getTerminalFrom().getConnectable().getId(), voltageAngleLimit.getConnectableSide(voltageAngleLimit.getTerminalFrom()), context, FROM);
            writeTerminalRef(voltageAngleLimit.getTerminalTo().getConnectable().getId(), voltageAngleLimit.getConnectableSide(voltageAngleLimit.getTerminalTo()), context, TO);

            context.getWriter().writeEndElement();
        });
    }

    public static void read(Network network, NetworkXmlReaderContext context) {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_10, context, () -> {

            double limit = XmlUtil.readDoubleAttribute(context.getReader(), LIMIT);
            String flowDirectionString = context.getReader().getAttributeValue(null, FLOW_DIRECTION);

            VoltageAngleLimitAdder adder = network.newVoltageAngleLimit();
            adder
                .withLimit(limit)
                .withFlowDirection(FlowDirection.valueOf(flowDirectionString));

            XmlUtil.readUntilEndElement(VOLTAGE_ANGLE_LIMIT, context.getReader(), () -> {
                TerminalRef from = null;
                TerminalRef to = null;
                if (context.getReader().getLocalName().equals(FROM)) {
                    from = readTerminalRef(context);
                    adder.from(from);
                } else if (context.getReader().getLocalName().equals(TO)) {
                    to = readTerminalRef(context);
                    adder.to(to);
                } else {
                    throw new PowsyblException("Unexpected TerminalRef: " + context.getReader().getLocalName());
                }
            });

            adder.add();
        });
    }

    private static void writeTerminalRef(String id, TerminalRef.Side side, NetworkXmlWriterContext context, String ft) throws XMLStreamException {
        context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(context.isValid()), ft);
        context.getWriter().writeAttribute(ID, id);
        context.getWriter().writeAttribute(SIDE, side.name());
    }

    private static TerminalRef readTerminalRef(NetworkXmlReaderContext context) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
        String side = context.getReader().getAttributeValue(null, "side");
        if (side == null) {
            return TerminalRef.create(id);
        } else {
            return TerminalRef.create(id, TerminalRef.Side.valueOf(side));
        }
    }

    private VoltageAngleLimitXml() {
    }
}
