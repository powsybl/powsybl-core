/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
abstract class AbstractAcDcConverterSerDe<T extends AcDcConverter<T>, A extends AcDcConverterAdder<T, A>> extends AbstractSimpleIdentifiableSerDe<T, A, VoltageLevel> {

    protected void readRootElementPqiAttributes(T converter, NetworkDeserializerContext context) {
        readPQ(1, converter.getTerminal1(), context.getReader());
        converter.getTerminal2().ifPresent(terminal2 -> readPQ(2, terminal2, context.getReader()));
        DcTerminal dcTerminal1 = converter.getDcTerminal1();
        DcTerminal dcTerminal2 = converter.getDcTerminal2();
        double dcP1 = context.getReader().readDoubleAttribute("dcP1");
        double dcI1 = context.getReader().readDoubleAttribute("dcI1");
        double dcP2 = context.getReader().readDoubleAttribute("dcP2");
        double dcI2 = context.getReader().readDoubleAttribute("dcI2");
        dcTerminal1.setP(dcP1).setI(dcI1);
        dcTerminal2.setP(dcP2).setI(dcI2);
    }

    @Override
    protected void writeRootElementAttributes(final T converter, final VoltageLevel parent, final NetworkSerializerContext context) {
        DcTerminal dcTerminal1 = converter.getDcTerminal1();
        DcTerminal dcTerminal2 = converter.getDcTerminal2();
        context.getWriter().writeStringAttribute("dcNode1", dcTerminal1.getDcNode().getId());
        context.getWriter().writeBooleanAttribute("dcConnected1", dcTerminal1.isConnected());
        context.getWriter().writeStringAttribute("dcNode2", dcTerminal2.getDcNode().getId());
        context.getWriter().writeBooleanAttribute("dcConnected2", dcTerminal2.isConnected());
        context.getWriter().writeDoubleAttribute("idleLoss", converter.getIdleLoss());
        context.getWriter().writeDoubleAttribute("switchingLoss", converter.getSwitchingLoss());
        context.getWriter().writeDoubleAttribute("resistiveLoss", converter.getResistiveLoss());
        context.getWriter().writeEnumAttribute("controlMode", converter.getControlMode());
        context.getWriter().writeDoubleAttribute("targetP", converter.getTargetP());
        context.getWriter().writeDoubleAttribute("targetVdc", converter.getTargetVdc());

        writeNodeOrBus(converter, context);
    }

    protected void writeRootElementPqiAttributes(final T converter, final NetworkSerializerContext context) {
        writePQ(1, converter.getTerminal1(), context.getWriter());
        converter.getTerminal2().ifPresent(terminal2 -> writePQ(2, terminal2, context.getWriter()));
        DcTerminal dcTerminal1 = converter.getDcTerminal1();
        DcTerminal dcTerminal2 = converter.getDcTerminal2();
        context.getWriter().writeDoubleAttribute("dcP1", dcTerminal1.getP());
        context.getWriter().writeDoubleAttribute("dcI1", dcTerminal1.getI());
        context.getWriter().writeDoubleAttribute("dcP2", dcTerminal2.getP());
        context.getWriter().writeDoubleAttribute("dcI2", dcTerminal2.getI());
    }

    @Override
    protected void writeSubElements(T converter, VoltageLevel vl, NetworkSerializerContext context) {
        TerminalRefSerDe.writeTerminalRef(converter.getPccTerminal(), context, "pccTerminal");
        super.writeSubElements(converter, vl, context);
    }

    protected void readRootElementCommonAttributes(final A adder, final VoltageLevel voltageLevel, final NetworkDeserializerContext context) {
        String dcNode1Id = context.getReader().readStringAttribute("dcNode1");
        boolean dcConnected1 = context.getReader().readBooleanAttribute("dcConnected1");
        String dcNode2Id = context.getReader().readStringAttribute("dcNode2");
        boolean dcConnected2 = context.getReader().readBooleanAttribute("dcConnected2");
        double idleLoss = context.getReader().readDoubleAttribute("idleLoss");
        double switchingLoss = context.getReader().readDoubleAttribute("switchingLoss");
        double resistiveLoss = context.getReader().readDoubleAttribute("resistiveLoss");
        AcDcConverter.ControlMode controlMode = context.getReader().readEnumAttribute("controlMode", AcDcConverter.ControlMode.class);
        double targetP = context.getReader().readDoubleAttribute("targetP");
        double targetVdc = context.getReader().readDoubleAttribute("targetVdc");
        adder
            .setDcNode1(dcNode1Id)
            .setDcConnected1(dcConnected1)
            .setDcNode2(dcNode2Id)
            .setDcConnected2(dcConnected2)
            .setControlMode(controlMode)
            .setTargetP(targetP)
            .setTargetVdc(targetVdc)
            .setIdleLoss(idleLoss)
            .setSwitchingLoss(switchingLoss)
            .setResistiveLoss(resistiveLoss);
        readNodeOrBus(adder, voltageLevel.getTopologyKind(), context);
    }

    @Override
    protected void readSubElement(String elementName, T converter, NetworkDeserializerContext context) {
        if ("pccTerminal".equals(elementName)) {
            TerminalRefSerDe.readTerminalRef(context, converter.getNetwork(), converter::setPccTerminal);
        } else {
            super.readSubElement(elementName, converter, context);
        }
    }

    @Override
    protected void readSubElements(final T converter, final NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> readSubElement(elementName, converter, context));
    }
}
