/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChangerHolder;
import com.powsybl.iidm.network.RatioTapChangerHolder;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.TapChanger;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.LinkData;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.iidm.network.util.LinkData.BranchAdmittanceMatrix;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author Elena Kaltakova <kaltakovae at aia.es>
 */
public class StateVariablesAdder {

    public StateVariablesAdder(CgmesModel cgmes, Network n) {
        this.cgmes = Objects.requireNonNull(cgmes);
        this.network = Objects.requireNonNull(n);
        this.cimVersion = ((CgmesModelTripleStore) cgmes).getCimVersion();
        this.originalTerminals = cgmes.terminals();
        this.originalFullModel = cgmes.fullModel(CgmesSubset.STATE_VARIABLES.getProfile());
        this.originalTopologicalIslands = cgmes.topologicalIslands();
        this.originalSVcontext = originalSVcontext();
        this.originalTopologicalNodes = cgmes.topologicalNodes();
        this.boundaryNodesFromDanglingLines = boundaryNodesFromDanglingLines();
    }

    private String originalSVcontext() {
        PropertyBags pbs = cgmes.graph();
        PropertyBag defaultSvContext = new PropertyBag(Collections.singletonList(CgmesNames.GRAPH));
        defaultSvContext.put(CgmesNames.GRAPH, CgmesSubset.STATE_VARIABLES.toString());
        return pbs.stream()
            .filter(graph -> graph.getId(CgmesNames.GRAPH).contains(CgmesSubset.STATE_VARIABLES.getIdentifier()))
            .findAny()
            .orElse(defaultSvContext)
            .getId(CgmesNames.GRAPH);
    }

    public void addStateVariablesToCgmes() {
        // Clear the previous SV data in CGMES model
        // and fill it with the Network current state values
        cgmes.clear(CgmesSubset.STATE_VARIABLES);

        if (cimVersion != 14) {
            addModelDescriptionToCgmes();
            addTopologicalIslandsToCgmes();
        }

        addVoltagesForTopologicalNodes();

        addVoltagesForBoundaryNodes();

        addPowerFlows();

        addShuntCompensatorSections();

        addTapSteps();

        addStatus();
    }

    private void addVoltagesForTopologicalNodes() {
        // Try to add voltages for all TP nodes existing in the CGMES model
        PropertyBags svVoltages = new PropertyBags();
        if (cgmes.isNodeBreaker()) {
            addVoltagesForTopologicalNodesNodeBreaker(svVoltages);
        } else {
            // For CGMES bus-branch models:
            // In IIDM there is a bus at bus-breaker view
            // for each CGMES topological node
            cgmes.topologicalNodes().stream().forEach(tp -> {
                String tpId = tp.getId(CgmesNames.TOPOLOGICAL_NODE);
                Bus b = network.getBusBreakerView().getBus(tpId);
                if (b != null) {
                    svVoltages.add(buildSvVoltage(tp.getId(CgmesNames.TOPOLOGICAL_NODE), b.getV(), b.getAngle()));
                } else {
                    // If no IIDM bus has been found it may correspond
                    // to a boundary, that will be added later
                    // FIXME(Luma) We should have uniform processing of TP nodes
                    // present in the original data that have not received an SvVoltage
                    // this processing should be common to node-breaker and bus-branch
                }
            });
        }
        cgmes.add(originalSVcontext, "SvVoltage", svVoltages);
    }

