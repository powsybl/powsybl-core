/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.iidm.network.Bus;
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
//        for (Bus b : n.getBusBreakerView().getBuses()) {
//            PropertyBag p = new PropertyBag(SV_VOLTAGE_PROPERTIES);
//            p.put(CgmesNames.ANGLE, fs(b.getAngle()));
//            p.put(CgmesNames.VOLTAGE, fs(b.getV()));
//            p.put("TopologicalNode", topologicalNodeFromBusId(b.getId()));
//            voltages.add(p);
//        }
//        cgmes.add(CgmesSubset.STATE_VARIABLES, "SvVoltage", voltages);

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
            List<String> danglingLines = new ArrayList<>();
            n.getDanglingLineStream().forEach(dLine -> danglingLines.add(dLine.getId()));
            List<String> allBoundaryNodes = boundaryNodes(cgmes);
            List<String> modelRelatedBoundaryNodes = new ArrayList<>();
            for (Map<String, String> sides : acLineNodes(cgmes, danglingLines).values()) {
                if (allBoundaryNodes.contains(sides.get(SIDE1))) {
                    modelRelatedBoundaryNodes.add(sides.get(SIDE1));
                }
                if (allBoundaryNodes.contains(sides.get(SIDE2))) {
                    modelRelatedBoundaryNodes.add(sides.get(SIDE2));
                }
            }

            modelRelatedBoundaryNodes.forEach(node -> {
                PropertyBag p = new PropertyBag(SV_VOLTAGE_PROPERTIES);
                    p.put(CgmesNames.ANGLE, fs(0.0));
                    p.put(CgmesNames.VOLTAGE, fs(0.0));
                    p.put(CgmesNames.TOPOLOGICAL_NODE, node);
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

    private static List<String> boundaryNodes(CgmesModel cgmes) {
        PropertyBags boundaryNodes = cgmes.boundaryNodes();
        List<String> nodesId = new ArrayList<>();
        boundaryNodes.forEach(node -> nodesId.add(node.getId("Node")));
        return Collections.unmodifiableList(nodesId);
    }

    private static Map<String, Map<String, String>> acLineNodes(CgmesModel cgmes, List<String> danglingLines) {
        Map<String, Map<String, String>> acLineNodes = new HashMap<>();
        PropertyBags terminals = cgmes.terminals();
        for (PropertyBag line : cgmes.acLineSegments()) {
            String lineId = line.getId(CgmesNames.AC_LINE_SEGMENT);
            Map<String, String> m = new HashMap<String, String>();
            // we need only acLinesSegments that were converted into DanglingLines
            if (!danglingLines.contains(lineId)) {
                continue;
            }
            for (PropertyBag terminal : terminals) {
//                Map<String, String> m = new HashMap<String, String>();
                String terminalId = terminal.getId(CgmesNames.TERMINAL);
                if (terminalId.equals(line.getId(CgmesNames.TERMINAL1))) {
                    m.put(SIDE1, terminal.getId(CgmesNames.TOPOLOGICAL_NODE));
                    acLineNodes.put(lineId, m);
                } else if (terminalId.equals(line.getId(CgmesNames.TERMINAL2))) {
                    m.put(SIDE2, terminal.getId(CgmesNames.TOPOLOGICAL_NODE));
                    acLineNodes.put(lineId, m);
                } else {
                    continue;
                }
            }
        }
        return acLineNodes;
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
