/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
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

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class StateVariablesExport {

    private static final String SV_VOLTAGE_ANGLE = "SvVoltage.angle";
    private static final String SV_VOLTAGE_V = "SvVoltage.v";
    private static final String SV_VOLTAGE_TOPOLOGICAL_NODE = "SvVoltage.TopologicalNode";

    private static final Logger LOG = LoggerFactory.getLogger(StateVariablesExport.class);

    public static void write(Network network, XMLStreamWriter writer) {
        write(network, writer, new CgmesExportContext(network));
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            String cimNamespace = context.getCim().getNamespace();
            CgmesExportUtil.writeRdfRoot(cimNamespace, context.getCim().getEuPrefix(), context.getCim().getEuNamespace(), writer);

            if (context.getCimVersion() >= 16) {
                CgmesExportUtil.writeModelDescription(writer, context.getSvModelDescription(), context);
                writeTopologicalIslands(network, cimNamespace, writer);
                // Note: unmapped topological nodes (node breaker) & boundary topological nodes are not written in topological islands
            }

            writeVoltagesForTopologicalNodes(network, cimNamespace, writer);
            writeVoltagesForBoundaryNodes(network, cimNamespace, writer);
            writePowerFlows(network, cimNamespace, writer, context);
            writeShuntCompensatorSections(network, cimNamespace, writer);
            writeTapSteps(network, cimNamespace, writer);
            writeStatus(network, cimNamespace, writer);
            writeConverters(network, cimNamespace, writer);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeTopologicalIslands(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        Map<String, String> angleRefs = buildAngleRefs(network);
        Map<String, List<String>> islands = buildIslands(network);
        for (Map.Entry<String, List<String>> island : islands.entrySet()) {
            if (!angleRefs.containsKey(island.getKey())) {
                Supplier<String> log = () -> String.format("Synchronous component  %s does not have a defined slack bus: it is ignored", island.getKey());
                LOG.info(log.get());
                continue;
            }
            String islandId = CgmesExportUtil.getUniqueId();
            writer.writeStartElement(cimNamespace, CgmesNames.TOPOLOGICAL_ISLAND);
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, islandId);
            writer.writeStartElement(cimNamespace, CgmesNames.NAME);
            writer.writeCharacters(islandId); // Use id as name
            writer.writeEndElement();
            CgmesExportUtil.writeReference("TopologicalIsland.AngleRefTopologicalNode", angleRefs.get(island.getKey()), cimNamespace, writer);
            for (String tn : island.getValue()) {
                CgmesExportUtil.writeReference("TopologicalIsland.TopologicalNodes", tn, cimNamespace, writer);
            }
            writer.writeEndElement();
        }
    }

    private static Map<String, String> buildAngleRefs(Network network) {
        Map<String, String> angleRefs = new HashMap<>();
        for (VoltageLevel vl : network.getVoltageLevels()) {
            SlackTerminal slackTerminal = vl.getExtension(SlackTerminal.class);
            buildAngleRefs(slackTerminal, angleRefs);
        }
        return angleRefs;
    }

    private static void buildAngleRefs(SlackTerminal slackTerminal, Map<String, String> angleRefs) {
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

    private static void buildAngleRefs(int synchronousComponentNum, String topologicalNodeId, Map<String, String> angleRefs) {
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

    private static Map<String, List<String>> buildIslands(Network network) {
        Map<String, List<String>> islands = new HashMap<>();
        for (Bus b : network.getBusBreakerView().getBuses()) {
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

    private static void writeVoltagesForTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (Bus b : network.getBusBreakerView().getBuses()) {
            writeVoltage(b.getId(), b.getV(), b.getAngle(), cimNamespace, writer);
        }
    }

    private static void writeVoltagesForBoundaryNodes(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (DanglingLine dl : network.getDanglingLines()) {
            Bus b = dl.getTerminal().getBusView().getBus();
            Optional<String> topologicalNode = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE);
            if (topologicalNode.isPresent()) {
                if (dl.hasProperty("v") && dl.hasProperty("angle")) {
                    writeVoltage(topologicalNode.get(), Double.valueOf(dl.getProperty("v", "NaN")), Double.valueOf(dl.getProperty("angle", "NaN")), cimNamespace, writer);
                } else if (b != null) {
                    writeVoltage(topologicalNode.get(), dl.getBoundary().getV(), dl.getBoundary().getAngle(), cimNamespace, writer);
                } else {
                    writeVoltage(topologicalNode.get(), 0.0, 0.0, cimNamespace, writer);
                }
            }
        }
        // Voltages at inner nodes of Tie Lines
        // (boundary nodes that have been left inside CGM)
        for (Line l : network.getLines()) {
            if (!l.isTieLine()) {
                continue;
            }
            TieLine tieLine = (TieLine) l;
            String topologicalNode = tieLine.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE)
                    .orElseGet(() -> tieLine.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tieLine.getHalf1().getId() + "." + CgmesNames.TOPOLOGICAL_NODE));
            if (topologicalNode != null) {
                writeVoltage(topologicalNode, tieLine.getHalf1().getBoundary().getV(), tieLine.getHalf1().getBoundary().getAngle(), cimNamespace, writer);
            }
        }
    }

    private static void writeVoltage(String topologicalNode, double v, double angle, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "SvVoltage");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, CgmesExportUtil.getUniqueId());
        writer.writeStartElement(cimNamespace, SV_VOLTAGE_ANGLE);
        writer.writeCharacters(CgmesExportUtil.format(angle));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, SV_VOLTAGE_V);
        writer.writeCharacters(CgmesExportUtil.format(v));
        writer.writeEndElement();
        CgmesExportUtil.writeReference(SV_VOLTAGE_TOPOLOGICAL_NODE, topologicalNode, cimNamespace, writer);
        writer.writeEndElement();
    }

    private static void writePowerFlows(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        writeInjectionsPowerFlows(network, cimNamespace, writer, Network::getLoadStream);
        writeInjectionsPowerFlows(network, cimNamespace, writer, Network::getGeneratorStream);
        writeInjectionsPowerFlows(network, cimNamespace, writer, Network::getShuntCompensatorStream);
        writeInjectionsPowerFlows(network, cimNamespace, writer, Network::getStaticVarCompensatorStream);
        writeInjectionsPowerFlows(network, cimNamespace, writer, Network::getBatteryStream);

        network.getDanglingLineStream().forEach(dl -> {
            // FIXME: the values (p0/q0) are wrong: these values are target and never updated, not calculated flows
            // DanglingLine's attributes will be created to store calculated flows on the boundary side
            if (context.exportBoundaryPowerFlows()) {
                dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary")
                        .ifPresent(terminal -> writePowerFlow(terminal, dl.getBoundary().getP(), dl.getBoundary().getQ(), cimNamespace, writer));
            }
            dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal")
                    .ifPresent(eit -> writePowerFlow(eit, -dl.getBoundary().getP(), -dl.getBoundary().getQ(), cimNamespace, writer));
        });

        network.getBranchStream().forEach(b -> {
            b.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1)
                .ifPresent(t -> writePowerFlow((String) t, b.getTerminal1().getP(), b.getTerminal1().getQ(), cimNamespace, writer));
            b.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2)
                .ifPresent(t -> writePowerFlow((String) t, b.getTerminal2().getP(), b.getTerminal2().getQ(), cimNamespace, writer));
            if (b instanceof TieLine && context.exportBoundaryPowerFlows()) {
                TieLine tl = (TieLine) b;
                Optional.ofNullable(tl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tl.getHalf1().getId() + ".Terminal_Network"))
                        .ifPresent(t -> writePowerFlow(t, tl.getTerminal1().getP(), tl.getTerminal1().getQ(), cimNamespace, writer));
                Optional.ofNullable(tl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tl.getHalf2().getId() + ".Terminal_Network"))
                        .ifPresent(t -> writePowerFlow(t, tl.getTerminal2().getP(), tl.getTerminal2().getQ(), cimNamespace, writer));
                tl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "HALF1." + CgmesNames.TERMINAL + "_Boundary")
                        .ifPresent(t -> writePowerFlow(t, tl.getHalf1().getBoundary().getP(), tl.getHalf1().getBoundary().getQ(), cimNamespace, writer));
                tl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "HALF2." + CgmesNames.TERMINAL + "_Boundary")
                        .ifPresent(t -> writePowerFlow(t, tl.getHalf2().getBoundary().getP(), tl.getHalf2().getBoundary().getQ(), cimNamespace, writer));
                Optional.ofNullable(tl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tl.getHalf1().getId() + ".Terminal_Boundary"))
                        .ifPresent(t -> writePowerFlow(t, tl.getHalf1().getBoundary().getP(), tl.getHalf1().getBoundary().getQ(), cimNamespace, writer));
                Optional.ofNullable(tl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tl.getHalf2().getId() + ".Terminal_Boundary"))
                        .ifPresent(t -> writePowerFlow(t, tl.getHalf2().getBoundary().getP(), tl.getHalf2().getBoundary().getQ(), cimNamespace, writer));
            }
        });

        network.getThreeWindingsTransformerStream().forEach(twt -> {
            twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1)
                .ifPresent(t -> writePowerFlow(t, twt.getLeg1().getTerminal().getP(), twt.getLeg1().getTerminal().getQ(), cimNamespace, writer));
            twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2)
                .ifPresent(t -> writePowerFlow(t, twt.getLeg2().getTerminal().getP(), twt.getLeg2().getTerminal().getQ(), cimNamespace, writer));
            twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL3)
                .ifPresent(t -> writePowerFlow(t, twt.getLeg3().getTerminal().getP(), twt.getLeg3().getTerminal().getQ(), cimNamespace, writer));
        });

        if (context.exportFlowsForSwitches()) {
            network.getVoltageLevelStream().forEach(vl -> {
                SwitchesFlow swflows = new SwitchesFlow(vl);
                vl.getSwitches().forEach(sw -> {
                    if (swflows.hasFlow(sw.getId())) {
                        sw.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1)
                            .ifPresent(t -> writePowerFlow(t, swflows.getP1(sw.getId()), swflows.getQ1(sw.getId()), cimNamespace, writer));
                        sw.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2)
                            .ifPresent(t -> writePowerFlow(t, swflows.getP2(sw.getId()), swflows.getQ2(sw.getId()), cimNamespace, writer));
                    }
                });
            });
        }
    }

    private static <I extends Injection<I>> void writeInjectionsPowerFlows(Network network, String cimNamespace, XMLStreamWriter writer, Function<Network, Stream<I>> getInjectionStream) {
        getInjectionStream.apply(network).forEach(i -> writePowerFlow(i.getTerminal(), cimNamespace, writer));
    }

    private static void writePowerFlow(Terminal terminal, String cimNamespace, XMLStreamWriter writer) {
        String cgmesTerminal = ((Connectable<?>) terminal.getConnectable()).getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1).orElse(null);
        if (cgmesTerminal != null) {
            writePowerFlow(cgmesTerminal, getTerminalP(terminal), terminal.getQ(), cimNamespace, writer);
        } else if (terminal.getConnectable() instanceof Load && terminal.getConnectable().isFictitious()) {
            writeFictitiousLoadPowerFlow(terminal, cimNamespace, writer);
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

    private static void writeFictitiousLoadPowerFlow(Terminal terminal, String cimNamespace, XMLStreamWriter writer) {
        // Fictitious loads are created in IIDM to keep track of mismatches in the input case,
        // These mismatches are given by SvInjection CGMES objects
        // These loads have been taken into account as inputs for potential power flow analysis
        // TODO(Luma) Not sure that its values should be written back as SvInjection objects
        // Because in our output we should write our current mismatches
        // Original mismatches, if they have been used, should be written as loads
        // But that would mean to introduce a new object in the Equipment profile
        Load svInjection = (Load) terminal.getConnectable();
        Bus bus = svInjection.getTerminal().getBusBreakerView().getBus();
        if (bus == null) {
            LOG.warn("Fictitious load does not have a BusView bus. No SvInjection is written");
        } else {
            // SvInjection will be assigned to the first of the TNs mapped to the bus
            writeSvInjection(svInjection, bus.getId(), cimNamespace, writer);
        }
    }

    private static void writePowerFlow(String terminal, double p, double q, String cimNamespace, XMLStreamWriter writer) {
        // Export only if flow is a number
        if (Double.isNaN(p) && Double.isNaN(q)) {
            return;
        }
        try {
            writer.writeStartElement(cimNamespace, "SvPowerFlow");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, CgmesExportUtil.getUniqueId());
            writer.writeStartElement(cimNamespace, "SvPowerFlow.p");
            writer.writeCharacters(CgmesExportUtil.format(p));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "SvPowerFlow.q");
            writer.writeCharacters(CgmesExportUtil.format(q));
            writer.writeEndElement();
            CgmesExportUtil.writeReference("SvPowerFlow.Terminal", terminal, cimNamespace, writer);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeSvInjection(Load svInjection, String topologicalNode, String cimNamespace, XMLStreamWriter writer) {
        try {
            writer.writeStartElement(cimNamespace, "SvInjection");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, svInjection.getId());
            writer.writeStartElement(cimNamespace, "SvInjection.pInjection");
            writer.writeCharacters(CgmesExportUtil.format(svInjection.getP0()));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "SvInjection.qInjection");
            writer.writeCharacters(CgmesExportUtil.format(svInjection.getQ0()));
            writer.writeEndElement();
            CgmesExportUtil.writeReference("SvInjection.TopologicalNode", topologicalNode, cimNamespace, writer);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeShuntCompensatorSections(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            writer.writeStartElement(cimNamespace, "SvShuntCompensatorSections");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, CgmesExportUtil.getUniqueId());
            CgmesExportUtil.writeReference("SvShuntCompensatorSections.ShuntCompensator", s.getId(), cimNamespace, writer);
            writer.writeStartElement(cimNamespace, "SvShuntCompensatorSections.sections");
            writer.writeCharacters(CgmesExportUtil.format(s.getSectionCount()));
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private static void writeTapSteps(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            if (twt.hasPhaseTapChanger()) {
                String ptcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 1).orElseGet(() -> twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeSvTapStep(ptcId, twt.getPhaseTapChanger().getTapPosition(), cimNamespace, writer);
            } else if (twt.hasRatioTapChanger()) {
                String rtcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 1).orElseGet(() -> twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeSvTapStep(rtcId, twt.getRatioTapChanger().getTapPosition(), cimNamespace, writer);
            }
        }

        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            int i = 1;
            for (ThreeWindingsTransformer.Leg leg : Arrays.asList(twt.getLeg1(), twt.getLeg2(), twt.getLeg3())) {
                if (leg.hasPhaseTapChanger()) {
                    String ptcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeSvTapStep(ptcId, leg.getPhaseTapChanger().getTapPosition(), cimNamespace, writer);
                } else if (leg.hasRatioTapChanger()) {
                    String rtcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeSvTapStep(rtcId, leg.getRatioTapChanger().getTapPosition(), cimNamespace, writer);
                }
                i++;
            }
        }
    }

    private static void writeSvTapStep(String tapChangerId, int tapPosition, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "SvTapStep");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, CgmesExportUtil.getUniqueId());
        writer.writeStartElement(cimNamespace, "SvTapStep.position");
        writer.writeCharacters(CgmesExportUtil.format(tapPosition));
        writer.writeEndElement();
        CgmesExportUtil.writeReference("SvTapStep.TapChanger", tapChangerId, cimNamespace, writer);
        writer.writeEndElement();
    }

    private static void writeStatus(Network network, String cimNamespace, XMLStreamWriter writer) {
        // create SvStatus, iterate on Connectables, check Terminal status, add
        // to SvStatus
        network.getConnectableStream().forEach(c -> writeConnectableStatus((Connectable<?>) c, cimNamespace, writer));

        // RK: For dangling lines (boundaries), the AC Line Segment is considered in service if and only if it is connected on the network side.
        // If it is disconnected on the boundary side, it might not appear on the SV file.
    }

    private static void writeConnectableStatus(Connectable<?> connectable, String cimNamespace, XMLStreamWriter writer) {
        writeStatus(Boolean.toString(connectable.getTerminals().stream().anyMatch(Terminal::isConnected)), connectable.getId(), cimNamespace, writer);
    }

    private static void writeStatus(String inService, String conductingEquipmentId, String cimNamespace, XMLStreamWriter writer) {
        try {
            writer.writeStartElement(cimNamespace, "SvStatus");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, CgmesExportUtil.getUniqueId());
            writer.writeStartElement(cimNamespace, "SvStatus.inService");
            writer.writeCharacters(inService);
            writer.writeEndElement();
            CgmesExportUtil.writeReference("SvStatus.ConductingEquipment", conductingEquipmentId, cimNamespace, writer);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeConverters(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (HvdcConverterStation<?> converterStation : network.getHvdcConverterStations()) {
            CgmesExportUtil.writeStartAbout(CgmesExportUtil.converterClassName(converterStation), converterStation.getId(), cimNamespace, writer);
            writer.writeStartElement(cimNamespace, "ACDCConverter.poleLossP");
            writer.writeCharacters(CgmesExportUtil.format(getPoleLossP(converterStation)));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "ACDCConverter.idc");
            writer.writeCharacters(CgmesExportUtil.format(0));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "ACDCConverter.uc");
            writer.writeCharacters(CgmesExportUtil.format(0));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "ACDCConverter.udc");
            writer.writeCharacters(CgmesExportUtil.format(0));
            writer.writeEndElement();
            if (converterStation instanceof LccConverterStation) {
                writer.writeStartElement(cimNamespace, "CsConverter.alpha");
                writer.writeCharacters(CgmesExportUtil.format(0));
                writer.writeEndElement();
                writer.writeStartElement(cimNamespace, "CsConverter.gamma");
                writer.writeCharacters(CgmesExportUtil.format(0));
                writer.writeEndElement();
            } else if (converterStation instanceof VscConverterStation) {
                writer.writeStartElement(cimNamespace, "VsConverter.delta");
                writer.writeCharacters(CgmesExportUtil.format(0));
                writer.writeEndElement();
                writer.writeStartElement(cimNamespace, "VsConverter.uf");
                writer.writeCharacters(CgmesExportUtil.format(0));
                writer.writeEndElement();
            }
            writer.writeEndElement();
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

    private StateVariablesExport() {
    }
}
