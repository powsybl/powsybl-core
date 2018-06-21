/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.function.Supplier;

import static com.powsybl.iidm.xml.IidmXmlConstants.IIDM_URI;
/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractConnectableXml<T extends Connectable, A extends IdentifiableAdder<A>, P extends Container> extends AbstractIdentifiableXml<T, A, P> {

    private static final String BUS = "bus";
    private static final String CONNECTABLE_BUS = "connectableBus";
    private static final String NODE = "node";

    private static final String CURRENT_LIMITS = "currentLimits";

    private static String indexToString(Integer index) {
        return index != null ? index.toString() : "";
    }

    protected static void writeNodeOrBus(Integer index, Terminal t, NetworkXmlWriterContext context) throws XMLStreamException {
        TopologyLevel topologyLevel = TopologyLevel.min(t.getVoltageLevel().getTopologyKind(), context.getOptions().getTopologyLevel());
        switch (topologyLevel) {
            case NODE_BREAKER:
                writeNode(index, t, context);
                break;
            case BUS_BREAKER:
                writeBus(index, t.getBusBreakerView().getBus(), t.getBusBreakerView().getConnectableBus(), context);
                break;
            case BUS_BRANCH:
                writeBus(index, t.getBusView().getBus(), t.getBusView().getConnectableBus(), context);
                break;
            default:
                throw new AssertionError("Unexpected TopologyLevel value: " + topologyLevel);
        }

        if (index != null) {
            context.getWriter().writeAttribute("voltageLevelId" + index, context.getAnonymizer().anonymizeString(t.getVoltageLevel().getId()));
        }
    }

    private static void writeNode(Integer index, Terminal t, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute(NODE + indexToString(index),
            Integer.toString(t.getNodeBreakerView().getNode()));
    }

    private static void writeBus(Integer index, Bus bus, Bus connectableBus, NetworkXmlWriterContext context) throws XMLStreamException {
        if (bus != null) {
            context.getWriter().writeAttribute(BUS + indexToString(index), context.getAnonymizer().anonymizeString(bus.getId()));
        }
        if (connectableBus != null) {
            context.getWriter().writeAttribute(CONNECTABLE_BUS + indexToString(index), context.getAnonymizer().anonymizeString(connectableBus.getId()));
        }
    }

    protected static void readNodeOrBus(InjectionAdder adder, NetworkXmlReaderContext context) {
        String bus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS));
        String connectableBus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, CONNECTABLE_BUS));
        Integer node = XmlUtil.readOptionalIntegerAttribute(context.getReader(), NODE);
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

    protected static void readNodeOrBus(BranchAdder adder, NetworkXmlReaderContext context) {
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

    protected static void readNodeOrBus(int index, LegAdder adder, NetworkXmlReaderContext context) {
        String bus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS + index));
        String connectableBus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, CONNECTABLE_BUS + index));
        Integer node = XmlUtil.readOptionalIntegerAttribute(context.getReader(), NODE + index);
        String voltageLevelId = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "voltageLevelId" + index));
        if (bus != null) {
            adder.setBus(bus);
        }
        if (connectableBus != null) {
            adder.setConnectableBus(connectableBus);
        }
        if (node != null) {
            adder.setNode(node);
        }
        adder.setVoltageLevel(voltageLevelId);
    }

    protected static void writePQ(Integer index, Terminal t, XMLStreamWriter writer) throws XMLStreamException {
        XmlUtil.writeOptionalDouble("p" + indexToString(index), t.getP(), Double.NaN, writer);
        XmlUtil.writeOptionalDouble("q" + indexToString(index), t.getQ(), Double.NaN, writer);
    }

    protected static void readPQ(Integer index, Terminal t, XMLStreamReader reader) {
        double p = XmlUtil.readOptionalDoubleAttribute(reader, "p" + indexToString(index));
        double q = XmlUtil.readOptionalDoubleAttribute(reader, "q" + indexToString(index));
        t.setP(p)
            .setQ(q);
    }

    public static void readCurrentLimits(Integer index, Supplier<CurrentLimitsAdder> currentLimitOwner, XMLStreamReader reader) throws XMLStreamException {
        CurrentLimitsAdder adder = currentLimitOwner.get();
        double permanentLimit = XmlUtil.readOptionalDoubleAttribute(reader, "permanentLimit");
        adder.setPermanentLimit(permanentLimit);
        XmlUtil.readUntilEndElement(CURRENT_LIMITS + indexToString(index), reader, () -> {
            if ("temporaryLimit".equals(reader.getLocalName())) {
                String name = reader.getAttributeValue(null, "name");
                int acceptableDuration = XmlUtil.readOptionalIntegerAttribute(reader, "acceptableDuration", Integer.MAX_VALUE);
                double value = XmlUtil.readOptionalDoubleAttribute(reader, "value", Double.MAX_VALUE);
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
        if (!Double.isNaN(limits.getPermanentLimit())
            || !limits.getTemporaryLimits().isEmpty()) {
            if (limits.getTemporaryLimits().isEmpty()) {
                writer.writeEmptyElement(nsUri, CURRENT_LIMITS + indexToString(index));
            } else {
                writer.writeStartElement(nsUri, CURRENT_LIMITS + indexToString(index));
            }
            XmlUtil.writeDouble("permanentLimit", limits.getPermanentLimit(), writer);
            for (CurrentLimits.TemporaryLimit tl : limits.getTemporaryLimits()) {
                writer.writeEmptyElement(IIDM_URI, "temporaryLimit");
                writer.writeAttribute("name", tl.getName());
                XmlUtil.writeOptionalInt("acceptableDuration", tl.getAcceptableDuration(), Integer.MAX_VALUE, writer);
                XmlUtil.writeOptionalDouble("value", tl.getValue(), Double.MAX_VALUE, writer);
                XmlUtil.writeOptionalBoolean("fictitious", tl.isFictitious(), false, writer);
            }
            if (!limits.getTemporaryLimits().isEmpty()) {
                writer.writeEndElement();
            }
        }
    }

    protected static void writeTerminalRef(Terminal t, NetworkXmlWriterContext context, String elementName) throws XMLStreamException {
        Connectable c = t.getConnectable();
        if (!context.getFilter().test(c)) {
            throw new PowsyblException("Oups, terminal ref point to a filtered equipment " + c.getId());
        }
        context.getWriter().writeEmptyElement(IIDM_URI, elementName);
        context.getWriter().writeAttribute("id", context.getAnonymizer().anonymizeString(c.getId()));
        if (c.getTerminals().size() > 1) {
            if (c instanceof Injection) {
                // nothing to do
            } else if (c instanceof Branch) {
                Branch branch = (Branch) c;
                context.getWriter().writeAttribute("side", branch.getSide(t).name());
            } else if (c instanceof ThreeWindingsTransformer) {
                ThreeWindingsTransformer twt = (ThreeWindingsTransformer) c;
                context.getWriter().writeAttribute("side", twt.getSide(t).name());
            } else {
                throw new AssertionError("Unexpected Connectable instance: " + c.getClass());
            }
        }
    }

    protected static Terminal readTerminalRef(Network network, String id, String side) {
        Identifiable identifiable = network.getIdentifiable(id);
        if (identifiable instanceof Injection) {
            return ((Injection) identifiable).getTerminal();
        } else if (identifiable instanceof Branch) {
            return side.equals(Branch.Side.ONE.name()) ? ((Branch) identifiable).getTerminal1()
                : ((Branch) identifiable).getTerminal2();
        } else if (identifiable instanceof ThreeWindingsTransformer) {
            ThreeWindingsTransformer twt = (ThreeWindingsTransformer) identifiable;
            return twt.getTerminal(ThreeWindingsTransformer.Side.valueOf(side));
        } else {
            throw new AssertionError("Unexpected Identifiable instance: " + identifiable.getClass());
        }
    }
}
