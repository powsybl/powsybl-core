/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.xml;

import javax.xml.stream.XMLStreamException;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class VoltageAngleLimitXml {

    private static final String VOLTAGE_ANGLE_LIMIT = "voltageAngleLimit";
    private static final String ID = "id";
    private static final String LOW_LIMIT = "lowLimit";
    private static final String HIGH_LIMIT = "highLimit";
    private static final String FROM = "from";
    private static final String TO = "to";

    public static void write(VoltageAngleLimit voltageAngleLimit, NetworkXmlWriterContext context) {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_11, context, () -> {

            context.getWriter().writeStartElement(context.getVersion().getNamespaceURI(context.isValid()), VOLTAGE_ANGLE_LIMIT);
            try {
                context.getWriter().writeAttribute(ID, context.getAnonymizer().anonymizeString(voltageAngleLimit.getId()));
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
            voltageAngleLimit.getLowLimit().ifPresent(low -> {
                try {
                    XmlUtil.writeDouble(LOW_LIMIT, low, context.getWriter());
                } catch (XMLStreamException e) {
                    throw new UncheckedXmlStreamException(e);
                }
            });
            voltageAngleLimit.getHighLimit().ifPresent(high -> {
                try {
                    XmlUtil.writeDouble(HIGH_LIMIT, high, context.getWriter());
                } catch (XMLStreamException e) {
                    throw new UncheckedXmlStreamException(e);
                }
            });
            TerminalRefXml.writeTerminalRef(voltageAngleLimit.getTerminalFrom(), context, FROM);
            TerminalRefXml.writeTerminalRef(voltageAngleLimit.getTerminalTo(), context, TO);

            context.getWriter().writeEndElement();
        });
    }

    public static void read(Network network, NetworkXmlReaderContext context) {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_11, context, () -> {

            String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, ID));
            double lowLimit = XmlUtil.readOptionalDoubleAttribute(context.getReader(), LOW_LIMIT);
            double highLimit = XmlUtil.readOptionalDoubleAttribute(context.getReader(), HIGH_LIMIT);

            VoltageAngleLimitAdder adder = network.newVoltageAngleLimit();
            adder.setId(id);
            if (!Double.isNaN(lowLimit)) {
                adder.setLowLimit(lowLimit);
            }
            if (!Double.isNaN(highLimit)) {
                adder.setHighLimit(highLimit);
            }
            XmlUtil.readUntilEndElement(VOLTAGE_ANGLE_LIMIT, context.getReader(), () -> {
                if (context.getReader().getLocalName().equals(FROM)) {
                    Terminal from = TerminalRefXml.readTerminal(context, network);
                    adder.from(from);
                } else if (context.getReader().getLocalName().equals(TO)) {
                    Terminal to = TerminalRefXml.readTerminal(context, network);
                    adder.to(to);
                }
            });

            adder.add();
        });
    }

    private VoltageAngleLimitXml() {
    }
}
