/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.function.Consumer;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
abstract class AbstractAcDcConverterSerDe<T extends AcDcConverter<T>, A extends AcDcConverterAdder<T, A>> extends AbstractComplexIdentifiableSerDe<T, A, VoltageLevel> {

    protected void readRootElementPqiAttributes(List<Consumer<T>> toApply, A adder, NetworkDeserializerContext context) {
        double p1 = context.getReader().readDoubleAttribute("p1");
        double q1 = context.getReader().readDoubleAttribute("q1");
        toApply.add(converter -> converter.getTerminal1().setP(p1).setQ(q1));

        if (adder.withTerminal2()) {
            double p2 = context.getReader().readDoubleAttribute("p2");
            double q2 = context.getReader().readDoubleAttribute("q2");
            toApply.add(converter -> converter.getTerminal2().ifPresent(terminal -> terminal.setP(p2).setQ(q2)));
        }

        double dcP1 = context.getReader().readDoubleAttribute("dcP1");
        double dcI1 = context.getReader().readDoubleAttribute("dcI1");
        double dcP2 = context.getReader().readDoubleAttribute("dcP2");
        double dcI2 = context.getReader().readDoubleAttribute("dcI2");
        toApply.add(converter -> {
            converter.getDcTerminal1().setP(dcP1).setI(dcI1);
            converter.getDcTerminal2().setP(dcP2).setI(dcI2);
        });
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
        writePI(converter.getDcTerminal1(), context.getWriter());
        writePI(converter.getDcTerminal2(), context.getWriter());
    }

    @Override
    protected void writeSubElements(T converter, VoltageLevel vl, NetworkSerializerContext context) {
        TerminalRefSerDe.writeTerminalRef(converter.getPccTerminal(), context, "pccTerminal");
        DroopCurveSerDe.INSTANCE.write(converter, context);
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
    protected void readSubElement(String elementName, String id, List<Consumer<T>> toApply, NetworkDeserializerContext context) {
        if ("pccTerminal".equals(elementName)) {
            TerminalRefSerDe.TerminalData terminalData = TerminalRefSerDe.readTerminalData(context);
            toApply.add(converter -> context.addEndTask(DeserializationEndTask.Step.AFTER_EXTENSIONS, () -> {
                Terminal terminal = TerminalRefSerDe.resolve(terminalData.id(), terminalData.side(), terminalData.number(), converter.getNetwork());
                converter.setPccTerminal(terminal);
            }));
        } else if (DroopCurveSerDe.ELEM_DROOP_CURVE.equals(elementName)) {
            DroopCurveSerDe.INSTANCE.read(toApply, context);
        } else {
            super.readSubElement(elementName, id, toApply, context);
        }
    }

    @Override
    protected void readSubElements(String id, A adder, List<Consumer<T>> toApply, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> readSubElement(elementName, id, toApply, context));
    }
}
