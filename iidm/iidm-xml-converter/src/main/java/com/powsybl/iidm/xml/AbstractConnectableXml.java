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
    private static final String BUS_ID = "busId";
    private static final String CONNECTABLE_BUS = "connectableBus";
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
                writeBus(index, t.getBusBreakerView().getBus(), t.getBusBreakerView().getConnectionStatus(), context);
                break;
            case BUS_BRANCH:
                writeBus(index, t.getBusView().getBus(), t.getBusView().getConnectionStatus(), context);
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

    private static void writeBus(Integer index, Bus bus, Terminal.ConnectionStatus connectionStatus, NetworkXmlWriterContext context) {
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_3, context, () -> {
            if (bus != null) {
                if (Terminal.ConnectionStatus.CONNECTED.equals(connectionStatus)) {
                    context.getWriter().writeAttribute(BUS + indexToString(index), context.getAnonymizer().anonymizeString(bus.getId()));
                    context.getWriter().writeAttribute(CONNECTABLE_BUS + indexToString(index), context.getAnonymizer().anonymizeString(bus.getId()));
                } else {
                    context.getWriter().writeAttribute(CONNECTABLE_BUS + indexToString(index), context.getAnonymizer().anonymizeString(bus.getId()));
                }
            }
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_4, context, () -> {
            if (bus != null) {
                context.getWriter().writeAttribute(BUS_ID + indexToString(index), context.getAnonymizer().anonymizeString(bus.getId()));
            }
            if (connectionStatus != null) {
                context.getWriter().writeAttribute(CONNECTION_STATUS + indexToString(index), connectionStatus.toString());
            }
        });
    }

    protected static void readNodeOrBus(InjectionAdder adder, NetworkXmlReaderContext context) {
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_3, context, () -> {
            String bus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS));
            String connectableBus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, CONNECTABLE_BUS));
            if (bus != null) {
                adder.setBus(bus);
                adder.setConnectionStatus(Terminal.ConnectionStatus.CONNECTED);
            } else {
                if (connectableBus != null) {
                    adder.setBus(connectableBus);
                    adder.setConnectionStatus(Terminal.ConnectionStatus.CONNECTABLE);
                }
            }
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_4, context, () -> {
            String busId = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS_ID));
            String status = context.getReader().getAttributeValue(null, CONNECTION_STATUS);
            if (busId != null) {
                adder.setBus(busId);
                if (status != null) {
                    if (Terminal.ConnectionStatus.CONNECTED.toString().equals(status)) {
                        adder.setConnectionStatus(Terminal.ConnectionStatus.CONNECTED);
                    } else {
                        adder.setConnectionStatus(Terminal.ConnectionStatus.CONNECTABLE);
                    }
                }
            }
        });
        Integer node = XmlUtil.readOptionalIntegerAttribute(context.getReader(), NODE);
        if (node != null) {
            adder.setNode(node);
        }

    }

    protected static void readNodeOrBus(BranchAdder adder, NetworkXmlReaderContext context) {
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_3, context, () -> {
            String bus1 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS + "1"));
            String connectableBus1 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, CONNECTABLE_BUS + "1"));
            if (bus1 != null) {
                adder.setBus1(bus1);
                adder.setConnectionStatus1(Terminal.ConnectionStatus.CONNECTED);
            } else {
                if (connectableBus1 != null) {
                    adder.setBus1(connectableBus1);
                    adder.setConnectionStatus1(Terminal.ConnectionStatus.CONNECTABLE);
                }
            }
            String bus2 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS + "2"));
            String connectableBus2 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, CONNECTABLE_BUS + "2"));
            if (bus2 != null) {
                adder.setBus2(bus2);
                adder.setConnectionStatus2(Terminal.ConnectionStatus.CONNECTED);
            } else {
                if (connectableBus2 != null) {
                    adder.setBus2(connectableBus2);
                    adder.setConnectionStatus2(Terminal.ConnectionStatus.CONNECTABLE);
                }
            }
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_4, context, () -> {
            String busId1 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS_ID + "1"));
            String status1 = context.getReader().getAttributeValue(null, CONNECTION_STATUS + "1");
            if (busId1 != null) {
                adder.setBus1(busId1);
                if (status1 != null) {
                    if (Terminal.ConnectionStatus.CONNECTED.toString().equals(status1)) {
                        adder.setConnectionStatus1(Terminal.ConnectionStatus.CONNECTED);
                    } else {
                        adder.setConnectionStatus1(Terminal.ConnectionStatus.CONNECTABLE);
                    }
                }
            }
            String busId2 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS_ID + "2"));
            String status2 = context.getReader().getAttributeValue(null, CONNECTION_STATUS + "2");
            if (busId2 != null) {
                adder.setBus2(busId2);
                if (status2 != null) {
                    if (Terminal.ConnectionStatus.CONNECTED.toString().equals(status2)) {
                        adder.setConnectionStatus2(Terminal.ConnectionStatus.CONNECTED);
                    } else {
                        adder.setConnectionStatus2(Terminal.ConnectionStatus.CONNECTABLE);
                    }
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
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_3, context, () -> {
            String bus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS + index));
            String connectableBus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, CONNECTABLE_BUS + index));
            if (bus != null) {
                adder.setBus(bus);
                adder.setConnectionStatus(Terminal.ConnectionStatus.CONNECTED);
            } else {
                if (connectableBus != null) {
                    adder.setBus(connectableBus);
                    adder.setConnectionStatus(Terminal.ConnectionStatus.CONNECTABLE);
                }
            }
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_4, context, () -> {
            String busId = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, BUS_ID + index));
            String status = context.getReader().getAttributeValue(null, CONNECTION_STATUS + index);
            if (busId != null) {
                adder.setBus(busId);
                if (status != null) {
                    if (Terminal.ConnectionStatus.CONNECTED.toString().equals(status)) {
                        adder.setConnectionStatus(Terminal.ConnectionStatus.CONNECTED);
                    } else {
                        adder.setConnectionStatus(Terminal.ConnectionStatus.CONNECTABLE);
                    }
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