    private void addVoltagesForTopologicalNodesNodeBreaker(PropertyBags svVoltages) {
        Set<String> processedTPNodes = new HashSet<>();
        // Try to find an IIDM bus for every TP node
        // In the CGMES model we have cached
        // for every IIDM voltage level and node
        // the original related TP node
        cgmes.iidmNodesForTopologicalNodes().forEach((iidmVlNode, tp) -> {
            if (processedTPNodes.contains(tp)) {
                return;
            }
            VoltageLevel vl = network.getVoltageLevel(iidmVlNode.getLeft());
            int iidmNode = iidmVlNode.getRight().intValue();
            Bus b = getBusViewBus(vl, iidmNode);
            if (b != null) {
                svVoltages.add(buildSvVoltage(tp, b.getV(), b.getAngle()));
                processedTPNodes.add(tp);
            }
        });
        // FIXME(Luma) This processing should be common to bus-branch
        // Buses not processed will be exported with the same values of the original CGMES data
        originalTopologicalNodes.stream()
            .filter(tn -> tn.get(CgmesNames.ANGLE) != null && tn.get(CgmesNames.VOLTAGE) != null)
            .filter(tn -> !processedTPNodes.contains(tn.getId(CgmesNames.TOPOLOGICAL_NODE)))
            .filter(tn -> !boundaryNodesFromDanglingLines.values().contains(tn.getId(CgmesNames.TOPOLOGICAL_NODE)))
            .forEach(pb -> {
                String tnId = pb.getId(CgmesNames.TOPOLOGICAL_NODE);
                double v = pb.asDouble(CgmesNames.VOLTAGE);
                double angle = pb.asDouble(CgmesNames.ANGLE);
                svVoltages.add(buildSvVoltage(tnId, v, angle));
            });
    }

    private static Bus getBusViewBus(VoltageLevel vl, int iidmNode) {
        // We use IIDM API to locate node-breaker buses
        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();
        if (!topo.hasAttachedEquipment(iidmNode)) {
            LOG.error("IIDM node {} not valid", iidmNode);
            return null;
        }
        // If there is no Terminal at this IIDM node,
        // then find from it the first connected node with a Terminal
        Terminal t = topo.getOptionalTerminal(iidmNode)
                .orElseGet(() -> Networks.getEquivalentTerminal(vl, iidmNode));
        if (t == null) {
            LOG.error("Can't find a Terminal for IIDM node {}", iidmNode);
            return null;
        }
        return t.getBusView().getBus();
    }

    private void addVoltagesForBoundaryNodes() {
        // Add voltages for TP nodes located at boundaries
        PropertyBags svVoltages = new PropertyBags();
        boundaryNodesFromDanglingLines.forEach((line, tpNodeId) -> {
            DanglingLine dl = network.getDanglingLine(line);
            double v;
            double angle;
            // If available as a property, use it
            if (dl.hasProperty("v") && dl.hasProperty("angle")) {
                v = Double.valueOf(dl.getProperty("v"));
                angle = Double.valueOf(dl.getProperty("angle"));
            } else {
                // Compute boundary node voltage from known network voltage
                Bus b = dl.getTerminal().getBusBreakerView().getBus();
                if (b != null) {
                    Complex v2 = calcVoltage(dl.getR(), dl.getX(), dl.getG(), dl.getB(), b.getV(), b.getAngle(), dl.getP0(), dl.getQ0());
                    v = v2.abs();
                    angle = Math.toDegrees(v2.getArgument());
                } else {
                    // TODO(Luma) No bus for the dangling line, assume voltage is 0 ?
                    v = 0;
                    angle = 0;
                }
            }
            svVoltages.add(buildSvVoltage(tpNodeId, v, angle));
        });
        cgmes.add(originalSVcontext, "SvVoltage", svVoltages);
    }

    private void addPowerFlows() {
        PropertyBags svPowerFlows = new PropertyBags();
        addPowerFlows(svPowerFlows, network.getLoads());
        addPowerFlows(svPowerFlows, network.getGenerators());
        addPowerFlows(svPowerFlows, network.getShuntCompensators());
        addPowerFlows(svPowerFlows, network.getStaticVarCompensators());
        addPowerFlows(svPowerFlows, network.getBatteries());

        // FIXME(Luma) This has to be reviewed
        // Now, SvPowerFlow at boundaries are set as they were in original CGMES
        // Also, there is a loop through boundary nodes for each terminal
        for (PropertyBag terminal : originalTerminals) {
            if (terminal.getId("SvPowerFlow") == null) {
                continue;
            }
            boundaryNodesFromDanglingLines.values().forEach(value -> {
                if (CgmesTerminal.topologicalNode(terminal).equals(value)) {
                    svPowerFlows.add(buildSvPowerFlow(terminal));
                }
            });
        }
        cgmes.add(originalSVcontext, "SvPowerFlow", svPowerFlows);
    }

