/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
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
        g.findVoltageRegulatorStatus().ifPresent(voltageRegulatorOn -> {
            try {
                context.getWriter().writeAttribute("voltageRegulatorOn", Boolean.toString(voltageRegulatorOn));
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
        XmlUtil.writeDouble("targetP", g.getTargetP(), context.getWriter());
        XmlUtil.writeDouble("targetV", g.getTargetV(), context.getWriter());
        XmlUtil.writeDouble("targetQ", g.getTargetQ(), context.getWriter());
        writeNodeOrBus(null, g.getTerminal(), context);
        writePQ(null, g.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(Generator g, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        if (!Objects.equals(g, g.getRegulatingTerminal().getConnectable())) {
            TerminalRefXml.writeTerminalRef(g.getRegulatingTerminal(), context, "regulatingTerminal");
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
        String voltageRegulatorOn = context.getReader().getAttributeValue(null, "voltageRegulatorOn");
        double targetP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetP");
        double targetV = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetV");
        double targetQ = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetQ");
        readNodeOrBus(adder, context);
        adder.setEnergySource(energySource)
                .setMinP(minP)
                .setMaxP(maxP)
                .setRatedS(ratedS)
                .setTargetP(targetP)
                .setTargetV(targetV)
                .setTargetQ(targetQ);
        if (voltageRegulatorOn != null) {
            adder.setVoltageRegulatorOn(Boolean.parseBoolean(voltageRegulatorOn));
        }
        Generator g = adder.add();
        readPQ(null, g.getTerminal(), context.getReader());
        return g;
    }

    @Override
    protected void readSubElements(Generator g, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "regulatingTerminal":
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
