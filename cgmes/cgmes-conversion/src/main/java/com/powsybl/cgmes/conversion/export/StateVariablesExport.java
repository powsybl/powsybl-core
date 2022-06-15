/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.extensions.CgmesIidmMapping;
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
import java.util.stream.Collectors;
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
        context.setExportEquipment(false);
    }

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
            for (CgmesIidmMapping.CgmesTopologicalNode tn : context.getUnmappedTopologicalNodes()) {
                writeVoltage(tn.getCgmesId(), 0.0, 0.0);
            }
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
            Bus bus = slackTerminal.getTerminal().getBusView().getBus();
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

    private void buildAngleRefs(int synchronousComponentNum, String busId, Map<String, String> angleRefs) {
        String componentNum = String.valueOf(synchronousComponentNum);
        if (angleRefs.containsKey(componentNum)) {
            Supplier<String> log = () -> String.format("Several slack buses are defined for synchronous component %s: only first slack bus (%s) is taken into account",
                    componentNum, angleRefs.get(componentNum));
            LOG.info(log.get());
            return;
        }
        CgmesIidmMapping.CgmesTopologicalNode topologicalNode = context.getTopologicalNodesByBusViewBus(busId).iterator().next();
        angleRefs.put(componentNum, topologicalNode.getCgmesId());
    }

    private void buildAngleRefs(String busId, Map<String, String> angleRefs) {
        CgmesIidmMapping.CgmesTopologicalNode topologicalNode = context.getTopologicalNodesByBusViewBus(busId).iterator().next();
        angleRefs.put(topologicalNode.getCgmesId(),
                topologicalNode.getCgmesId());
    }

    private Map<String, List<String>> buildIslands() {
        Map<String, List<String>> islands = new HashMap<>();
        for (Bus b : context.getNetwork().getBusView().getBuses()) {
            if (b.getSynchronousComponent() != null) {
                int num = b.getSynchronousComponent().getNum();
                islands.computeIfAbsent(String.valueOf(num), i -> new ArrayList<>());
                islands.get(String.valueOf(num)).addAll(context.getTopologicalNodesByBusViewBus(b.getId()).stream().map(CgmesIidmMapping.CgmesTopologicalNode::getCgmesId).collect(Collectors.toSet()));
            } else {
                islands.put(b.getId(), Collections.singletonList(b.getId()));
            }
        }
        return islands;
    }

    private void writeVoltagesForTopologicalNodes() throws XMLStreamException {
        for (Bus b : context.getNetwork().getBusView().getBuses()) {
            for (CgmesIidmMapping.CgmesTopologicalNode topologicalNode : context.getTopologicalNodesByBusViewBus(b.getId())) {
                writeVoltage(topologicalNode.getCgmesId(), b.getV(), b.getAngle());
            }
        }
    }

    private void writeVoltagesForBoundaryNodes() throws XMLStreamException {
        for (DanglingLine dl : context.getNetwork().getDanglingLines()) {
            Bus b = dl.getTerminal().getBusView().getBus();
            Optional<String> topologicalNode = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE);
            if (topologicalNode.isPresent()) {
                if (dl.hasProperty("v") && dl.hasProperty("angle")) {
                    writeVoltage(topologicalNode.get(), Double.valueOf(dl.getProperty("v", "NaN")), Double.valueOf(dl.getProperty("angle", "NaN")));
                } else if (b != null) {
                    writeVoltage(topologicalNode.get(), dl.getBoundary().getV(), dl.getBoundary().getAngle());
                } else {
                    writeVoltage(topologicalNode.get(), 0.0, 0.0);
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
            String topologicalNode = tieLine.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE)
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

        context.getNetwork().getDanglingLineStream().forEach(dl -> {
            // FIXME: the values (p0/q0) are wrong: these values are target and never updated, not calculated flows
            // DanglingLine's attributes will be created to store calculated flows on the boundary side
            if (context.exportBoundaryPowerFlows()) {
                dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary")
                        .ifPresent(terminal -> writePowerFlow(terminal, dl.getBoundary().getP(), dl.getBoundary().getQ()));
            }
            dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Network")
                            .ifPresent(terminal -> writePowerFlow(terminal, dl.getTerminal().getP(), dl.getTerminal().getQ()));
            dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal")
                    .ifPresent(eit -> writePowerFlow(eit, -dl.getBoundary().getP(), -dl.getBoundary().getQ()));
        });

        context.getNetwork().getBranchStream().forEach(b -> {
            b.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1)
                .ifPresent(t -> writePowerFlow((String) t, b.getTerminal1().getP(), b.getTerminal1().getQ()));
            b.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2)
                .ifPresent(t -> writePowerFlow((String) t, b.getTerminal2().getP(), b.getTerminal2().getQ()));
            if (b instanceof TieLine && context.exportBoundaryPowerFlows()) {
                TieLine tl = (TieLine) b;
                Optional.ofNullable(tl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tl.getHalf1().getId() + ".Terminal_Network"))
                        .ifPresent(t -> writePowerFlow(t, tl.getTerminal1().getP(), tl.getTerminal1().getQ()));
                Optional.ofNullable(tl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tl.getHalf2().getId() + ".Terminal_Network"))
                        .ifPresent(t -> writePowerFlow(t, tl.getTerminal2().getP(), tl.getTerminal2().getQ()));
                tl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "HALF1." + CgmesNames.TERMINAL + "_Boundary")
                        .ifPresent(t -> writePowerFlow(t, tl.getHalf1().getBoundary().getP(), tl.getHalf1().getBoundary().getQ()));
                tl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "HALF2." + CgmesNames.TERMINAL + "_Boundary")
                        .ifPresent(t -> writePowerFlow(t, tl.getHalf2().getBoundary().getP(), tl.getHalf2().getBoundary().getQ()));
                Optional.ofNullable(tl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tl.getHalf1().getId() + ".Terminal_Boundary"))
                        .ifPresent(t -> writePowerFlow(t, tl.getHalf1().getBoundary().getP(), tl.getHalf1().getBoundary().getQ()));
                Optional.ofNullable(tl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tl.getHalf2().getId() + ".Terminal_Boundary"))
                        .ifPresent(t -> writePowerFlow(t, tl.getHalf2().getBoundary().getP(), tl.getHalf2().getBoundary().getQ()));
            }
        });

        context.getNetwork().getThreeWindingsTransformerStream().forEach(twt -> {
            twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1)
                .ifPresent(t -> writePowerFlow(t, twt.getLeg1().getTerminal().getP(), twt.getLeg1().getTerminal().getQ()));
            twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2)
                .ifPresent(t -> writePowerFlow(t, twt.getLeg2().getTerminal().getP(), twt.getLeg2().getTerminal().getQ()));
            twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL3)
                .ifPresent(t -> writePowerFlow(t, twt.getLeg3().getTerminal().getP(), twt.getLeg3().getTerminal().getQ()));
        });

        if (context.exportFlowsForSwitches()) {
            context.getNetwork().getVoltageLevelStream().forEach(vl -> {
                SwitchesFlow swflows = new SwitchesFlow(vl);
                vl.getSwitches().forEach(sw -> {
                    if (swflows.hasFlow(sw.getId())) {
                        sw.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1)
                            .ifPresent(t -> writePowerFlow(t, swflows.getP1(sw.getId()), swflows.getQ1(sw.getId())));
                        sw.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2)
                            .ifPresent(t -> writePowerFlow(t, swflows.getP2(sw.getId()), swflows.getQ2(sw.getId())));
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
        String cgmesTerminal = ((Connectable<?>) terminal.getConnectable()).getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1).orElse(null);
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
        if (terminal.getConnectable() instanceof ShuntCompensator) {
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
        Bus bus = load.getTerminal().getBusView().getBus();
        if (bus == null) {
            LOG.warn("Fictitious load does not have a BusView bus. No SvInjection is written");
        } else {
            // SvInjection will be assigned to the first of the TNs mapped to the bus
            CgmesIidmMapping.CgmesTopologicalNode topologicalNode = context.getTopologicalNodesByBusViewBus(bus.getId()).iterator().next();
            writeSvInjection(load, topologicalNode.getCgmesId());
        }
    }

    private void writePowerFlow(String terminal, double p, double q) {
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
            writeReference("SvPowerFlow.Terminal", terminal);
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
            writeReference("SvShuntCompensatorSections.ShuntCompensator",  context.getNamingStrategy().getCgmesId(s));
            xmlWriter.writeStartElement(cimNamespace, "SvShuntCompensatorSections.sections");
            xmlWriter.writeCharacters(CgmesExportUtil.format(s.getSectionCount()));
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
        }
    }

    private void writeTapSteps() throws XMLStreamException {
        for (TwoWindingsTransformer twt : context.getNetwork().getTwoWindingsTransformers()) {
            if (twt.hasPhaseTapChanger()) {
                String ptcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 1).orElseGet(() -> twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeSvTapStep(ptcId, twt.getPhaseTapChanger().getTapPosition());
                writeSvTapStepHidden(twt, ptcId);
            } else if (twt.hasRatioTapChanger()) {
                String rtcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 1).orElseGet(() -> twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeSvTapStep(rtcId, twt.getRatioTapChanger().getTapPosition());
                writeSvTapStepHidden(twt, rtcId);
            }
        }

        for (ThreeWindingsTransformer twt : context.getNetwork().getThreeWindingsTransformers()) {
            int i = 1;
            for (ThreeWindingsTransformer.Leg leg : Arrays.asList(twt.getLeg1(), twt.getLeg2(), twt.getLeg3())) {
                if (leg.hasPhaseTapChanger()) {
                    String ptcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeSvTapStep(ptcId, leg.getPhaseTapChanger().getTapPosition());
                    writeSvTapStepHidden(twt, ptcId);
                } else if (leg.hasRatioTapChanger()) {
                    String rtcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeSvTapStep(rtcId, leg.getRatioTapChanger().getTapPosition());
                    writeSvTapStepHidden(twt, rtcId);
                }
                i++;
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
