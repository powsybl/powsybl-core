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
    private static final String CONNECT = "connect";
    private static final String NODE = "node";
    private static final String VOLTAGE_LEVEL_ID = "voltageLevelId";

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
                writeBus(index, t.getBusBreakerView().getConnectableBus().getId(), t.getBusBreakerView().getBus() != null, context);
                break;
            case BUS_BRANCH:
                writeBus(index, t.getBusView().getConnectableBus().getId(), t.getBusView().getBus() != null, context);
                break;
            default:
                throw new AssertionError("Unexpected TopologyLevel value: " + topologyLevel);
        }

        if (index != null) {
            context.getWriter().writeAttribute(VOLTAGE_LEVEL_ID + index, context.getAnonymizer().anonymizeString(t.getVoltageLevel().getId()));
        }
    }

    private static void writeNode(Integer index, Terminal t, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute(NODE + indexToString(index),
            Integer.toString(t.getNodeBreakerView().getNode()));
    }

    private static void writeBus(Integer index, String busId, Boolean isConnected, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute(BUS + indexToString(index), context.getAnonymizer().anonymizeString(busId));
        context.getWriter().writeAttribute(CONNECT + indexToString(index), Boolean.toString(isConnected));
    }

    protected static void readNodeOrBus(InjectionAdder adder, NetworkXmlReaderContext context) {
        if (context.getVersion().equals("1_0")) {
            String connectableBus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, CONNECTABLE_BUS));
            String bus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS));
            if (bus != null) {
                adder.setBus(bus);
            }
            if (connectableBus != null) {
                adder.setConnectableBus(connectableBus);
            }
        } else {
            String connectableBus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS));
            boolean isConnected = XmlUtil.readBoolAttribute(context.getReader(), CONNECT);
            adder.setConnectableBus(connectableBus);
            if (isConnected) {
                adder.setBus(connectableBus);
            }
        }

        Integer node = XmlUtil.readOptionalIntegerAttribute(context.getReader(), NODE);
        if (node != null) {
            adder.setNode(node);
        }
    }

    protected static void readNodeOrBus(BranchAdder adder, NetworkXmlReaderContext context) {
        if (context.getVersion().equals("1_0")) {
            readVersion10Buses(adder, context);
        } else {
            String connectableBus1 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS + "1"));
            boolean isConnected1 = XmlUtil.readBoolAttribute(context.getReader(), CONNECT + "1");
            adder.setConnectableBus1(connectableBus1);
            if (isConnected1) {
                adder.setBus1(connectableBus1);
            }
            String connectableBus2 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS + "2"));
            boolean isConnected2 = XmlUtil.readBoolAttribute(context.getReader(), CONNECT + "2");
            adder.setConnectableBus2(connectableBus2);
            if (isConnected2) {
                adder.setBus2(connectableBus2);
            }
        }

        readNodesAndVoltageLevels(adder, context);
    }

    private static void readVersion10Buses(BranchAdder adder, NetworkXmlReaderContext context) {
        String bus1 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "bus1"));
        String connectableBus1 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "connectableBus1"));
        String bus2 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "bus2"));
        String connectableBus2 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "connectableBus2"));
        if (bus1 != null) {
            adder.setBus1(bus1);
        }
        if (connectableBus1 != null) {
            adder.setConnectableBus1(connectableBus1);
        }
        if (bus2 != null) {
            adder.setBus2(bus2);
        }
        if (connectableBus2 != null) {
            adder.setConnectableBus2(connectableBus2);
        }
    }

    private static void readNodesAndVoltageLevels(BranchAdder adder, NetworkXmlReaderContext context) {
        readNodeAndVoltageLevel(adder, context, 1);
        readNodeAndVoltageLevel(adder, context, 2);
    }

    private static void readNodeAndVoltageLevel(BranchAdder adder, NetworkXmlReaderContext context, int side) {
        if (side != 1 && side != 2) {
            throw new AssertionError("Unexcepted side '" + side + "' found in voltage level or node.");
        } else {
            String voltageLevelId = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, VOLTAGE_LEVEL_ID + side));
            Integer node = XmlUtil.readOptionalIntegerAttribute(context.getReader(), "node" + side);
            if (side == 1) {
                adder.setVoltageLevel1(voltageLevelId);
                if (node != null) {
                    adder.setNode1(node);
                }
            } else {
                adder.setVoltageLevel2(voltageLevelId);
                if (node != null) {
                    adder.setNode2(node);
                }
            }
        }
    }


    protected static void readNodeOrBus(int index, LegAdder adder, NetworkXmlReaderContext context) {
        if (context.getVersion().equals("1_0")) {
            String bus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS + index));
            String connectableBus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, CONNECTABLE_BUS + index));
            if (bus != null) {
                adder.setBus(bus);
            }
            if (connectableBus != null) {
                adder.setConnectableBus(connectableBus);
            }
        } else {
            String connectableBus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS + index));
            boolean isConnected = XmlUtil.readBoolAttribute(context.getReader(), CONNECT + index);
            adder.setConnectableBus(connectableBus);
            if (isConnected) {
                adder.setBus(connectableBus);
            }
        }

        Integer node = XmlUtil.readOptionalIntegerAttribute(context.getReader(), NODE + index);
        if (node != null) {
            adder.setNode(node);
        }
        String voltageLevelId = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, VOLTAGE_LEVEL_ID + index));
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
        if (c instanceof Injection) {
            context.getWriter().writeAttribute("id", context.getAnonymizer().anonymizeString(c.getId()));
        }

        if (c.getTerminals().size() > 1) {
            if (c instanceof TwoWindingsTransformer) {
                TwoWindingsTransformer twt = (TwoWindingsTransformer) c;
                context.getWriter().writeAttribute("side", twt.getSide(t).name());
            } else if (c instanceof ThreeWindingsTransformer) {
                ThreeWindingsTransformer twt = (ThreeWindingsTransformer) c;
                context.getWriter().writeAttribute("side", twt.getSide(t).name());
            } else if (c instanceof Branch) {
                Branch branch = (Branch) c;
                context.getWriter().writeAttribute("id", context.getAnonymizer().anonymizeString(c.getId()));
                context.getWriter().writeAttribute("side", branch.getSide(t).name());
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
