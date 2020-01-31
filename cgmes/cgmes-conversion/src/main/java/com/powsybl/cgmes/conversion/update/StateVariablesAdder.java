/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.util.LinkData;
import com.powsybl.iidm.network.util.LinkData.BranchAdmittanceMatrix;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author Elena Kaltakova <kaltakovae at aia.es>
 */
public class StateVariablesAdder {

    public StateVariablesAdder(CgmesModel cgmes) {
        this.cgmes = Objects.requireNonNull(cgmes);
        this.originalSVdata = originalSVdata(cgmes);
        this.originalSVcontext = originalSVcontext(cgmes);
    }

    private Map<String, PropertyBags> originalSVdata(CgmesModel cgmes) {
        originalSVdata.put(TERMINALS_SV, cgmes.terminalsSV());
        if (!isCimVersion14(cgmes)) {
            originalSVdata.put("fullModelSV", cgmes.fullModelSV());
            originalSVdata.put("topologicalIslands", cgmes.topologicalIslands());
        }
        return originalSVdata;
    }

    private String originalSVcontext(CgmesModel cgmes) {
        // if grid has no FullModel, then we can return Subset
        if (!isCimVersion14(cgmes)) {
            PropertyBags pb = cgmes.fullModelSV();
            if (pb.get(0).containsKey("graph")) {
                return pb.get(0).getId("graph");
            }
        }
        return CgmesSubset.STATE_VARIABLES.toString();
    }

