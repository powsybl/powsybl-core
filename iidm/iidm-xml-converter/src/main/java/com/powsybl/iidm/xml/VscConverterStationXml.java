/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.VscConverterStationAdder;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class VscConverterStationXml extends AbstractConnectableXml<VscConverterStation, VscConverterStationAdder, VoltageLevel> {

    static final VscConverterStationXml INSTANCE = new VscConverterStationXml();

    static final String ROOT_ELEMENT_NAME = "vscConverterStation";

    private static final String REGULATING_TERMINAL = "regulatingTerminal";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(VscConverterStation cs, VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeBooleanAttribute("voltageRegulatorOn", cs.isVoltageRegulatorOn());
        context.getWriter().writeFloatAttribute("lossFactor", cs.getLossFactor());
        context.getWriter().writeDoubleAttribute("voltageSetpoint", cs.getVoltageSetpoint());
        context.getWriter().writeDoubleAttribute("reactivePowerSetpoint", cs.getReactivePowerSetpoint());
        writeNodeOrBus(null, cs.getTerminal(), context);
        writePQ(null, cs.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(VscConverterStation cs, VoltageLevel vl, NetworkXmlWriterContext context) {
        ReactiveLimitsXml.INSTANCE.write(cs, context);
        IidmXmlUtil.assertMinimumVersionAndRunIfNotDefault(!Objects.equals(cs, cs.getRegulatingTerminal().getConnectable()),
                ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED,
                IidmXmlVersion.V_1_6, context, () -> TerminalRefXml.writeTerminalRef(cs.getRegulatingTerminal(), context, REGULATING_TERMINAL));
    }

    @Override
    protected VscConverterStationAdder createAdder(VoltageLevel vl) {
        return vl.newVscConverterStation();
    }

    @Override
    protected VscConverterStation readRootElementAttributes(VscConverterStationAdder adder, NetworkXmlReaderContext context) {
        boolean voltageRegulatorOn = context.getReader().readBooleanAttribute("voltageRegulatorOn");
        float lossFactor = context.getReader().readFloatAttribute("lossFactor");
        double voltageSetpoint = context.getReader().readDoubleAttribute("voltageSetpoint");
        double reactivePowerSetpoint = context.getReader().readDoubleAttribute("reactivePowerSetpoint");
        readNodeOrBus(adder, context);
        adder
                .setLossFactor(lossFactor)
                .setVoltageSetpoint(voltageSetpoint)
                .setReactivePowerSetpoint(reactivePowerSetpoint)
                .setVoltageRegulatorOn(voltageRegulatorOn);
        VscConverterStation cs = adder.add();
        readPQ(null, cs.getTerminal(), context.getReader());
        return cs;
    }

    @Override
    protected void readSubElements(VscConverterStation cs, NetworkXmlReaderContext context) {
        context.getReader().readUntilEndNode(getRootElementName(), () -> {
            switch (context.getReader().getNodeName()) {
                case "reactiveCapabilityCurve":
                case "minMaxReactiveLimits":
                    ReactiveLimitsXml.INSTANCE.read(cs, context);
                    break;
                case REGULATING_TERMINAL:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_6, context);
                    String id = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("id"));
                    String side = context.getReader().readStringAttribute("side");
                    context.getEndTasks().add(() -> cs.setRegulatingTerminal(TerminalRefXml
                            .readTerminalRef(cs.getNetwork(), id, side)));
                    break;
                default:
                    super.readSubElements(cs, context);
            }
        });
    }
}
