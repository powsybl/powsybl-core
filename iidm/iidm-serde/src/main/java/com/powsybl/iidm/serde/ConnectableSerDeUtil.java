/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class ConnectableSerDeUtil {

    static final String TEMPORARY_LIMITS_ARRAY_ELEMENT_NAME = "temporaryLimits";
    static final String TEMPORARY_LIMITS_ROOT_ELEMENT_NAME = "temporaryLimit";

    private ConnectableSerDeUtil() {
    }

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

    static final String CURRENT_LIMITS = "currentLimits";

    private static String indexToString(Integer index) {
        return index != null ? index.toString() : "";
    }

    public static boolean hasValidOperationalLimits(Branch<?> branch, NetworkSerializerContext context) {
        if (context.getVersion().compareTo(IidmVersion.V_1_5) >= 0) {
            return !branch.getOperationalLimits1().isEmpty() || !branch.getOperationalLimits2().isEmpty();
        }
        return branch.getCurrentLimits1().isPresent() || branch.getCurrentLimits2().isPresent();
    }

    public static boolean hasValidOperationalLimits(FlowsLimitsHolder limitsHolder, NetworkSerializerContext context) {
        if (context.getVersion().compareTo(IidmVersion.V_1_5) >= 0) {
            return !limitsHolder.getOperationalLimits().isEmpty();
        }
        return limitsHolder.getCurrentLimits().isPresent();
    }

    public static void writeNodeOrBus(Integer index, Terminal t, NetworkSerializerContext context) {
        if (index != null) {
            context.getWriter().writeStringAttribute("voltageLevelId" + index, context.getAnonymizer().anonymizeString(t.getVoltageLevel().getId()));
        }
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
                throw new IllegalStateException("Unexpected TopologyLevel value: " + topologyLevel);
        }
    }

    private static void writeNode(Integer index, Terminal t, NetworkSerializerContext context) {
        context.getWriter().writeIntAttribute(NODE + indexToString(index), t.getNodeBreakerView().getNode());
    }

    private static void writeBus(Integer index, Bus bus, Bus connectableBus, NetworkSerializerContext context) {
        if (bus != null) {
            context.getWriter().writeStringAttribute(BUS + indexToString(index), context.getAnonymizer().anonymizeString(bus.getId()));
        }
        if (connectableBus != null) {
            context.getWriter().writeStringAttribute(CONNECTABLE_BUS + indexToString(index), context.getAnonymizer().anonymizeString(connectableBus.getId()));
        }
    }

    public static void readNodeOrBus(InjectionAdder<?, ?> adder, NetworkDeserializerContext context) {
        readNodeOrBus(adder, "", context);
    }

    public static void readNodeOrBus(InjectionAdder<?, ?> adder, String suffix, NetworkDeserializerContext context) {
        String bus = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute(BUS + suffix));
        String connectableBus = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute(CONNECTABLE_BUS + suffix));
        Integer node = context.getReader().readIntAttribute(NODE + suffix);
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

    public static void readNodeOrBus(BranchAdder<?, ?> adder, NetworkDeserializerContext context) {
        String voltageLevelId1 = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("voltageLevelId1"));
        String bus1 = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("bus1"));
        String connectableBus1 = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("connectableBus1"));
        Integer node1 = context.getReader().readIntAttribute("node1");
        String voltageLevelId2 = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("voltageLevelId2"));
        String bus2 = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("bus2"));
        String connectableBus2 = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("connectableBus2"));
        Integer node2 = context.getReader().readIntAttribute("node2");
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

    public static void readNodeOrBus(int index, LegAdder adder, NetworkDeserializerContext context) {
        String voltageLevelId = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("voltageLevelId" + index));
        String bus = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute(BUS + index));
        String connectableBus = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute(CONNECTABLE_BUS + index));
        Integer node = context.getReader().readIntAttribute(NODE + index);
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

    public static void writePQ(Integer index, Terminal t, TreeDataWriter writer) {
        writer.writeDoubleAttribute("p" + indexToString(index), t.getP());
        writer.writeDoubleAttribute("q" + indexToString(index), t.getQ());
    }

    public static void readPQ(Integer index, Terminal t, TreeDataReader reader) {
        double p = reader.readDoubleAttribute("p" + indexToString(index));
        double q = reader.readDoubleAttribute("q" + indexToString(index));
        t.setP(p)
                .setQ(q);
    }

    public static void readActivePowerLimits(ActivePowerLimitsAdder activePowerLimitsAdder, TreeDataReader reader) {
        readLoadingLimits(ACTIVE_POWER_LIMITS, activePowerLimitsAdder, reader);
    }

    public static void readApparentPowerLimits(ApparentPowerLimitsAdder apparentPowerLimitsAdder, TreeDataReader reader) {
        readLoadingLimits(APPARENT_POWER_LIMITS, apparentPowerLimitsAdder, reader);
    }

    public static void readCurrentLimits(CurrentLimitsAdder currentLimitsAdder, TreeDataReader reader) {
        readLoadingLimits(CURRENT_LIMITS, currentLimitsAdder, reader);
    }

    private static <A extends LoadingLimitsAdder> void readLoadingLimits(String type, A adder, TreeDataReader reader) {
        double permanentLimit = reader.readDoubleAttribute("permanentLimit");
        adder.setPermanentLimit(permanentLimit);
        reader.readChildNodes(elementName -> {
            if (TEMPORARY_LIMITS_ROOT_ELEMENT_NAME.equals(elementName)) {
                String name = reader.readStringAttribute("name");
                int acceptableDuration = reader.readIntAttribute("acceptableDuration", Integer.MAX_VALUE);
                double value = reader.readDoubleAttribute("value", Double.MAX_VALUE);
                boolean fictitious = reader.readBooleanAttribute("fictitious", false);
                reader.readEndNode();
                adder.beginTemporaryLimit()
                        .setName(name)
                        .setAcceptableDuration(acceptableDuration)
                        .setValue(value)
                        .setFictitious(fictitious)
                        .endTemporaryLimit();
            } else {
                throw new PowsyblException("Unknown element name '" + elementName + "' in '" + type + "'");
            }
        });
        adder.add();
    }

    static void writeActivePowerLimits(Integer index, ActivePowerLimits limits, TreeDataWriter writer, IidmVersion version,
                                              boolean valid, ExportOptions exportOptions) {
        writeLoadingLimits(index, limits, writer, version.getNamespaceURI(valid), version, valid, exportOptions, ACTIVE_POWER_LIMITS);
    }

    static void writeApparentPowerLimits(Integer index, ApparentPowerLimits limits, TreeDataWriter writer, IidmVersion version,
                                              boolean valid, ExportOptions exportOptions) {
        writeLoadingLimits(index, limits, writer, version.getNamespaceURI(valid), version, valid, exportOptions, APPARENT_POWER_LIMITS);
    }

    public static void writeCurrentLimits(Integer index, CurrentLimits limits, TreeDataWriter writer, IidmVersion version,
                                          ExportOptions exportOptions) {
        writeCurrentLimits(index, limits, writer, version, true, exportOptions);
    }

    public static void writeCurrentLimits(Integer index, CurrentLimits limits, TreeDataWriter writer, IidmVersion version,
                                          boolean valid, ExportOptions exportOptions) {
        writeCurrentLimits(index, limits, writer, version.getNamespaceURI(valid), version, valid, exportOptions);
    }

    public static void writeCurrentLimits(Integer index, CurrentLimits limits, TreeDataWriter writer, String nsUri, IidmVersion version,
                                          ExportOptions exportOptions) {
        writeLoadingLimits(index, limits, writer, nsUri, version, true, exportOptions, CURRENT_LIMITS);
    }

    public static void writeCurrentLimits(Integer index, CurrentLimits limits, TreeDataWriter writer, String nsUri, IidmVersion version,
                                          boolean valid, ExportOptions exportOptions) {
        writeLoadingLimits(index, limits, writer, nsUri, version, valid, exportOptions, CURRENT_LIMITS);
    }

    private static <L extends LoadingLimits> void writeLoadingLimits(Integer index, L limits, TreeDataWriter writer, String nsUri, IidmVersion version,
                                           boolean valid, ExportOptions exportOptions, String type) {
        if (!Double.isNaN(limits.getPermanentLimit())
                || !limits.getTemporaryLimits().isEmpty()) {
            writer.writeStartNode(nsUri, type + indexToString(index));
            writer.writeDoubleAttribute("permanentLimit", limits.getPermanentLimit());
            writer.writeStartNodes();
            for (LoadingLimits.TemporaryLimit tl : IidmSerDeUtil.sortedTemporaryLimits(limits.getTemporaryLimits(), exportOptions)) {
                writer.writeStartNode(version.getNamespaceURI(valid), TEMPORARY_LIMITS_ROOT_ELEMENT_NAME);
                writer.writeStringAttribute("name", tl.getName());
                writer.writeIntAttribute("acceptableDuration", tl.getAcceptableDuration(), Integer.MAX_VALUE);
                writer.writeDoubleAttribute("value", tl.getValue(), Double.MAX_VALUE);
                writer.writeBooleanAttribute("fictitious", tl.isFictitious(), false);
                writer.writeEndNode();
            }
            writer.writeEndNodes();
            writer.writeEndNode();
        }
    }
}
