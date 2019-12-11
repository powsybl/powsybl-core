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
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public final class StateVariablesAdder {

    private StateVariablesAdder() {
    }

    public static void add(Network n, CgmesModel cgmes) {
        // TODO Add full model data with proper profile (StateVariables)
        // FullModel is defined in ModelDescription:
        // http://iec.ch/TC57/61970-552/ModelDescription/1#

        PropertyBags voltages = new PropertyBags();
        // Check for bus branch model
        if (!cgmes.isNodeBreaker()) {
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
            boundaryNodesFromDanglingLines(cgmes, n).entrySet().forEach(entry -> {
                PropertyBag p = new PropertyBag(SV_VOLTAGE_PROPERTIES);
                DanglingLine dl = n.getDanglingLine(entry.getKey());
                Bus b = dl.getTerminal().getBusBreakerView().getBus();
                if (b != null) {
                    // calculate complex voltage value: abs for VOLTAGE, degrees for ANGLE
                    Complex v2 = complexVoltage(dl.getR(), dl.getX(), dl.getG(), dl.getB(), b.getV(), b.getAngle(), dl.getP0(), dl.getQ0());
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
        }

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
                powerFlows.add(createPowerFlowProperties(cgmes, s.getTerminal()));
            }
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
    }

    // FIXME elena Uses LinkData class, not yet in master, I've copied in into the package as LinkDataTmp
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
        "TopologicalNode");
    private static final List<String> SV_POWERFLOW_PROPERTIES = Arrays.asList("p", "q", CgmesNames.TERMINAL);
    private static final List<String> SV_SHUNTCOMPENSATORSECTIONS_PROPERTIES = Arrays.asList("ShuntCompensator",
        "continuousSections");
    public static final String SIDE1 = "side1";
    public static final String SIDE2 = "side2";

    private static final Logger LOG = LoggerFactory.getLogger(StateVariablesAdder.class);
}
