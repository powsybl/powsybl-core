/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.extensions.CgmesDanglingLineBoundaryNodeAdder;
import com.powsybl.cgmes.model.*;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;
import com.powsybl.iidm.network.util.SV;
import com.powsybl.iidm.network.util.TieLineUtil;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 *         <p>
 *         A ConductingEquipment has at least one Terminal. From the Terminal we
 *         get either its ConnectivityNode or its TopologicalNode, depending on
 *         the conversion context
 */
public abstract class AbstractConductingEquipmentConversion extends AbstractIdentifiedObjectConversion {

    private static final String CONNECTED = "connected";
    private static final PropertyBag EMPTY_PROPERTY_BAG = new PropertyBag(Collections.emptyList(), false);

    protected AbstractConductingEquipmentConversion(
            String type,
            PropertyBag p,
            Context context) {
        super(type, p, context);
        numTerminals = 1;
        terminals = new TerminalData[]{null, null, null};
        terminals[0] = new TerminalData(CgmesNames.TERMINAL, p, context);
        steadyStatePowerFlow = new PowerFlow(p, "p", "q");

    }

    protected AbstractConductingEquipmentConversion(
            String type,
            PropertyBag p,
            Context context,
            int numTerminals) {
        super(type, p, context);
        // Information about each terminal is in properties of the unique property bag
        if (numTerminals > 3) {
            throw new IllegalArgumentException("Invalid number of terminals at " + id + ": " + numTerminals);
        }
        terminals = new TerminalData[]{null, null, null};
        this.numTerminals = numTerminals;
        for (int k = 1; k <= numTerminals; k++) {
            int k0 = k - 1;
            terminals[k0] = new TerminalData(CgmesNames.TERMINAL + k, p, context);
        }
        steadyStatePowerFlow = PowerFlow.UNDEFINED;
    }

    protected AbstractConductingEquipmentConversion(
            String type,
            PropertyBags ps,
            Context context) {
        super(type, ps, context);
        // Information about each terminal is in each separate property bags
        // It is assumed the property bags are already sorted
        this.numTerminals = ps.size();
        terminals = new TerminalData[]{null, null, null};
        if (numTerminals > 3) {
            throw new IllegalStateException("numTerminals should be less or equal to 3 but is " + numTerminals);
        }
        for (int k = 1; k <= numTerminals; k++) {
            int k0 = k - 1;
            terminals[k0] = new TerminalData(CgmesNames.TERMINAL, ps.get(k0), context);
        }
        steadyStatePowerFlow = PowerFlow.UNDEFINED;
    }

    public String findPairingKey(String boundaryNode) {
        return findPairingKey(context, boundaryNode);
    }

    public static String findPairingKey(Context context, String boundaryNode) {
        return context.boundary().nameAtBoundary(boundaryNode);
    }

    public String boundaryNode() {
        // Only one of the end points can be in the boundary
        if (isBoundary(1)) {
            return nodeId(1);
        } else if (isBoundary(2)) {
            return nodeId(2);
        }
        return null;
    }

