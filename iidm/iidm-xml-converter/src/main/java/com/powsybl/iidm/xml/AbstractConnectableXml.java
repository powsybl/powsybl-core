/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.function.Supplier;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractConnectableXml<T extends Connectable, A extends IdentifiableAdder<A>, P extends Container> extends AbstractIdentifiableXml<T, A, P> {

    private static final String BUS = "bus";
    private static final String CONNECTABLE_BUS = "connectableBus";
    private static final String BUS_ID = "busId";
    private static final String CONNECTION_STATUS = "connectionStatus";
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
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_2, context, () -> {
            if (bus != null) {
                XmlUtil.writeString(BUS + indexToString(index), context.getAnonymizer().anonymizeString(bus.getId()), context.getWriter());
            }
            if (connectableBus != null) {
                XmlUtil.writeString(CONNECTABLE_BUS + indexToString(index), context.getAnonymizer().anonymizeString(connectableBus.getId()), context.getWriter());
            }
        });

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
            String connectionStatus = null;
            if (bus != null) {
                connectionStatus = "CONNECTED";
                XmlUtil.writeString(BUS_ID + indexToString(index), context.getAnonymizer().anonymizeString(bus.getId()), context.getWriter());
            }
            if (connectableBus != null) {
                if (connectionStatus == null) {
                    XmlUtil.writeString(BUS_ID + indexToString(index), context.getAnonymizer().anonymizeString(connectableBus.getId()), context.getWriter());
                    connectionStatus = "CONNECTABLE";
                } else {
                    connectionStatus = connectionStatus + " | CONNECTABLE";
                }
                XmlUtil.writeString(CONNECTION_STATUS + indexToString(index), connectionStatus, context.getWriter());
            }
        });
    }

    protected static void readNodeOrBus(InjectionAdder adder, NetworkXmlReaderContext context) {
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_2, context, () -> {
            String bus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS));
            String connectableBus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, CONNECTABLE_BUS));
            if (bus != null) {
                adder.setBus(bus);
            }
            if (connectableBus != null) {
                adder.setConnectableBus(connectableBus);
            }
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
            String busId = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS_ID));
            String connectionStatus = context.getReader().getAttributeValue(null, CONNECTION_STATUS);
            if (busId != null && connectionStatus != null) {
                if (connectionStatus.contains("CONNECTED")) {
                    adder.setBus(busId);
                }
                if (connectionStatus.contains("CONNECTABLE")) {
                    adder.setConnectableBus(busId);
                }
            }
        });
        Integer node = XmlUtil.readOptionalIntegerAttribute(context.getReader(), NODE);
        if (node != null) {
            adder.setNode(node);
        }
    }

    protected static void readNodeOrBus(BranchAdder adder, NetworkXmlReaderContext context) {
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_2, context, () -> {
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
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
            String busId1 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "busId1"));
            String connectionStatus1 = context.getReader().getAttributeValue(null, "connectionStatus1");
            String busId2 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "busId2"));
            String connectionStatus2 = context.getReader().getAttributeValue(null, "connectionStatus2");
            if (busId1 != null && connectionStatus1 != null) {
                if (connectionStatus1.contains("CONNECTED")) {
                    adder.setBus1(busId1);
                }
                if (connectionStatus1.contains("CONNECTABLE")) {
                    adder.setConnectableBus1(busId1);
                }
            }
            if (busId2 != null && connectionStatus2 != null) {
                if (connectionStatus2.contains("CONNECTED")) {
                    adder.setBus2(busId2);
                }
                if (connectionStatus2.contains("CONNECTABLE")) {
                    adder.setConnectableBus2(busId2);
                }
            }
        });
        Integer node1 = XmlUtil.readOptionalIntegerAttribute(context.getReader(), "node1");
        String voltageLevelId1 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "voltageLevelId1"));
        Integer node2 = XmlUtil.readOptionalIntegerAttribute(context.getReader(), "node2");
        String voltageLevelId2 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "voltageLevelId2"));
        if (node1 != null) {
            adder.setNode1(node1);
        }
        adder.setVoltageLevel1(voltageLevelId1);
        if (node2 != null) {
            adder.setNode2(node2);
        }
        adder.setVoltageLevel2(voltageLevelId2);
    }

    protected static void readNodeOrBus(int index, LegAdder adder, NetworkXmlReaderContext context) {
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_2, context, () -> {
            String bus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS + index));
            String connectableBus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, CONNECTABLE_BUS + index));
            if (bus != null) {
                adder.setBus(bus);
            }
            if (connectableBus != null) {
                adder.setConnectableBus(connectableBus);
            }
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
            String busId = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS_ID + index));
            String connectionStatus = context.getReader().getAttributeValue(null, CONNECTION_STATUS + index);
            if (busId != null && connectionStatus != null) {
                if (connectionStatus.contains("CONNECTED")) {
                    adder.setBus(busId);
                }
                if (connectionStatus.contains("CONNECTABLE")) {
                    adder.setConnectableBus(busId);
                }
            }
        });
        Integer node = XmlUtil.readOptionalIntegerAttribute(context.getReader(), NODE + index);
        String voltageLevelId = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "voltageLevelId" + index));
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

    /**
     * @deprecated Use {@link #writeCurrentLimits(Integer, CurrentLimits, XMLStreamWriter, IidmXmlVersion, ExportOptions)} instead.
     */
    @Deprecated
    public static void writeCurrentLimits(Integer index, CurrentLimits limits, XMLStreamWriter writer, IidmXmlVersion version) throws XMLStreamException {
        writeCurrentLimits(index, limits, writer, version, new ExportOptions());
    }

    public static void writeCurrentLimits(Integer index, CurrentLimits limits, XMLStreamWriter writer, IidmXmlVersion version,
                                          ExportOptions exportOptions) throws XMLStreamException {
        writeCurrentLimits(index, limits, writer, version.getNamespaceURI(), version, exportOptions);
    }

    /**
     * @deprecated Use {@link #writeCurrentLimits(Integer, CurrentLimits, XMLStreamWriter, String, IidmXmlVersion, ExportOptions)} instead.
     */
    @Deprecated
    public static void writeCurrentLimits(Integer index, CurrentLimits limits, XMLStreamWriter writer, String nsUri, IidmXmlVersion version) throws XMLStreamException {
        writeCurrentLimits(index, limits, writer, nsUri, version, new ExportOptions());
    }

    public static void writeCurrentLimits(Integer index, CurrentLimits limits, XMLStreamWriter writer, String nsUri, IidmXmlVersion version,
                                          ExportOptions exportOptions) throws XMLStreamException {
        if (!Double.isNaN(limits.getPermanentLimit())
                || !limits.getTemporaryLimits().isEmpty()) {
            if (limits.getTemporaryLimits().isEmpty()) {
                writer.writeEmptyElement(nsUri, CURRENT_LIMITS + indexToString(index));
            } else {
                writer.writeStartElement(nsUri, CURRENT_LIMITS + indexToString(index));
            }
            XmlUtil.writeDouble("permanentLimit", limits.getPermanentLimit(), writer);
            for (CurrentLimits.TemporaryLimit tl : IidmXmlUtil.sortedTemporaryLimits(limits.getTemporaryLimits(), exportOptions)) {
                writer.writeEmptyElement(version.getNamespaceURI(), "temporaryLimit");
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

    /**
     * @deprecated Use {@link TerminalRefXml#writeTerminalRef(Terminal, NetworkXmlWriterContext, String)} instead.
     */
    @Deprecated
    protected static void writeTerminalRef(Terminal t, NetworkXmlWriterContext context, String elementName) throws XMLStreamException {
        TerminalRefXml.writeTerminalRef(t, context, elementName);
    }

    /**
     * @deprecated Use {@link TerminalRefXml#readTerminalRef(Network, String, String)} instead.
     */
    @Deprecated
    protected static Terminal readTerminalRef(Network network, String id, String side) {
        return TerminalRefXml.readTerminalRef(network, id, side);
    }
}
