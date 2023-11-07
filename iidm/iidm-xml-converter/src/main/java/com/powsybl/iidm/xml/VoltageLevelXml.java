/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.iidm.xml.util.IidmXmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.powsybl.iidm.xml.PropertiesXml.NAME;
import static com.powsybl.iidm.xml.PropertiesXml.VALUE;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class VoltageLevelXml extends AbstractSimpleIdentifiableXml<VoltageLevel, VoltageLevelAdder, Container<? extends Identifiable<?>>> {

    static final VoltageLevelXml INSTANCE = new VoltageLevelXml();

    static final String ROOT_ELEMENT_NAME = "voltageLevel";
    static final String ARRAY_ELEMENT_NAME = "voltageLevels";

    private static final Logger LOGGER = LoggerFactory.getLogger(VoltageLevelXml.class);

    private static final String NODE_BREAKER_TOPOLOGY_ELEMENT_NAME = "nodeBreakerTopology";
    private static final String BUS_BREAKER_TOPOLOGY_ELEMENT_NAME = "busBreakerTopology";
    private static final String NODE_COUNT = "nodeCount";
    private static final String UNEXPECTED_ELEMENT = "Unexpected element: ";
    private static final String INJ_ROOT_ELEMENT_NAME = "inj";
    private static final String INJ_ARRAY_ELEMENT_NAME = "fictitiousInjections";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(VoltageLevel vl, Container<? extends Identifiable<?>> c, NetworkXmlWriterContext context) {
        context.getWriter().writeDoubleAttribute("nominalV", vl.getNominalV());
        context.getWriter().writeDoubleAttribute("lowVoltageLimit", vl.getLowVoltageLimit());
        context.getWriter().writeDoubleAttribute("highVoltageLimit", vl.getHighVoltageLimit());

        TopologyLevel topologyLevel = TopologyLevel.min(vl.getTopologyKind(), context.getOptions().getTopologyLevel());
        context.getWriter().writeStringAttribute("topologyKind", topologyLevel.getTopologyKind().name());
    }

    @Override
    protected void writeSubElements(VoltageLevel vl, Container<? extends Identifiable<?>> c, NetworkXmlWriterContext context) {
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

    private void writeNodeBreakerTopology(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), NODE_BREAKER_TOPOLOGY_ELEMENT_NAME);
        IidmXmlUtil.writeIntAttributeUntilMaximumVersion(NODE_COUNT, vl.getNodeBreakerView().getMaximumNodeIndex() + 1, IidmXmlVersion.V_1_1, context);

        context.getWriter().writeStartNodes(BusbarSectionXml.ROOT_ELEMENT_NAME);
        for (BusbarSection bs : IidmXmlUtil.sorted(vl.getNodeBreakerView().getBusbarSections(), context.getOptions())) {
            BusbarSectionXml.INSTANCE.write(bs, null, context);
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeStartNodes(AbstractSwitchXml.ARRAY_ELEMENT_NAME);
        for (Switch sw : IidmXmlUtil.sorted(vl.getNodeBreakerView().getSwitches(), context.getOptions())) {
            NodeBreakerViewSwitchXml.INSTANCE.write(sw, vl, context);
        }
        context.getWriter().writeEndNodes();

        writeNodeBreakerTopologyInternalConnections(vl, context);

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_1, context, () -> {
            Map<String, Set<Integer>> nodesByBus = Networks.getNodesByBus(vl);
            context.getWriter().writeStartNodes(BusXml.ARRAY_ELEMENT_NAME);
            IidmXmlUtil.sorted(vl.getBusView().getBusStream(), context.getOptions())
                    .filter(bus -> !Double.isNaN(bus.getV()) || !Double.isNaN(bus.getAngle()))
                    .forEach(bus -> {
                        Set<Integer> nodes = nodesByBus.get(bus.getId());
                        writeCalculatedBus(bus, nodes, context);
                    });
            context.getWriter().writeEndNodes();
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_8, context, () -> {
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

    private static void writeCalculatedBus(Bus bus, Set<Integer> nodes, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), "bus");
        context.getWriter().writeDoubleAttribute("v", bus.getV());
        context.getWriter().writeDoubleAttribute("angle", bus.getAngle());
        context.getWriter().writeIntArrayAttribute("nodes", nodes);
        if (context.getVersion().compareTo(IidmXmlVersion.V_1_11) >= 0 && bus.hasProperty()) {
            PropertiesXml.write(bus, context);
        }
        context.getWriter().writeEndNode();
    }

    private void writeNodeBreakerTopologyInternalConnections(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes(NodeBreakerViewInternalConnectionXml.ARRAY_ELEMENT_NAME);
        for (VoltageLevel.NodeBreakerView.InternalConnection ic : IidmXmlUtil.sortedInternalConnections(vl.getNodeBreakerView().getInternalConnections(), context.getOptions())) {
            NodeBreakerViewInternalConnectionXml.INSTANCE.write(ic.getNode1(), ic.getNode2(), context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeBusBreakerTopology(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), BUS_BREAKER_TOPOLOGY_ELEMENT_NAME);

        context.getWriter().writeStartNodes(BusXml.ARRAY_ELEMENT_NAME);
        for (Bus b : IidmXmlUtil.sorted(vl.getBusBreakerView().getBuses(), context.getOptions())) {
            if (!context.getFilter().test(b)) {
                continue;
            }
            BusXml.INSTANCE.write(b, null, context);
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeStartNodes(AbstractSwitchXml.ARRAY_ELEMENT_NAME);
        for (Switch sw : IidmXmlUtil.sorted(vl.getBusBreakerView().getSwitches(), context.getOptions())) {
            Bus b1 = vl.getBusBreakerView().getBus1(sw.getId());
            Bus b2 = vl.getBusBreakerView().getBus2(sw.getId());
            if (!context.getFilter().test(b1) || !context.getFilter().test(b2)) {
                continue;
            }
            BusBreakerViewSwitchXml.INSTANCE.write(sw, vl, context);
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeEndNode();
    }

    private void writeBusBranchTopology(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), BUS_BREAKER_TOPOLOGY_ELEMENT_NAME);

        context.getWriter().writeStartNodes(BusXml.ARRAY_ELEMENT_NAME);
        for (Bus b : IidmXmlUtil.sorted(vl.getBusView().getBuses(), context.getOptions())) {
            if (!context.getFilter().test(b)) {
                continue;
            }
            BusXml.INSTANCE.write(b, null, context);
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeEndNode();
    }

    private void writeGenerators(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes(GeneratorXml.ARRAY_ELEMENT_NAME);
        for (Generator g : IidmXmlUtil.sorted(vl.getGenerators(), context.getOptions())) {
            if (!context.getFilter().test(g)) {
                continue;
            }
            GeneratorXml.INSTANCE.write(g, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeBatteries(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes(BatteryXml.ARRAY_ELEMENT_NAME);
        for (Battery b : IidmXmlUtil.sorted(vl.getBatteries(), context.getOptions())) {
            if (!context.getFilter().test(b)) {
                continue;
            }
            BatteryXml.INSTANCE.write(b, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeLoads(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes(LoadXml.ARRAY_ELEMENT_NAME);
        for (Load l : IidmXmlUtil.sorted(vl.getLoads(), context.getOptions())) {
            if (!context.getFilter().test(l)) {
                continue;
            }
            LoadXml.INSTANCE.write(l, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeShuntCompensators(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes(ShuntXml.ARRAY_ELEMENT_NAME);
        for (ShuntCompensator sc : IidmXmlUtil.sorted(vl.getShuntCompensators(), context.getOptions())) {
            if (!context.getFilter().test(sc)) {
                continue;
            }
            ShuntXml.INSTANCE.write(sc, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeDanglingLines(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes(DanglingLineXml.ARRAY_ELEMENT_NAME);
        for (DanglingLine dl : IidmXmlUtil.sorted(vl.getDanglingLines(DanglingLineFilter.ALL), context.getOptions())) {
            if (!context.getFilter().test(dl) || context.getVersion().compareTo(IidmXmlVersion.V_1_10) < 0 && dl.isPaired()) {
                continue;
            }
            DanglingLineXml.INSTANCE.write(dl, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeStaticVarCompensators(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes(StaticVarCompensatorXml.ARRAY_ELEMENT_NAME);
        for (StaticVarCompensator svc : IidmXmlUtil.sorted(vl.getStaticVarCompensators(), context.getOptions())) {
            if (!context.getFilter().test(svc)) {
                continue;
            }
            StaticVarCompensatorXml.INSTANCE.write(svc, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeVscConverterStations(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes(VscConverterStationXml.ARRAY_ELEMENT_NAME);
        for (VscConverterStation cs : IidmXmlUtil.sorted(vl.getVscConverterStations(), context.getOptions())) {
            if (!context.getFilter().test(cs)) {
                continue;
            }
            VscConverterStationXml.INSTANCE.write(cs, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeLccConverterStations(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes(LccConverterStationXml.ARRAY_ELEMENT_NAME);
        for (LccConverterStation cs : IidmXmlUtil.sorted(vl.getLccConverterStations(), context.getOptions())) {
            if (!context.getFilter().test(cs)) {
                continue;
            }
            LccConverterStationXml.INSTANCE.write(cs, vl, context);
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
    protected VoltageLevel readRootElementAttributes(VoltageLevelAdder adder, Container<? extends Identifiable<?>> c, NetworkXmlReaderContext context) {
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
    protected void readSubElements(VoltageLevel vl, NetworkXmlReaderContext context) {
        context.getReader().readUntilEndNode(getRootElementName(), () -> {
            switch (context.getReader().getNodeName()) {
                case NODE_BREAKER_TOPOLOGY_ELEMENT_NAME -> readNodeBreakerTopology(vl, context);
                case BUS_BREAKER_TOPOLOGY_ELEMENT_NAME -> readBusBreakerTopology(vl, context);
                case GeneratorXml.ROOT_ELEMENT_NAME, GeneratorXml.ARRAY_ELEMENT_NAME
                        -> GeneratorXml.INSTANCE.read(vl, context);
                case BatteryXml.ROOT_ELEMENT_NAME, BatteryXml.ARRAY_ELEMENT_NAME
                        -> BatteryXml.INSTANCE.read(vl, context);
                case LoadXml.ROOT_ELEMENT_NAME, LoadXml.ARRAY_ELEMENT_NAME
                        -> LoadXml.INSTANCE.read(vl, context);
                case ShuntXml.ROOT_ELEMENT_NAME, ShuntXml.ARRAY_ELEMENT_NAME
                        -> ShuntXml.INSTANCE.read(vl, context);
                case DanglingLineXml.ROOT_ELEMENT_NAME, DanglingLineXml.ARRAY_ELEMENT_NAME
                        -> DanglingLineXml.INSTANCE.read(vl, context);
                case StaticVarCompensatorXml.ROOT_ELEMENT_NAME, StaticVarCompensatorXml.ARRAY_ELEMENT_NAME
                        -> StaticVarCompensatorXml.INSTANCE.read(vl, context);
                case VscConverterStationXml.ROOT_ELEMENT_NAME, VscConverterStationXml.ARRAY_ELEMENT_NAME
                        -> VscConverterStationXml.INSTANCE.read(vl, context);
                case LccConverterStationXml.ROOT_ELEMENT_NAME, LccConverterStationXml.ARRAY_ELEMENT_NAME
                        -> LccConverterStationXml.INSTANCE.read(vl, context);
                default -> super.readSubElements(vl, context);
            }
        });
    }

    private void readNodeBreakerTopology(VoltageLevel vl, NetworkXmlReaderContext context) {
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_1, context, () -> LOGGER.trace("attribute " + NODE_BREAKER_TOPOLOGY_ELEMENT_NAME + ".nodeCount is ignored."));
        context.getReader().readUntilEndNode(NODE_BREAKER_TOPOLOGY_ELEMENT_NAME, () -> {
            switch (context.getReader().getNodeName()) {
                case BusbarSectionXml.ROOT_ELEMENT_NAME, BusbarSectionXml.ARRAY_ELEMENT_NAME
                        -> BusbarSectionXml.INSTANCE.read(vl, context);
                case AbstractSwitchXml.ROOT_ELEMENT_NAME, AbstractSwitchXml.ARRAY_ELEMENT_NAME
                        -> NodeBreakerViewSwitchXml.INSTANCE.read(vl, context);
                case NodeBreakerViewInternalConnectionXml.ROOT_ELEMENT_NAME, NodeBreakerViewInternalConnectionXml.ARRAY_ELEMENT_NAME
                        -> NodeBreakerViewInternalConnectionXml.INSTANCE.read(vl, context);
                case BusXml.ROOT_ELEMENT_NAME, BusXml.ARRAY_ELEMENT_NAME
                        -> readCalculatedBus(vl, context);
                case INJ_ROOT_ELEMENT_NAME, INJ_ARRAY_ELEMENT_NAME
                        -> readFictitiousInjection(vl, context);
                default -> throw new IllegalStateException(UNEXPECTED_ELEMENT + context.getReader().getNodeName());
            }
        });
    }

    private void readCalculatedBus(VoltageLevel vl, NetworkXmlReaderContext context) {
        IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, BusXml.ROOT_ELEMENT_NAME, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        double v = context.getReader().readDoubleAttribute("v");
        double angle = context.getReader().readDoubleAttribute("angle");
        List<Integer> busNodes = context.getReader().readIntArrayAttribute("nodes");
        Map<String, String> properties = new HashMap<>();
        context.getReader().readUntilEndNode(BusXml.ROOT_ELEMENT_NAME, () -> {
            if (context.getReader().getNodeName().equals(PropertiesXml.ROOT_ELEMENT_NAME)) {
                String name = context.getReader().readStringAttribute(NAME);
                String value = context.getReader().readStringAttribute(VALUE);
                properties.put(name, value);
            } else {
                throw new IllegalStateException(UNEXPECTED_ELEMENT + context.getReader().getNodeName());
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

    private void readFictitiousInjection(VoltageLevel vl, NetworkXmlReaderContext context) {
        IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, INJ_ROOT_ELEMENT_NAME, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_8, context);
        int node = context.getReader().readIntAttribute("node");
        double p0 = context.getReader().readDoubleAttribute("fictitiousP0");
        double q0 = context.getReader().readDoubleAttribute("fictitiousQ0");
        if (!Double.isNaN(p0)) {
            vl.getNodeBreakerView().setFictitiousP0(node, p0);
        }
        if (!Double.isNaN(q0)) {
            vl.getNodeBreakerView().setFictitiousQ0(node, q0);
        }
    }

    private void readBusBreakerTopology(VoltageLevel vl, NetworkXmlReaderContext context) {
        context.getReader().readUntilEndNode(BUS_BREAKER_TOPOLOGY_ELEMENT_NAME, () -> {
            switch (context.getReader().getNodeName()) {
                case BusXml.ROOT_ELEMENT_NAME,
                        BusXml.ARRAY_ELEMENT_NAME -> BusXml.INSTANCE.read(vl, context);
                case AbstractSwitchXml.ROOT_ELEMENT_NAME,
                        AbstractSwitchXml.ARRAY_ELEMENT_NAME -> BusBreakerViewSwitchXml.INSTANCE.read(vl, context);
                default -> throw new IllegalStateException(UNEXPECTED_ELEMENT + context.getReader().getNodeName());
            }
        });
    }
}