    private <I extends Injection<?>> void addPowerFlows(PropertyBags svPowerFlows, Iterable<I> injections) {
        injections.forEach(i -> {
            int sequenceNumber = 1;
            PropertyBag p = buildSvPowerFlow(i.getTerminal(), sequenceNumber);
            if (p != null) {
                svPowerFlows.add(p);
            } else {
                LOG.error("No SvPowerFlow created for {}", i.getId());
            }
        });
    }

    private void addShuntCompensatorSections() {
        PropertyBags svShuntCompensatorSections = new PropertyBags();
        for (ShuntCompensator s : network.getShuntCompensators()) {
            svShuntCompensatorSections.add(buildSvShuntCompensatorSections(s));
        }
        cgmes.add(originalSVcontext, "SvShuntCompensatorSections", svShuntCompensatorSections);
    }

    private void addTapSteps() {
        // Tap position property name is different on some CIM versions
        String positionPropertyName = getTapChangerPositionName();
        final List<String> tapStepPropertyNames = Arrays.asList(positionPropertyName, CgmesNames.TAP_CHANGER);

        PropertyBags svTapSteps = new PropertyBags();
        for (TwoWindingsTransformer t : network.getTwoWindingsTransformers()) {
            addRatioTapStep(t, t, tapStepPropertyNames, positionPropertyName, svTapSteps);
            addPhaseTapStep(t, t, tapStepPropertyNames, positionPropertyName, svTapSteps);
        }
        for (ThreeWindingsTransformer t : network.getThreeWindingsTransformers()) {
            Arrays.asList(t.getLeg1(), t.getLeg2(), t.getLeg3()).forEach(leg -> {
                addRatioTapStep(leg, t, tapStepPropertyNames, positionPropertyName, svTapSteps);
                addPhaseTapStep(leg, t, tapStepPropertyNames, positionPropertyName, svTapSteps);
            });
        }
        cgmes.add(originalSVcontext, "SvTapStep", svTapSteps);
    }

    private void addRatioTapStep(RatioTapChangerHolder tch, Identifiable<?> transformer, List<String> tapStepPropertyNames, String positionPropertyName, PropertyBags tapSteps) {
        tch.getOptionalRatioTapChanger().ifPresent(tc -> {
            // We do not have an identifier for the tap changer in IIDM
            // We need to query the CGMES model for the original tap changer identifier
            // FIXME(Luma) consider using not deprecated: cgmes.ratioTapChangerListForPowerTransformer(transformer.getId()).get(legNum)
            String tcId = cgmes.ratioTapChangerForPowerTransformer(transformer.getId());
            tapSteps.add(buildSvTapPosition(tapStepPropertyNames, positionPropertyName, tc, tcId));
        });
    }

    private void addPhaseTapStep(PhaseTapChangerHolder tch, Identifiable<?> transformer, List<String> tapStepPropertyNames, String positionPropertyName, PropertyBags tapSteps) {
        tch.getOptionalPhaseTapChanger().ifPresent(tc -> {
            // FIXME(Luma) consider using not deprecated: cgmes.phaseTapChangerListForPowerTransformer(transformer.getId()).get(legNum)
            String tcId = cgmes.phaseTapChangerForPowerTransformer(transformer.getId());
            tapSteps.add(buildSvTapPosition(tapStepPropertyNames, positionPropertyName, tc, tcId));
        });
    }

