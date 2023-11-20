/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import static com.powsybl.iidm.xml.ConnectableXmlUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class StaticVarCompensatorXml extends AbstractSimpleIdentifiableXml<StaticVarCompensator, StaticVarCompensatorAdder, VoltageLevel> {

    static final StaticVarCompensatorXml INSTANCE = new StaticVarCompensatorXml();

    static final String ROOT_ELEMENT_NAME = "staticVarCompensator";
    static final String ARRAY_ELEMENT_NAME = "staticVarCompensators";

    private static final String REGULATING_TERMINAL = "regulatingTerminal";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(StaticVarCompensator svc, VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeDoubleAttribute("bMin", svc.getBmin());
        context.getWriter().writeDoubleAttribute("bMax", svc.getBmax());
        String[] voltageSetpointName = {"voltageSetpoint"};
        String[] reactivePowerSetpointName = {"reactivePowerSetpoint"};
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_2, context, () -> {
            voltageSetpointName[0] = "voltageSetPoint";
            reactivePowerSetpointName[0] = "reactivePowerSetPoint";
        });
        context.getWriter().writeDoubleAttribute(voltageSetpointName[0], svc.getVoltageSetpoint());
        context.getWriter().writeDoubleAttribute(reactivePowerSetpointName[0], svc.getReactivePowerSetpoint());

        if (svc.getRegulationMode() != null) {
            context.getWriter().writeStringAttribute("regulationMode", svc.getRegulationMode().name());
        }
        writeNodeOrBus(null, svc.getTerminal(), context);
        writePQ(null, svc.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(StaticVarCompensator svc, VoltageLevel vl, NetworkXmlWriterContext context) {
        IidmXmlUtil.assertMinimumVersionAndRunIfNotDefault(svc != svc.getRegulatingTerminal().getConnectable(),
                ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED,
                IidmXmlVersion.V_1_1, context, () -> TerminalRefXml.writeTerminalRef(svc.getRegulatingTerminal(), context, REGULATING_TERMINAL));
    }

    @Override
    protected StaticVarCompensatorAdder createAdder(VoltageLevel vl) {
        return vl.newStaticVarCompensator();
    }

    @Override
    protected StaticVarCompensator readRootElementAttributes(StaticVarCompensatorAdder adder, VoltageLevel voltageLevel, NetworkXmlReaderContext context) {
        double bMin = context.getReader().readDoubleAttribute("bMin");
        double bMax = context.getReader().readDoubleAttribute("bMax");

        String[] voltageSetpointName = {"voltageSetpoint"};
        String[] reactivePowerSetpointName = {"reactivePowerSetpoint"};
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_2, context, () -> {
            voltageSetpointName[0] = "voltageSetPoint";
            reactivePowerSetpointName[0] = "reactivePowerSetPoint";
        });
        double voltageSetpoint = context.getReader().readDoubleAttribute(voltageSetpointName[0]);
        double reactivePowerSetpoint = context.getReader().readDoubleAttribute(reactivePowerSetpointName[0]);

        StaticVarCompensator.RegulationMode regulationMode = context.getReader().readEnumAttribute("regulationMode", StaticVarCompensator.RegulationMode.class);
        adder.setBmin(bMin)
                .setBmax(bMax)
                .setVoltageSetpoint(voltageSetpoint)
                .setReactivePowerSetpoint(reactivePowerSetpoint)
                .setRegulationMode(regulationMode);
        readNodeOrBus(adder, context);
        StaticVarCompensator svc = adder.add();
        readPQ(null, svc.getTerminal(), context.getReader());
        return svc;
    }

    @Override
    protected void readSubElements(StaticVarCompensator svc, NetworkXmlReaderContext context) {
        context.getReader().readChildNodes(elementName -> {
            if (elementName.equals(REGULATING_TERMINAL)) {
                IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
                TerminalRefXml.readTerminalRef(context, svc.getNetwork(), svc::setRegulatingTerminal);
            } else {
                readSubElement(elementName, svc, context);
            }
        });
    }
}
