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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class VoltageLevelXml extends AbstractIdentifiableXml<VoltageLevel, VoltageLevelAdder, Container<? extends Identifiable<?>>> {

    static final VoltageLevelXml INSTANCE = new VoltageLevelXml();

    static final String ROOT_ELEMENT_NAME = "voltageLevel";

    private static final Logger LOGGER = LoggerFactory.getLogger(VoltageLevelXml.class);

    private static final String NODE_BREAKER_TOPOLOGY_ELEMENT_NAME = "nodeBreakerTopology";
    private static final String BUS_BREAKER_TOPOLOGY_ELEMENT_NAME = "busBreakerTopology";
    private static final String NODE_COUNT = "nodeCount";

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
                throw new AssertionError("Unexpected TopologyLevel value: " + topologyLevel);
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

        context.getWriter().writeStartNodes("busbarSections");
        for (BusbarSection bs : IidmXmlUtil.sorted(vl.getNodeBreakerView().getBusbarSections(), context.getOptions())) {
            BusbarSectionXml.INSTANCE.write(bs, null, context);
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeStartNodes("switches");
        for (Switch sw : IidmXmlUtil.sorted(vl.getNodeBreakerView().getSwitches(), context.getOptions())) {
            NodeBreakerViewSwitchXml.INSTANCE.write(sw, vl, context);
        }
        context.getWriter().writeEndNodes();

        writeNodeBreakerTopologyInternalConnections(vl, context);

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_1, context, () -> {
            Map<String, Set<Integer>> nodesByBus = Networks.getNodesByBus(vl);
            context.getWriter().writeStartNodes("buses");
            IidmXmlUtil.sorted(vl.getBusView().getBusStream(), context.getOptions())
                    .filter(bus -> !Double.isNaN(bus.getV()) || !Double.isNaN(bus.getAngle()))
                    .forEach(bus -> {
                        Set<Integer> nodes = nodesByBus.get(bus.getId());
                        writeCalculatedBus(bus, nodes, context);
                    });
            context.getWriter().writeEndNodes();
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_8, context, () -> {
            context.getWriter().writeStartNodes("fictitiousInjections");
            for (int node : vl.getNodeBreakerView().getNodes()) {
                double fictP0 = vl.getNodeBreakerView().getFictitiousP0(node);
                double fictQ0 = vl.getNodeBreakerView().getFictitiousQ0(node);
                if (fictP0 != 0.0 || fictQ0 != 0.0) {
                    context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), "inj");
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
        context.getWriter().writeStringAttribute("nodes", StringUtils.join(nodes.toArray(), ','));
        context.getWriter().writeEndNode();
    }

    private void writeNodeBreakerTopologyInternalConnections(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes("internalConnections");
        for (VoltageLevel.NodeBreakerView.InternalConnection ic : IidmXmlUtil.sortedInternalConnections(vl.getNodeBreakerView().getInternalConnections(), context.getOptions())) {
            NodeBreakerViewInternalConnectionXml.INSTANCE.write(ic.getNode1(), ic.getNode2(), context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeBusBreakerTopology(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), BUS_BREAKER_TOPOLOGY_ELEMENT_NAME);

        context.getWriter().writeStartNodes("buses");
        for (Bus b : IidmXmlUtil.sorted(vl.getBusBreakerView().getBuses(), context.getOptions())) {
            if (!context.getFilter().test(b)) {
                continue;
            }
            BusXml.INSTANCE.write(b, null, context);
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeStartNodes("switches");
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

        context.getWriter().writeStartNodes("buses");
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
        context.getWriter().writeStartNodes("generators");
        for (Generator g : IidmXmlUtil.sorted(vl.getGenerators(), context.getOptions())) {
            if (!context.getFilter().test(g)) {
                continue;
            }
            GeneratorXml.INSTANCE.write(g, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeBatteries(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes("batteries");
        for (Battery b : IidmXmlUtil.sorted(vl.getBatteries(), context.getOptions())) {
            if (!context.getFilter().test(b)) {
                continue;
            }
            BatteryXml.INSTANCE.write(b, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeLoads(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes("loads");
        for (Load l : IidmXmlUtil.sorted(vl.getLoads(), context.getOptions())) {
            if (!context.getFilter().test(l)) {
                continue;
            }
            LoadXml.INSTANCE.write(l, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeShuntCompensators(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes("shuntCompensators");
        for (ShuntCompensator sc : IidmXmlUtil.sorted(vl.getShuntCompensators(), context.getOptions())) {
            if (!context.getFilter().test(sc)) {
                continue;
            }
            ShuntXml.INSTANCE.write(sc, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeDanglingLines(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes("danglingLines");
        for (DanglingLine dl : IidmXmlUtil.sorted(vl.getDanglingLines(), context.getOptions())) {
            if (!context.getFilter().test(dl)) {
                continue;
            }
            DanglingLineXml.INSTANCE.write(dl, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeStaticVarCompensators(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes("staticVarCompensators");
        for (StaticVarCompensator svc : IidmXmlUtil.sorted(vl.getStaticVarCompensators(), context.getOptions())) {
            if (!context.getFilter().test(svc)) {
                continue;
            }
            StaticVarCompensatorXml.INSTANCE.write(svc, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeVscConverterStations(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes("vscConverterStations");
        for (VscConverterStation cs : IidmXmlUtil.sorted(vl.getVscConverterStations(), context.getOptions())) {
            if (!context.getFilter().test(cs)) {
                continue;
            }
            VscConverterStationXml.INSTANCE.write(cs, vl, context);
        }
        context.getWriter().writeEndNodes();
    }

    private void writeLccConverterStations(VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes("lccConverterStations");
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
        if (c instanceof Network) {
            return ((Network) c).newVoltageLevel();
        }
        if (c instanceof Substation) {
            return ((Substation) c).newVoltageLevel();
        }
        throw new AssertionError();
    }

    @Override
    protected VoltageLevel readRootElementAttributes(VoltageLevelAdder adder, NetworkXmlReaderContext context) {
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
                case NODE_BREAKER_TOPOLOGY_ELEMENT_NAME:
                    readNodeBreakerTopology(vl, context);
                    break;

                case BUS_BREAKER_TOPOLOGY_ELEMENT_NAME:
                    readBusBreakerTopology(vl, context);
                    break;

                case GeneratorXml.ROOT_ELEMENT_NAME:
                    GeneratorXml.INSTANCE.read(vl, context);
                    break;

                case BatteryXml.ROOT_ELEMENT_NAME:
                    BatteryXml.INSTANCE.read(vl, context);
                    break;

                case LoadXml.ROOT_ELEMENT_NAME:
                    LoadXml.INSTANCE.read(vl, context);
                    break;

                case ShuntXml.ROOT_ELEMENT_NAME:
                    ShuntXml.INSTANCE.read(vl, context);
                    break;

                case DanglingLineXml.ROOT_ELEMENT_NAME:
                    DanglingLineXml.INSTANCE.read(vl, context);
                    break;

                case StaticVarCompensatorXml.ROOT_ELEMENT_NAME:
                    StaticVarCompensatorXml.INSTANCE.read(vl, context);
                    break;

                case VscConverterStationXml.ROOT_ELEMENT_NAME:
                    VscConverterStationXml.INSTANCE.read(vl, context);
                    break;

                case LccConverterStationXml.ROOT_ELEMENT_NAME:
                    LccConverterStationXml.INSTANCE.read(vl, context);
                    break;

                default:
                    super.readSubElements(vl, context);
            }
        });
    }

    private void readNodeBreakerTopology(VoltageLevel vl, NetworkXmlReaderContext context) {
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_1, context, () -> LOGGER.trace("attribute " + NODE_BREAKER_TOPOLOGY_ELEMENT_NAME + ".nodeCount is ignored."));
        context.getReader().readUntilEndNode(NODE_BREAKER_TOPOLOGY_ELEMENT_NAME, () -> {
            switch (context.getReader().getNodeName()) {
                case BusbarSectionXml.ROOT_ELEMENT_NAME:
                    BusbarSectionXml.INSTANCE.read(vl, context);
                    break;

                case AbstractSwitchXml.ROOT_ELEMENT_NAME:
                    NodeBreakerViewSwitchXml.INSTANCE.read(vl, context);
                    break;

                case NodeBreakerViewInternalConnectionXml.ROOT_ELEMENT_NAME:
                    NodeBreakerViewInternalConnectionXml.INSTANCE.read(vl, context);
                    break;

                case BusXml.ROOT_ELEMENT_NAME:
                    readCalculatedBus(vl, context);
                    break;

                case "inj":
                    readFictitiousInjection(vl, context);
                    break;

                default:
                    throw new AssertionError("Unexpected element: " + context.getReader().getNodeName());
            }
        });
    }

    private void readCalculatedBus(VoltageLevel vl, NetworkXmlReaderContext context) {
        IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, BusXml.ROOT_ELEMENT_NAME, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        double v = context.getReader().readDoubleAttribute("v");
        double angle = context.getReader().readDoubleAttribute("angle");
        String nodesString = context.getReader().readStringAttribute("nodes");
        context.getEndTasks().add(() -> {
            for (String str : nodesString.split(",")) {
                int node = Integer.parseInt(str);
                Terminal terminal = vl.getNodeBreakerView().getTerminal(node);
                if (terminal != null) {
                    Bus b = terminal.getBusView().getBus();
                    if (b != null) {
                        b.setV(v).setAngle(angle);
                        break;
                    }
                }
            }
        });
    }

    private void readFictitiousInjection(VoltageLevel vl, NetworkXmlReaderContext context) {
        IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, "inj", IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_8, context);
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
                case BusXml.ROOT_ELEMENT_NAME:
                    BusXml.INSTANCE.read(vl, context);
                    break;

                case AbstractSwitchXml.ROOT_ELEMENT_NAME:
                    BusBreakerViewSwitchXml.INSTANCE.read(vl, context);
                    break;

                default:
                    throw new AssertionError("Unexpected element: " + context.getReader().getNodeName());
            }
        });
    }
}