    private void addStatus() {
        PropertyBags svStatus = new PropertyBags();
        network.getConnectableStream()
            .forEach(c -> {
                svStatus.add(buildSvStatus(c));
            });

        // SvStatus at boundaries set as it was in original CGMES
        // FIXME(Luma) This double loop could be lengthy
        // Confirm if there is a better way of obtaining
        // all equipment connected at boundary nodes
        for (PropertyBag terminal : originalTerminals) {
            if (terminal.getId("SvStatus") == null) {
                continue;
            }
            boundaryNodesFromDanglingLines.values().forEach(value -> {
                if (CgmesTerminal.topologicalNode(terminal).equals(value)) {
                    svStatus.add(buildSvStatus(terminal));
                }
            });
        }
        cgmes.add(originalSVcontext, "SvStatus", svStatus);
    }

    private Map<String, String> boundaryNodesFromDanglingLines() {
        Map<String, String> nodesFromLines = new HashMap<>();
        List<String> boundaryNodes = cgmes.boundaryNodes().pluckLocals("Node");

        for (PropertyBag line : cgmes.acLineSegments()) {
            String lineId = line.getId(CgmesNames.AC_LINE_SEGMENT);
            // we need only acLinesSegments that were converted into DanglingLines
            if (network.getDanglingLine(lineId) == null) {
                continue;
            }
            String tpNode1 = cgmes.terminal(line.getId(CgmesNames.TERMINAL1)).topologicalNode();
            String tpNode2 = cgmes.terminal(line.getId(CgmesNames.TERMINAL2)).topologicalNode();
            // find not null boundary node for line
            if (boundaryNodes.contains(tpNode1)) {
                nodesFromLines.put(lineId, tpNode1);
            } else if (boundaryNodes.contains(tpNode2)) {
                nodesFromLines.put(lineId, tpNode2);
            }
        }
        return nodesFromLines;
    }

    // FIXME(Luma) Consider having this method in LinkData or SV
    private static Complex calcVoltage(double r, double x, double g, double b,
        double v, double angle, double p, double q) {
        BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(r, x, 1.0, 0.0, 1.0, 0.0,
            new Complex(g * 0.5, b * 0.5), new Complex(g * 0.5, b * 0.5));
        Complex v1 = ComplexUtils.polar2Complex(v, Math.toRadians(angle));
        Complex s1 = new Complex(p, q);
        return (s1.conjugate().divide(v1.conjugate()).subtract(adm.y11().multiply(v1))).divide(adm.y12());
    }

    private String getTapChangerPositionName() {
        if (cgmes instanceof CgmesModelTripleStore) {
            return cimVersion == 14 ? CgmesNames.CONTINUOUS_POSITION : CgmesNames.POSITION;
        } else {
            return CgmesNames.POSITION;
        }
    }

    // added TopologicalIsland as it was in cgmes : original topology is
    // preserved.
    private void addTopologicalIslandsToCgmes() {
        // For properties such as "cim:TopologicalIsland.TopologicalNodes", which might
        // have arbitrary number of values, SPARQL will return multiple result sets.
        // All properties values will be equal, except the multiValued property
        // TopologicalNodes.
        // Also, there can be > 1 TopologicalIsland, so we need to re-group PropertyBags
        // by TopologicalIsland ID.
        Map<String, PropertyBags> byTopologicalIslandId = new HashMap<>();
        originalTopologicalIslands.forEach(pb -> {
            String island = pb.getId(CgmesNames.TOPOLOGICAL_ISLAND);
            if (byTopologicalIslandId.keySet().contains(island)) {
                byTopologicalIslandId.get(island).add(pb);
            } else {
                PropertyBags pbs = new PropertyBags();
                pbs.add(pb);
                byTopologicalIslandId.put(island, pbs);
            }
        });
        // now we can process all TPNodes from each island, and put them in one
        // multivaluedProperty.
        PropertyBags topologicalIslands = new PropertyBags();
        byTopologicalIslandId.values().forEach(island -> {
            PropertyBag topologicalIsland = new PropertyBag(SV_TOPOLOGICALISLAND_PROPERTIES);
            topologicalIsland.setClassPropertyNames(Collections.singletonList(CgmesNames.NAME));
            topologicalIsland.setMultivaluedProperty(Collections.singletonList("TopologicalNodes"));
            topologicalIsland.put(CgmesNames.NAME, island.get(0).getId("name"));
            topologicalIsland.put(CgmesNames.ANGLEREF_TOPOLOGICALNODE,
                island.get(0).getId(CgmesNames.ANGLEREF_TOPOLOGICALNODE));
            topologicalIsland.put(CgmesNames.TOPOLOGICAL_NODES,
                String.join(",", island.pluckLocals(CgmesNames.TOPOLOGICAL_NODES)));
            topologicalIslands.add(topologicalIsland);
        });
        cgmes.add(originalSVcontext, CgmesNames.TOPOLOGICAL_ISLAND, topologicalIslands);
    }

