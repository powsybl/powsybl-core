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
import javax.xml.stream.XMLStreamWriter;
import java.util.function.Supplier;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class ConnectableXml<T extends Connectable, A extends IdentifiableAdder<A>, P extends Container> extends IdentifiableXml<T, A, P> {

    private static String indexToString(Integer index) {
        return index != null ? index.toString() : "";
    }

    protected static void writeNodeOrBus(Integer index, Terminal t, XmlWriterContext context) throws XMLStreamException {
        if (context.getOptions().isForceBusBranchTopo()) {
            Bus bus = t.getBusView().getBus();
            if (bus != null) {
                context.getWriter().writeAttribute("bus" + indexToString(index), context.getAnonymizer().anonymizeString(bus.getId()));
            }
            Bus connectableBus = t.getBusView().getConnectableBus();
            if (connectableBus != null) {
                context.getWriter().writeAttribute("connectableBus" + indexToString(index), context.getAnonymizer().anonymizeString(connectableBus.getId()));
            }
        } else {
            switch (t.getVoltageLevel().getTopologyKind()) {
                case NODE_BREAKER:
                    context.getWriter().writeAttribute("node" + indexToString(index),
                            Integer.toString(t.getNodeBreakerView().getNode()));
                    break;

                case BUS_BREAKER:
                    Bus bus = t.getBusBreakerView().getBus();
                    if (bus != null) {
                        context.getWriter().writeAttribute("bus" + indexToString(index), context.getAnonymizer().anonymizeString(bus.getId()));
                    }
                    Bus connectableBus = t.getBusBreakerView().getConnectableBus();
                    if (connectableBus != null) {
                        context.getWriter().writeAttribute("connectableBus" + indexToString(index), context.getAnonymizer().anonymizeString(connectableBus.getId()));
                    }
                    break;

                default:
                    throw new AssertionError();
            }
        }
        if (index != null) {
            context.getWriter().writeAttribute("voltageLevelId" + index, context.getAnonymizer().anonymizeString(t.getVoltageLevel().getId()));
        }
    }

    protected static void readNodeOrBus(SingleTerminalConnectableAdder adder, XmlReaderContext context) {
        String bus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "bus"));
        String connectableBus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "connectableBus"));
        Integer node = XmlUtil.readOptionalIntegerAttribute(context.getReader(), "node");
        if (bus != null) {
            adder.setBus(bus);
        }
        if (connectableBus != null) {
            adder.setConnectableBus(connectableBus);
        }
        if (node != null) {
            adder.setNode(node);
        }
    }

    protected static void readNodeOrBus(TwoTerminalsConnectableAdder adder, XmlReaderContext context) {
        String bus1 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "bus1"));
        String connectableBus1 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "connectableBus1"));
        Integer node1 = XmlUtil.readOptionalIntegerAttribute(context.getReader(), "node1");
        String voltageLevelId1 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "voltageLevelId1"));
        String bus2 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "bus2"));
        String connectableBus2 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "connectableBus2"));
        Integer node2 = XmlUtil.readOptionalIntegerAttribute(context.getReader(), "node2");
        String voltageLevelId2 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "voltageLevelId2"));
        if (bus1 != null) {
            adder.setBus1(bus1);
        }
        if (connectableBus1 != null) {
            adder.setConnectableBus1(connectableBus1);
        }
        if (node1 != null) {
            adder.setNode1(node1);
        }
        adder.setVoltageLevel1(voltageLevelId1);
        if (bus2 != null) {
            adder.setBus2(bus2);
        }
        if (connectableBus2 != null) {
            adder.setConnectableBus2(connectableBus2);
        }
        if (node2 != null) {
            adder.setNode2(node2);
        }
        adder.setVoltageLevel2(voltageLevelId2);
    }

    protected static void writePQ(Integer index, Terminal t, XMLStreamWriter writer) throws XMLStreamException {
        if (!Float.isNaN(t.getP())) {
            XmlUtil.writeFloat("p" + indexToString(index), t.getP(), writer);
        }
        if (!Float.isNaN(t.getQ())) {
            XmlUtil.writeFloat("q" + indexToString(index), t.getQ(), writer);
        }
    }

    protected static void readPQ(Integer index, Terminal t, XMLStreamReader reader) {
        float p = XmlUtil.readOptionalFloatAttribute(reader, "p" + indexToString(index));
        float q = XmlUtil.readOptionalFloatAttribute(reader, "q" + indexToString(index));
        t.setP(p)
                .setQ(q);
    }

    public static void readCurrentLimits(Integer index, Supplier<CurrentLimitsAdder> currentLimitOwner, XMLStreamReader reader) throws XMLStreamException {
        CurrentLimitsAdder adder = currentLimitOwner.get();
        float permanentLimit = XmlUtil.readOptionalFloatAttribute(reader, "permanentLimit");
        adder.setPermanentLimit(permanentLimit);
        XmlUtil.readUntilEndElement("currentLimits" + indexToString(index), reader, () -> {
            if ("temporaryLimit".equals(reader.getLocalName())) {
                String name = reader.getAttributeValue(null, "name");
                int acceptableDuration = XmlUtil.readOptionalIntegerAttribute(reader, "acceptableDuration", Integer.MAX_VALUE);
                float value = XmlUtil.readOptionalFloatAttribute(reader, "value", Float.MAX_VALUE);
                boolean fictitious = XmlUtil.readOptionalBoolAttribute(reader, "fictitious", false);
                adder.beginTemporaryLimit()
                        .setName(name)
                        .setAcceptableDuration(acceptableDuration)
                        .setValue(value)
                        .setFictitious(fictitious)
                        .endTemporaryLimit();
            }
        });
        adder.add();
    }

    public static void writeCurrentLimits(Integer index, CurrentLimits limits, XMLStreamWriter writer) throws XMLStreamException {
        writeCurrentLimits(index, limits, writer, IIDM_URI);
    }

    public static void writeCurrentLimits(Integer index, CurrentLimits limits, XMLStreamWriter writer, String nsUri) throws XMLStreamException {
        if (!Float.isNaN(limits.getPermanentLimit())
                || limits.getTemporaryLimits().size() > 0) {
            if (limits.getTemporaryLimits().isEmpty()) {
                writer.writeEmptyElement(nsUri, "currentLimits" + indexToString(index));
            } else {
                writer.writeStartElement(nsUri, "currentLimits" + indexToString(index));
            }
            XmlUtil.writeFloat("permanentLimit", limits.getPermanentLimit(), writer);
            for (CurrentLimits.TemporaryLimit tl : limits.getTemporaryLimits()) {
                writer.writeStartElement(IIDM_URI, "temporaryLimit");
                writer.writeAttribute("name", tl.getName());
                if (tl.getAcceptableDuration() != Integer.MAX_VALUE) {
                    writer.writeAttribute("acceptableDuration", Integer.toString(tl.getAcceptableDuration()));
                }
                if (tl.getValue() != Float.MAX_VALUE) {
                    writer.writeAttribute("value", Float.toString(tl.getValue()));
                }
                if (tl.isFictitious()) {
                    writer.writeAttribute("fictitious", Boolean.toString(tl.isFictitious()));
                }
                writer.writeEndElement();
            }
            if (!limits.getTemporaryLimits().isEmpty()) {
                writer.writeEndElement();
            }
        }
    }

    protected static void writeTerminalRef(Terminal t, XmlWriterContext context, String elementName) throws XMLStreamException {
        Connectable c = t.getConnectable();
        if (!context.getFilter().test(c)) {
            throw new RuntimeException("Oups, terminal ref point to a filtered equipment " + c.getId());
        }
        context.getWriter().writeEmptyElement(IIDM_URI, elementName);
        context.getWriter().writeAttribute("id", context.getAnonymizer().anonymizeString(c.getId()));
        if (c.getTerminals().size() > 1) {
            if (c instanceof SingleTerminalConnectable) {
                // nothing to do
            } else if (c instanceof TwoTerminalsConnectable) {
                TwoTerminalsConnectable branch = (TwoTerminalsConnectable) c;
                context.getWriter().writeAttribute("side", branch.getTerminal1() == t
                        ? TwoTerminalsConnectable.Side.ONE.name() : TwoTerminalsConnectable.Side.TWO.name());
            } else if (c instanceof ThreeWindingsTransformer) {
                ThreeWindingsTransformer twt = (ThreeWindingsTransformer) c;
                ThreeWindingsTransformer.Side side;
                if (twt.getLeg1().getTerminal() == t) {
                    side = ThreeWindingsTransformer.Side.ONE;
                } else if (twt.getLeg2().getTerminal() == t) {
                    side = ThreeWindingsTransformer.Side.TWO;
                } else if (twt.getLeg3().getTerminal() == t) {
                    side = ThreeWindingsTransformer.Side.THREE;
                } else {
                    throw new InternalError();
                }
                context.getWriter().writeAttribute("side", side.name());
            } else {
                throw new AssertionError();
            }
        }
    }

    protected static Terminal readTerminalRef(Network network, String id, String side) {
        Identifiable identifiable = network.getIdentifiable(id);
        if (identifiable instanceof SingleTerminalConnectable) {
            return ((SingleTerminalConnectable) identifiable).getTerminal();
        } else if (identifiable instanceof TwoTerminalsConnectable) {
            return side.equals(TwoTerminalsConnectable.Side.ONE.name()) ? ((TwoTerminalsConnectable) identifiable).getTerminal1()
                    : ((TwoTerminalsConnectable) identifiable).getTerminal2();
        } else if (identifiable instanceof ThreeWindingsTransformer) {
            throw new AssertionError("TODO"); // FIXME
        } else {
            throw new AssertionError();
        }
    }
}
