/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.extensions.CgmesDanglingLineBoundaryNodeAdder;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;
import com.powsybl.iidm.network.util.SV;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.List;
import java.util.Optional;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 *         <p>
 *         A ConductingEquipment has at least one Terminal. From the Terminal we
 *         get either its ConnectivityNode or its TopologicalNode, depending of
 *         the conversion context
 */
public abstract class AbstractConductingEquipmentConversion extends AbstractIdentifiedObjectConversion {

    protected AbstractConductingEquipmentConversion(
        String type,
        PropertyBag p,
        Context context) {
        super(type, p, context);
        numTerminals = 1;
        terminals = new TerminalData[] {null, null, null};
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
        terminals = new TerminalData[] {null, null, null};
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
        terminals = new TerminalData[] {null, null, null};
        if (numTerminals > 3) {
            throw new IllegalStateException("numTerminals should be less or equal to 3 but is " + numTerminals);
        }
        for (int k = 1; k <= numTerminals; k++) {
            int k0 = k - 1;
            terminals[k0] = new TerminalData(CgmesNames.TERMINAL, ps.get(k0), context);
        }
        steadyStatePowerFlow = PowerFlow.UNDEFINED;
    }

    public String findUcteXnodeCode(String boundaryNode) {
        return findUcteXnodeCode(context, boundaryNode);
    }

