/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class StaticVarCompensatorSerDe extends AbstractSimpleIdentifiableSerDe<StaticVarCompensator, StaticVarCompensatorAdder, VoltageLevel> {

    static final StaticVarCompensatorSerDe INSTANCE = new StaticVarCompensatorSerDe();

    static final String ROOT_ELEMENT_NAME = "staticVarCompensator";
    static final String ARRAY_ELEMENT_NAME = "staticVarCompensators";

    private static final String REGULATING_TERMINAL = "regulatingTerminal";
    private static final String REGULATION_MODE = "regulationMode";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(StaticVarCompensator svc, VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeDoubleAttribute("bMin", svc.getBmin());
        context.getWriter().writeDoubleAttribute("bMax", svc.getBmax());
        String[] voltageSetpointName = {"voltageSetpoint"};
        String[] reactivePowerSetpointName = {"reactivePowerSetpoint"};
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_2, context, () -> {
            voltageSetpointName[0] = "voltageSetPoint";
            reactivePowerSetpointName[0] = "reactivePowerSetPoint";
        });
        context.getWriter().writeDoubleAttribute(voltageSetpointName[0], svc.getVoltageSetpoint());
        context.getWriter().writeDoubleAttribute(reactivePowerSetpointName[0], svc.getReactivePowerSetpoint());

        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_13, context, () -> {
            if (svc.isRegulating()) {
                context.getWriter().writeEnumAttribute(REGULATION_MODE, svc.getRegulationMode());
            } else {
                context.getWriter().writeEnumAttribute(REGULATION_MODE, null);
            }
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_14, context, () -> {
            context.getWriter().writeEnumAttribute(REGULATION_MODE, svc.getRegulationMode());
            context.getWriter().writeBooleanAttribute("regulating", svc.isRegulating());
        });
        writeNodeOrBus(null, svc.getTerminal(), context);
        writePQ(null, svc.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(StaticVarCompensator svc, VoltageLevel vl, NetworkSerializerContext context) {
        IidmSerDeUtil.assertMinimumVersionAndRunIfNotDefault(svc != svc.getRegulatingTerminal().getConnectable(),
                ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED,
                IidmVersion.V_1_1, context, () -> TerminalRefSerDe.writeTerminalRef(svc.getRegulatingTerminal(), context, REGULATING_TERMINAL));
    }

    @Override
    protected StaticVarCompensatorAdder createAdder(VoltageLevel vl) {
        return vl.newStaticVarCompensator();
    }

    @Override
    protected StaticVarCompensator readRootElementAttributes(StaticVarCompensatorAdder adder, VoltageLevel voltageLevel, NetworkDeserializerContext context) {
        double bMin = context.getReader().readDoubleAttribute("bMin");
        double bMax = context.getReader().readDoubleAttribute("bMax");

        String[] voltageSetpointName = {"voltageSetpoint"};
        String[] reactivePowerSetpointName = {"reactivePowerSetpoint"};
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_2, context, () -> {
            voltageSetpointName[0] = "voltageSetPoint";
            reactivePowerSetpointName[0] = "reactivePowerSetPoint";
        });
        double voltageSetpoint = context.getReader().readDoubleAttribute(voltageSetpointName[0]);
        double reactivePowerSetpoint = context.getReader().readDoubleAttribute(reactivePowerSetpointName[0]);

        StaticVarCompensator.RegulationMode regulationMode = context.getReader().readEnumAttribute(REGULATION_MODE, StaticVarCompensator.RegulationMode.class);
        final boolean[] regulating = new boolean[1];
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_13, context, () -> {
            regulating[0] = regulationMode != null;
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_14, context, () -> {
            regulating[0] = context.getReader().readBooleanAttribute("regulating");
        });

        adder.setBmin(bMin)
                .setBmax(bMax)
                .setVoltageSetpoint(voltageSetpoint)
                .setReactivePowerSetpoint(reactivePowerSetpoint)
                .setRegulationMode(regulationMode)
                .setRegulating(regulating[0]);

        readNodeOrBus(adder, context, voltageLevel.getTopologyKind());
        StaticVarCompensator svc = adder.add();
        readPQ(null, svc.getTerminal(), context.getReader());
        return svc;
    }

    @Override
    protected void readSubElements(StaticVarCompensator svc, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            if (elementName.equals(REGULATING_TERMINAL)) {
                IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
                TerminalRefSerDe.readTerminalRef(context, svc.getNetwork(), svc::setRegulatingTerminal);
            } else {
                readSubElement(elementName, svc, context);
            }
        });
    }
}