    // Added full model data with proper profile (StateVariables)
    // FullModel is defined in ModelDescription:
    // http://iec.ch/TC57/61970-552/ModelDescription/1#
    private void addModelDescriptionToCgmes() {
        PropertyBags fullModelSV = new PropertyBags();
        // for properties such as "md:Model.DependentOn" which might have arbitrary
        // number of values, SPARQL will return multiple result sets.
        // All are equal, except the multiValued property value.
        if (!originalFullModel.isEmpty()) {
            PropertyBag newModelDescription = new PropertyBag(SV_FULLMODEL_PROPERTIES);
            newModelDescription
                .setClassPropertyNames(
                    Arrays.asList(CgmesNames.SCENARIO_TIME, CgmesNames.CREATED, CgmesNames.DESCRIPTION,
                        CgmesNames.VERSION, CgmesNames.DEPENDENT_ON, CgmesNames.PROFILE,
                        CgmesNames.MODELING_AUTHORITY_SET));
            newModelDescription.setMultivaluedProperty(Collections.singletonList(CgmesNames.DEPENDENT_ON));
            newModelDescription.put(CgmesNames.SCENARIO_TIME, originalFullModel.get(0).getId("scenarioTime"));
            newModelDescription.put(CgmesNames.CREATED, originalFullModel.get(0).getId("created"));
            newModelDescription.put(CgmesNames.DESCRIPTION, originalFullModel.get(0).getId("description"));
            newModelDescription.put(CgmesNames.VERSION, originalFullModel.get(0).getId("version"));
            newModelDescription.put(CgmesNames.DEPENDENT_ON,
                String.join(",", originalFullModel.pluckLocals("DependentOn")));
            newModelDescription.put(CgmesNames.PROFILE, originalFullModel.get(0).getId("profile"));
            newModelDescription.put(CgmesNames.MODELING_AUTHORITY_SET,
                originalFullModel.get(0).getId("modelingAuthoritySet"));
            fullModelSV.add(newModelDescription);
            cgmes.add(originalSVcontext, CgmesNames.FULL_MODEL, fullModelSV);
        }
    }

    private static PropertyBag buildSvVoltage(String tpNode, double voltage, double angle) {
        PropertyBag p = new PropertyBag(SV_VOLTAGE_PROPERTIES);
        p.put(CgmesNames.TOPOLOGICAL_NODE, tpNode);
        p.put(CgmesNames.VOLTAGE, fstr(voltage));
        p.put(CgmesNames.ANGLE, fstr(angle));
        return p;
    }

    private PropertyBag buildSvPowerFlow(Terminal terminal, int sequenceNumber) {
        String cgmesTerminal = ((Connectable<?>) terminal.getConnectable()).getAliasFromType(CgmesNames.TERMINAL1).orElse(null);
        if (cgmesTerminal == null) {
            return null;
        }
        PropertyBag p = new PropertyBag(SV_POWERFLOW_PROPERTIES);
        p.put("p", fstr(terminal.getP()));
        p.put("q", fstr(terminal.getQ()));
        p.put(CgmesNames.TERMINAL, cgmesTerminal);
        return p;
    }

