/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.VscConverterStationAdder;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import java.util.Objects;

import javax.xml.stream.XMLStreamException;

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
    protected boolean hasSubElements(VscConverterStation cs) {
        return true;
    }

    @Override
    protected void writeRootElementAttributes(VscConverterStation cs, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("voltageRegulatorOn", Boolean.toString(cs.isVoltageRegulatorOn()));
        XmlUtil.writeFloat("lossFactor", cs.getLossFactor(), context.getWriter());
        XmlUtil.writeDouble("voltageSetpoint", cs.getVoltageSetpoint(), context.getWriter());
        XmlUtil.writeDouble("reactivePowerSetpoint", cs.getReactivePowerSetpoint(), context.getWriter());
        writeNodeOrBus(null, cs.getTerminal(), context);
        writePQ(null, cs.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(VscConverterStation cs, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        ReactiveLimitsXml.INSTANCE.write(cs, context);

        // Remote regulatingTerminal has been written since 1.6, from 1.8 local regulatingTerminal is also written
        if (cs.getRegulatingTerminal() != null) {
            if (!Objects.equals(cs, cs.getRegulatingTerminal().getConnectable())) {
                IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_6, context);
                IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_6, context, () -> TerminalRefXml.writeTerminalRef(cs.getRegulatingTerminal(), context, REGULATING_TERMINAL));
            } else {
                //IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_8, context, () -> TerminalRefXml.writeTerminalRef(sc.getRegulatingTerminal(), context, REGULATING_TERMINAL));
            }
        }
    }

    @Override
    protected VscConverterStationAdder createAdder(VoltageLevel vl) {
        return vl.newVscConverterStation();
    }

    @Override
    protected VscConverterStation readRootElementAttributes(VscConverterStationAdder adder, NetworkXmlReaderContext context) {
        boolean voltageRegulatorOn = Boolean.parseBoolean(context.getReader().getAttributeValue(null, "voltageRegulatorOn"));
        float lossFactor = XmlUtil.readFloatAttribute(context.getReader(), "lossFactor");
        double voltageSetpoint = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "voltageSetpoint");
        double reactivePowerSetpoint = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "reactivePowerSetpoint");
        readNodeOrBus(adder, context);

        // The regulating terminal is local or remote in enabled controls and null, local or remote in disabled ones.
        // As the regulating terminal is set after the equipment has been created (it is managed as a subElement),
        // in enabled controls the regulating terminal is initially localized and overwritten later by the read one
        // (a null regulating terminal is not allowed when the equipment is created).
        // In disabled controls only the read regulation terminal is set (a null regulating terminal is allowed).
        // Until version 1.8 there is an ambiguity reading disabled controls as only remote regulating terminals are written.
        // Not written regulating terminals could be null or local. Ambiguity is solved by considering them null.

        boolean useLocalRegulation = false;
        if (voltageRegulatorOn) {
            useLocalRegulation = true;
        }

        adder
                .setLossFactor(lossFactor)
                .setVoltageSetpoint(voltageSetpoint)
                .setReactivePowerSetpoint(reactivePowerSetpoint)
                .useLocalRegulation(useLocalRegulation)
                .setVoltageRegulatorOn(voltageRegulatorOn);
        VscConverterStation cs = adder.add();
        readPQ(null, cs.getTerminal(), context.getReader());
        return cs;
    }

    @Override
    protected void readSubElements(VscConverterStation cs, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "reactiveCapabilityCurve":
                case "minMaxReactiveLimits":
                    ReactiveLimitsXml.INSTANCE.read(cs, context);
                    break;
                case REGULATING_TERMINAL:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_6, context);
                    String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
                    String side = context.getReader().getAttributeValue(null, "side");
                    context.getEndTasks().add(() -> cs.setRegulatingTerminal(TerminalRefXml
                            .readTerminalRef(cs.getNetwork(), id, side)));
                    break;
                default:
                    super.readSubElements(cs, context);
            }
        });
    }
}