    public static String findUcteXnodeCode(Context context, String boundaryNode) {
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
        if (context.config().convertBoundary()) {
            if (valid()) {
                convert();
            }
        } else {
            // The default action for a conducting equipment totally inside the boundary
            // is to accumulate the power flows of connected terminals at boundary node
            for (int k = 1; k <= numTerminals; k++) {
                if (terminalConnected(k)) {
                    context.boundary().addPowerFlowAtNode(nodeId(k), powerFlowSV(k));
                }
            }
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

    public void convertToDanglingLine(int boundarySide) {
        convertToDanglingLine(boundarySide, 0.0, 0.0, 0.0, 0.0);
    }

    public void convertToDanglingLine(int boundarySide, double r, double x, double gch, double bch) {
        // Non-boundary side (other side) of the line
        int modelSide = 3 - boundarySide;
        String boundaryNode = nodeId(boundarySide);

        // check again boundary node is correct
        if (!isBoundary(boundarySide) || isBoundary(modelSide)) {
            throw new PowsyblException(String.format("Unexpected boundarySide and modelSide at boundaryNode: %s", boundaryNode));
        }

        PowerFlow f = new PowerFlow(0, 0);
        // Only consider potential power flow at boundary side if that side is connected
        if (terminalConnected(boundarySide) && context.boundary().hasPowerFlow(boundaryNode)) {
            f = context.boundary().powerFlowAtNode(boundaryNode);
        }
        // There should be some equipment at boundarySide to model exchange through that
        // point
        // But we have observed, for the test case conformity/miniBusBranch,
        // that the ACLineSegment:
        // _5150a037-e241-421f-98b2-fe60e5c90303 XQ1-N1
        // ends in a boundary node where there is no other line,
        // does not have energy consumer or equivalent injection
        if (terminalConnected(boundarySide)
            && !context.boundary().hasPowerFlow(boundaryNode)
            && context.boundary().equivalentInjectionsAtNode(boundaryNode).isEmpty()) {
            missing("Equipment for modeling consumption/injection at boundary node");
        }

        DanglingLineAdder dlAdder = voltageLevel(modelSide).map(vl -> vl.newDanglingLine()
                        .setEnsureIdUnicity(context.config().isEnsureIdAliasUnicity())
                        .setR(r)
                        .setX(x)
                        .setG(gch)
                        .setB(bch)
                        .setUcteXnodeCode(findUcteXnodeCode(boundaryNode)))
                .orElseThrow(() -> new CgmesModelException("Dangling line " + id + " has no container"));
        identify(dlAdder);
        connect(dlAdder, modelSide);
        EquivalentInjectionConversion equivalentInjectionConversion = getEquivalentInjectionConversionForDanglingLine(
            boundaryNode);
        DanglingLine dl;
        if (equivalentInjectionConversion != null) {
            dl = equivalentInjectionConversion.convertOverDanglingLine(dlAdder, f);
            Optional.ofNullable(dl.getGeneration()).ifPresent(equivalentInjectionConversion::convertReactiveLimits);
        } else {
            dl = dlAdder
                    .setP0(f.p())
                    .setQ0(f.q())
                    .add();
        }
        context.terminalMapping().add(terminalId(boundarySide), dl.getBoundary(), 2);
        dl.addAlias(terminalId(boundarySide), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary");
        dl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary", terminalId(boundarySide)); // TODO: delete when aliases are correctly handled by mergedlines
        dl.addAlias(terminalId(boundarySide == 1 ? 2 : 1), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal");
        dl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal", terminalId(boundarySide == 1 ? 2 : 1)); // TODO: delete when aliases are correctly handled by mergedlines
        Optional.ofNullable(topologicalNodeId(boundarySide)).ifPresent(tn -> dl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY, tn));
        Optional.ofNullable(connectivityNodeId(boundarySide)).ifPresent(cn ->
            dl.addAlias(cn, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.CONNECTIVITY_NODE_BOUNDARY)
        );
        context.namingStrategy().readIdMapping(dl, type);
        setBoundaryNodeInfo(boundaryNode, dl);
        // In a Dangling Line the CGMES side and the IIDM side may not be the same
        // Dangling lines in IIDM only have one terminal, one side
        // We do not have SSH values at the model side, it is a line flow. We take directly SV values
        context.convertedTerminal(terminalId(modelSide), dl.getTerminal(), 1, powerFlowSV(modelSide));

        // If we do not have power flow at model side and we can compute it,
        // do it and assign the result at the terminal of the dangling line
        if (context.config().computeFlowsAtBoundaryDanglingLines()
            && terminalConnected(modelSide)
            && !powerFlowSV(modelSide).defined()
            && context.boundary().hasVoltage(boundaryNode)) {

            if (isZ0(dl)) {
                // Flow out must be equal to the consumption seen at boundary
                Optional<DanglingLine.Generation> generation = Optional.ofNullable(dl.getGeneration());
                dl.getTerminal().setP(dl.getP0() - generation.map(DanglingLine.Generation::getTargetP).orElse(0.0));
                dl.getTerminal().setQ(dl.getQ0() - generation.map(DanglingLine.Generation::getTargetQ).orElse(0.0));

            } else {
                setDanglingLineModelSideFlow(dl, boundaryNode);
            }
        }
    }

    public static void calculateVoltageAndAngleInBoundaryBus(DanglingLine dl) {
        double v = dl.getBoundary().getV();
        double angle = dl.getBoundary().getAngle();

        if (!Double.isNaN(v) && !Double.isNaN(angle)) {
            dl.setProperty("v", Double.toString(v));
            dl.setProperty("angle", Double.toString(angle));
        }
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

    private boolean isZ0(DanglingLine dl) {
        return dl.getR() == 0.0 && dl.getX() == 0.0 && dl.getG() == 0.0 && dl.getB() == 0.0;
    }

    private void setDanglingLineModelSideFlow(DanglingLine dl, String boundaryNode) {

        double v = context.boundary().vAtBoundary(boundaryNode);
        double angle = context.boundary().angleAtBoundary(boundaryNode);
        // The net sum of power flow "entering" at boundary is "exiting"
        // through the line, we have to change the sign of the sum of flows
        // at the node when we consider flow at line end
        Optional<DanglingLine.Generation> generation = Optional.ofNullable(dl.getGeneration());
        double p = dl.getP0() - generation.map(DanglingLine.Generation::getTargetP).orElse(0.0);
        double q = dl.getQ0() - generation.map(DanglingLine.Generation::getTargetQ).orElse(0.0);
        SV svboundary = new SV(-p, -q, v, angle, Branch.Side.ONE);
        // The other side power flow must be computed taking into account
        // the same criteria used for ACLineSegment: total shunt admittance
        // is divided in 2 equal shunt admittance at each side of series impedance
        double g = dl.getG() / 2;
        double b = dl.getB() / 2;
        SV svmodel = svboundary.otherSide(dl.getR(), dl.getX(), g, b, g, b, 1.0, 0.0);
        dl.getTerminal().setP(svmodel.getP());
        dl.getTerminal().setQ(svmodel.getQ());
    }

    private EquivalentInjectionConversion getEquivalentInjectionConversionForDanglingLine(String boundaryNode) {
        List<PropertyBag> eis = context.boundary().equivalentInjectionsAtNode(boundaryNode);
        if (eis.isEmpty()) {
            return null;
        } else if (eis.size() > 1) {
            // This should not happen
            // We have decided to create a dangling line,
            // so only one MAS at this boundary point,
            // so there must be only one equivalent injection
            invalid("Multiple equivalent injections at boundary node");
            return null;
        } else {
            return new EquivalentInjectionConversion(eis.get(0), context);
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

    String cgmesVoltageLevelId() {
        return terminals[0].cgmesVoltageLevelId;
    }

    String cgmesVoltageLevelId(int n) {
        return terminals[n - 1].cgmesVoltageLevelId;
    }

    String iidmVoltageLevelId() {
        return terminals[0].iidmVoltageLevelId;
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

    PowerFlow powerFlowSV() {
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
            throw new IllegalStateException();
        }
        for (int k = 0; k < ts.length; k++) {
            int n = k + 1;
            Terminal t = ts[k];
            context.convertedTerminal(terminalId(n), t, n, powerFlowSV(n));
        }
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
                // cgmesVoltageLevelId may be null if terminal is contained in a Line
                // (happens in boundaries)
                cgmesVoltageLevelId = context.cgmes().voltageLevel(t, context.nodeBreaker());
            }
            if (cgmesVoltageLevelId != null) {
                String iidmVl = context.namingStrategy().getIidmId("VoltageLevel", cgmesVoltageLevelId);
                iidmVoltageLevelId = context.substationIdMapping().voltageLevelIidm(iidmVl);
                voltageLevel = context.network().getVoltageLevel(iidmVoltageLevelId);
            } else {
                // if terminal is contained in a Line Container, a fictitious voltage level is created,
                // its ID is composed by its connectivity node ID + '_VL' sufix
                voltageLevel = context.network().getVoltageLevel(nodeId + "_VL");
                if (voltageLevel != null) {
                    iidmVoltageLevelId = t.connectivityNode() + "_VL";
                } else {
                    iidmVoltageLevelId = null;
                }
            }
        }
    }

    // Connections

    public void connect(InjectionAdder<?> adder) {
        if (context.nodeBreaker()) {
            adder.setNode(iidmNode());
        } else {
            adder.setBus(terminalConnected() ? busId() : null).setConnectableBus(busId());
        }
    }

    public void connect(InjectionAdder<?> adder, int terminal) {
        if (context.nodeBreaker()) {
            adder.setNode(iidmNode(terminal));
        } else {
            adder.setBus(terminalConnected(terminal) ? busId(terminal) : null).setConnectableBus(busId(terminal));
        }
    }

    public void connect(BranchAdder<?> adder) {
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
                .setBus1(terminalConnected(1) ? busId1 : null)
                .setBus2(terminalConnected(2) ? busId2 : null)
                .setConnectableBus1(busId1)
                .setConnectableBus2(busId2);
        }
    }

    public void connect(BranchAdder<?> adder,
        String iidmVoltageLevelId1, String busId1, boolean t1Connected, int node1,
        String iidmVoltageLevelId2, String busId2, boolean t2Connected, int node2) {
        connect(context, adder, iidmVoltageLevelId1, busId1, t1Connected, node1, iidmVoltageLevelId2, busId2, t2Connected, node2);
    }

    public static void connect(Context context, BranchAdder<?> adder,
        String iidmVoltageLevelId1, String busId1, boolean t1Connected, int node1,
        String iidmVoltageLevelId2, String busId2, boolean t2Connected, int node2) {
        if (context.nodeBreaker()) {
            adder
                .setVoltageLevel1(iidmVoltageLevelId1)
                .setVoltageLevel2(iidmVoltageLevelId2)
                .setNode1(node1)
                .setNode2(node2);
        } else {
            adder
                .setVoltageLevel1(iidmVoltageLevelId1)
                .setVoltageLevel2(iidmVoltageLevelId2)
                .setBus1(t1Connected ? busId1 : null)
                .setBus2(t2Connected ? busId2 : null)
                .setConnectableBus1(busId1)
                .setConnectableBus2(busId2);
        }
    }

    public void connect(BranchAdder<?> adder, boolean t1Connected, boolean t2Connected) {
        connect(adder, t1Connected, t2Connected, true);
    }

    public void connect(BranchAdder<?> adder, boolean t1Connected, boolean t2Connected, boolean branchIsClosed) {
        if (context.nodeBreaker()) {
            adder
                .setVoltageLevel1(iidmVoltageLevelId(1))
                .setVoltageLevel2(iidmVoltageLevelId(2))
                .setNode1(iidmNode(1, branchIsClosed))
                .setNode2(iidmNode(2, branchIsClosed));
        } else {
            String busId1 = busId(1);
            String busId2 = busId(2);
            adder
                .setVoltageLevel1(iidmVoltageLevelId(1))
                .setVoltageLevel2(iidmVoltageLevelId(2))
                .setBus1(t1Connected && branchIsClosed ? busId1 : null)
                .setBus2(t2Connected && branchIsClosed ? busId2 : null)
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

    public void connect(LegAdder adder, int terminal) {
        if (context.nodeBreaker()) {
            adder
                .setVoltageLevel(iidmVoltageLevelId(terminal))
                .setNode(iidmNode(terminal));
        } else {
            adder
                .setVoltageLevel(iidmVoltageLevelId(terminal))
                .setBus(terminalConnected(terminal) ? busId(terminal) : null)
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
        context.namingStrategy().readIdMapping(identifiable, type);
    }

    protected BoundaryLine createBoundaryLine(String boundaryNode) {
        int modelEnd = 1;
        if (nodeId(1).equals(boundaryNode)) {
            modelEnd = 2;
        }
        String id = iidmId();
        String name = iidmName();
        String modelIidmVoltageLevelId = iidmVoltageLevelId(modelEnd);
        boolean modelTconnected = terminalConnected(modelEnd);
        String modelBus = busId(modelEnd);
        String modelTerminalId = terminalId(modelEnd);
        String boundaryTerminalId = terminalId(modelEnd == 1 ? 2 : 1);
        int modelNode = -1;
        if (context.nodeBreaker()) {
            modelNode = iidmNode(modelEnd);
        }
        PowerFlow modelPowerFlow = powerFlowSV(modelEnd);
        return new BoundaryLine(id, name, modelIidmVoltageLevelId, modelBus, modelTconnected, modelNode,
            modelTerminalId, getBoundarySide(modelEnd), boundaryTerminalId, modelPowerFlow);
    }

    private static Branch.Side getBoundarySide(int modelEnd) {
        if (modelEnd == 1) {
            return Branch.Side.TWO;
        } else {
            return Branch.Side.ONE;
        }
    }

    protected double p0() {
        return powerFlow().defined() ? powerFlow().p() : 0.0;
    }

    protected double q0() {
        return powerFlow().defined() ? powerFlow().q() : 0.0;
    }

    private final TerminalData[] terminals;
    private final PowerFlow steadyStatePowerFlow;
}
