/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlReader;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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
        if (context.getVersion().compareTo(IidmXmlVersion.V_1_5) >= 0) {
            return !branch.getOperationalLimits1().isEmpty() || !branch.getOperationalLimits2().isEmpty();
        }
        return branch.getCurrentLimits1().isPresent() || branch.getCurrentLimits2().isPresent();
    }

    protected static boolean hasValidOperationalLimits(FlowsLimitsHolder limitsHolder, NetworkXmlWriterContext context) {
        if (context.getVersion().compareTo(IidmXmlVersion.V_1_5) >= 0) {
            return !limitsHolder.getOperationalLimits().isEmpty();
        }
        return limitsHolder.getCurrentLimits().isPresent();
    }

    protected static void writeNodeOrBus(Integer index, Terminal t, NetworkXmlWriterContext context) {
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
            context.getWriter().writeStringAttribute("voltageLevelId" + index, context.getAnonymizer().anonymizeString(t.getVoltageLevel().getId()));
        }
    }

    private static void writeNode(Integer index, Terminal t, NetworkXmlWriterContext context) {
        context.getWriter().writeStringAttribute(NODE + indexToString(index),
                Integer.toString(t.getNodeBreakerView().getNode()));
    }

    private static void writeBus(Integer index, Bus bus, Bus connectableBus, NetworkXmlWriterContext context) {
        if (bus != null) {
            context.getWriter().writeStringAttribute(BUS + indexToString(index), context.getAnonymizer().anonymizeString(bus.getId()));
        }
        if (connectableBus != null) {
            context.getWriter().writeStringAttribute(CONNECTABLE_BUS + indexToString(index), context.getAnonymizer().anonymizeString(connectableBus.getId()));
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

    protected static void writePQ(Integer index, Terminal t, XmlWriter writer) {
        writer.writeDoubleAttribute("p" + indexToString(index), t.getP(), Double.NaN);
        writer.writeDoubleAttribute("q" + indexToString(index), t.getQ(), Double.NaN);
    }

    protected static void readPQ(Integer index, Terminal t, XmlReader reader) {
        double p = reader.readDoubleAttribute("p" + indexToString(index));
        double q = reader.readDoubleAttribute("q" + indexToString(index));
        t.setP(p)
                .setQ(q);
    }

    public static void readActivePowerLimits(Integer index, ActivePowerLimitsAdder activePowerLimitsAdder, XmlReader reader) throws XMLStreamException {
        readLoadingLimits(index, ACTIVE_POWER_LIMITS, activePowerLimitsAdder, reader);
    }

    public static void readApparentPowerLimits(Integer index, ApparentPowerLimitsAdder apparentPowerLimitsAdder, XmlReader reader) throws XMLStreamException {
        readLoadingLimits(index, APPARENT_POWER_LIMITS, apparentPowerLimitsAdder, reader);
    }

    public static void readCurrentLimits(Integer index, CurrentLimitsAdder currentLimitsAdder, XmlReader reader) throws XMLStreamException {
        readLoadingLimits(index, CURRENT_LIMITS, currentLimitsAdder, reader);
    }

    private static <A extends LoadingLimitsAdder> void readLoadingLimits(Integer index, String type, A adder, XmlReader reader) throws XMLStreamException {
        double permanentLimit = reader.readDoubleAttribute("permanentLimit");
        adder.setPermanentLimit(permanentLimit);
        XmlUtil.readUntilEndElement(type + indexToString(index), reader, () -> {
            if ("temporaryLimit".equals(reader.getElementName())) {
                String name = reader.readStringAttribute("name");
                int acceptableDuration = reader.readIntAttribute("acceptableDuration", Integer.MAX_VALUE);
                double value = reader.readDoubleAttribute("value", Double.MAX_VALUE);
                boolean fictitious = reader.readBooleanAttribute("fictitious", false);
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

    static void writeActivePowerLimits(Integer index, ActivePowerLimits limits, XmlWriter writer, IidmXmlVersion version,
                                              boolean valid, ExportOptions exportOptions) {
        writeLoadingLimits(index, limits, writer, version.getNamespaceURI(valid), version, valid, exportOptions, ACTIVE_POWER_LIMITS);
    }

    static void writeApparentPowerLimits(Integer index, ApparentPowerLimits limits, XmlWriter writer, IidmXmlVersion version,
                                              boolean valid, ExportOptions exportOptions) {
        writeLoadingLimits(index, limits, writer, version.getNamespaceURI(valid), version, valid, exportOptions, APPARENT_POWER_LIMITS);
    }

    public static void writeCurrentLimits(Integer index, CurrentLimits limits, XmlWriter writer, IidmXmlVersion version,
                                          ExportOptions exportOptions) {
        writeCurrentLimits(index, limits, writer, version, true, exportOptions);
    }

    public static void writeCurrentLimits(Integer index, CurrentLimits limits, XmlWriter writer, IidmXmlVersion version,
                                          boolean valid, ExportOptions exportOptions) {
        writeCurrentLimits(index, limits, writer, version.getNamespaceURI(valid), version, valid, exportOptions);
    }

    public static void writeCurrentLimits(Integer index, CurrentLimits limits, XmlWriter writer, String nsUri, IidmXmlVersion version,
                                          ExportOptions exportOptions) {
        writeLoadingLimits(index, limits, writer, nsUri, version, true, exportOptions, CURRENT_LIMITS);
    }

    public static void writeCurrentLimits(Integer index, CurrentLimits limits, XmlWriter writer, String nsUri, IidmXmlVersion version,
                                          boolean valid, ExportOptions exportOptions) {
        writeLoadingLimits(index, limits, writer, nsUri, version, valid, exportOptions, CURRENT_LIMITS);
    }

    private static <L extends LoadingLimits> void writeLoadingLimits(Integer index, L limits, XmlWriter writer, String nsUri, IidmXmlVersion version,
                                           boolean valid, ExportOptions exportOptions, String type) {
        if (!Double.isNaN(limits.getPermanentLimit())
                || !limits.getTemporaryLimits().isEmpty()) {
            if (limits.getTemporaryLimits().isEmpty()) {
                writer.writeEmptyElement(nsUri, type + indexToString(index));
            } else {
                writer.writeStartElement(nsUri, type + indexToString(index));
            }
            writer.writeDoubleAttribute("permanentLimit", limits.getPermanentLimit());
            for (LoadingLimits.TemporaryLimit tl : IidmXmlUtil.sortedTemporaryLimits(limits.getTemporaryLimits(), exportOptions)) {
                writer.writeEmptyElement(version.getNamespaceURI(valid), "temporaryLimit");
                writer.writeStringAttribute("name", tl.getName());
                writer.writeIntAttribute("acceptableDuration", tl.getAcceptableDuration(), Integer.MAX_VALUE);
                writer.writeDoubleAttribute("value", tl.getValue(), Double.MAX_VALUE);
                writer.writeBooleanAttribute("fictitious", tl.isFictitious(), false);
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
