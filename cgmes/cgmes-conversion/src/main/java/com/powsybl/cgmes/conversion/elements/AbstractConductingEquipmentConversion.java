/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.BranchAdder;
import com.powsybl.iidm.network.InjectionAdder;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 *         <p>
 *         A ConductingEquipment has at least one Terminal. From the Terminal we
 *         get either its ConnectivityNode or its TopologicalNode, depending of
 *         the conversion context
 */
public abstract class AbstractConductingEquipmentConversion extends AbstractIdentifiedObjectConversion {

    public AbstractConductingEquipmentConversion(
        String type,
        PropertyBag p,
        Context context) {
        super(type, p, context);
        numTerminals = 1;
        terminals = new TerminalData[] {null, null, null};
        terminals[0] = new TerminalData(CgmesNames.TERMINAL, p, context);
        steadyStatePowerFlow = new PowerFlow(p, "p", "q");
    }

    public AbstractConductingEquipmentConversion(
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

    public AbstractConductingEquipmentConversion(
        String type,
        PropertyBags ps,
        Context context) {
        super(type, ps, context);
        // Information about each terminal is in each separate property bags
        // It is assumed the property bags are already sorted
        this.numTerminals = ps.size();
        terminals = new TerminalData[] {null, null, null};
        assert numTerminals <= 3;
        for (int k = 1; k <= numTerminals; k++) {
            int k0 = k - 1;
            terminals[k0] = new TerminalData(CgmesNames.TERMINAL, ps.get(k0), context);
        }
        steadyStatePowerFlow = PowerFlow.UNDEFINED;
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
                    context.boundary().addPowerFlowAtNode(nodeId(k), powerFlow(k));
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
            if (voltageLevel(k) == null) {
                missing(String.format("VoltageLevel of terminal %d %s (iidm %s)",
                    k,
                    cgmesVoltageLevelId(k),
                    iidmVoltageLevelId(k)));
                return false;
            }
        }
        return true;
    }

    String nodeIdPropertyName() {
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

    boolean isBoundary(int n) {
        return voltageLevel(n) == null || context.boundary().containsNode(nodeId(n));
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
        return terminals[0].voltageLevel;
    }

    VoltageLevel voltageLevel(int n) {
        return terminals[n - 1].voltageLevel;
    }

    protected Substation substation() {
        String sid = context.cgmes().substation(terminals[0].t, context.nodeBreaker());
        return context.network().getSubstation(context.substationIdMapping().iidm(sid));
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
        switch (context.config().getProfileUsedForInitialStateValues()) {
            case SSH:
                if (steadyStateHypothesisPowerFlow().defined()) {
                    return steadyStateHypothesisPowerFlow();
                }
                if (stateVariablesPowerFlow().defined()) {
                    return stateVariablesPowerFlow();
                }
                break;
            case SV:
                if (stateVariablesPowerFlow().defined()) {
                    return stateVariablesPowerFlow();
                }
                if (steadyStateHypothesisPowerFlow().defined()) {
                    return steadyStateHypothesisPowerFlow();
                }
                break;
        }
        return PowerFlow.UNDEFINED;
    }

    PowerFlow powerFlow(int n) {
        switch (context.config().getProfileUsedForInitialStateValues()) {
            case SSH:
                if (steadyStateHypothesisPowerFlow().defined()) {
                    return steadyStateHypothesisPowerFlow();
                }
                if (stateVariablesPowerFlow(n).defined()) {
                    return stateVariablesPowerFlow(n);
                }
                break;
            case SV:
                if (stateVariablesPowerFlow(n).defined()) {
                    return stateVariablesPowerFlow(n);
                }
                if (steadyStateHypothesisPowerFlow().defined()) {
                    return steadyStateHypothesisPowerFlow();
                }
                break;
        }
        return PowerFlow.UNDEFINED;
    }

    // Terminals

    protected void convertedTerminals(Terminal... ts) {
        assert ts.length == numTerminals;
        for (int k = 0; k < ts.length; k++) {
            int n = k + 1;
            Terminal t = ts[k];
            context.convertedTerminal(terminalId(n), t, n, powerFlow(n));
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
            this.busId = context.namingStrategy().getId("Bus", nodeId);
            if (context.config().convertBoundary()
                && context.boundary().containsNode(nodeId)) {
                cgmesVoltageLevelId = Context.boundaryVoltageLevelId(nodeId);
            } else {
                // cgmesVoltageLevelId may be null if terminal is contained in a Line
                // (happens in boundaries)
                cgmesVoltageLevelId = context.cgmes().voltageLevel(t, context.nodeBreaker());
            }
            if (cgmesVoltageLevelId != null) {
                iidmVoltageLevelId = context.namingStrategy().getId("VoltageLevel",
                    cgmesVoltageLevelId);
                voltageLevel = context.network().getVoltageLevel(iidmVoltageLevelId);
            } else {
                iidmVoltageLevelId = null;
                voltageLevel = null;
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
                .setBus1(t1Connected ? busId1 : null)
                .setBus2(t2Connected ? busId2 : null)
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

    private final TerminalData[] terminals;
    private final PowerFlow steadyStatePowerFlow;
}
