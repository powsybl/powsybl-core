/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.VoltageLevel;

import javax.xml.stream.XMLStreamException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GeneratorXml extends AbstractConnectableXml<Generator, GeneratorAdder, VoltageLevel> {

    static final GeneratorXml INSTANCE = new GeneratorXml();

    static final String ROOT_ELEMENT_NAME = "generator";
    static final String REGULATING_TERMINAL = "regulatingTerminal";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(Generator g) {
        return true;
    }

    @Override
    protected void writeRootElementAttributes(Generator g, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("energySource", g.getEnergySource().name());
        XmlUtil.writeDouble("minP", g.getMinP(), context.getWriter());
        XmlUtil.writeDouble("maxP", g.getMaxP(), context.getWriter());
        XmlUtil.writeDouble("ratedS", g.getRatedS(), context.getWriter());
        context.getWriter().writeAttribute("voltageRegulatorOn", Boolean.toString(g.isVoltageRegulatorOn()));
        XmlUtil.writeDouble("targetP", g.getTargetP(), context.getWriter());
        XmlUtil.writeDouble("targetV", g.getTargetV(), context.getWriter());
        XmlUtil.writeDouble("targetQ", g.getTargetQ(), context.getWriter());
        writeNodeOrBus(null, g.getTerminal(), context);
        writePQ(null, g.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(Generator g, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        if (g.getRegulatingTerminal() != null) {
            // Remote regulatingTerminal is always written, from 1.8 local regulatingTerminal is also written
            if (!Objects.equals(g, g.getRegulatingTerminal().getConnectable())) {
                TerminalRefXml.writeTerminalRef(g.getRegulatingTerminal(), context, REGULATING_TERMINAL);
            } else {
                //IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_8, context, () -> TerminalRefXml.writeTerminalRef(g.getRegulatingTerminal(), context, REGULATING_TERMINAL));
            }
        }
        ReactiveLimitsXml.INSTANCE.write(g, context);
    }

    @Override
    protected GeneratorAdder createAdder(VoltageLevel vl) {
        return vl.newGenerator();
    }

    @Override
    protected Generator readRootElementAttributes(GeneratorAdder adder, NetworkXmlReaderContext context) {
        String energySourceStr = context.getReader().getAttributeValue(null, "energySource");
        EnergySource energySource = energySourceStr != null ? EnergySource.valueOf(energySourceStr) : null;
        double minP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "minP");
        double maxP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "maxP");
        double ratedS = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "ratedS");
        boolean voltageRegulatorOn = Boolean.parseBoolean(context.getReader().getAttributeValue(null, "voltageRegulatorOn"));
        double targetP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetP");
        double targetV = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetV");
        double targetQ = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetQ");
        readNodeOrBus(adder, context);

        // The regulating terminal is local or remote in enabled controls and null, local or remote in disabled ones.
        // As the regulating terminal is set after the equipment has been created (it is managed as a subElement),
        // in enabled controls the regulating terminal is initially localized and overwritten later by the read one
        // (a null regulating terminal is not allowed when the equipment is created).
        // In disabled controls only the read regulation terminal is set (a null regulating terminal is allowed).
        // Until version 1.8 there is an ambiguity reading disabled controls as only remote regulating terminals are written.
        // Not written regulating terminals could be null or local. Ambiguity is solved by considering them null.

        boolean useLocalRegulationOn = false;
        if (voltageRegulatorOn) {
            useLocalRegulationOn = true;
        }

        adder.setEnergySource(energySource)
                .setMinP(minP)
                .setMaxP(maxP)
                .setRatedS(ratedS)
                .setTargetP(targetP)
                .setTargetV(targetV)
                .setTargetQ(targetQ)
                .useLocalRegulation(useLocalRegulationOn)
                .setVoltageRegulatorOn(voltageRegulatorOn);
        Generator g = adder.add();
        readPQ(null, g.getTerminal(), context.getReader());
        return g;
    }

    @Override
    protected void readSubElements(Generator g, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case REGULATING_TERMINAL:
                    String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
                    String side = context.getReader().getAttributeValue(null, "side");
                    context.getEndTasks().add(() -> g.setRegulatingTerminal(TerminalRefXml.readTerminalRef(g.getNetwork(), id, side)));
                    break;

                case "reactiveCapabilityCurve":
                case "minMaxReactiveLimits":
                    ReactiveLimitsXml.INSTANCE.read(g, context);
                    break;

                default:
                    super.readSubElements(g, context);
            }
        });
    }
}
