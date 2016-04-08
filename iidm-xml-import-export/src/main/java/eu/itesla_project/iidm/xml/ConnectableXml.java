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
abstract class ConnectableXml<T extends Connectable, A extends IdentifiableAdder<A>, P extends Container> extends IdentifiableXml<T, A, P> {

    private static String indexToString(Integer index) {
        return index != null ? index.toString() : "";
    }

    protected static void writeNodeOrBus(Integer index, Terminal t, XmlWriterContext context) throws XMLStreamException {
        if (context.getOptions().isForceBusBranchTopo()) {
            Bus bus = t.getBusView().getBus();
            if (bus != null) {
                context.getWriter().writeAttribute("bus" + indexToString(index), bus.getId());
            }
            Bus connectableBus = t.getBusView().getConnectableBus();
            if (connectableBus != null) {
                context.getWriter().writeAttribute("connectableBus" + indexToString(index), connectableBus.getId());
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
                        context.getWriter().writeAttribute("bus" + indexToString(index), bus.getId());
                    }
                    Bus connectableBus = t.getBusBreakerView().getConnectableBus();
                    if (connectableBus != null) {
                        context.getWriter().writeAttribute("connectableBus" + indexToString(index), connectableBus.getId());
                    }
                    break;

                default:
                    throw new AssertionError();
            }
        }
        if (index != null) {
            context.getWriter().writeAttribute("voltageLevelId" + index, t.getVoltageLevel().getId());
        }
    }

    protected static void readNodeOrBus(SingleTerminalConnectableAdder adder, XMLStreamReader reader) {
        String bus = reader.getAttributeValue(null, "bus");
        String connectableBus = reader.getAttributeValue(null, "connectableBus");
        Integer node = getOptionalIntegerAttributeValue(reader, "node");
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

    protected static void readNodeOrBus(TwoTerminalsConnectableAdder adder, XMLStreamReader reader) {
        String bus1 = reader.getAttributeValue(null, "bus1");
        String connectableBus1 = reader.getAttributeValue(null, "connectableBus1");
        Integer node1 = getOptionalIntegerAttributeValue(reader, "node1");
        String voltageLevelId1 = reader.getAttributeValue(null, "voltageLevelId1");
        String bus2 = reader.getAttributeValue(null, "bus2");
        String connectableBus2 = reader.getAttributeValue(null, "connectableBus2");
        Integer node2 = getOptionalIntegerAttributeValue(reader, "node2");
        String voltageLevelId2 = reader.getAttributeValue(null, "voltageLevelId2");
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
            writeFloat("p" + indexToString(index), t.getP(), writer);
        }
        if (!Float.isNaN(t.getQ())) {
            writeFloat("q" + indexToString(index), t.getQ(), writer);
        }
    }

    protected static void readPQ(Integer index, Terminal t, XMLStreamReader reader) {
        float p = readOptionalFloatAttribute(reader, "p" + indexToString(index));
        float q = readOptionalFloatAttribute(reader, "q" + indexToString(index));
        t.setP(p)
                .setQ(q);
    }

    protected void readCurrentLimits(Integer index, Supplier<CurrentLimitsAdder> currentLimitOwner, XMLStreamReader reader) throws XMLStreamException {
        CurrentLimitsAdder adder = currentLimitOwner.get();
        float permanentLimit = readOptionalFloatAttribute(reader, "permanentLimit");
        adder.setPermanentLimit(permanentLimit);
        XmlUtil.readUntilEndElement("currentLimits" + indexToString(index), reader, () -> {
            if ("temporaryLimit".equals(reader.getLocalName())) {
                int acceptableDuration = Integer.valueOf(reader.getAttributeValue(null, "acceptableDuration"));
                float limit = Float.valueOf(XmlUtil.readUntilEndElement("temporaryLimit", reader, null));
                adder.beginTemporaryLimit()
                        .setAcceptableDuration(acceptableDuration)
                        .setLimit(limit)
                        .endTemporaryLimit();
            }
        });
        adder.add();
    }

    protected static void writeCurrentLimits(Integer index, CurrentLimits limits, XMLStreamWriter writer) throws XMLStreamException {
        if (!Float.isNaN(limits.getPermanentLimit())
                || limits.getTemporaryLimits().size() > 0) {
            if (limits.getTemporaryLimits().isEmpty()) {
                writer.writeEmptyElement(IIDM_URI, "currentLimits" + indexToString(index));
            } else {
                writer.writeStartElement(IIDM_URI, "currentLimits" + indexToString(index));
            }
            writeFloat("permanentLimit", limits.getPermanentLimit(), writer);
            for (CurrentLimits.TemporaryLimit tl : limits.getTemporaryLimits()) {
                writer.writeStartElement(IIDM_URI, "temporaryLimit");
                writer.writeAttribute("acceptableDuration", Integer.toString(tl.getAcceptableDuration()));
                writer.writeCharacters(Float.toString(tl.getLimit()));
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
        context.getWriter().writeAttribute("id", c.getId());
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