    @Override
    public boolean insideBoundary() {
        // A conducting equipment is inside boundary if
        // the nodes of all its terminals are inside boundary
        for (int k = 1; k <= numTerminals; k++) {
            if (!context.boundary().containsNode(nodeId(k))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void convertInsideBoundary() {
        if (context.config().convertBoundary() && valid()) {
            convert();
        }
    }

    @Override
    public boolean valid() {
        for (int k = 1; k <= numTerminals; k++) {
            if (nodeId(k) == null) {
                missing(nodeIdPropertyName() + k);
                return false;
            }
            if (voltageLevel(k).isEmpty()) {
                missing(String.format("VoltageLevel of terminal %d %s (iidm %s)",
                        k,
                        cgmesVoltageLevelId(k),
                        iidmVoltageLevelId(k)));
                return false;
            }
        }
        return true;
    }

    boolean validNodes() {
        for (int k = 1; k <= numTerminals; k++) {
            if (nodeId(k) == null) {
                missing(nodeIdPropertyName() + k);
                return false;
            }
        }
        return true;
    }

    protected String nodeIdPropertyName() {
        return context.nodeBreaker() ? "ConnectivityNode" : "TopologicalNode";
    }

    String terminalId() {
        return terminals[0].t.id();
    }

    String terminalId(int n) {
        return terminals[n - 1].t.id();
    }

    protected String nodeId() {
        return context.nodeBreaker()
                ? terminals[0].t.connectivityNode()
                : terminals[0].t.topologicalNode();
    }

    protected String nodeId(int n) {
        return context.nodeBreaker()
                ? terminals[n - 1].t.connectivityNode()
                : terminals[n - 1].t.topologicalNode();
    }

    protected String topologicalNodeId(int n) {
        return terminals[n - 1].t.topologicalNode();
    }

    protected String connectivityNodeId(int n) {
        return terminals[n - 1].t.connectivityNode();
    }

    protected boolean isBoundary(int n) {
        return voltageLevel(n).isEmpty() || context.boundary().containsNode(nodeId(n));
    }

    public DanglingLine convertToDanglingLine(String eqInstance, int boundarySide, String originalClass) {
        return convertToDanglingLine(eqInstance, boundarySide, 0.0, 0.0, 0.0, 0.0, originalClass);
    }

    public DanglingLine convertToDanglingLine(String eqInstance, int boundarySide, double r, double x, double gch, double bch, String originalClass) {
        // Non-boundary side (other side) of the line
        int modelSide = 3 - boundarySide;
        String boundaryNode = nodeId(boundarySide);

        // check again boundary node is correct
        if (!isBoundary(boundarySide) || isBoundary(modelSide)) {
            throw new PowsyblException(String.format("Unexpected boundarySide and modelSide at boundaryNode: %s", boundaryNode));
        }

        DanglingLineAdder dlAdder = voltageLevel(modelSide).map(vl -> vl.newDanglingLine()
                        .setEnsureIdUnicity(context.config().isEnsureIdAliasUnicity())
                        .setR(r)
                        .setX(x)
                        .setG(gch)
                        .setB(bch)
                        .setPairingKey(findPairingKey(boundaryNode)))
                .orElseThrow(() -> new CgmesModelException("Dangling line " + id + " has no container"));
        identify(dlAdder);
        connectWithOnlyEq(dlAdder, modelSide);
        Optional<EquivalentInjectionConversion> equivalentInjectionConversion = getEquivalentInjectionConversionForDanglingLine(context, boundaryNode, eqInstance);
        DanglingLine dl;
        if (equivalentInjectionConversion.isPresent()) {
            dl = equivalentInjectionConversion.get().convertOverDanglingLine(dlAdder);
            Optional.ofNullable(dl.getGeneration()).ifPresent(equivalentInjectionConversion.get()::convertReactiveLimits);
        } else {
            dl = dlAdder
                    .setP0(Double.NaN)
                    .setQ0(Double.NaN)
                    .add();
        }
        context.terminalMapping().add(terminalId(boundarySide), dl.getBoundary(), 2);
        dl.addAlias(terminalId(boundarySide), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL_BOUNDARY);
        dl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL_BOUNDARY, terminalId(boundarySide)); // TODO: delete when aliases are correctly handled by mergedlines
        dl.addAlias(terminalId(boundarySide == 1 ? 2 : 1), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1);
        dl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal", terminalId(boundarySide == 1 ? 2 : 1)); // TODO: delete when aliases are correctly handled by mergedlines

        Optional.ofNullable(topologicalNodeId(boundarySide)).ifPresent(tn -> {
            if (isTopologicalNodeDefinedAtBoundary(tn, boundarySide)) {
                dl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY, tn);
            }
        });

        Optional.ofNullable(connectivityNodeId(boundarySide)).ifPresent(cn -> dl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.CONNECTIVITY_NODE_BOUNDARY, cn));
        setBoundaryNodeInfo(boundaryNode, dl);
        // In a Dangling Line the CGMES side and the IIDM side may not be the same
        // Dangling lines in IIDM only have one terminal, one side
        context.convertedTerminalWithOnlyEq(terminalId(modelSide), dl.getTerminal(), 1);

        dl.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, originalClass);
        return dl;
    }

    // A TopologicalNode is considered an official boundary node when it is defined in the TP_BD file.
    // We also treat a TopologicalNode as official when no boundary information is available,
    // this occurs when reading bus-branch models that lack both EQ_BD and TP_BD files.
    // In both cases, the TopologicalNode must be recorded for use during the export process.
    private boolean isTopologicalNodeDefinedAtBoundary(String topologicalNode, int boundarySide) {
        return context.boundary().containsNode(topologicalNode) || connectivityNodeId(boundarySide) == null;
    }

    protected static void updateDanglingLine(DanglingLine danglingLine, boolean isBoundaryTerminalConnected, Context context) {
        updateTerminals(danglingLine, context, danglingLine.getTerminal());
        updateTargetsAndRegulationAndOperationalLimits(danglingLine, isBoundaryTerminalConnected, context);
        computeFlowsOnModelSide(danglingLine, context);
    }

    // There should be some equipment at boundarySide to model exchange through that point
    // But we have observed, for the test case conformity/miniBusBranch, that the ACLineSegment:
    // _5150a037-e241-421f-98b2-fe60e5c90303 XQ1-N1
    // ends in a boundary node where there is no other line,
    // does not have energy consumer or equivalent injection
    private static void updateTargetsAndRegulationAndOperationalLimits(DanglingLine danglingLine, boolean isConnectedOnBoundarySide, Context context) {
        EquivalentInjectionConversion.update(danglingLine, isConnectedOnBoundarySide, context);
        danglingLine.getOperationalLimitsGroups().forEach(operationalLimitsGroup -> OperationalLimitConversion.update(operationalLimitsGroup, context));
    }

    public static boolean isBoundaryTerminalConnected(DanglingLine danglingLine, Context context) {
        return getBoundaryCgmesTerminal(danglingLine, context).map(cgmesTerminalData -> cgmesTerminalData.asBoolean(CONNECTED, true)).orElse(true);
    }

    private static Optional<PropertyBag> getBoundaryCgmesTerminal(DanglingLine danglingLine, Context context) {
        String cgmesTerminalId = danglingLine.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL_BOUNDARY).orElse(null);
        return cgmesTerminalId != null ? Optional.ofNullable(context.cgmesTerminal(cgmesTerminalId)) : Optional.empty();
    }

    // In a Dangling Line the CGMES side and the IIDM side may not be the same
    // Dangling lines in IIDM only have one terminal, one side
    // We do not have SSH values at the model side, it is a line flow. We take directly SV values

    protected static void computeFlowsOnModelSide(DanglingLine danglingLine, Context context) {
        if (context.config().computeFlowsAtBoundaryDanglingLines()
                && danglingLine.getTerminal().isConnected()
                && !isFlowOnModelSideDefined(danglingLine)) {

            if (isZ0(danglingLine)) {
                // Flow out must be equal to the consumption seen at boundary
                Optional<DanglingLine.Generation> generation = Optional.ofNullable(danglingLine.getGeneration());
                danglingLine.getTerminal().setP(danglingLine.getP0() - generation.map(DanglingLine.Generation::getTargetP).orElse(0.0));
                danglingLine.getTerminal().setQ(danglingLine.getQ0() - generation.map(DanglingLine.Generation::getTargetQ).orElse(0.0));
            } else {
                setDanglingLineModelSideFlow(danglingLine, context);
            }
        }
    }

    private static boolean isFlowOnModelSideDefined(DanglingLine danglingLine) {
        return Double.isFinite(danglingLine.getTerminal().getP()) && Double.isFinite(danglingLine.getTerminal().getQ());
    }

    public static void calculateVoltageAndAngleInBoundaryBus(DanglingLine dl) {
        double v = dl.getBoundary().getV();
        double angle = dl.getBoundary().getAngle();

        if (isVoltageDefined(v, angle)) {
            setVoltageProperties(dl, v, angle);
        }
    }

    public static void calculateVoltageAndAngleInBoundaryBus(DanglingLine dl1, DanglingLine dl2) {
        double v = TieLineUtil.getBoundaryV(dl1, dl2);
        double angle = TieLineUtil.getBoundaryAngle(dl1, dl2);

        if (!isVoltageDefined(v, angle)) {
            v = dl1.getBoundary().getV();
            angle = dl1.getBoundary().getAngle();
        }
        if (!isVoltageDefined(v, angle)) {
            v = dl2.getBoundary().getV();
            angle = dl2.getBoundary().getAngle();
        }
        if (isVoltageDefined(v, angle)) {
            setVoltageProperties(dl1, v, angle);
            setVoltageProperties(dl2, v, angle);
        }
    }

    private static boolean isVoltageDefined(double v, double angle) {
        return !Double.isNaN(v) && !Double.isNaN(angle);
    }

    private static void setVoltageProperties(DanglingLine dl, double v, double angle) {
        dl.setProperty("v", Double.toString(v));
        dl.setProperty("angle", Double.toString(angle));
    }

    private void setBoundaryNodeInfo(String boundaryNode, DanglingLine dl) {
        if (context.boundary().isHvdc(boundaryNode) || context.boundary().lineAtBoundary(boundaryNode) != null) {
            dl.newExtension(CgmesDanglingLineBoundaryNodeAdder.class)
                    .setHvdc(context.boundary().isHvdc(boundaryNode))
                    .setLineEnergyIdentificationCodeEic(context.boundary().lineAtBoundary(boundaryNode))
                    .add();

            // TODO: when merged extensions will be handled, this code can be deleted
            if (context.boundary().isHvdc(boundaryNode)) {
                dl.setProperty("isHvdc", "true");
            }
            if (context.boundary().lineAtBoundary(boundaryNode) != null) {
                dl.setProperty("lineEnergyIdentificationCodeEIC", context.boundary().lineAtBoundary(boundaryNode));
            }
        }
    }

    private static boolean isZ0(DanglingLine dl) {
        return dl.getR() == 0.0 && dl.getX() == 0.0 && dl.getG() == 0.0 && dl.getB() == 0.0;
    }

    private static void setDanglingLineModelSideFlow(DanglingLine dl, Context context) {
        Optional<PropertyBag> svVoltage = getCgmesSvVoltageOnBoundarySide(dl, context);
        if (svVoltage.isEmpty()) {
            return;
        }
        double v = svVoltage.get().asDouble(CgmesNames.VOLTAGE, Double.NaN);
        double angle = svVoltage.get().asDouble(CgmesNames.ANGLE, Double.NaN);
        if (!isVoltageDefined(v, angle)) {
            return;
        }
        // The net sum of power flow "entering" at boundary is "exiting"
        // through the line, we have to change the sign of the sum of flows
        // at the node when we consider flow at line end
        Optional<DanglingLine.Generation> generation = Optional.ofNullable(dl.getGeneration());
        double p = dl.getP0() - generation.map(DanglingLine.Generation::getTargetP).orElse(0.0);
        double q = dl.getQ0() - generation.map(DanglingLine.Generation::getTargetQ).orElse(0.0);
        SV svboundary = new SV(-p, -q, v, angle, TwoSides.ONE);
        // The other side power flow must be computed taking into account
        // the same criteria used for ACLineSegment: total shunt admittance
        // is divided in 2 equal shunt admittance at each side of series impedance
        double g = dl.getG() / 2;
        double b = dl.getB() / 2;
        SV svmodel = svboundary.otherSide(dl.getR(), dl.getX(), g, b, g, b, 1.0, 0.0);
        dl.getTerminal().setP(svmodel.getP());
        dl.getTerminal().setQ(svmodel.getQ());
    }

    private static Optional<PropertyBag> getCgmesSvVoltageOnBoundarySide(DanglingLine danglingLine, Context context) {
        String topologicalNodeIdOnBoundarySide = getTopologicalNodeIdOnBoundarySide(danglingLine, context);
        if (topologicalNodeIdOnBoundarySide != null) {
            return Optional.ofNullable(context.svVoltage(topologicalNodeIdOnBoundarySide));
        }
        return Optional.empty();
    }

    private static String getTopologicalNodeIdOnBoundarySide(DanglingLine danglingLine, Context context) {
        String topologicalNodeIdOnBoundarySide = danglingLine.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY);
        if (topologicalNodeIdOnBoundarySide != null) {
            return topologicalNodeIdOnBoundarySide;
        }
        String terminalIdOnBoundarySide = danglingLine.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL_BOUNDARY);
        if (terminalIdOnBoundarySide != null) {
            PropertyBag cgmesTerminal = context.cgmesTerminal(terminalIdOnBoundarySide);
            if (cgmesTerminal != null) {
                return cgmesTerminal.getId(CgmesNames.TOPOLOGICAL_NODE);
            }
        }
        return null;
    }

    private static Optional<EquivalentInjectionConversion> getEquivalentInjectionConversionForDanglingLine(Context context, String boundaryNode, String eqInstance) {
        List<PropertyBag> eis = context.boundary().equivalentInjectionsAtNode(boundaryNode);
        if (eis.isEmpty()) {
            return Optional.empty();
        } else if (eis.size() == 1) {
            return Optional.of(new EquivalentInjectionConversion(eis.get(0), context));
        } else {
            // Select the EI that is defined in the same EQ instance of the given line
            String eqInstancePropertyName = "graph";
            List<PropertyBag> eisEqInstance = eis.stream().filter(eik -> eik.getId(eqInstancePropertyName).equals(eqInstance)).toList();

            if (eisEqInstance.size() == 1) {
                return Optional.of(new EquivalentInjectionConversion(eisEqInstance.get(0), context));
            } else {
                context.invalid("Boundary node " + boundaryNode,
                        "Assembled model does not contain only one equivalent injection in the same graph " + eqInstance);
                return Optional.empty();
            }
        }
    }

    int iidmNode() {
        return iidmNode(1, true);
    }

    int iidmNode(int n) {
        return iidmNode(n, true);
    }

    int iidmNode(int n, boolean equipmentIsConnected) {
        if (!context.nodeBreaker()) {
            throw new ConversionException("Can't request an iidmNode if conversion context is not node-breaker");
        }
        VoltageLevel vl = terminals[n - 1].voltageLevel;
        CgmesTerminal t = terminals[n - 1].t;
        return context.nodeMapping().iidmNodeForTerminal(t, type.equals("Switch"), vl, equipmentIsConnected);
    }

    String busId() {
        return terminals[0].busId;
    }

    String busId(int n) {
        return terminals[n - 1].busId;
    }

    boolean terminalConnected() {
        return terminals[0].t.connected();
    }

    boolean terminalConnected(int n) {
        return terminals[n - 1].t.connected();
    }

    String cgmesVoltageLevelId(int n) {
        return terminals[n - 1].cgmesVoltageLevelId;
    }

    String iidmVoltageLevelId(int n) {
        return terminals[n - 1].iidmVoltageLevelId;
    }

    protected VoltageLevel voltageLevel() {
        if (terminals[0].iidmVoltageLevelId != null) {
            VoltageLevel vl = context.network().getVoltageLevel(terminals[0].iidmVoltageLevelId);
            if (vl != null) {
                return vl;
            } else {
                throw new CgmesModelException(type + " " + id + " voltage level " + terminals[0].iidmVoltageLevelId + " has not been created in IIDM");
            }
        } else if (terminals[0].voltageLevel != null) {
            return terminals[0].voltageLevel;
        }
        throw new CgmesModelException(type + " " + id + " has no container");
    }

    Optional<VoltageLevel> voltageLevel(int n) {
        if (terminals[n - 1].iidmVoltageLevelId != null) {
            return Optional.ofNullable(context.network().getVoltageLevel(terminals[n - 1].iidmVoltageLevelId));
        } else if (terminals[n - 1].voltageLevel != null) {
            return Optional.of(terminals[n - 1].voltageLevel);
        }
        return Optional.empty();
    }

    protected Optional<Substation> substation() {
        return (terminals[0].voltageLevel != null) ? terminals[0].voltageLevel.getSubstation() : Optional.empty();
    }

    private PowerFlow stateVariablesPowerFlow() {
        return terminals[0].t.flow();
    }

    public PowerFlow stateVariablesPowerFlow(int n) {
        return terminals[n - 1].t.flow();
    }

    private PowerFlow steadyStateHypothesisPowerFlow() {
        return steadyStatePowerFlow;
    }

    PowerFlow powerFlow() {
        if (steadyStateHypothesisPowerFlow().defined()) {
            return steadyStateHypothesisPowerFlow();
        }
        if (stateVariablesPowerFlow().defined()) {
            return stateVariablesPowerFlow();
        }
        return PowerFlow.UNDEFINED;
    }

    PowerFlow powerFlowSV(int n) {
        if (stateVariablesPowerFlow(n).defined()) {
            return stateVariablesPowerFlow(n);
        }
        return PowerFlow.UNDEFINED;
    }

    // Terminals

    protected void convertedTerminals(Terminal... ts) {
        if (ts.length != numTerminals) {
            throw new IllegalArgumentException();
        }
        for (int k = 0; k < ts.length; k++) {
            int n = k + 1;
            Terminal t = ts[k];
            context.convertedTerminal(terminalId(n), t, n, powerFlowSV(n));
        }
    }

    protected void convertedTerminalsWithOnlyEq(Terminal... ts) {
        if (ts.length != numTerminals) {
            throw new IllegalArgumentException();
        }
        for (int k = 0; k < ts.length; k++) {
            int n = k + 1;
            Terminal t = ts[k];
            context.convertedTerminalWithOnlyEq(terminalId(n), t, n);
        }
    }

    public static void updateTerminals(Connectable<?> connectable, Context context, Terminal... ts) {
        PropertyBags cgmesTerminals = getCgmesTerminals(connectable, context, ts.length);
        for (int k = 0; k < ts.length; k++) {
            updateTerminal(cgmesTerminals.get(k), ts[k], context);
        }
    }

    private static void updateTerminal(PropertyBag cgmesTerminal, Terminal terminal, Context context) {
        if (updateConnect(terminal, context)) {
            boolean connectedInUpdate = cgmesTerminal.asBoolean(CONNECTED, true);
            if (!terminal.isConnected() && connectedInUpdate) {
                terminal.connect();
            } else if (terminal.isConnected() && !connectedInUpdate) {
                terminal.disconnect();
            }
        }
        if (setPQAllowed(terminal)) {
            PowerFlow f = new PowerFlow(cgmesTerminal, "p", "q");
            if (f.defined()) {
                terminal.setP(f.p());
                terminal.setQ(f.q());
            }
        }
    }

    private static boolean updateConnect(Terminal terminal, Context context) {
        if (terminal.getVoltageLevel().getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            return context.config().updateTerminalConnectionInNodeBreakerVoltageLevel();
        } else {
            return true;
        }
    }

    private static boolean setPQAllowed(Terminal t) {
        return t.getConnectable().getType() != IdentifiableType.BUSBAR_SECTION;
    }

    private static PropertyBag getCgmesTerminal(Connectable<?> connectable, Context context) {
        return getCgmesTerminals(connectable, context, 1).get(0);
    }

    private static PropertyBags getCgmesTerminals(Connectable<?> connectable, Context context, int numTerminals) {
        PropertyBags propertyBags = new PropertyBags();
        getTerminalTags(numTerminals).forEach(terminalTag -> propertyBags.add(
                connectable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + terminalTag)
                        .map(cgmesTerminalId -> getCgmesTerminal(cgmesTerminalId, context))
                        .orElse(EMPTY_PROPERTY_BAG)));
        return propertyBags;

    }

    private static List<String> getTerminalTags(int numTerminals) {
        return switch (numTerminals) {
            case 1 -> List.of(CgmesNames.TERMINAL1);
            case 2 -> List.of(CgmesNames.TERMINAL1, CgmesNames.TERMINAL2);
            case 3 -> List.of(CgmesNames.TERMINAL1, CgmesNames.TERMINAL2, CgmesNames.TERMINAL3);
            default -> throw new PowsyblException("unexpected number of terminals " + numTerminals);
        };
    }

    // If the propertyBag is not received, an empty one is returned.
    private static PropertyBag getCgmesTerminal(String cgmesTerminalId, Context context) {
        return context.cgmesTerminal(cgmesTerminalId) != null
                ? context.cgmesTerminal(cgmesTerminalId)
                : EMPTY_PROPERTY_BAG;
    }

    private final int numTerminals;

    static class TerminalData {
        private final CgmesTerminal t;
        private final String busId;
        private final String cgmesVoltageLevelId;
        private final String iidmVoltageLevelId;
        private final VoltageLevel voltageLevel;

        TerminalData(String terminalPropertyName, PropertyBag p, Context context) {
            t = context.cgmes().terminal(p.getId(terminalPropertyName));
            String nodeId = context.nodeBreaker() ? t.connectivityNode() : t.topologicalNode();
            this.busId = context.namingStrategy().getIidmId("Bus", nodeId);
            if (context.config().convertBoundary()
                && context.boundary().containsNode(nodeId)) {
                cgmesVoltageLevelId = Context.boundaryVoltageLevelId(nodeId);
            } else {
                // If the terminal's node is contained in a Line (happens in boundaries) or in a Substation, a fictitious VoltageLevel is created
                cgmesVoltageLevelId = findCgmesVoltageLevelIdForContainer(nodeId, context);
            }
            if (cgmesVoltageLevelId != null) {
                String iidmVl = context.namingStrategy().getIidmId("VoltageLevel", cgmesVoltageLevelId);
                iidmVoltageLevelId = context.nodeContainerMapping().voltageLevelIidm(iidmVl);
                voltageLevel = context.network().getVoltageLevel(iidmVoltageLevelId);
            } else {
                iidmVoltageLevelId = null;
                voltageLevel = null;
            }
        }

        // if nodeId is included in a Line Container, the fictitious voltage level must be considered
        private static String findCgmesVoltageLevelIdForContainer(String nodeId, Context context) {
            String cgmesVoltageLevelId = null;
            Optional<CgmesContainer> cgmesContainer = context.cgmes().nodeContainer(nodeId);
            if (cgmesContainer.isPresent()) {
                cgmesVoltageLevelId = cgmesContainer.get().voltageLevel();
                if (cgmesVoltageLevelId == null) {
                    cgmesVoltageLevelId = context.nodeContainerMapping().getFictitiousVoltageLevelForContainer(cgmesContainer.get().id(), nodeId);
                }
            }
            return cgmesVoltageLevelId;
        }
    }

    // Connect

    public void connect(InjectionAdder<?, ?> adder) {
        if (context.nodeBreaker()) {
            adder.setNode(iidmNode());
        } else {
            adder.setBus(terminalConnected() ? busId() : null).setConnectableBus(busId());
        }
    }

    public void connectWithOnlyEq(InjectionAdder<?, ?> adder) {
        if (context.nodeBreaker()) {
            adder.setNode(iidmNode());
        } else {
            adder.setBus(null).setConnectableBus(busId());
        }
    }

    public void connectWithOnlyEq(InjectionAdder<?, ?> adder, int terminal) {
        if (context.nodeBreaker()) {
            adder.setNode(iidmNode(terminal));
        } else {
            adder.setBus(null).setConnectableBus(busId(terminal));
        }
    }

    public void connectWithOnlyEq(BranchAdder<?, ?> adder) {
        if (context.nodeBreaker()) {
            adder
                    .setVoltageLevel1(iidmVoltageLevelId(1))
                    .setVoltageLevel2(iidmVoltageLevelId(2))
                    .setNode1(iidmNode(1))
                    .setNode2(iidmNode(2));
        } else {
            String busId1 = busId(1);
            String busId2 = busId(2);
            adder
                    .setVoltageLevel1(iidmVoltageLevelId(1))
                    .setVoltageLevel2(iidmVoltageLevelId(2))
                    .setBus1(null)
                    .setBus2(null)
                    .setConnectableBus1(busId1)
                    .setConnectableBus2(busId2);
        }
    }

    public void connect(VoltageLevel.NodeBreakerView.SwitchAdder adder, boolean open) {
        if (!context.nodeBreaker()) {
            throw new ConversionException("Not in node breaker context");
        }
        adder.setNode1(iidmNode(1)).setNode2(iidmNode(2)).setOpen(open);
    }

    public void connect(VoltageLevel.BusBreakerView.SwitchAdder adder, boolean open) {
        adder
            .setBus1(busId(1))
            .setBus2(busId(2))
            .setOpen(open || !terminalConnected(1) || !terminalConnected(2));
    }

    public void connectWithOnlyEq(LegAdder adder, int terminal) {
        if (context.nodeBreaker()) {
            adder
                .setVoltageLevel(iidmVoltageLevelId(terminal))
                .setNode(iidmNode(terminal));
        } else {
            adder
                .setVoltageLevel(iidmVoltageLevelId(terminal))
                .setBus(null)
                .setConnectableBus(busId(terminal));
        }
    }

    protected void addAliasesAndProperties(Identifiable<?> identifiable) {
        int i = 1;
        for (TerminalData td : terminals) {
            if (td == null) {
                break;
            }
            identifiable.addAlias(td.t.id(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + i, context.config().isEnsureIdAliasUnicity());
            i++;
        }
    }

    protected double p0() {
        return powerFlow().defined() ? powerFlow().p() : 0.0;
    }

    protected double q0() {
        return powerFlow().defined() ? powerFlow().q() : 0.0;
    }

    protected static PowerFlow updatedPowerFlow(Connectable<?> connectable, PropertyBag cgmesData, Context context) {
        PowerFlow steadyStateHypothesisPowerFlow = new PowerFlow(cgmesData, "p", "q");
        if (steadyStateHypothesisPowerFlow.defined()) {
            return steadyStateHypothesisPowerFlow;
        }
        PropertyBag cgmesTerminal = getCgmesTerminal(connectable, context);
        PowerFlow stateVariablesPowerFlow = new PowerFlow(cgmesTerminal, "p", "q");
        if (stateVariablesPowerFlow.defined()) {
            return stateVariablesPowerFlow;
        }
        return PowerFlow.UNDEFINED;
    }

    protected static Optional<PropertyBag> findCgmesRegulatingControl(Connectable<?> connectable, Context context) {
        String regulatingControlId = connectable.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.REGULATING_CONTROL);
        return regulatingControlId != null ? Optional.ofNullable(context.regulatingControl(regulatingControlId)) : Optional.empty();
    }

    protected static int findTerminalSign(Connectable<?> connectable) {
        return findTerminalSign(connectable, "");
    }

    protected static int findTerminalSign(Connectable<?> connectable, String end) {
        String terminalSign = connectable.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL_SIGN + end);
        return terminalSign != null ? Integer.parseInt(terminalSign) : 1;
    }

    protected static double findTargetV(PropertyBag regulatingControl, double defaultValue, DefaultValueUse use) {
        return findTargetV(regulatingControl, CgmesNames.TARGET_VALUE, defaultValue, use);
    }

    protected static double findTargetV(PropertyBag regulatingControl, String propertyTag, double defaultValue, DefaultValueUse use) {
        double targetV = regulatingControl.asDouble(propertyTag);
        return useDefaultValue(regulatingControl.containsKey(propertyTag), isValidTargetV(targetV), use) ? defaultValue : targetV;
    }

    protected static double findTargetQ(PropertyBag regulatingControl, int terminalSign, double defaultValue, DefaultValueUse use) {
        return findTargetValue(regulatingControl, terminalSign, defaultValue, use);
    }

    protected static double findTargetQ(PropertyBag regulatingControl, String propertyTag, int terminalSign, double defaultValue, DefaultValueUse use) {
        return findTargetValue(regulatingControl, propertyTag, terminalSign, defaultValue, use);
    }

    protected static double findTargetValue(PropertyBag regulatingControl, int terminalSign, double defaultValue, DefaultValueUse use) {
        return findTargetValue(regulatingControl, CgmesNames.TARGET_VALUE, terminalSign, defaultValue, use);
    }

    protected static double findTargetValue(PropertyBag regulatingControl, String propertyTag, int terminalSign, double defaultValue, DefaultValueUse use) {
        double targetValue = regulatingControl.asDouble(propertyTag);
        return useDefaultValue(regulatingControl.containsKey(propertyTag), isValidTargetValue(targetValue), use) ? defaultValue : targetValue * terminalSign;
    }

    protected static double findTargetDeadband(PropertyBag regulatingControl, double defaultValue, DefaultValueUse use) {
        double targetDeadband = regulatingControl.asDouble(CgmesNames.TARGET_DEADBAND);
        return useDefaultValue(regulatingControl.containsKey(CgmesNames.TARGET_DEADBAND), isValidTargetDeadband(targetDeadband), use) ? defaultValue : targetDeadband;
    }

    protected static boolean findRegulatingOn(PropertyBag regulatingControl, boolean defaultValue, DefaultValueUse use) {
        return findRegulatingOn(regulatingControl, CgmesNames.ENABLED, defaultValue, use);
    }

    protected static boolean findRegulatingOn(PropertyBag regulatingControl, String propertyTag, boolean defaultValue, DefaultValueUse use) {
        Optional<Boolean> isRegulatingOn = regulatingControl.asBoolean(propertyTag);
        return useDefaultValue(isRegulatingOn.isPresent(), true, use) ? defaultValue : isRegulatingOn.orElse(false);
    }

    private static boolean useDefaultValue(boolean isDefined, boolean isValid, DefaultValueUse use) {
        return use == DefaultValueUse.ALWAYS
                || use == DefaultValueUse.NOT_DEFINED && !isDefined
                || use == DefaultValueUse.NOT_VALID && !isValid;
    }

    protected static boolean isValidTargetV(double targetV) {
        return targetV > 0.0;
    }

    protected static boolean isValidTargetQ(double targetQ) {
        return isValidTargetValue(targetQ);
    }

    protected static boolean isValidTargetValue(double targetValue) {
        return Double.isFinite(targetValue);
    }

    protected static boolean isValidTargetDeadband(double targetDeadband) {
        return targetDeadband >= 0.0;
    }

    /**
     * Specifies when to use the default value.
     * <br/>
     * The available options are:
     * <ul>
     *   <li><b>NEVER</b>: The default value is never used.</li>
     *   <li><b>NOT_DEFINED</b>: The default value is used only when the property is not defined.</li>
     *   <li><b>NOT_VALID</b>: The default value is used when the property is defined but contains an invalid value.</li>
     *   <li><b>ALWAYS</b>: The default value is always used, regardless of the property's state.</li>
     * </ul>
     */
    protected enum DefaultValueUse {
        NEVER,
        NOT_DEFINED,
        NOT_VALID,
        ALWAYS
    }

    private final TerminalData[] terminals;
    private final PowerFlow steadyStatePowerFlow;
}
