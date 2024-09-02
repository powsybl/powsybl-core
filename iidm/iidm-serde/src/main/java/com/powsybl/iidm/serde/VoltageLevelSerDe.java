/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.powsybl.iidm.serde.PropertiesSerDe.NAME;
import static com.powsybl.iidm.serde.PropertiesSerDe.VALUE;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class VoltageLevelSerDe extends AbstractSimpleIdentifiableSerDe<VoltageLevel, VoltageLevelAdder, Container<? extends Identifiable<?>>> {

    static final VoltageLevelSerDe INSTANCE = new VoltageLevelSerDe();

    static final String ROOT_ELEMENT_NAME = "voltageLevel";
    static final String ARRAY_ELEMENT_NAME = "voltageLevels";

    private static final Logger LOGGER = LoggerFactory.getLogger(VoltageLevelSerDe.class);

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
    protected void writeRootElementAttributes(VoltageLevel vl, Container<? extends Identifiable<?>> c, NetworkSerializerContext context) {
        context.getWriter().writeDoubleAttribute("nominalV", vl.getNominalV());
        context.getWriter().writeDoubleAttribute("lowVoltageLimit", vl.getLowVoltageLimit());
        context.getWriter().writeDoubleAttribute("highVoltageLimit", vl.getHighVoltageLimit());

        TopologyLevel topologyLevel = TopologyLevel.min(vl.getTopologyKind(), context.getOptions().getTopologyLevel());
        context.getWriter().writeEnumAttribute("topologyKind", topologyLevel.getTopologyKind());
    }

    @Override
    protected void writeSubElements(VoltageLevel vl, Container<? extends Identifiable<?>> c, NetworkSerializerContext context) {
        TopologyLevel topologyLevel = TopologyLevel.min(vl.getTopologyKind(), context.getOptions().getTopologyLevel());
        switch (topologyLevel) {
            case NODE_BREAKER -> writeNodeBreakerTopology(vl, context);
            case BUS_BREAKER -> writeBusBreakerTopology(vl, context);
            case BUS_BRANCH -> writeBusBranchTopology(vl, context);
            default -> throw new IllegalStateException("Unexpected TopologyLevel value: " + topologyLevel);
        }

        writeGenerators(vl, context);
        writeBatteries(vl, context);
        writeLoads(vl, context);
        writeShuntCompensators(vl, context);
        writeDanglingLines(vl, context);
        writeStaticVarCompensators(vl, context);
        writeVscConverterStations(vl, context);
        writeLccConverterStations(vl, context);
        writeGrounds(vl, context);
    }

    private void writeNodeBreakerTopology(VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), NODE_BREAKER_TOPOLOGY_ELEMENT_NAME);
        IidmSerDeUtil.writeIntAttributeUntilMaximumVersion(NODE_COUNT, vl.getNodeBreakerView().getMaximumNodeIndex() + 1, IidmVersion.V_1_1, context);

        context.getWriter().writeStartNodes();
        for (BusbarSection bs : IidmSerDeUtil.sorted(vl.getNodeBreakerView().getBusbarSections(), context.getOptions())) {
            BusbarSectionSerDe.INSTANCE.write(bs, null, context);
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeStartNodes();
        for (Switch sw : IidmSerDeUtil.sorted(vl.getNodeBreakerView().getSwitches(), context.getOptions())) {
            NodeBreakerViewSwitchSerDe.INSTANCE.write(sw, vl, context);
        }
        context.getWriter().writeEndNodes();

        writeNodeBreakerTopologyInternalConnections(vl, context);

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_1, context, () -> {
            Map<String, Set<Integer>> nodesByBus = Networks.getNodesByBus(vl);
            context.getWriter().writeStartNodes();
            IidmSerDeUtil.sorted(vl.getBusView().getBusStream(), context.getOptions())
                    .filter(bus -> !Double.isNaN(bus.getV()) || !Double.isNaN(bus.getAngle()))
                    .forEach(bus -> {
                        Set<Integer> nodes = nodesByBus.get(bus.getId());
                        writeCalculatedBus(bus, nodes, context);
                    });
            context.getWriter().writeEndNodes();
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_8, context, () -> {
            context.getWriter().writeStartNodes();
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

    private static void writeCalculatedBus(Bus bus, Set<Integer> nodes, NetworkSerializerContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), "bus");
        context.getWriter().writeDoubleAttribute("v", bus.getV());
        context.getWriter().writeDoubleAttribute("angle", bus.getAngle());
        context.getWriter().writeIntArrayAttribute("nodes", nodes);
        if (context.getVersion().compareTo(IidmVersion.V_1_11) >= 0 && bus.hasProperty()) {
            PropertiesSerDe.write(bus, context);
        }
        context.getWriter().writeEndNode();
    }

    private void writeNodeBreakerTopologyInternalConnections(VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        for (VoltageLevel.NodeBreakerView.InternalConnection ic : IidmSerDeUtil.sortedInternalConnections(vl.getNodeBreakerView().getInternalConnections(), context.getOptions())) {
            NodeBreakerViewInternalConnectionSerDe.INSTANCE.write(ic.getNode1(), ic.getNode2(), context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeBusBreakerTopology(VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), BUS_BREAKER_TOPOLOGY_ELEMENT_NAME);

        context.getWriter().writeStartNodes();
        for (Bus b : IidmSerDeUtil.sorted(vl.getBusBreakerView().getBuses(), context.getOptions())) {
            if (!context.getFilter().test(b)) {
                continue;
            }
            BusSerDe.INSTANCE.write(b, null, context);
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeStartNodes();
        for (Switch sw : IidmSerDeUtil.sorted(vl.getBusBreakerView().getSwitches(), context.getOptions())) {
            Bus b1 = vl.getBusBreakerView().getBus1(sw.getId());
            Bus b2 = vl.getBusBreakerView().getBus2(sw.getId());
            if (!context.getFilter().test(b1) || !context.getFilter().test(b2)) {
                continue;
            }
            BusBreakerViewSwitchSerDe.INSTANCE.write(sw, vl, context);
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeEndNode();
    }

    private void writeBusBranchTopology(VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), BUS_BREAKER_TOPOLOGY_ELEMENT_NAME);

        context.getWriter().writeStartNodes();
        for (Bus b : IidmSerDeUtil.sorted(vl.getBusView().getBuses(), context.getOptions())) {
            if (!context.getFilter().test(b)) {
                continue;
            }
            BusSerDe.INSTANCE.write(b, null, context);
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeEndNode();
    }

    private void writeGenerators(VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        for (Generator g : IidmSerDeUtil.sorted(vl.getGenerators(), context.getOptions())) {
            if (!context.getFilter().test(g)) {
                continue;
            }
            GeneratorSerDe.INSTANCE.write(g, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeBatteries(VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        for (Battery b : IidmSerDeUtil.sorted(vl.getBatteries(), context.getOptions())) {
            if (!context.getFilter().test(b)) {
                continue;
            }
            BatterySerDe.INSTANCE.write(b, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeLoads(VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        for (Load l : IidmSerDeUtil.sorted(vl.getLoads(), context.getOptions())) {
            if (!context.getFilter().test(l)) {
                continue;
            }
            LoadSerDe.INSTANCE.write(l, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeShuntCompensators(VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        for (ShuntCompensator sc : IidmSerDeUtil.sorted(vl.getShuntCompensators(), context.getOptions())) {
            if (!context.getFilter().test(sc)) {
                continue;
            }
            ShuntSerDe.INSTANCE.write(sc, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeDanglingLines(VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        for (DanglingLine dl : IidmSerDeUtil.sorted(vl.getDanglingLines(DanglingLineFilter.ALL), context.getOptions())) {
            if (!context.getFilter().test(dl) || context.getVersion().compareTo(IidmVersion.V_1_10) < 0 && dl.isPaired()) {
                continue;
            }
            DanglingLineSerDe.INSTANCE.write(dl, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeStaticVarCompensators(VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        for (StaticVarCompensator svc : IidmSerDeUtil.sorted(vl.getStaticVarCompensators(), context.getOptions())) {
            if (!context.getFilter().test(svc)) {
                continue;
            }
            StaticVarCompensatorSerDe.INSTANCE.write(svc, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeVscConverterStations(VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        for (VscConverterStation cs : IidmSerDeUtil.sorted(vl.getVscConverterStations(), context.getOptions())) {
            if (!context.getFilter().test(cs)) {
                continue;
            }
            VscConverterStationSerDe.INSTANCE.write(cs, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeLccConverterStations(VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        for (LccConverterStation cs : IidmSerDeUtil.sorted(vl.getLccConverterStations(), context.getOptions())) {
            if (!context.getFilter().test(cs)) {
                continue;
            }
            LccConverterStationSerDe.INSTANCE.write(cs, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeGrounds(VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        for (Ground g : IidmSerDeUtil.sorted(vl.getGrounds(), context.getOptions())) {
            if (!context.getFilter().test(g)) {
                continue;
            }
            GroundSerDe.INSTANCE.write(g, vl, context);
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
    protected VoltageLevel readRootElementAttributes(VoltageLevelAdder adder, Container<? extends Identifiable<?>> c, NetworkDeserializerContext context) {
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
    protected void readSubElements(VoltageLevel vl, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case NODE_BREAKER_TOPOLOGY_ELEMENT_NAME -> readNodeBreakerTopology(vl, context);
                case BUS_BREAKER_TOPOLOGY_ELEMENT_NAME -> readBusBreakerTopology(vl, context);
                case GeneratorSerDe.ROOT_ELEMENT_NAME -> GeneratorSerDe.INSTANCE.read(vl, context);
                case BatterySerDe.ROOT_ELEMENT_NAME -> BatterySerDe.INSTANCE.read(vl, context);
                case LoadSerDe.ROOT_ELEMENT_NAME -> LoadSerDe.INSTANCE.read(vl, context);
                case ShuntSerDe.ROOT_ELEMENT_NAME -> ShuntSerDe.INSTANCE.read(vl, context);
                case DanglingLineSerDe.ROOT_ELEMENT_NAME -> DanglingLineSerDe.INSTANCE.read(vl, context);
                case StaticVarCompensatorSerDe.ROOT_ELEMENT_NAME -> StaticVarCompensatorSerDe.INSTANCE.read(vl, context);
                case VscConverterStationSerDe.ROOT_ELEMENT_NAME -> VscConverterStationSerDe.INSTANCE.read(vl, context);
                case LccConverterStationSerDe.ROOT_ELEMENT_NAME -> LccConverterStationSerDe.INSTANCE.read(vl, context);
                case GroundSerDe.ROOT_ELEMENT_NAME -> GroundSerDe.INSTANCE.read(vl, context);
                default -> readSubElement(elementName, vl, context);
            }
        });
    }

    private void readNodeBreakerTopology(VoltageLevel vl, NetworkDeserializerContext context) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_1, context, () -> {
            context.getReader().readIntAttribute(NODE_COUNT);
            LOGGER.trace("attribute " + NODE_BREAKER_TOPOLOGY_ELEMENT_NAME + ".nodeCount is ignored.");
        });
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case BusbarSectionSerDe.ROOT_ELEMENT_NAME -> BusbarSectionSerDe.INSTANCE.read(vl, context);
                case AbstractSwitchSerDe.ROOT_ELEMENT_NAME -> NodeBreakerViewSwitchSerDe.INSTANCE.read(vl, context);
                case NodeBreakerViewInternalConnectionSerDe.ROOT_ELEMENT_NAME -> NodeBreakerViewInternalConnectionSerDe.INSTANCE.read(vl, context);
                case BusSerDe.ROOT_ELEMENT_NAME -> readCalculatedBus(vl, context);
                case INJ_ROOT_ELEMENT_NAME -> readFictitiousInjection(vl, context);
                default -> throw new PowsyblException(String.format("Unknown element name '%s' in 'nodeBreakerTopology'", elementName));
            }
        });
    }

    private void readCalculatedBus(VoltageLevel vl, NetworkDeserializerContext context) {
        IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, BusSerDe.ROOT_ELEMENT_NAME, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
        double v = context.getReader().readDoubleAttribute("v");
        double angle = context.getReader().readDoubleAttribute("angle");
        List<Integer> busNodes = context.getReader().readIntArrayAttribute("nodes");
        Map<String, String> properties = new HashMap<>();
        context.getReader().readChildNodes(elementName -> {
            if (elementName.equals(PropertiesSerDe.ROOT_ELEMENT_NAME)) {
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

    private void readFictitiousInjection(VoltageLevel vl, NetworkDeserializerContext context) {
        IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, INJ_ROOT_ELEMENT_NAME, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_8, context);
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

    private void readBusBreakerTopology(VoltageLevel vl, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case BusSerDe.ROOT_ELEMENT_NAME -> BusSerDe.INSTANCE.read(vl, context);
                case AbstractSwitchSerDe.ROOT_ELEMENT_NAME -> BusBreakerViewSwitchSerDe.INSTANCE.read(vl, context);
                default -> throw new PowsyblException(String.format("Unknown element name '%s' in 'busBreakerTopology'", elementName));
            }
        });
    }
}
