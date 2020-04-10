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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.util.LinkData;
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
        if (cgmes.isNodeBreaker()) {
            // TODO we need to export SV file data for NodeBraker
            LOG.warn("NodeBreaker view require further investigation to map correctly Topological Nodes");
            return;
        }
        // Clear the previous SV data in CGMES model
        // and fill it with the Network current state values
        cgmes.clear(CgmesSubset.STATE_VARIABLES);

        if (cimVersion != 14) {
            addModelDescriptionToCgmes();
            addTopologicalIslandsToCgmes();
        }

        addVoltagesForTopologicalNodes();

        addVoltagesForBoundaryNodes();

        addPowerFlowToCgmes();

        addShuntCompensatorSectionsToCgmes();

        addTapStepToCgmes();

        addStatusToCgmes();
    }

    private void addVoltagesForTopologicalNodes() {
        PropertyBags voltages = new PropertyBags();
        // add voltages for TpNodes existing in the Model
        for (PropertyBag tn : cgmes.topologicalNodes()) {
            Bus b = network.getBusBreakerView().getBus(tn.getId(CgmesNames.TOPOLOGICAL_NODE));
            PropertyBag p = new PropertyBag(SV_VOLTAGE_PROPERTIES);
            if (b != null) {
                p.put(CgmesNames.ANGLE, fs(b.getAngle()));
                p.put(CgmesNames.VOLTAGE, fs(b.getV()));
                p.put(CgmesNames.TOPOLOGICAL_NODE, tn.getId(CgmesNames.TOPOLOGICAL_NODE));
                voltages.add(p);
            }
        }
        cgmes.add(originalSVcontext, "SvVoltage", voltages);
    }

    private void addVoltagesForBoundaryNodes() {
        PropertyBags voltages = new PropertyBags();
        // add voltages for TpNodes existing in the Model's boundaries
        boundaryNodesFromDanglingLines.forEach((line, bnode) -> {
            PropertyBag p = new PropertyBag(SV_VOLTAGE_PROPERTIES);
            DanglingLine dl = network.getDanglingLine(line);
            Bus b = dl.getTerminal().getBusBreakerView().getBus();
            if (b != null) {
                // calculate complex voltage value: abs for VOLTAGE, degrees for ANGLE
                Complex v2 = complexVoltage(dl.getR(), dl.getX(), dl.getG(), dl.getB(), b.getV(), b.getAngle(),
                    dl.getP0(), dl.getQ0());
                p.put(CgmesNames.ANGLE, fs(Math.toDegrees(v2.getArgument())));
                p.put(CgmesNames.VOLTAGE, fs(v2.abs()));
                p.put(CgmesNames.TOPOLOGICAL_NODE, bnode);
            } else {
                p.put(CgmesNames.ANGLE, fs(0.0));
                p.put(CgmesNames.VOLTAGE, fs(0.0));
                p.put(CgmesNames.TOPOLOGICAL_NODE, bnode);
            }
            voltages.add(p);
        });
        cgmes.add(originalSVcontext, "SvVoltage", voltages);
    }

    private void addPowerFlowToCgmes() {
        PropertyBags powerFlows = new PropertyBags();
        addInjectionPowerFlowToCgmes(powerFlows, network.getLoads());
        addInjectionPowerFlowToCgmes(powerFlows, network.getGenerators());
        addInjectionPowerFlowToCgmes(powerFlows, network.getShuntCompensators());
        addInjectionPowerFlowToCgmes(powerFlows, network.getStaticVarCompensators());
        addInjectionPowerFlowToCgmes(powerFlows, network.getBatteries());

        // PowerFlow at boundaries set as it was in original cgmes.
        for (PropertyBag terminal : originalTerminals) {
            if (terminal.getId("SvPowerFlow") == null) {
                continue;
            }
            boundaryNodesFromDanglingLines.values().forEach(value -> {
                if (CgmesTerminal.topologicalNode(terminal).equals(value)) {
                    PropertyBag p = new PropertyBag(SV_POWERFLOW_PROPERTIES);
                    p.put("p", terminal.getId("p"));
                    p.put("q", terminal.getId("q"));
                    p.put(CgmesNames.TERMINAL, terminal.getId(CgmesNames.TERMINAL));
                    powerFlows.add(p);
                }
            });
        }
        cgmes.add(originalSVcontext, "SvPowerFlow", powerFlows);
    }

    private <I extends Injection> void addInjectionPowerFlowToCgmes(PropertyBags powerFlows,
        Iterable<I> injectionStream) {
        injectionStream.forEach(i -> {
            PropertyBag p = createPowerFlowProperties(i.getTerminal());
            if (p != null) {
                powerFlows.add(p);
            } else if (i instanceof Load) {
                // FIXME CGMES SvInjection objects created as loads
                LOG.error("No SvPowerFlow created for load {}", i.getId());
            }
        });
    }

    private void addShuntCompensatorSectionsToCgmes() {
        PropertyBags shuntCompensatorSections = new PropertyBags();
        for (ShuntCompensator s : network.getShuntCompensators()) {
            PropertyBag p = new PropertyBag(SV_SHUNTCOMPENSATORSECTIONS_PROPERTIES);
            p.put("continuousSections", is(s.getCurrentSectionCount()));
            p.put("ShuntCompensator", s.getId());
            shuntCompensatorSections.add(p);
        }
        cgmes.add(originalSVcontext, "SvShuntCompensatorSections", shuntCompensatorSections);
    }

    private void addTapStepToCgmes() {
        PropertyBags tapSteps = new PropertyBags();
        String tapChangerPositionName = getTapChangerPositionName();
        final List<String> svTapStepProperties = Arrays.asList(tapChangerPositionName, CgmesNames.TAP_CHANGER);
        for (TwoWindingsTransformer t : network.getTwoWindingsTransformers()) {
            PropertyBag p = new PropertyBag(svTapStepProperties);
            // TODO If we could store an identifier for the tap changer in IIDM
            // then we would not need to query the CGMES model
            if (hasPhaseTapChanger(t)) {
                p.put(tapChangerPositionName, is(t.getPhaseTapChanger().getTapPosition()));
                p.put(CgmesNames.TAP_CHANGER, cgmes.phaseTapChangerForPowerTransformer(t.getId()));
                tapSteps.add(p);
            } else if (hasRatioTapChanger(t)) {
                p.put(tapChangerPositionName, is(t.getRatioTapChanger().getTapPosition()));
                p.put(CgmesNames.TAP_CHANGER, cgmes.ratioTapChangerForPowerTransformer(t.getId()));
                tapSteps.add(p);
            }
        }

        for (ThreeWindingsTransformer t : network.getThreeWindingsTransformers()) {
            PropertyBag p = new PropertyBag(svTapStepProperties);
            Arrays.asList(t.getLeg1(), t.getLeg2(), t.getLeg3()).forEach(leg -> {
                if (hasPhaseTapChanger(leg)) {
                    p.put(tapChangerPositionName, is(leg.getPhaseTapChanger().getTapPosition()));
                    p.put(CgmesNames.TAP_CHANGER, cgmes.phaseTapChangerForPowerTransformer(t.getId()));
                    tapSteps.add(p);
                } else if (hasRatioTapChanger(leg)) {
                    p.put(tapChangerPositionName, is(leg.getRatioTapChanger().getTapPosition()));
                    p.put(CgmesNames.TAP_CHANGER, cgmes.ratioTapChangerForPowerTransformer(t.getId()));
                    tapSteps.add(p);
                }
            });
        }

        cgmes.add(originalSVcontext, "SvTapStep", tapSteps);
    }

    private boolean hasPhaseTapChanger(Object leg) {
        if (leg instanceof Leg) {
            return ((Leg) leg).getPhaseTapChanger() != null;
        } else if (leg instanceof TwoWindingsTransformer) {
            return ((TwoWindingsTransformer) leg).getPhaseTapChanger() != null;
        }
        return false;
    }

    private boolean hasRatioTapChanger(Object leg) {
        if (leg instanceof Leg) {
            return ((Leg) leg).getRatioTapChanger() != null;
        } else if (leg instanceof TwoWindingsTransformer) {
            return ((TwoWindingsTransformer) leg).getRatioTapChanger() != null;
        }
        return false;
    }

    private void addStatusToCgmes() {
        // create SvStatus, iterate on Connectables, check Terminal status, add
        // to SvStatus
        PropertyBags svStatus = new PropertyBags();
        network.getConnectableStream()
            .forEach(c -> {
                PropertyBag p = new PropertyBag(SV_SVSTATUS_PROPERTIES);
                p.put(IN_SERVICE,
                    Boolean.toString(((Connectable<?>) c).getTerminals().stream().anyMatch(Terminal::isConnected)));
                p.put(CgmesNames.CONDUCTING_EQUIPMENT, c.getId());
                svStatus.add(p);
            });

        // SvStatus at boundaries set as it was in original cgmes.
        for (PropertyBag terminal : originalTerminals) {
            if (terminal.getId("SvStatus") == null) {
                continue;
            }
            boundaryNodesFromDanglingLines.values().forEach(value -> {
                if (terminal.getId(CgmesNames.TOPOLOGICAL_NODE).equals(value)) {
                    PropertyBag p = new PropertyBag(SV_SVSTATUS_PROPERTIES);
                    p.put(IN_SERVICE, terminal.getId(IN_SERVICE));
                    p.put(CgmesNames.CONDUCTING_EQUIPMENT, terminal.getId(CgmesNames.CONDUCTING_EQUIPMENT));
                    svStatus.add(p);
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

    private static Complex complexVoltage(double r, double x, double g, double b,
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

    private PropertyBag createPowerFlowProperties(Terminal terminal) {
        // TODO If we could store a terminal identifier in IIDM
        // we would not need to obtain it querying CGMES for the related equipment
        String cgmesTerminal = cgmes.terminalForEquipment(terminal.getConnectable().getId());
        if (cgmesTerminal == null) {
            return null;
        }
        PropertyBag p = new PropertyBag(SV_POWERFLOW_PROPERTIES);
        p.put("p", fs(terminal.getP()));
        p.put("q", fs(terminal.getQ()));
        p.put(CgmesNames.TERMINAL, cgmesTerminal);
        return p;
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

    private static String fs(double value) {
        return Double.isNaN(value) ? String.valueOf(0.0) : String.valueOf(value);
    }

    private static String is(int value) {
        return String.valueOf(value);
    }

    private final CgmesModel cgmes;
    private final Network network;
    private final int cimVersion;
    private final PropertyBags originalTerminals;
    private final PropertyBags originalFullModel;
    private final PropertyBags originalTopologicalIslands;
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
