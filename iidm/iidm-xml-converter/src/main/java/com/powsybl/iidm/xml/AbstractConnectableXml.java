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
    private static final String NODE = "node";

    static final String ACTIVE_POWER_LIMITS = "activePowerLimits";
    static final String APPARENT_POWER_LIMITS = "apparentPowerLimits";
    static final String ACTIVE_POWER_LIMITS_1 = "activePowerLimits1";
    static final String ACTIVE_POWER_LIMITS_2 = "activePowerLimits2";
    static final String APPARENT_POWER_LIMITS_1 = "apparentPowerLimits1";
    static final String APPARENT_POWER_LIMITS_2 = "apparentPowerLimits2";
    static final String ACTIVE_POWER_LIMITS_3 = "activePowerLimits3";
    static final String APPARENT_POWER_LIMITS_3 = "apparentPowerLimits3";

    private static final String CURRENT_LIMITS = "currentLimits";

    private static String indexToString(Integer index) {
        return index != null ? index.toString() : "";
    }

    protected static boolean hasValidOperationalLimits(Branch<?> branch, NetworkXmlWriterContext context) {
        if (context.getVersion().compareTo(IidmXmlVersion.V_1_5) > 0) {
            return !branch.getOperationalLimits1().isEmpty() || !branch.getOperationalLimits2().isEmpty();
        }
        return branch.getCurrentLimits1() != null || branch.getCurrentLimits2() != null;
    }

    protected static boolean hasValidOperationalLimits(FlowsLimitsHolder limitsHolder, NetworkXmlWriterContext context) {
        if (context.getVersion().compareTo(IidmXmlVersion.V_1_5) > 0) {
            return !limitsHolder.getOperationalLimits().isEmpty();
        }
        return limitsHolder.getCurrentLimits() != null;
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

    public static void readActivePowerLimits(Integer index, Supplier<ActivePowerLimitsAdder> activePowerLimitsOwner, XMLStreamReader reader) throws XMLStreamException {
        readLoadingLimits(index, ACTIVE_POWER_LIMITS, activePowerLimitsOwner, reader);
    }

    public static void readApparentPowerLimits(Integer index, Supplier<ApparentPowerLimitsAdder> apparentPowerLimitsOwner, XMLStreamReader reader) throws XMLStreamException {
        readLoadingLimits(index, APPARENT_POWER_LIMITS, apparentPowerLimitsOwner, reader);
    }

    public static void readCurrentLimits(Integer index, Supplier<CurrentLimitsAdder> currentLimitOwner, XMLStreamReader reader) throws XMLStreamException {
        readLoadingLimits(index, CURRENT_LIMITS, currentLimitOwner, reader);
    }

    private static <A extends LoadingLimitsAdder> void readLoadingLimits(Integer index, String type, Supplier<A> limitOwner, XMLStreamReader reader) throws XMLStreamException {
        A adder = limitOwner.get();
        double permanentLimit = XmlUtil.readOptionalDoubleAttribute(reader, "permanentLimit");
        adder.setPermanentLimit(permanentLimit);
        XmlUtil.readUntilEndElement(type + indexToString(index), reader, () -> {
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

    static void writeActivePowerLimits(Integer index, ActivePowerLimits limits, XMLStreamWriter writer, IidmXmlVersion version,
                                              ExportOptions exportOptions) throws XMLStreamException {
        writeLoadingLimits(index, limits, writer, version.getNamespaceURI(), version, exportOptions, ACTIVE_POWER_LIMITS);
    }

    static void writeApparentPowerLimits(Integer index, ApparentPowerLimits limits, XMLStreamWriter writer, IidmXmlVersion version,
                                              ExportOptions exportOptions) throws XMLStreamException {
        writeLoadingLimits(index, limits, writer, version.getNamespaceURI(), version, exportOptions, APPARENT_POWER_LIMITS);
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
        writeLoadingLimits(index, limits, writer, nsUri, version, exportOptions, CURRENT_LIMITS);
    }

    private static <L extends LoadingLimits> void writeLoadingLimits(Integer index, L limits, XMLStreamWriter writer, String nsUri, IidmXmlVersion version,
                                           ExportOptions exportOptions, String type) throws XMLStreamException {
        if (!Double.isNaN(limits.getPermanentLimit())
                || !limits.getTemporaryLimits().isEmpty()) {
            if (limits.getTemporaryLimits().isEmpty()) {
                writer.writeEmptyElement(nsUri, type + indexToString(index));
            } else {
                writer.writeStartElement(nsUri, type + indexToString(index));
            }
            XmlUtil.writeDouble("permanentLimit", limits.getPermanentLimit(), writer);
            for (LoadingLimits.TemporaryLimit tl : IidmXmlUtil.sortedTemporaryLimits(limits.getTemporaryLimits(), exportOptions)) {
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