    private static PropertyBag buildSvPowerFlow(PropertyBag terminal) {
        PropertyBag p = new PropertyBag(SV_POWERFLOW_PROPERTIES);
        p.put("p", terminal.getId("p"));
        p.put("q", terminal.getId("q"));
        p.put(CgmesNames.TERMINAL, terminal.getId(CgmesNames.TERMINAL));
        return p;
    }

    private static PropertyBag buildSvTapPosition(List<String> properties, String tapPositionName, TapChanger<?, ?> tc, String tcId) {
        PropertyBag p = new PropertyBag(properties);
        p.put(tapPositionName, istr(tc.getTapPosition()));
        p.put(CgmesNames.TAP_CHANGER, tcId);
        return p;
    }

    private static PropertyBag buildSvShuntCompensatorSections(ShuntCompensator s) {
        PropertyBag p = new PropertyBag(SV_SHUNTCOMPENSATORSECTIONS_PROPERTIES);
        p.put("continuousSections", istr(s.getSectionCount()));
        p.put("ShuntCompensator", s.getId());
        return p;
    }

    private static PropertyBag buildSvStatus(Connectable<?> c) {
        PropertyBag p = new PropertyBag(SV_SVSTATUS_PROPERTIES);
        p.put(IN_SERVICE, Boolean.toString(c.getTerminals().stream().anyMatch(Terminal::isConnected)));
        p.put(CgmesNames.CONDUCTING_EQUIPMENT, c.getId());
        return p;
    }

    private static PropertyBag buildSvStatus(PropertyBag terminal) {
        PropertyBag p = new PropertyBag(SV_SVSTATUS_PROPERTIES);
        p.put(IN_SERVICE, terminal.getId(IN_SERVICE));
        p.put(CgmesNames.CONDUCTING_EQUIPMENT, terminal.getId(CgmesNames.CONDUCTING_EQUIPMENT));
        return p;
    }

    // Avoid trailing zeros
    private static String fstr(double value) {
        return CgmesExport.format(value);
    }

    private static String istr(int value) {
        return String.valueOf(value);
    }

    private final CgmesModel cgmes;
    private final Network network;
    private final int cimVersion;
    private final PropertyBags originalTerminals;
    private final PropertyBags originalFullModel;
    private final PropertyBags originalTopologicalIslands;
    private final PropertyBags originalTopologicalNodes;
    private final String originalSVcontext;
    private final Map<String, String> boundaryNodesFromDanglingLines;
    private static final String IN_SERVICE = "inService";

    private static final List<String> SV_VOLTAGE_PROPERTIES = Arrays.asList(CgmesNames.ANGLE, CgmesNames.VOLTAGE,
        CgmesNames.TOPOLOGICAL_NODE);
    private static final List<String> SV_POWERFLOW_PROPERTIES = Arrays.asList("p", "q", CgmesNames.TERMINAL);
    private static final List<String> SV_SHUNTCOMPENSATORSECTIONS_PROPERTIES = Arrays.asList("ShuntCompensator",
        "continuousSections");
    private static final List<String> SV_SVSTATUS_PROPERTIES = Arrays.asList(IN_SERVICE,
        CgmesNames.CONDUCTING_EQUIPMENT);
    private static final List<String> SV_FULLMODEL_PROPERTIES = Arrays.asList(CgmesNames.SCENARIO_TIME,
        CgmesNames.CREATED, CgmesNames.DESCRIPTION,
        CgmesNames.VERSION, CgmesNames.DEPENDENT_ON,
        CgmesNames.PROFILE, CgmesNames.MODELING_AUTHORITY_SET);
    private static final List<String> SV_TOPOLOGICALISLAND_PROPERTIES = Arrays.asList(CgmesNames.NAME,
        CgmesNames.ANGLEREF_TOPOLOGICALNODE, CgmesNames.TOPOLOGICAL_NODES);

    private static final Logger LOG = LoggerFactory.getLogger(StateVariablesAdder.class);
}
