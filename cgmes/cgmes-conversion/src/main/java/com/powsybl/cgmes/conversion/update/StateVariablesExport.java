/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

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
    private static final String DATA_NAMESPACE = "http://microgrid/#";

    private static final String ID = "ID";

    private static final String SV_VOLTAGE_ANGLE = "SvVoltage.angle";
    private static final String SV_VOLTAGE_V = "SvVoltage.v";
    private static final String SV_VOLTAGE_TOPOLOGICAL_NODE = "SvVoltage.TopologicalNode";

    private static final String CIM_VERSION = "CIM_version";
    private static final Logger LOG = LoggerFactory.getLogger(StateVariablesExport.class);

    public static void write(Network network, XMLStreamWriter writer) {
        try {
            initializeWriter(writer);

            if ("16".equals(network.getProperty(CIM_VERSION))) {
                writeSvModelDescription(network, writer);
                writeTopologicalIslands(network, writer);
            }

            writeVoltagesForTopologicalNodes(network, writer);
            writeVoltagesForBoundaryNodes(network, writer);
            writePowerFlowToCgmes(network, writer);
            writeShuntCompensatorSectionsToCgmes(network, writer);
            writeTapStepToCgmes(network, writer);
            writeStatusToCgmes(network, writer);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void initializeWriter(XMLStreamWriter writer) throws XMLStreamException {
        writer.setPrefix("entsoe", ENTSOE_NAMESPACE);
        writer.setPrefix("rdf", RDF_NAMESPACE);
        writer.setPrefix("cim", CIM_NAMESPACE);
        writer.setPrefix("md", MD_NAMESPACE);
        writer.setPrefix("data", DATA_NAMESPACE);
        writer.writeStartElement(RDF_NAMESPACE, "RDF");
        writer.writeNamespace("entsoe", ENTSOE_NAMESPACE);
        writer.writeNamespace("rdf", RDF_NAMESPACE);
        writer.writeNamespace("cim", CIM_NAMESPACE);
        writer.writeNamespace("md", MD_NAMESPACE);
        writer.writeNamespace("data", DATA_NAMESPACE);
    }

    private static void writeSvModelDescription(Network network, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(MD_NAMESPACE, "FullModel");
        writer.writeAttribute(RDF_NAMESPACE, "about", "urn:uuid:" + getUniqueId());
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.SCENARIO_TIME);
        writer.writeCharacters(network.getProperty(CgmesNames.SCENARIO_TIME));
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.CREATED);
        writer.writeCharacters(DateTime.now().toString());
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.DESCRIPTION);
        writer.writeCharacters(network.getProperty(CgmesNames.DESCRIPTION));
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.VERSION);
        writer.writeCharacters(String.valueOf(Integer.parseInt(network.getProperty(CgmesNames.VERSION)) + 1));
        writer.writeEndElement();
        for (String dependency : network.getProperty(CgmesNames.DEPENDENT_ON).split(",")) {
            writer.writeEmptyElement(MD_NAMESPACE, CgmesNames.DEPENDENT_ON);
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, dependency);
        }
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.PROFILE);
        writer.writeCharacters("http://entsoe.eu/CIM/StateVariables/4/1");
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.MODELING_AUTHORITY_SET);
        writer.writeCharacters(network.getProperty(CgmesNames.MODELING_AUTHORITY_SET)); // TODO: what do you put for mergingView?
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
            writer.writeStartElement(CIM_NAMESPACE, "SvVoltage");
            writer.writeAttribute(RDF_NAMESPACE, ID, getUniqueId());
            writer.writeStartElement(CIM_NAMESPACE, SV_VOLTAGE_ANGLE);
            writer.writeCharacters(fs(b.getAngle()));
            writer.writeEndElement();
            writer.writeStartElement(CIM_NAMESPACE, SV_VOLTAGE_V);
            writer.writeCharacters(fs(b.getV()));
            writer.writeEndElement();
            writer.writeEmptyElement(CIM_NAMESPACE, SV_VOLTAGE_TOPOLOGICAL_NODE);
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + b.getId());
            writer.writeEndElement();
        }
    }

    private static void writeVoltagesForBoundaryNodes(Network network, XMLStreamWriter writer) throws XMLStreamException {
        for (DanglingLine dl : network.getDanglingLines()) {
            Bus b = dl.getTerminal().getBusBreakerView().getBus();
            writer.writeStartElement(CIM_NAMESPACE, "SvVoltage");
            if (b != null) {
                // calculate complex voltage value: abs for VOLTAGE, degrees for ANGLE
                Complex v2 = complexVoltage(dl.getR(), dl.getX(), dl.getG(), dl.getB(), b.getV(), b.getAngle(),
                        dl.getP0(), dl.getQ0());
                writer.writeStartElement(CIM_NAMESPACE, SV_VOLTAGE_ANGLE);
                writer.writeCharacters(fs(Math.toDegrees(v2.getArgument())));
                writer.writeEndElement();
                writer.writeStartElement(CIM_NAMESPACE, SV_VOLTAGE_V);
                writer.writeCharacters(fs(v2.abs()));
                writer.writeEndElement();
                writer.writeEmptyElement(CIM_NAMESPACE, SV_VOLTAGE_TOPOLOGICAL_NODE);
                writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + dl.getUcteXnodeCode());
            } else {
                writer.writeStartElement(CIM_NAMESPACE, SV_VOLTAGE_ANGLE);
                writer.writeCharacters("0.0");
                writer.writeEndElement();
                writer.writeStartElement(CIM_NAMESPACE, SV_VOLTAGE_V);
                writer.writeCharacters("0.0");
                writer.writeEndElement();
                writer.writeEmptyElement(CIM_NAMESPACE, SV_VOLTAGE_TOPOLOGICAL_NODE);
                writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + dl.getUcteXnodeCode());
            }
            writer.writeEndElement();
        }
    }

    private static void writePowerFlowToCgmes(Network network, XMLStreamWriter writer) throws XMLStreamException {
        writeInjectionPowerFlowToCgmes(network, writer, Network::getLoadStream);
        writeInjectionPowerFlowToCgmes(network, writer, Network::getGeneratorStream);
        writeInjectionPowerFlowToCgmes(network, writer, Network::getShuntCompensatorStream);
        writeInjectionPowerFlowToCgmes(network, writer, Network::getStaticVarCompensatorStream);
        writeInjectionPowerFlowToCgmes(network, writer, Network::getBatteryStream);

        for (DanglingLine dl : network.getDanglingLines()) {
            if (!Boolean.parseBoolean(dl.getProperty("hasPowerFlow"))) {
                continue;
            }
            String boundarySideStr = dl.getProperty("boundarySide");
            if (boundarySideStr != null) {
                writer.writeStartElement(CIM_NAMESPACE, "SvPowerFlow");
                writer.writeAttribute(RDF_NAMESPACE, ID, getUniqueId());
                writer.writeStartElement(CIM_NAMESPACE, "SvPowerFlow.p");
                writer.writeCharacters(String.valueOf(dl.getP0()));
                writer.writeEndElement();
                writer.writeStartElement(CIM_NAMESPACE, "SvPowerFlow.q");
                writer.writeCharacters(String.valueOf(dl.getQ0()));
                writer.writeEndElement();
                writer.writeEmptyElement(CIM_NAMESPACE, "SvPowerFlow.Terminal");
                writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + dl.getProperty(CgmesNames.TERMINAL + boundarySideStr));
                writer.writeEndElement();
            }
        }
    }

    private static <I extends Injection<I>> void writeInjectionPowerFlowToCgmes(Network network, XMLStreamWriter writer, Function<Network, Stream<I>> getInjectionStream) {
        getInjectionStream.apply(network).forEach(i -> writePowerFlowProperties(i.getTerminal(), writer));
    }

    private static void writePowerFlowProperties(Terminal terminal, XMLStreamWriter writer) {
        String cgmesTerminal = ((Connectable<?>) terminal.getConnectable()).getAliasFromType(CgmesNames.TERMINAL1).orElse(null);
        if (cgmesTerminal != null) {
            try {
                writer.writeStartElement(CIM_NAMESPACE, "SvPowerFlow");
                writer.writeAttribute(RDF_NAMESPACE, ID, getUniqueId());
                writer.writeStartElement(CIM_NAMESPACE, "SvPowerFlow.p");
                writer.writeCharacters(fs(terminal.getP()));
                writer.writeEndElement();
                writer.writeStartElement(CIM_NAMESPACE, "SvPowerFlow.q");
                writer.writeCharacters(fs(terminal.getQ()));
                writer.writeEndElement();
                writer.writeEmptyElement(CIM_NAMESPACE, "SvPowerFlow.Terminal");
                writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + cgmesTerminal);
                writer.writeEndElement();
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        } else if (terminal.getConnectable() instanceof Load) {
            // FIXME CGMES SvInjection objects created as loads
            LOG.error("No SvPowerFlow created for load {}", terminal.getConnectable().getId());
        }
    }

    private static void writeShuntCompensatorSectionsToCgmes(Network network, XMLStreamWriter writer) throws XMLStreamException {
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

    private static void writeTapStepToCgmes(Network network, XMLStreamWriter writer) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            if (twt.hasPhaseTapChanger()) {
                String ptcId = twt.getAliasFromType(CgmesNames.PHASE_TAP_CHANGER + 1).orElseGet(() -> twt.getAliasFromType(CgmesNames.PHASE_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeSvTapStep(twt.getPhaseTapChanger().getTapPosition(), ptcId, writer);
            } else if (twt.hasRatioTapChanger()) {
                String rtcId = twt.getAliasFromType(CgmesNames.RATIO_TAP_CHANGER + 1).orElseGet(() -> twt.getAliasFromType(CgmesNames.RATIO_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeSvTapStep(twt.getRatioTapChanger().getTapPosition(), rtcId, writer);
            }
        }

        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            int i = 1;
            for (ThreeWindingsTransformer.Leg leg : Arrays.asList(twt.getLeg1(), twt.getLeg2(), twt.getLeg3())) {
                if (leg.hasPhaseTapChanger()) {
                    String ptcId = twt.getAliasFromType(CgmesNames.PHASE_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeSvTapStep(leg.getPhaseTapChanger().getTapPosition(), ptcId, writer);
                } else if (leg.hasRatioTapChanger()) {
                    String rtcId = twt.getAliasFromType(CgmesNames.RATIO_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeSvTapStep(leg.getRatioTapChanger().getTapPosition(), rtcId, writer);
                }
                i++;
            }
        }
    }

    private static void writeSvTapStep(int tapPosition, String tapChangerId, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(CIM_NAMESPACE, "SvTapStep");
        writer.writeStartElement(CIM_NAMESPACE, "SvTapStep.position");
        writer.writeCharacters(is(tapPosition));
        writer.writeEndElement();
        writer.writeEmptyElement(CIM_NAMESPACE, "SvTapStep.TapChanger");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + tapChangerId);
        writer.writeEndElement();
    }

    private static void writeStatusToCgmes(Network network, XMLStreamWriter writer) {
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

    private static String fs(double value) {
        return Double.isNaN(value) ? String.valueOf(0.0) : String.valueOf(value);
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
