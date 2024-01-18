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

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

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
        context.getWriter().writeStringAttribute(BUS + indexToString(index),
                Optional.ofNullable(bus).map(b -> context.getAnonymizer().anonymizeString(b.getId())).orElse(null));
        context.getWriter().writeStringAttribute(CONNECTABLE_BUS + indexToString(index),
                Optional.ofNullable(connectableBus).map(b -> context.getAnonymizer().anonymizeString(b.getId())).orElse(null));
    }

    public static void readNodeOrBus(InjectionAdder<?, ?> adder, NetworkDeserializerContext context, TopologyKind topologyKind) {
        readNodeOrBus(adder, "", context, topologyKind);
    }

    public static void readNodeOrBus(InjectionAdder<?, ?> adder, String suffix, NetworkDeserializerContext context, TopologyKind topologyKind) {
        readNodeOrBus(suffix, topologyKind, adder::setNode, adder::setBus, adder::setConnectableBus, context);
    }

    private static void readNode(IntConsumer nodeAdder, String suffix, NetworkDeserializerContext context) {
        nodeAdder.accept(context.getReader().readIntAttribute(NODE + suffix));
    }

    private static void readBus(Consumer<String> busAdder, String suffix, NetworkDeserializerContext context) {
        busAdder.accept(context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute(BUS + suffix)));
    }

    private static void readConnectableBus(Consumer<String> busAdder, String suffix, NetworkDeserializerContext context) {
        busAdder.accept(context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute(CONNECTABLE_BUS + suffix)));
    }

    public static void readVoltageLevelAndNodeOrBus(BranchAdder<?, ?> adder, Network network, NetworkDeserializerContext context) {
        readVoltageLevelAndNodeOrBus("1", adder::setVoltageLevel1, adder::setNode1, adder::setBus1, adder::setConnectableBus1, network, context);
        readVoltageLevelAndNodeOrBus("2", adder::setVoltageLevel2, adder::setNode2, adder::setBus2, adder::setConnectableBus2, network, context);
    }

    private static void readVoltageLevelAndNodeOrBus(String suffix, Consumer<String> voltageLevelSetter, IntConsumer nodeSetter, Consumer<String> busSetter, Consumer<String> connectableBusSetter, Network network, NetworkDeserializerContext context) {
        String voltageLevelId = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("voltageLevelId" + suffix));
        voltageLevelSetter.accept(voltageLevelId);
        readNodeOrBus(suffix, getTopologKind(voltageLevelId, network), nodeSetter, busSetter, connectableBusSetter, context);
    }

    private static void readNodeOrBus(String suffix, TopologyKind topologyKind, IntConsumer nodeSetter, Consumer<String> busSetter, Consumer<String> connectableBusSetter, NetworkDeserializerContext context) {
        switch (topologyKind) {
            case NODE_BREAKER -> readNode(nodeSetter, suffix, context);
            case BUS_BREAKER -> {
                readBus(busSetter, suffix, context);
                readConnectableBus(connectableBusSetter, suffix, context);
            }
            default -> throw new IllegalStateException();
        }
    }

    private static TopologyKind getTopologKind(String vlId, Network network) {
        VoltageLevel vl = network.getVoltageLevel(vlId);
        if (vl == null) {
            throw new PowsyblException("Voltage level '" + vlId + "' not found");
        }
        return vl.getTopologyKind();
    }

    public static void readNodeOrBus(int index, LegAdder adder, Network network, NetworkDeserializerContext context) {
        readVoltageLevelAndNodeOrBus(String.valueOf(index), adder::setVoltageLevel, adder::setNode, adder::setBus, adder::setConnectableBus, network, context);
    }

    public static void writePQ(Integer index, Terminal t, TreeDataWriter writer) {
        writer.writeDoubleAttribute("p" + indexToString(index), t.getP());
        writer.writeDoubleAttribute("q" + indexToString(index), t.getQ());
    }

    public static void writeOptionalPQ(Integer index, Terminal t, TreeDataWriter writer, BooleanSupplier write) {
        Double nullableP = write.getAsBoolean() ? t.getP() : null;
        Double nullableQ = write.getAsBoolean() ? t.getQ() : null;
        writer.writeOptionalDoubleAttribute("p" + indexToString(index), nullableP);
        writer.writeOptionalDoubleAttribute("q" + indexToString(index), nullableQ);
    }

    public static void readPQ(Integer index, Terminal t, TreeDataReader reader) {
        double p = reader.readDoubleAttribute("p" + indexToString(index));
        double q = reader.readDoubleAttribute("q" + indexToString(index));
        t.setP(p)
                .setQ(q);
    }

    public static void readOptionalPQ(Integer index, Terminal t, TreeDataReader reader) {
        reader.readOptionalDoubleAttribute("p" + indexToString(index))
                .ifPresent(t::setP);
        reader.readOptionalDoubleAttribute("q" + indexToString(index))
                .ifPresent(t::setQ);
    }

    public static void readActivePowerLimits(ActivePowerLimitsAdder activePowerLimitsAdder, TreeDataReader reader, IidmVersion iidmVersion, ImportOptions options) {
        readLoadingLimits(ACTIVE_POWER_LIMITS, activePowerLimitsAdder, reader, iidmVersion, options);
    }

    public static void readApparentPowerLimits(ApparentPowerLimitsAdder apparentPowerLimitsAdder, TreeDataReader reader, IidmVersion iidmVersion, ImportOptions options) {
        readLoadingLimits(APPARENT_POWER_LIMITS, apparentPowerLimitsAdder, reader, iidmVersion, options);
    }

    public static void readCurrentLimits(CurrentLimitsAdder currentLimitsAdder, TreeDataReader reader, IidmVersion iidmVersion, ImportOptions options) {
        readLoadingLimits(CURRENT_LIMITS, currentLimitsAdder, reader, iidmVersion, options);
    }

    private static <L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>> void readLoadingLimits(String type, A adder, TreeDataReader reader, IidmVersion iidmVersion, ImportOptions options) {
        double permanentLimit = reader.readDoubleAttribute("permanentLimit");
        if (Double.isNaN(permanentLimit) && iidmVersion.compareTo(IidmVersion.V_1_12) >= 0) {
            throw new PowsyblException("permanentLimit is absent in '" + type + "'");
        }
        adder.setPermanentLimit(permanentLimit);
        // Read and add the temporary limits
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
        adder.fixLimits(options.getMissingPermanentLimitPercentage()).add();
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
        if (limits != null && (!Double.isNaN(limits.getPermanentLimit()) || !limits.getTemporaryLimits().isEmpty())) {
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
