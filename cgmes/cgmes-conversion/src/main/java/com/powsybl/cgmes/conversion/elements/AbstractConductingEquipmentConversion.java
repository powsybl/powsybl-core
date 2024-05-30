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
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;
import com.powsybl.iidm.network.util.TieLineUtil;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.List;
import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
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
        if (context.config().convertBoundary()) {
            if (valid()) {
                convert();
            }
        } else {
            throw new ConversionException("Unexpected equipment inside the boundary: " + this.id);
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

    void updateTerminalData(Identifiable<?> identifiable) {
        // FIXME(Luma) we try to gather terminal data for this equipment ...
        //  do we have to take into account the eventual cgmes naming strategy??
        //Identifiable<?> idable = network.getIdentifiable(id);
        // Some elements may not have been stored in the Network
        // (Equivalent injections at boundaries are not found)
        // FIXME(Luma) Check why they are not added as aliases for the corresponding dangling lines?
        //  is it because more than one dangling line may have the same associated equivalent injection?
        //if (idable == null) {
        //    context.missing(id, () -> "Identifiable not found in Network for update");
        //    return;
        //}
        String terminalId = identifiable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1).orElse(null);
        terminals[0] = TerminalData.createFromTerminalId(terminalId, context);
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

    public DanglingLine convertToDanglingLine(String eqInstance, int boundarySide) {
        return convertToDanglingLine(eqInstance, boundarySide, 0.0, 0.0, 0.0, 0.0);
    }

    public DanglingLine convertToDanglingLine(String eqInstance, int boundarySide, double r, double x, double gch, double bch) {
        // Non-boundary side (other side) of the line
        int modelSide = 3 - boundarySide;
        String boundaryNode = nodeId(boundarySide);

        // check again boundary node is correct
        if (!isBoundary(boundarySide) || isBoundary(modelSide)) {
            throw new PowsyblException(String.format("Unexpected boundarySide and modelSide at boundaryNode: %s", boundaryNode));
        }

        // There should be some equipment at boundarySide to model exchange through that
        // point
        // But we have observed, for the test case conformity/miniBusBranch,
        // that the ACLineSegment:
        // _5150a037-e241-421f-98b2-fe60e5c90303 XQ1-N1
        // ends in a boundary node where there is no other line,
        // does not have energy consumer or equivalent injection
        if (terminalConnected(boundarySide)
            && context.boundary().equivalentInjectionsAtNode(boundaryNode).isEmpty()) {
            missing("Equipment for modeling consumption/injection at boundary node");
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
        connect(dlAdder, modelSide);
        Optional<EquivalentInjectionConversion> equivalentInjectionConversion = getEquivalentInjectionConversionForDanglingLine(context, boundaryNode, eqInstance);
        DanglingLine dl;
        if (equivalentInjectionConversion.isPresent()) {
            dl = equivalentInjectionConversion.get().convertOverDanglingLine(dlAdder);
            Optional.ofNullable(dl.getGeneration()).ifPresent(equivalentInjectionConversion.get()::convertReactiveLimits);
        } else {
            dl = dlAdder
                    .setP0(0.0)
                    .setQ0(0.0)
                    .add();
        }
        context.terminalMapping().add(terminalId(boundarySide), dl.getBoundary(), 2);
        dl.addAlias(terminalId(boundarySide), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL_BOUNDARY);
        dl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL_BOUNDARY, terminalId(boundarySide)); // TODO: delete when aliases are correctly handled by mergedlines
        dl.addAlias(terminalId(boundarySide == 1 ? 2 : 1), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1);
        dl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal", terminalId(boundarySide == 1 ? 2 : 1)); // TODO: delete when aliases are correctly handled by mergedlines
        Optional.ofNullable(topologicalNodeId(boundarySide)).ifPresent(tn -> dl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY, tn));
        Optional.ofNullable(connectivityNodeId(boundarySide)).ifPresent(cn ->
            dl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.CONNECTIVITY_NODE_BOUNDARY, cn)
        );
        setBoundaryNodeInfo(boundaryNode, dl);
        // In a Dangling Line the CGMES side and the IIDM side may not be the same
        // Dangling lines in IIDM only have one terminal, one side
        context.convertedTerminal(terminalId(modelSide), dl.getTerminal(), 1);

        return dl;
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

    private static Optional<EquivalentInjectionConversion> getEquivalentInjectionConversionForDanglingLine(Context context, String boundaryNode, String eqInstance) {
        List<PropertyBag> eis = context.boundary().equivalentInjectionsAtNode(boundaryNode);
        if (eis.isEmpty()) {
            return Optional.empty();
        } else if (eis.size() == 1) {
            return Optional.of(new EquivalentInjectionConversion(eis.get(0), context));
        } else {
            // Select the EI thas is defined in the same EQ instance of the given line
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
        return iidmNode(1);
    }

    int iidmNode(int n) {
        if (!context.nodeBreaker()) {
            throw new ConversionException("Can't request an iidmNode if conversion context is not node-breaker");
        }
        VoltageLevel vl = terminals[n - 1].voltageLevel;
        CgmesTerminal t = terminals[n - 1].t;
        return context.nodeMapping().iidmNodeForTerminal(t, vl);
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

    // Terminals

    protected void convertedTerminals(Terminal... ts) {
        if (ts.length != numTerminals) {
            throw new IllegalStateException();
        }
        for (int k = 0; k < ts.length; k++) {
            int n = k + 1;
            Terminal t = ts[k];
            context.convertedTerminal(terminalId(n), t, n);
        }
    }

    private final int numTerminals;

    static class TerminalData {
        private final CgmesTerminal t;
        private final String busId;
        private final String cgmesVoltageLevelId;
        private final String iidmVoltageLevelId;
        private final VoltageLevel voltageLevel;

        static TerminalData createFromTerminalId(String terminalId, Context context) {
            return new TerminalData(context.cgmes().terminal(terminalId), context);
        }

        TerminalData(String terminalPropertyName, PropertyBag p, Context context) {
            this(context.cgmes().terminal(p.getId(terminalPropertyName)), context);
        }

        private TerminalData(CgmesTerminal t, Context context) {
            this.t = t;

            // FIXME(Luma) the read model may contain incomplete terminal definitions (SSH only has been read)
            if (t == null) {
                busId = null;
                cgmesVoltageLevelId = null;
                iidmVoltageLevelId = null;
                voltageLevel = null;
            } else {
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
                        iidmVoltageLevelId = nodeId + "_VL";
                    } else {
                        iidmVoltageLevelId = null;
                    }
                }
            }
        }
    }

    // Connections

    public void connect(InjectionAdder<?, ?> adder) {
        if (context.nodeBreaker()) {
            adder.setNode(iidmNode());
        } else {
            adder.setBus(terminalConnected() ? busId() : null).setConnectableBus(busId());
        }
    }

    public void connect(InjectionAdder<?, ?> adder, int terminal) {
        if (context.nodeBreaker()) {
            adder.setNode(iidmNode(terminal));
        } else {
            adder.setBus(terminalConnected(terminal) ? busId(terminal) : null).setConnectableBus(busId(terminal));
        }
    }

    public void connect(BranchAdder<?, ?> adder) {
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

    public static void connect(Context context, InjectionAdder<?, ?> adder, String busId, boolean connected, int node) {
        if (context.nodeBreaker()) {
            adder.setNode(node);
        } else {
            adder
                    .setBus(connected ? busId : null)
                    .setConnectableBus(busId);
        }
    }

    public void connect(BranchAdder<?, ?> adder,
        String iidmVoltageLevelId1, String busId1, boolean t1Connected, int node1,
        String iidmVoltageLevelId2, String busId2, boolean t2Connected, int node2) {
        connect(context, adder, iidmVoltageLevelId1, busId1, t1Connected, node1, iidmVoltageLevelId2, busId2, t2Connected, node2);
    }

    public static void connect(Context context, BranchAdder<?, ?> adder,
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

    public void connect(BranchAdder<?, ?> adder, boolean t1Connected, boolean t2Connected) {
        connect(adder, t1Connected, t2Connected, true);
    }

    public void connect(BranchAdder<?, ?> adder, boolean t1Connected, boolean t2Connected, boolean branchIsClosed) {
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
