/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GeneratorXml extends ConnectableXml<Generator, GeneratorAdder, VoltageLevel> {

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
    protected void writeRootElementAttributes(Generator g, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("energySource", g.getEnergySource().name());
        XmlUtil.writeFloat("minP", g.getMinP(), context.getWriter());
        XmlUtil.writeFloat("maxP", g.getMaxP(), context.getWriter());
        XmlUtil.writeFloat("ratedS", g.getRatedS(), context.getWriter());
        context.getWriter().writeAttribute("voltageRegulatorOn", Boolean.toString(g.isVoltageRegulatorOn()));
        XmlUtil.writeFloat("targetP", g.getTargetP(), context.getWriter());
        XmlUtil.writeFloat("targetV", g.getTargetV(), context.getWriter());
        XmlUtil.writeFloat("targetQ", g.getTargetQ(), context.getWriter());
        writeNodeOrBus(null, g.getTerminal(), context);
        writePQ(null, g.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(Generator g, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
        if (g.getRegulatingTerminal() != null
                && !Objects.equals(g.getRegulatingTerminal().getBusBreakerView().getConnectableBus(),
                                  g.getTerminal().getBusBreakerView().getConnectableBus())) {
            writeTerminalRef(g.getRegulatingTerminal(), context, "regulatingTerminal");
        }
        switch (g.getReactiveLimits().getKind()) {
            case CURVE: {
                ReactiveCapabilityCurve curve = g.getReactiveLimits(ReactiveCapabilityCurve.class);
                context.getWriter().writeStartElement(IIDM_URI, "reactiveCapabilityCurve");
                for (ReactiveCapabilityCurve.Point point : curve.getPoints()) {
                    context.getWriter().writeEmptyElement(IIDM_URI, "point");
                    XmlUtil.writeFloat("p", point.getP(), context.getWriter());
                    XmlUtil.writeFloat("minQ", point.getMinQ(), context.getWriter());
                    XmlUtil.writeFloat("maxQ", point.getMaxQ(), context.getWriter());
                }
                context.getWriter().writeEndElement();
            }
            break;

            case MIN_MAX: {
                MinMaxReactiveLimits limits = g.getReactiveLimits(MinMaxReactiveLimits.class);
                context.getWriter().writeEmptyElement(IIDM_URI, "minMaxReactiveLimits");
                XmlUtil.writeFloat("minQ", limits.getMinQ(), context.getWriter());
                XmlUtil.writeFloat("maxQ", limits.getMaxQ(), context.getWriter());
            }
            break;

            default:
                throw new AssertionError();
        }
    }

    @Override
    protected GeneratorAdder createAdder(VoltageLevel vl) {
        return vl.newGenerator();
    }

    @Override
    protected Generator readRootElementAttributes(GeneratorAdder adder, XMLStreamReader reader, List<Runnable> endTasks) {
        EnergySource energySource = EnergySource.valueOf(reader.getAttributeValue(null, "energySource"));
        float minP = XmlUtil.readFloatAttribute(reader, "minP");
        float maxP = XmlUtil.readFloatAttribute(reader, "maxP");
        float ratedS = XmlUtil.readOptionalFloatAttribute(reader, "ratedS");
        boolean voltageRegulatorOn = XmlUtil.readBoolAttribute(reader, "voltageRegulatorOn");
        float targetP = XmlUtil.readFloatAttribute(reader, "targetP");
        float targetV = XmlUtil.readOptionalFloatAttribute(reader, "targetV");
        float targetQ = XmlUtil.readOptionalFloatAttribute(reader, "targetQ");
        readNodeOrBus(adder, reader);
        Generator g = adder.setEnergySource(energySource)
                .setMinP(minP)
                .setMaxP(maxP)
                .setRatedS(ratedS)
                .setVoltageRegulatorOn(voltageRegulatorOn)
                .setTargetP(targetP)
                .setTargetV(targetV)
                .setTargetQ(targetQ)
                .add();
        readPQ(null, g.getTerminal(), reader);
        return g;
    }

    @Override
    protected void readSubElements(Generator g, XMLStreamReader reader, List<Runnable> endTasks) throws XMLStreamException {
        readUntilEndRootElement(reader, () -> {
            switch (reader.getLocalName()) {
                case "regulatingTerminal":
                    String id = reader.getAttributeValue(null, "id");
                    String side = reader.getAttributeValue(null, "side");
                    endTasks.add(() -> g.setRegulatingTerminal(readTerminalRef(g.getTerminal().getVoltageLevel().getSubstation().getNetwork(), id, side)));
                    break;

                case "reactiveCapabilityCurve":
                    ReactiveCapabilityCurveAdder curveAdder = g.newReactiveCapabilityCurve();
                    XmlUtil.readUntilEndElement("reactiveCapabilityCurve", reader, () -> {
                        if (reader.getLocalName().equals("point")) {
                            float p = XmlUtil.readFloatAttribute(reader, "p");
                            float minQ = XmlUtil.readFloatAttribute(reader, "minQ");
                            float maxQ = XmlUtil.readFloatAttribute(reader, "maxQ");
                            curveAdder.beginPoint()
                                    .setP(p)
                                    .setMinQ(minQ)
                                    .setMaxQ(maxQ)
                                    .endPoint();
                        }
                    });
                    curveAdder.add();
                    break;

                case "minMaxReactiveLimits":
                    float min = XmlUtil.readFloatAttribute(reader, "minQ");
                    float max = XmlUtil.readFloatAttribute(reader, "maxQ");
                    g.newMinMaxReactiveLimits()
                            .setMinQ(min)
                            .setMaxQ(max)
                            .add();
                    break;

                default:
                    super.readSubElements(g, reader, endTasks);
            }
        });
    }
}