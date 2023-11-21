/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageAngleLimit;
import com.powsybl.iidm.network.VoltageAngleLimitAdder;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class VoltageAngleLimitSerDe {

    static final String ROOT_ELEMENT_NAME = "voltageAngleLimit";
    static final String ARRAY_ELEMENT_NAME = "voltageAngleLimits";
    private static final String ID = "id";
    private static final String LOW_LIMIT = "lowLimit";
    private static final String HIGH_LIMIT = "highLimit";
    private static final String FROM = "from";
    private static final String TO = "to";

    public static void write(VoltageAngleLimit voltageAngleLimit, NetworkSerializerContext context) {
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_11, context, () -> {
            context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), ROOT_ELEMENT_NAME);
            context.getWriter().writeStringAttribute(ID, context.getAnonymizer().anonymizeString(voltageAngleLimit.getId()));
            voltageAngleLimit.getLowLimit().ifPresent(low -> context.getWriter().writeDoubleAttribute(LOW_LIMIT, low));
            voltageAngleLimit.getHighLimit().ifPresent(high -> context.getWriter().writeDoubleAttribute(HIGH_LIMIT, high));
            TerminalRefSerDe.writeTerminalRef(voltageAngleLimit.getTerminalFrom(), context, FROM);
            TerminalRefSerDe.writeTerminalRef(voltageAngleLimit.getTerminalTo(), context, TO);
            context.getWriter().writeEndNode();
        });
    }

    public static void read(Network network, NetworkDeserializerContext context) {
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_11, context, () -> {

            String id = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute(ID));
            double lowLimit = context.getReader().readDoubleAttribute(LOW_LIMIT);
            double highLimit = context.getReader().readDoubleAttribute(HIGH_LIMIT);

            VoltageAngleLimitAdder adder = network.newVoltageAngleLimit();
            adder.setId(id);
            if (!Double.isNaN(lowLimit)) {
                adder.setLowLimit(lowLimit);
            }
            if (!Double.isNaN(highLimit)) {
                adder.setHighLimit(highLimit);
            }
            context.getReader().readChildNodes(elementName -> {
                Terminal terminal = TerminalRefSerDe.readTerminal(context, network);
                switch (elementName) {
                    case FROM -> adder.from(terminal);
                    case TO -> adder.to(terminal);
                    default -> throw new PowsyblException("Unknown element name '" + elementName + "' in 'voltageAngleLimit'");
                }
            });

            adder.add();
        });
    }

    private VoltageAngleLimitSerDe() {
    }
}
