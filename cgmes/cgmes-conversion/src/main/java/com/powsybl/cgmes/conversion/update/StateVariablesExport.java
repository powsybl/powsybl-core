/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import com.powsybl.cgmes.conversion.extensions.CgmesSvMetadata;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.util.LinkData;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class StateVariablesExport {

    private static final String ENTSOE_NAMESPACE = "http://entsoe.eu/CIM/SchemaExtension/3/1#";
    private static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String CIM_NAMESPACE = "http://iec.ch/TC57/2013/CIM-schema-cim16#";
    private static final String MD_NAMESPACE = "http://iec.ch/TC57/61970-552/ModelDescription/1#";

    private static final String ID = "ID";

    private static final String SV_VOLTAGE_ANGLE = "SvVoltage.angle";
    private static final String SV_VOLTAGE_V = "SvVoltage.v";
    private static final String SV_VOLTAGE_TOPOLOGICAL_NODE = "SvVoltage.TopologicalNode";

    private static final String CIM_VERSION = "CIM_version";

    private static final Logger LOG = LoggerFactory.getLogger(StateVariablesExport.class);

    private static final boolean EXPORT_BRANCH_POWER_FLOWS = false;

    public static void write(Network network, XMLStreamWriter writer) {
        try {
            writeRdf(writer);

            if ("16".equals(network.getProperty(CIM_VERSION))) {
                writeSvModelDescription(network, writer);
                writeTopologicalIslands(network, writer);
            }

            writeVoltagesForTopologicalNodes(network, writer);
            writeVoltagesForBoundaryNodes(network, writer);
            writePowerFlows(network, writer);
            writeShuntCompensatorSections(network, writer);
            writeTapSteps(network, writer);
            writeStatus(network, writer);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeRdf(XMLStreamWriter writer) throws XMLStreamException {
        writer.setPrefix("entsoe", ENTSOE_NAMESPACE);
        writer.setPrefix("rdf", RDF_NAMESPACE);
        writer.setPrefix("cim", CIM_NAMESPACE);
        writer.setPrefix("md", MD_NAMESPACE);
        writer.writeStartElement(RDF_NAMESPACE, "RDF");
        writer.writeNamespace("entsoe", ENTSOE_NAMESPACE);
        writer.writeNamespace("rdf", RDF_NAMESPACE);
        writer.writeNamespace("cim", CIM_NAMESPACE);
        writer.writeNamespace("md", MD_NAMESPACE);
    }

    private static void writeSvModelDescription(Network network, XMLStreamWriter writer) throws XMLStreamException {
        CgmesSvMetadata svMetadata = network.getExtension(CgmesSvMetadata.class);
        if (svMetadata == null) {
            return;
        }
        writer.writeStartElement(MD_NAMESPACE, "FullModel");
        writer.writeAttribute(RDF_NAMESPACE, "about", "urn:uuid:" + getUniqueId());
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.SCENARIO_TIME);
        writer.writeCharacters(svMetadata.getScenarioTime());
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.CREATED);
        writer.writeCharacters(DateTime.now().toString());
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.DESCRIPTION);
        writer.writeCharacters(svMetadata.getDescription());
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.VERSION);
        writer.writeCharacters(is(svMetadata.getSvVersion()));
        writer.writeEndElement();
        for (String dependency : svMetadata.getDependencies()) {
            writer.writeEmptyElement(MD_NAMESPACE, CgmesNames.DEPENDENT_ON);
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, dependency);
        }
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.PROFILE);
        writer.writeCharacters("http://entsoe.eu/CIM/StateVariables/4/1");
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.MODELING_AUTHORITY_SET);
        writer.writeCharacters(svMetadata.getModelingAuthoritySet()); // TODO: what do you put for mergingView?
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeTopologicalIslands(Network network, XMLStreamWriter writer) throws XMLStreamException {
        Map<Integer, List<String>> islands = new HashMap<>();
        Map<Integer, String> angleRefs = new HashMap<>();
        if (network.getProperty("CGMES_topology").equals("NODE_BREAKER")) {
            // TODO we need to export SV file data for NodeBraker
            LOG.warn("NodeBreaker view require further investigation to map correctly Topological Nodes");
            return;
        }
        for (VoltageLevel vl : network.getVoltageLevels()) {
            SlackTerminal slackTerminal = vl.getExtension(SlackTerminal.class);
            if (slackTerminal != null) {
                angleRefs.put(slackTerminal.getTerminal().getBusBreakerView().getBus().getSynchronousComponent().getNum(),
                        slackTerminal.getTerminal().getBusBreakerView().getBus().getId());
            }
        }
        for (Bus b : network.getBusBreakerView().getBuses()) {
            if (b.getSynchronousComponent() != null) {
                int num = b.getSynchronousComponent().getNum();
                islands.computeIfAbsent(num, i -> new ArrayList<>());
                islands.get(num).add(b.getId());
            }
        }

        for (Map.Entry<Integer, List<String>> island : islands.entrySet()) {
            writer.writeStartElement(CIM_NAMESPACE, CgmesNames.TOPOLOGICAL_ISLAND);
            writer.writeAttribute(RDF_NAMESPACE, ID, getUniqueId());
            writer.writeStartElement(CIM_NAMESPACE, CgmesNames.NAME);
            writer.writeCharacters(getUniqueId()); // TODO do we need another name?
            writer.writeEndElement();
            writer.writeEmptyElement(CIM_NAMESPACE, "TopologicalIsland.AngleRefTopologicalNode");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + angleRefs.getOrDefault(island.getKey(), island.getValue().get(0)));
            for (String tn : island.getValue()) {
                writer.writeEmptyElement(CIM_NAMESPACE, "TopologicalIsland.TopologicalNodes");
                writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + tn);
            }
            writer.writeEndElement();
        }
    }

    private static void writeVoltagesForTopologicalNodes(Network network, XMLStreamWriter writer) throws XMLStreamException {
        if (network.getProperty("CGMES_topology").equals("NODE_BREAKER")) {
            // TODO we need to export SV file data for NodeBraker
            LOG.warn("NodeBreaker view require further investigation to map correctly Topological Nodes");
            return;
        }
        for (Bus b : network.getBusBreakerView().getBuses()) {
            writeVoltage(b.getId(), b.getV(), b.getAngle(), writer);
        }
    }

    private static void writeVoltagesForBoundaryNodes(Network network, XMLStreamWriter writer) throws XMLStreamException {
        for (DanglingLine dl : network.getDanglingLines()) {
            Bus b = dl.getTerminal().getBusBreakerView().getBus();
            Optional<String> topologicalNode = dl.getAliasFromType(CgmesNames.TOPOLOGICAL_NODE);
            if (topologicalNode.isPresent()) {
                if (dl.hasProperty("v") && dl.hasProperty("angle")) {
                    writeVoltage(topologicalNode.get(), Double.valueOf(dl.getProperty("v", "NaN")), Double.valueOf(dl.getProperty("angle", "NaN")), writer);
                } else if (b != null) {
                    // calculate complex voltage value: abs for VOLTAGE, degrees for ANGLE
                    Complex v2 = complexVoltage(dl.getR(), dl.getX(), dl.getG(), dl.getB(), b.getV(), b.getAngle(), dl.getP0(), dl.getQ0());
                    writeVoltage(topologicalNode.get(), v2.abs(), Math.toDegrees(v2.getArgument()), writer);
                } else {
                    writeVoltage(topologicalNode.get(), 0.0, 0.0, writer);
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
            // FIXME(Luma) Obtain voltage at inner node
            LOG.error("Must export topologicalNode voltage for boundary Tie Line {}", tieLine);
        }
    }

    private static void writeVoltage(String topologicalNode, double v, double angle, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(CIM_NAMESPACE, "SvVoltage");
        writer.writeAttribute(RDF_NAMESPACE, ID, getUniqueId());
        writer.writeStartElement(CIM_NAMESPACE, SV_VOLTAGE_ANGLE);
        writer.writeCharacters(format(angle));
        writer.writeEndElement();
        writer.writeStartElement(CIM_NAMESPACE, SV_VOLTAGE_V);
        writer.writeCharacters(format(v));
        writer.writeEndElement();
        writer.writeEmptyElement(CIM_NAMESPACE, SV_VOLTAGE_TOPOLOGICAL_NODE);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + topologicalNode);
        writer.writeEndElement();
    }

    private static void writePowerFlows(Network network, XMLStreamWriter writer) throws XMLStreamException {
        writeInjectionsPowerFlows(network, writer, Network::getLoadStream);
        writeInjectionsPowerFlows(network, writer, Network::getGeneratorStream);
        writeInjectionsPowerFlows(network, writer, Network::getShuntCompensatorStream);
        writeInjectionsPowerFlows(network, writer, Network::getStaticVarCompensatorStream);
        writeInjectionsPowerFlows(network, writer, Network::getBatteryStream);

        for (DanglingLine dl : network.getDanglingLines()) {
            if (EXPORT_BRANCH_POWER_FLOWS) {
                String boundarySideStr = dl.getProperty("boundarySide");
                if (boundarySideStr != null) {
                    // The flow at the original Line terminal must have opposite sign of a load modeled at dangling line
                    dl.getAliasFromType(CgmesNames.TERMINAL + boundarySideStr)
                        .ifPresent(linet -> writePowerFlow(linet, -dl.getP0(), -dl.getQ0(), writer));
                }
            }
            dl.getAliasFromType("EquivalentInjectionTerminal")
                .ifPresent(eit -> writePowerFlow(eit, dl.getP0(), dl.getQ0(), writer));
        }
    }

    private static <I extends Injection<I>> void writeInjectionsPowerFlows(Network network, XMLStreamWriter writer, Function<Network, Stream<I>> injectionStream) {
        injectionStream.apply(network).forEach(i -> writePowerFlow(i.getTerminal(), writer));
    }

    private static void writePowerFlow(Terminal terminal, XMLStreamWriter writer) {
        String cgmesTerminal = ((Connectable<?>) terminal.getConnectable()).getAliasFromType(CgmesNames.TERMINAL1).orElse(null);
        if (cgmesTerminal != null) {
            writePowerFlow(cgmesTerminal, terminal.getP(), terminal.getQ(), writer);
        } else {
            LOG.error("No SvPowerFlow created for {}", terminal.getConnectable().getId());
        }
    }

    private static void writePowerFlow(String terminal, double p, double q, XMLStreamWriter writer) {
        try {
            writer.writeStartElement(CIM_NAMESPACE, "SvPowerFlow");
            writer.writeAttribute(RDF_NAMESPACE, ID, getUniqueId());
            writer.writeStartElement(CIM_NAMESPACE, "SvPowerFlow.p");
            writer.writeCharacters(format(p));
            writer.writeEndElement();
            writer.writeStartElement(CIM_NAMESPACE, "SvPowerFlow.q");
            writer.writeCharacters(format(q));
            writer.writeEndElement();
            writer.writeEmptyElement(CIM_NAMESPACE, "SvPowerFlow.Terminal");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + terminal);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeShuntCompensatorSections(Network network, XMLStreamWriter writer) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            writer.writeStartElement(CIM_NAMESPACE, "SvShuntCompensatorSections");
            writer.writeAttribute(RDF_NAMESPACE, ID, getUniqueId());
            writer.writeEmptyElement(CIM_NAMESPACE, "SvShuntCompensatorSections.ShuntCompensator");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + s.getId());
            writer.writeStartElement(CIM_NAMESPACE, "SvShuntCompensatorSections.continuousSections");
            writer.writeCharacters(is(s.getSectionCount()));
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private static void writeTapSteps(Network network, XMLStreamWriter writer) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            if (twt.hasPhaseTapChanger()) {
                String ptcId = twt.getAliasFromType(CgmesNames.PHASE_TAP_CHANGER + 1).orElseGet(() -> twt.getAliasFromType(CgmesNames.PHASE_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeSvTapStep(ptcId, twt.getPhaseTapChanger().getTapPosition(), writer);
            } else if (twt.hasRatioTapChanger()) {
                String rtcId = twt.getAliasFromType(CgmesNames.RATIO_TAP_CHANGER + 1).orElseGet(() -> twt.getAliasFromType(CgmesNames.RATIO_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeSvTapStep(rtcId, twt.getRatioTapChanger().getTapPosition(), writer);
            }
        }

        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            int i = 1;
            for (ThreeWindingsTransformer.Leg leg : Arrays.asList(twt.getLeg1(), twt.getLeg2(), twt.getLeg3())) {
                if (leg.hasPhaseTapChanger()) {
                    String ptcId = twt.getAliasFromType(CgmesNames.PHASE_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeSvTapStep(ptcId, leg.getPhaseTapChanger().getTapPosition(), writer);
                } else if (leg.hasRatioTapChanger()) {
                    String rtcId = twt.getAliasFromType(CgmesNames.RATIO_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeSvTapStep(rtcId, leg.getRatioTapChanger().getTapPosition(), writer);
                }
                i++;
            }
        }
    }

    private static void writeSvTapStep(String tapChangerId, int tapPosition, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(CIM_NAMESPACE, "SvTapStep");
        writer.writeAttribute(RDF_NAMESPACE, ID, getUniqueId());
        writer.writeStartElement(CIM_NAMESPACE, "SvTapStep.position");
        writer.writeCharacters(is(tapPosition));
        writer.writeEndElement();
        writer.writeEmptyElement(CIM_NAMESPACE, "SvTapStep.TapChanger");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + tapChangerId);
        writer.writeEndElement();
    }

    private static void writeStatus(Network network, XMLStreamWriter writer) {
        // create SvStatus, iterate on Connectables, check Terminal status, add
        // to SvStatus
        network.getConnectableStream().forEach(c -> writeConnectableStatus((Connectable<?>) c, writer));

        // SvStatus at boundaries set as it was in original cgmes.
        network.getDanglingLineStream()
                .filter(dl -> dl.hasProperty("inService"))
                .forEach(dl -> writeStatus(dl.getProperty("inService"), dl.getId(), writer));
    }

    private static void writeConnectableStatus(Connectable<?> connectable, XMLStreamWriter writer) {
        writeStatus(Boolean.toString(connectable.getTerminals().stream().anyMatch(Terminal::isConnected)), connectable.getId(), writer);
    }

    private static void writeStatus(String inService, String conductingEquipmentId, XMLStreamWriter writer) {
        try {
            writer.writeStartElement(CIM_NAMESPACE, "SvStatus");
            writer.writeAttribute(RDF_NAMESPACE, ID, getUniqueId());
            writer.writeStartElement(CIM_NAMESPACE, "SvStatus.inService");
            writer.writeCharacters(inService);
            writer.writeEndElement();
            writer.writeEmptyElement(CIM_NAMESPACE, "SvStatus.ConductingEquipment");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + conductingEquipmentId);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    // Avoid trailing zeros
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.##############");

    private static String format(double value) {
        return Double.isNaN(value) ? DOUBLE_FORMAT.format(0.0) : DOUBLE_FORMAT.format(value);
    }

    private static Complex complexVoltage(double r, double x, double g, double b,
                                          double v, double angle, double p, double q) {
        LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(r, x, 1.0, 0.0, 1.0, 0.0,
                new Complex(g * 0.5, b * 0.5), new Complex(g * 0.5, b * 0.5));
        Complex v1 = ComplexUtils.polar2Complex(v, Math.toRadians(angle));
        Complex s1 = new Complex(p, q);
        return (s1.conjugate().divide(v1.conjugate()).subtract(adm.y11().multiply(v1))).divide(adm.y12());
    }

    private static String is(int value) {
        return String.valueOf(value);
    }

    private StateVariablesExport() {
    }
}
