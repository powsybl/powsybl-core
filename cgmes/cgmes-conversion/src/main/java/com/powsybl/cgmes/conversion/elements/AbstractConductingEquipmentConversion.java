/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.cgmes.model.CgmesModel.CgmesTerminal;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * <p>
 * A ConductingEquipment has at least one Terminal. From the Terminal we get either its
 * ConnectivityNode or its TopologicalNode, depending of the conversion context
 */
public abstract class AbstractConductingEquipmentConversion extends AbstractIdentifiedObjectConversion {

    public AbstractConductingEquipmentConversion(
            String type,
            PropertyBag p,
            Conversion.Context context) {
        super(type, p, context);
        numTerminals = 1;
        terminals = new TerminalData[]{null, null, null};
        terminals[0] = new TerminalData(CgmesNames.TERMINAL, p, context);
        equipmentPowerFlow = new PowerFlow(p, "p", "q");
    }

    public AbstractConductingEquipmentConversion(
            String type,
            PropertyBag p,
            Conversion.Context context,
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
        equipmentPowerFlow = PowerFlow.UNDEFINED;
    }

    public AbstractConductingEquipmentConversion(
            String type,
            PropertyBags ps,
            Conversion.Context context) {
        super(type, ps, context);
        // Information about each terminal is in each separate property bags
        // It is assumed the property bags are already sorted
        this.numTerminals = ps.size();
        terminals = new TerminalData[]{null, null, null};
        assert numTerminals <= 3;
        for (int k = 1; k <= numTerminals; k++) {
            int k0 = k - 1;
            terminals[k0] = new TerminalData(CgmesNames.TERMINAL, ps.get(k0), context);
        }
        equipmentPowerFlow = PowerFlow.UNDEFINED;
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

    String nodeId() {
        return context.nodeBreaker()
                ? terminals[0].t.connectivityNode()
                : terminals[0].t.topologicalNode();
    }

    String nodeId(int n) {
        return context.nodeBreaker()
                ? terminals[n - 1].t.connectivityNode()
                : terminals[n - 1].t.topologicalNode();
    }

    boolean isBoundary(int n) {
        return voltageLevel(n) == null || context.boundary().containsNode(nodeId(n));
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

    VoltageLevel voltageLevel() {
        return terminals[0].voltageLevel;
    }

    VoltageLevel voltageLevel(int n) {
        return terminals[n - 1].voltageLevel;
    }

    Substation substation() {
        String sid = terminals[0].t.substation();
        return context.network().getSubstation(context.substationIdMapping().iidm(sid));
    }

    PowerFlow terminalPowerFlow() {
        return terminals[0].t.flow();
    }

    public PowerFlow terminalPowerFlow(int n) {
        return terminals[n - 1].t.flow();
    }

    PowerFlow equipmentPowerFlow() {
        return equipmentPowerFlow;
    }

    PowerFlow powerFlow() {
        // Could come either from terminal data or from property bag
        return terminalPowerFlow().defined() ? terminalPowerFlow() : equipmentPowerFlow();
    }

    PowerFlow powerFlow(int n) {
        // Could come either from terminal data or from property bag
        return terminalPowerFlow(n).defined() ? terminalPowerFlow(n) : equipmentPowerFlow();
    }

    void convertedTerminal(String terminalId, Terminal t, int n, PowerFlow f) {
        // Record the mapping between CGMES and IIDM terminals
        context.terminalMapping().add(terminalId, t, n);
        // Update the power flow at terminal
        if (f.defined()) {
            t.setP(f.p());
            t.setQ(f.q());
        }
    }

    void convertedTerminals(Terminal... ts) {
        assert ts.length == numTerminals;
        for (int k = 0; k < ts.length; k++) {
            int n = k + 1;
            Terminal t = ts[k];
            convertedTerminal(terminalId(n), t, n, powerFlow(n));
        }
    }

    private final int numTerminals;

    static class TerminalData {
        private final CgmesTerminal t;
        private final String busId;
        private final String cgmesVoltageLevelId;
        private final String iidmVoltageLevelId;
        private final VoltageLevel voltageLevel;

        TerminalData(String terminalPropertyName, PropertyBag p, Conversion.Context context) {
            t = context.cgmes().terminal(p.getId(terminalPropertyName));
            String nodeId = context.nodeBreaker() ? t.connectivityNode() : t.topologicalNode();
            this.busId = context.namingStrategy().getId("Bus", nodeId);
            if (context.config().convertBoundary()
                    && context.boundary().containsNode(nodeId)) {
                cgmesVoltageLevelId = context.boundaryVoltageLevelId(nodeId);
            } else {
                // cgmesVoltageLevelId may be null if terminal is contained in a Line
                // (happens in boundaries)
                cgmesVoltageLevelId = t.voltageLevel();
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

    private final TerminalData[] terminals;
    private final PowerFlow equipmentPowerFlow;
}
