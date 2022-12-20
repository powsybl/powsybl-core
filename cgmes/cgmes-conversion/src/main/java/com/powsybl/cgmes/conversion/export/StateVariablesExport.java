/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.extensions.CgmesTapChanger;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.util.SwitchesFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class StateVariablesExport extends AbstractCgmesExporter {

    private static final String SV_VOLTAGE_ANGLE = "SvVoltage.angle";
    private static final String SV_VOLTAGE_V = "SvVoltage.v";
    private static final String SV_VOLTAGE_TOPOLOGICAL_NODE = "SvVoltage.TopologicalNode";

    private static final Logger LOG = LoggerFactory.getLogger(StateVariablesExport.class);

    StateVariablesExport(CgmesExportContext context, XMLStreamWriter xmlWriter) {
        super(context, xmlWriter);
    }

    @Override
    public void export() {
        try {
            CgmesExportUtil.writeRdfRoot(cimNamespace, context.getCim().getEuPrefix(), context.getCim().getEuNamespace(), xmlWriter);

            if (context.getCimVersion() >= 16) {
                CgmesExportUtil.writeModelDescription(xmlWriter, context.getSvModelDescription(), context);
                writeTopologicalIslands();
                // Note: unmapped topological nodes (node breaker) & boundary topological nodes are not written in topological islands
            }

            writeVoltagesForTopologicalNodes();
            writeVoltagesForBoundaryNodes();
            writePowerFlows();
            writeShuntCompensatorSections();
            writeTapSteps();
            writeStatus();
            writeConverters();

            xmlWriter.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private void writeTopologicalIslands() throws XMLStreamException {
        Map<String, String> angleRefs = buildAngleRefs();
        Map<String, List<String>> islands = buildIslands();
        for (Map.Entry<String, List<String>> island : islands.entrySet()) {
            if (!angleRefs.containsKey(island.getKey())) {
                Supplier<String> log = () -> String.format("Synchronous component  %s does not have a defined slack bus: it is ignored", island.getKey());
                LOG.info(log.get());
                continue;
            }
            String islandId = CgmesExportUtil.getUniqueId();
            writeStartId(CgmesNames.TOPOLOGICAL_ISLAND, islandId, false);
            xmlWriter.writeStartElement(cimNamespace, CgmesNames.NAME);
            xmlWriter.writeCharacters(islandId); // Use id as name
            xmlWriter.writeEndElement();
            writeReference("TopologicalIsland.AngleRefTopologicalNode", angleRefs.get(island.getKey()));
            for (String tn : island.getValue()) {
                writeReference("TopologicalIsland.TopologicalNodes", tn);
            }
            xmlWriter.writeEndElement();
        }
    }

    private Map<String, String> buildAngleRefs() {
        Map<String, String> angleRefs = new HashMap<>();
        for (VoltageLevel vl : context.getNetwork().getVoltageLevels()) {
            SlackTerminal slackTerminal = vl.getExtension(SlackTerminal.class);
            buildAngleRefs(slackTerminal, angleRefs);
        }
        return angleRefs;
    }

    private void buildAngleRefs(SlackTerminal slackTerminal, Map<String, String> angleRefs) {
        if (slackTerminal != null && slackTerminal.getTerminal() != null) {
            Bus bus = slackTerminal.getTerminal().getBusBreakerView().getBus();
            if (bus != null && bus.getSynchronousComponent() != null) {
                buildAngleRefs(bus.getSynchronousComponent().getNum(), bus.getId(), angleRefs);
            } else if (bus != null) {
                buildAngleRefs(bus.getId(), angleRefs);
            } else {
                Supplier<String> message = () -> String.format("Slack terminal at equipment %s is not connected and is not exported as slack terminal", slackTerminal.getTerminal().getConnectable().getId());
                LOG.info(message.get());
            }
        }
    }

    private void buildAngleRefs(int synchronousComponentNum, String topologicalNodeId, Map<String, String> angleRefs) {
        String componentNum = String.valueOf(synchronousComponentNum);
        if (angleRefs.containsKey(componentNum)) {
            Supplier<String> log = () -> String.format("Several slack buses are defined for synchronous component %s: only first slack bus (%s) is taken into account",
                    componentNum, angleRefs.get(componentNum));
            LOG.info(log.get());
            return;
        }
        angleRefs.put(componentNum, topologicalNodeId);
    }

    private static void buildAngleRefs(String topologicalNodeId, Map<String, String> angleRefs) {
        angleRefs.put(topologicalNodeId, topologicalNodeId);
    }

    private Map<String, List<String>> buildIslands() {
        Map<String, List<String>> islands = new HashMap<>();
        for (Bus b : context.getNetwork().getBusBreakerView().getBuses()) {
            if (b.getSynchronousComponent() != null) {
                int num = b.getSynchronousComponent().getNum();
                islands.computeIfAbsent(String.valueOf(num), i -> new ArrayList<>());
                islands.get(String.valueOf(num)).add(b.getId());
            } else {
                islands.put(b.getId(), Collections.singletonList(b.getId()));
            }
        }
        return islands;
    }

    private void writeVoltagesForTopologicalNodes() throws XMLStreamException {
        for (Bus b : context.getNetwork().getBusBreakerView().getBuses()) {
            writeVoltage(b.getId(), b.getV(), b.getAngle());
        }
    }

    private void writeVoltagesForBoundaryNodes() throws XMLStreamException {
        for (DanglingLine dl : context.getNetwork().getDanglingLines()) {
            Bus b = dl.getTerminal().getBusView().getBus();
            String topologicalNode = dl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + dl.getId() + "." + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY);
            if (topologicalNode != null) {
                if (dl.hasProperty("v") && dl.hasProperty("angle")) {
                    writeVoltage(topologicalNode, Double.parseDouble(dl.getProperty("v", "NaN")), Double.parseDouble(dl.getProperty("angle", "NaN")));
                } else if (b != null) {
                    writeVoltage(topologicalNode, dl.getBoundary().getV(), dl.getBoundary().getAngle());
                } else {
                    writeVoltage(topologicalNode, 0.0, 0.0);
                }
            }
        }
        // Voltages at inner nodes of Tie Lines
        // (boundary nodes that have been left inside CGM)
        for (Line l : context.getNetwork().getLines()) {
            if (!l.isTieLine()) {
                continue;
            }
            TieLine tieLine = (TieLine) l;
            String topologicalNode = tieLine.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY)
                    .orElseGet(() -> tieLine.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tieLine.getHalf1().getId() + "." + CgmesNames.TOPOLOGICAL_NODE));
            if (topologicalNode != null) {
                writeVoltage(topologicalNode, tieLine.getHalf1().getBoundary().getV(), tieLine.getHalf1().getBoundary().getAngle());
            }
        }
    }

    private void writeVoltage(String topologicalNode, double v, double angle) throws XMLStreamException {
        writeStartId("SvVoltage", CgmesExportUtil.getUniqueId(), false);
        xmlWriter.writeStartElement(cimNamespace, SV_VOLTAGE_ANGLE);
        xmlWriter.writeCharacters(CgmesExportUtil.format(angle));
        xmlWriter.writeEndElement();
        xmlWriter.writeStartElement(cimNamespace, SV_VOLTAGE_V);
        xmlWriter.writeCharacters(CgmesExportUtil.format(v));
        xmlWriter.writeEndElement();
        CgmesExportUtil.writeReference(SV_VOLTAGE_TOPOLOGICAL_NODE, topologicalNode, cimNamespace, xmlWriter);
        xmlWriter.writeEndElement();
    }

    private void writePowerFlows() {
        writeInjectionsPowerFlows(Network::getLoadStream);
        writeInjectionsPowerFlows(Network::getGeneratorStream);
        writeInjectionsPowerFlows(Network::getShuntCompensatorStream);
        writeInjectionsPowerFlows(Network::getStaticVarCompensatorStream);
        writeInjectionsPowerFlows(Network::getBatteryStream);

        // Fictitious loads are not exported as Equipment, they are just added to SV as SvInjection
        for (Load load : context.getNetwork().getLoads()) {
            if (load.isFictitious()) {
                writeSvInjection(load);
            }
        }

        Map<String, Double> equivalentInjectionTerminalP = new HashMap<>();
        Map<String, Double> equivalentInjectionTerminalQ = new HashMap<>();
        context.getNetwork().getDanglingLineStream().forEach(dl -> {
            // FIXME: the values (p0/q0) are wrong: these values are target and never updated, not calculated flows
            // DanglingLine's attributes will be created to store calculated flows on the boundary side
            if (context.exportBoundaryPowerFlows()) {
                writePowerFlowTerminalFromAlias(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary", dl.getBoundary().getP(), dl.getBoundary().getQ());
            }
            writePowerFlowTerminalFromAlias(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Network", dl.getTerminal().getP(), dl.getTerminal().getQ());
            equivalentInjectionTerminalP.compute(context.getNamingStrategy().getCgmesIdFromProperty(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal"), (k, v) -> v == null ? -dl.getBoundary().getP() : v - dl.getBoundary().getP());
            equivalentInjectionTerminalQ.compute(context.getNamingStrategy().getCgmesIdFromProperty(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal"), (k, v) -> v == null ? -dl.getBoundary().getQ() : v - dl.getBoundary().getQ());
        });
        equivalentInjectionTerminalP.keySet().forEach(eiId -> writePowerFlow(eiId, equivalentInjectionTerminalP.get(eiId), equivalentInjectionTerminalQ.get(eiId)));

        context.getNetwork().getBranchStream().forEach(b -> {
            writePowerFlowTerminalFromAlias(b, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, b.getTerminal1().getP(), b.getTerminal1().getQ());
            writePowerFlowTerminalFromAlias(b, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2, b.getTerminal2().getP(), b.getTerminal2().getQ());
            if (b instanceof TieLine && context.exportBoundaryPowerFlows()) {
                TieLine tl = (TieLine) b;
                writePowerFlowTerminalFromProperty(tl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tl.getHalf1().getId() + ".Terminal_Network",
                        tl.getTerminal1().getP(), tl.getTerminal1().getQ());
                writePowerFlowTerminalFromProperty(tl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tl.getHalf2().getId() + ".Terminal_Network",
                        tl.getTerminal2().getP(), tl.getTerminal2().getQ());
                writePowerFlowTerminalFromAlias(tl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "HALF1." + CgmesNames.TERMINAL + "_Boundary", tl.getHalf1().getBoundary().getP(), tl.getHalf1().getBoundary().getQ());
                writePowerFlowTerminalFromAlias(tl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "HALF2." + CgmesNames.TERMINAL + "_Boundary", tl.getHalf2().getBoundary().getP(), tl.getHalf2().getBoundary().getQ());
            }
        });

        context.getNetwork().getThreeWindingsTransformerStream().forEach(twt -> {
            writePowerFlowTerminalFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, twt.getLeg1().getTerminal().getP(), twt.getLeg1().getTerminal().getQ());
            writePowerFlowTerminalFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2, twt.getLeg2().getTerminal().getP(), twt.getLeg2().getTerminal().getQ());
            writePowerFlowTerminalFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL3, twt.getLeg3().getTerminal().getP(), twt.getLeg3().getTerminal().getQ());
        });

        if (context.exportFlowsForSwitches()) {
            context.getNetwork().getVoltageLevelStream().forEach(vl -> {
                SwitchesFlow swflows = new SwitchesFlow(vl);
                vl.getSwitches().forEach(sw -> {
                    if (context.isExportedEquipment(sw) && swflows.hasFlow(sw.getId())) {
                        writePowerFlowTerminalFromAlias(sw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, swflows.getP1(sw.getId()), swflows.getQ1(sw.getId()));
                        writePowerFlowTerminalFromAlias(sw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2, swflows.getP2(sw.getId()), swflows.getQ2(sw.getId()));
                    }
                });
            });
        }
    }

    private <I extends Injection<I>> void writeInjectionsPowerFlows(Function<Network, Stream<I>> getInjectionStream) {
        getInjectionStream.apply(context.getNetwork()).forEach(i -> {
            if (context.isExportedEquipment(i)) {
                writePowerFlow(i.getTerminal());
            }
        });
    }

    private void writePowerFlow(Terminal terminal) {
        String cgmesTerminal = CgmesExportUtil.getTerminalId(terminal, context);
        if (cgmesTerminal != null) {
            writePowerFlow(cgmesTerminal, getTerminalP(terminal), terminal.getQ());
        } else {
            LOG.error("No defined CGMES terminal for {}", terminal.getConnectable().getId());
        }
    }

    private static double getTerminalP(Terminal terminal) {
        double p = terminal.getP();
        if (!Double.isNaN(p)) {
            return p;
        }
        // P is NaN
        if (Double.isNaN(terminal.getQ())) {
            return p;
        }
        // P is NaN and Q != NaN
        if (terminal.getConnectable() instanceof StaticVarCompensator) {
            return 0.0;
        }
        return p;
    }

    private void writeSvInjection(Load load) {
        // Fictitious loads are created in IIDM to keep track of mismatches in the input case,
        // These mismatches are given by SvInjection CGMES objects
        // These loads have been taken into account as inputs for potential power flow analysis
        // They will be written back as SvInjection objects in the SV profile
        // We do not want to export them back as new objects in the EQ profile
        Bus bus = load.getTerminal().getBusBreakerView().getBus();
        if (bus == null) {
            LOG.warn("Fictitious load does not have a BusView bus. No SvInjection is written");
        } else {
            // SvInjection will be assigned to the first of the TNs mapped to the bus
            writeSvInjection(load, bus.getId());
        }
    }

    private void writePowerFlowTerminalFromAlias(Identifiable<?> c, String aliasTypeForTerminalId, double p, double q) {
        // Export only if we have a terminal identifier
        if (c.getAliasFromType(aliasTypeForTerminalId).isPresent()) {
            String cgmesTerminalId = context.getNamingStrategy().getCgmesIdFromAlias(c, aliasTypeForTerminalId);
            writePowerFlow(cgmesTerminalId, p, q);
        }
    }

    private void writePowerFlowTerminalFromProperty(Identifiable<?> c, String propertyNameForTerminalId, double p, double q) {
        // Export only if we have a terminal identifier
        String cgmesTerminalId = context.getNamingStrategy().getCgmesIdFromProperty(c, propertyNameForTerminalId);
        if (cgmesTerminalId != null) {
            writePowerFlow(cgmesTerminalId, p, q);
        }
    }

    private void writePowerFlow(String cgmesTerminalId, double p, double q) {
        // Export only if flow is a number
        if (Double.isNaN(p) && Double.isNaN(q)) {
            return;
        }
        try {
            writeStartId("SvPowerFlow", CgmesExportUtil.getUniqueId(), false);
            xmlWriter.writeStartElement(cimNamespace, "SvPowerFlow.p");
            xmlWriter.writeCharacters(CgmesExportUtil.format(p));
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement(cimNamespace, "SvPowerFlow.q");
            xmlWriter.writeCharacters(CgmesExportUtil.format(q));
            xmlWriter.writeEndElement();
            writeReference("SvPowerFlow.Terminal", cgmesTerminalId);
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private void writeSvInjection(Load svInjection, String topologicalNode) {
        try {
            writeStartId("SvInjection", context.getNamingStrategy().getCgmesId(svInjection), false);
            xmlWriter.writeStartElement(cimNamespace, "SvInjection.pInjection");
            xmlWriter.writeCharacters(CgmesExportUtil.format(svInjection.getP0()));
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement(cimNamespace, "SvInjection.qInjection");
            xmlWriter.writeCharacters(CgmesExportUtil.format(svInjection.getQ0()));
            xmlWriter.writeEndElement();
            writeReference("SvInjection.TopologicalNode", topologicalNode);
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private void writeShuntCompensatorSections() throws XMLStreamException {
        for (ShuntCompensator s : context.getNetwork().getShuntCompensators()) {
            writeStartId("SvShuntCompensatorSections", CgmesExportUtil.getUniqueId(), false);
            writeReference("SvShuntCompensatorSections.ShuntCompensator", context.getNamingStrategy().getCgmesId(s));
            xmlWriter.writeStartElement(cimNamespace, "SvShuntCompensatorSections.sections");
            xmlWriter.writeCharacters(CgmesExportUtil.format(s.getSectionCount()));
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
        }
    }

    private void writeTapSteps() throws XMLStreamException {
        for (TwoWindingsTransformer twt : context.getNetwork().getTwoWindingsTransformers()) {
            // For two-windings transformers tap changer may be at end number 1 or 2
            // If we have exported the EQ the tap changer may have been moved from end 2 to end 1, where IIDM has modelled it.
            // If we are exporting only the SV the tap changer alias to use is the one of the original location
            if (twt.hasPhaseTapChanger()) {
                int endNumber = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 1).isPresent() ? 1 : 2;
                String ptcId = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + endNumber);
                writeSvTapStep(ptcId, twt.getPhaseTapChanger().getTapPosition());
                writeSvTapStepHidden(twt, ptcId);
            } else if (twt.hasRatioTapChanger()) {
                int endNumber = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 1).isPresent() ? 1 : 2;
                String rtcId = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + endNumber);
                writeSvTapStep(rtcId, twt.getRatioTapChanger().getTapPosition());
                writeSvTapStepHidden(twt, rtcId);
            }
        }

        for (ThreeWindingsTransformer twt : context.getNetwork().getThreeWindingsTransformers()) {
            int endNumber = 1;
            for (ThreeWindingsTransformer.Leg leg : Arrays.asList(twt.getLeg1(), twt.getLeg2(), twt.getLeg3())) {
                if (leg.hasPhaseTapChanger()) {
                    String ptcId = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + endNumber);
                    writeSvTapStep(ptcId, leg.getPhaseTapChanger().getTapPosition());
                    writeSvTapStepHidden(twt, ptcId);
                } else if (leg.hasRatioTapChanger()) {
                    String rtcId = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + endNumber);
                    writeSvTapStep(rtcId, leg.getRatioTapChanger().getTapPosition());
                    writeSvTapStepHidden(twt, rtcId);
                }
                endNumber++;
            }
        }
    }

    private <C extends Connectable<C>> void writeSvTapStepHidden(Connectable<C> eq, String tcId) throws XMLStreamException {
        CgmesTapChangers<C> cgmesTcs = eq.getExtension(CgmesTapChangers.class);
        // If we are exporting equipment definitions the hidden tap changer will not be exported
        // because it has been included in the model for the only tap changer left in IIDM
        // If we are exporting only SSH, SV, ... we have to write the step we have saved for it
        if (cgmesTcs != null && !context.isExportEquipment()) {
            for (CgmesTapChanger cgmesTc : cgmesTcs.getTapChangers()) {
                if (cgmesTc.isHidden() && cgmesTc.getCombinedTapChangerId().equals(tcId)) {
                    int step = cgmesTc.getStep().orElseThrow(() -> new PowsyblException("Non null step expected for tap changer " + cgmesTc.getId()));
                    writeSvTapStep(cgmesTc.getId(), step);
                }
            }
        }
    }

    private void writeSvTapStep(String tapChangerId, int tapPosition) throws XMLStreamException {
        writeStartId("SvTapStep", CgmesExportUtil.getUniqueId(), false);
        xmlWriter.writeStartElement(cimNamespace, "SvTapStep.position");
        xmlWriter.writeCharacters(CgmesExportUtil.format(tapPosition));
        xmlWriter.writeEndElement();
        writeReference("SvTapStep.TapChanger", tapChangerId);
        xmlWriter.writeEndElement();
    }

    private void writeStatus() {
        // create SvStatus, iterate on Connectables, check Terminal status, add to SvStatus
        context.getNetwork().getConnectableStream().forEach(c -> {
            if (context.isExportedEquipment(c)) {
                writeConnectableStatus(c);
            }
        });

        // RK: For dangling lines (boundaries), the AC Line Segment is considered in service if and only if it is connected on the network side.
        // If it is disconnected on the boundary side, it might not appear on the SV file.
    }

    private void writeConnectableStatus(Connectable<?> connectable) {
        writeStatus(Boolean.toString(connectable.getTerminals().stream().anyMatch(Terminal::isConnected)), context.getNamingStrategy().getCgmesId(connectable));
    }

    private void writeStatus(String inService, String conductingEquipmentId) {
        try {
            writeStartId("SvStatus", CgmesExportUtil.getUniqueId(), false);
            xmlWriter.writeStartElement(cimNamespace, "SvStatus.inService");
            xmlWriter.writeCharacters(inService);
            xmlWriter.writeEndElement();
            writeReference("SvStatus.ConductingEquipment", conductingEquipmentId);
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private void writeConverters() throws XMLStreamException {
        for (HvdcConverterStation<?> converterStation : context.getNetwork().getHvdcConverterStations()) {
            writeStartAbout(CgmesExportUtil.converterClassName(converterStation), context.getNamingStrategy().getCgmesId(converterStation));
            xmlWriter.writeStartElement(cimNamespace, "ACDCConverter.poleLossP");
            xmlWriter.writeCharacters(CgmesExportUtil.format(getPoleLossP(converterStation)));
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement(cimNamespace, "ACDCConverter.idc");
            xmlWriter.writeCharacters(CgmesExportUtil.format(0));
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement(cimNamespace, "ACDCConverter.uc");
            xmlWriter.writeCharacters(CgmesExportUtil.format(0));
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement(cimNamespace, "ACDCConverter.udc");
            xmlWriter.writeCharacters(CgmesExportUtil.format(0));
            xmlWriter.writeEndElement();
            if (converterStation instanceof LccConverterStation) {
                xmlWriter.writeStartElement(cimNamespace, "CsConverter.alpha");
                xmlWriter.writeCharacters(CgmesExportUtil.format(0));
                xmlWriter.writeEndElement();
                xmlWriter.writeStartElement(cimNamespace, "CsConverter.gamma");
                xmlWriter.writeCharacters(CgmesExportUtil.format(0));
                xmlWriter.writeEndElement();
            } else if (converterStation instanceof VscConverterStation) {
                xmlWriter.writeStartElement(cimNamespace, "VsConverter.delta");
                xmlWriter.writeCharacters(CgmesExportUtil.format(0));
                xmlWriter.writeEndElement();
                xmlWriter.writeStartElement(cimNamespace, "VsConverter.uf");
                xmlWriter.writeCharacters(CgmesExportUtil.format(0));
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();
        }
    }

    private static double getPoleLossP(HvdcConverterStation<?> converterStation) {
        double poleLoss;
        if (CgmesExportUtil.isConverterStationRectifier(converterStation)) {
            double p = converterStation.getTerminal().getP();
            if (Double.isNaN(p)) {
                p = converterStation.getHvdcLine().getActivePowerSetpoint();
            }
            poleLoss = p * converterStation.getLossFactor() / 100;
        } else {
            double p = converterStation.getTerminal().getP();
            if (Double.isNaN(p)) {
                p = converterStation.getHvdcLine().getActivePowerSetpoint();
            }
            double otherConverterStationLossFactor = converterStation.getOtherConverterStation().map(HvdcConverterStation::getLossFactor).orElse(0.0f);
            double pDCInverter = Math.abs(p) * (1 - otherConverterStationLossFactor / 100);
            poleLoss = pDCInverter * converterStation.getLossFactor() / 100;
        }
        return poleLoss;
    }
}
