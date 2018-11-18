/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import javax.xml.stream.XMLStreamException;

import static com.powsybl.iidm.xml.IidmXmlConstants.IIDM_URI;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class VoltageLevelXml extends AbstractIdentifiableXml<VoltageLevel, VoltageLevelAdder, Substation> {

    static final VoltageLevelXml INSTANCE = new VoltageLevelXml();

    static final String ROOT_ELEMENT_NAME = "voltageLevel";

    static final String NODE_BREAKER_TOPOLOGY_ELEMENT_NAME = "nodeBreakerTopology";

    static final String BUS_BREAKER_TOPOLOGY_ELEMENT_NAME = "busBreakerTopology";

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
        writeLoads(vl, context);
        writeShuntCompensators(vl, context);
        writeDanglingLines(vl, context);
        writeStaticVarCompensators(vl, context);
        writeVscConverterStations(vl, context);
        writeLccConverterStations(vl, context);
    }

    private void writeNodeBreakerTopology(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeStartElement(IIDM_URI, NODE_BREAKER_TOPOLOGY_ELEMENT_NAME);
        context.getWriter().writeAttribute("nodeCount", Integer.toString(vl.getNodeBreakerView().getNodeCount()));
        for (BusbarSection bs : vl.getNodeBreakerView().getBusbarSections()) {
            BusbarSectionXml.INSTANCE.write(bs, null, context);
        }
        for (Switch sw : vl.getNodeBreakerView().getSwitches()) {
            NodeBreakerViewSwitchXml.INSTANCE.write(sw, vl, context);
        }
        writeNodeBreakerTopologyInternalConnections(vl, context);
        context.getWriter().writeEndElement();
    }

    private void writeNodeBreakerTopologyInternalConnections(VoltageLevel vl, NetworkXmlWriterContext context) {
        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();
        int[] nodes = topo.getNodes();
        // There is no way in IIDM to obtain the list of internal connections,
        // we have to traverse all connectivity and consider an internal connection
        // when there are two nodes linked with an edge that does not have an
        // associated object
        final TIntSet explored = new TIntHashSet();
        for (int n : nodes) {
            if (explored.contains(n) || topo.getTerminal(n) == null) {
                continue;
            }
            explored.add(n);
            topo.traverse(n, (n1, sw, n2) -> {
                explored.add(n2);
                if (sw == null) {
                    writeNodeBreakerTopologyInternalConnection(n1, n2, context);
                }
                return topo.getTerminal(n2) == null;
            });
        }
    }

    private void writeNodeBreakerTopologyInternalConnection(int n1, int n2, NetworkXmlWriterContext context) {
        try {
            NodeBreakerViewInternalConnectionXml.INSTANCE.write(n1, n2, context);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private void writeBusBreakerTopology(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeStartElement(IIDM_URI, BUS_BREAKER_TOPOLOGY_ELEMENT_NAME);
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
        context.getWriter().writeStartElement(IIDM_URI, BUS_BREAKER_TOPOLOGY_ELEMENT_NAME);
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

    private void writeLoads(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        for (Load l : vl.getLoads()) {
            if (!context.getFilter().test(l)) {
                continue;
            }
            LoadXml.INSTANCE.write(l, vl, context);
        }
    }

    private void writeShuntCompensators(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        for (ShuntCompensator sc : vl.getShuntCompensators()) {
            if (!context.getFilter().test(sc)) {
                continue;
            }
            ShuntXml.INSTANCE.write(sc, vl, context);
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
                    int nodeCount = XmlUtil.readIntAttribute(context.getReader(), "nodeCount");
                    vl.getNodeBreakerView().setNodeCount(nodeCount);
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

                            default:
                                throw new AssertionError();
                        }
                    });
                    break;

                case BUS_BREAKER_TOPOLOGY_ELEMENT_NAME:
                    XmlUtil.readUntilEndElement(BUS_BREAKER_TOPOLOGY_ELEMENT_NAME, context.getReader(), () -> {
                        switch (context.getReader().getLocalName()) {
                            case BusXml.ROOT_ELEMENT_NAME:
                                BusXml.INSTANCE.read(vl, context);
                                break;

                            case BusBreakerViewSwitchXml.ROOT_ELEMENT_NAME:
                                BusBreakerViewSwitchXml.INSTANCE.read(vl, context);
                                break;

                            default:
                                throw new AssertionError();
                        }
                    });
                    break;

                case GeneratorXml.ROOT_ELEMENT_NAME:
                    GeneratorXml.INSTANCE.read(vl, context);
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
}