    public void add(Network n, CgmesModel cgmes) {

        if (!isCimVersion14(cgmes)) {
            addModelDescription();
            addTopologicalIslands();
        }

        PropertyBags voltages = new PropertyBags();
        // Check for bus branch model
        if (cgmes.isNodeBreaker()) {
            return;
        }
        // add voltages for TpNodes existing in the Model
        for (PropertyBag tn : cgmes.topologicalNodes()) {
            Bus b = n.getBusBreakerView().getBus(tn.getId(CgmesNames.TOPOLOGICAL_NODE));
            PropertyBag p = new PropertyBag(SV_VOLTAGE_PROPERTIES);
            if (b != null) {
                p.put(CgmesNames.ANGLE, fs(b.getAngle()));
                p.put(CgmesNames.VOLTAGE, fs(b.getV()));
                p.put(CgmesNames.TOPOLOGICAL_NODE, tn.getId(CgmesNames.TOPOLOGICAL_NODE));
                voltages.add(p);
            }
        }

        // add voltages for TpNodes existing in the Model's boundaries
        Map<String, String> boundaryNodesFromDanglingLines = boundaryNodesFromDanglingLines(cgmes, n);
        boundaryNodesFromDanglingLines.entrySet().forEach(entry -> {
            PropertyBag p = new PropertyBag(SV_VOLTAGE_PROPERTIES);
            DanglingLine dl = n.getDanglingLine(entry.getKey());
            Bus b = dl.getTerminal().getBusBreakerView().getBus();
            if (b != null) {
                // calculate complex voltage value: abs for VOLTAGE, degrees for ANGLE
                Complex v2 = complexVoltage(dl.getR(), dl.getX(), dl.getG(), dl.getB(), b.getV(), b.getAngle(),
                    dl.getP0(), dl.getQ0());
                p.put(CgmesNames.ANGLE, fs(Math.toDegrees(v2.getArgument())));
                p.put(CgmesNames.VOLTAGE, fs(v2.abs()));
                p.put(CgmesNames.TOPOLOGICAL_NODE, entry.getValue());
            } else {
                p.put(CgmesNames.ANGLE, fs(0.0));
                p.put(CgmesNames.VOLTAGE, fs(0.0));
                p.put(CgmesNames.TOPOLOGICAL_NODE, entry.getValue());
            }
            voltages.add(p);
        });

        cgmes.add(originalSVcontext, "SvVoltage", voltages);

        PropertyBags powerFlows = new PropertyBags();
        for (Load l : n.getLoads()) {
            PropertyBag p = createPowerFlowProperties(cgmes, l.getTerminal());
            if (p != null) {
                powerFlows.add(p);
            } else {
                // FIXME CGMES SvInjection objects created as loads
                LOG.error("No SvPowerFlow created for load {}", l.getId());
            }
        }
        for (Generator g : n.getGenerators()) {
            PropertyBag p = createPowerFlowProperties(cgmes, g.getTerminal());
            if (p != null) {
                powerFlows.add(p);
            }
        }
        for (ShuntCompensator s : n.getShuntCompensators()) {
            PropertyBag p = createPowerFlowProperties(cgmes, s.getTerminal());
            if (p != null) {
                powerFlows.add(p);
            }
        }

        // PowerFlow at boundaries set as it was in original cgmes.
        for (PropertyBag terminal : originalSVdata.get(TERMINALS_SV)) {
            Objects.requireNonNull(terminal);
            if (terminal.getId("SvPowerFlow") == null) {
                continue;
            }
            boundaryNodesFromDanglingLines.values().forEach(value -> {
                if (terminal.getId(CgmesNames.TOPOLOGICAL_NODE).equals(value)) {
                    PropertyBag p = new PropertyBag(SV_POWERFLOW_PROPERTIES);
                    p.put("p", terminal.getId("p"));
                    p.put("q", terminal.getId("q"));
                    p.put(CgmesNames.TERMINAL, terminal.getId(CgmesNames.TERMINAL));
                    powerFlows.add(p);
                }
            });
        }
        cgmes.add(originalSVcontext, "SvPowerFlow", powerFlows);

        PropertyBags shuntCompensatorSections = new PropertyBags();
        for (ShuntCompensator s : n.getShuntCompensators()) {
            PropertyBag p = new PropertyBag(SV_SHUNTCOMPENSATORSECTIONS_PROPERTIES);
            p.put("continuousSections", is(s.getCurrentSectionCount()));
            p.put("ShuntCompensator", s.getId());
            shuntCompensatorSections.add(p);
        }
        cgmes.add(originalSVcontext, "SvShuntCompensatorSections", shuntCompensatorSections);

        PropertyBags tapSteps = new PropertyBags();
        String tapChangerPositionName = getTapChangerPositionName(cgmes);
        final List<String> svTapStepProperties = Arrays.asList(tapChangerPositionName, CgmesNames.TAP_CHANGER);
        for (TwoWindingsTransformer t : n.getTwoWindingsTransformers()) {
            PropertyBag p = new PropertyBag(svTapStepProperties);
            // TODO If we could store an identifier for the tap changer in IIDM
            // then we would not need to query the CGMES model
            if (t.getPhaseTapChanger() != null) {
                p.put(tapChangerPositionName, is(t.getPhaseTapChanger().getTapPosition()));
                p.put(CgmesNames.TAP_CHANGER, cgmes.phaseTapChangerForPowerTransformer(t.getId()));
                tapSteps.add(p);
            } else if (t.getRatioTapChanger() != null) {
                p.put(tapChangerPositionName, is(t.getRatioTapChanger().getTapPosition()));
                p.put(CgmesNames.TAP_CHANGER, cgmes.ratioTapChangerForPowerTransformer(t.getId()));
                tapSteps.add(p);
            }
        }
        cgmes.add(originalSVcontext, "SvTapStep", tapSteps);

        // create SvStatus, iterate on Connectables, check Terminal status, add
        // to SvStatus
        PropertyBags svStatus = new PropertyBags();
        Map<String, Boolean> addedConnectables = new HashMap<>();
        for (VoltageLevel v : n.getVoltageLevels()) {
            for (Connectable<?> c : v.getConnectables()) {
                for (Terminal t : c.getTerminals()) {
                    if (t == null) {
                        continue;
                    }
                    // need to check if connectable was already added
                    if (addedConnectables.get(c.getId()) == null) {
                        addedConnectables.put(c.getId(), true);
                        PropertyBag p = new PropertyBag(SV_SVSTATUS_PROPERTIES);
                        if (c.getId() != null) {
                            p.put(IN_SERVICE, String.valueOf(t.isConnected()));
                            p.put(CgmesNames.CONDUCTING_EQUIPMENT, c.getId());
                            svStatus.add(p);
                        }
                    }
                }
            }
        }

        // SvStatus at boundaries set as it was in original cgmes.
        for (PropertyBag terminal : originalSVdata.get(TERMINALS_SV)) {
            Objects.requireNonNull(terminal);
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

    private boolean isCimVersion14(CgmesModel cgmes) {
        boolean is14 = false;
        if (cgmes instanceof CgmesModelTripleStore) {
            is14 = ((CgmesModelTripleStore) cgmes).getCimNamespace().indexOf("cim14#") != -1;
        }
        return is14;
    }

    // added TopologicalIsland as it was in cgmes : original topology is
    // preserved.
    private void addTopologicalIslands() {
        PropertyBags originalTpIslands = originalSVdata.get("topologicalIslands");
        if (!originalTpIslands.isEmpty()) {
            // there can be > 1 TopologicalIsland, we need to re-group PropertyBags by
            // TopologicalIsland ID. For each TopologicalIsland we will have multiple
            // results from SPARQL query,
            // due to Multivalued Property "TopologicalNodes"
            Map<String, PropertyBags> byTopologicalIslandId = new HashMap<>();
            originalTpIslands.forEach(pb -> {
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
            byTopologicalIslandId.values().forEach(value -> {
                PropertyBag topologicalIsland = new PropertyBag(SV_TOPOLOGICALISLAND_PROPERTIES);
                topologicalIsland.setClassPropertyNames(Arrays.asList(CgmesNames.NAME));
                topologicalIsland.setMultivaluedProperty(Arrays.asList("TopologicalNodes"));
                topologicalIsland.put(CgmesNames.NAME, value.get(0).getId("name"));
                topologicalIsland.put(CgmesNames.ANGLEREF_TOPOLOGICALNODE,
                    value.get(0).getId(CgmesNames.ANGLEREF_TOPOLOGICALNODE));
                topologicalIsland.put(CgmesNames.TOPOLOGICAL_NODES,
                    getMultivaluedProperty(value, CgmesNames.TOPOLOGICAL_NODES));
                topologicalIslands.add(topologicalIsland);
            });
            cgmes.add(originalSVcontext, CgmesNames.TOPOLOGICAL_ISLAND, topologicalIslands);
        }
    }

    // Added full model data with proper profile (StateVariables)
    // FullModel is defined in ModelDescription:
    // http://iec.ch/TC57/61970-552/ModelDescription/1#
    private void addModelDescription() {
        PropertyBags fullModelSV = new PropertyBags();
        PropertyBags originalModelDescription = originalSVdata.get("fullModelSV");
        // for properties such as "md:Model.DependentOn" which might have arbitrary
        // number of values, SPARQL will return multiple result sets.
        // All are equal, except the multiValued property value.
        if (!originalModelDescription.isEmpty()) {
            PropertyBag newModelDescription = new PropertyBag(SV_FULLMODEL_PROPERTIES);
            newModelDescription
                .setClassPropertyNames(
                    Arrays.asList(CgmesNames.SCENARIO_TIME, CgmesNames.CREATED, CgmesNames.DESCRIPTION,
                        CgmesNames.VERSION, CgmesNames.DEPENDENT_ON, CgmesNames.PROFILE,
                        CgmesNames.MODELING_AUTHORITY_SET));
            newModelDescription.setMultivaluedProperty(Arrays.asList(CgmesNames.DEPENDENT_ON));
            newModelDescription.put(CgmesNames.SCENARIO_TIME, originalModelDescription.get(0).getId("scenarioTime"));
            newModelDescription.put(CgmesNames.CREATED, originalModelDescription.get(0).getId("created"));
            newModelDescription.put(CgmesNames.DESCRIPTION, originalModelDescription.get(0).getId("description"));
            newModelDescription.put(CgmesNames.VERSION, originalModelDescription.get(0).getId("version"));
            newModelDescription.put(CgmesNames.DEPENDENT_ON,
                getMultivaluedProperty(originalModelDescription, "DependentOn"));
            newModelDescription.put(CgmesNames.PROFILE, originalModelDescription.get(0).getId("profile"));
            newModelDescription.put(CgmesNames.MODELING_AUTHORITY_SET,
                originalModelDescription.get(0).getId("modelingAuthoritySet"));
            fullModelSV.add(newModelDescription);
            cgmes.add(originalSVcontext, CgmesNames.FULL_MODEL, fullModelSV);
        }
    }

    private String getMultivaluedProperty(PropertyBags pb, String multivaluedPropertyName) {
        // for properties such as "md:Model.DependentOn" which might have arbitrary
        // number of values, SPARQL will return multiple result sets. We will loop
        // through all and collect all values for multiValued property.
        List<String> list = new ArrayList<>();
        pb.forEach(m -> list.add(m.getId(multivaluedPropertyName)));
        return String.join(",", list);
    }

    private static Complex complexVoltage(double r, double x, double g, double b,
        double v, double angle, double p, double q) {
        BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(r, x, 1.0, 0.0, 1.0, 0.0,
            new Complex(g * 0.5, b * 0.5), new Complex(g * 0.5, b * 0.5));
        Complex v1 = ComplexUtils.polar2Complex(v, Math.toRadians(angle));
        Complex s1 = new Complex(p, q);
        return (s1.conjugate().divide(v1.conjugate()).subtract(adm.y11.multiply(v1))).divide(adm.y12);
    }

    private static Map<String, String> boundaryNodesFromDanglingLines(CgmesModel cgmes, Network n) {
        Map<String, String> nodesFromLines = new HashMap<>();
        List<String> boundaryNodes = new ArrayList<>();
        cgmes.boundaryNodes().forEach(node -> boundaryNodes.add(node.getId("Node")));

        for (PropertyBag line : cgmes.acLineSegments()) {
            String lineId = line.getId(CgmesNames.AC_LINE_SEGMENT);
            // we need only acLinesSegments that were converted into DanglingLines
            if (n.getDanglingLine(lineId) == null) {
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

    private static String getTapChangerPositionName(CgmesModel cgmes) {
        if (cgmes instanceof CgmesModelTripleStore) {
            return (((CgmesModelTripleStore) cgmes).getCimNamespace().indexOf("cim14#") != -1)
                ? CgmesNames.CONTINUOUS_POSITION
                : CgmesNames.POSITION;
        } else {
            return CgmesNames.POSITION;
        }
    }

    private static PropertyBag createPowerFlowProperties(CgmesModel cgmes, Terminal terminal) {
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

    private static String fs(double value) {
        return String.valueOf(value);
    }

    private static String is(int value) {
        return String.valueOf(value);
    }

    private CgmesModel cgmes;
    private Map<String, PropertyBags> originalSVdata = new HashMap<>();
    private String originalSVcontext;
    private static final String TERMINALS_SV = "terminalsSV";
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
