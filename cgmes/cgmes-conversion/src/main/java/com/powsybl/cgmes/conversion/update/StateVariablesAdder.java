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

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.update.LinkDataTmp.BranchAdmittanceMatrix;
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
        this.cgmes = cgmes;
        this.originalSVdata = originalSVdata(cgmes);
    }

    public Map<String, PropertyBags> originalSVdata(CgmesModel cgmes) {
        originalSVdata.put("terminalsSV", cgmes.terminalsSV());
        if (!isCimVersion14(cgmes)) {
            originalSVdata.put("fullModelSV", cgmes.fullModelSV());
        }
        return originalSVdata;
    }

    private String getDependentOn() {
        List<String> l = new ArrayList<>();
        originalSVdata.get("fullModelSV").forEach(m -> {
            l.add(m.getId("DependentOn"));
        });
        return String.join(",", l);
    }

    public void add(Network n, CgmesModel cgmes) {
        // TODO Add full model data with proper profile (StateVariables)
        // FullModel is defined in ModelDescription:
        // http://iec.ch/TC57/61970-552/ModelDescription/1#
        if (!isCimVersion14(cgmes)) {
            addFullModel();
        }

        // TODO add TopologicalIsland as it was in cgmes : original topology is
        // preserved.

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

        cgmes.add(CgmesSubset.STATE_VARIABLES, "SvVoltage", voltages);

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
        for (PropertyBag terminal : originalSVdata.get("terminalsSV")) {
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
        cgmes.add(CgmesSubset.STATE_VARIABLES, "SvPowerFlow", powerFlows);

        PropertyBags shuntCompensatorSections = new PropertyBags();
        for (ShuntCompensator s : n.getShuntCompensators()) {
            PropertyBag p = new PropertyBag(SV_SHUNTCOMPENSATORSECTIONS_PROPERTIES);
            p.put("continuousSections", is(s.getCurrentSectionCount()));
            p.put("ShuntCompensator", s.getId());
            shuntCompensatorSections.add(p);
        }
        cgmes.add(CgmesSubset.STATE_VARIABLES, "SvShuntCompensatorSections", shuntCompensatorSections);

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
        cgmes.add(CgmesSubset.STATE_VARIABLES, "SvTapStep", tapSteps);

        // create SvStatus, iterate on Connectables, check if Terminal status, add
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
                            p.put("inService", String.valueOf(t.isConnected()));
                            p.put(CgmesNames.CONDUCTING_EQUIPMENT, c.getId());
                            svStatus.add(p);
                        }
                    }
                }
            }
        }

        // SvStatus at boundaries set as it was in original cgmes.
        for (PropertyBag terminal : originalSVdata.get("terminalsSV")) {
            if (terminal.getId("SvStatus") == null) {
                continue;
            }
            boundaryNodesFromDanglingLines.values().forEach(value -> {
                if (terminal.getId(CgmesNames.TOPOLOGICAL_NODE).equals(value)) {
                    PropertyBag p = new PropertyBag(SV_SVSTATUS_PROPERTIES);
                    p.put("inService", terminal.getId("inService"));
                    p.put(CgmesNames.CONDUCTING_EQUIPMENT, terminal.getId(CgmesNames.CONDUCTING_EQUIPMENT));
                    svStatus.add(p);
                }
            });
        }
        cgmes.add(CgmesSubset.STATE_VARIABLES, "SvStatus", svStatus);
    }

    private boolean isCimVersion14(CgmesModel cgmes) {
        boolean is14 = false;
        if (cgmes instanceof CgmesModelTripleStore) {
            is14 = ((CgmesModelTripleStore) cgmes).getCimNamespace().indexOf("cim14#") != -1;
        }
        return is14;
    }

    private void addFullModel() {
        PropertyBags fullModelSV = new PropertyBags();
        PropertyBag originModelObj = originalSVdata.get("fullModelSV").get(0);

        PropertyBag newModelObj = new PropertyBag(SV_FULLMODEL_PROPERTIES);
        newModelObj.put("scenarioTime", originModelObj.getId("scenarioTime"));
        newModelObj.put("created", originModelObj.getId("created"));
        newModelObj.put("description", originModelObj.getId("description"));
        newModelObj.put("version", originModelObj.getId("version"));
        newModelObj.put("DependentOn", getDependentOn());
        newModelObj.put("profile", originModelObj.getId("profile"));
        newModelObj.put("modelingAuthoritySet", originModelObj.getId("modelingAuthoritySet"));

        fullModelSV.add(newModelObj);
        cgmes.add(CgmesSubset.STATE_VARIABLES, "FullModel", fullModelSV);
    }

    // FIXME elena Uses LinkData class, not yet in master, I've copied in into the
    // package as LinkDataTmp
    private static Complex complexVoltage(double r, double x, double g, double b,
        double v, double angle, double p, double q) {
        BranchAdmittanceMatrix adm = LinkDataTmp.calculateBranchAdmittance(r, x, 1.0, 0.0, 1.0, 0.0,
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

    private static String topologicalNodeFromBusId(String iidmBusId) {
        // TODO Consider potential namingStrategy transformations
        return iidmBusId;
    }

    private static final List<String> SV_VOLTAGE_PROPERTIES = Arrays.asList(CgmesNames.ANGLE, CgmesNames.VOLTAGE,
        CgmesNames.TOPOLOGICAL_NODE);
    private static final List<String> SV_POWERFLOW_PROPERTIES = Arrays.asList("p", "q", CgmesNames.TERMINAL);
    private static final List<String> SV_SHUNTCOMPENSATORSECTIONS_PROPERTIES = Arrays.asList("ShuntCompensator",
        "continuousSections");
    private static final List<String> SV_SVSTATUS_PROPERTIES = Arrays.asList("inService",
        CgmesNames.CONDUCTING_EQUIPMENT);
    private static final List<String> SV_FULLMODEL_PROPERTIES = Arrays.asList("scenarioTime", "created", "description",
        "version", "DependentOn", "profile", "modelingAuthoritySet");
    private CgmesModel cgmes;
    private Map<String, PropertyBags> originalSVdata = new HashMap<>();

    private static final Logger LOG = LoggerFactory.getLogger(StateVariablesAdder.class);
}
