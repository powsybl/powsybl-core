/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.*;
import org.joda.time.DateTime;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class VoltageLevelXml extends IdentifiableXml<VoltageLevel, VoltageLevelAdder, Substation> {

    static final VoltageLevelXml INSTANCE = new VoltageLevelXml();

    static final String ROOT_ELEMENT_NAME = "voltageLevel";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(VoltageLevel vl) {
        return true;
    }

    @Override
    protected void writeRootElementAttributes(VoltageLevel vl, Substation s, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeFloat("nominalV", vl.getNominalV(), context.getWriter());
        XmlUtil.writeFloat("lowVoltageLimit", vl.getLowVoltageLimit(), context.getWriter());
        XmlUtil.writeFloat("highVoltageLimit", vl.getHighVoltageLimit(), context.getWriter());
        context.getWriter().writeAttribute("topologyKind", vl.getTopologyKind().name());
    }

    @Override
    protected void writeSubElements(VoltageLevel vl, Substation s, XmlWriterContext context) throws XMLStreamException {
        if (context.getOptions().isForceBusBranchTopo()) {
            context.getWriter().writeStartElement(IIDM_URI, "busBreakerTopology");
            for (Bus b : vl.getBusView().getBuses()) {
                if (!context.getFilter().test(b)) {
                    continue;
                }
                BusXml.INSTANCE.write(b, null, context);
            }
            context.getWriter().writeEndElement();
        } else {
            switch (vl.getTopologyKind()) {
                case NODE_BREAKER:
                    context.getWriter().writeStartElement(IIDM_URI, "nodeBreakerTopology");
                    context.getWriter().writeAttribute("nodeCount", Integer.toString(vl.getNodeBreakerView().getNodeCount()));
                    for (BusbarSection bs : vl.getNodeBreakerView().getBusbarSections()) {
                        BusbarSectionXml.INSTANCE.write(bs, null, context);
                    }
                    for (Switch sw : vl.getNodeBreakerView().getSwitches()) {
                        NodeBreakerViewSwitchXml.INSTANCE.write(sw, vl, context);
                    }
                    context.getWriter().writeEndElement();
                    break;

                case BUS_BREAKER:
                    context.getWriter().writeStartElement(IIDM_URI, "busBreakerTopology");
                    for (Bus b : vl.getBusBreakerView().getBuses()) {
                        if (!context.getFilter().test(b)) {
                            continue;
                        }
                        BusXml.INSTANCE.write(b, null, context);
                    }
                    for (Switch sw : vl.getBusBreakerView().getSwitches()) {
                        Bus b1 = vl.getBusBreakerView().getBus1(sw.getId());
                        Bus b2 = vl.getBusBreakerView().getBus2(sw.getId());
                        if (!context.getFilter().test(b1) || !context.getFilter().test(b2)) {
                            continue;
                        }
                        BusBreakerViewSwitchXml.INSTANCE.write(sw, vl, context);
                    }
                    context.getWriter().writeEndElement();
                    break;

                default:
                    throw new AssertionError();
            }
        }
        if (vl.getGeneratorCount() > 0) {
            for (Generator g : vl.getGenerators()) {
                if (!context.getFilter().test(g)) {
                    continue;
                }
                GeneratorXml.INSTANCE.write(g, null, context);
            }
        }
        if (vl.getLoadCount() > 0) {
            for (Load l : vl.getLoads()) {
                if (!context.getFilter().test(l)) {
                    continue;
                }

                LoadXml.INSTANCE.write(l, null, context);
            }
        }
        if (vl.getShuntCount() > 0) {
            for (ShuntCompensator sc : vl.getShunts()) {
                if (!context.getFilter().test(sc)) {
                    continue;
                }
                ShuntXml.INSTANCE.write(sc, null, context);
            }
        }
        if (vl.getDanglingLineCount() > 0) {
            for (DanglingLine dl : vl.getDanglingLines()) {
                if (!context.getFilter().test(dl)) {
                    continue;
                }
                DanglingLineXml.INSTANCE.write(dl, null, context);
            }
        }
        if (vl.getStaticVarCompensatorCount() > 0) {
            for (StaticVarCompensator svc : vl.getStaticVarCompensators()) {
                if (!context.getFilter().test(svc)) {
                    continue;
                }
                StaticVarCompensatorXml.INSTANCE.write(svc, null, context);
            }
        }
    }

    @Override
    protected VoltageLevelAdder createAdder(Substation s) {
        return s.newVoltageLevel();
    }

    @Override
    protected VoltageLevel readRootElementAttributes(VoltageLevelAdder adder, XMLStreamReader reader, List<Runnable> endTasks) {
        float nominalV = XmlUtil.readFloatAttribute(reader, "nominalV");
        float lowVoltageLimit = XmlUtil.readOptionalFloatAttribute(reader, "lowVoltageLimit");
        float highVoltageLimit = XmlUtil.readOptionalFloatAttribute(reader, "highVoltageLimit");
        TopologyKind topologyKind = TopologyKind.valueOf(reader.getAttributeValue(null, "topologyKind"));
        return adder
                .setNominalV(nominalV)
                .setLowVoltageLimit(lowVoltageLimit)
                .setHighVoltageLimit(highVoltageLimit)
                .setTopologyKind(topologyKind)
                .add();
    }

    @Override
    protected void readSubElements(VoltageLevel vl, XMLStreamReader reader, List<Runnable> endTasks) throws XMLStreamException {
        readUntilEndRootElement(reader, () -> {
            switch (reader.getLocalName()) {
                case "nodeBreakerTopology":
                    int nodeCount = XmlUtil.readIntAttribute(reader, "nodeCount");
                    vl.getNodeBreakerView().setNodeCount(nodeCount);
                    XmlUtil.readUntilEndElement("nodeBreakerTopology", reader, () -> {
                        switch (reader.getLocalName()) {
                            case BusbarSectionXml.ROOT_ELEMENT_NAME:
                                BusbarSectionXml.INSTANCE.read(reader, vl, endTasks);
                                break;

                            case NodeBreakerViewSwitchXml.ROOT_ELEMENT_NAME:
                                NodeBreakerViewSwitchXml.INSTANCE.read(reader, vl, endTasks);
                                break;

                            default:
                                throw new AssertionError();
                        }
                    });
                    break;

                case "busBreakerTopology":
                    XmlUtil.readUntilEndElement("busBreakerTopology", reader, () -> {
                        switch (reader.getLocalName()) {
                            case BusXml.ROOT_ELEMENT_NAME:
                                BusXml.INSTANCE.read(reader, vl, endTasks);
                                break;

                            case BusBreakerViewSwitchXml.ROOT_ELEMENT_NAME:
                                BusBreakerViewSwitchXml.INSTANCE.read(reader, vl, endTasks);
                                break;

                            default:
                                throw new AssertionError();
                        }
                    });
                    break;

                case GeneratorXml.ROOT_ELEMENT_NAME:
                    GeneratorXml.INSTANCE.read(reader, vl, endTasks);
                    break;

                case LoadXml.ROOT_ELEMENT_NAME:
                    LoadXml.INSTANCE.read(reader, vl, endTasks);
                    break;

                case ShuntXml.ROOT_ELEMENT_NAME:
                    ShuntXml.INSTANCE.read(reader, vl, endTasks);
                    break;

                case DanglingLineXml.ROOT_ELEMENT_NAME:
                    DanglingLineXml.INSTANCE.read(reader, vl, endTasks);
                    break;

                case StaticVarCompensatorXml.ROOT_ELEMENT_NAME:
                    StaticVarCompensatorXml.INSTANCE.read(reader, vl, endTasks);
                    break;

                default:
                    super.readSubElements(vl, reader, endTasks);
            }
        });
    }
}
