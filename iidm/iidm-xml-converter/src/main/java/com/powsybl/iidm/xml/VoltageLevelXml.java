/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.common.collect.Lists;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.IidmImportExportType;
import com.powsybl.iidm.network.*;

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

    private boolean isGeneratorsHavingControlValues(VoltageLevel vl, NetworkXmlWriterContext context) {
        for (Generator g : vl.getGenerators()) {
            if (!context.getFilter().test(g)) {
                continue;
            }
            if (GeneratorXml.INSTANCE.hasControlValues(g)) {
                return true;
            }
        }
        return false;
    }

    private boolean isShuntCompensatorsHavingControlValues(VoltageLevel vl, NetworkXmlWriterContext context) {
        for (ShuntCompensator sc : vl.getShuntCompensators()) {
            if (!context.getFilter().test(sc)) {
                continue;
            }
            if (ShuntXml.INSTANCE.hasControlValues(sc)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLoadsHavingControlValues(VoltageLevel vl, NetworkXmlWriterContext context) {
        for (Load l : vl.getLoads()) {
            if (!context.getFilter().test(l)) {
                continue;
            }
            if (LoadXml.INSTANCE.hasControlValues(l)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStaticVarCompensatorsHavingControlValues(VoltageLevel vl, NetworkXmlWriterContext context) {
        for (StaticVarCompensator svc : vl.getStaticVarCompensators()) {
            if (!context.getFilter().test(svc)) {
                continue;
            }
            if (StaticVarCompensatorXml.INSTANCE.hasControlValues(svc)) {
                return true;
            }
        }
        return false;
    }

    private boolean isVscConverterStationsHavingControlValues(VoltageLevel vl, NetworkXmlWriterContext context) {
        for (VscConverterStation cs : vl.getVscConverterStations()) {
            if (!context.getFilter().test(cs)) {
                continue;
            }
            if (VscConverterStationXml.INSTANCE.hasControlValues(cs))  {
                return true;
            }
        }
        return false;
    }

    private boolean isLccConverterStationsHavingControlValues(VoltageLevel vl, NetworkXmlWriterContext context) {
        for (LccConverterStation cs : vl.getLccConverterStations()) {
            if (!context.getFilter().test(cs)) {
                continue;
            }
            if (LccConverterStationXml.INSTANCE.hasControlValues(cs)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDanglingLinesHavingControlValues(VoltageLevel vl, NetworkXmlWriterContext context) {
        for (DanglingLine dl : vl.getDanglingLines()) {
            if (!context.getFilter().test(dl)) {
                continue;
            }
            if (DanglingLineXml.INSTANCE.hasControlValues(dl)) {
                return true;
            }
        }
        return false;
    }

    boolean hasControlValues(VoltageLevel vl, NetworkXmlWriterContext context) {
        return isGeneratorsHavingControlValues(vl, context) ||
                isDanglingLinesHavingControlValues(vl, context) ||
                isShuntCompensatorsHavingControlValues(vl, context) ||
                isLoadsHavingControlValues(vl, context) ||
                isStaticVarCompensatorsHavingControlValues(vl, context) ||
                isVscConverterStationsHavingControlValues(vl, context) ||
                isLccConverterStationsHavingControlValues(vl, context);
    }

    private boolean isGeneratorsHavingStateValues(VoltageLevel vl, NetworkXmlWriterContext context) {
        for (Generator g : vl.getGenerators()) {
            if (!context.getFilter().test(g)) {
                continue;
            }
            if (GeneratorXml.INSTANCE.hasStateValues(g)) {
                return true;
            }
        }
        return false;
    }

    private boolean isShuntCompensatorsHavingStateValues(VoltageLevel vl, NetworkXmlWriterContext context) {
        for (ShuntCompensator sc : vl.getShuntCompensators()) {
            if (!context.getFilter().test(sc)) {
                continue;
            }
            if (ShuntXml.INSTANCE.hasStateValues(sc)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLoadsHavingStateValues(VoltageLevel vl, NetworkXmlWriterContext context) {
        for (Load l : vl.getLoads()) {
            if (!context.getFilter().test(l)) {
                continue;
            }
            if (LoadXml.INSTANCE.hasStateValues(l)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStaticVarCompensatorsHavingStateValues(VoltageLevel vl, NetworkXmlWriterContext context) {
        for (StaticVarCompensator svc : vl.getStaticVarCompensators()) {
            if (!context.getFilter().test(svc)) {
                continue;
            }
            if (StaticVarCompensatorXml.INSTANCE.hasStateValues(svc)) {
                return true;
            }
        }
        return false;
    }

    private boolean isVscConverterStationsHavingStateValues(VoltageLevel vl, NetworkXmlWriterContext context) {
        for (VscConverterStation cs : vl.getVscConverterStations()) {
            if (!context.getFilter().test(cs)) {
                continue;
            }
            if (VscConverterStationXml.INSTANCE.hasStateValues(cs))  {
                return true;
            }
        }
        return false;
    }

    private boolean isLccConverterStationsHavingStateValues(VoltageLevel vl, NetworkXmlWriterContext context) {
        for (LccConverterStation cs : vl.getLccConverterStations()) {
            if (!context.getFilter().test(cs)) {
                continue;
            }
            if (LccConverterStationXml.INSTANCE.hasStateValues(cs)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDanglingLinesHavingStateValues(VoltageLevel vl, NetworkXmlWriterContext context) {
        for (DanglingLine dl : vl.getDanglingLines()) {
            if (!context.getFilter().test(dl)) {
                continue;
            }
            if (DanglingLineXml.INSTANCE.hasStateValues(dl)) {
                return true;
            }
        }
        return false;
    }

    boolean hasStateValues(VoltageLevel vl, NetworkXmlWriterContext context) {
        return isGeneratorsHavingStateValues(vl, context) ||
                isDanglingLinesHavingStateValues(vl, context) ||
                isShuntCompensatorsHavingStateValues(vl, context) ||
                isLoadsHavingStateValues(vl, context) ||
                isStaticVarCompensatorsHavingStateValues(vl, context) ||
                isVscConverterStationsHavingStateValues(vl, context) ||
                isLccConverterStationsHavingStateValues(vl, context);
    }

    boolean hasTopoValues(VoltageLevel s, NetworkXmlWriterContext context) {
        // To do
        return s.getShuntCompensatorCount() > 0;
    }

    @Override
    protected void writeRootElementAttributes(VoltageLevel vl, Substation s, NetworkXmlWriterContext context) throws XMLStreamException {
        if (context.getOptions().getImportExportType() == IidmImportExportType.FULL_IIDM) {
            XmlUtil.writeDouble("nominalV", vl.getNominalV(), context.getWriter());
            XmlUtil.writeDouble("lowVoltageLimit", vl.getLowVoltageLimit(), context.getWriter());
            XmlUtil.writeDouble("highVoltageLimit", vl.getHighVoltageLimit(), context.getWriter());
            TopologyLevel topologyLevel = TopologyLevel.min(vl.getTopologyKind(), context.getOptions().getTopologyLevel());
            context.getWriter().writeAttribute("topologyKind", topologyLevel.getTopologyKind().name());
        }
    }

    @Override
    protected void writeSubElements(VoltageLevel vl, Substation s, NetworkXmlWriterContext context) throws XMLStreamException {
        TopologyLevel topologyLevel = TopologyLevel.min(vl.getTopologyKind(), context.getOptions().getTopologyLevel());
        if ((topologyLevel == TopologyLevel.NODE_BREAKER && context.getTargetFile() == IncrementalIidmFiles.TOPO) ||
                (topologyLevel != TopologyLevel.NODE_BREAKER && context.getTargetFile() == IncrementalIidmFiles.STATE)){
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
        }

        writeGenerators(vl, context, topologyLevel);
        writeLoads(vl, context, topologyLevel);
        writeShuntCompensators(vl, context, topologyLevel);
        writeDanglingLines(vl, context, topologyLevel);
        writeStaticVarCompensators(vl, context, topologyLevel);
        writeVscConverterStations(vl, context);
        writeLccConverterStations(vl, context);
    }

    private void writeNodeBreakerTopology(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        if (context.getTargetFile() == IncrementalIidmFiles.TOPO && vl.getNodeBreakerView().getSwitchCount() == 0 ||
                context.getTargetFile() == IncrementalIidmFiles.STATE && vl.getNodeBreakerView().getBusbarSectionCount() == 0) {
            return;
        }
        context.getWriter().writeStartElement(IIDM_URI, NODE_BREAKER_TOPOLOGY_ELEMENT_NAME);
        if (context.getOptions().getImportExportType() == IidmImportExportType.FULL_IIDM) {
            context.getWriter().writeAttribute("nodeCount", Integer.toString(vl.getNodeBreakerView().getNodeCount()));
        }
        if (context.getOptions().getImportExportType() == IidmImportExportType.FULL_IIDM || context.getTargetFile() == IncrementalIidmFiles.STATE) {
            for (BusbarSection bs : vl.getNodeBreakerView().getBusbarSections()) {
                BusbarSectionXml.INSTANCE.write(bs, null, context);
            }
        }
        if (context.getOptions().getImportExportType() == IidmImportExportType.FULL_IIDM || context.getTargetFile() == IncrementalIidmFiles.TOPO) {
            for (Switch sw : vl.getNodeBreakerView().getSwitches()) {
                NodeBreakerViewSwitchXml.INSTANCE.write(sw, vl, context);
            }
        }
        writeNodeBreakerTopologyInternalConnections(vl, context);
        context.getWriter().writeEndElement();
    }

    private void writeNodeBreakerTopologyInternalConnections(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        for (VoltageLevel.NodeBreakerView.InternalConnection ic : vl.getNodeBreakerView().getInternalConnections()) {
            NodeBreakerViewInternalConnectionXml.INSTANCE.write(ic.getNode1(), ic.getNode2(), context);
        }
    }

    private void writeBusBreakerTopology(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        if (context.getTargetFile() == IncrementalIidmFiles.TOPO && (vl.getBusBreakerView().getSwitchCount() == 0 &&
                Lists.newArrayList(vl.getBusBreakerView().getBuses()).isEmpty())) {
            return;
        }
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

    private void writeGenerators(VoltageLevel vl, NetworkXmlWriterContext context, TopologyLevel topologyLevel) throws XMLStreamException {
        if (context.getTargetFile() == IncrementalIidmFiles.TOPO && topologyLevel == TopologyLevel.NODE_BREAKER) {
            return;
        }
        for (Generator g : vl.getGenerators()) {
            if (!context.getFilter().test(g) ||
                    (!GeneratorXml.INSTANCE.hasControlValues(g) && context.getTargetFile() == IncrementalIidmFiles.CONTROL) ||
                    (!GeneratorXml.INSTANCE.hasStateValues(g) && context.getTargetFile() == IncrementalIidmFiles.STATE)) {
                continue;
            }
            GeneratorXml.INSTANCE.write(g, vl, context);
        }
    }

    private void writeLoads(VoltageLevel vl, NetworkXmlWriterContext context, TopologyLevel topologyLevel) throws XMLStreamException {
        if ((context.getTargetFile() == IncrementalIidmFiles.TOPO && topologyLevel == TopologyLevel.NODE_BREAKER) ||
                (context.getTargetFile() == IncrementalIidmFiles.CONTROL)) {
            return;
        }
        for (Load l : vl.getLoads()) {
            if (!context.getFilter().test(l) ||
                    (context.getTargetFile() == IncrementalIidmFiles.CONTROL) ||
                    (!LoadXml.INSTANCE.hasStateValues(l) && context.getTargetFile() == IncrementalIidmFiles.STATE)) {
                continue;
            }
            LoadXml.INSTANCE.write(l, vl, context);
        }
    }

    private void writeShuntCompensators(VoltageLevel vl, NetworkXmlWriterContext context, TopologyLevel topologyLevel) throws XMLStreamException {
        if (context.getTargetFile() == IncrementalIidmFiles.TOPO && topologyLevel == TopologyLevel.NODE_BREAKER) {
            return;
        }
        for (ShuntCompensator sc : vl.getShuntCompensators()) {
            if (!context.getFilter().test(sc) ||
                    (!ShuntXml.INSTANCE.hasStateValues(sc) && context.getTargetFile() == IncrementalIidmFiles.STATE)) {
                continue;
            }
            ShuntXml.INSTANCE.write(sc, vl, context);
        }
    }

    private void writeDanglingLines(VoltageLevel vl, NetworkXmlWriterContext context, TopologyLevel topologyLevel) throws XMLStreamException {
        if ((context.getTargetFile() == IncrementalIidmFiles.TOPO && topologyLevel == TopologyLevel.NODE_BREAKER) ||
                (context.getTargetFile() == IncrementalIidmFiles.CONTROL)) {
            return;
        }
        for (DanglingLine dl : vl.getDanglingLines()) {
            if (!context.getFilter().test(dl) ||
                    (context.getTargetFile() == IncrementalIidmFiles.CONTROL) ||
                    (!DanglingLineXml.INSTANCE.hasStateValues(dl) && context.getTargetFile() == IncrementalIidmFiles.STATE)) {
                continue;
            }
            DanglingLineXml.INSTANCE.write(dl, vl, context);
        }
    }

    private void writeStaticVarCompensators(VoltageLevel vl, NetworkXmlWriterContext context, TopologyLevel topologyLevel) throws XMLStreamException {
        if ((context.getTargetFile() == IncrementalIidmFiles.TOPO && topologyLevel == TopologyLevel.NODE_BREAKER) ||
                (context.getTargetFile() == IncrementalIidmFiles.STATE)) {
            return;
        }
        for (StaticVarCompensator svc : vl.getStaticVarCompensators()) {
            if (!context.getFilter().test(svc) ||
                    (!StaticVarCompensatorXml.INSTANCE.hasStateValues(svc) && context.getTargetFile() == IncrementalIidmFiles.STATE)) {
                continue;
            }
            StaticVarCompensatorXml.INSTANCE.write(svc, vl, context);
        }
    }

    private void writeVscConverterStations(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        for (VscConverterStation cs : vl.getVscConverterStations()) {
            if (!context.getFilter().test(cs) ||
                    (!VscConverterStationXml.INSTANCE.hasStateValues(cs) && context.getTargetFile() == IncrementalIidmFiles.STATE)) {
                continue;
            }
            VscConverterStationXml.INSTANCE.write(cs, vl, context);
        }
    }

    private void writeLccConverterStations(VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        if (context.getTargetFile() == IncrementalIidmFiles.CONTROL) {
            return;
        }
        for (LccConverterStation cs : vl.getLccConverterStations()) {
            if (!context.getFilter().test(cs) ||
                    (!LccConverterStationXml.INSTANCE.hasStateValues(cs) && context.getTargetFile() == IncrementalIidmFiles.STATE)) {
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
