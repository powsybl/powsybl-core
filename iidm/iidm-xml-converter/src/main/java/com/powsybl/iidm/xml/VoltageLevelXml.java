/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.iidm.xml.util.IidmXmlUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.util.Map;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class VoltageLevelXml extends AbstractIdentifiableXml<VoltageLevel, VoltageLevelAdder, Substation> {

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
    protected boolean hasSubElements(VoltageLevel vl) {
        return true;
    }

    @Override
    protected void writeRootElementAttributes(VoltageLevel vl, Substation s, NetworkXmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("nominalV", vl.getNominalV(), context.getWriter());
        XmlUtil.writeDouble("lowVoltageLimit", vl.getLowVoltageLimit(), context.getWriter());
        XmlUtil.writeDouble("highVoltageLimit", vl.getHighVoltageLimit(), context.getWriter());

        TopologyLevel topologyLevel = TopologyLevel.min(vl.getTopologyKind(), context.getOptions().getTopologyLevel());
        context.getWriter().writeAttribute("topologyKind", topologyLevel.getTopologyKind().name());
    }

    @Override
    protected void writeSubElements(VoltageLevel vl, Substation s, NetworkXmlWriterContext context) throws XMLStreamException {
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

    private void writeNodeBreakerTopology(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeStartElement(context.getVersion().getNamespaceURI(), NODE_BREAKER_TOPOLOGY_ELEMENT_NAME);
        IidmXmlUtil.writeIntAttributeUntilMaximumVersion(NODE_COUNT, vl.getNodeBreakerView().getMaximumNodeIndex() + 1, IidmXmlVersion.V_1_1, context);
        for (BusbarSection bs : vl.getNodeBreakerView().getBusbarSections()) {
            BusbarSectionXml.INSTANCE.write(bs, null, context);
        }
        for (Switch sw : vl.getNodeBreakerView().getSwitches()) {
            NodeBreakerViewSwitchXml.INSTANCE.write(sw, vl, context);
        }
        writeNodeBreakerTopologyInternalConnections(vl, context);

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_1, context, () -> {
            Map<String, Set<Integer>> nodesByBus = Networks.getNodesByBus(vl);
            vl.getBusView().getBusStream()
                    .filter(bus -> !Double.isNaN(bus.getV()) || !Double.isNaN(bus.getAngle()))
                    .forEach(bus -> {
                        Set<Integer> nodes = nodesByBus.get(bus.getId());
                        writeCalculatedBus(bus, nodes, context);
                    });
        });
        context.getWriter().writeEndElement();
    }

    private static void writeCalculatedBus(Bus bus, Set<Integer> nodes, NetworkXmlWriterContext context) {
        try {
            context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(), "bus");
            XmlUtil.writeDouble("v", bus.getV(), context.getWriter());
            XmlUtil.writeDouble("angle", bus.getAngle(), context.getWriter());
            context.getWriter().writeAttribute("nodes", StringUtils.join(nodes.toArray(), ','));
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private void writeNodeBreakerTopologyInternalConnections(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        for (VoltageLevel.NodeBreakerView.InternalConnection ic : vl.getNodeBreakerView().getInternalConnections()) {
            NodeBreakerViewInternalConnectionXml.INSTANCE.write(ic.getNode1(), ic.getNode2(), context);
        }
    }

    private void writeBusBreakerTopology(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeStartElement(context.getVersion().getNamespaceURI(), BUS_BREAKER_TOPOLOGY_ELEMENT_NAME);
        for (Bus b : vl.getBusBreakerView().getBuses()) {
            if (!context.getFilter().test(b)) {
                continue;
            }
            BusXml.INSTANCE.write(b, null, context);
        }
        for (Switch sw : vl.getBusBreakerView().getSwitches()) {
            Bus b1 = vl.getBusBreakerView().getBus1(context.getAnonymizer().anonymizeString(sw.getId()));
            Bus b2 = vl.getBusBreakerView().getBus2(context.getAnonymizer().anonymizeString(sw.getId()));
            if (!context.getFilter().test(b1) || !context.getFilter().test(b2)) {
                continue;
            }
            BusBreakerViewSwitchXml.INSTANCE.write(sw, vl, context);
        }
        context.getWriter().writeEndElement();
    }

    private void writeBusBranchTopology(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeStartElement(context.getVersion().getNamespaceURI(), BUS_BREAKER_TOPOLOGY_ELEMENT_NAME);
        for (Bus b : vl.getBusView().getBuses()) {
            if (!context.getFilter().test(b)) {
                continue;
            }
            BusXml.INSTANCE.write(b, null, context);
        }
        context.getWriter().writeEndElement();
    }

    private void writeGenerators(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        for (Generator g : vl.getGenerators()) {
            if (!context.getFilter().test(g)) {
                continue;
            }
            GeneratorXml.INSTANCE.write(g, vl, context);
        }
    }

    private void writeBatteries(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        for (Battery b : vl.getBatteries()) {
            if (!context.getFilter().test(b)) {
                continue;
            }
            BatteryXml.INSTANCE.write(b, vl, context);
        }
    }

    private void writeLoads(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        for (Load l : vl.getLoads()) {
            if (!context.getFilter().test(l)) {
                continue;
            }
            LoadXml.INSTANCE.write(l, vl, context);
        }
    }

    private void writeShuntCompensators(VoltageLevel vl, NetworkXmlWriterContext context) {
        for (ShuntCompensator sc : vl.getShuntCompensators()) {
            if (!context.getFilter().test(sc)) {
                continue;
            }
            IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_1, context, () -> {
                try {
                    ShuntXml.INSTANCE.write(sc, vl, context);
                } catch (XMLStreamException e) {
                    throw new UncheckedXmlStreamException(e);
                }
            });
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> {
                try {
                    if (ShuntCompensatorModelType.LINEAR == sc.getModelType()) {
                        LinearShuntXml.INSTANCE.write(sc, vl, context);
                    } else if (ShuntCompensatorModelType.NON_LINEAR == sc.getModelType()) {
                        NonLinearShuntXml.INSTANCE.write(sc, vl, context);
                    } else {
                        throw new PowsyblException(String.format("Unexpected model type: %s", sc.getModelType()));
                    }
                } catch (XMLStreamException e) {
                    throw new UncheckedXmlStreamException(e);
                }
            });
        }
    }

    private void writeDanglingLines(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        for (DanglingLine dl : vl.getDanglingLines()) {
            if (!context.getFilter().test(dl)) {
                continue;
            }
            DanglingLineXml.INSTANCE.write(dl, vl, context);
        }
    }

    private void writeStaticVarCompensators(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        for (StaticVarCompensator svc : vl.getStaticVarCompensators()) {
            if (!context.getFilter().test(svc)) {
                continue;
            }
            StaticVarCompensatorXml.INSTANCE.write(svc, vl, context);
        }
    }

    private void writeVscConverterStations(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        for (VscConverterStation cs : vl.getVscConverterStations()) {
            if (!context.getFilter().test(cs)) {
                continue;
            }
            VscConverterStationXml.INSTANCE.write(cs, vl, context);
        }
    }

    private void writeLccConverterStations(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        for (LccConverterStation cs : vl.getLccConverterStations()) {
            if (!context.getFilter().test(cs)) {
                continue;
            }
            LccConverterStationXml.INSTANCE.write(cs, vl, context);
        }
    }

    @Override
    protected VoltageLevelAdder createAdder(Substation s) {
        return s.newVoltageLevel();
    }

    @Override
    protected VoltageLevel readRootElementAttributes(VoltageLevelAdder adder, NetworkXmlReaderContext context) {
        double nominalV = XmlUtil.readDoubleAttribute(context.getReader(), "nominalV");
        double lowVoltageLimit = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "lowVoltageLimit");
        double highVoltageLimit = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "highVoltageLimit");
        TopologyKind topologyKind = TopologyKind.valueOf(context.getReader().getAttributeValue(null, "topologyKind"));
        return adder
                .setNominalV(nominalV)
                .setLowVoltageLimit(lowVoltageLimit)
                .setHighVoltageLimit(highVoltageLimit)
                .setTopologyKind(topologyKind)
                .add();
    }

    @Override
    protected void readSubElements(VoltageLevel vl, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
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

                case LinearShuntXml.ROOT_ELEMENT_NAME:
                    LinearShuntXml.INSTANCE.read(vl, context);
                    break;

                case NonLinearShuntXml.ROOT_ELEMENT_NAME:
                    NonLinearShuntXml.INSTANCE.read(vl, context);
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

    private void readNodeBreakerTopology(VoltageLevel vl, NetworkXmlReaderContext context) throws XMLStreamException {
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_1, context, () -> LOGGER.info("attribute " + NODE_BREAKER_TOPOLOGY_ELEMENT_NAME + ".nodeCount is ignored."));
        XmlUtil.readUntilEndElement(NODE_BREAKER_TOPOLOGY_ELEMENT_NAME, context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case BusbarSectionXml.ROOT_ELEMENT_NAME:
                    BusbarSectionXml.INSTANCE.read(vl, context);
                    break;

                case NodeBreakerViewSwitchXml.ROOT_ELEMENT_NAME:
                    NodeBreakerViewSwitchXml.INSTANCE.read(vl, context);
                    break;

                case NodeBreakerViewInternalConnectionXml.ROOT_ELEMENT_NAME:
                    NodeBreakerViewInternalConnectionXml.INSTANCE.read(vl, context);
                    break;

                case BusXml.ROOT_ELEMENT_NAME:
                    readCalculatedBus(vl, context);
                    break;

                default:
                    throw new AssertionError("Unexpected element: " + context.getReader().getLocalName());
            }
        });
    }

    private void readCalculatedBus(VoltageLevel vl, NetworkXmlReaderContext context) {
        IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, BusXml.ROOT_ELEMENT_NAME, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        double v = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "v");
        double angle = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "angle");
        String nodesString = context.getReader().getAttributeValue(null, "nodes");
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

    private void readBusBreakerTopology(VoltageLevel vl, NetworkXmlReaderContext context) throws XMLStreamException {
        XmlUtil.readUntilEndElement(BUS_BREAKER_TOPOLOGY_ELEMENT_NAME, context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case BusXml.ROOT_ELEMENT_NAME:
                    BusXml.INSTANCE.read(vl, context);
                    break;

                case BusBreakerViewSwitchXml.ROOT_ELEMENT_NAME:
                    BusBreakerViewSwitchXml.INSTANCE.read(vl, context);
                    break;

                default:
                    throw new AssertionError("Unexpected element: " + context.getReader().getLocalName());
            }
        });
    }
}
