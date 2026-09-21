/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.AcDcConverter;
import com.powsybl.iidm.network.AcDcConverterAdder;
import com.powsybl.iidm.network.DcTerminal;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
abstract class AbstractAcDcConverterSerDe<T extends AcDcConverter<T>, A extends AcDcConverterAdder<T, A>> extends AbstractComplexIdentifiableSerDe<T, A, VoltageLevel> {

    private static final String ATTR_CONTROL_MODE = "controlMode";
    static final String PCC_TERMINAL = "pccTerminal";

    /**
     * Serialized shape of {@link AcDcConverter.ControlMode} for IIDM versions up to V1_17, where the droop
     * control mode was still named <code>P_PCC_DROOP</code>.
     */
    private enum ControlModeSerDe {
        P_PCC,
        V_DC,
        P_PCC_DROOP;

        static ControlModeSerDe from(AcDcConverter.ControlMode controlMode) {
            return switch (controlMode) {
                case P_PCC -> P_PCC;
                case V_DC -> V_DC;
                case DC_DROOP -> P_PCC_DROOP;
            };
        }

        AcDcConverter.ControlMode toControlMode() {
            return switch (this) {
                case P_PCC -> AcDcConverter.ControlMode.P_PCC;
                case V_DC -> AcDcConverter.ControlMode.V_DC;
                case P_PCC_DROOP -> AcDcConverter.ControlMode.DC_DROOP;
            };
        }
    }

    protected void readRootElementPqiAttributes(List<Consumer<T>> toApply, A adder, NetworkDeserializerContext context) {
        double p1 = context.getReader().readDoubleAttribute("p1");
        double q1 = context.getReader().readDoubleAttribute("q1");
        toApply.add(converter -> converter.getTerminal1().setP(p1).setQ(q1));

        if (adder.hasTwoAcTerminals()) {
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
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_17, context, () ->
            context.getWriter().writeEnumAttribute(ATTR_CONTROL_MODE, ControlModeSerDe.from(converter.getControlMode())));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_18, context, () ->
            context.getWriter().writeEnumAttribute(ATTR_CONTROL_MODE, converter.getControlMode()));
        context.getWriter().writeDoubleAttribute("targetP", converter.getTargetP());
        context.getWriter().writeDoubleAttribute("targetVdc", converter.getTargetVdc());
        if (converter.getMinP() != -Double.MAX_VALUE && !context.getOptions().isForceExportNetworkWithBetaFeatures()) {
            throw new NotImplementedException(getRootElementName() + " '" + converter.getId() + "': minP serialization is not yet supported. " +
                "To force the export of the network and ignore this value, either use the config parameter iidm.export.xml.force-export-network-with-beta-features, " +
                "or ExportOptions.setForceExportNetworkWithBetaFeatures");
        }
        if (converter.getMaxP() != Double.MAX_VALUE && !context.getOptions().isForceExportNetworkWithBetaFeatures()) {
            throw new NotImplementedException(getRootElementName() + " '" + converter.getId() + "': maxP serialization is not yet supported. " +
                "To force the export of the network and ignore this value, either use the config parameter iidm.export.xml.force-export-network-with-beta-features, " +
                "or ExportOptions.setForceExportNetworkWithBetaFeatures");
        }

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
        TerminalRefSerDe.writeTerminalRef(converter.getPccTerminal(), context, PCC_TERMINAL);
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
        AtomicReference<AcDcConverter.ControlMode> controlMode = new AtomicReference<>();
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_17, context, () ->
            controlMode.set(context.getReader().readEnumAttribute(ATTR_CONTROL_MODE, ControlModeSerDe.class).toControlMode()));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_18, context, () ->
            controlMode.set(context.getReader().readEnumAttribute(ATTR_CONTROL_MODE, AcDcConverter.ControlMode.class)));
        double targetP = context.getReader().readDoubleAttribute("targetP");
        double targetVdc = context.getReader().readDoubleAttribute("targetVdc");
        adder
            .setDcNode1(dcNode1Id)
            .setDcConnected1(dcConnected1)
            .setDcNode2(dcNode2Id)
            .setDcConnected2(dcConnected2)
            .setControlMode(controlMode.get())
            .setTargetP(targetP)
            .setTargetVdc(targetVdc)
            .setIdleLoss(idleLoss)
            .setSwitchingLoss(switchingLoss)
            .setResistiveLoss(resistiveLoss);
        readNodeOrBus(adder, voltageLevel.getTopologyKind(), context);
    }

    @Override
    protected void readSubElement(String elementName, String id, List<Consumer<T>> toApply, NetworkDeserializerContext context) {
        if (PCC_TERMINAL.equals(elementName)) {
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
