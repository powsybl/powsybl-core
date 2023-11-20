/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.iidm.serializer.util.IidmSerializerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.powsybl.iidm.serializer.PropertiesSerializer.NAME;
import static com.powsybl.iidm.serializer.PropertiesSerializer.VALUE;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class VoltageLevelSerializer extends AbstractSimpleIdentifiableSerializer<VoltageLevel, VoltageLevelAdder, Container<? extends Identifiable<?>>> {

    static final VoltageLevelSerializer INSTANCE = new VoltageLevelSerializer();

    static final String ROOT_ELEMENT_NAME = "voltageLevel";
    static final String ARRAY_ELEMENT_NAME = "voltageLevels";

    private static final Logger LOGGER = LoggerFactory.getLogger(VoltageLevelSerializer.class);

    private static final String NODE_BREAKER_TOPOLOGY_ELEMENT_NAME = "nodeBreakerTopology";
    private static final String BUS_BREAKER_TOPOLOGY_ELEMENT_NAME = "busBreakerTopology";
    private static final String NODE_COUNT = "nodeCount";
    static final String INJ_ROOT_ELEMENT_NAME = "inj";
    static final String INJ_ARRAY_ELEMENT_NAME = "fictitiousInjections";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(VoltageLevel vl, Container<? extends Identifiable<?>> c, NetworkSerializerWriterContext context) {
        context.getWriter().writeDoubleAttribute("nominalV", vl.getNominalV());
        context.getWriter().writeDoubleAttribute("lowVoltageLimit", vl.getLowVoltageLimit());
        context.getWriter().writeDoubleAttribute("highVoltageLimit", vl.getHighVoltageLimit());

        TopologyLevel topologyLevel = TopologyLevel.min(vl.getTopologyKind(), context.getOptions().getTopologyLevel());
        context.getWriter().writeStringAttribute("topologyKind", topologyLevel.getTopologyKind().name());
    }

    @Override
    protected void writeSubElements(VoltageLevel vl, Container<? extends Identifiable<?>> c, NetworkSerializerWriterContext context) {
        TopologyLevel topologyLevel = TopologyLevel.min(vl.getTopologyKind(), context.getOptions().getTopologyLevel());
        switch (topologyLevel) {
            case NODE_BREAKER:
                writeNodeBreakerTopology(vl, context);
                break;

            case BUS_BREAKER:
                writeBusBreakerTopology(vl, context);
                break;

            case BUS_BRANCH:
                writeBusBranchTopology(vl, context);
                break;

            default:
                throw new IllegalStateException("Unexpected TopologyLevel value: " + topologyLevel);
        }

        writeGenerators(vl, context);
        writeBatteries(vl, context);
        writeLoads(vl, context);
        writeShuntCompensators(vl, context);
        writeDanglingLines(vl, context);
        writeStaticVarCompensators(vl, context);
        writeVscConverterStations(vl, context);
        writeLccConverterStations(vl, context);
    }

    private void writeNodeBreakerTopology(VoltageLevel vl, NetworkSerializerWriterContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), NODE_BREAKER_TOPOLOGY_ELEMENT_NAME);
        IidmSerializerUtil.writeIntAttributeUntilMaximumVersion(NODE_COUNT, vl.getNodeBreakerView().getMaximumNodeIndex() + 1, IidmVersion.V_1_1, context);

        context.getWriter().writeStartNodes(BusbarSectionSerializer.ARRAY_ELEMENT_NAME);
        for (BusbarSection bs : IidmSerializerUtil.sorted(vl.getNodeBreakerView().getBusbarSections(), context.getOptions())) {
            BusbarSectionSerializer.INSTANCE.write(bs, null, context);
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeStartNodes(AbstractSwitchSerializer.ARRAY_ELEMENT_NAME);
        for (Switch sw : IidmSerializerUtil.sorted(vl.getNodeBreakerView().getSwitches(), context.getOptions())) {
            NodeBreakerViewSwitchSerializer.INSTANCE.write(sw, vl, context);
        }
        context.getWriter().writeEndNodes();

        writeNodeBreakerTopologyInternalConnections(vl, context);

        IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_1, context, () -> {
            Map<String, Set<Integer>> nodesByBus = Networks.getNodesByBus(vl);
            context.getWriter().writeStartNodes(BusSerializer.ARRAY_ELEMENT_NAME);
            IidmSerializerUtil.sorted(vl.getBusView().getBusStream(), context.getOptions())
                    .filter(bus -> !Double.isNaN(bus.getV()) || !Double.isNaN(bus.getAngle()))
                    .forEach(bus -> {
                        Set<Integer> nodes = nodesByBus.get(bus.getId());
                        writeCalculatedBus(bus, nodes, context);
                    });
            context.getWriter().writeEndNodes();
        });
        IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_8, context, () -> {
            context.getWriter().writeStartNodes(INJ_ARRAY_ELEMENT_NAME);
            for (int node : vl.getNodeBreakerView().getNodes()) {
                double fictP0 = vl.getNodeBreakerView().getFictitiousP0(node);
                double fictQ0 = vl.getNodeBreakerView().getFictitiousQ0(node);
                if (fictP0 != 0.0 || fictQ0 != 0.0) {
                    context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), INJ_ROOT_ELEMENT_NAME);
                    context.getWriter().writeIntAttribute("node", node);
                    context.getWriter().writeDoubleAttribute("fictitiousP0", fictP0, 0.0);
                    context.getWriter().writeDoubleAttribute("fictitiousQ0", fictQ0, 0.0);
                    context.getWriter().writeEndNode();
                }
            }
            context.getWriter().writeEndNodes();
        });
        context.getWriter().writeEndNode();
    }

    private static void writeCalculatedBus(Bus bus, Set<Integer> nodes, NetworkSerializerWriterContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), "bus");
        context.getWriter().writeDoubleAttribute("v", bus.getV());
        context.getWriter().writeDoubleAttribute("angle", bus.getAngle());
        context.getWriter().writeIntArrayAttribute("nodes", nodes);
        if (context.getVersion().compareTo(IidmVersion.V_1_11) >= 0 && bus.hasProperty()) {
            PropertiesSerializer.write(bus, context);
        }
        context.getWriter().writeEndNode();
    }

    private void writeNodeBreakerTopologyInternalConnections(VoltageLevel vl, NetworkSerializerWriterContext context) {
        context.getWriter().writeStartNodes(NodeBreakerViewInternalConnectionSerializer.ARRAY_ELEMENT_NAME);
        for (VoltageLevel.NodeBreakerView.InternalConnection ic : IidmSerializerUtil.sortedInternalConnections(vl.getNodeBreakerView().getInternalConnections(), context.getOptions())) {
            NodeBreakerViewInternalConnectionSerializer.INSTANCE.write(ic.getNode1(), ic.getNode2(), context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeBusBreakerTopology(VoltageLevel vl, NetworkSerializerWriterContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), BUS_BREAKER_TOPOLOGY_ELEMENT_NAME);

        context.getWriter().writeStartNodes(BusSerializer.ARRAY_ELEMENT_NAME);
        for (Bus b : IidmSerializerUtil.sorted(vl.getBusBreakerView().getBuses(), context.getOptions())) {
            if (!context.getFilter().test(b)) {
                continue;
            }
            BusSerializer.INSTANCE.write(b, null, context);
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeStartNodes(AbstractSwitchSerializer.ARRAY_ELEMENT_NAME);
        for (Switch sw : IidmSerializerUtil.sorted(vl.getBusBreakerView().getSwitches(), context.getOptions())) {
            Bus b1 = vl.getBusBreakerView().getBus1(sw.getId());
            Bus b2 = vl.getBusBreakerView().getBus2(sw.getId());
            if (!context.getFilter().test(b1) || !context.getFilter().test(b2)) {
                continue;
            }
            BusBreakerViewSwitchSerializer.INSTANCE.write(sw, vl, context);
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeEndNode();
    }

    private void writeBusBranchTopology(VoltageLevel vl, NetworkSerializerWriterContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), BUS_BREAKER_TOPOLOGY_ELEMENT_NAME);

        context.getWriter().writeStartNodes(BusSerializer.ARRAY_ELEMENT_NAME);
        for (Bus b : IidmSerializerUtil.sorted(vl.getBusView().getBuses(), context.getOptions())) {
            if (!context.getFilter().test(b)) {
                continue;
            }
            BusSerializer.INSTANCE.write(b, null, context);
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeEndNode();
    }

    private void writeGenerators(VoltageLevel vl, NetworkSerializerWriterContext context) {
        context.getWriter().writeStartNodes(GeneratorSerializer.ARRAY_ELEMENT_NAME);
        for (Generator g : IidmSerializerUtil.sorted(vl.getGenerators(), context.getOptions())) {
            if (!context.getFilter().test(g)) {
                continue;
            }
            GeneratorSerializer.INSTANCE.write(g, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeBatteries(VoltageLevel vl, NetworkSerializerWriterContext context) {
        context.getWriter().writeStartNodes(BatterySerializer.ARRAY_ELEMENT_NAME);
        for (Battery b : IidmSerializerUtil.sorted(vl.getBatteries(), context.getOptions())) {
            if (!context.getFilter().test(b)) {
                continue;
            }
            BatterySerializer.INSTANCE.write(b, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeLoads(VoltageLevel vl, NetworkSerializerWriterContext context) {
        context.getWriter().writeStartNodes(LoadSerializer.ARRAY_ELEMENT_NAME);
        for (Load l : IidmSerializerUtil.sorted(vl.getLoads(), context.getOptions())) {
            if (!context.getFilter().test(l)) {
                continue;
            }
            LoadSerializer.INSTANCE.write(l, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeShuntCompensators(VoltageLevel vl, NetworkSerializerWriterContext context) {
        context.getWriter().writeStartNodes(ShuntSerializer.ARRAY_ELEMENT_NAME);
        for (ShuntCompensator sc : IidmSerializerUtil.sorted(vl.getShuntCompensators(), context.getOptions())) {
            if (!context.getFilter().test(sc)) {
                continue;
            }
            ShuntSerializer.INSTANCE.write(sc, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeDanglingLines(VoltageLevel vl, NetworkSerializerWriterContext context) {
        context.getWriter().writeStartNodes(DanglingLineSerializer.ARRAY_ELEMENT_NAME);
        for (DanglingLine dl : IidmSerializerUtil.sorted(vl.getDanglingLines(DanglingLineFilter.ALL), context.getOptions())) {
            if (!context.getFilter().test(dl) || context.getVersion().compareTo(IidmVersion.V_1_10) < 0 && dl.isPaired()) {
                continue;
            }
            DanglingLineSerializer.INSTANCE.write(dl, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeStaticVarCompensators(VoltageLevel vl, NetworkSerializerWriterContext context) {
        context.getWriter().writeStartNodes(StaticVarCompensatorSerializer.ARRAY_ELEMENT_NAME);
        for (StaticVarCompensator svc : IidmSerializerUtil.sorted(vl.getStaticVarCompensators(), context.getOptions())) {
            if (!context.getFilter().test(svc)) {
                continue;
            }
            StaticVarCompensatorSerializer.INSTANCE.write(svc, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeVscConverterStations(VoltageLevel vl, NetworkSerializerWriterContext context) {
        context.getWriter().writeStartNodes(VscConverterStationSerializer.ARRAY_ELEMENT_NAME);
        for (VscConverterStation cs : IidmSerializerUtil.sorted(vl.getVscConverterStations(), context.getOptions())) {
            if (!context.getFilter().test(cs)) {
                continue;
            }
            VscConverterStationSerializer.INSTANCE.write(cs, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeLccConverterStations(VoltageLevel vl, NetworkSerializerWriterContext context) {
        context.getWriter().writeStartNodes(LccConverterStationSerializer.ARRAY_ELEMENT_NAME);
        for (LccConverterStation cs : IidmSerializerUtil.sorted(vl.getLccConverterStations(), context.getOptions())) {
            if (!context.getFilter().test(cs)) {
                continue;
            }
            LccConverterStationSerializer.INSTANCE.write(cs, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    @Override
    protected VoltageLevelAdder createAdder(Container<? extends Identifiable<?>> c) {
        if (c instanceof Network network) {
            return network.newVoltageLevel();
        }
        if (c instanceof Substation substation) {
            return substation.newVoltageLevel();
        }
        throw new IllegalStateException();
    }

    @Override
    protected VoltageLevel readRootElementAttributes(VoltageLevelAdder adder, Container<? extends Identifiable<?>> c, NetworkSerializerReaderContext context) {
        double nominalV = context.getReader().readDoubleAttribute("nominalV");
        double lowVoltageLimit = context.getReader().readDoubleAttribute("lowVoltageLimit");
        double highVoltageLimit = context.getReader().readDoubleAttribute("highVoltageLimit");
        TopologyKind topologyKind = context.getReader().readEnumAttribute("topologyKind", TopologyKind.class);
        return adder
                .setNominalV(nominalV)
                .setLowVoltageLimit(lowVoltageLimit)
                .setHighVoltageLimit(highVoltageLimit)
                .setTopologyKind(topologyKind)
                .add();
    }

    @Override
    protected void readSubElements(VoltageLevel vl, NetworkSerializerReaderContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case NODE_BREAKER_TOPOLOGY_ELEMENT_NAME -> readNodeBreakerTopology(vl, context);
                case BUS_BREAKER_TOPOLOGY_ELEMENT_NAME -> readBusBreakerTopology(vl, context);
                case GeneratorSerializer.ROOT_ELEMENT_NAME -> GeneratorSerializer.INSTANCE.read(vl, context);
                case BatterySerializer.ROOT_ELEMENT_NAME -> BatterySerializer.INSTANCE.read(vl, context);
                case LoadSerializer.ROOT_ELEMENT_NAME -> LoadSerializer.INSTANCE.read(vl, context);
                case ShuntSerializer.ROOT_ELEMENT_NAME -> ShuntSerializer.INSTANCE.read(vl, context);
                case DanglingLineSerializer.ROOT_ELEMENT_NAME -> DanglingLineSerializer.INSTANCE.read(vl, context);
                case StaticVarCompensatorSerializer.ROOT_ELEMENT_NAME -> StaticVarCompensatorSerializer.INSTANCE.read(vl, context);
                case VscConverterStationSerializer.ROOT_ELEMENT_NAME -> VscConverterStationSerializer.INSTANCE.read(vl, context);
                case LccConverterStationSerializer.ROOT_ELEMENT_NAME -> LccConverterStationSerializer.INSTANCE.read(vl, context);
                default -> readSubElement(elementName, vl, context);
            }
        });
    }

    private void readNodeBreakerTopology(VoltageLevel vl, NetworkSerializerReaderContext context) {
        IidmSerializerUtil.runUntilMaximumVersion(IidmVersion.V_1_1, context, () -> {
            context.getReader().readStringAttribute(NODE_COUNT);
            LOGGER.trace("attribute " + NODE_BREAKER_TOPOLOGY_ELEMENT_NAME + ".nodeCount is ignored.");
        });
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case BusbarSectionSerializer.ROOT_ELEMENT_NAME -> BusbarSectionSerializer.INSTANCE.read(vl, context);
                case AbstractSwitchSerializer.ROOT_ELEMENT_NAME -> NodeBreakerViewSwitchSerializer.INSTANCE.read(vl, context);
                case NodeBreakerViewInternalConnectionSerializer.ROOT_ELEMENT_NAME -> NodeBreakerViewInternalConnectionSerializer.INSTANCE.read(vl, context);
                case BusSerializer.ROOT_ELEMENT_NAME -> readCalculatedBus(vl, context);
                case INJ_ROOT_ELEMENT_NAME -> readFictitiousInjection(vl, context);
                default -> throw new PowsyblException(String.format("Unknown element name '%s' in 'nodeBreakerTopology'", elementName));
            }
        });
    }

    private void readCalculatedBus(VoltageLevel vl, NetworkSerializerReaderContext context) {
        IidmSerializerUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, BusSerializer.ROOT_ELEMENT_NAME, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
        double v = context.getReader().readDoubleAttribute("v");
        double angle = context.getReader().readDoubleAttribute("angle");
        List<Integer> busNodes = context.getReader().readIntArrayAttribute("nodes");
        Map<String, String> properties = new HashMap<>();
        context.getReader().readChildNodes(elementName -> {
            if (elementName.equals(PropertiesSerializer.ROOT_ELEMENT_NAME)) {
                String name = context.getReader().readStringAttribute(NAME);
                String value = context.getReader().readStringAttribute(VALUE);
                context.getReader().readEndNode();
                properties.put(name, value);
            } else {
                throw new PowsyblException(String.format("Unknown element name '%s' in 'bus'", elementName));
            }
        });
        context.getEndTasks().add(() -> {
            for (int node : busNodes) {
                Terminal terminal = vl.getNodeBreakerView().getTerminal(node);
                if (terminal != null) {
                    Bus b = terminal.getBusView().getBus();
                    if (b != null) {
                        b.setV(v).setAngle(angle);
                        properties.forEach(b::setProperty);
                        break;
                    }
                }
            }
        });
    }

    private void readFictitiousInjection(VoltageLevel vl, NetworkSerializerReaderContext context) {
        IidmSerializerUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, INJ_ROOT_ELEMENT_NAME, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_8, context);
        int node = context.getReader().readIntAttribute("node");
        double p0 = context.getReader().readDoubleAttribute("fictitiousP0");
        double q0 = context.getReader().readDoubleAttribute("fictitiousQ0");
        context.getReader().readEndNode();
        if (!Double.isNaN(p0)) {
            vl.getNodeBreakerView().setFictitiousP0(node, p0);
        }
        if (!Double.isNaN(q0)) {
            vl.getNodeBreakerView().setFictitiousQ0(node, q0);
        }
    }

    private void readBusBreakerTopology(VoltageLevel vl, NetworkSerializerReaderContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case BusSerializer.ROOT_ELEMENT_NAME -> BusSerializer.INSTANCE.read(vl, context);
                case AbstractSwitchSerializer.ROOT_ELEMENT_NAME -> BusBreakerViewSwitchSerializer.INSTANCE.read(vl, context);
                default -> throw new PowsyblException(String.format("Unknown element name '%s' in 'busBreakerTopology'", elementName));
            }
        });
    }
}
